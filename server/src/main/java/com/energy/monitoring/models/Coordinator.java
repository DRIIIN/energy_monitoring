package com.energy.monitoring.models;

import java.sql.Timestamp;

/* Класс координатора */
public class Coordinator {
    private int       id;
    private int       userId;
    private String    name;
    private String    mac;
    private String    ip;
    private int       port;
    private String    status;
    private Timestamp createdAt;
    private Timestamp lastSeen;

    public Coordinator(int id, int userId, String name, String mac, String ip, int port, String status, Timestamp createdAt, Timestamp lastSeen) {
        this.id        = id;
        this.userId    = userId;
        this.name      = name;
        this.mac       = mac;
        this.ip        = ip;
        this.port      = port;
        this.status    = status;
        this.createdAt = createdAt;
        this.lastSeen  = lastSeen;
    }
    
    public int getId() { 
        return id; 
    }
    
    public int getUserId() { 
        return userId; 
    }

    public String getName() { 
        return name; 
    }

    public String getMac() { 
        return mac; 
    }

    public String getIp() { 
        return ip; 
    }

    public int getPort() { 
        return port; 
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

    public void setId(int newId) { 
        id = newId; 
    }
    
    public void setUserId(int newUserId) { 
        userId = newUserId; 
    }

    public void setName(String newName) { 
        name = newName; 
    }

    public void setMac(String newMac) { 
        mac = newMac; 
    }

    public void setIp(String newIp) { 
        ip = newIp; 
    }

    public void setPort(int newPort) { 
        port = newPort; 
    }

    public void setStatus(String newStatus) { 
        status = newStatus; 
    }

    public void setCreatedAt(Timestamp newCreatedAt) { 
        createdAt = newCreatedAt; 
    }

    public void setLastSeen(Timestamp newLastSeen) { 
        lastSeen = newLastSeen; 
    }
}
