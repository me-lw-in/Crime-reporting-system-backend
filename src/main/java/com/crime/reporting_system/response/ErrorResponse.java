package com.crime.reporting_system.response;

public class ErrorResponse {
    private String message;
    private String type;

    public ErrorResponse(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}