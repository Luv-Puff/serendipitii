package com.example.a611.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.a611.MainActivity;
import com.example.a611.R;
import com.example.a611.classes.Common;
import com.example.a611.classes.SendLocationActivitiy;
import com.example.a611.classes.Tracking;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;

public class MyBackgroundService extends Service {

    private static final String CHANNEL_ID = "my_channel";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = "com.example.a611"+".started_fromed_notification";

    private IBinder mBinder = new LocalBinder();
    private static final long UPDATE_INTERVAL_IN_MUL = 10000;
    private static final long FASTESET_UPDATE_INTERVAL_IN_MUL = UPDATE_INTERVAL_IN_MUL/2;
    private static final int NOTI_ID = 1223;
    private boolean mChangingConfig = false;
    private NotificationManager mnotificationManager;

    private LocationRequest locationRequest;
    private LocationSettingsRequest.Builder builder;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Handler mServiceHandeler;
    private Location mlocation;

    public MyBackgroundService() {
    }
    @Override
    public void onCreate(){
       // super.onCreate();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };
        createLocationRequest();
        getLastLoction();
        HandlerThread handlerThread =  new HandlerThread("");
        handlerThread.start();
        mServiceHandeler = new Handler(handlerThread.getLooper());
        mnotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW);
            mChannel.setSound(null,null);
            mnotificationManager.createNotificationChannel(mChannel);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,false);
        if (startedFromNotification){
            removeLocationUpdates();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfig = true;

    }

    public void removeLocationUpdates() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            Common.setRequestLocationUpdates(this,false);
            stopSelf();
        }catch (SecurityException e){
            Common.setRequestLocationUpdates(this,true);
            Log.e("AAA","Lost permission. Could not remove updates");
        }
    }

    private void getLastLoction() {
        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if(task.isSuccessful()&&task.getResult()!=null){
                                mlocation = task.getResult();
                            }else{
                                Log.d("AAA","Failed to get location");
                            }

                        }
                    });
        }catch (Exception e){
            Log.d("AAA","Lost location Permission. "+e);
        }
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MUL);
        locationRequest.setFastestInterval(FASTESET_UPDATE_INTERVAL_IN_MUL);
        //locationRequest.setSmallestDisplacement(10f);
    }

    private void  onNewLocation(Location lastLocation){
        mlocation = lastLocation;
        EventBus.getDefault().postSticky(new SendLocationActivitiy(mlocation));

        if (serviceIsRunningInForeGround(this)) {
            mnotificationManager.notify(NOTI_ID,getNotification());
            //mnotificationManager.notify(NOTI_ID,new Notification.Builder());
        }
    }

    private boolean serviceIsRunningInForeGround(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo: manager.getRunningServices(Integer.MAX_VALUE)){
            if (getClass().getName().equals(serviceInfo.service.getClassName())){
                if (serviceInfo.foreground){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");

        stopForeground(true);
        mChangingConfig = false;
        return  mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        stopForeground(true);
        mChangingConfig = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (!mChangingConfig&& Common.requsetingLocationUpdates(this)){
            startForeground(NOTI_ID,getNotification());
        }
        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandeler.removeCallbacks(null);
        super.onDestroy();
    }

    public Notification getNotification() {
        Intent intent = new Intent(this,MyBackgroundService.class);
        String text = Common.getLocationText(mlocation);

        if (mlocation!=null&& FirebaseAuth.getInstance().getCurrentUser()!=null){
            DatabaseReference locations = FirebaseDatabase.getInstance().getReference().child("Locations");
            locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new Tracking(
                    FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                    String.valueOf(mlocation.getLatitude()),
                    String.valueOf(mlocation.getLongitude())
            ) );
        }

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION,true);
        PendingIntent servicePendingIntent = PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this,0,
                new Intent(this, MainActivity.class),0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setDefaults(0).setSound(null).setVibrate(null)
                .addAction(R.drawable.ic_launch_black_24dp,"Launch",activityPendingIntent)
                .addAction(R.drawable.ic_cancel_black_24dp,"Stop Service", servicePendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(text).setContentTitle(Common.getLocationTitle(this)).setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW).setTicker(text).setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            builder.setChannelId(CHANNEL_ID);
        }
        return builder.build();
    }

    public void requestLocationUpdates() {
        Common.setRequestLocationUpdates(this,true);
        startService(new Intent(getApplicationContext(),MyBackgroundService.class));

        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
        }catch (SecurityException e){
            Log.e("AAAA","Lost location permission. Could not requset:"+e);
        }
    }

//    public void setNotification(Notification notification) {
//        this.notification = notification;
//    }

    public class LocalBinder extends Binder {
        public MyBackgroundService getService(){
            return MyBackgroundService.this;
        }
    }
}
