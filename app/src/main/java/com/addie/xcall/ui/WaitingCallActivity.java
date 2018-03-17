package com.addie.xcall.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.addie.xcall.R;
import com.addie.xcall.receivers.NetworkChangeReceiver;
import com.addie.xcall.utils.NoResponseHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class WaitingCallActivity extends AppCompatActivity {

    private static final String CALL_REQUEST_KEY = "call_request";


    private NetworkChangeReceiver networkChangeReceiver;
    private BroadcastReceiver mDialogReceiver;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDBRef;
    private BroadcastReceiver finishReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_call);

        ButterKnife.bind(this);

        initialiseAuthAndDatabaseReference();


        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("ACTION_TRIGGER_RECEIVER");

        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, filter);


        finishReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("finish_waitingcallactivity")) {
                    finish();
                }
            }
        };
        registerReceiver(finishReceiver, new IntentFilter("finish_waitingcallactivity"));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 36000);

        NoResponseHandler.getHandler(this);
    }

    private void initialiseAuthAndDatabaseReference() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDBRef = mDatabase.getReference();

    }

    @Override
    protected void onResume() {
        super.onResume();
        createDialogReceiver();
    }


    private void createDialogReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_NO_INTERNET");

        mDialogReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("ACTION_NO_INTERNET")) {
                    Toast.makeText(context, "Internet connection lost.Please try again", Toast.LENGTH_LONG).show();
                    NoResponseHandler.stopHandler();
                    finish();
                }
            }
        };
        registerReceiver(mDialogReceiver, filter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mDialogReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
        NoResponseHandler.stopHandler();
        unregisterReceiver(finishReceiver);

    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));

    }


    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(WaitingCallActivity.this);
        builder.setTitle(R.string.no_internet)
                .setMessage("The call process will be terminated")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        String callerId = mAuth.getCurrentUser().getUid();
                        mDBRef.child("callers").child(callerId).child(CALL_REQUEST_KEY).setValue("false");
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }
}
