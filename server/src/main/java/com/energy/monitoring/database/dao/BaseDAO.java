package com.energy.monitoring.database.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.energy.monitoring.database.JDBC;

/* Методы установки соединения с базой данных */
public abstract class BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(BaseDAO.class); // Объект Logger для текущего класса

    protected Connection connection;
    
    public BaseDAO() {
        try {
            this.connection = JDBC.getConnection();
        } catch (SQLException e) {
            logger.error("Failed to get database connection: {}", e.getMessage());
            throw new RuntimeException("Database connection failed: {}", e);
        }
    }
    
    protected Connection getConnection() throws SQLException {
        return JDBC.getConnection();
    }
}