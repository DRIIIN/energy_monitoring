package com.energy.monitoring.models;

import java.sql.Timestamp;

/* Класс прибора учёта */
public class Metter {
    private int       id;
    private int       coordinatorId;
    private String    zbLongAddr;
    private short     zbShortAddr;
    private String    name;
    private String    status;
    private Timestamp createdAt;
    private Timestamp lastSeen;
    private double    voltage;
    private double    current;
    private double    activePower;
    private double    reactivePower;
    private double    apparentPower;
    private double    powerFactor;
    private double    frequency;
    private double    neutralCurrent;

    public Metter(int id, int coordinatorId, String zbLongAddr, short zbShortAddr, String name, String status, Timestamp createdAt, Timestamp lastSeen) {
        this.id            = id;
        this.coordinatorId = coordinatorId;
        this.zbLongAddr    = zbLongAddr;
        this.zbShortAddr   = zbShortAddr;
        this.name          = name;
        this.status        = status;
        this.createdAt     = createdAt;
        this.lastSeen      = lastSeen;
    }

    public int getId() {
        return id;
    }
    
    public int getCoordinatorId() {
        return coordinatorId;
    }
    
    public String getZbLongAddr() {
        return zbLongAddr;
    }
    
    public short getZbShortAddr() {
        return zbShortAddr;
    }
    
    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getLastSeen() {
        return lastSeen;
    }

    public double getVoltage() {
        return voltage;
    }
    
    public double getCurrent() {
        return current;
    }
    
    public double getActivePower() {
        return activePower;
    }
    
    public double getReactivePower() {
        return reactivePower;
    }
    
    public double getApparentPower() {
        return apparentPower;
    }
    
    public double getPowerFactor() {
        return powerFactor;
    }
    
    public double getFrequency() {
        return frequency;
    }

    public double getNeutralCurrent() {
        return neutralCurrent;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCoordinatorId(int coordinatorId) {
        this.coordinatorId = coordinatorId;
    }

    public void setZbLongAddr(String zbLongAddr) {
        this.zbLongAddr = zbLongAddr;
    }

    public void setZbShortAddr(short zbShortAddr) {
        this.zbShortAddr = zbShortAddr;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastSeen(Timestamp lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

    public void setActivePower(double activePower) {
        this.activePower = activePower;
    }

    public void setReactivePower(double reactivePower) {
        this.reactivePower = reactivePower;
    }

    public void setApparentPower(double apparentPower) {
        this.apparentPower = apparentPower;
    }

    public void setPowerFactor(double powerFactor) {
        this.powerFactor = powerFactor;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public void setNeutralCurrent(double neutralCurrent) {
        this.neutralCurrent = neutralCurrent;
    }
}