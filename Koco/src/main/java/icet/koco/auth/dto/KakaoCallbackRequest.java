package icet.koco.auth.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoCallbackRequest {
    private String code;
}