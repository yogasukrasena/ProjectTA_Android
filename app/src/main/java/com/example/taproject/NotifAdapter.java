package com.example.taproject;

import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.NotifViewHolder> {
    private List<NotifClass> dataList;
    private OnNotifClickListener onNotifClickListener;

    public NotifAdapter(List<NotifClass> dataList, OnNotifClickListener onNotifClickListener){
        this.dataList = dataList;
        this.onNotifClickListener = onNotifClickListener;
    }

    @Override
    public NotifViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.notif_cardview, parent, false);
        return new NotifAdapter.NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotifViewHolder holder, int position) {
        NotifClass notifClass = dataList.get(position);
        holder.jenis_notif.setText(notifClass.getJenis_pesan());
        holder.isi_notif.setText(notifClass.getIsi_pesan());
        holder.waktu.setText(notifClass.getWaktu());
        holder.tanggal.setText(notifClass.getTanggal());
        String urlMaps = notifClass.getUrl();
        holder.url_maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNotifClickListener.onNotifClick(urlMaps);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class NotifViewHolder extends RecyclerView.ViewHolder {
        private TextView jenis_notif, isi_notif, waktu, tanggal;
        private CardView url_maps;
        public NotifViewHolder(View itemView) {
            super(itemView);
            jenis_notif = (TextView) itemView.findViewById(R.id.jenis_notif);
            isi_notif = (TextView) itemView.findViewById(R.id.isi_notif);
            waktu = (TextView) itemView.findViewById(R.id.waktu);
            tanggal = (TextView) itemView.findViewById(R.id.tanggal);
            url_maps = (CardView) itemView.findViewById(R.id.click_maps);
        }
    }
}
