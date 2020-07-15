package com.example.a611.classes;

import android.location.Location;

public class SendLocationActivitiy {
    private  Location location;
    public SendLocationActivitiy(Location mlocation) {
        this.location =mlocation;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
