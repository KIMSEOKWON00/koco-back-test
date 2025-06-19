package icet.koco.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class UserCategoryStatDto implements UserCategoryStatProjection {
    private Long categoryId;
    private String categoryName;
    private Integer correctRate;

    @Override
    public Long getCategoryId() {
        return categoryId;
    }

    @Override
    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public Integer getCorrectRate() {
        return correctRate;
    }
}
