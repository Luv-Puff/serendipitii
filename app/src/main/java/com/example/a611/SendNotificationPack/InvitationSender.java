package com.example.a611.SendNotificationPack;

public class InvitationSender {
    public Invitation data;
    public String to;

    public InvitationSender() {
    }

    public InvitationSender(Invitation data, String to) {
        this.data = data;
        this.to = to;
    }
}
