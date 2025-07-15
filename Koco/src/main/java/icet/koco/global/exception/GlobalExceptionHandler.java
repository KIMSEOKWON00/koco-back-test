package icet.koco.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import icet.koco.enums.ApiResponseCode;
import icet.koco.global.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.io.PrintWriter;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final ObjectMapper objectMapper;

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<?>> handleBadRequest(IllegalArgumentException ex) {
		return buildErrorResponse(ApiResponseCode.BAD_REQUEST, ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(AlreadyLikedException.class)
	public ResponseEntity<ApiResponse<?>> handleAlreadyLikedException(AlreadyLikedException ex) {
		return buildErrorResponse(ApiResponseCode.ALREADY_EXISTS, ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiResponse<?>> handleBadRequest(BadRequestException ex) {
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

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiResponse<?>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
		return buildErrorResponse(ApiResponseCode.MAX_UPLOAD_SIZE_EXCEEDED, "파일 크기가 제한을 초과했습니다.", HttpStatus.PAYLOAD_TOO_LARGE);
	}

	/**
	 * 공통 Exception 처리 (SSE vs 일반 요청 분기)
	 */
	@ExceptionHandler(Exception.class)
	public void handleExceptionForSseAndHttp(Exception ex, HttpServletRequest request, HttpServletResponse response) {
		String accept = request.getHeader("accept");

		if (request.getRequestURI().contains("/v3/api-docs")) {
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		ex.printStackTrace();

		if (accept != null && accept.contains("text/event-stream")) {
			// SSE 요청 응답
			response.setContentType("text/event-stream;charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);

			try (PrintWriter writer = response.getWriter()) {
				writer.write("event: error\n");
				writer.write("data: " + ex.getMessage().replace("\n", " ") + "\n\n");
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			// 일반 요청 응답
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			ApiResponse<?> error = ApiResponse.fail(ApiResponseCode.INTERNAL_SERVER_ERROR, ex.getMessage());

			try {
				objectMapper.writeValue(response.getWriter(), error);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private ResponseEntity<ApiResponse<?>> buildErrorResponse(ApiResponseCode code, String message, HttpStatus status) {
		return ResponseEntity.status(status)
			.body(ApiResponse.fail(code, message));
	}
}
