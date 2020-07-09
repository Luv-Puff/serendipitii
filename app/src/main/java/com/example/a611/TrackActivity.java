package com.example.a611;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TrackActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    DatabaseReference locations;
    private Location mLastLocation;
    Double Lat,Long;
    ArrayList<String> emailList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locations = FirebaseDatabase.getInstance().getReference("Locations");
        if (getIntent()!=null){
            emailList = getIntent().getStringArrayListExtra("email");
        }

        for (String email :emailList){
            if (!TextUtils.isEmpty(email)){
                loadLocationForUser(email);
            }
        }

    }

    private void loadLocationForUser(String email){
        Query user_Location = locations.orderByChild("email").equalTo(email);
        user_Location.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Location currentUser =new Location("");
                displayLocation(currentUser);
                Location Friendlocation = new Location("");
                for (DataSnapshot snapshot :dataSnapshot.getChildren()){
                    Tracking t = snapshot.getValue(Tracking.class);
                    LatLng FriendlatLng = new LatLng(Double.parseDouble(t.getLat()),Double.parseDouble(t.getLong()));
                    Friendlocation.setLatitude(Double.parseDouble(t.getLat()));
                    Friendlocation.setLongitude(Double.parseDouble(t.getLong()));

                    mMap.addMarker(new MarkerOptions().position(FriendlatLng).title(t.getEmail()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(FriendlatLng,18.0f));
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayLocation(final Location currentUser) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED )
        {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()){
                    mLastLocation = task.getResult();

                    if (mLastLocation != null){

                        currentUser.setLatitude(mLastLocation.getLatitude());currentUser.setLongitude(mLastLocation.getLongitude());
                        Log.d("AAAA",""+currentUser.getLatitude()+"    "+currentUser.getLongitude());
                        LatLng myLatLng = new LatLng(currentUser.getLatitude(),currentUser.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(myLatLng).title("Me"));
                        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
                            locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new Tracking(
                                    FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                                    String.valueOf(mLastLocation.getLatitude()),
                                    String.valueOf(mLastLocation.getLongitude())
                            ) );
                        }

                    }else {
                        Toast.makeText(TrackActivity.this,"I just can't.",Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(TrackActivity.this,"I just cannot.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}

