package icet.koco.enums;

import lombok.Getter;

@Getter
public enum ErrorMessage {
    // 400 (Bad Request - BadRequestException)
    INVALID_CATEGORY_INCLUDED("존재하지 않는 카테고리가 포함되어 있습니다."),
    INVALID_PROBLEM_INCLUDED("해당 문제 번호를 가진 백준 문제가 없습니다."),

    // 401 (Unauthorized - UnauthorizedException)
	INVALID_ACCESS_TOKEN("유효하지 않은 access token 입니다."),
	INVALID_REFRESH_TOKEN("유효하지 않은 refresh token 입니다."),
	USER_ID_EXTRACTION_FAILED("토큰에서 사용자 ID를 추출할 수 없습니다."),
	OAUTH_NOT_FOUND("OAuth 정보가 존재하지 않습니다."),
	REDIS_NOT_MATCH("REDIS에 저장된 값과 일치하지 않습니다."),
	DB_NOT_MATCH("DB에 저장된 값과 일치하지 않습니다."),


    // 403 (Forbidden - ForbiddenException)
    NO_POST_PERMISSION("게시글 수정/삭제 권한이 없습니다."),
    NO_COMMENT_PERMISSION("댓글 수정/삭제 권한이 없습니다."),
	NO_ALARM_PERMISSION("해당 알림을 읽음/삭제할 권한이 없습니다."),
	NO_LIKE_PERMISSION("본인의 좋아요만 취소할 수 있습니다."),


    // 404 (Not Found - ResourceNotFoundException)
    USER_NOT_FOUND("존재하지 않는 사용자입니다."),
    POST_NOT_FOUND("존재하지 않는 게시글입니다."),
	CATEGORY_NOT_FOUND("존재하지 않는 카테고리입니다."),
    COMMENT_NOT_FOUND("존재하지 않는 댓글입니다."),
    LIKE_NOT_FOUND("존재하지 않는 좋아요입니다."),
	ALARM_NOT_FOUND("존재하지 않는 알림입니다."),
	PROBLEM_SET_NOT_FOUND("존재하지 않는 문제집입니다."),
	SOLUTION_NOT_FOUND("존재하지 않는 해설입니다."),
	PROBLEM_NOT_FOUND("존재하지 않는 문제입니다."),
	PROBLEM_NOT_IN_PROBLEM_SET("문제 ID %d는 문제집 ID %d에 포함되어 있지 않습니다."),

	ALREADY_LIKED_ERROR("이미 좋아요를 누른 게시글입니다."),
	ALREADY_UNLIKED_ERROR("이미 좋아요 취소를 누른 게시글입니다."),
	LIKE_CONCURRENCY_FAILURE("동시성 문제로 좋아요 등록/취소에 실패했습니다."),
	ALREADY_SOLUTION_EXIST("이미 해당 문제에 대한 해설이 존재합니다.");

	private final String message;

	ErrorMessage(String message) {
		this.message = message;
	}

	public String format(Object... args) {
		return String.format(this.message, args);
	}
}
