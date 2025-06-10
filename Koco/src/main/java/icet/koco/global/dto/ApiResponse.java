package icet.koco.global.dto;


import icet.koco.enums.ApiResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(ApiResponseCode code, String message, T data) {
        return ApiResponse.<T>builder()
                .code(code.getCode())
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> fail(ApiResponseCode code, String message) {
        return ApiResponse.<T>builder()
                .code(code.getCode())
                .message(message)
                .data(null)
                .build();
    }
}
