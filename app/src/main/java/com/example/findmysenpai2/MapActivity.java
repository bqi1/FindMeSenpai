package com.example.findmysenpai2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_INTERVAL = 5000;

    private GoogleMap googleMap;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.map);

        SupportMapFragment mapFragment = (SupportMapFragment)this.getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.forceLocationPermissions();

        // Starting location on the map is wherever we are.
        FusedLocationProviderClient provider = LocationServices.getFusedLocationProviderClient(this);
        provider.getLastLocation().addOnCompleteListener(locationTask -> {
            Location location = locationTask.getResult();
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        });

        // Listen for whenever our location changes
        this.listenForLocationUpdates();
    }

    private void forceLocationPermissions() {
        // Force them to accept location permissions
        boolean hasLocationPermissions = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        while (!hasLocationPermissions) {
            this.requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 99);
        }
    }

    private void listenForLocationUpdates() {
        FusedLocationProviderClient provider = LocationServices.getFusedLocationProviderClient(this);
        provider.requestLocationUpdates(LocationRequest.create().setInterval(REQUEST_LOCATION_INTERVAL), new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                // TODO: update user marker location
            }
        }, Looper.getMainLooper());
    }

}
