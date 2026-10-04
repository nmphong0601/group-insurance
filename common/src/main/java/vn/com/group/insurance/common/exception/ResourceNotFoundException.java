package vn.com.group.insurance.common.exception;

/**
 * Exception thrown when a requested entity or resource is not found.
 */
public class ResourceNotFoundException extends BaseException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super("NOT_FOUND", message);
    }

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super("NOT_FOUND", String.format("%s with identifier [%s] was not found.", resourceName, identifier));
    }
}
