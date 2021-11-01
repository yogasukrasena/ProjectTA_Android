package com.example.taproject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class SettingActivity extends AppCompatActivity {
    //deklarasi code firebase database dan storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    StorageReference storageReference;

    //code deklarasi edit text data pengguna dan keluarga
    private EditText namaPengguna;
    private EditText namaKeluarga;

    //code deklarasi elemen didalam popup
    private CardView uploadCardview;
    private ImageView imageContainer;
    private CardView progressCard;
    private ProgressBar progressUpload;

    //deklarasi elemen lainnya
    private TextView hapusLog;
    private AppCompatButton submit_hapuslog;
    private AppCompatButton cancel_hapuslog;
    private TextView fingerMenu;
    private AppCompatButton submit;
    private AppCompatButton cancel;
    private ImageView fotoProfile;

    //Kode permintaan untuk memilih metode pengambilan gamabr
    private static final int REQUEST_CODE_CAMERA = 1;
    private static final int REQUEST_CODE_GALLERY = 2;
    Dialog uploadFoto;
    Dialog hapusLogAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);

        //call database reference and retrive to view
        databaseReference = database.getReference("Device_50:02:91:C9:DF:C4");
        //Mendapatkan Referensi dari Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference();

        //menampilkan data nama pengguna dan keluarga
        getData();

        fotoProfile = findViewById(R.id.foto_profile);
        databaseReference.addValueEventListener(new ValueEventListener() {
            String linkFoto;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                linkFoto = snapshot.child("foto_profile").getValue(String.class);
                showProfile(fotoProfile,linkFoto);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingActivity.this, "Gagal mengambil foto profile", Toast.LENGTH_SHORT).show();
            }
        });

        fotoProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tampilPopUp(v);
            }
        });

        //onclik hapus fingger
        hapusLog = (TextView) findViewById(R.id.hapus_log);
        hapusLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hapusLogAll();
            }
        });

        //onclik in setting to fingger list
        fingerMenu = (TextView) findViewById(R.id.fingger_button);
        fingerMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFinggerPrint();
            }
        });

        //onclik upload data
        submit = (AppCompatButton) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData();
            }
        });

        //onclik cancel
        cancel = (AppCompatButton) findViewById(R.id.cancel);
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

    //fungsi mengambil foto profile default ketika belum ada foto yg diupload
    public int getDefaultImage(String imageName){
        int drawableResourceId = this.getResources().getIdentifier(imageName, "drawable", this.getPackageName());
        return drawableResourceId;
    }

    //menampilkan foto profile dari firebase
    public void showProfile(ImageView imageView, String linkFoto){
        StorageReference dataFotoRef = storageReference.child("foto_profile/").child(linkFoto);
        if(linkFoto.equals("default_foto")){
            Glide.with(SettingActivity.this).load(getDefaultImage("man")).into(imageView);
        }else{
            if(!SettingActivity.this.isFinishing()){
                Glide.with(SettingActivity.this).load(dataFotoRef).into(imageView);
                Log.d("getFoto", String.valueOf(dataFotoRef));
            }
        }
    }

    //fungsi upload data nama pengguna dan nama keluarga
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

    //fungsi menampilkan popup opsi upload foto profile
    public void tampilPopUp(View view){
        uploadFoto = new Dialog(this);
        uploadFoto.setContentView(R.layout.popup_setting_activity);
        uploadFoto.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        uploadFoto.show();

        //deklarasi elemen didalam window popup
        uploadCardview = uploadFoto.findViewById(R.id.upload_cardview);
        imageContainer = uploadFoto.findViewById(R.id.image_container);
        progressCard = uploadFoto.findViewById(R.id.progress_card);
        progressUpload = uploadFoto.findViewById(R.id.progress_upload);

        //button pada view pilih sumber gambar
        AppCompatButton ambilGambar = (AppCompatButton) uploadFoto.findViewById(R.id.ambil_gambar);
        ambilGambar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fungsi ambil gambar dari camera
                Intent imageIntentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(imageIntentCamera, REQUEST_CODE_CAMERA);
            }
        });

        AppCompatButton cariGambar = (AppCompatButton) uploadFoto.findViewById(R.id.cari_gambar);
        cariGambar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fungsi cari gambar dari file storage
                Intent imageIntentGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(imageIntentGallery, REQUEST_CODE_GALLERY);
            }
        });

        //button pada halaman view upload gambar
        AppCompatButton uploadGambar = (AppCompatButton) uploadFoto.findViewById(R.id.upload_button);
        uploadGambar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        AppCompatButton cancelGambar = (AppCompatButton) uploadFoto.findViewById(R.id.cancel_foto_button);
        cancelGambar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cancel upload gambar
                uploadCardview.setVisibility(View.GONE);
            }
        });
    }

    //fungsi untuk melakukan handel hasil data dari kedua sumber
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                if (resultCode == RESULT_OK) {
                    uploadCardview.setVisibility(View.VISIBLE);
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    imageContainer.setImageBitmap(bitmap);
                } break;
            case REQUEST_CODE_GALLERY:
                if (resultCode == RESULT_OK) {
                    uploadCardview.setVisibility(View.VISIBLE);
                    Uri uri = data.getData();
                    imageContainer.setImageURI(uri);
                } break;
        }
    }

    //fungsi upload foto profile dengan compress
    private void uploadImage(){
        //Mendapatkan data dari ImageView sebagai Bytes
        imageContainer.setDrawingCacheEnabled(true);
        imageContainer.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageContainer.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //Mengkompress bitmap menjadi JPG dengan kualitas gambar 80%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream);
        byte[] bytes = stream.toByteArray();

        //Lokasi lengkap dimana gambar akan disimpan
        String nama_file = System.currentTimeMillis()+".jpg";
        String path_image = "foto_profile/"+nama_file;
        UploadTask uploadTask = storageReference.child(path_image).putBytes(bytes);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressCard.setVisibility(View.GONE);
                uploadCardview.setVisibility(View.GONE);
                uploadFoto.dismiss();
                Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUrl = uri.toString();
                        databaseReference.child("foto_profile").setValue(nama_file);
                        Log.d("HasilUrl", imageUrl);
                    }
                });
                Toast.makeText(SettingActivity.this, "Uploading Berhasil", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressCard.setVisibility(View.GONE);
                uploadCardview.setVisibility(View.GONE);
                uploadFoto.dismiss();
                Toast.makeText(SettingActivity.this, "Uploading Gagal", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                progressCard.setVisibility(View.VISIBLE);
                uploadCardview.setVisibility(View.GONE);
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressUpload.setProgress((int) progress);
                // menghapus foto sebelumnya
                ValueEventListener getDataFoto = new ValueEventListener() {
                    String linkFoto;
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        linkFoto = snapshot.child("foto_profile").getValue(String.class);
                        StorageReference dataFotoRef = storageReference.child("foto_profile/").child(linkFoto);
                        Log.d("fotoHapus", String.valueOf(dataFotoRef));
                        dataFotoRef.delete();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SettingActivity.this, "Foto profile sebelumnya gagal dihapus", Toast.LENGTH_SHORT).show();
                    }
                };databaseReference.addListenerForSingleValueEvent(getDataFoto);
            }
        });
    }

    //fungsi hapus semua log
    //menyisakan satu data log terakhir untuk mempertahankan struktur data pada aplikasi
    public void hapusLogAll(){
        hapusLogAll = new Dialog(this);
        hapusLogAll.setContentView(R.layout.popup_dellog);
        hapusLogAll.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        hapusLogAll.show();

        submit_hapuslog = hapusLogAll.findViewById(R.id.lanjutkan_hapus);
        submit_hapuslog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DataSnapshot datalog = snapshot.child("log_data");
                        String child_terbaru = "";
                        for(DataSnapshot user : datalog.getChildren()){
                            String namakey = user.getKey();
                            child_terbaru = namakey;
                        }
                        Log.d("namaLog", child_terbaru);
                        // validasi jumlah data yang masih tersedia
                        for(DataSnapshot hapus : datalog.getChildren()){
                            int data = (int) datalog.getChildrenCount();
                            if(data == 1){
                                Toast.makeText(SettingActivity.this, "Data log sudah bersih", Toast.LENGTH_SHORT).show();
                            }else{
                                if(!hapus.getKey().equals(child_terbaru)){
                                    hapus.getRef().removeValue();
                                    Toast.makeText(SettingActivity.this, "Data log berhasil dihapus", Toast.LENGTH_SHORT).show();
                                    hapusLogAll.dismiss();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SettingActivity.this, "Gagal menghapus log", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        cancel_hapuslog = hapusLogAll.findViewById(R.id.cancel);
        cancel_hapuslog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hapusLogAll.dismiss();
            }
        });
    }

    public void openFinggerPrint(){
        Intent intent = new Intent(this, FinggerActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(keyCode,event);
    }
}
