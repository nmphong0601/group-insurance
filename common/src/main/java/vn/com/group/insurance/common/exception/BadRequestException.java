package vn.com.group.insurance.common.exception;

/**
 * Exception thrown when a request has invalid arguments or state.
 */
public class BadRequestException extends BaseException {

    private static final long serialVersionUID = 1L;

    public BadRequestException(String message) {
        super("BAD_REQUEST", message);
    }

    public BadRequestException(String errorCode, String message) {
        super(errorCode, message);
    }
}
