package com.energy.monitoring.components;

/* Методы для формирования тел http-ответов в формате json */
public class JsonResponses {
    public static String formingUniversalResponse(boolean status, String message) {
        return "{\"success\":" + status + ",\"message\":\"" + message + "\"}";
    }
    
    public static String formingUserValidateSuccessResponse(String userName, int userId) {
        return "{\"success\":true,\"data\":{" +
                    "\"valid\":true," +
                    "\"user\":{" +
                        "\"id\":"         + userId   + "," +
                        "\"username\":\"" + userName + "\"" +
                    "}" +
                "}}";  
    }

    public static String formingUserUnValidateResponse() {
        return "{\"success\":true,\"data\":{\"valid\":false}}";    
    }

    public static String formingUserLoginSuccessResponse(String token, int userId, String username, boolean isActive) {
        return "{\"success\":true,\"data\":{" +
                    "\"token\":\"" + token + "\"," +
                    "\"user\":{" +
                        "\"id\":"         + userId   + "," +
                        "\"username\":\"" + username + "\"," +
                        "\"is_active\":"  + isActive +
                    "}" +
                "}}";
    }

    public static String formingUserRegisterSuccessResponse(String userName, int userId) {
        return "{\"success\":true,\"data\":{" +
                    "\"id\":"         + userId   + "," +
                    "\"username\":\"" + userName + "\"," +
                    "\"message\":\"Registration successful. Please login.\"" +
                "}}";
    }

    public static String formingUserProfileSuccessResponse(String userName, int userId) {
        return "{\"success\":true,\"data\":{" +
                    "\"id\":"         + userId   + "," +
                    "\"username\":\"" + userName + "\"" +
                "}}";
    }

}
