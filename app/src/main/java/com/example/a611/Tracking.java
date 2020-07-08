package com.example.a611;

public class Tracking {
    private String email,Lat,Long;

    public Tracking(){

    }

    public Tracking(String email, String lat, String aLong) {
        this.email = email;
        Lat = lat;
        Long = aLong;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLat(String lat) {
        Lat = lat;
    }

    public void setLong(String aLong) {
        Long = aLong;
    }

    public String getEmail() {
        return email;
    }

    public String getLat() {
        return Lat;
    }

    public String getLong() {
        return Long;
    }
}
