package com.example.a611.classes;

public class User {
    private String email,status,Token;

    public User(){

    }
    public User(String email,String status){
        this.email = email;
        this.status = status;
        return;
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
