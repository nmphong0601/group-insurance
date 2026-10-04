package vn.com.group.insurance.common.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * Detailed error payload for validation failures and business exceptions.
 */
public class ErrorDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private String errorCode;
    private String errorMessage;
    private Map<String, String> fieldErrors;

    public ErrorDetail() {
    }

    public ErrorDetail(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ErrorDetail(String errorCode, String errorMessage, Map<String, String> fieldErrors) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.fieldErrors = fieldErrors;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
