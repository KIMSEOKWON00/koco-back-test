package icet.koco.problemSet.dto;

import icet.koco.problemSet.entity.Problem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProblemDto {
    private Long problemNumber;  // 백준 문제 번호
    private Long problemId;      // 서비스 내부 문제 ID
    private String title;
    private String tier;
    private String bojUrl;

    public static ProblemDto from(Problem problem) {
        return ProblemDto.builder()
            .problemNumber(problem.getNumber())
            .problemId(problem.getId())
            .title(problem.getTitle())
            .tier(problem.getTier())
            .bojUrl(problem.getBojUrl())
            .build();
    }
}
