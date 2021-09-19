package com.example.findmysenpai2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Base64;

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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;
import java.util.Set;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_INTERVAL = 5000;

    private GoogleMap googleMap;
    private Marker userMarker;  // our marker on the map

    private Set<Marker> otherMarkers;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.map);

        this.otherMarkers = new HashSet<>();

        SupportMapFragment mapFragment = (SupportMapFragment)this.getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.forceLocationPermissions();
        this.startUserLocationTask();
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
                if (MapActivity.this.userMarker == null) {
                    // First time retrieving map data
                    Location location = locationResult.getLastLocation();
                    LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

                    MapActivity.this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
                    MapActivity.this.userMarker = googleMap.addMarker(new MarkerOptions().position(position).title("Me"));
                    MapActivity.this.loadAvatar();
                }


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

                MapActivity.this.updateNetworkLocation(position);
            }
        }, Looper.getMainLooper());
    }

    private void updateNetworkLocation(LatLng position) {
        String deviceId = Settings.Secure.getString(this.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("device", deviceId)
                .get().addOnSuccessListener(task -> {
                    task.forEach(document -> {
                        DocumentReference reference = db.collection("users").document(document.getId());
                        reference.update("longitute", position.longitude, "latitude", position.latitude);

                        MapActivity.this.updateOtherMarkers(document.getString("roomCode"));
                    });
                });
    }

    private void updateOtherMarkers(String roomCode) {
        String deviceId = Settings.Secure.getString(this.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereNotEqualTo("device", deviceId)
                .whereEqualTo("roomCode", roomCode)
                .get().addOnSuccessListener(task -> {
                    for (Marker marker : this.otherMarkers) {
                        marker.remove();
                    }
                    this.otherMarkers.clear();

                    task.forEach(document -> {
                        String base64Avatar = document.getString("base64Image");
                        BitmapDescriptor avatar = this.getAvatar(base64Avatar);
                        MarkerOptions options = new MarkerOptions()
                                .title(document.getString("name"))
                                .position(new LatLng(document.getDouble("latitude"), document.getDouble("longitude")));
                        if (avatar != null) {
                            options.icon(avatar);
                        }

                        Marker marker = this.googleMap.addMarker(options);
                        this.otherMarkers.add(marker);
                    });
                });
    }

    private void loadAvatar() {
        String deviceId = Settings.Secure.getString(this.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("device", deviceId)
                .get().addOnSuccessListener(task -> {
            if (task.getDocuments().size() > 0) {
                DocumentSnapshot documentSnapshot = task.getDocuments().get(0);
                String base64Avatar = documentSnapshot.getString("base64Image");
                BitmapDescriptor descriptor = this.getAvatar(base64Avatar);
                if (descriptor != null) {
                    this.userMarker.setIcon(descriptor);
                }
            }
        });
    }

    private BitmapDescriptor getAvatar(String base64) {
        if (base64.length() > 0) {
            byte[] decoded = Base64.decode(base64, 0);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = false;
            Bitmap image = BitmapFactory.decodeByteArray(decoded, 0, decoded.length, options);
            image = Bitmap.createScaledBitmap(image, 128, 128, true);
            return BitmapDescriptorFactory.fromBitmap(image);
        } else {
            return null;
        }
    }

}
