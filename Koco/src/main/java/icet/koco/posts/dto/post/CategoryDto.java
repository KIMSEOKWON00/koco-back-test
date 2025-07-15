package icet.koco.posts.dto.post;

import icet.koco.problemSet.entity.Category;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long categoryId;
    private String categoryName;

    public static CategoryDto from(Category category) {
        return CategoryDto.builder()
                .categoryId(category.getId())
                .categoryName(category.getName())
                .build();
    }
}