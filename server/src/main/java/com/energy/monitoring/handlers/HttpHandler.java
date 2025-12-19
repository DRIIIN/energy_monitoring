package com.energy.monitoring.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.energy.monitoring.components.HttpStatusCodes;
import com.energy.monitoring.controllers.AuthController;

public class HttpHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(HttpHandler.class.getName());
    
    private final Socket clientSocket;
    
    public HttpHandler(Socket socket) {
        this.clientSocket = socket;
    }
    
    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            OutputStream output = clientSocket.getOutputStream();
            PrintWriter  writer = new PrintWriter(
            new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {
            
            String requestLine = reader.readLine();
            if (requestLine == null) return;
            
            logger.info("Request: ".concat(requestLine));
            
            // Парсинг метода и пути
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                sendError(writer, HttpStatusCodes.BAD_REQUEST, "Bad Request");
                return;
            }
            
            String method = requestParts[0];
            String path = requestParts[1];
            
            // Чтение заголовков
            Map<String, String> headers = new HashMap<>();
            String line;
            int contentLength = 0;
            String origin = null;
            
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] headerParts = line.split(":", 2);
                if (headerParts.length == 2) {
                    String headerName = headerParts[0].trim().toLowerCase();
                    String headerValue = headerParts[1].trim();
                    headers.put(headerName, headerValue);
                    
                    if (headerName.equals("content-length")) {
                        contentLength = Integer.parseInt(headerValue);
                    } else if (headerName.equals("origin")) {
                        origin = headerValue;
                    }
                }
            }
            
            // Чтение тела запроса (если есть)
            String body = "";
            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                reader.read(bodyChars, 0, contentLength);
                body = new String(bodyChars);
            }
            
            // Создание объекта запроса
            HttpRequest request = new HttpRequest(method, path, headers, body);
            
            // Маршрутизация запросов
            HttpResponse response = routeRequest(request);
            
            // Отправка ответа с правильными CORS заголовками
            sendResponse(writer, response, origin);
            
        } catch (IOException e) {
            logger.warning("Error handling request".concat(" -> ").concat(String.valueOf(e)));
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.warning("Error closing socket".concat(" -> ").concat(String.valueOf(e)));
            }
        }
    }
    
    private HttpResponse routeRequest(HttpRequest request) {
        String path   = request.getPath();
        String method = request.getMethod();
        
        // Обработка preflight OPTIONS запросов (CORS)
        if (method.equals("OPTIONS")) {
            return handleOptionsRequest(request);
        }
        
        // Главная страница
        if (method.equals("GET") && (path.equals("/") || path.equals("/index.html"))) {
            return serveStaticFile("index.html");
        }
        
        // Статические файлы из клиентской директории
        if (method.equals("GET") && !path.startsWith("/api/")) {
            return serveStaticFile(path);
        }
        
        // API маршруты
        if (path.startsWith("/api/auth")) {
            return AuthController.handleRequest(request);
        } else if (path.equals("/api/health")) {
            return handleHealthCheck();
        }
        
        return HttpResponse.notFound("{\"success\":false,\"message\":\"Endpoint not found\"}");
    }
    
    private HttpResponse handleOptionsRequest(HttpRequest request) {
        // CORS preflight response - возвращаем пустое тело
        return new HttpResponse(HttpStatusCodes.OK, "OK", new byte[0], "text/plain");
    }
    
    private HttpResponse handleHealthCheck() {
        String json = "{\"status\":\"ok\",\"timestamp\":\"" + System.currentTimeMillis() + "\"}";
        return HttpResponse.ok(json, "application/json");
    }
    
    private HttpResponse serveStaticFile(String path) {
        if (path.equals("/")) path = "/index.html";
        
        try {
            String filePath = "client" + path;
            File file = new File(filePath);
            
            if (!file.exists() || file.isDirectory()) {
                return HttpResponse.notFound("File not found: " + path);
            }
            
            String contentType = getContentType(filePath);
            byte[] content = java.nio.file.Files.readAllBytes(file.toPath());
            
            return new HttpResponse(HttpStatusCodes.OK, "OK", content, contentType);
            
        } catch (IOException e) {
            logger.warning("Error serving static file: ".concat(path).concat(" -> ").concat(String.valueOf(e)));
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
    
    private String getContentType(String filePath) {
        if      (filePath.endsWith(".html")) return "text/html; charset=utf-8";
        else if (filePath.endsWith(".css"))  return "text/css; charset=utf-8";
        else if (filePath.endsWith(".js"))   return "application/javascript; charset=utf-8";
        else if (filePath.endsWith(".json")) return "application/json; charset=utf-8";
        else if (filePath.endsWith(".png"))  return "image/png";
        else if (filePath.endsWith(".jpg"))  return "image/jpeg";
        else if (filePath.endsWith(".jpeg")) return "image/jpeg";
        else if (filePath.endsWith(".ico"))  return "image/x-icon";
        else                                 return "text/plain; charset=utf-8";
    }
    
    private void sendResponse(PrintWriter writer, HttpResponse response, String origin) {
        // Заголовки ответа
        writer.printf("HTTP/1.1 %d %s\r\n", response.getStatusCode(), response.getStatusMessage());
        writer.printf("Content-Type: %s\r\n", response.getContentType());
        writer.printf("Content-Length: %d\r\n", response.getBody().length);
        
        // CORS заголовки - РАЗРЕШАЕМ ЛЮБОЙ ORIGIN
        // Для тестирования и разработки
        if (origin == null || origin.isEmpty()) {
            writer.println("Access-Control-Allow-Origin: *");
            writer.println("Access-Control-Allow-Credentials: false");
        } else {
            writer.println("Access-Control-Allow-Origin: " + origin);
            writer.println("Access-Control-Allow-Credentials: true");
        }
        
        writer.println("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
        writer.println("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");
        writer.println("Access-Control-Max-Age: 86400");
        writer.println("Connection: close");
        writer.println();
        writer.flush();
        
        // Тело ответа
        try {
            clientSocket.getOutputStream().write(response.getBody());
        } catch (IOException e) {
            logger.warning("Error writing response body".concat(" -> ").concat(String.valueOf(e)));
        }
    }
    
    private void sendError(PrintWriter writer, int statusCode, String message) {
        writer.printf("HTTP/1.1 %d %s\r\n", statusCode, message);
        writer.println("Content-Type: text/plain; charset=utf-8");
        writer.println("Access-Control-Allow-Origin: *"); // Разрешаем для ошибок тоже
        writer.println("Connection: close");
        writer.println();
        writer.println(message);
        writer.flush();
    }
}