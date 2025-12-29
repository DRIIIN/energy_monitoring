package com.energy.monitoring;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.energy.monitoring.config.Config;
import com.energy.monitoring.config.ConfigKeys;
import com.energy.monitoring.database.JDBC;
import com.energy.monitoring.handlers.HttpHandler;

/* Основные методы работы с сервером */
public class Server {
    private static final Logger logger      = LoggerFactory.getLogger(Server.class);      // Объект Logger для текущего класса
    private static final String CONFIG_FILE = "src\\main\\resources\\config\\config.properties"; // Имя файла с конфигурационными параметрами сервера

    private static int PORT;                                              // Текущий порт сервера
    private static int THREAD_POOL_SIZE;                                  // Максимальное количество потоков
    private static int CLIENT_WAITING_TIMEOUT;                            // Таймаут клиента

    private final    int             port;
    private final    ExecutorService threadPool;
    private volatile boolean         isRunning;
    private          ServerSocket    serverSocket;

    public Server(int port) {
        this.port       = port;                                           // Порт сервера
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE); // Количество потоков под задачи
        this.isRunning  = false;                                          // Статус сервера
    }

    // Возвращает true, если сервер активен, иначе - false
    public boolean isRunning() {
        return isRunning;
    }
    
    // Возвращает количество доступных для вычислений потоков
    public int getActiveThreads() {
        return THREAD_POOL_SIZE - ((ThreadPoolExecutor) threadPool).getActiveCount();
    }
    
    // Возвращает количество выполненных задач
    public long getCompletedTasks() {
        return ((ThreadPoolExecutor) threadPool).getCompletedTaskCount();
    }

    // Останавливает работу сервера
    public void shutdown() {
        isRunning = false;
        logger.info("Shutting down server...");
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                logger.info("Server socket closed");
            } catch (IOException e) {
                logger.warn("Error closing server socket: {}", e.getMessage());
            }
        }
        
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(CLIENT_WAITING_TIMEOUT, TimeUnit.MILLISECONDS)) {
                threadPool.shutdownNow();
                logger.warn("Thread pool forced shutdown");
            } else {
                logger.info("Thread pool shutdown complete");
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("Thread pool shutdown interrupted {}", e.getMessage());
        }
        
        logger.info("Server stopped");
    }

    // Начинает работу сервера
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            isRunning = true;
            
            logger.info("==========================================");
            logger.info("Energy Monitoring System Server");
            logger.info("Server started on port: {}", port);
            logger.info("Thread pool size: {}", THREAD_POOL_SIZE);
            logger.info("==========================================");
            
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(CLIENT_WAITING_TIMEOUT);
                    
                    logger.info("New client connected: {}:{}",clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                    
                    threadPool.execute(new HttpHandler(clientSocket));
                } catch (IOException e) {
                    if (isRunning) {
                        logger.error("Error accepting client connection: {}", e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to start server on port {}: {}", port, e.getMessage());
        } finally {
            shutdown();
        }
    }

    public static void main(String[] args) {

        Config.load(CONFIG_FILE);

        String dbUrl      = Config.getString(ConfigKeys.DataBase.URL);
        String dbUser     = Config.getString(ConfigKeys.DataBase.USER);
        String dbPassword = Config.getString(ConfigKeys.DataBase.PASSWORD);
        
        JDBC.configure(dbUrl, dbUser, dbPassword);
        try {
            Connection datBaseConnection = JDBC.getConnection();
            logger.info("Succsessfull getting connection with data-base {}", datBaseConnection.getCatalog());
        } catch (SQLException e) {
            logger.error("Server not started: {}", e.getMessage());
            System.exit(1);
        }

        PORT                   = Config.getInt(ConfigKeys.Server.PORT);
        THREAD_POOL_SIZE       = Config.getInt(ConfigKeys.Server.MAX_THREADS);
        CLIENT_WAITING_TIMEOUT = Config.getInt(ConfigKeys.Server.CLIENT_WEITING_TIMEOUT);

        final Server server = new Server(PORT);
        try {
            server.start();
        } catch (Exception e) {
            logger.error("Fatal error starting server: {}", e.getMessage());
            System.exit(1);
        }
    }

}
