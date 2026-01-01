package com.energy.monitoring.controllers;

import java.sql.SQLException;
import java.util.Map;

import com.energy.monitoring.components.HttpConstructions;
import com.energy.monitoring.components.HttpConstructions.ContentTypes;
import com.energy.monitoring.components.HttpConstructions.EndPoints;
import com.energy.monitoring.components.HttpConstructions.Methods;
import com.energy.monitoring.components.HttpStatusCodes;
import com.energy.monitoring.components.JsonResponses;
import com.energy.monitoring.database.dao.MeterDAO;
import com.energy.monitoring.models.HttpRequest;
import com.energy.monitoring.models.HttpResponse;
import com.energy.monitoring.models.Meter;
import com.energy.monitoring.utils.JwtUtil;

/* Клаасс метадов обработки запросов приборам учёта */
public class MeterController {
    // Обрабатывает запрос request и отправляет на него ответ
    public static HttpResponse handleRequest(HttpRequest request) {
        Map<String, String> headers = request.getHeaders();
        String              token   = headers.get(HttpConstructions.JsonBlocks.AUTHORIZATION);
        
        if (JwtUtil.validateToken(token)) {
            String endPoint   = request.getPath();
            String method     = request.getMethod();
            
            try {
                int meterId = getMeterIdFromEndPoint(endPoint);
                final String meterEndPoint = EndPoints.METER + meterId;
                return switch (method) {
                    case Methods.GET -> {
                        if (endPoint.equals(meterEndPoint)) {
                            yield handlerGetMeterData(meterId);
                        } else {
                            yield HttpResponse.notFound(JsonResponses.formingUniversalResponse(false, "Meter endpoint not found"));
                        }
                    }
                    default -> HttpResponse.notFound(JsonResponses.formingUniversalResponse(false, "Meter method is incorest"));
                };
            } catch (Exception e) {
                return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Server error: ".concat(e.getMessage())));
            }
        } else {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Invalid token"));
        }
    }

    // Возвращает id счётчика на основе конечной точки из запроса
    private static int getMeterIdFromEndPoint(String endPoint) {
        String[] parts = endPoint.split("/");
        
        if ((parts.length == 4 && parts[2].equals("meters"))) {
            return Integer.parseInt(parts[3]);
        } else {
            return 0;
        }
    }

    // Формирует ответ на http-запрос получения информации о показаниях счётчика с id meterId
    private static HttpResponse handlerGetMeterData(int meterId) {
        try {
            MeterDAO meterDAO = new MeterDAO();
            Meter    meter    = meterDAO.getMeter(meterId);
            
            if (meter == null) {
                return HttpResponse.notFound(JsonResponses.formingUniversalResponse(false, "Coordinator not found"));
            } else {
                String response = JsonResponses.formingGetMeterDataSuccessResponse(meter.getVoltage(), meter.getCurrent(), meter.getActivePower(), meter.getReactivePower(), 
                                                                                   meter.getApparentPower(), meter.getPowerFactor(), meter.getFrequency(), meter.getNeutralCurrent());
                return HttpResponse.ok(response, ContentTypes.JSON);
            }
        } catch (SQLException e) {
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, JsonResponses.formingUniversalResponse(false, "Database error: " + e.getMessage()));
        }
    }
}
