package com.example.taproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    StorageReference storageReference;
    private TextView retriveBPM;
    private TextView retriveStatusDevice;
    private TextView retriveStatusGps;
    private TextView retriveSpo2;
    private TextView retriveRuntime;
    private TextView retrivePengguna;
    private TextView retriveKeluarga;
    private TextView retriveBattery;
    private ImageView imageProfile;
    private boolean device_status = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu_activity);

        //call database reference and retrive to view
        databaseReference = database.getReference("Device_50:02:91:C9:DF:C4");
        //Mendapatkan Referensi dari Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference();

        retrivePengguna = findViewById(R.id.nama_pengguna);
        retriveKeluarga = findViewById(R.id.nama_keluarga);
        retriveStatusDevice = findViewById(R.id.statusDevice);
        retriveStatusGps = findViewById(R.id.statusGps);
        retriveSpo2 = findViewById(R.id.spo2);
        retriveBPM = findViewById(R.id.BPM);
        retriveBattery = findViewById(R.id.baterai);
        retriveRuntime = findViewById(R.id.time);
        imageProfile = findViewById(R.id.foto_profile);
        //call function
        refreshData();
        getData();

        //fungsi menampilkan foto profile
        databaseReference.addValueEventListener(new ValueEventListener() {
            String linkFoto;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                linkFoto = snapshot.child("foto_profile").getValue(String.class);
                showProfile(imageProfile,linkFoto);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Gagal mengambil foto profile", Toast.LENGTH_SHORT).show();
            }
        });

        //initialize fragment
        Fragment fragment = new MapFragment();

        //open fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameMaps, fragment)
                .commit();

        //onclik maps to maps view
        CardView maps = (CardView) findViewById(R.id.mapsbutton);
        maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapsActivity();
            }
        });

        //onclik bpm to bpm log
        CardView bpm = (CardView) findViewById(R.id.cardBPM);
        bpm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBpmActivity();
            }
        });

        //onclik power to power log
        CardView spo2 = (CardView) findViewById(R.id.cardSpo);
        spo2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openPowerActivity();
            }
        });

        //onclik time to time log
        CardView time = (CardView) findViewById(R.id.cardTime);
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimeActivity();
            }
        });

        //onclik setting to setting menu
        ImageView setting = (ImageView) findViewById(R.id.setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingActivity();
            }
        });

        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefreshMain);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
                pullToRefresh.setRefreshing(false);
            }
        });
    }
    //fungsi untuk melakukan refresh data
    public void refreshData(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(device_status){
                    device_status = false;
                    databaseReference.child("flag_status").child("status_device").setValue(0);
                    databaseReference.child("flag_status").child("status_gps").setValue(0);
                    databaseReference.child("raw_data").child("battery_level").setValue(0);
                    databaseReference.child("raw_data").child("bpm_level").setValue(0);
                    databaseReference.child("raw_data").child("spo2_level").setValue(0);
                    databaseReference.child("raw_data").child("selisih_data").setValue("00:00");
                }else{
                    handler.postDelayed(this, 100);
                }
            }
        },100);
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
            Glide.with(this).load(getDefaultImage("man")).into(imageView);
        }else{
            Glide.with(this).load(dataFotoRef).into(imageView);
            Log.d("getFoto", String.valueOf(dataFotoRef));
        }
    }

    public void getData(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            Integer status_device, status_gps;
            Integer bpm_value, baterai_value, spo_value;
            String nama_pengguna, nama_keluarga;
            String waktu_mulai, waktu_berjalan, count_waktu, get_waktu;
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //parameter penentu status device dan gps
                device_status = true;
                //get data identitas pengguna alat
                nama_pengguna = dataSnapshot.child("user_pengguna").getValue(String.class);
                nama_keluarga = dataSnapshot.child("user_keluarga").getValue(String.class);

                //get data field data flag
                DataSnapshot dataflag = dataSnapshot.child("flag_status");

                status_device = dataflag.child("status_device").getValue(Integer.class);
                status_gps = dataflag.child("status_gps").getValue(Integer.class);

                //get data field raw data
                DataSnapshot dataraw = dataSnapshot.child("raw_data");

                bpm_value = dataraw.child("bpm_level").getValue(Integer.class);
                baterai_value = dataraw.child("battery_level").getValue(Integer.class);
                spo_value = dataraw.child("spo2_level").getValue(Integer.class);

                DataSnapshot datalog = dataSnapshot.child("log_data");
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
                SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                Date datenow = new Date();
                String tanggal_sekarang = currentDate.format(datenow);
                String child_terbaru = "";
                for(DataSnapshot user : datalog.getChildren()){
                    String namakey = user.getKey();
                    String[] split = namakey.split("_");
                    String nilaikey = split[0];
                    if(nilaikey.equals(tanggal_sekarang)){
                        child_terbaru = namakey;
                    }else{
                        child_terbaru = namakey;
                    }
                }
                waktu_mulai = datalog.child(child_terbaru+"/waktu_mulai").getValue(String.class);
                waktu_berjalan = datalog.child(child_terbaru+"/waktu_berjalan").getValue(String.class);
                Date jam_mulai = null;
                Date jam_berjalan = null;
                try {
                    if(waktu_berjalan == null || waktu_mulai == null){
                        jam_mulai = df.parse(waktu_mulai);
                        jam_berjalan = df.parse(waktu_mulai);
                    }else{
                        jam_mulai = df.parse(waktu_mulai);
                        jam_berjalan = df.parse(waktu_berjalan);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long selisih = jam_mulai.getTime() - jam_berjalan.getTime();

                int days = (int) (selisih / (1000*60*60*24));
                int hours = (int) ((selisih - (1000*60*60*24*days)) / (1000*60*60));
                int min = (int) (selisih - (1000*60*60*24*days) - (1000*60*60*hours)) / (1000*60);
                hours = (hours < 0 ? -hours : hours);
                min = (min <0 ? -min : min);
                count_waktu = String.format("%02d:%02d",hours,min);

                //upload data kalkulasi selisih runing time alat ke dalam firebase
                databaseReference.child("log_data").child(child_terbaru+"/selisih_waktu").setValue(count_waktu);
                //get data selisih waktu
                get_waktu = dataraw.child("selisih_data").getValue(String.class);

                //set data retrive to view
                retrivePengguna.setText(nama_pengguna);
                retriveKeluarga.setText(nama_keluarga);
                retriveBattery.setText(Integer.toString(baterai_value));
                retriveBPM.setText(Integer.toString(bpm_value));
                retriveSpo2.setText(Integer.toString(spo_value));
                retriveRuntime.setText(get_waktu);

                if(status_device == 1){
                    //menampilkan status connect serta kalkulasi selisih waktu pada halaman home
                    databaseReference.child("raw_data").child("selisih_data").setValue(count_waktu);
                    retriveStatusDevice.setText(getString(R.string.connect));
                    retriveStatusDevice.setTextColor(getColor(R.color.connect));
                }else{
                    retriveStatusDevice.setText(getString(R.string.disconect));
                    retriveStatusDevice.setTextColor(getColor(R.color.disconnect));
                }

                if(status_gps == 1){
                    retriveStatusGps.setText(getString(R.string.connect));
                    retriveStatusGps.setTextColor(getColor(R.color.connect));
                }else{
                    retriveStatusGps.setText(getString(R.string.disconect));
                    retriveStatusGps.setTextColor(getColor(R.color.disconnect));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openMapsActivity(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void  openBpmActivity(){
        Intent intent = new Intent(this, BpmActivity.class);
        startActivity(intent);
    }

    public void  openPowerActivity(){
        Intent intent = new Intent(this, Spo2Activity.class);
        startActivity(intent);
    }

    public void  openTimeActivity(){
        Intent intent = new Intent(this, TimeActivity.class);
        startActivity(intent);
    }

    public void openSettingActivity(){
        Intent intent = new Intent(this, SettingActivity.class);
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