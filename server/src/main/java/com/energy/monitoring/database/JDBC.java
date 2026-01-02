package com.energy.monitoring.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* API для общения с базой данных */
public class JDBC {
    private static final Logger logger = LoggerFactory.getLogger(JDBC.class); // Объект Logger для текущего класса

    private static String url;      // Ссылка для доступа к базе данных
    private static String user;     // Пользоветель базы данных
    private static String password; // Пароль пользователя
    
    static {
        try {
            // Class.forName("com.mysql.cj.jdbc.Driver");
            // logger.info("MySQL JDBC Driver loaded successfully");
            Class.forName("org.postgresql.Driver");
            logger.info("PostgreSQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            // logger.error("Failed to load MySQL JDBC Driver: {}", e.getMessage());
            // throw new RuntimeException("MySQL JDBC Driver not found: {}", e);
            logger.error("Failed to load PostgreSQL JDBC Driver: {}", e.getMessage());
            throw new RuntimeException("PostgreSQL JDBC Driver not found: {}", e);
        }
    }
    
    public static void configure(String url, String user, String password) {
        JDBC.url      = url;
        JDBC.user     = user;
        JDBC.password = password;
    }
    
    // Возвращает объект Connection текущего соединения с базой данных
    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user",     user);
        props.setProperty("password", password);
        
        return DriverManager.getConnection(url, props);
    }
    
    // Закрывает соединение connection с базой данных
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("Error closing connection: {}", e.getMessage());
            }
        }
    }
    
    // Останавливает текущее соединение с базой данных
    public static void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            logger.error("Error closing ResultSet {}", e.getMessage());
        }
        
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            logger.error("Error closing Statement: {}", e.getMessage());
        }
        
        closeConnection(conn);
    }
}