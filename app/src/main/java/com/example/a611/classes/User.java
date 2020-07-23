package com.example.a611.classes;

public class User {
    private String email,status,UID;

    

    public User(String email, String status, String token) {
        this.email = email;
        this.status = status;
        UID = token;
    }

    public User(){

    }
    public String getUID() {
        return UID;
    }

    public void setUID(String token) {
        UID = token;
    }
    

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
