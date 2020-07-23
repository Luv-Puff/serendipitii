package com.example.a611.recycler;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a611.R;
import com.example.a611.classes.Invitee;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.List;

public class inviteelistAdapter extends RecyclerView.Adapter<inviteelistAdapter.PostViewHolder> {
    private Context mCtx;
    private List<Invitee> postList;

    public inviteelistAdapter(Context mCtx, List<Invitee> postList) {
        this.mCtx = mCtx;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostViewHolder(
                LayoutInflater.from(mCtx).inflate(R.layout.inivitee_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final inviteelistAdapter.PostViewHolder holder, int position) {
        Invitee post = postList.get(position);
        if (post.isStatus().equals("true")){
            holder.Invitee_status.setText("Accepted");
        }else if(post.isStatus().equals("")){
            holder.Invitee_status.setText("Not Yet Replied");
        }
        else {
            holder.Invitee_status.setText("Denied");
        }
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
        databaseReference.child(post.getUID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.Invitee_email.setText(dataSnapshot.child("email").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
    class PostViewHolder extends RecyclerView.ViewHolder  {

        TextView Invitee_email,Invitee_status;

        public PostViewHolder(View itemView)  {
            super(itemView);
            Invitee_email = itemView.findViewById(R.id.invitee_email);
            Invitee_status = itemView.findViewById(R.id.invited_status);
        }

    }
}

