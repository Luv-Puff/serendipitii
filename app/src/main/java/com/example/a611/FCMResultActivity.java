package com.example.a611;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.a611.SendNotificationPack.Token;
import com.example.a611.classes.Invitee;
import com.example.a611.classes.User;
import com.example.a611.classes.inviter;
import com.example.a611.recycler.inviteelistAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FCMResultActivity extends AppCompatActivity {
    TextView inviter_view;
    RecyclerView recyclerView;
    Button button,button2;
    DatabaseReference InviteRef,TokenRef;
    String Invitation_id;
    inviteelistAdapter inviteelistAdapter;
    ArrayList<Invitee> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fcmresult);
        inviter_view = findViewById(R.id.inviter_text);
        recyclerView = findViewById(R.id.InviteeRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        button = findViewById(R.id.invitedbutton);
        button2 = findViewById(R.id.invitedbutton2);
        InviteRef = FirebaseDatabase.getInstance().getReference("Invitations");
        TokenRef = FirebaseDatabase.getInstance().getReference("Tokens");
        Invitation_id = getIntent().getStringExtra("IID");
        InviteRef.child(Invitation_id).child("inviter_uid").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                inviter inviter = new inviter();
                inviter.setInviter_uid(dataSnapshot.getValue().toString());
                TokenRef.child(inviter.getInviter_uid()).child("email").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        inviter_view.setText("Inviter:"+dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        arrayList = new ArrayList<>();
        inviteelistAdapter = new inviteelistAdapter(this,arrayList);
        recyclerView.setAdapter(inviteelistAdapter);
        InviteRef.child(Invitation_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayList.clear();
                for (DataSnapshot d: dataSnapshot.getChildren()){
                    if (d.getKey().equals("inviter_uid")){
                        continue;
                    }
                    Invitee invitee = new Invitee();
                    invitee.setStatus(d.getValue().toString());
                    invitee.setUID(d.getKey());
                    arrayList.add(invitee);
                }
                inviteelistAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
