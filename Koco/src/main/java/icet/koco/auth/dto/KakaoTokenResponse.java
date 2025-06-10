package icet.koco.auth.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoTokenResponse {
    private String access_token;
    private String refresh_token;
}