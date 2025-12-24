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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.energy.monitoring.components.HttpConstructions.ContentTypes;
import com.energy.monitoring.components.HttpConstructions.EndPoints;
import com.energy.monitoring.components.HttpConstructions.JsonBlocks;
import com.energy.monitoring.components.HttpConstructions.Methods;
import com.energy.monitoring.components.HttpStatusCodes;
import com.energy.monitoring.components.JsonResponses;
import com.energy.monitoring.controllers.AuthController;
import com.energy.monitoring.controllers.CoordinatorController;
import com.energy.monitoring.models.HttpRequest;
import com.energy.monitoring.models.HttpResponse;

/* Главный класс обработки http-запросов */
public class HttpHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class); // Объект Logger для текущего класса
    
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
            if (requestLine == null) {
                return;
            }
            
            logger.info("Received HTTP-Request: {}", requestLine);
            
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                sendError(writer, HttpStatusCodes.BAD_REQUEST, "Bad Request");
                return;
            }
            
            String method = requestParts[0];
            String path   = requestParts[1];
            
            Map<String, String> headers = new HashMap<>();
            String line;
            int contentLength = 0;
            String origin     = null;
            
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] headerParts = line.split(":", 2);
                if (headerParts.length == 2) {
                    String headerName  = headerParts[0].trim().toLowerCase();
                    String headerValue = headerParts[1].trim();
                    headers.put(headerName, headerValue);
                    
                    if (headerName.equals(JsonBlocks.CONTENT_LENGTH)) {
                        contentLength = Integer.parseInt(headerValue);
                    } else
                    if (headerName.equals(JsonBlocks.ORIG)) {
                        origin = headerValue;
                    }
                }
            }
            
            String body = "";
            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                reader.read(bodyChars, 0, contentLength);
                body = new String(bodyChars);
            }
            
            HttpRequest request   = new HttpRequest(method, path, headers, body);
            HttpResponse response = routeRequest(request);
            
            sendResponse(writer, response, origin);
            
        } catch (IOException e) {
            logger.error("Error handling request: {}", e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.error("Error closing socket: {}", e.getMessage());
            }
        }
    }
    
    private HttpResponse routeRequest(HttpRequest request) {
        String path   = request.getPath();
        String method = request.getMethod();
        if (method.equals(Methods.OPTIONS)) {
            return handleOptionsRequest();
        } else
        if (method.equals(Methods.GET) && (path.equals(EndPoints.MAIN) || path.equals(EndPoints.MAIN_PAGE))) {
            return serveStaticFile(EndPoints.MAIN_PAGE);
        } else
        if (method.equals(Methods.GET) && !path.startsWith(EndPoints.API)) {
            return serveStaticFile(path);
        } else
        if (path.startsWith(EndPoints.AUTH)) {
            return AuthController.handleRequest(request);
        } else 
        if (path.equals(EndPoints.HEALTH)) {
            return handleHealthCheck();
        } else 
        if (path.startsWith(EndPoints.COORDINATORS)) {
            return CoordinatorController.handleRequest(request);
        } else {
            return HttpResponse.notFound(JsonResponses.formingUniversalResponse(false, "Endpoint not found"));
        }
    }
    
    private HttpResponse handleHealthCheck() {
        String json = JsonResponses.formingHealthCheckResponse(System.currentTimeMillis());
        
        return HttpResponse.ok(json, ContentTypes.JSON);
    }
    
    private HttpResponse handleOptionsRequest() {
        return new HttpResponse(HttpStatusCodes.OK, "OK", new byte[0], ContentTypes.PLAIN);
    }

    private HttpResponse serveStaticFile(String path) {
        try {
            String filePath = JsonBlocks.CLIENT + path;
            File file       = new File(filePath);
            
            if (!file.exists() || file.isDirectory()) {
                return HttpResponse.notFound("File not found: " + path);
            }
            
            String contentType = getContentType(filePath);
            byte[] content = java.nio.file.Files.readAllBytes(file.toPath());
            
            return new HttpResponse(HttpStatusCodes.OK, "OK", content, contentType);
            
        } catch (IOException e) {
            logger.error("Error serving static file {}: {}",path, e.getMessage());
            return HttpResponse.error(HttpStatusCodes.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
    
    private String getContentType(String filePath) {
        if (filePath.endsWith(".html")) {
            return ContentTypes.HTML;
        }else
        if (filePath.endsWith(".css")) {
            return ContentTypes.CSS;
        } else
        if (filePath.endsWith(".js")) {
            return ContentTypes.JS;
        } else 
        if (filePath.endsWith(".json")) {
            return ContentTypes.JSON;
        }else 
        if (filePath.endsWith(".png")) {
            return ContentTypes.PNG;
        }else 
        if (filePath.endsWith(".jpg"))  {
            return ContentTypes.JPEG;
        }else 
        if (filePath.endsWith(".jpeg")) {
            return ContentTypes.JPEG;
        }else 
        if (filePath.endsWith(".ico")) {
            return ContentTypes.ICO;
        }else {
            return ContentTypes.PLAIN;
        }
    }
    
    private void sendResponse(PrintWriter writer, HttpResponse response, String origin) {
        writer.printf("HTTP/1.1 %d %s\r\n",     response.getStatusCode(), response.getStatusMessage());
        writer.printf("Content-Type: %s\r\n",   response.getContentType());
        writer.printf("Content-Length: %d\r\n", response.getBody().length);
        
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
        
        try {
            clientSocket.getOutputStream().write(response.getBody());
        } catch (IOException e) {
            logger.error("Error writing response body: ", e.getMessage());
        }
    }
    
    private void sendError(PrintWriter writer, int statusCode, String message) {
        writer.printf ("HTTP/1.1 %d %s\r\n", statusCode, message);
        writer.println("Content-Type: text/plain; charset=utf-8");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println("Connection: close");
        writer.println();
        writer.println(message);
        writer.flush();
    }
}