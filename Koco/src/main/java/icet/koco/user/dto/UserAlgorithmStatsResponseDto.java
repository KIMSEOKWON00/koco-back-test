package icet.koco.user.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserAlgorithmStatsResponseDto {
    private List<CategoryStat> studyStats;

    @Getter
    @Builder
    public static class CategoryStat {
        private Long categoryId;
        private String categoryName;
        private Integer correctRate;
    }
}
