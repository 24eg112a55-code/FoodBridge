package com.communityfoodsharing.exception;

public class BusinessException extends RuntimeException {
    private final int status;
    public BusinessException(int status, String message) { super(message); this.status = status; }
    public int getStatus() { return status; }
    public static BusinessException notFound(String message) { return new BusinessException(404, message); }
    public static BusinessException conflict(String message) { return new BusinessException(409, message); }
    public static BusinessException forbidden(String message) { return new BusinessException(403, message); }
    public static BusinessException badRequest(String message) { return new BusinessException(400, message); }
}