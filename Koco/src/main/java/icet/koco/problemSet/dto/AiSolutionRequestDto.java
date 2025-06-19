package icet.koco.problemSet.dto;

import lombok.Getter;

@Getter
public class AiSolutionRequestDto {
    private Long problemNumber;
    private ProblemCheck problem_check;
    private String problem_solving;
    private SolutionCode solution_code;

    @Getter
    public static class ProblemCheck {
        private String problem_description;
        private String algorithm;
    }

    @Getter
    public static class SolutionCode {
        private String python;
        private String cpp;
        private String java;
    }
}
