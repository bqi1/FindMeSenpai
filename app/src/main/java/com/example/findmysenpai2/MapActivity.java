package com.example.findmysenpai2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_INTERVAL = 5000;

    private GoogleMap googleMap;
    private Marker userMarker;  // our marker on the map


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

            this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
            this.userMarker = googleMap.addMarker(new MarkerOptions().position(position).title("Me"));

            // Listen for whenever our location changes and update the marker accordingly
            this.startUserLocationTask();
        });
    }

    private void forceLocationPermissions() {
        // Force them to accept location permissions
        boolean hasLocationPermissions = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        while (!hasLocationPermissions) {
            this.requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 99);
        }
    }

    private void startUserLocationTask() {
        FusedLocationProviderClient provider = LocationServices.getFusedLocationProviderClient(this);

        provider.requestLocationUpdates(LocationRequest.create().setInterval(REQUEST_LOCATION_INTERVAL), new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Marker currentUserMarker = MapActivity.this.userMarker;
                Location location = locationResult.getLastLocation();

                LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                LatLng lastPosition = currentUserMarker.getPosition();

                MapActivity.this.userMarker.setPosition(position);

                // We should readjust the camera back to our marker if the person is moving.
                boolean readjustCamera = Math.abs(position.latitude - lastPosition.latitude) > 0.01d || Math.abs(position.longitude - lastPosition.longitude) > 0.01d;
                if (readjustCamera) {
                    MapActivity.this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
                }
            }
        }, Looper.getMainLooper());
    }

}
