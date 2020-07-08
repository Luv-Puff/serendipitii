package com.example.a611;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyLocationService extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATE = "com.example.kiss123.UPDATE_ACTION";
    DatabaseReference databaseReference;
    public MyLocationService() {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Locations");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent!=null){
            final String action = intent.getAction();
            if (action.equals(ACTION_PROCESS_UPDATE)){
                LocationResult result = LocationResult.extractResult(intent);
                if (result!=null){
                    Location location = result.getLastLocation();
                    try {
                        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new Tracking(
                                FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                                String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude())
                        ));

                    }catch (Exception e){
                        Toast.makeText(context,String.valueOf(location.getLatitude())+String.valueOf(location.getLongitude()),Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

    }


}
