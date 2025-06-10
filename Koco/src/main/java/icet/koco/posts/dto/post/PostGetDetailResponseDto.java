package icet.koco.posts.dto.post;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostGetDetailResponseDto {
    private Long postId;
    private String title;
    private LocalDateTime createdAt;
    private List<CategoryDto> categories;
    private String content;
    private AuthorDto author;
    private Integer commentCount;
    private Integer likeCount;
    private boolean liked;      // 로그인된 유저가 좋아요 눌렀는지 여부

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryDto {
        private Long categoryId;
        private String categoryName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorDto {
        private Long userId;
        private String nickname;
        private String imgUrl;
    }
}


