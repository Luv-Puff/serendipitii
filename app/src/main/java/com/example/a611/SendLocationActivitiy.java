package com.example.a611;

import android.location.Location;

class SendLocationActivitiy {
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
