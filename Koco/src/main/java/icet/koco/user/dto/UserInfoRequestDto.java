package icet.koco.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoRequestDto {
    private String nickname;
    private String statusMsg;
    private String profileImgUrl;
}
