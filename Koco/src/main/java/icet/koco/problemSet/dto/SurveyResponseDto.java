package icet.koco.problemSet.dto;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SurveyResponseDto {
    private String code;
    private String message;
    private Map<String, List<Long>> data;
}