package icet.koco.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponseDto {
    private Long userId;
    private String nickname;
    private String statusMsg;
    private String profileImgUrl;
}
