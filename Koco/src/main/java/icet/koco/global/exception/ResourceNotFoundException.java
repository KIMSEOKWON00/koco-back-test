package icet.koco.global.exception;

import icet.koco.enums.ErrorMessage;

public class ResourceNotFoundException extends RuntimeException {
    private final ErrorMessage errorMessage;

    public ResourceNotFoundException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = null;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorMessage = null;
    }
}
