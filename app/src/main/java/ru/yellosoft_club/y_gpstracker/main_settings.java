package ru.yellosoft_club.y_gpstracker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.security.Security;

public class main_settings extends AppCompatActivity

implements NavigationView.OnNavigationItemSelectedListener {


    GoogleMap googleMap;
    private TextView tv;
    private LocationManager locationManager;
    private LocationListener listener;

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
        TextView tvView = (TextView)navigationView.getHeaderView(0).findViewById(R.id.textView2);
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        tvView.setText(name);
        //Нужен ещё уникальный ключ

        
        tv = (TextView) findViewById(R.id.textView);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        createMapView();
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
       // locationManager.requestLocationUpdates("gps", 5000, 0, listener);
        //configure_button();
    }

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

    private void createMapView(){

            if(null == googleMap){
                ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        main_settings.this.googleMap = googleMap;
                        if(null == googleMap) {
                            Toast.makeText(getApplicationContext(),
                                    "Error creating map",Toast.LENGTH_SHORT).show();
                        }
                        try {
                            LatLng sydney = new LatLng(-33.867, 151.206);
                            googleMap.addMarker(new MarkerOptions().position(sydney).title("Я здесь" + sydney));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker").snippet("Y_GPS Tracker"));
                            googleMap.setMyLocationEnabled(true);
                        }
                        catch (SecurityException e){Log.e ("" ,"No maps - no problems", e);}
                    }
                });
            }

    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
              //  configure_button();
                break;
            default:
                break;
        }
    }
    //Немозможно инициализировать из манифеста uses-permission.
   //void configure_button() {

      //if (ActivityCompat.checkSelfPermission(this, Manifest.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
       //    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          //      requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 10);
          //  }
          //  return;
       // }
  //  }



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

}
