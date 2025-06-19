package icet.koco.posts.dto.post;

import icet.koco.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthorDto {
    private Long userId;
    private String nickname;
    private String imgUrl;

    public static AuthorDto from(User user) {
        return AuthorDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .imgUrl(user.getProfileImgUrl())
                .build();
    }
}
