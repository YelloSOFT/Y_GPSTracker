package ru.yellosoft_club.y_gpstracker;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationTrackingService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient googleClient;
    private boolean waitingForLocation;

    private DatabaseReference mDatabase;
    private DatabaseReference userReference;
    private DatabaseReference userLocationReference;

    @Override
    public void onCreate() {
        super.onCreate();
        googleClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleClient.connect();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    userReference = mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    userLocationReference = userReference.child("locations");
                } else {
                    userReference = null;
                    userLocationReference = null;
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!waitingForLocation) {
            waitingForLocation = true;
            if (googleClient.isConnected()) {
                startLocationUpdate();
            }
        }
        return Service.START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (waitingForLocation) {
            startLocationUpdate();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        stopLocationUpdate();
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            waitingForLocation = false;
            stopSelf();
            return;
        }

        Log.d("Location", "Starting location update");
        LocationRequest request = LocationRequest.create();
        request.setInterval(1000);
        request.setSmallestDisplacement(1);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleClient, request, (LocationListener) this);
    }

    private void stopLocationUpdate() {
        Log.d("Location", "Stopping location update");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleClient, (LocationListener) this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected static final int MINUTE_MS = 1000 * 60;
    protected static final float SUFFICIENT_ACCURACY_M = 25.0f;
    protected static final int SUFFICIENT_RECENCY_MS = MINUTE_MS * 3;

    protected boolean isLocationGoodEnough(Location location) {
        if (location == null)
            return false;

        long recency = new Date().getTime() - location.getTime();
        float accuracy = location.getAccuracy();

        if (recency < SUFFICIENT_RECENCY_MS && accuracy < SUFFICIENT_ACCURACY_M) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        String mask = "dd.MM.yyyy 'Time' HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mask);
        String date = simpleDateFormat.format(new Date());

        if (location != null) {
            Log.d("Location", "Recieved location: " + location.getLatitude() + " " + location.getLongitude() + "" + String.valueOf(date));
            if (isLocationGoodEnough(location)) {
                writeNewUser("", String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), String.valueOf(date));
            } else {
                Log.d("Location", "Location is not good enough");
            }
        } else {
            Log.d("Location", "Invalid my code");
        }
    }

    private void writeNewUser(String userId, String latitude, String longitude, String date) {
        if (userLocationReference != null) {
            UserLocation location = new UserLocation(Double.valueOf(latitude), Double.valueOf(longitude), String.valueOf(date));
            userLocationReference.push().setValue(location);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        googleClient.disconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
