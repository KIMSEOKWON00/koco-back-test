package icet.koco.problemSet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class PreviousProblemListResponseDto {
	List<Long> previousProblemIds;
	LocalDate previousIssueDate;
}
