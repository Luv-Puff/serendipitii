package com.example.a611;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a611.SendNotificationPack.APIService;
import com.example.a611.SendNotificationPack.Client;
import com.example.a611.SendNotificationPack.Data;
import com.example.a611.SendNotificationPack.Invitation;
import com.example.a611.SendNotificationPack.InvitationSender;
import com.example.a611.SendNotificationPack.MyResponse;
import com.example.a611.SendNotificationPack.NotificationSender;
import com.example.a611.SendNotificationPack.Token;
import com.example.a611.classes.User;
import com.example.a611.classes.inviter;
import com.example.a611.recycler.RecyclerTouchListener;
import com.example.a611.recycler.UserListAdapter;
import com.example.a611.services.MyLocationService;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestActivity extends AppCompatActivity {

    DatabaseReference onlineRef,counterRef,currentUserRef,locations,tokenRef,invititations;

    RecyclerView listOnline;
    private UserListAdapter userListAdapter;

    //Location
    private  static final int PERMISSION_code =7171;
    private static final int Play_service_resquest =7172;
    private LocationRequest locationRequest;
    private Location mLastLocation;

    private static final int Update_interval = 5000;
    private static final int Fastest_interval = 3000;

    private static final int Distance= 10;

    FusedLocationProviderClient fusedLocationClient;

    List<String> userlist = new ArrayList<>();
    private Toolbar toolbar;
    ImageButton bckBtn ,fwBtn;TextView listTxt;
    int selected_counter=0;
    public static boolean enableActionBar = false;

    private APIService apiService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        try {
            this.getActionBar().setDisplayShowTitleEnabled(false);
        }catch (Exception e){

        }

        toolbar = findViewById(R.id.usrList_toolbar);
        setSupportActionBar(toolbar);
        bckBtn = findViewById(R.id.menu_bk_arrow);
        bckBtn.setVisibility(View.GONE);
        fwBtn = findViewById(R.id.menu_foward_arrow);
        fwBtn.setVisibility(View.GONE);
        listTxt = findViewById(R.id.usrList_barTxt);
        listTxt.setVisibility(View.GONE);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);


        listOnline = findViewById(R.id.userlistrecycler);
        listOnline.setLayoutManager(new LinearLayoutManager(this));
        locations = FirebaseDatabase.getInstance().getReference().child("Locations");
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        counterRef = FirebaseDatabase.getInstance().getReference("lastOnline") ;
        tokenRef = FirebaseDatabase.getInstance().getReference("Tokens") ;
        invititations = FirebaseDatabase.getInstance().getReference("Invitations");
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            currentUserRef = FirebaseDatabase.getInstance().getReference("lastOnline").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
        //firebase
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(counterRef, User.class)
                        .build();
        userListAdapter = new UserListAdapter(this,options);
        listOnline.setAdapter( userListAdapter);
        listOnline.addOnItemTouchListener(new RecyclerTouchListener(this, listOnline, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                TextView uid_textView = view.findViewById(R.id.usr_UID);
                String selected_uid = uid_textView.getText().toString();
                TextView email_textView = view.findViewById(R.id.usr_email);
                String selected_email = email_textView.getText().toString();
                

                if (!email_exist(selected_uid,userlist)){
                    userlist.add(selected_uid);
                    selected_counter ++;
                    if (selected_counter>0){
                        enableActionBar = true;
                        bckBtn.setVisibility(View.VISIBLE);
                        fwBtn.setVisibility(View.VISIBLE);
                        listTxt.setText(""+selected_counter+" selected");
                        listTxt.setVisibility(View.VISIBLE);
                    }

                    Toast.makeText(TestActivity.this,""+selected_email+" added",Toast.LENGTH_SHORT).show();
                }else{
                    userlist.remove(selected_uid);
                    selected_counter --;
                    listTxt.setText(""+selected_counter+" selected");
                    if (selected_counter == 0){
                        enableActionBar = false;
                        listTxt.setText("");
                        listTxt.setVisibility(View.GONE);
                        bckBtn.setVisibility(View.GONE);
                        fwBtn.setVisibility(View.GONE);
                        userListAdapter.notifyDataSetChanged();
                    }
                    Toast.makeText(TestActivity.this,""+selected_email+" deleted" ,Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION
            },PERMISSION_code);
        }else{
            updateLocation();
        }

        bckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableActionBar = false;
                selected_counter = 0;
                listTxt.setText("");
                listTxt.setVisibility(View.GONE);
                bckBtn.setVisibility(View.GONE);
                fwBtn.setVisibility(View.GONE);
                userlist.clear();
                userListAdapter.notifyDataSetChanged();
            }
        });
        fwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String randaomString = random();
                inviter newinviter  =new inviter(FirebaseAuth.getInstance().getCurrentUser().getUid());
                invititations.child(randaomString).setValue(newinviter);
                for (String uid:userlist){
                    invititations.child(randaomString).child(uid).setValue("");
                    tokenRef.orderByKey().equalTo(uid).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Token token = dataSnapshot.getValue(Token.class);
                            //Log.d("TUSK",""+token.getToken());
                            sendInvitations(token.getToken(),"TestTitle",FirebaseAuth.getInstance().getCurrentUser().getEmail()+" Test Message",randaomString);
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
        });

        setUpSystem();
    }


    private void updateLocation(){
        createLocationRequest();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,getPendingIntent());
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, MyLocationService.class);
        intent.setAction(MyLocationService.ACTION_PROCESS_UPDATE);

        return PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(Update_interval);
        locationRequest.setFastestInterval(Fastest_interval);
        locationRequest.setSmallestDisplacement(Distance);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    protected void onStart() {
        super.onStart();
        userListAdapter.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        //currentUserRef.removeValue();
        userListAdapter.stopListening();
    }

    public void sendInvitations(String usertoken, String title, String message,String IID) {
        Invitation data = new Invitation(title, message,IID);
        InvitationSender sender = new InvitationSender(data,usertoken);
        //NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendInvitation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Toast.makeText(TestActivity.this, "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }



    private boolean email_exist(String search,List<String> myList){
        for(String str: myList) {
            if(str.trim().contains(search))
                return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_code:
                if (grantResults.length>0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateLocation();

                }
        }
    }

    private void setUpSystem() {
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Boolean.class)){
                    if (FirebaseAuth.getInstance().getCurrentUser()!=null){
                        currentUserRef.onDisconnect().removeValue();
                        counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(),"Online",FirebaseAuth.getInstance().getCurrentUser().getUid()));
                    }

                    userListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        counterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (userlist.size()>0){
                    for (String email :userlist){
                        boolean ifexist = false;
                        for (DataSnapshot Postsnapshot:dataSnapshot.getChildren()){
                            User user = Postsnapshot.getValue(User.class);
                            if (user.getEmail().equals(email)){
                                ifexist = true;
                                break;
                            }
                        }
                        if (!ifexist){
                            userlist.remove(email);
                            selected_counter --;
                            listTxt.setText(""+selected_counter+" selected");
                            if (selected_counter == 0){
                                enableActionBar = false;
                                listTxt.setText("");
                                listTxt.setVisibility(View.GONE);
                                bckBtn.setVisibility(View.GONE);
                                fwBtn.setVisibility(View.GONE);
                                userListAdapter.notifyDataSetChanged();
                            }
                            Toast.makeText(TestActivity.this,""+email+" disappered" ,Toast.LENGTH_SHORT).show();
                        }
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public static String random() {
        Random generator = new Random();
        char[] chars= "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = 12;
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = chars[generator.nextInt(chars.length)];
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
