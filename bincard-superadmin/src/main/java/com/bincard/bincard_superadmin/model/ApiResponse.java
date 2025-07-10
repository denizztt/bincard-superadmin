package com.bincard.bincard_superadmin.model;

/**
 * API yanıt wrapper sınıfı
 */
public class ApiResponse<T> {
    private String message;
    private T data;
    private boolean success;
    
    public ApiResponse() {}
    
    public ApiResponse(String message, T data, boolean success) {
        this.message = message;
        this.data = data;
        this.success = success;
    }
    
    // Getters and setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
