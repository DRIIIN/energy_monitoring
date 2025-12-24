package com.energy.monitoring.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.energy.monitoring.components.DataBaseFildNames;
import com.energy.monitoring.components.SqlRequests;
import com.energy.monitoring.database.JDBC;
import com.energy.monitoring.models.Coordinator;

/* Методы для взаимодействия с тааблицей координаторов */
public class CoordinatorDAO {
    
    public Coordinator createCoordinator(int userId, String name, String ip, int port) throws SQLException {
        String sql = SqlRequests.Coordinator.CREATE_COORDINATOR;
        
        try (Connection connection = JDBC.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt   (1, userId);
            stmt.setString(2, name);
            stmt.setString(3, ip);
            stmt.setInt   (4, port);
            stmt.setString(5, "offline");
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating coordinator failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new Coordinator(
                        generatedKeys.getInt(1),
                        userId,
                        name,
                        ip,
                        port,
                        "offline",
                        null,
                        null
                    );
                } else {
                    throw new SQLException("Creating coordinator failed, no ID obtained.");
                }
            }
        }
    }
    
    public List<Coordinator> getUserCoordinators(int userId) throws SQLException {
        String sql = SqlRequests.Coordinator.GET_USER_COORDINATORS;
        List<Coordinator> coordinators = new ArrayList<>();
        
        try (Connection connection  = JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Coordinator coordinator = new Coordinator(
                        rs.getInt      (DataBaseFildNames.Tables.Coordinator.ID),
                        rs.getInt      (DataBaseFildNames.Tables.Coordinator.USER_ID),
                        rs.getString   (DataBaseFildNames.Tables.Coordinator.NAME),
                        rs.getString   (DataBaseFildNames.Tables.Coordinator.IP),
                        rs.getInt      (DataBaseFildNames.Tables.Coordinator.PORT),
                        rs.getString   (DataBaseFildNames.Tables.Coordinator.STATUS),
                        rs.getTimestamp(DataBaseFildNames.Tables.Coordinator.CREATED_AT),
                        rs.getTimestamp(DataBaseFildNames.Tables.Coordinator.LAST_SEEN)
                    );
                    coordinators.add(coordinator);
                }
            }
        }
        return coordinators;
    }
    
    public Coordinator getCoordinator(int coordinatorId) throws SQLException {
        String sql = SqlRequests.Coordinator.GET_COORDINATOR;
        
        try (Connection connection  = JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, coordinatorId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Coordinator(
                        rs.getInt      (DataBaseFildNames.Tables.Coordinator.ID),
                        rs.getInt      (DataBaseFildNames.Tables.Coordinator.USER_ID),
                        rs.getString   (DataBaseFildNames.Tables.Coordinator.NAME),
                        rs.getString   (DataBaseFildNames.Tables.Coordinator.IP),
                        rs.getInt      (DataBaseFildNames.Tables.Coordinator.PORT),
                        rs.getString   (DataBaseFildNames.Tables.Coordinator.STATUS),
                        rs.getTimestamp(DataBaseFildNames.Tables.Coordinator.CREATED_AT),
                        rs.getTimestamp(DataBaseFildNames.Tables.Coordinator.LAST_SEEN)
                    );
                }
            } catch (SQLException e){
                return null;
            }
        }

        return null;
    }
    
    public void updateCoordinatorStatus(int coordinatorId, String status) throws SQLException {
        String sql = SqlRequests.Coordinator.UPDATE_COORDINATOR_STATUS;
        
        try (Connection connection  = JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString   (1, status);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setInt      (3, coordinatorId);
            
            stmt.executeUpdate();
        }
    }
    
    public void deleteCoordinator(int coordinatorId) throws SQLException {
        String sql = SqlRequests.Coordinator.DELETE_COORDINATOR;
        
        try (Connection connection  = JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, coordinatorId);
            stmt.executeUpdate();
        }
    }
    
    public boolean coordinatorBelongsToUser(int coordinatorId, int userId) throws SQLException {
        String sql = SqlRequests.Coordinator.COORDINATORS_BELONGS_TO_USER;
        
        try (Connection connection  = JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, coordinatorId);
            stmt.setInt(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }
}