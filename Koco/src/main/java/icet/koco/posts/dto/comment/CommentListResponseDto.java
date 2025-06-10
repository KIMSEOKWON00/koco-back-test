package icet.koco.posts.dto.comment;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CommentListResponseDto {
    private Long postId;
    private Long nextCursorId;
    private boolean hasNext;
    private List<CommentDetailDto> comments;

    @Builder
    @Getter
    public static class CommentDetailDto {
        private Long id;
        private String comment;
        private AuthorDto author;
        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    public static class AuthorDto {
        private Long userId;
        private String nickname;
        private String imgUrl;
    }
}
