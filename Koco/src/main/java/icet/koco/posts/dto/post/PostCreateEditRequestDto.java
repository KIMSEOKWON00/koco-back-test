package icet.koco.posts.dto.post;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostCreateEditRequestDto {
    private Long problemNumber;
    private String title;
    private String content;
    private List<String> category;
}
