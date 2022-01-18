package com.example.taproject;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotifActivity extends AppCompatActivity implements OnNotifClickListener{
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private NotifAdapter adapter;
    private List<NotifClass> dataList;
    private CardView noData;
    private AppCompatButton submit_hapus;
    private AppCompatButton cancel_hapus;
    private ImageView delNotif;
    Dialog hapusNotif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notif_activity);
        databaseReference = database.getReference("Device_50:02:91:C9:DF:C4");
        databaseReference.child("flag_status/status_notifikasi").setValue(0);
        //menutup notifikasi pada bar
        NotificationManager notificationManager =(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        noData = findViewById(R.id.card_nodata);

        recyclerView = (RecyclerView) findViewById(R.id.recyler_view_notif);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataList = new ArrayList<>();

        addData();

        delNotif = findViewById(R.id.hapus_icon);
        delNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hapusDataNotif();
            }
        });

        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dataList.clear();
                addData();
                pullToRefresh.setRefreshing(false);
            }
        });
    }

    void addData(){
        final DatabaseReference datahasil = databaseReference.child("notif_data");
        datahasil.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        NotifClass l = npsnapshot.getValue(NotifClass.class);
                        dataList.add(l);
                    }
                    Collections.reverse(dataList);
                    adapter = new NotifAdapter(dataList, NotifActivity.this);
                    recyclerView.setAdapter(adapter);
                    delNotif.setVisibility(View.VISIBLE);
                }else{
                    noData.setVisibility(View.VISIBLE);
                    delNotif.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //window dialog hapus data notifikasi
    public void hapusDataNotif(){
        hapusNotif = new Dialog(this);
        hapusNotif.setContentView(R.layout.popup_delnotif);
        hapusNotif.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        hapusNotif.show();

        submit_hapus = hapusNotif.findViewById(R.id.lanjutkan_hapus);
        submit_hapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            //deklarasi child data dengan snapshot
                            DataSnapshot dataNotif = snapshot.child("notif_data");
                            //proses hapus data notif pada firebase dengan perulangan for
                            for(DataSnapshot data : dataNotif.getChildren()){
                                data.getRef().removeValue();
                            }
                            hapusNotif.dismiss();
                            Toast.makeText(NotifActivity.this, "Data Notifikasi berhasil dihapus", Toast.LENGTH_SHORT).show();
                            dataList.clear();
                            addData();
                        }else{
                            Toast.makeText(NotifActivity.this, "Data Notifikasi tidak tersedia", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(NotifActivity.this, "Gagal menghapus Notifikasi", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        cancel_hapus = hapusNotif.findViewById(R.id.cancel);
        cancel_hapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hapusNotif.dismiss();
            }
        });
    }

    //akses google maps dari button
    public void mapsAkses(String urlMaps){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(urlMaps));
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(keyCode,event);
    }

    public void onNotifClick(String urlMaps){
        mapsAkses(urlMaps);
    }
}
