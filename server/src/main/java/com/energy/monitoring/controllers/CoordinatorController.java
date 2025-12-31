package com.energy.monitoring.controllers;

import java.sql.SQLException;
import java.util.Arrays;
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
import com.energy.monitoring.utils.UartUtil;

/* Клаасс метадов обработки запросов координаторам */
public class CoordinatorController {
    // private static final Logger logger      = LoggerFactory.getLogger(CoordinatorController.class);  // Объект Logger для текущего класса

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
                final String commandEndPoint     = EndPoints.COORDINATOR + coordinatorId + EndPoints.COMMAND;


                // logger.info(connectEndPoint + " " + endPoint + " " + coordinatorId);
                return switch (method) {
                    case Methods.POST -> {
                        if (endPoint.equals(EndPoints.COORDINATORS)) {
                            yield handlerCreateCoordinator(request, userId);
                        } else 
                        if (endPoint.equals(connectEndPoint)) {
                            yield handlerConnectionToCoordinator(userId, coordinatorId);
                        } else
                        if (endPoint.equals(commandEndPoint)) {
                            yield handlerCommandToCoordinator(request, coordinatorId);
                        } else {
                            yield HttpResponse.notFound(JsonResponses.formingUniversalResponse(false, "Coordinators endpoint not found"));
                        }
                    }
                    case Methods.GET -> {
                        if (endPoint.equals(EndPoints.COORDINATORS)) {
                            yield handlerGetUserCoordinators(userId);
                        } else 
                        if (endPoint.equals(coordinatorEndPoint)) {
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

    // Возвращает id координатора на основе конечной точки из запроса
    private static int getCoordinatorIdFromEndPoint(String endPoint) {
        String[] parts = endPoint.split("/");
        
        if ((parts.length == 4 && parts[2].equals("coordinators")) || (parts.length == 5 && (parts[4].equals("connect") || parts[4].equals("command")))) {
            return Integer.parseInt(parts[3]);
        } else {
            return 0;
        }
    }
    
    // Формирует ответ на http-запрос создания координатора пользователю с id userId
    private static HttpResponse handlerCreateCoordinator(HttpRequest request, int userId) {
        try {
            // logger.info("Handler create coordinator request: {}", request.getBody());
            String body   = request.getBody();
            String name   = JsonResponses.extractFromJson(body, JsonBlocks.NAME);
            String mac    = JsonResponses.extractFromJson(body, JsonBlocks.MAC);
            String ip     = JsonResponses.extractFromJson(body, JsonBlocks.IP);
            String port_s = JsonResponses.extractFromJson(body, JsonBlocks.PORT);
            Integer port  = port_s != null ? Integer.valueOf(port_s) : 0;

            if (name == null || mac == null) {
                return HttpResponse.badRequest(JsonResponses.formingUniversalResponse(false, "Missing required fields"));
            }
            
            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            Coordinator coordinator       = coordinatorDAO.createCoordinator(userId, name, mac, ip, port);
            
            String response = JsonResponses.formingCreateCoordinatorSuccessResponse(coordinator.getId(), coordinator.getName(), coordinator.getMac(), coordinator.getIp(), 
                                                                                    coordinator.getPort(), coordinator.getStatus());
            // logger.info("Handler create c oordinator response: {}", response);
            return HttpResponse.created(response);
        } catch (SQLException e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Database error: " + e.getMessage()));
        }
    }

    // Формирует ответ на http-запрос на получение информации о координаторах по id userID
    private static HttpResponse handlerGetUserCoordinators(int userId) {
        try {
            // logger.info("handlerGetUserCoordinators");
            CoordinatorDAO    coordinatorDAO = new CoordinatorDAO();
            List<Coordinator> coordinators   = coordinatorDAO.getUserCoordinators(userId);
            
            String response = JsonResponses.formingStartOfGetUserCoordinatorsResponse();
            for (int i = 0; i < coordinators.size(); i++) {
                Coordinator coordinator = coordinators.get(i);
                response += JsonResponses.formingMiddleOfGetUserCoordinatorsResponse(coordinator.getId(), coordinator.getName(), coordinator.getMac(), coordinator.getIp(), 
                                                                                     coordinator.getPort(), coordinator.getStatus(), coordinator.getCreatedAt(), coordinator.getLastSeen());
            }
            response = JsonResponses.formingEndOfGetUserCoordinatorsResponse(response);

            // logger.info("Response: {}", response);
            return HttpResponse.ok(response, ContentTypes.JSON);       
        } catch (SQLException e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Database error: " + e.getMessage()));
        }
    }

    // Формирует ответ на http-запрос получения информации о координаторе по id coordinatorId 
    private static HttpResponse handlerGetCoordinator(int userId, int coordinatorId) {
        try {
            // logger.info("handlerGetCoordinator");
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
                
                String response = JsonResponses.formingStartOfGetCoordinatorResponse(coordinator.getId(), coordinator.getName(), coordinator.getMac(), coordinator.getIp(), 
                                                                                     coordinator.getPort(), coordinator.getStatus(), coordinator.getCreatedAt(), coordinator.getLastSeen());
                for (int i = 0; i < meters.size(); i++) {
                    Meter meter = meters.get(i);

                    response += JsonResponses.formingMiddleOfGetUserCoordinatorsResponse(meter.getId(), meter.getName(), coordinator.getMac(), meter.getZbLongAddr(), 
                                                                                         meter.getZbShortAddr(), meter.getStatus(), meter.getCreatedAt(), meter.getLastSeen());
                }
                response = JsonResponses.formingEndOfGetCoordinatorResponse(response);
                // logger.info("Response: {}", response);

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
            boolean        connected      = checkConnectionToCoordinatorOnUart(coordinator.getMac());

            if (!coordinatorDAO.coordinatorBelongsToUser(coordinatorId, userId)) {
                return HttpResponse.unauthorized(JsonResponses.formingUniversalResponse(false, "Access denied"));
            } else
            if (connected) {
                coordinatorDAO.updateCoordinatorStatus(coordinatorId, DeviseStatuses.ONLINE);
                
                MeterDAO meterDAO  = new MeterDAO();
                List<Meter> meters = meterDAO.getMetersByCoordinator(coordinatorId);
                
                String response = JsonResponses.formingStartOfConnectionToCoordinatorResponse(coordinator.getId(), coordinator.getName(), coordinator.getMac(), coordinator.getIp(), 
                                                                                              coordinator.getPort(), DeviseStatuses.ONLINE, coordinator.getCreatedAt(), coordinator.getLastSeen());
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
    
    // Возвращает true, если известен серверу com-порт, на котором висит координатор с mac-адресом macAddress, иначе - false 
    private static boolean checkConnectionToCoordinatorOnUart(String macAddress) {
        try {
            Thread.sleep(1000);

            String portName = UartUtil.getCommPortByMacAddress(macAddress);
            
            return portName != null;

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

                return HttpResponse.ok(JsonResponses.formingUniversalResponse(true, "Coordinator deleted"), ContentTypes.JSON);
            }
            
        } catch (SQLException e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Database error: " + e.getMessage()));
        }
    }

    // Возвращает строку string в виде набота байт
    private static byte[] convertStringToBytes(String string) {
        // logger.info("len: {}", string.length());
        int len = string.length();
        if (len > 0 && len % 2 == 0) {
            byte[] bytes = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                bytes[i / 2] = (byte) Integer.parseInt(string.substring(i, i + 2), 16);
            }

            return bytes;
        } else {
            return null;
        }
    }

    // Формирует ответ на http-запрос отправки команды координатору с id coordinatorId
    private static HttpResponse handlerCommandToCoordinator(HttpRequest request, int coordinatorId) {
        try {
            String body          = request.getBody();
            byte commandCode     = convertStringToBytes(JsonResponses.extractFromJson(body, JsonBlocks.COMMAND))[0];
            String commandParams = JsonResponses.extractFromJson(body, JsonBlocks.PARAMETERS);
            byte[] byteParameters = commandParams != null && commandParams.length() > 0 ? convertStringToBytes(commandParams) : null;

            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            Coordinator    coordinator    = coordinatorDAO.getCoordinator(coordinatorId);
            String         macAddress     = coordinator.getMac();

            byte[] response = UartUtil.sendCommandToCoordinator(macAddress, commandCode, byteParameters);
            
            return HttpResponse.ok(JsonResponses.formingCoordinatorCommandSuccessResponse(coordinatorId, response[0], Arrays.copyOfRange(response, 1, response.length)), ContentTypes.JSON);
        } catch (SQLException e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Database error: " + e.getMessage()));
        }
    }
}