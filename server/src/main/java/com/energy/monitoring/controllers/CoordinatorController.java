package com.energy.monitoring.controllers;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.energy.monitoring.components.HttpConstructions;
import com.energy.monitoring.components.HttpConstructions.ContentTypes;
import com.energy.monitoring.components.HttpConstructions.DeviseStatuses;
import com.energy.monitoring.components.HttpConstructions.EndPoints;
import com.energy.monitoring.components.HttpConstructions.JsonBlocks;
import com.energy.monitoring.components.HttpConstructions.Methods;
import com.energy.monitoring.components.HttpStatusCodes;
import com.energy.monitoring.components.JsonResponses;
import com.energy.monitoring.database.dao.CoordinatorDAO;
import com.energy.monitoring.database.dao.MeterDAO;
import com.energy.monitoring.models.Coordinator;
import com.energy.monitoring.models.HttpRequest;
import com.energy.monitoring.models.HttpResponse;
import com.energy.monitoring.models.Meter;
import com.energy.monitoring.utils.JwtUtil;

/* Клаасс метадов обработки запросов координаторам */
public class CoordinatorController {
    // Обрабатывает запрос request и отправляет на него ответ
    public static HttpResponse handleRequest(HttpRequest request) {
        Map<String, String> headers = request.getHeaders();
        String              token   = headers.get(HttpConstructions.JsonBlocks.AUTHORIZATION);
        
        if (JwtUtil.validateToken(token)) {
            String endPoint   = request.getPath();
            String method     = request.getMethod();
            
            try {
                int userId        = JwtUtil.getUserIdFromToken(token);
                int coordinatorId = getCoordinatorIdFromEndPoint(endPoint);
                final String coordinatorEndPoint = EndPoints.COORDINATOR + coordinatorId;
                final String connectEndPoint     = EndPoints.COORDINATOR + coordinatorId + EndPoints.CONNECT;

                return switch (method) {
                    case Methods.POST -> {
                        if (endPoint.equals(EndPoints.COORDINATORS)) {
                            yield handlerCreateCoordinator(request, userId);
                        } else if (endPoint.equals(connectEndPoint)) {
                            yield handlerConnectionToCoordinator(userId, coordinatorId);
                        } else {
                            yield HttpResponse.notFound(JsonResponses.formingUniversalResponse(false, "Coordinators endpoint not found"));
                        }
                    }
                    case Methods.GET -> {
                        if (endPoint.equals(EndPoints.COORDINATORS)) {
                            yield handlerGetUserCoordinators(userId);
                        } else if (endPoint.equals(coordinatorEndPoint)) {
                            yield handlerGetCoordinator(userId, coordinatorId);
                        } else {
                            yield HttpResponse.notFound(JsonResponses.formingUniversalResponse(false, "Coordinators endpoint not found"));
                        }
                    }
                    case Methods.DELETE -> {
                        if (endPoint.equals(coordinatorEndPoint)) {
                            yield handlereDeletionOfCoordinator(userId, coordinatorId);
                        } else {
                            yield HttpResponse.notFound(JsonResponses.formingUniversalResponse(false, "Coordinators endpoint not found"));
                        }
                    }
                    default -> HttpResponse.notFound(JsonResponses.formingUniversalResponse(false, "Coordinators method is incorest"));
                };
            } catch (Exception e) {
                return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Server error: ".concat(e.getMessage())));
            }
        } else {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Invalid token"));
        }
    }

    // Возвращает id координатора на конечной точки из запроса
    private static int getCoordinatorIdFromEndPoint(String endPoint) {
        String[] parts = endPoint.split("/");

        return parts.length == 4 ? Integer.parseInt(parts[3]) : 0;
    }
    
    // Формирует ответ на http-запрос создания координатора пользователю с id userId
    private static HttpResponse handlerCreateCoordinator(HttpRequest request, int userId) {
        try {
            String body   = request.getBody();
            String name   = JsonResponses.extractFromJson(body, JsonBlocks.NAME);
            String ip     = JsonResponses.extractFromJson(body, JsonBlocks.IP);
            Integer port  = Integer.valueOf(JsonResponses.extractFromJson(body, JsonBlocks.PORT));
            
            if (name == null || ip == null || port == 0) {
                return HttpResponse.badRequest(JsonResponses.formingUniversalResponse(false, "Missing required fields"));
            }

            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            Coordinator coordinator       = coordinatorDAO.createCoordinator(userId, name, ip, port);
            
            String response = JsonResponses.formingCreateCoordinatorSuccessResponse(coordinator.getId(), coordinator.getName(), coordinator.getIp(), 
                                                                                    coordinator.getPort(), coordinator.getStatus());
            
            return HttpResponse.created(response);
        } catch (SQLException e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Database error: " + e.getMessage()));
        }
    }

    // Формирует ответ на http-запрос на получение информации о координаторах по id userID
    private static HttpResponse handlerGetUserCoordinators(int userId) {
        try {
            CoordinatorDAO    coordinatorDAO = new CoordinatorDAO();
            List<Coordinator> coordinators   = coordinatorDAO.getUserCoordinators(userId);
            
            String response = JsonResponses.formingStartOfGetUserCoordinatorsResponse();
            for (int i = 0; i < coordinators.size(); i++) {
                Coordinator coordinator = coordinators.get(i);
                response += JsonResponses.formingMiddleOfGetUserCoordinatorsResponse(coordinator.getId(), coordinator.getName(), coordinator.getIp(), coordinator.getPort(), 
                                                                                        coordinator.getStatus(), coordinator.getCreatedAt(), coordinator.getLastSeen());
            }
            response = JsonResponses.formingEndOfGetUserCoordinatorsResponse(response);

            return HttpResponse.ok(response, ContentTypes.JSON);       
        } catch (SQLException e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Database error: " + e.getMessage()));
        }
    }

    // Формирует ответ на http-запрос получения информации о координаторе по id coordinatorId 
    private static HttpResponse handlerGetCoordinator(int userId, int coordinatorId) {
        try {
            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            Coordinator    coordinator    = coordinatorDAO.getCoordinator(coordinatorId);
            
            if (!coordinatorDAO.coordinatorBelongsToUser(coordinatorId, userId)) {
                return HttpResponse.unauthorized(JsonResponses.formingUniversalResponse(false, "It's not your network"));
            } else
            if (coordinator == null) {
                return HttpResponse.notFound(JsonResponses.formingUniversalResponse(false, "Coordinator not found"));
            } else {
                MeterDAO    meterDAO = new MeterDAO();
                List<Meter> meters   = meterDAO.getMetersByCoordinator(coordinatorId);
                
                String response = JsonResponses.formingStartOfGetCoordinatorResponse(coordinator.getId(), coordinator.getName(), coordinator.getIp(), coordinator.getPort(), 
                                                                                     coordinator.getStatus(), coordinator.getCreatedAt(), coordinator.getLastSeen());
                for (int i = 0; i < meters.size(); i++) {
                    Meter meter = meters.get(i);

                    response += JsonResponses.formingMiddleOfGetUserCoordinatorsResponse(meter.getId(), meter.getName(), meter.getZbLongAddr(), meter.getZbShortAddr(), 
                                                                                         meter.getStatus(), meter.getCreatedAt(), meter.getLastSeen());
                }
                response = JsonResponses.formingEndOfGetCoordinatorResponse(response);
                
                return HttpResponse.ok(response, ContentTypes.JSON);
            }
        } catch (SQLException e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Database error: " + e.getMessage()));
        }
    }

    // Формирует ответ на http-запрос установки соединения с координатором c id coordinatorId пользователя с id userId
    private static HttpResponse handlerConnectionToCoordinator(int userId, int coordinatorId) {
        try {
            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            Coordinator    coordinator    = coordinatorDAO.getCoordinator(coordinatorId);
            boolean        connected      = emulateConnection();

            if (!coordinatorDAO.coordinatorBelongsToUser(coordinatorId, userId)) {
                return HttpResponse.unauthorized(JsonResponses.formingUniversalResponse(false, "Access denied"));
            } else
            if (connected) {
                coordinatorDAO.updateCoordinatorStatus(coordinatorId, DeviseStatuses.ONLINE);
                
                MeterDAO meterDAO  = new MeterDAO();
                List<Meter> meters = meterDAO.getMetersByCoordinator(coordinatorId);
                
                String response = JsonResponses.formingStartOfConnectionToCoordinatorResponse(coordinator.getId(), coordinator.getName(), coordinator.getIp(), coordinator.getPort(), 
                                                                                              DeviseStatuses.ONLINE, coordinator.getCreatedAt(), coordinator.getLastSeen());
                for (int i = 0; i < meters.size(); i++) {
                    Meter meter = meters.get(i);

                    response += JsonResponses.formingMiddleOfConnectionToCoordinatorResponse(meter.getId(), meter.getName(), meter.getZbLongAddr(), meter.getZbShortAddr(), 
                                                                                             meter.getStatus(), meter.getCreatedAt(), meter.getLastSeen());
                }
                response = JsonResponses.formingEndOfGetCoordinatorResponse(response);
                
                return HttpResponse.ok(response, ContentTypes.JSON);
            } else {
                coordinatorDAO.updateCoordinatorStatus(coordinatorId, DeviseStatuses.ERROR);
                return HttpResponse.error(HttpStatusCodes.SERVICE_UNAVAILABLE, JsonResponses.formingUniversalResponse(false, "Failed to connect to coordinator"));
            }
            
        } catch (SQLException e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Database error: " + e.getMessage()));
        }
    }
    
    // Всегда успешная эмуляция установки соединения с координатером
    private static boolean emulateConnection() {
        try {
            Thread.sleep(1000);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }
    
   // Формирует ответ на http-запрос удаления координатора с id coordinatorId пользователя с id userId
    private static HttpResponse handlereDeletionOfCoordinator(int userId, int coordinatorId) {
        try {
            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            
            if (!coordinatorDAO.coordinatorBelongsToUser(coordinatorId, userId)) {
                return HttpResponse.unauthorized(JsonResponses.formingUniversalResponse(false, "It's not your network"));
            } else {
                coordinatorDAO.deleteCoordinator(coordinatorId);

                return HttpResponse.ok(JsonResponses.formingUniversalResponse(true, "Coordinator deleted"), "application/json");
            }
            
        } catch (SQLException e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Database error: " + e.getMessage()));
        }
    }
}