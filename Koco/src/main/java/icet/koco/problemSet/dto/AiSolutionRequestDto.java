package icet.koco.problemSet.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AiSolutionRequestDto {
    private Long problemNumber;
    private ProblemCheck problem_check;
    private String problem_solving;
    private SolutionCode solution_code;

	@Builder
    @Getter
    public static class ProblemCheck {
        private String problem_description;
        private String algorithm;
    }
	@Builder
    @Getter
    public static class SolutionCode {
        private String python;
        private String cpp;
        private String java;
    }
}
