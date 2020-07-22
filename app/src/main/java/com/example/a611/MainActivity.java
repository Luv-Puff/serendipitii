package com.example.a611;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.a611.SendNotificationPack.Token;
import com.example.a611.classes.Common;
import com.example.a611.classes.SendLocationActivitiy;
import com.example.a611.classes.Tracking;
import com.example.a611.classes.User;
import com.example.a611.services.FirebaseMessagingService;
import com.example.a611.services.MyBackgroundService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    Button requstLocationButton,removeLocationButton,Loginbtn,usrListbtn,testbtn;
    MyBackgroundService myBackgroundService = null;
    boolean mBound =false;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    DatabaseReference onlineRef,counterRef,currentUserRef,locations,TokenRef;

    LocationManager lm;
    private Location mLastLocation;

    private static final String CHANNEL_ID = "my_channel";
    private  final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyBackgroundService.LocalBinder binder = (MyBackgroundService.LocalBinder)service;
            myBackgroundService = binder.getService();
            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myBackgroundService = null;
            mBound = false;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

        }

        Dexter.withContext(this).withPermissions(Arrays.asList(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
                //, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )).withListener(new MultiplePermissionsListener() {

            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                requstLocationButton = findViewById(R.id.requset_Locrion_button);
                removeLocationButton = findViewById(R.id.remove_Location_button);


                requstLocationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lm = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
                        boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        boolean network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                        if (!gps_enabled&&!network_enabled){
                            LocationRequest locationRequest = LocationRequest.create();
                            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                                    .addLocationRequest(locationRequest);

                            Task<LocationSettingsResponse> result =
                                    LocationServices.getSettingsClient(MainActivity.this).checkLocationSettings(builder.build());
                            result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                                @Override
                                public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                                    try {
                                        LocationSettingsResponse response = task.getResult(ApiException.class);
                                        // All location settings are satisfied. The client can initialize location
                                        // requests here.
                                    } catch (ApiException exception) {
                                        switch (exception.getStatusCode()) {
                                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                                // Location settings are not satisfied. But could be fixed by showing the
                                                // user a dialog.
                                                try {
                                                    // Cast to a resolvable exception.
                                                    ResolvableApiException resolvable = (ResolvableApiException) exception;
                                                    // Show the dialog by calling startResolutionForResult(),
                                                    // and check the result in onActivityResult().
                                                    resolvable.startResolutionForResult(
                                                            MainActivity.this,
                                                            LocationRequest.PRIORITY_HIGH_ACCURACY);
                                                } catch (IntentSender.SendIntentException e) {
                                                    // Ignore the error.
                                                } catch (ClassCastException e) {
                                                    // Ignore, should be an impossible error.
                                                }
                                                break;
                                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                                // Location settings are not satisfied. However, we have no way to fix the
                                                // settings so we won't show the dialog.
                                                break;
                                        }
                                    }
                                }
                            });
                        }

                        myBackgroundService.requestLocationUpdates();
                    }
                });

                removeLocationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myBackgroundService.removeLocationUpdates();
                    }
                });
                setButtonState(Common.requsetingLocationUpdates(MainActivity.this));
                bindService(new Intent(MainActivity.this,MyBackgroundService.class),serviceConnection,Context.BIND_AUTO_CREATE);

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }
        }).check();


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        mAuth = FirebaseAuth.getInstance();

        Loginbtn= findViewById(R.id.login_btn);
        Loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logStatus();
            }
        });
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            Loginbtn.setText(""+FirebaseAuth.getInstance().getCurrentUser().getEmail()+" Logout");
        }

        usrListbtn = findViewById(R.id.user_list);
        usrListbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,UserListActivity.class);
                startActivity(intent);
            }
        });

        testbtn = findViewById(R.id.Testbtn);
        testbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,TestActivity.class);
                startActivity(intent);
            }
        });




        locations = FirebaseDatabase.getInstance().getReference().child("Locations");
        TokenRef = FirebaseDatabase.getInstance().getReference().child("Tokens");
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        counterRef = FirebaseDatabase.getInstance().getReference("lastOnline");
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            currentUserRef = FirebaseDatabase.getInstance().getReference("lastOnline").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }

        setupDistance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // No user is signed in
        } else {
            // User is  signed in
            counterRef = FirebaseDatabase.getInstance().getReference("lastOnline");
            currentUserRef = FirebaseDatabase.getInstance().getReference("lastOnline").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            currentUserRef.onDisconnect().removeValue();
            counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(),"Online"));
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) { return;  }
                            if( task.getResult() == null)
                                return;
                            // Get new Instance ID token
                            String token = task.getResult().getToken();
                            Token newToken = new Token();
                            newToken.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                            newToken.setToken(token);

                            // Log and toast
                            //Log.i("FCM","firebase token " + token);
                            TokenRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(newToken);
                        }
                    });

        }
    }

    @Override
    protected void onStop() {
        if(mBound){
            unbindService(serviceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Common.KEY_REQUESTING_LOCATION_UPDATE)){
            setButtonState(sharedPreferences.getBoolean(Common.KEY_REQUESTING_LOCATION_UPDATE,false));
        }
    }

    private void setButtonState(boolean b) {
        if (b){// isRequestable
            removeLocationButton.setEnabled(true);
            requstLocationButton.setEnabled(false);

        }else {
            removeLocationButton.setEnabled(false);
            requstLocationButton.setEnabled(true);
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void  onListenLocation(SendLocationActivitiy event){
        if (event != null){
            String data = new StringBuilder()
                    .append(event.getLocation().getLatitude()).append("/**/")
                    .append(event.getLocation().getLongitude()).toString();

            Toast.makeText(myBackgroundService,data,Toast.LENGTH_SHORT).show();

            locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new Tracking(
                    FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                    String.valueOf(event.getLocation().getLatitude()),
                    String.valueOf(event.getLocation().getLongitude())
            ) );
        }
    }
    private void logStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // No user is signed in
            signIn();
        } else {
            // User is  signed in
            logOut();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,101);

    }
    private  void  logOut(){
        currentUserRef.removeValue();
        mAuth.signOut();
        Toast.makeText(MainActivity.this," 已登出",Toast.LENGTH_LONG).show();
        Loginbtn.setText(" Login");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(MainActivity.this,""+e,Toast.LENGTH_LONG).show();
            }
        }else if(requestCode == LocationRequest.PRIORITY_HIGH_ACCURACY){
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    break;
                default:
                    break;
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //Toast.makeText(MainActivity.this,"歡迎，"+user.getEmail(),Toast.LENGTH_LONG).show();
                            counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(),"Online"));
                            FirebaseInstanceId.getInstance().getInstanceId()
                                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                            if (!task.isSuccessful()) { return;  }
                                            if( task.getResult() == null)
                                                return;
                                            // Get new Instance ID token
                                            String token = task.getResult().getToken();

                                            // Log and toast
                                            Log.i("FCM","firebase token " + token);
                                            Token newToken = new Token();
                                            newToken.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                            newToken.setToken(token);

                                            TokenRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(newToken);
                                        }
                                    });

                            Loginbtn.setText(""+user.getEmail()+" Logout");
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this,""+task.getException(),Toast.LENGTH_LONG).show();

                        }

                        // ...
                    }
                });
    }

    private void setupDistance(){
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Boolean.class)){
                    if (FirebaseAuth.getInstance().getCurrentUser()!=null){
                        currentUserRef.onDisconnect().removeValue();
                        counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(),"Online"));
                        FirebaseInstanceId.getInstance().getInstanceId()
                                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                        if (!task.isSuccessful()) { return;  }
                                        if( task.getResult() == null)
                                            return;
                                        // Get new Instance ID token
                                        String token = task.getResult().getToken();

                                        // Log and toast
                                        Log.i("FCM","firebase token " + token);
                                    }
                                });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        counterRef.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String key) {
                User newuser = dataSnapshot.getValue(User.class);
                if (FirebaseAuth.getInstance().getCurrentUser()==null){
                    return;
                }
                if (!newuser.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                    locations.orderByChild("email").equalTo(newuser.getEmail()).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Tracking tracking= dataSnapshot.getValue(Tracking.class);
                            cauculateDistance(tracking);

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String key) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void cauculateDistance(final Tracking t){
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
                        LatLng start = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                        LatLng end = new LatLng(Double.parseDouble(t.getLat()),Double.parseDouble(t.getLong()));
                        double dis = getDistance(start,end);
                        if (dis <= 500){
                            String notiText = "用戶"+t.getEmail()+"在附近，要約嗎?";
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this,CHANNEL_ID)
                                    .setContentText(notiText).setContentTitle("Serendipitii").setSmallIcon(R.mipmap.ic_launcher)
                                    .setPriority(Notification.PRIORITY_HIGH).setAutoCancel(true);
                            PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,0, new Intent(MainActivity.this,MainActivity.class),0);
                            builder.setContentIntent(pendingIntent);
                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            notificationManager.notify(0,builder.build());
                        }
                    }else {
                    }
                }else{
                }
            }
        });

    }

    public double getDistance(LatLng start, LatLng end){
        double lat1 = (Math.PI/180)*start.latitude;
        double lat2 = (Math.PI/180)*end.latitude;

        double lon1 = (Math.PI/180)*start.longitude;
        double lon2 = (Math.PI/180)*end.longitude;

        //地球半徑
        double R = 6371;

        //兩點間距離 km，如果想要米的話，結果*1000就可以了
        double d =  Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1))*R;

        return d*1000;
    }

}
