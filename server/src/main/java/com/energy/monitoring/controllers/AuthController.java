package com.energy.monitoring.controllers;

import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.energy.monitoring.components.HttpConstructions;
import com.energy.monitoring.components.HttpConstructions.ContentTypes;
import com.energy.monitoring.components.HttpConstructions.EndPoints;
import com.energy.monitoring.components.HttpConstructions.JsonBlocks;
import com.energy.monitoring.components.HttpConstructions.Methods;
import com.energy.monitoring.components.HttpStatusCodes;
import com.energy.monitoring.components.JsonResponses;
import com.energy.monitoring.database.dao.UserDAO;
import com.energy.monitoring.handlers.HttpRequest;
import com.energy.monitoring.handlers.HttpResponse;
import com.energy.monitoring.models.User;
import com.energy.monitoring.utils.JwtUtil;

/* Обработчик конфигурационных запросов */
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class); // Объект Logger для текущего класса
    
    // Обрабатывает запрос request и отправляет на него ответ
    public static HttpResponse handleRequest(HttpRequest request) {
        String endPoint   = request.getPath();
        String method     = request.getMethod();
        
        try {
            return switch (method) {
                case Methods.POST -> 
                    switch (endPoint) {
                        case EndPoints.LOGING          -> handleLogin(request);
                        case EndPoints.LOGOUT          -> handleLogout(request);
                        case EndPoints.REGISTER        -> handleRegister(request);
                        default                        -> HttpResponse.notFound(JsonResponses.formingUniversalResponse(false, "Auth endpoint not found"));
                    };
                case Methods.GET -> 
                    switch (endPoint) {
                        case EndPoints.VALIDATE        -> handleValidate(request);
                        case EndPoints.PROFILE         -> handleProfile(request);
                        default                        -> HttpResponse.notFound(JsonResponses.formingUniversalResponse(false, "Auth endpoint not found"));
                    };
                default                                -> HttpResponse.notFound(JsonResponses.formingUniversalResponse(false, "Auth method is incorest"));
            };
        } catch (Exception e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Server error: ".concat(e.getMessage())));
        }
    }

    // Формируется ответ на http-запрос авторизации
    private static HttpResponse handleLogin(HttpRequest request) {
        try {
            String body = request.getBody();
            
            if (body.contains(JsonBlocks.USERNAME) && body.contains(JsonBlocks.PASSWORD)) {
                String username = extractFromJson(body, JsonBlocks.USERNAME);
                String password = extractFromJson(body, JsonBlocks.PASSWORD);
                
                if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                    return HttpResponse.badRequest(JsonResponses.formingUniversalResponse(false, "Username or password are required"));
                }

                try {
                    UserDAO userDAO = new UserDAO();
                    User    user    = userDAO.authenticate(username, password);
                    
                    if (user != null) {
                        int userId          = user.getId();
                        userDAO.activateUser(userId);
                        user = userDAO.authenticate(username, password);

                        String token        = JwtUtil.generateToken(username, userId);
                        String jsonResponse = JsonResponses.formingUserLoginSuccessResponse(token, userId, username, user.getIsActive());

                        return HttpResponse.ok(jsonResponse, HttpConstructions.ContentTypes.JSON);
                    } else {
                        return HttpResponse.unauthorized(JsonResponses.formingUniversalResponse(false, "Invalid credentials or account is not active"));
                    }
                } catch (SQLException e) {
                    return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Database error: ".concat(e.getMessage())));
                }
            }
            
            return HttpResponse.unauthorized(JsonResponses.formingUniversalResponse(false, "Invalid credentials"));
        } catch (Exception e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Error processing login: ".concat(e.getMessage())));
        }
    }
    
    // Формируется ответ на http-запрос валидации сессии
    private static HttpResponse handleValidate(HttpRequest request) {
        try {
            Map<String, String> headers = request.getHeaders();
            String              token   = headers.get(HttpConstructions.JsonBlocks.AUTHORIZATION);
            
            String jsonResponse;
            if (JwtUtil.validateToken(token)) {
                jsonResponse = JsonResponses.formingUserValidateSuccessResponse(JwtUtil.getUsernameFromToken(token), JwtUtil.getUserIdFromToken(token));
            } else {
                jsonResponse = JsonResponses.formingUserUnValidateResponse();
            }
            return HttpResponse.ok(jsonResponse, ContentTypes.JSON);
        } catch (Exception e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Error validating token: ".concat(e.getMessage())));
        }
    }
    
    // Формируется ответ на http-запрос деавторизации
    private static HttpResponse handleLogout(HttpRequest request) {
        try {
            Map<String, String> headers = request.getHeaders();
            String              token   = headers.get(HttpConstructions.JsonBlocks.AUTHORIZATION);
            
            if (JwtUtil.validateToken(token)) {
                UserDAO userDAO = new UserDAO();
                userDAO.deactivateUser(JwtUtil.getUserIdFromToken(token));

                return HttpResponse.ok(JsonResponses.formingUniversalResponse(true , "Logged out successfully"), ContentTypes.JSON);
            }
            
            return HttpResponse.ok(JsonResponses.formingUniversalResponse(false, "Logged out not successfully"), ContentTypes.JSON);
        } catch (SQLException e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Error loged out of user: ".concat(e.getMessage())));
        }
    }

    // Формируется ответ на http-запрос регистрации
    private static HttpResponse handleRegister(HttpRequest request) {
        try {
            String body     = request.getBody();
            String username = extractFromJson(body, JsonBlocks.USERNAME);
            String password = extractFromJson(body, JsonBlocks.PASSWORD);

            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                return HttpResponse.badRequest(JsonResponses.formingUniversalResponse(false, "Username or password are required"));
            }
            
            try {
                UserDAO userDAO = new UserDAO();

                if (userDAO.userExists(username)) {
                    return HttpResponse.error(HttpStatusCodes.CONFLICT, JsonResponses.formingUniversalResponse(false, "Username already exists"));
                }
                
                User newUser = userDAO.createUser(username, password);
                userDAO.deactivateUser(newUser.getId());
                String jsonResponse = JsonResponses.formingUserRegisterSuccessResponse(username, newUser.getId());
                
                return HttpResponse.created(jsonResponse);
                
            } catch (SQLException e) {
                logger.warn("SQL Exeption fail: {}", e.getMessage());
                return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Database error: " + e.getMessage()));
            }
        } catch (Exception e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Error processing registration: " + e.getMessage()));
        }
    }

    // Формируется ответ на http-запрос информации о профиле
    private static HttpResponse handleProfile(HttpRequest request) {
        try {
            Map<String, String> headers = request.getHeaders();
            String              token   = headers.get(HttpConstructions.JsonBlocks.AUTHORIZATION);
            
            if (JwtUtil.validateToken(token)) {
                String jsonResponse = JsonResponses.formingUserProfileSuccessResponse(JwtUtil.getUsernameFromToken(token), JwtUtil.getUserIdFromToken(token));
                
                return HttpResponse.ok(jsonResponse, ContentTypes.JSON);
            }
            
            return HttpResponse.unauthorized(JsonResponses.formingUniversalResponse(false, "Not authenticated"));
        } catch (Exception e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Error getting profile: " + e.getMessage()));
        }
    }
    
    // Метод для извлечения значений из JSON
    private static String extractFromJson(String json, String key) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\":");
            if (keyIndex == -1) {
                logger.warn("Not find a key: {}", key);
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
            logger.error("Error in extracten key: {}", e.getMessage());
            return null;
        }
    }
}