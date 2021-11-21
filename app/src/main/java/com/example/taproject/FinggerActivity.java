package com.example.taproject;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FinggerActivity extends AppCompatActivity implements OnImageClickListener {
    private RecyclerView recyclerView;
    private FinggerAdapter adapter;
    private ArrayList<FinggerClass> dataList;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;

    private CardView tambahFinger;
    private CardView noData;
    private TextView maxFinger;

    //variable dari popup
    private CardView finggerSucess;
    private CardView finggerDissmis;
    private CardView delFingerSuccess;
    private CardView delFingerDissmis;
    private AppCompatButton lanjutkan_popup;
    private AppCompatButton batalkan_popup;
    private AppCompatButton kembali_popup;
    private AppCompatButton batalkan_scan;
    private AppCompatButton kembali;
    private AppCompatButton hapus_popup;
    private AppCompatButton hapus_kembali_popup;
    private AppCompatButton hapus_batalkan_popup;
    private AppCompatButton hapus_kembali;
    private TextView pemanduStatus;
    private TextView hapusStatus;
    private Integer statusAlat;
    private String statusFinger;
    private String rollFingerId;
    //popup dialog
    Dialog validasi_roll, scan_finger;
    Dialog validasi_del, hapus_finger;
    // variable valuelistener untuk validasi fingerprint
    ValueEventListener rollEventListener;
    ValueEventListener delEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingger_activity);

        //call database reference and retrive to view
        databaseReference = database.getReference("Device_50:02:91:C9:DF:C4");

        getData();

        maxFinger = findViewById(R.id.max_finger);
        tambahFinger = findViewById(R.id.tambah_sidikjari);
        noData = findViewById(R.id.card_nodata);

        tambahFinger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validasiRoll();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_fingger);

        adapter = new FinggerAdapter(dataList,this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(FinggerActivity.this);

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);
    }

    public void getData(){
        dataList = new ArrayList<>();
        DatabaseReference dataFinger = databaseReference.child("raw_data").child("finger_data");
        dataFinger.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    //mengambil data fingerprint dari alat
                    String idFinger = snapshot.getValue(String.class);
                    String[] separator = idFinger.split(":");
                    String lastData = "";
                    Integer counter = 0;
                    for (String item : separator) {
                        counter++;
                        lastData = item;
                        dataList.add(new FinggerClass(item));
                    }
                    Log.d("hasilSplit", lastData);
                    //validasi tidak menampilkan opsi tambah finger ketika sudah
                    //terdapat 5 finger yang terdaftar
                    if (counter >= 5) {
                        tambahFinger.setVisibility(View.GONE);
                        maxFinger.setVisibility(View.VISIBLE);
                    } else if (lastData.equals("0")){
                        tambahFinger.setVisibility(View.VISIBLE);
                        noData.setVisibility(View.VISIBLE);
                    } else {
                        tambahFinger.setVisibility(View.VISIBLE);
                        maxFinger.setVisibility(View.GONE);
                    }

                    //penentuan id finger ketika ingin menambahkan finger baru
                    Integer lastFingerId = 0, i = 0;
                    for (String add : separator) {
                        lastFingerId = Integer.parseInt(add);
                        i++;
                        if (!i.equals(lastFingerId)) {
                            lastFingerId = i;
                            break;
                        } else {
                            lastFingerId = Integer.parseInt(add) + 1;
                        }
                        Log.d("dataSpace", String.valueOf(lastFingerId));
                    }
                    rollFingerId = "enfinger" + lastFingerId.toString();
                    Log.d("dataRoll", rollFingerId);
                }else{
                    noData.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FinggerActivity.this, "Data gagal didapatkan", Toast.LENGTH_SHORT).show();
            }
        });
        //mendapatkan data status device
        DatabaseReference status_alat = databaseReference.child("flag_status").child("status_device");
        status_alat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    statusAlat = snapshot.getValue(Integer.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FinggerActivity.this, "Data gagal didapatkan", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void validasiRoll(){
        validasi_roll = new Dialog(this);
        validasi_roll.setContentView(R.layout.popup_valroll);
        validasi_roll.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        validasi_roll.show();

        finggerSucess = validasi_roll.findViewById(R.id.fingger_sucess);
        finggerDissmis = validasi_roll.findViewById(R.id.finger_dissmis);

        if (statusAlat == 0) {
            finggerDissmis.setVisibility(View.VISIBLE);
        }else{
            finggerSucess.setVisibility(View.VISIBLE);
        }

        lanjutkan_popup = validasi_roll.findViewById(R.id.lanjutkan);
        lanjutkan_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //upload trigger enroll sidik jari
                databaseReference.child("flag_status")
                        .child("status_finger")
                        .setValue(rollFingerId);
                popupRollFinger();
            }
        });

        batalkan_popup = validasi_roll.findViewById(R.id.cancel);
        batalkan_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validasi_roll.dismiss();
            }
        });

        kembali_popup = validasi_roll.findViewById(R.id.kembali);
        kembali_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validasi_roll.dismiss();
            }
        });
    }

    public void popupRollFinger(){
        validasi_roll.dismiss();
        scan_finger = new Dialog(this);
        scan_finger.setContentView(R.layout.popup_rollfinger);
        scan_finger.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        scan_finger.setCancelable(false);
        scan_finger.setCanceledOnTouchOutside(false);
        scan_finger.show();

        //menampilkan status scaning
        pemanduStatus = scan_finger.findViewById(R.id.status_scan);

        //button trigger untuk kembali
        kembali = scan_finger.findViewById(R.id.kembali);

        //lokasi field database penambahan sidik jari
        DatabaseReference dataFingerStatus = databaseReference.child("flag_status").child("status_finger");

        //fungsi mendapatkan data status finger
        rollEventListener = new ValueEventListener() {
            Integer none_status;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    statusFinger = snapshot.getValue(String.class);

                    kembali.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!statusFinger.equals("noneAction")) {
                                databaseReference.child("flag_status")
                                        .child("status_finger")
                                        .setValue("done0");
                                Toast.makeText(FinggerActivity.this, "Penambahan sidik jari berhasil", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            none_status = 1;
                            Log.d("statusFinger", statusFinger.toString());
                        }
                    });

                    if (statusFinger.equals(rollFingerId)) {
                        pemanduStatus.setText("Mulai melakukan scan sidik jari..");
                        kembali.setVisibility(View.GONE);
                    } else if (statusFinger.equals("firstPlace")) {
                        pemanduStatus.setText("Letakan sidik jari pengguna..");
                        kembali.setVisibility(View.GONE);
                    } else if (statusFinger.equals("placeAgain")) {
                        pemanduStatus.setText("Letakan sidik jari sekali lagi..");
                        kembali.setVisibility(View.GONE);
                    } else if (statusFinger.equals("fail")) {
                        pemanduStatus.setText("Sidik jari tidak sesuai, gagal disimpan..");
                        kembali.setVisibility(View.VISIBLE);
                    } else if (statusFinger.equals("enDone")) {
                        pemanduStatus.setText("Sidik jari berhasil ditambahkan..");
                        kembali.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FinggerActivity.this, "Data gagal didapatkan", Toast.LENGTH_SHORT).show();
            }
        };
        dataFingerStatus.addValueEventListener(rollEventListener);
    }

    public void validasiDel(String fingger_id){
        validasi_del = new Dialog(this);
        validasi_del.setContentView(R.layout.popup_valdel);
        validasi_del.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        validasi_del.show();

        delFingerSuccess = validasi_del.findViewById(R.id.hapus_success);
        delFingerDissmis = validasi_del.findViewById(R.id.hapus_dissmis);

        hapus_popup = validasi_del.findViewById(R.id.lanjutkan_hapus);
        hapus_kembali_popup = validasi_del.findViewById(R.id.kembali);
        hapus_batalkan_popup = validasi_del.findViewById(R.id.cancel);

        if (statusAlat == 0) {
            delFingerDissmis.setVisibility(View.VISIBLE);
        }else{
            delFingerSuccess.setVisibility(View.VISIBLE);
        }

        hapus_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupDeleteFinger(fingger_id);
            }
        });

        hapus_kembali_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validasi_del.dismiss();
            }
        });

        hapus_batalkan_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validasi_del.dismiss();
            }
        });
    }

    public void popupDeleteFinger(String fingger_id){
        validasi_del.dismiss();
        hapus_finger = new Dialog(this);
        hapus_finger.setContentView(R.layout.popup_delfinger);
        hapus_finger.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        hapus_finger.setCancelable(false);
        hapus_finger.setCanceledOnTouchOutside(false);
        hapus_finger.show();

        hapus_kembali = hapus_finger.findViewById(R.id.kembali);
        hapusStatus = hapus_finger.findViewById(R.id.status_hapus);

        //lokasi field database penambahan sidik jari
        DatabaseReference dataFingerStatus = databaseReference.child("flag_status").child("status_finger");

        String id = "delfinger"+fingger_id;
        dataFingerStatus.setValue(id);

        delEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    statusFinger = snapshot.getValue(String.class);

                    hapus_kembali.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!statusFinger.equals("noneAction")) {
                                databaseReference.child("flag_status")
                                        .child("status_finger")
                                        .setValue("done0");
                                Toast.makeText(FinggerActivity.this, "Hapus fingerprint berhasil", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });

                    if (statusFinger.equals(id)) {
                        hapusStatus.setText("Mohon tunggu hingga proses selesai..");
                        delFingerSuccess.setVisibility(View.VISIBLE);
                    } else if (statusFinger.equals("delDone")) {
                        hapusStatus.setText("Hapus fingerprint berhasil");
                        delFingerSuccess.setVisibility(View.VISIBLE);
                        hapus_kembali.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FinggerActivity.this, "Data gagal didapatkan", Toast.LENGTH_SHORT).show();
            }
        };
        dataFingerStatus.addValueEventListener(delEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(rollEventListener != null){
            databaseReference.removeEventListener(rollEventListener);
        }else if(delEventListener != null){
            databaseReference.removeEventListener(delEventListener);
        }
    }

    @Override
    public void onImageClick(String idFinger) {
        validasiDel(idFinger);
    }
}
