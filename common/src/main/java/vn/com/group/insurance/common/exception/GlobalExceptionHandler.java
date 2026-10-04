package vn.com.group.insurance.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.com.group.insurance.common.dto.ApiResponse;
import vn.com.group.insurance.common.dto.ErrorDetail;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handler for REST controllers across all microservices.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        ErrorDetail error = new ErrorDetail(ex.getErrorCode(), ex.getMessage());
        ApiResponse<ErrorDetail> response = new ApiResponse<>(false, HttpStatus.NOT_FOUND.value(), ex.getMessage(), error);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        ErrorDetail error = new ErrorDetail(ex.getErrorCode(), ex.getMessage());
        ApiResponse<ErrorDetail> response = new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), ex.getMessage(), error);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ErrorDetail error = new ErrorDetail("VALIDATION_FAILED", "Request validation failed", fieldErrors);
        ApiResponse<ErrorDetail> response = new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), "Validation failed", error);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        ErrorDetail error = new ErrorDetail("INVALID_ARGUMENT", ex.getMessage());
        ApiResponse<ErrorDetail> response = new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), ex.getMessage(), error);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleGenericException(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        ErrorDetail error = new ErrorDetail("INTERNAL_SERVER_ERROR", ex.getMessage());
        ApiResponse<ErrorDetail> response = new ApiResponse<>(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred", error);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
