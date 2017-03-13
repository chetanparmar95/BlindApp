package com.example.appforblind;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Calendar;

public class LocationUploadService extends Service {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessageDatabaseReference;
    GPSCurrentLocation currentLocation;
    boolean canGetLocation = false;
    public LocationUploadService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseInstanceId.getInstance().getToken();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessageDatabaseReference = mFirebaseDatabase.getReference().child("Location");
        currentLocation = new GPSCurrentLocation(getApplicationContext());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        while(true){
            try {

                if(canGetLocation){

                    Calendar c = Calendar.getInstance();
                    mMessageDatabaseReference.push().setValue(
                            new Location(
                            currentLocation.getLatitude() ,
                            currentLocation.getLongitude() ,
                            c.getTime())
                    );

                }
                Thread.sleep(1000 *  60  *  30);
            }
            catch(Exception e){
                Log.e("Location Upload Service",e.getMessage());
                break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
