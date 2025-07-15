package icet.koco.problemRecommendation.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class RecommendedProblemResponseDto {
	private LocalDate date;
	private List<RecommendedProblemDto> problems;

	@Data
	@Builder
	public static class RecommendedProblemDto {
		private Long problemId;
		private Long problemNumber;
		private String title;
		private String tier;
		private String bojUrl;
	}

}
