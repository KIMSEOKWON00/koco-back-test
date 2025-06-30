package icet.koco.global.exception;

import icet.koco.enums.ErrorMessage;

public class AlreadyLikedException extends RuntimeException {
    private final ErrorMessage errorMessage;

    public AlreadyLikedException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = null;
    }

    public AlreadyLikedException(String message) {
        super(message);
        this.errorMessage = null;
    }
}