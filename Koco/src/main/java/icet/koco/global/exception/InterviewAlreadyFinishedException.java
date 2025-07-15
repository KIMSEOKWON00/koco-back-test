package icet.koco.global.exception;

import icet.koco.enums.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InterviewAlreadyFinishedException extends RuntimeException {
	private final ErrorMessage errorMessage;

	public InterviewAlreadyFinishedException(ErrorMessage errorMessage) {
		super(errorMessage.getMessage());
		this.errorMessage = null;
	}

	public InterviewAlreadyFinishedException(String message) {
		super(message);
		this.errorMessage = null;
	}
}
