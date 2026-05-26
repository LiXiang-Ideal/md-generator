package com.mdgenerator.server.config;

import com.mdgenerator.server.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * 全局异常处理器
 *
 * <p>统一处理Controller层抛出的异常，避免在每个Controller中重复try-catch。
 * 返回标准化的JSON错误响应格式。</p>
 *
 * @author MD Generator Team
 * @version 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException e) {
        logger.warn("参数错误: {}", e.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
    }

    /**
     * 处理路径遍历攻击异常
     */
    @ExceptionHandler(InvalidPathException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidPath(InvalidPathException e) {
        logger.warn("非法路径访问: {}", e.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "路径不合法"));
    }

    /**
     * 处理数据库连接异常
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<Object>> handleSQLException(SQLException e) {
        logger.error("数据库错误: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "数据库操作失败: " + e.getMessage()));
    }

    /**
     * 处理IO异常
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<Object>> handleIOException(IOException e) {
        logger.error("IO错误: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "文件操作失败: " + e.getMessage()));
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
        logger.error("运行时错误: {}", e.getMessage(), e);
        // 对于敏感信息，不直接返回给客户端
        String message = e.getMessage();
        if (message != null && (message.contains("password") || message.contains("credential"))) {
            message = "操作失败，请检查输入参数";
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, message));
    }

    /**
     * 兜底处理所有未捕获异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        logger.error("未知错误: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误"));
    }
}
