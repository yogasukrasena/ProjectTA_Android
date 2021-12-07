package com.example.taproject;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.TimeViewHolder> {
    private List<TimeClass> dataList;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;

    public TimeAdapter(List<TimeClass> dataList){
        this.dataList = dataList;
    }

    @Override
    public TimeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.time_cardview, parent, false);
        return new TimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TimeViewHolder holder, int position) {
        databaseReference = database.getReference("Device_50:02:91:C9:DF:C4");
        DatabaseReference dataLog = databaseReference.child("log_data");
        dataLog.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot data : snapshot.getChildren()){
                        String childNama = data.getKey();

                        String waktu_mulai = snapshot.child(childNama+"/waktu_mulai").getValue(String.class);
                        String waktu_berjalan = snapshot.child(childNama+"/waktu_berjalan").getValue(String.class);

                        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
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
                        String count_waktu = String.format("%02d:%02d",hours,min);

                        databaseReference.child("log_data").child(childNama+"/selisih_waktu").setValue(count_waktu);
                        Log.d("namaLog",childNama);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //ketika data gagal didapatkan
            }
        });

        //parsing data kedalam holder
        TimeClass timeClass = dataList.get(position);
        holder.tanggal.setText(timeClass.getTanggal());
        holder.waktu_mulai.setText(timeClass.getWaktu_mulai());
        holder.waktu_berjalan.setText(timeClass.getWaktu_berjalan());
        holder.selisih_waktu.setText(timeClass.getSelisih_waktu());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class TimeViewHolder extends RecyclerView.ViewHolder {
        private final TextView selisih_waktu;
        private final TextView tanggal;
        private final TextView waktu_mulai;
        private final TextView waktu_berjalan;

        public TimeViewHolder(View itemView) {
            super(itemView);
            selisih_waktu = (TextView) itemView.findViewById(R.id.timeValue);
            tanggal = (TextView) itemView.findViewById(R.id.tanggal);
            waktu_mulai = (TextView) itemView.findViewById(R.id.jam_mulai);
            waktu_berjalan = (TextView) itemView.findViewById(R.id.jam_akhir);
        }
    }
}
