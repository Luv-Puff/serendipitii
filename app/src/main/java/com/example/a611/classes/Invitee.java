package com.example.a611.classes;

public class Invitee {

    private  String UID;
    private  String status;

    public Invitee() {
    }

    public Invitee(String UID, String status) {
        this.UID = UID;
        this.status = status;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String isStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
