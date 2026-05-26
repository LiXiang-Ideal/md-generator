package com.mdgenerator.server.dto;

/**
 * 统一API响应封装
 */
public class ApiResponse<T> {

    /** 状态码: 200成功, 400参数错误, 500服务器错误 */
    private int code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 时间戳 */
    private long timestamp;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.code = 200;
        resp.message = "success";
        resp.data = data;
        resp.timestamp = System.currentTimeMillis();
        return resp;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> resp = success(data);
        resp.message = message;
        return resp;
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.code = code;
        resp.message = message;
        resp.timestamp = System.currentTimeMillis();
        return resp;
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }

    // Getters
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}