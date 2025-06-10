package icet.koco.problemSet.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProblemSetResponseDto {
    private LocalDate date;
    private Long problemSetId;

    @JsonIgnore
    private boolean isAnswered;

    @JsonProperty("isAnswered")
    public boolean getIsAnswered() {
        return isAnswered;
    }
    private List<ProblemDto> problems;
}
