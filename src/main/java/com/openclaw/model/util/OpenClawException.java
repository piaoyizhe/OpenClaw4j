package com.openclaw.model;

public class OpenClawException extends Exception {
    private String errorCode;
    private String userFriendlyMessage;

    public OpenClawException(String message) {
        super(message);
        this.userFriendlyMessage = "处理您的请求时出现错误，请稍后再试。";
    }

    public OpenClawException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.userFriendlyMessage = "处理您的请求时出现错误，请稍后再试。";
    }

    public OpenClawException(String message, String errorCode, String userFriendlyMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public OpenClawException(String message, Throwable cause) {
        super(message, cause);
        this.userFriendlyMessage = "处理您的请求时出现错误，请稍后再试。";
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }
}
