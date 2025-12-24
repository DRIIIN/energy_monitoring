package com.energy.monitoring.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.energy.monitoring.components.DataBaseFildNames;
import com.energy.monitoring.components.SqlRequests;
import com.energy.monitoring.database.JDBC;
import com.energy.monitoring.models.User;
import com.energy.monitoring.utils.PasswordHasher;

/* Методы для взаимодействия с тааблицей пользователей */
public class UserDAO {
    public User createUser(String login, String password) throws SQLException {
        String sql = SqlRequests.User.CREATE_USER;
        
        try (Connection connection  = JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, login);
            stmt.setString(2, PasswordHasher.hashPassword(password));
            stmt.setInt   (3, 0);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new User(
                        generatedKeys.getInt(1),
                        login,
                        password,
                        0
                    );
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }
    
    public User authenticate(String login, String password) throws SQLException {
        String sql = SqlRequests.User.AUTENTIFICATE;
        
        try (Connection connection = JDBC.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, login);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString(DataBaseFildNames.Tables.User.PASSWORD);
                    if (PasswordHasher.verifyPassword(password, storedHash)) {
                        return new User(
                            rs.getInt   (DataBaseFildNames.Tables.User.ID),
                            rs.getString(DataBaseFildNames.Tables.User.LOGIN),
                            storedHash,
                            rs.getInt   (DataBaseFildNames.Tables.User.IS_ACTIVE)
                        );
                    }
                }
            }
        }
        return null;
    }
    
    public boolean userExists(String login) throws SQLException {
        String sql = SqlRequests.User.USER_EXISTS;
        
        try (Connection connection  = JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, login);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }
    
    public void activateUser(int userId) throws SQLException {
        String sql = SqlRequests.User.ACTIVATE_USER;
        
        try (Connection connection  = JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    
    public void deactivateUser(int userId) throws SQLException {
        String sql = SqlRequests.User.DEACTIVATE_USER;
        
        try (Connection connection  = JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}