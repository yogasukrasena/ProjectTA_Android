package com.example.taproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
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
    private ImageView notifImage;
    private String tanggal, waktu, notifLog;
    private String dataNotifikasi, lokasiData, statusLokasi;
    private Double gpsLat, gpsLong;
    private Integer status_device, status_gps, status_tombol, status_notif;
    private Integer count_status = 0;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu_activity);

        startService(new Intent(this, NotificationService.class));

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
        notifImage = findViewById(R.id.notif_ada);
        //call function
        getData();
        mStatusDevice.run();

        //fungsi menampilkan foto profile
        DatabaseReference dataFotoProfile = databaseReference.child("foto_profile");
        dataFotoProfile.addValueEventListener(new ValueEventListener() {
            String linkFoto;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    linkFoto = snapshot.getValue(String.class);
                    showProfile(imageProfile, linkFoto);
                }
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

        //onclik baterai to open baterai log
        CardView baterai = (CardView) findViewById(R.id.cardBaterai);
        baterai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBatteryActivity();
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

        //onclik notifikasi to setting menu
        ImageView  notifIcon = (ImageView) findViewById(R.id.notifikasi);
        notifIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotifActivity();
            }
        });
    }

    private Runnable mStatusDevice = new Runnable() {
        @Override
        public void run() {
            count_status = count_status - 1;
            mHandler.postDelayed(this,1000);
            Log.d("count_data", String.valueOf(count_status));
            if(count_status <= 0){
                databaseReference.child("flag_status").child("status_device").setValue(0);
                databaseReference.child("flag_status").child("status_gps").setValue(0);
                databaseReference.child("raw_data").child("battery_level").setValue(0);
                databaseReference.child("raw_data").child("bpm_level").setValue(0);
                databaseReference.child("raw_data").child("spo2_level").setValue(0);
                databaseReference.child("raw_data").child("selisih_data").setValue("00:00");
            }else if(count_status>0){
                databaseReference.child("flag_status").child("status_device").setValue(1);
            }
        }
    };

    //fungsi mengambil foto profile default ketika belum ada foto yg diupload
    public int getDefaultImage(String imageName){
        int drawableResourceId = this.getResources().getIdentifier(imageName, "drawable", this.getPackageName());
        return drawableResourceId;
    }

    //menampilkan foto profile dari firebase
    public void showProfile(ImageView imageView, String linkFoto){
        StorageReference dataFotoRef = storageReference.child("foto_profile/").child(linkFoto);
        if(linkFoto.equals("default_foto")){
            Glide.with(getApplicationContext()).load(getDefaultImage("man")).into(imageView);
        }else{
            Glide.with(getApplicationContext()).load(dataFotoRef).into(imageView);
            Log.d("getFoto", String.valueOf(dataFotoRef));
        }
    }

    public void getData(){
        //mendapatkan data flag status
        DatabaseReference dataFlagStatus = databaseReference.child("flag_status");
        dataFlagStatus.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //parameter penentu status device dan gps
                    status_device = snapshot.child("status_device").getValue(Integer.class);
                    status_gps = snapshot.child("status_gps").getValue(Integer.class);
                    status_tombol = snapshot.child("status_button").getValue(Integer.class);
                    status_notif = snapshot.child("status_notifikasi").getValue(Integer.class);
                    //status alat
                    if(status_device == 1){
                        //menampilkan status connect serta kalkulasi selisih waktu pada halaman home
                        retriveStatusDevice.setText(getString(R.string.connect));
                        retriveStatusDevice.setTextColor(getColor(R.color.connect));
                    }else{
                        retriveStatusDevice.setText(getString(R.string.disconect));
                        retriveStatusDevice.setTextColor(getColor(R.color.disconnect));
                    }
                    //status gps
                    if(status_gps == 1){
                        retriveStatusGps.setText(getString(R.string.connect));
                        retriveStatusGps.setTextColor(getColor(R.color.connect));
                        statusLokasi = "Terkoneksi. ";
                    }else{
                        retriveStatusGps.setText(getString(R.string.disconect));
                        retriveStatusGps.setTextColor(getColor(R.color.disconnect));
                        statusLokasi = "Tidak Terkoneksi. ";
                    }
                    //status tombol notifikasi
                    if(status_tombol == 5){
                        notificationPengguna();
                    }
                    //tanda notifikasi
                    if(status_notif == 1){
                        notifImage.setVisibility(View.VISIBLE);
                    }else{
                        notifImage.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Fail to get data status", Toast.LENGTH_SHORT).show();
            }
        });

        //mendapatkan data raw data
        DatabaseReference dataRaw = databaseReference.child("raw_data");
        dataRaw.addValueEventListener(new ValueEventListener() {
            Integer bpm_value, baterai_value, spo_value;
            String get_waktu;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //mendapatkan nilai data pada setiap field data raw
                    bpm_value = snapshot.child("bpm_level").getValue(Integer.class);
                    baterai_value = snapshot.child("battery_level").getValue(Integer.class);
                    spo_value = snapshot.child("spo2_level").getValue(Integer.class);
                    get_waktu = snapshot.child("selisih_data").getValue(String.class);
                    gpsLat = snapshot.child("GPS_Lat").getValue(Double.class);
                    gpsLong = snapshot.child("GPS_Long").getValue(Double.class);
                    lokasiData = "https://maps.google.com/?q="+gpsLat+","+gpsLong;
                    //mengirimkan notifikasi berdasarkan kondisi baterai
                    if(baterai_value == 20 || baterai_value == 10){
                        dataNotifikasi = "Baterai perangkat lemah, sisa: "+baterai_value+"%. Status lokasi perangkat adalah : "+statusLokasi+
                                "Klik untuk melihat lokasi terakhir perangkat.";
                        notificationPerangkat();
                    }else if(baterai_value == 5 || baterai_value <= 3 && baterai_value > 0){
                        dataNotifikasi = "Baterai perangkat habis, sisa: "+baterai_value+"%. Status lokasi perangkat adalah : "+statusLokasi+
                                "Klik untuk melihat lokasi terakhir perangkat.";
                        notificationPerangkat();
                    }
                    //menampilkan data pada menu home
                    retriveBattery.setText(Integer.toString(baterai_value));
                    retriveBPM.setText(Integer.toString(bpm_value));
                    retriveSpo2.setText(Integer.toString(spo_value));
                    retriveRuntime.setText(get_waktu);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Fail to get data raw", Toast.LENGTH_SHORT).show();
            }
        });
        //mendapatkan data selisih untuk log
        DatabaseReference dataLog = databaseReference.child("log_data");
        dataLog.addValueEventListener(new ValueEventListener() {
            String waktu_mulai, waktu_berjalan, count_waktu;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                    String child_terbaru = "";
                    for(DataSnapshot user : snapshot.getChildren()){
                        String namakey = user.getKey();
                        child_terbaru = namakey;
                    }
                    waktu_mulai = snapshot.child(child_terbaru+"/waktu_mulai").getValue(String.class);
                    waktu_berjalan = snapshot.child(child_terbaru+"/waktu_berjalan").getValue(String.class);
                    count_status = 70;
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
                    dataRaw.child("selisih_data").setValue(count_waktu);

                    //menampilkan data selisih pada halaman utama
                    dataLog.child(child_terbaru+"/selisih_waktu").setValue(count_waktu);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Fail to get data selisih", Toast.LENGTH_SHORT).show();
            }
        });

        //mendapatkan data pengguna tongkat dan aplikasi
        databaseReference.addValueEventListener(new ValueEventListener() {
            String nama_pengguna, nama_keluarga;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //get data identitas pengguna alat
                    nama_pengguna = snapshot.child("user_pengguna").getValue(String.class);
                    nama_keluarga = snapshot.child("user_keluarga").getValue(String.class);
                    //menampilkan data pengguna
                    retrivePengguna.setText(nama_pengguna);
                    retriveKeluarga.setText(nama_keluarga);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Fail to get data pengguna", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String dataTanggal(){
        SimpleDateFormat getTgl = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat getWkt = new SimpleDateFormat("HH:mm");

        tanggal = getTgl.format(new Date());
        waktu = getWkt.format(new Date());

        notifLog = tanggal+"_"+waktu;

        return notifLog;
    }

    public void notificationPengguna(){
        //menentukan data dalam notifikasi
        String judulNotif = "Pesan Dari Pengguna";
        dataNotifikasi = "Pengguna perangkat meminta untuk dijemput. Status lokasi perangkat adalah : "+statusLokasi+
                "Klik untuk melihat lokasi terakhir perangkat.";

        //fungsi akses menu dari notifikasi
        Intent intent = new Intent(this, NotifActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_ONE_SHOT);
        //build notifikasi yang ditampilkan
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("n","n", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(R.color.white);
            channel.setDescription(judulNotif);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        //menentukan suara dan getaran notifikasi masuk
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] vibrate = {500,500};

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"n")
                .setSmallIcon(R.mipmap.blind_stick)
                .setContentTitle(judulNotif)
                .setContentText("Jemput Pengguna")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(dataNotifikasi))
                .setAutoCancel(true)
                .setSound(uri)
                .setVibrate(vibrate)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1, builder.build());

        //mengirimkan data notifikasi kedalam firebase
        databaseReference.child("flag_status/status_notifikasi").setValue(1);
        databaseReference.child("notif_data/"+dataTanggal()+"/jenis_pesan").setValue(judulNotif);
        databaseReference.child("notif_data/"+dataTanggal()+"/tanggal").setValue(tanggal);
        databaseReference.child("notif_data/"+dataTanggal()+"/waktu").setValue(waktu);
        databaseReference.child("notif_data/"+dataTanggal()+"/isi_pesan").setValue(dataNotifikasi);
        databaseReference.child("notif_data/"+dataTanggal()+"/url").setValue(lokasiData);
    }

    public void notificationPerangkat(){
        //menentukan isi dan judul pesan
        String judulPesan = "Pesan Dari Perangkat";
        //fungsi akses menu dari notifikasi
        Intent intent = new Intent(this, NotifActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_ONE_SHOT);
        //build notifikasi yang ditampilkan
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("n","n", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(R.color.white);
            channel.setDescription(judulPesan);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        //menentukan suara dan getaran dari notifikasi
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] vibrate = {500,500};

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"n")
                .setSmallIcon(R.mipmap.blind_stick)
                .setContentTitle(judulPesan)
                .setContentText("Baterai Perangkat Lemah")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(dataNotifikasi))
                .setAutoCancel(true)
                .setSound(uri)
                .setVibrate(vibrate)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(2, builder.build());

        //mengirimkan data notifikasi kedalam firebase
        databaseReference.child("flag_status/status_notifikasi").setValue(1);
        databaseReference.child("notif_data/"+dataTanggal()+"/jenis_pesan").setValue(judulPesan);
        databaseReference.child("notif_data/"+dataTanggal()+"/tanggal").setValue(tanggal);
        databaseReference.child("notif_data/"+dataTanggal()+"/waktu").setValue(waktu);
        databaseReference.child("notif_data/"+dataTanggal()+"/isi_pesan").setValue(dataNotifikasi);
        databaseReference.child("notif_data/"+dataTanggal()+"/url").setValue(lokasiData);
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

    public void  openBatteryActivity(){
        Intent intent = new Intent(this, BatteryActivity.class);
        startActivity(intent);
    }

    public void openSettingActivity(){
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    public void openNotifActivity(){
        Intent intent = new Intent(this, NotifActivity.class);
        startActivity(intent);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_BACK){
//            finish();
//        }
//        return super.onKeyDown(keyCode,event);
//    }
}