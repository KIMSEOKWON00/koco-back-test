package icet.koco.posts.dto.like;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeResponseDto {
    private Long postId;
    private boolean liked;
    private Integer likeCount;
}
