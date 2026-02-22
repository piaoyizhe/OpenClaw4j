package com.openclaw.utils;

/**
 * OpenClaw基础异常类
 * 所有OpenClaw相关的异常都应该继承自这个类
 */
public class OpenClawException extends Exception {
    private final ErrorCode errorCode;
    private final String details;

    /**
     * 错误代码枚举
     */
    public enum ErrorCode {
        CONFIG_ERROR("配置错误"),
        DATABASE_ERROR("数据库错误"),
        API_ERROR("API调用错误"),
        NETWORK_ERROR("网络错误"),
        TASK_ERROR("任务执行错误"),
        MEMORY_ERROR("内存管理错误"),
        PARSING_ERROR("解析错误"),
        VALIDATION_ERROR("参数验证错误"),
        TIMEOUT_ERROR("超时错误"),
        UNKNOWN_ERROR("未知错误");

        private final String description;

        ErrorCode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 构造方法
     * @param errorCode 错误代码
     * @param message 错误消息
     */
    public OpenClawException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * 构造方法
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param details 详细错误信息
     */
    public OpenClawException(ErrorCode errorCode, String message, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * 构造方法
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param cause 原始异常
     */
    public OpenClawException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = cause != null ? cause.getMessage() : null;
    }

    /**
     * 获取错误代码
     * @return 错误代码
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 获取详细错误信息
     * @return 详细错误信息
     */
    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[OpenClawException]")
          .append(" [")
          .append(errorCode.name())
          .append(": ")
          .append(errorCode.getDescription())
          .append("]: ")
          .append(getMessage());
        if (details != null) {
            sb.append(" (Details: ").append(details).append(")");
        }
        return sb.toString();
    }
}
