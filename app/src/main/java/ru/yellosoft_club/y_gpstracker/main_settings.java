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
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static ru.yellosoft_club.y_gpstracker.R.id.TFaddress;

public class main_settings extends AppCompatActivity

implements NavigationView.OnNavigationItemSelectedListener {

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    GoogleMap googleMap;
    private TextView tv;
    private LocationManager locationManager;
    private LocationListener listener;
    private EditText TF;
    private DatabaseReference mDatabase;
    private DatabaseReference userReference;
    private DatabaseReference userLocationReference;
    private ChildEventListener userLocationListener;
    private List<Polyline> route = new ArrayList<>();
    private List<Pair<String, UserLocation>> locations = new ArrayList<Pair<String, UserLocation>>();
    private List<TrackedUserFriend> trackedFriends = new ArrayList<>();

    private boolean firstFix = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent i = new Intent(this, authorization.class);
            startActivity(i);
            finish();
            return;
        }

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
        String email = "Email: ";
        tvView.setText(email + user.getEmail());
        //Uid в боковой части (навигации)
        TextView tvView2 = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textView);
        String udid = null;
        String udid2 = "Udid: \n";
        udid = user.getUid();
        tvView2.setText(udid2 + udid);
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
    }
    //Запись в бд
    @Override
    protected void onStart() {
        super.onStart();

        final String mask = "dd.MM.yyyy 'Time' HH:mm:ss";
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mask);

        userLocationListener = new ChildEventListener() {

            private boolean isTodayLocation(UserLocation location) {
                try {
                    Calendar now = GregorianCalendar.getInstance();
                    Date date = simpleDateFormat.parse(location.getDate());
                    Calendar calendar = GregorianCalendar.getInstance();
                    calendar.setTime(date);
                    if (calendar.get(Calendar.YEAR) < now.get(Calendar.YEAR) || calendar.get(Calendar.MONTH) < now.get(Calendar.MONTH) || calendar.get(Calendar.DAY_OF_MONTH) < now.get(Calendar.DAY_OF_MONTH)) {
                        return false;
                    } else {
                        return true;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                UserLocation location = dataSnapshot.getValue(UserLocation.class);
                if (!isTodayLocation(location)) {
                    return;
                }

                locations.add(new Pair<String, UserLocation>(dataSnapshot.getKey(), location));

                if (locations.size() > 1000) {
                    Pair<String, UserLocation> pair = locations.remove(0);
                    userLocationReference.child(pair.first).removeValue();
                }

                schedulePolylineRedraw();

                if (locations.size() > 0 && !firstFix) {
                    firstFix = true;
                    LatLng firstLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 14));
                }
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


        int[] color = new int[] {
                0xffff0000, // RED
                0xff00ff00, // GREEN
                0xff0000ff,  // BLUE
                0xff8b00ff, //Фиолетовый (цвет добавляется после 0хff)
                0xffffa500,   0xff964b00,
                0xff5b646e,   0xffb097a7

        };
        int colorIndex = 0;

        for (UserFriend friend : SelectedFriends.getInstance().getSelectedFriends()) {
            TrackedUserFriend trackedFriend = new TrackedUserFriend(friend, color[colorIndex]);
            trackedFriend.startTracking();
            trackedFriends.add(trackedFriend);

            colorIndex++;
            if (colorIndex == color.length) {
                colorIndex = 0;
            }
        }
    }

    private void schedulePolylineRedraw() {
        mainThreadHandler.removeCallbacks(redrawPolyline);
        mainThreadHandler.postDelayed(redrawPolyline, 100);
    }

    private Runnable redrawPolyline = new Runnable() {
        @Override
        public void run() {
            List<UserLocation> userLocations = new ArrayList<>();
            for (Pair<String, UserLocation> location : locations) {
                userLocations.add(location.second);
            }

            drawPolyline(route, Color.BLACK, userLocations);
        }
    };

    private void clearPolyline(List<Polyline> polyline) {
        for (Polyline line : polyline) {
            line.remove();
        }
        polyline.clear();
    }

    private static final int MAX_GAP = 500;

    private void drawPolyline(List<Polyline> polyline, int color, List<UserLocation> locations) {
        clearPolyline(polyline);

        UserLocation previousLocation = null;
        PolylineOptions options = null;
        for (UserLocation location : locations) {
            if (previousLocation == null  || Geo.distance(location.getLatitude(), location.getLongitude(), previousLocation.getLatitude(), previousLocation.getLongitude()) > MAX_GAP) {
                if (options != null) {
                    Polyline line = googleMap.addPolyline(options);
                    polyline.add(line);
                }

                options = new PolylineOptions();
                options.color(color);
            }
            options.add(new LatLng(location.getLatitude(), location.getLongitude()));
            previousLocation = location;
        }

        if (options != null) {
            Polyline line = googleMap.addPolyline(options);
            polyline.add(line);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        userLocationReference.removeEventListener(userLocationListener);

        locations.clear();

        for (TrackedUserFriend friend : trackedFriends) {
            friend.stopTracking();
        }
        trackedFriends.clear();
    }

    public class TrackedUserFriend {

        private UserFriend friend;

        private DatabaseReference friendReference;
        private DatabaseReference friendLocationReference;
        private ChildEventListener friendLocationListener;

        private int color;
        private List<Polyline> friendRoute = new ArrayList<>();

        private List<UserLocation> friendLocations = new ArrayList<UserLocation>();

        public TrackedUserFriend(UserFriend friend, int color) {
            this.friend = friend;
            friendReference = mDatabase.child("users").child(friend.getUdid());
            friendLocationReference = friendReference.child("locations");
            this.color = color;
        }

        public void startTracking() {
            friendLocationListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    friendLocations.add(dataSnapshot.getValue(UserLocation.class));
                    scheduleFriendPolylineRedraw();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            friendLocationReference.addChildEventListener(friendLocationListener);
        }

        private void stopTracking() {
            friendLocationReference.removeEventListener(friendLocationListener);
        }

        private void scheduleFriendPolylineRedraw() {
            mainThreadHandler.removeCallbacks(redrawFriendPolyline);
            mainThreadHandler.postDelayed(redrawFriendPolyline, 100);
        }

        private Runnable redrawFriendPolyline = new Runnable() {
            @Override
            public void run() {
                drawPolyline(friendRoute, color, friendLocations);
            }
        };

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
                    attempToDisplayMyLocation();
                }
            });
        }
    }

    private void attempToDisplayMyLocation() {
        try {
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Log.e("", "No maps - no problems", e);
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
                    } else {
                        attempToDisplayMyLocation();
                        Intent i = new Intent(MyApplication.getInstance(), LocationTrackingService.class);
                        MyApplication.getInstance().startService(i);
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
        } else if (id == R.id.save_friends) {
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


}
