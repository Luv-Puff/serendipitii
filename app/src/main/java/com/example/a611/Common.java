package com.example.a611;

import android.content.Context;
import android.location.Location;

import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.util.Date;

public class Common {
    public static final String KEY_REQUESTING_LOCATION_UPDATE = "LocationUpdateEnable";
    public  static  String getLocationText(Location location){
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            return location == null?"Unknown Location": new StringBuilder().append(location.getLatitude()).
                    append("/").append(location.getLongitude()).append("/").
                    append(FirebaseAuth.getInstance().getCurrentUser().getEmail()).toString();
        }else{
            return location == null?"Unknown Location": new StringBuilder().append(location.getLatitude()).
                    append("/").
                    append(location.getLongitude()).toString();
        }

    }

    public static CharSequence getLocationTitle(MyBackgroundService myBackgroundService) {
        return String.format("Location Updated: %1$s", DateFormat.getDateInstance().format(new Date()));
    }

    public static void setRequestLocationUpdates(Context context, boolean b) {

        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATE,b).apply();
    }

    public static boolean requsetingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_REQUESTING_LOCATION_UPDATE,false);
    }
}
