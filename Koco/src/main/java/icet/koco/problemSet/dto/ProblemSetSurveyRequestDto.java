package icet.koco.problemSet.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class ProblemSetSurveyRequestDto {
    private Long problemSetId;
    private List<ProblemSurveyRequestDto> responses;
}