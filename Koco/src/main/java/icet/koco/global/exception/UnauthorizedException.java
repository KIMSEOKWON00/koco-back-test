package icet.koco.global.exception;

import icet.koco.enums.ErrorMessage;

public class UnauthorizedException extends RuntimeException {
    private final ErrorMessage errorMessage;

    public UnauthorizedException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = null;
    }

    public UnauthorizedException(String message) {
        super(message);
        this.errorMessage = null;
    }
}
