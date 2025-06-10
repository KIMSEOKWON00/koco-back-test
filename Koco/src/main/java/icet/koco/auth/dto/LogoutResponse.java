package icet.koco.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LogoutResponse {
    private String code;
    private String message;
    private Data data;

    @Builder
    public LogoutResponse(String code, String message, String redirectUrl) {
        this.code = code;
        this.message = message;
        this.data = new Data(redirectUrl);
    }

    @Getter
    public static class Data {
        private final String redirectUrl;

        public Data(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }
    }
}
