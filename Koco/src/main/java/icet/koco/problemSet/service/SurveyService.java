package icet.koco.problemSet.service;

import icet.koco.enums.DifficultyLevel;
import icet.koco.global.exception.ResourceNotFoundException;
import icet.koco.problemSet.dto.ProblemSetSurveyRequestDto;
import icet.koco.problemSet.dto.ProblemSurveyRequestDto;
import icet.koco.problemSet.dto.SurveyResponseDto;
import icet.koco.problemSet.entity.Problem;
import icet.koco.problemSet.entity.ProblemSet;
import icet.koco.problemSet.entity.Survey;
import icet.koco.problemSet.repository.ProblemRepository;
import icet.koco.problemSet.repository.ProblemSetProblemRepository;
import icet.koco.problemSet.repository.ProblemSetRepository;
import icet.koco.problemSet.repository.SurveyRepository;
import icet.koco.user.entity.User;
import icet.koco.user.repository.UserRepository;
import icet.koco.user.service.UserAlgorithmStatsService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyService {

    private final UserAlgorithmStatsService userAlgorithmStatsService;

    private final UserRepository userRepository;
    private final ProblemSetRepository problemSetRepository;
    private final ProblemRepository problemRepository;
    private final SurveyRepository surveyRepository;
    private final ProblemSetProblemRepository problemSetProblemRepository;

    @Transactional
    public SurveyResponseDto submitSurvey(Long userId, ProblemSetSurveyRequestDto requestDto) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        ProblemSet problemSet = problemSetRepository.findById(requestDto.getProblemSetId())
            .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 문제집입니다."));

        List<Survey> surveysToSave = new ArrayList<>();

        for (ProblemSurveyRequestDto response : requestDto.getResponses()) {
            Problem problem = problemRepository.findById(response.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException(">>>>> 문제 ID " + response.getProblemId() + "가 존재하지 않습니다."));

            // 문제 Id가 해당 ProblemSet에 있는지 검증
            boolean exists = problemSetProblemRepository.existsByProblemSetIdAndProblemId(problemSet.getId(), problem.getId());
            if (!exists) {
                throw new ResourceNotFoundException("문제 ID " + problem.getId() + "는 문제집 " + problemSet.getId() + "에 포함되어 있지 않습니다.");
            }

            Survey survey = Survey.builder()
                .user(user)
                .problemSet(problemSet)
                .problem(problem)
                .isSolved(response.getIsSolved())
                .difficultyLevel(DifficultyLevel.valueOf(response.getDifficultyLevel()))
                .answeredAt(LocalDateTime.now())
                .build();

            surveysToSave.add(survey);
        }

        // 디비에 넣기 한 번에 수행
        List<Survey> savedSurveys = surveyRepository.saveAll(surveysToSave);

        // 사용자 알고리즘 통계 업데이트
        userAlgorithmStatsService.updateStatsFromSurveys(userId);

        List<Long> savedIds = savedSurveys.stream()
            .map(Survey::getId)
            .toList();

        return SurveyResponseDto.builder()
            .code("SURVEY_CREATED")
            .message("출제 문제집에 대한 설문응답이 성공적으로 생성되었습니다.")
            .data(Map.of("surveyId", savedIds))
            .build();
    }
}
