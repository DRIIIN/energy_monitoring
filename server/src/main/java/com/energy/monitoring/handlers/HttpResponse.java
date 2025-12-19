package com.energy.monitoring.handlers;

import java.nio.charset.StandardCharsets;

import com.energy.monitoring.components.HttpStatusCodes;

/* Класс http-ответа */
public class HttpResponse {
    private final int    statusCode;
    private final String statusMessage;
    private final byte[] body;
    private final String contentType;
    
    public HttpResponse(int statusCode, String statusMessage, byte[] body, String contentType) {
        this.statusCode    = statusCode;
        this.statusMessage = statusMessage;
        this.body          = body;
        this.contentType   = contentType;
    }
    
    public HttpResponse(int statusCode, String statusMessage, String body, String contentType) {
        this(statusCode, statusMessage, body.getBytes(StandardCharsets.UTF_8), contentType);
    }
    
    public static HttpResponse ok(String body, String contentType) {
        return new HttpResponse(HttpStatusCodes.OK, "OK", body, contentType);
    }
    
    public static HttpResponse created(String body) {
        return new HttpResponse(HttpStatusCodes.CREATED, "Created", body, "application/json");
    }
    
    public static HttpResponse badRequest(String body) {
        return new HttpResponse(HttpStatusCodes.BAD_REQUEST, "Bad Request", body, "application/json");
    }
    
    public static HttpResponse unauthorized(String body) {
        return new HttpResponse(HttpStatusCodes.UNAUTHORIZED, "Unauthorized", body, "application/json");
    }
    
    public static HttpResponse notFound(String body) {
        return new HttpResponse(HttpStatusCodes.NOT_FOUND, "Not Found", body, "application/json");
    }
    
    public static HttpResponse error(int code, String body) {
        return new HttpResponse(code, "Error", body, "application/json");
    }
    
    public int getStatusCode() { 
        return statusCode; 
    }
    
    public String getStatusMessage() { 
        return statusMessage; 
    }

    public byte[] getBody() { 
        return body; 
    }

    public String getContentType() { 
        return contentType; 
    }
}