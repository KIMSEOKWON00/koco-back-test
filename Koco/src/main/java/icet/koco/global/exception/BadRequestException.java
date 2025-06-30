package icet.koco.global.exception;

import icet.koco.enums.ErrorMessage;

public class BadRequestException extends RuntimeException {
    private final ErrorMessage errorMessage;

    public BadRequestException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = null;
    }

    public BadRequestException(String message) {
        super(message);
        this.errorMessage = null;
    }
}
