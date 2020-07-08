package com.example.a611;

public class User {
    private String email,status;

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
}
