package com.ufc.com.googlemapslocalizacaoeplayservices.views;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ufc.com.googlemapslocalizacaoeplayservices.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("location");
        double lat = bundle.getDouble("Lat");
        double lont = bundle.getDouble("Long");

        LatLng myPosition = new LatLng(lat, lont);
        mMap.addMarker(new MarkerOptions().position(myPosition).title("Marker in Myposition"));
        float zoomLevel = 16.0f; //zoom que a camera vai dá
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, zoomLevel));
    }
}
