package vn.com.group.insurance.common.exception;

/**
 * Base unchecked exception for business errors in the application.
 */
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;

    public BaseException(String message) {
        super(message);
        this.errorCode = "INTERNAL_ERROR";
    }

    public BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
