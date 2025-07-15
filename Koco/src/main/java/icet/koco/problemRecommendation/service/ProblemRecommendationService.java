package icet.koco.problemRecommendation.service;

import icet.koco.enums.ErrorMessage;
import icet.koco.global.exception.ResourceNotFoundException;
import icet.koco.problemRecommendation.dto.RecommendationRequestDto;
import icet.koco.problemRecommendation.dto.RecommendedProblemResponseDto;
import icet.koco.problemRecommendation.dto.RecommendedProblemResponseDto.RecommendedProblemDto;
import icet.koco.problemRecommendation.entity.ProblemRecommendation;
import icet.koco.problemRecommendation.repository.ProblemRecommendationRepository;
import icet.koco.problemSet.entity.Problem;
import icet.koco.problemSet.entity.Survey;
import icet.koco.problemSet.repository.ProblemRepository;
import icet.koco.problemSet.repository.ProblemSetRepository;
import icet.koco.problemSet.repository.SurveyRepository;
import icet.koco.user.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemRecommendationService {

	private final ProblemRepository problemRepository;
	private final ProblemRecommendationRepository problemRecommendationRepository;
	private final ProblemSetRepository problemSetRepository;
	private final SurveyRepository surveyRepository;
	private final RedisTemplate<String, Object> redisTemplate;
	private final UserRepository userRepository;

	public void saveRecommendations(RecommendationRequestDto dto) {
		List<ProblemRecommendation> recommendations = new ArrayList<>();
		LocalDate now = LocalDate.now();
		ProblemRecommendation.Type[] types = ProblemRecommendation.Type.values(); // TT, FT, TF, FF

		for (int i = 0; i < dto.getRecommendations().size() && i < types.length; i++) {
			List<Long> pair = dto.getRecommendations().get(i);

			if (pair.size() != 2) continue;

			ProblemRecommendation rec = ProblemRecommendation.builder()
				.type(types[i]) // TT → FT → TF → FF 순서 보장
				.problem1Id(pair.get(0))
				.problem2Id(pair.get(1))
				.createdAt(now)
				.build();

			recommendations.add(rec);
		}

		problemRecommendationRepository.saveAll(recommendations);
	}

	public RecommendedProblemResponseDto getRecommendedProblems(Long userId, LocalDate date) {
		// 유저 확인
		userRepository.findByIdAndDeletedAtIsNull(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

		String redisKey = "recommendation:" + userId + ":" + date;

		// 캐시 확인
		Object cachedObj = redisTemplate.opsForValue().get(redisKey);
		if (cachedObj instanceof RecommendedProblemResponseDto cached) {
			return cached;
		}

		// 기준 날짜 계산 (전날 문제집)
		LocalDate previousDay = getPreviousIssueDate(date);

		// 문제집 문제 ID 조회
		List<Long> problemIds = problemSetRepository.findProblemIdsByProblemSetCreatedAt(previousDay);
		if (problemIds.isEmpty()) {
			throw new ResourceNotFoundException(ErrorMessage.PROBLEM_SET_NOT_FOUND);
		}

		// 설문 조회
		List<Survey> surveys = surveyRepository.findByUserIdAndProblemIdInAndProblemSetCreatedAt(
			userId, problemIds, previousDay);

		// 설문 결과가 없는 경우에는 과거 캐싱된 추천 중 가장 가까운 날짜 반환
		if (surveys.isEmpty()) {
			for (int i = 1; i <= 7; i++) { // 최대 7일 전까지 확인
				LocalDate pastDate = date.minusDays(i);
				String pastKey = "recommendation:" + userId + ":" + pastDate;
				Object fallback = redisTemplate.opsForValue().get(pastKey);
				if (fallback instanceof RecommendedProblemResponseDto cachedPast) {
					return cachedPast;
				}
			}
			throw new ResourceNotFoundException("최근 설문 기반 추천 내역이 없습니다.");
		}

		// 설문 결과 → solvedMap 구성
		Map<Long, Boolean> solvedMap = surveys.stream()
			.collect(Collectors.toMap(s -> s.getProblem().getId(), Survey::isSolved));

		// 유저 설문 기반 type 조합
		Set<String> userTypes = new HashSet<>();
		for (int i = 0; i < problemIds.size(); i++) {
			for (int j = i + 1; j < problemIds.size(); j++) {
				Boolean s1 = solvedMap.get(problemIds.get(i));
				Boolean s2 = solvedMap.get(problemIds.get(j));
				if (s1 == null || s2 == null) continue;

				String type = (s1 ? "T" : "F") + (s2 ? "T" : "F");
				userTypes.add(type);
			}
		}

		// 추천쌍 필터
		List<ProblemRecommendation> recs = problemRecommendationRepository.findAllByCreatedAtDate(date);
		if (recs.isEmpty()) {
			throw new ResourceNotFoundException(ErrorMessage.RECOMMENDED_PROBLEM_NOT_FOUND);
		}

		Set<Long> problemIdSet = new HashSet<>();
		for (ProblemRecommendation r : recs) {
			if (userTypes.contains(r.getType().name())) {
				problemIdSet.add(r.getProblem1Id());
				problemIdSet.add(r.getProblem2Id());
			}
		}

		// 문제 정보 조회
		List<Problem> problems = problemRepository.findAllById(problemIdSet);
		List<RecommendedProblemDto> dtoList = problems.stream()
			.map(p -> RecommendedProblemDto.builder()
				.problemId(p.getId())
				.problemNumber(p.getNumber())
				.title(p.getTitle())
				.tier(p.getTier())
				.bojUrl(p.getBojUrl())
				.build())
			.toList();

		RecommendedProblemResponseDto responseDto = RecommendedProblemResponseDto.builder()
			.date(date)
			.problems(dtoList)
			.build();

		// 캐싱
		redisTemplate.opsForValue().set(redisKey, responseDto, Duration.ofDays(2));
		return responseDto;
	}



	public LocalDate getPreviousIssueDate(LocalDate baseDate) {
		LocalDate date = baseDate.minusDays(1);
		DayOfWeek day = date.getDayOfWeek();
		if (day == DayOfWeek.SUNDAY) return date.minusDays(2);
		if (day == DayOfWeek.SATURDAY) return date.minusDays(1);
		return date;
	}

}
