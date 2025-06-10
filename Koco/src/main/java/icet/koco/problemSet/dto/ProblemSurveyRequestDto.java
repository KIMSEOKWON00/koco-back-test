package icet.koco.problemSet.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProblemSurveyRequestDto {
    private Long problemId;
    private boolean isSolved;
    private String difficultyLevel;
}