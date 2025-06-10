package icet.koco.problemSet.service;

import icet.koco.global.exception.ForbiddenException;
import icet.koco.global.exception.ResourceNotFoundException;
import icet.koco.problemSet.dto.ProblemSetResponseDto;
import icet.koco.problemSet.dto.ProblemDto;
import icet.koco.problemSet.dto.ProblemSolutionResponseDto;
import icet.koco.problemSet.entity.Problem;
import icet.koco.problemSet.entity.ProblemSet;
import icet.koco.problemSet.entity.ProblemSetProblem;
import icet.koco.problemSet.entity.Solution;
import icet.koco.problemSet.repository.ProblemRepository;
import icet.koco.problemSet.repository.ProblemSetProblemRepository;
import icet.koco.problemSet.repository.ProblemSetRepository;
import icet.koco.problemSet.repository.SolutionRepository;
import icet.koco.problemSet.repository.SurveyRepository;
import icet.koco.user.entity.User;
import icet.koco.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProblemSetService {

    private final ProblemSetRepository problemSetRepository;
    private final ProblemRepository problemRepository;
    private final SolutionRepository solutionRepository;
    private final ProblemSetProblemRepository problemSetProblemRepository;
    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProblemSetResponseDto getProblemSetByDate(Long userId, LocalDate date) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new ForbiddenException("사용자를 찾을 수 없습니다."));

        ProblemSet problemSet = problemSetRepository.findByCreatedAt(date)
            .orElseThrow(() -> new ForbiddenException("해당 날짜에 출제된 문제집을 찾을 수 없습니다."));

        // N+1 방지: JOIN FETCH
        List<ProblemSetProblem> mappings = problemSetProblemRepository.findWithProblemsByProblemSet(problemSet);

        List<Problem> problems = mappings.stream()
            .map(ProblemSetProblem::getProblem)
            .toList();

        List<Long> problemIds = problems.stream()
            .map(Problem::getId)
            .toList();

        // 리스트로 한번만 조회
        List<Long> answeredProblemIds = surveyRepository.findProblemIdsByUserAndProblemSet(userId, problemSet.getId());

        boolean isAnswered = (answeredProblemIds.size() == problems.size()) && answeredProblemIds.containsAll(problemIds);

        List<ProblemDto> problemDtos = problems.stream()
            .map(ProblemDto::from)
            .toList();

        return ProblemSetResponseDto.builder()
            .date(problemSet.getCreatedAt())
            .problemSetId(problemSet.getId())
            .isAnswered(isAnswered)
            .problems(problemDtos)
            .build();
    }

    @Transactional(readOnly = true)
    public ProblemSolutionResponseDto getProblemSolution(Long problemNumber) {
        Problem problem = problemRepository.findByNumber(problemNumber)
            .orElseThrow(() -> new ResourceNotFoundException("해당 문제를 찾을 수 없습니다."));

        Solution solution = solutionRepository.findByProblem(problem)
            .orElseThrow(() -> new ResourceNotFoundException("해당 문제에 대한 해설이 없습니다."));

        return ProblemSolutionResponseDto.builder()
            .bojUrl(problem.getBojUrl())
            .problemNumber(problem.getNumber())
            .tier(problem.getTier())
            .title(problem.getTitle())
            .timeLimit(problem.getTimeLimit())
            .memoryLimit(problem.getMemoryLimit())
            .submissionCnt(problem.getSubmissionCnt())
            .answerCnt(problem.getAnswerCnt())
            .correctPplCnt(problem.getCorrectPplCnt())
            .correctRate(problem.getCorrectRate())
            .description(problem.getDescription())
            .inputDescription(problem.getInputDescription())
            .outputDescription(problem.getOutputDescription())
            .inputExample(problem.getInputExample())
            .outputExample(problem.getOutputExample())
            .problemCheck(solution.getDescription())
            .algorithm(solution.getAlgorithm())
            .problemSolving(solution.getProblemSolving())
            .solutionCode(Map.of(
                "cpp", solution.getCodeCpp(),
                "java", solution.getCodeJava(),
                "python", solution.getCodePy()
            ))
            .build();
    }
}

