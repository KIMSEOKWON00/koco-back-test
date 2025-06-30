package icet.koco.auth.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// 응답 DTO
@Getter
@Setter
@Builder
public class RefreshResponse {
    private String code;
    private String message;
    private Map<String, String> data;
}