package com.example.taproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BatteryAdapter extends RecyclerView.Adapter<BatteryAdapter.BatteryViewHolder> {
    private List<BatteryClass> dataList;

    public BatteryAdapter(List<BatteryClass> datalist){
        this.dataList = datalist;
    }
    
    @Override
    public BatteryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.battery_cardview, parent, false);
        return new BatteryAdapter.BatteryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BatteryViewHolder holder, int position) {
        BatteryClass batteryClass = dataList.get(position);
        holder.baterai_mulai.setText(String.valueOf(batteryClass.getBaterai_mulai()));
        holder.baterai_terakhir.setText(String.valueOf(batteryClass.getBaterai_terakhir()));
        holder.tanggal.setText(batteryClass.getTanggal());
        holder.waktu_mulai.setText(batteryClass.getWaktu_mulai());
        holder.waktu_berjalan.setText(batteryClass.getWaktu_berjalan());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class BatteryViewHolder extends RecyclerView.ViewHolder {
        private TextView baterai_mulai, baterai_terakhir, tanggal, waktu_mulai, waktu_berjalan;
        
        public BatteryViewHolder(@NonNull View itemView) {
            super(itemView);
            baterai_mulai = (TextView) itemView.findViewById(R.id.battery_start);
            baterai_terakhir = (TextView) itemView.findViewById(R.id.battery_now);
            tanggal = (TextView) itemView.findViewById(R.id.tanggal);
            waktu_mulai = (TextView) itemView.findViewById(R.id.jam_mulai);
            waktu_berjalan = (TextView) itemView.findViewById(R.id.jam_akhir);    
        }
    }
}
