package com.umutflash.openactivity.ui.home;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.umutflash.openactivity.AddSpotActivity;
import com.umutflash.openactivity.DetailViewActivity;
import com.umutflash.openactivity.R;
import com.umutflash.openactivity.adapter.FavorietsAdapter;
import com.umutflash.openactivity.data.model.Spot;
import com.umutflash.openactivity.data.model.SpotEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    public static final int ZOOM_FACTOR = 11;
    public static final int FASTEST_INTERVAL = 1000;
    @BindView(R.id.fab)
    FloatingActionButton fabBtn;

    private GoogleMap mMap;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLocation;
    private LocationRequest mLocationRequest;

    private DatabaseReference mDataRef;
    private List<SpotEntry> favoriets;
    private LatLng pickupLocation;
    private Map<Marker, Map<String, Object>> markers;
    private Map<String, Object> dataModel;


    private static final String ARG_SPOT_ID = "spotId";
    private static final String ARG_TITLE = "title";
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_IMAGE_URL = "imageUrl";
    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONITUDE = "longitude";

    private Boolean startFlag = false;

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);

        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            checkLocationPermission();
        }

        ButterKnife.bind(this, root);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        markers = new HashMap<>();
        dataModel = new HashMap<>();

        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AddSpotActivity.class);
                intent.putExtra(ARG_LATITUDE, mLocation.getLatitude());
                intent.putExtra(ARG_LONITUDE, mLocation.getLongitude());
                startActivity(intent);
            }
        });
        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(FASTEST_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_FACTOR));

        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        
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
                    Intent intent = new Intent(getContext(), DetailViewActivity.class);
                    intent.putExtra(FavorietsAdapter.ARG_Spot, spot);
                    startActivity(intent);
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
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(getContext())
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(getContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getContext() != null) {
                    mLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (!startFlag) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        startFlag = true;
                    }
                }
            }
        }
    };
}
