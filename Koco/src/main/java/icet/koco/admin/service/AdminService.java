package icet.koco.admin.service;

import icet.koco.admin.dto.TodayProblemSetRequestDto;
import icet.koco.enums.ErrorMessage;
import icet.koco.global.exception.BadRequestException;
import icet.koco.problemSet.entity.*;
import icet.koco.problemSet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final ProblemRepository problemRepository;
    private final ProblemSetRepository problemSetRepository;
    private final ProblemSetProblemRepository problemSetProblemRepository;
    private final ProblemSetSolutionRespository problemSetSolutionRepository;
    private final SolutionRepository solutionRepository;

    public Long createTodayProblemset(TodayProblemSetRequestDto requestDto) {
        // 오늘의 문제집 만들기
        ProblemSet problemSet = ProblemSet.builder()
                .createdAt(LocalDate.now())
                .build();

        problemSetRepository.save(problemSet);

        // number(백준 문제 번호)로 problem 조회
        List<Problem> problems = problemRepository.findByNumberIn(requestDto.getProblemNumbers());

        if (problems.size() != requestDto.getProblemNumbers().size()) {
            throw new BadRequestException(ErrorMessage.INVALID_PROBLEM_INCLUDED);
        }

        // problem_set_problem 매핑
        List<ProblemSetProblem> mappings = problems.stream()
                .map(p -> ProblemSetProblem.builder()
                        .problemSet(problemSet)
                        .problem(p)
                        .build()
                )
                .collect(Collectors.toList());
        problemSetProblemRepository.saveAll(mappings);

        // 해당 problem들의 solution 조회
        List<Long> problemIds = problems.stream()
                .map(Problem::getId)
                .collect(Collectors.toList());

        List<Solution> solutions = solutionRepository.findByProblemIdIn(problemIds);

        // problem_set_solution 매핑
        List<ProblemSetSolution> solutionMappings = solutions.stream()
                .map(s -> ProblemSetSolution.builder()
                        .problemSet(problemSet)
                        .solution(s)
                        .build()
                )
                .collect(Collectors.toList());
        problemSetSolutionRepository.saveAll(solutionMappings);

        return problemSet.getId();
    }
}
