package com.openclaw.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 错误处理器
 * 提供统一的错误处理机制
 */
public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);
    private static ErrorHandler instance;

    /**
     * 获取单例实例
     * @return ErrorHandler实例
     */
    public static synchronized ErrorHandler getInstance() {
        if (instance == null) {
            instance = new ErrorHandler();
        }
        return instance;
    }

    /**
     * 处理异常
     * @param e 异常对象
     * @return 标准化的错误消息
     */
    public String handleException(Exception e) {
        if (e instanceof OpenClawException) {
            return handleOpenClawException((OpenClawException) e);
        } else {
            return handleGenericException(e);
        }
    }

    /**
     * 处理OpenClawException异常
     * @param e OpenClawException异常对象
     * @return 标准化的错误消息
     */
    private String handleOpenClawException(OpenClawException e) {
        OpenClawException.ErrorCode errorCode = e.getErrorCode();
        String message = e.getMessage();
        String details = e.getDetails();

        // 记录错误日志
        logger.error("[{}] {}", errorCode.name(), message);
        if (details != null) {
            logger.error("Error details: {}", details);
        }
        if (e.getCause() != null) {
            logger.error("Root cause:", e.getCause());
        }

        // 根据错误代码返回不同的错误消息
        switch (errorCode) {
            case CONFIG_ERROR:
                return "配置错误: " + message + (details != null ? " (" + details + ")" : "");
            case DATABASE_ERROR:
                return "数据库错误: " + message + (details != null ? " (" + details + ")" : "");
            case API_ERROR:
                return "API调用错误: " + message + (details != null ? " (" + details + ")" : "");
            case NETWORK_ERROR:
                return "网络错误: " + message + (details != null ? " (" + details + ")" : "");
            case TASK_ERROR:
                return "任务执行错误: " + message + (details != null ? " (" + details + ")" : "");
            case MEMORY_ERROR:
                return "内存管理错误: " + message + (details != null ? " (" + details + ")" : "");
            case PARSING_ERROR:
                return "解析错误: " + message + (details != null ? " (" + details + ")" : "");
            case VALIDATION_ERROR:
                return "参数验证错误: " + message + (details != null ? " (" + details + ")" : "");
            case TIMEOUT_ERROR:
                return "超时错误: " + message + (details != null ? " (" + details + ")" : "");
            case UNKNOWN_ERROR:
            default:
                return "未知错误: " + message + (details != null ? " (" + details + ")" : "");
        }
    }

    /**
     * 处理通用异常
     * @param e 通用异常对象
     * @return 标准化的错误消息
     */
    private String handleGenericException(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            message = "发生未知错误";
        }

        // 记录错误日志
        logger.error("Generic exception: {}", message, e);

        // 尝试将通用异常映射到OpenClawException
        if (e instanceof java.sql.SQLException) {
            return handleOpenClawException(new OpenClawException(
                    OpenClawException.ErrorCode.DATABASE_ERROR,
                    "数据库操作失败",
                    message
            ));
        } else if (e instanceof java.io.IOException) {
            return handleOpenClawException(new OpenClawException(
                    OpenClawException.ErrorCode.NETWORK_ERROR,
                    "IO操作失败",
                    message
            ));
        } else if (e instanceof java.net.SocketTimeoutException) {
            return handleOpenClawException(new OpenClawException(
                    OpenClawException.ErrorCode.TIMEOUT_ERROR,
                    "网络超时",
                    message
            ));
        } else if (e instanceof org.json.JSONException) {
            return handleOpenClawException(new OpenClawException(
                    OpenClawException.ErrorCode.PARSING_ERROR,
                    "JSON解析失败",
                    message
            ));
        } else if (e instanceof java.util.concurrent.TimeoutException) {
            return handleOpenClawException(new OpenClawException(
                    OpenClawException.ErrorCode.TIMEOUT_ERROR,
                    "操作超时",
                    message
            ));
        } else {
            return handleOpenClawException(new OpenClawException(
                    OpenClawException.ErrorCode.UNKNOWN_ERROR,
                    "发生未知错误",
                    message
            ));
        }
    }

    /**
     * 处理异常并返回错误代码
     * @param e 异常对象
     * @return 错误代码
     */
    public OpenClawException.ErrorCode getErrorCode(Exception e) {
        if (e instanceof OpenClawException) {
            return ((OpenClawException) e).getErrorCode();
        } else if (e instanceof java.sql.SQLException) {
            return OpenClawException.ErrorCode.DATABASE_ERROR;
        } else if (e instanceof java.io.IOException) {
            return OpenClawException.ErrorCode.NETWORK_ERROR;
        } else if (e instanceof java.net.SocketTimeoutException) {
            return OpenClawException.ErrorCode.TIMEOUT_ERROR;
        } else if (e instanceof org.json.JSONException) {
            return OpenClawException.ErrorCode.PARSING_ERROR;
        } else if (e instanceof java.util.concurrent.TimeoutException) {
            return OpenClawException.ErrorCode.TIMEOUT_ERROR;
        } else {
            return OpenClawException.ErrorCode.UNKNOWN_ERROR;
        }
    }

    /**
     * 检查是否是致命错误
     * @param errorCode 错误代码
     * @return 是否是致命错误
     */
    public boolean isFatalError(OpenClawException.ErrorCode errorCode) {
        switch (errorCode) {
            case CONFIG_ERROR:
            case DATABASE_ERROR:
                return true;
            default:
                return false;
        }
    }
}
