package com.example.a611.recycler;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a611.R;
import com.example.a611.TrackActivity;
import com.example.a611.UserListActivity;
import com.example.a611.classes.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;

public class UserListAdapter extends FirebaseRecyclerAdapter<User,UserListAdapter.viewHolder> {
    private Context mCtx;

    public UserListAdapter(Context mCtx ,FirebaseRecyclerOptions<User> options) {
        super(options);
        this.mCtx = mCtx;

    }



    @Override
    protected void onBindViewHolder(@NonNull final viewHolder holder, int position, @NonNull User model) {
        if(FirebaseAuth.getInstance().getCurrentUser()!=null&&FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(model.getEmail())){
            holder.user_Card.setVisibility(View.GONE);
            holder.layout.setVisibility(View.GONE);
        }else{
            holder.email.setText(model.getEmail());
            holder.uid.setText(model.getUID());
        }

        if (!UserListActivity.enableActionBar){
            holder.check.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);

        return new viewHolder(view);
    }

    class viewHolder extends RecyclerView.ViewHolder{
        TextView email,uid; RelativeLayout layout; ImageView check;
        CardView user_Card;
        public viewHolder(View view){
            super(view);
            email = view.findViewById(R.id.usr_email);
            uid = view.findViewById(R.id.usr_UID);
            check = view.findViewById(R.id.user_checked);
            layout = view.findViewById(R.id.user_card);
            user_Card = view.findViewById(R.id.user_Card);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (check.getVisibility()==View.VISIBLE){
                        check.setVisibility(View.GONE);
                    }else{
                        check.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

}
