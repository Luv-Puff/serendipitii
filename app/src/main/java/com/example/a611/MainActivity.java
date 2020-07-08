package com.example.a611;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    Button requstLocationButton,removeLocationButton,Loginbtn,usrListbtn;
    MyBackgroundService myBackgroundService = null;
    boolean mBound =false;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    DatabaseReference onlineRef,counterRef,currentUserRef,locations;

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
        Dexter.withContext(this).withPermissions(Arrays.asList(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )).withListener(new MultiplePermissionsListener() {

            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                requstLocationButton = findViewById(R.id.requset_Locrion_button);
                removeLocationButton = findViewById(R.id.remove_Location_button);


                requstLocationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LocationManager lm = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
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


        locations = FirebaseDatabase.getInstance().getReference().child("Locations");
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        counterRef = FirebaseDatabase.getInstance().getReference("lastOnline");
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            currentUserRef = FirebaseDatabase.getInstance().getReference("lastOnline").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }




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
                            Toast.makeText(MainActivity.this,"歡迎，"+user.getEmail(),Toast.LENGTH_LONG).show();
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

}
