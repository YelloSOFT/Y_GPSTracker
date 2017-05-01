package ru.yellosoft_club.y_gpstracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ru.yellosoft_club.y_gpstracker.R.id.TFaddress;

public class main_settings extends AppCompatActivity

implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    GoogleMap googleMap;
    private TextView tv;
    private LocationManager locationManager;
    private LocationListener listener;
    private GoogleApiClient googleClient;
    private EditText TF;
    private DatabaseReference mDatabase;
    private DatabaseReference userReference;
    private DatabaseReference userLocationReference;
    private ChildEventListener userLocationListener;
    private Polyline route;
    private List<Pair<String, UserLocation>> locations = new ArrayList<Pair<String, UserLocation>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Email в боковой части (навигации)
        TextView tvView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textView2);
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        tvView.setText(name);
        //Uid в боковой части (навигации) (не полностью)
        TextView tvView2 = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textView);
        String uid = null;
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //
        tv = (TextView) findViewById(R.id.textView);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                tv.append("\n " + location.getLongitude() + " " + location.getLatitude());
            }

            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }

        };
        //Вызовы "функций"//
        createMapView();
        configure_button();
        //Вызовы "функций"//

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userReference = mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userLocationReference = userReference.child("locations");

        googleClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    // Запись в бд
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();

        userLocationListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                locations.add(new Pair<String, UserLocation>(dataSnapshot.getKey(), dataSnapshot.getValue(UserLocation.class)));

                if (locations.size() > 1000) {
                    Pair<String, UserLocation> pair = locations.remove(0);
                    userLocationReference.child(pair.first).removeValue();
                }

                if (route != null) {
                    route.remove();
                }
                PolylineOptions options = new PolylineOptions();
                for (Pair<String, UserLocation> location : locations) {
                    options.add(new LatLng(location.second.getLatitude(), location.second.getLongitude()));
                }
                options.color(Color.BLACK);
                route = googleMap.addPolyline(options);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("Database", "Removed child: " + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userLocationReference.addChildEventListener(userLocationListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdate();
        googleClient.disconnect();
        userLocationReference.removeEventListener(userLocationListener);
    }

    public static class UserLocation {

        private double latitude;
        private double longitude;
        private String date;

        public UserLocation() {

        }

        public UserLocation(double latitude, double longitude, String date) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.date = date;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    private void writeNewUser(String userId, String latitude, String longitude, String date) {
        UserLocation location = new UserLocation(Double.valueOf(latitude), Double.valueOf(longitude), String.valueOf(date));

        userLocationReference.push().setValue(location);
    }

    public void onSearch(View view) {
        EditText location_tf = (EditText) findViewById(R.id.TFaddress);
        String location = location_tf.getText().toString();
        List<Address> addressList = null;
        TF = (EditText) findViewById(TFaddress);
        if (TextUtils.isEmpty(TF.getText())) {
            TF.setError(("Введите Улицу или Город"));
            return;
        }

        if (location == null) {
        } else {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (addressList == null || addressList.size() == 0) {
            Toast.makeText(this, "Улица или Город - не найдены \uD83D\uDE32", Toast.LENGTH_LONG).show();
            return;
        }

        Address address = addressList.get(0);
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

        Intent intent = new Intent(this, main_settings.class);
        intent.putExtra("gorod_key", TF.getText().toString());
        String gorod = intent.getStringExtra("gorod_key");
        TF.setText(gorod);

        googleMap.addMarker(new MarkerOptions().position(latLng).title("Здесь - " + gorod));
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

    }

    public void changeType(View view) {
        if (googleMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void createMapView() {

        if (null == googleMap) {
            ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.mapView)).getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    main_settings.this.googleMap = googleMap;
                    if (null == googleMap) {
                        Toast.makeText(getApplicationContext(),
                                "Error creating map", Toast.LENGTH_SHORT).show();
                    }
                    try {

                        //LatLng sydney = new LatLng(-33.867, 151.206);
                        //googleMap.addMarker(new MarkerOptions().position(sydney).title("Я здесь" + sydney));
                        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                        //googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker").snippet("Y_GPS Tracker"));


                        googleMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        Log.e("", "No maps - no problems", e);
                    }
                }
            });
        }
    }

    void configure_button() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    if (!provider.contains("gps")) {
                        //GPS AФК to...
                        final Intent poke = new Intent();
                        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                        poke.setData(Uri.parse("3"));
                        sendBroadcast(poke);
                        //***************Включение настроек*********************//
                        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        int LOCATION_SETTINGS_REQUEST = 0;
                        startActivityForResult(i, LOCATION_SETTINGS_REQUEST);
                        //***************Включение настроек*********************//
                    }
                } else {
                    Toast.makeText(this, "Включите GPS! \uD83C\uDF0D", Toast.LENGTH_SHORT).show();
                }
            }
            return;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_settings, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.Share) {
            Intent share_intent = new Intent((Intent.ACTION_SEND));
            share_intent.setType("text/plain");
            String st2 = "Y_GPSTracker\nБесплатный GPS Трекер\nhttp://yellosoft-club.ru";
            share_intent.putExtra(Intent.EXTRA_SUBJECT, st2);
            share_intent.putExtra(Intent.EXTRA_TEXT, st2);
            share_intent.putExtra(Intent.EXTRA_EMAIL, st2);
            startActivity(Intent.createChooser(share_intent, "Поделиться ☺"));
        } else if (id == R.id.Donate) {
            //
        } else if (id == R.id.email_dev) {
            Intent intent = new Intent(main_settings.this, email_developer.class);
            startActivity(intent);
        } else if (id == R.id.friend_search) {
            Intent intent = new Intent(main_settings.this, friend_search.class);
            startActivity(intent);
        } else if (id == R.id.friend_search) {
            Intent intent = new Intent(main_settings.this, save_friends.class);
            startActivity(intent);
        } else if (id == R.id.About) {
            Toast.makeText(main_settings.this, "\uD83D\uDE3C", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(main_settings.this, about.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        stopLocationUpdate();

    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    @Override
    public void onLocationChanged(Location location) {

        String mask = "dd.MM.yyyy 'Time' HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mask);
        String date = simpleDateFormat.format(new Date());

        if (location != null) {
            Log.d("Location", "Recieved location: " + location.getLatitude() + " " + location.getLongitude() + "" + String.valueOf(date));
            writeNewUser("", String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), String.valueOf(date));
        } else {
            Log.d("Location", "Invalid my code");
        }
    }
}
