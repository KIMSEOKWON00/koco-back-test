package icet.koco.problemRecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RecommendationRequestDto {
	List<List<Long>> recommendations;
}
