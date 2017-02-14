package com.yadav.maps4;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);

    }

    @Override
    protected void onStart(){
        super.onStart();
        if(checkNetwork()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(MainActivity.this,MapsActivity.class);
                    startActivity(i);
                    finish();
                }
            },2000);
        }
        else {
               //No network connection  available
            Toast.makeText(MainActivity.this, "Opps!!! Enable data service...", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    boolean checkNetwork() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo != null) {

            if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

}
