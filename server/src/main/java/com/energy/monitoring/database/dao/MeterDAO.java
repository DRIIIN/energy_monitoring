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
import com.energy.monitoring.models.Meter;

/* Методы для взаимодействия с тааблицей приборов учёта */
public class MeterDAO {
    public Meter createMeter(int coordinatorId, String zbLongAddr, short zbShortAddr, String name) throws SQLException {
        String sql = SqlRequests.Meter.CREATE_METER;

        try (Connection connection = JDBC.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt   (1, coordinatorId);
            stmt.setString(2, zbLongAddr);
            stmt.setInt   (3, zbShortAddr);
            stmt.setString(4, name);
            stmt.setString(5, "online");
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating meter failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new Meter (
                        generatedKeys.getInt(1),
                        coordinatorId,
                        zbLongAddr,
                        zbShortAddr,
                        name,
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

    public List<Meter> getMetersByCoordinator(int coordinatorId) throws SQLException {
        String sql = SqlRequests.Meter.GET_METERS_BY_COORDINATOR;
        List<Meter> meters = new ArrayList<>();
        
        try (Connection connection  = JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, coordinatorId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Meter meter = new Meter(
                        rs.getInt      (DataBaseFildNames.Tables.Meter.ID),
                        rs.getInt      (DataBaseFildNames.Tables.Meter.COORDINATOR_ID),
                        rs.getString   (DataBaseFildNames.Tables.Meter.ZB_LONG_ADDR),
                        rs.getShort    (DataBaseFildNames.Tables.Meter.ZB_SHORT_ADDR),
                        rs.getString   (DataBaseFildNames.Tables.Meter.NAME),
                        rs.getString   (DataBaseFildNames.Tables.Meter.STATUS),
                        rs.getTimestamp(DataBaseFildNames.Tables.Meter.CREATED_AT),
                        rs.getTimestamp(DataBaseFildNames.Tables.Meter.LAST_SEEN),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.VOLTAGE),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.CURRENT),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.ACTIVE_POWER),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.REACTIVE_POWER),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.APPARENT_POWER),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.POWER_FACTOR),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.FREQUENCY),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.NEUTRAL_CURRENT)
                    );
                    meters.add(meter);
                }
            }
        }
        return meters;
    }

    public Meter getMeter(int meterId) throws SQLException {
        String sql = SqlRequests.Meter.GET_METER;
        
        try (Connection connection  = JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, meterId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Meter(
                        rs.getInt      (DataBaseFildNames.Tables.Meter.ID),
                        rs.getInt      (DataBaseFildNames.Tables.Meter.COORDINATOR_ID),
                        rs.getString   (DataBaseFildNames.Tables.Meter.ZB_LONG_ADDR),
                        rs.getShort    (DataBaseFildNames.Tables.Meter.ZB_SHORT_ADDR),
                        rs.getString   (DataBaseFildNames.Tables.Meter.NAME),
                        rs.getString   (DataBaseFildNames.Tables.Meter.STATUS),
                        rs.getTimestamp(DataBaseFildNames.Tables.Meter.CREATED_AT),
                        rs.getTimestamp(DataBaseFildNames.Tables.Meter.LAST_SEEN),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.VOLTAGE),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.CURRENT),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.ACTIVE_POWER),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.REACTIVE_POWER),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.APPARENT_POWER),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.POWER_FACTOR),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.FREQUENCY),
                        rs.getDouble   (DataBaseFildNames.Tables.Meter.NEUTRAL_CURRENT)
                    );
                }
            }
        }
        return null;
    }
    
    public void updateMeterData(int meterId, double voltage, double current, double activePower, double reactivePower, 
                                double apparentPower, double powerFactor, double frequency, double neutralCurrent) throws SQLException {
        String sql = SqlRequests.Meter.UPDATE_METER_DATA;
        
        try (Connection connection  = JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setDouble   (1,  voltage);
            stmt.setDouble   (2,  current);
            stmt.setDouble   (3,  activePower);
            stmt.setDouble   (4,  reactivePower);
            stmt.setDouble   (5,  apparentPower);
            stmt.setDouble   (6,  powerFactor);
            stmt.setDouble   (7,  frequency);
            stmt.setDouble   (8,  neutralCurrent);
            stmt.setTimestamp(9,  new Timestamp(System.currentTimeMillis()));
            stmt.setInt      (10, meterId);
            
            stmt.executeUpdate();
        }
    }

    public void updateMeterStatus(int meterId, String status) throws SQLException {
        String sql = SqlRequests.Meter.UPDATE_METER_STATUS;
        
        try (Connection connection  = JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString   (1, status);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setInt      (3, meterId);
            
            stmt.executeUpdate();
        }
    }
    
    public void deleteMeter(int meterId) throws SQLException {
        String sql = SqlRequests.Meter.DELETE_METER;
        
        try (Connection connection  = JDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, meterId);
            stmt.executeUpdate();
        }
    }
}