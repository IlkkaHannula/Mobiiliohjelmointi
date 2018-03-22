package com.hannula.ilkka.harjoitus910; //AIzaSyD5kQxFiOFRTjfb3W2L2MaJqkTMmn1TwhU

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLocation;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 98;

    private TextView helloTextView;
    private Button haePaikkaButton;
    private Context context = this;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helloTextView=findViewById(R.id.textView);
        haePaikkaButton=findViewById(R.id.button);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("lokapaikka", ("paikka on muuttunut"+location.getLatitude()+", "+location.getLongitude()));
                mLocation=location;

                if (mMap != null){
                    moveMarker(mMap, location.getLatitude(), location.getLongitude());
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };


        haePaikkaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //tarkistetaan lupa
                try {
                    kysyLupaa(context);
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                    if(mLocation!=null) {
                        helloTextView.setText(mLocation.getLatitude() + ", " + mLocation.getLongitude());
                    }else{
                        helloTextView.setText("Paikka ei vielä saatavilla... Kokeile uudestaan");
                    }
                }catch (SecurityException e){
                    Log.d("lokasofta", "Virhe: Sovelluksella ei ollut oikeuksia lokaatioon");
                }
            }
        });


    }

    public boolean kysyLupaa(final Context context){
        Log.d("lokasofta", "kysyLupaa()");
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("lokasofta", " Permission is not granted");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Log.d("lokasofta", "Kerran kysytty, mutta ei lupaa... Nyt ei kysytä uudestaan");

            } else {
                Log.d("lokasofta", " Request the permission");
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.

            }
            return false;
        } else {

            Log.d("lokasofta", "Permission has already been granted");
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("lokasofta ", "onRequestPermissionsResult()");

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("lokasofta", "lupa tuli!");
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.d("lokasofta", "Haetaan paikkaa tietyin väliajoin");
                        //Request location updates:
                        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,mLocationListener);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("lokasofta", "Ei tullu lupaa!");
                }
                return;
            }

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng pori = new LatLng(61.6, 21.25);
        mMap.addMarker(new MarkerOptions().position(pori).title("Surffaamaan!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pori, 8.0f));

    }

    private void moveMarker(GoogleMap mMap, double lat, double lng){
        LatLng location = new LatLng(lat, lng);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title("Uusi merkki"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 8.0f));
    }
}
