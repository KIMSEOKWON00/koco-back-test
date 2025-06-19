package icet.koco.problemSet.service;

import icet.koco.problemSet.dto.AiSolutionRequestDto;
import icet.koco.problemSet.entity.Problem;
import icet.koco.problemSet.entity.Solution;
import icet.koco.problemSet.repository.ProblemRepository;
import icet.koco.problemSet.repository.SolutionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SolutionService {

    private final SolutionRepository solutionRepository;
    private final ProblemRepository problemRepository;

    /**
     * AI 서버로부터 해설 받아오기
     * @param aiSolutionRequestDto
     */
    public void saveFromAi(AiSolutionRequestDto aiSolutionRequestDto) {
        // 백준 번호로 내부 problemId 조회
        Problem problem = problemRepository.findByNumber(aiSolutionRequestDto.getProblemNumber())
            .orElseThrow(() -> new IllegalArgumentException(
                "해당 문제 번호를 가진 Problem이 없습니다: " + aiSolutionRequestDto.getProblemNumber()));

        // 문제에 대한 해설이 이미 존재하는지 확인
        if (solutionRepository.existsByProblem(problem)) {
            throw new IllegalArgumentException("이미 해당 문제에 대한 해설이 존재합니다.");
        }

        // Solution 저장
        Solution solution = Solution.builder()
            .problem(problem)  // 연관관계 설정
            .description(aiSolutionRequestDto.getProblem_check().getProblem_description())
            .algorithm(aiSolutionRequestDto.getProblem_check().getAlgorithm())
            .problemSolving(aiSolutionRequestDto.getProblem_solving())
            .codeCpp(aiSolutionRequestDto.getSolution_code().getCpp())
            .codeJava(aiSolutionRequestDto.getSolution_code().getJava())
            .codePy(aiSolutionRequestDto.getSolution_code().getPython())
            .createdAt(LocalDateTime.now())
            .build();

        solutionRepository.save(solution);
    }
}