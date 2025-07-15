package icet.koco.posts.dto.post;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateEditRequestDto {
    private Long problemNumber;
    private String title;
    private String content;
    private List<String> category;
}
