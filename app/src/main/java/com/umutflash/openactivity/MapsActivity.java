package com.umutflash.openactivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.umutflash.openactivity.adapter.FavorietsAdapter;
import com.umutflash.openactivity.data.model.Spot;
import com.umutflash.openactivity.data.model.SpotEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    @BindView(R.id.add_spot)
    Button addSpotBtn;
    private GoogleMap mMap;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLocation;
    private LocationRequest mLocationRequest;

    private DatabaseReference mDataRef;
    private List<SpotEntry> favoriets;
    private LatLng pickupLocation;
    private Map<Marker, Map<String, Object>> markers;
    private Map<String, Object> dataModel;


    public static final String ARG_SPOT_ID = "spotId";
    public static final String ARG_TITLE = "title";
    public static final String ARG_CATEGORY = "category";
    public static final String ARG_DESCRIPTION = "description";
    public static final String ARG_IMAGE_URL = "imageUrl";
    public static final String ARG_LATITUDE = "latitude";
    public static final String ARG_LONITUDE = "longitude";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        ButterKnife.bind(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        markers = new HashMap<>();
        dataModel = new HashMap<>();

        addSpotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, AddSpotActivity.class);
                intent.putExtra("latitude", mLocation.getLatitude());
                intent.putExtra("longitude", mLocation.getLongitude());
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        } else {
            checkLocationPermission();
        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);

        fetchSpots();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Map dataModel = (Map) markers.get(marker);
                if (dataModel != null) {
                    String spotId = (String) dataModel.get(ARG_SPOT_ID);
                    String title = (String) dataModel.get(ARG_TITLE);
                    String category = (String) dataModel.get(ARG_CATEGORY);
                    String description = (String) dataModel.get(ARG_DESCRIPTION);
                    String imageURL = (String) dataModel.get(ARG_IMAGE_URL);
                    double latitude = (double) dataModel.get(ARG_LATITUDE);
                    double longitude = (double) dataModel.get(ARG_LONITUDE);

                    Spot spot = new Spot(spotId, "", title, category, description, imageURL, latitude, longitude);
                    Intent intent = new Intent(MapsActivity.this, DetailViewActivity.class);
                    intent.putExtra(FavorietsAdapter.ARG_Spot, spot);
                    startActivity(intent);
                    finish();
                }

                return false;
            }

        });

    }

    private void fetchSpots() {
        favoriets = new ArrayList<>();
        mDataRef = FirebaseDatabase.getInstance().getReference("spots");

        mDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    SpotEntry spot = postSnapshot.getValue(SpotEntry.class);
                    dataModel.put(ARG_SPOT_ID, spot.getSpotId());
                    dataModel.put(ARG_DESCRIPTION, spot.getDescription());
                    dataModel.put(ARG_TITLE, spot.getTitle());
                    dataModel.put(ARG_CATEGORY, spot.getCategory());
                    dataModel.put(ARG_IMAGE_URL, spot.getImageUrl());
                    dataModel.put(ARG_LATITUDE, spot.getLatitude());
                    dataModel.put(ARG_LONITUDE, spot.getLongitude());
                    pickupLocation = new LatLng(spot.getLatitude(), spot.getLongitude());
                    Marker marker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title(spot.getTitle()));
                    markers.put(marker, dataModel);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    mLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                }
            }
        }
    };
}
