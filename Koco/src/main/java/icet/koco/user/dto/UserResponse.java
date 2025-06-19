package icet.koco.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private String code;
    private String message;

    public static UserResponse ofSuccess() {
        return new UserResponse("USER_UPDATE_SUCCESS", "사용자 정보 업데이트 성공");
    }
}
