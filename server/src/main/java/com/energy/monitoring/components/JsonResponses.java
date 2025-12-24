package com.energy.monitoring.components;

import java.sql.Timestamp;

/* Методы для формирования тел http-ответов в формате json */
public class JsonResponses {
    // Метод для извлечения значений из JSON
    public static String extractFromJson(String json, String key) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\":");
            if (keyIndex == -1) {
                return null;
            }
            
            int startIndex = keyIndex + key.length() + 4;
            int endIndex   = json.indexOf("\"", startIndex);
            
            if (endIndex == -1) {
                endIndex = json.indexOf(",", startIndex);
                if (endIndex == -1) {
                    endIndex = json.indexOf("}", startIndex);
                }

                return json.substring(startIndex, endIndex).trim();
            }

            return json.substring(startIndex, endIndex);
        } catch (Exception e) {
            return null;
        }
    }

    public static String formingUniversalResponse(boolean status, String message) {
        return "{\"success\":" + status + ",\"message\":\"" + message + "\"}";
    }
    
    public static String formingHealthCheckResponse(long timestamp) {
        return "{\"status\":\"ok\",\"timestamp\":\"" + timestamp + "\"}";
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

    public static String formingCreateCoordinatorSuccessResponse(int id, String name, String ip, int port, String status) {
        return "{\"success\":true,\"data\":{" +
                    "\"id\":"       + id     + "," +
                    "\"name\":\""   + name   + "\"," +
                    "\"ip\":\""     + ip     + "\"," +
                    "\"port\":"     + port   + "," +
                    "\"status\":\"" + status + "\"," +
                    "\"message\":\"Coordinator created successfully\"" +
                "}}";
    }

    public static String formingStartOfGetUserCoordinatorsResponse() {
        return "{\"success\":true,\"data\":[";
    }
    public static String formingMiddleOfGetUserCoordinatorsResponse(int id, String name, String ip, int port, String status, Timestamp cteatedAt, Timestamp lastSeen) {
        return "{" +
                    "\"id\":"           + id        + "," +
                    "\"name\":\""       + name      + "\"," +
                    "\"ip\":\""         + ip        + "\"," +
                    "\"port\":"         + port      + "," +
                    "\"status\":\""     + status    + "\"," +
                    "\"created_at\":\"" + cteatedAt + "\"," +
                    "\"last_seen\":\""  + lastSeen  + "\"" +
               "},";
    }
    public static String formingEndOfGetUserCoordinatorsResponse(String response) {
        return response.substring(0, response.length() - 1) + "]}";
    }

    public static String formingStartOfGetCoordinatorResponse(int id, String name, String ip, int port, String status, Timestamp cteatedAt, Timestamp lastSeen) {
        return  "{\"success\":true,\"data\":{" +
                    "\"coordinator\":{" +
                        "\"id\":"           + id        + "," +
                        "\"name\":\""       + name      + "\"," +
                        "\"ip\":\""         + ip        + "\"," +
                        "\"port\":"         + port      + "," +
                        "\"status\":\""     + status    + "\"," +
                        "\"created_at\":\"" + cteatedAt + "\"," +
                        "\"last_seen\":\""  + lastSeen  + "\"" +
                   "},\"meters\":[\"";
    }
    public static String formingMiddleOfGetCoordinatorResponse(int meterId, String meterName, String meterZbLongAddr, short meterZbShortAddr, String meterStatus, Timestamp meterCreatedAt, Timestamp meterLastSeen) {
        return "{" +
                    "\"id\":"             + meterId          + "," +
                    "\"name\":\""         + meterName        + "\"," +
                    "\"zb_long_addr\":\"" + meterZbLongAddr  + "\"," +
                    "\"zb_short_addr\":"  + meterZbShortAddr + "," +
                    "\"status\":\""       + meterStatus      + "\"," +
                    "\"created_at\":\""   + meterCreatedAt   + "\"," +
                    "\"last_seen\":\""    + meterLastSeen    + "\"" +
               "},";
    }
    public static String formingEndOfGetCoordinatorResponse(String response) {
        return response.substring(0, response.length() - 1) + "]}}";
    }

    public static String formingStartOfConnectionToCoordinatorResponse(int id, String name, String ip, int port, String status, Timestamp cteatedAt, Timestamp lastSeen) {
        return  "{\"success\":true,\"data\":{" +
                    "\"message\":\"Connected to coordinator\"," +
                    "\"coordinator\":{" +
                        "\"id\":"           + id        + "," +
                        "\"name\":\""       + name      + "\"," +
                        "\"ip\":\""         + ip        + "\"," +
                        "\"port\":"         + port      + "," +
                        "\"status\":\""     + status    + "\"," +
                        "\"created_at\":\"" + cteatedAt + "\"," +
                        "\"last_seen\":\""  + lastSeen  + "\"" +
                   "},\"meters\":[\"";
    }
    public static String formingMiddleOfConnectionToCoordinatorResponse(int meterId, String meterName, String meterZbLongAddr, short meterZbShortAddr, String meterStatus, Timestamp meterCreatedAt, Timestamp meterLastSeen) {
        return "{" +
                    "\"id\":"             + meterId          + "," +
                    "\"name\":\""         + meterName        + "\"," +
                    "\"zb_long_addr\":\"" + meterZbLongAddr  + "\"," +
                    "\"zb_short_addr\":"  + meterZbShortAddr + "," +
                    "\"status\":\""       + meterStatus      + "\"," +
                    "\"created_at\":\""   + meterCreatedAt   + "\"," +
                    "\"last_seen\":\""    + meterLastSeen    + "\"" +
               "},";
    }
    public static String formingEndOfConnectionToCoordinatorResponse(String response) {
        return response.substring(0, response.length() - 1) + "]}}";
    }
}
