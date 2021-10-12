package com.example.taproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapFragment extends Fragment {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        databaseReference = database.getReference("Device_50:02:91:C9:DF:C4");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot datagps = dataSnapshot.child("raw_data");

                Double gpsLat = datagps.child("GPS_Lat").getValue(Double.class);
                Double gpsLong = datagps.child("GPS_Long").getValue(Double.class);

                //initialize maps
                if (!isAdded()) return;
                SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_maps);

                //Async map
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        googleMap.clear();
                        LatLng pengguna = new LatLng(gpsLat, gpsLong);
                        googleMap.addMarker(new MarkerOptions().position(pengguna).title("Posisi Terkini Pengguna"));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pengguna,18));
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //initialize view
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        //return view
        return view;
    }
}