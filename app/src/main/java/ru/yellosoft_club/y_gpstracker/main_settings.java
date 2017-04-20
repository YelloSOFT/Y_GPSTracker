package ru.yellosoft_club.y_gpstracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.IOException;
import java.util.List;

import static ru.yellosoft_club.y_gpstracker.R.id.TFaddress;

public class main_settings extends AppCompatActivity

implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {


    GoogleMap googleMap;
    private TextView tv;
    private LocationManager locationManager;
    private LocationListener listener;
    private GoogleApiClient googleClient;
    private EditText TF;
    private DatabaseReference mDatabase;

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
        String uid;
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

        googleClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    //    Запись в бд
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdate();
        googleClient.disconnect();
    }
    public static class UserLocation {

        private double latitude;
        private double longitude;

        public UserLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
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
    }

    private void writeNewUser(String userId, String latitude, String longitude) {
        UserLocation location = new UserLocation(Double.valueOf(latitude), Double.valueOf(longitude));

        DatabaseReference userReference = mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference userLocationReference = userReference.child("location");
        userLocationReference.setValue(location);

    }
//    Запись в бд
    //Типа проверка на Google сервисы
    //private boolean isGooglePlayServicesAvailable() {
    //  int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    //if (ConnectionResult.SUCCESS == status) {
    //    return true;
    // } else {
    //    GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
    //      return false;
    //  }
    // }

    public void onSearch(View view) {
        EditText location_tf = (EditText) findViewById(R.id.TFaddress);
        String location = location_tf.getText().toString();
        List<Address> addressList = null;
        //Цвет фона чёрный + если ненаходи город - крах
        TF = (EditText) findViewById(TFaddress);
        if (TextUtils.isEmpty(TF.getText())) {
            TF.setError(("Введите Улицу или Город"));
            return;
        }

        if (location == null)
        {
        } else
        {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Address address = addressList.get(0);
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

        Intent intent = new Intent(this,main_settings.class);
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
            share_intent.putExtra(Intent.EXTRA_SUBJECT,st2);
            share_intent.putExtra(Intent.EXTRA_TEXT,st2);
            share_intent.putExtra(Intent.EXTRA_EMAIL,st2);
            startActivity(Intent.createChooser(share_intent, "Поделиться ☺"));

        } else if (id == R.id.Donate) {
             //
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
        request.setSmallestDisplacement(10);
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

        if(location!=null)
        {
            Log.d("Location", "Recieved location: " + location.getLatitude() + " " + location.getLongitude());
            writeNewUser("",String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }
            else
            {
                Log.d("Location", "Invalid my code");
            }
        }
}




