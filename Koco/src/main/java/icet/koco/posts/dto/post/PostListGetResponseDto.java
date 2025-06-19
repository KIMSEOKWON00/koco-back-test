package icet.koco.posts.dto.post;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostListGetResponseDto {
    private Long nextCursorId;
    private boolean hasNext;
    private List<PostDetailDto> posts;

    @Getter
    @Builder
    public static class PostDetailDto {
        private Long postId;
        private Long problemNumber;
        private String title;
        private LocalDateTime createdAt;
        private List<CategoryDto> categories;
        private AuthorDto author;
        private Integer commentCount;
        private Integer likeCount;
    }

    @Getter
    @Builder
    public static class CategoryDto {
        private Long categoryId;
        private String categoryName;
    }

    @Getter
    @Builder
    public static class AuthorDto {
        private Long userId;
        private String nickname;
        private String imgUrl;
    }

}