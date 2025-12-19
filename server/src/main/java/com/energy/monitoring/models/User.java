package com.energy.monitoring.models;

/* Класс пользователя */
public class User {
    private int     id;
    private String  login;
    private String  password;
    private Boolean isActive;
    
    public User(int id, String login, String password, int isActive) {
        this.id       = id;
        this.login    = login;
        this.password = password;
        this.isActive = (isActive != 0);
    }
    
    public int getId() { 
        return id; 
    }
    
    public String getLogin() { 
        return login; 
    }

    public String getPassword() { 
        return password; 
    }

    public Boolean getIsActive() { 
        return isActive; 
    }

    public void setId(int newId) { 
        id = newId; 
    }
    
    public void setLogin(String newLogin) { 
        login = newLogin; 
    }

    public void setPassword(String newPassword) { 
        password = newPassword; 
    }

    public void setIsActive(Boolean newIsActive) { 
        isActive = newIsActive; 
    }
}