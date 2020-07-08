package com.example.a611;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class UserListAdapter extends FirebaseRecyclerAdapter<User,UserListAdapter.viewHolder> {
    private Context mCtx;

    public UserListAdapter(Context mCtx ,FirebaseRecyclerOptions<User> options) {
        super(options);
        this.mCtx = mCtx;

    }



    @Override
    protected void onBindViewHolder(@NonNull final viewHolder holder, int position, @NonNull User model) {
        holder.email.setText(model.getEmail());
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
        TextView email; RelativeLayout layout; ImageView check;
        public viewHolder(View view){
            super(view);
            email = view.findViewById(R.id.usr_email);
            check = view.findViewById(R.id.user_checked);
            layout = view.findViewById(R.id.user_card);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mCtx,TrackActivity.class);
                    i.putExtra("email",email.getText());

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
