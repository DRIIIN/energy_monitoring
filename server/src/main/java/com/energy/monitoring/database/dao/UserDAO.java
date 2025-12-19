package com.energy.monitoring.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.energy.monitoring.database.JDBC;
import com.energy.monitoring.models.User;

public class UserDAO {

    public User createUser(String login, String password) throws SQLException {
        String sql = "INSERT INTO users (login, password, is_active) VALUES (?, ?, ?)";
        
        try (Connection connection =  JDBC.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, login);
            stmt.setString(2, password);
            stmt.setInt(3, 0);
            
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
        String sql = "SELECT * FROM users WHERE login = ? AND password = ?";
        
        try (Connection connection = com.energy.monitoring.database.JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, login);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("login"),
                        rs.getString("password"),
                        rs.getInt("is_active")
                    );
                }
            }
        }
        return null;
    }
    
    public boolean userExists(String login) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM users WHERE login = ?";
        
        try (Connection connection = com.energy.monitoring.database.JDBC.getConnection();
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
        String sql = "UPDATE users SET is_active = 1 WHERE id = ?";
        
        try (Connection connection = com.energy.monitoring.database.JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    
    public void deactivateUser(int userId) throws SQLException {
        String sql = "UPDATE users SET is_active = 0 WHERE id = ?";
        
        try (Connection connection = com.energy.monitoring.database.JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}