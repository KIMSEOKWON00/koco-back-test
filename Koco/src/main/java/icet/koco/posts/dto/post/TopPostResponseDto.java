package icet.koco.posts.dto.post;

import icet.koco.posts.entity.Post;
import lombok.*;
import icet.koco.posts.dto.post.AuthorDto;
import icet.koco.posts.dto.post.CategoryDto;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TopPostResponseDto {
    private Long postId;
    private String title;
    private Long problemNumber;
    private int likeCount;
    private String createdAt;
    private List<CategoryDto> categories;
    private AuthorDto author;
    private int commentCount;

    public static TopPostResponseDto from(Post post) {
        return TopPostResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .problemNumber(post.getProblemNumber())
                .likeCount(post.getLikeCount())
                .createdAt(post.getCreatedAt().toString())
                .categories(post.getPostCategories().stream()
                        .map(pc -> CategoryDto.from(pc.getCategory()))
                        .toList())
                .author(AuthorDto.from(post.getUser()))
                .commentCount(post.getCommentCount())
                .build();
    }
}
