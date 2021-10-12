package com.example.taproject;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    private EditText namaPengguna;
    private EditText namaKeluarga;
    Dialog uploadFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);

        //call database reference and retrive to view
        databaseReference = database.getReference("Device_50:02:91:C9:DF:C4");

        //menampilkan data nama pengguna dan keluarga
        getData();

        uploadFoto = new Dialog(this);

        ImageView imageView = (ImageView) findViewById(R.id.foto_profile);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tampilPopUp(v);
            }
        });

        //onclik in setting to fingger list
        TextView textView = (TextView) findViewById(R.id.fingger_button);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFinggerPrint();
            }
        });

        //onclik upload data
        CardView submit = (CardView) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData();
            }
        });
        //onclik cancel
        CardView cancel = (CardView) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
                Toast.makeText(SettingActivity.this, "Update dibatalkan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getData(){
        //menentukan penempatan text
        namaPengguna = (EditText) findViewById(R.id.username_tongkat);
        namaKeluarga = (EditText) findViewById(R.id.username_keluarga);

        databaseReference.addValueEventListener(new ValueEventListener() {
            String nama_pengguna, nama_keluarga;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nama_pengguna = dataSnapshot.child("user_pengguna").getValue(String.class);
                nama_keluarga = dataSnapshot.child("user_keluarga").getValue(String.class);

                namaKeluarga.setText(nama_keluarga, TextView.BufferType.EDITABLE);
                namaPengguna.setText(nama_pengguna, TextView.BufferType.EDITABLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SettingActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void uploadData(){
        String nama_keluarga, nama_pengguna;
        nama_keluarga = namaKeluarga.getText().toString();
        nama_pengguna = namaPengguna.getText().toString();
        int length_keluarga = nama_keluarga.length();
        int length_pengguna = nama_pengguna.length();
        if(length_keluarga > 15 || length_keluarga < 5){
            Toast.makeText(SettingActivity.this, "Panjang karakter Nama Keluarga melebihi 15 atau kurang dari 5", Toast.LENGTH_SHORT).show();
        }else if(length_pengguna > 15 || length_pengguna <5){
            Toast.makeText(SettingActivity.this, "Panjang karakter Nama Pengguna melebihi 15 atau kurang dari 5", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(nama_keluarga) || TextUtils.isEmpty(nama_pengguna)){
            Toast.makeText(SettingActivity.this, "Data Nama Pengguna dan Nama Keluarga tidak boleh kosong", Toast.LENGTH_SHORT).show();
        }else {
            databaseReference.child("user_keluarga").setValue(nama_keluarga);
            databaseReference.child("user_pengguna").setValue(nama_pengguna);
            Toast.makeText(SettingActivity.this, "Data berhasil tersimpan", Toast.LENGTH_SHORT).show();
        }
    }

    public void tampilPopUp(View view){
        uploadFoto.setContentView(R.layout.popup_activity);
        uploadFoto.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        uploadFoto.show();
    }

    public void openFinggerPrint(){
        Intent intent = new Intent(this, FinggerActivity.class);
        startActivity(intent);
    }
}
