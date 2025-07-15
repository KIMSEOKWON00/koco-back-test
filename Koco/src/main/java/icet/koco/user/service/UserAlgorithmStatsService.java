package icet.koco.user.service;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import icet.koco.enums.ErrorMessage;
import icet.koco.global.exception.ResourceNotFoundException;
import icet.koco.problemSet.entity.Category;
import icet.koco.problemSet.repository.CategoryRepository;
import icet.koco.problemSet.repository.ProblemCategoryRepository;
import icet.koco.problemSet.repository.SurveyRepository;
import icet.koco.user.entity.User;
import icet.koco.user.entity.UserAlgorithmStats;
import icet.koco.user.repository.UserAlgorithmStatsRepository;
import icet.koco.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static icet.koco.problemSet.entity.QProblemCategory.problemCategory;
import static icet.koco.problemSet.entity.QSurvey.survey;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAlgorithmStatsService {

    private final UserAlgorithmStatsRepository statsRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final UserAlgorithmStatsRepository userAlgorithmStatsRepository;
    private final SurveyRepository surveyRepository;
    private final ProblemCategoryRepository problemCategoryRepository;

	private final JPAQueryFactory queryFactory;

	// record DTO 클래스
	public static record SurveyInfo(Long problemId, Boolean isSolved) {}

	public static record ProblemCategoryInfo(Long problemId, Long categoryId) {}

	// updateStatsFromSurveys - 비선형 정규화 (x^0.6 방식) 적용
	@Transactional
	public void updateStatsFromSurveys(Long userId) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

		userAlgorithmStatsRepository.deleteByUserId(userId);

		// Survey 데이터 추출 (문제 ID + 정답 여부)
		List<SurveyInfo> surveyInfos = queryFactory
			.select(Projections.constructor(SurveyInfo.class,
				survey.problem.id,
				survey.isSolved
			))
			.from(survey)
			.where(survey.user.id.eq(userId))
			.fetch();

		if (surveyInfos.isEmpty()) {
			log.info(">>>>> 유저 {} 의 설문 데이터 없음, 통계 생략", userId);
			return;
		}

		// 문제 ID → 정답 여부 Map
		Map<Long, Boolean> problemSolvedMap = surveyInfos.stream()
			.collect(Collectors.toMap(SurveyInfo::problemId, SurveyInfo::isSolved));

		// 문제 ID 목록
		List<Long> problemIds = new ArrayList<>(problemSolvedMap.keySet());

		// 문제 ID → 카테고리 ID 추출 (QueryDSL 으로 수정)
		List<ProblemCategoryInfo> categoryInfos = queryFactory
			.select(Projections.constructor(ProblemCategoryInfo.class,
				problemCategory.problem.id,
				problemCategory.category.id
			))
			.from(problemCategory)
			.where(problemCategory.problem.id.in(problemIds))
			.fetch();

		// 문제 → 카테고리 매핑
		Map<Long, List<Long>> problemToCategoryMap = categoryInfos.stream()
			.collect(Collectors.groupingBy(
				ProblemCategoryInfo::problemId,
				Collectors.mapping(ProblemCategoryInfo::categoryId, Collectors.toList())
			));

		// 카테고리별 정답 수 계산
		Map<Long, Integer> categoryCorrectCount = new HashMap<>();
		int maxCorrect = 0;

		for (Map.Entry<Long, Boolean> entry : problemSolvedMap.entrySet()) {
			if (!Boolean.TRUE.equals(entry.getValue())) continue;

			List<Long> categoryIds = problemToCategoryMap.getOrDefault(entry.getKey(), List.of());
			for (Long categoryId : categoryIds) {
				int newCount = categoryCorrectCount.getOrDefault(categoryId, 0) + 1;
				categoryCorrectCount.put(categoryId, newCount);
				maxCorrect = Math.max(maxCorrect, newCount);
			}
		}

		// 카테고리 일괄 조회 (N+1 방지)
		List<Category> categories = categoryRepository.findAllById(categoryCorrectCount.keySet());
		Map<Long, Category> categoryMap = categories.stream()
			.collect(Collectors.toMap(Category::getId, c -> c));

		// 정규화 후 저장
		List<UserAlgorithmStats> statsToSave = new ArrayList<>();
		for (Map.Entry<Long, Integer> entry : categoryCorrectCount.entrySet()) {
			Long categoryId = entry.getKey();
			int correct = entry.getValue();

			double normalized = (Math.pow(correct, 0.6) / Math.pow(maxCorrect, 0.6)) * 100.0;
			int capped = Math.min(95, (int) Math.round(normalized));

			Category category = categoryMap.get(categoryId);
			if (category == null) {
				throw new ResourceNotFoundException(ErrorMessage.CATEGORY_NOT_FOUND);
			}

			statsToSave.add(UserAlgorithmStats.builder()
				.user(user)
				.category(category)
				.correctRate(capped)
				.build());
		}
		userAlgorithmStatsRepository.saveAll(statsToSave);
	}
}
