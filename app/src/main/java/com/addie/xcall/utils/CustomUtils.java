package com.addie.xcall.utils;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.addie.xcall.ui.WaitingCallActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public final class CustomUtils {



    private static final String CALL_REQUEST_KEY = "call_request";

    private static String mFirebaseIDToken;

    public static void sendCallRequest(Context context, final String callerId, DatabaseReference mDBRef, final String mFirebaseIDToken) {

//      update call status in Db
        mDBRef.child("users").child(callerId).child(CALL_REQUEST_KEY).setValue("true");

        //old url
//        String url = "https://us-central1-talktime-5f9a9.cloudfunctions.net/sendPush";

        //new url
        String url = "https://us-central1-xcall-c532d.cloudfunctions.net/sendPush";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.d(error.toString());
            }

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
//                headers.put("Authorization", mFirebaseIDToken);
                headers.put("user_id",callerId);
                return headers;
            }
        };
        Timber.d(stringRequest.toString());
        Volley.newRequestQueue(context).add(stringRequest);

        context.startActivity(new Intent(context, WaitingCallActivity.class)/*.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)*/);
    }

    /**
     * Needed for authentication of user while using cloud functions
     */
    public static String getFirebaseIDToken(FirebaseAuth mAuth) {
        mAuth.getCurrentUser().getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            mFirebaseIDToken = task.getResult().getToken();

                        } else {
                            // Handle error -> task.getException();
                            task.getException().printStackTrace();
                        }
                    }
                });
        return mFirebaseIDToken;
    }


}
