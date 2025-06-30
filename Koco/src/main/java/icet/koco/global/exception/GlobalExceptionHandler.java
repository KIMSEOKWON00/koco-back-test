package icet.koco.global.exception;

import icet.koco.enums.ApiResponseCode;
import icet.koco.global.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(IllegalArgumentException ex) {
        return buildErrorResponse(ApiResponseCode.BAD_REQUEST, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AlreadyLikedException.class)
    public ResponseEntity<ApiResponse<?>> handleAlreadyLikedException(AlreadyLikedException ex) {
        return buildErrorResponse(ApiResponseCode.ALREADY_EXISTS, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorized(BadRequestException ex) {
        return buildErrorResponse(ApiResponseCode.BAD_REQUEST, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorized(UnauthorizedException ex) {
        return buildErrorResponse(ApiResponseCode.UNAUTHORIZED, ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<?>> handleForbidden(ForbiddenException ex) {
        return buildErrorResponse(ApiResponseCode.FORBIDDEN, ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse(ApiResponseCode.NOT_FOUND, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleInternal(Exception ex, HttpServletRequest request) {
        if (request.getRequestURI().contains("/v3/api-docs")) {
            return ResponseEntity.ok().build();
        }

        ex.printStackTrace();

        return buildErrorResponse(ApiResponseCode.INTERNAL_SERVER_ERROR, "서버에서 에러가 발생하였습니다", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return buildErrorResponse(ApiResponseCode.MAX_UPLOAD_SIZE_EXCEEDED, "파일 크기가 제한을 초과했습니다.", HttpStatus.PAYLOAD_TOO_LARGE);
    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponse(ApiResponseCode code, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.fail(code, message));
    }
}
