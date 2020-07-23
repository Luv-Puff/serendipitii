package com.example.a611.SendNotificationPack;

public class Invitation {
    private String Title;
    private String Message;
    private String IID;

    public Invitation(String title, String message, String IID) {
        Title = title;
        Message = message;
        this.IID = IID;
    }

    public Invitation() {
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getIID() {
        return IID;
    }

    public void setIID(String IID) {
        this.IID = IID;
    }
}
