package icet.koco.global.exception;

import icet.koco.global.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return buildErrorResponse("BAD_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 401
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return buildErrorResponse("UNAUTHORIZED", ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // 403
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        return buildErrorResponse("FORBIDDEN", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    // 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse("NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 500 (그 외 모든 예외)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternal(Exception ex, HttpServletRequest request) {
        // Swagger 요청은 예외를 아예 응답 없이 무시함
        if (request.getRequestURI().contains("/v3/api-docs")) {
            return ResponseEntity.ok().build(); // 예외를 던지지 말고 200 OK로 응답
        }

        return buildErrorResponse("INTERNAL_SERVER_ERROR", "서버에서 에러가 발생하였습니다", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 파일 업로드 크기 5MB로 제한
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("파일 크기가 제한을 초과했습니다.");
    }

    // 공통 응답 생성 함수
    private ResponseEntity<ErrorResponse> buildErrorResponse(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status)
            .body(ErrorResponse.builder()
                .code(code)
                .message(message)
                .data(null)
                .build());
    }
}
