package com.example.a611.SendNotificationPack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ActionReceiver extends BroadcastReceiver {
    DatabaseReference inviteRef;String IID;
    @Override
    public void onReceive(Context context, Intent intent) {



        boolean action=intent.getBooleanExtra("action",true);
        IID = intent.getStringExtra("IID");

        inviteRef =  FirebaseDatabase.getInstance().getReference("Invitations/"+IID).getRef() ;
        if(action){
            performAction1();
        } else{
            performAction2();
        }
        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }

    public void performAction1(){
        Log.d("TUSK",""+IID+ true);
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            inviteRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
        }

    }

    public void performAction2(){
        Log.d("TUSK",""+IID +false);
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            inviteRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(false);
        }
    }
}
