package com.example.taproject;

import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FinggerAdapter extends RecyclerView.Adapter<FinggerAdapter.FinggerViewHolder> {
    private ArrayList<FinggerClass> dataList;
    private OnImageClickListener onImageClickListener;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    Dialog hapusFinger;

    public FinggerAdapter(ArrayList<FinggerClass> dataList, OnImageClickListener onImageClickListener){
        this.dataList = dataList;
        this.onImageClickListener = onImageClickListener;
    }

    @Override
    public FinggerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.fingger_cardview,parent,false);
        return new FinggerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FinggerViewHolder holder, int position) {
        String idFinger = dataList.get(position).getFingger_id();
        holder.fingger_id.setText(idFinger);
        holder.hapus_finger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onImageClickListener.onImageClick(idFinger);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (dataList != null) ? dataList.size() : 0;
    }

    public class FinggerViewHolder extends RecyclerView.ViewHolder {
        private TextView fingger_id;
        private ImageView hapus_finger;

        public FinggerViewHolder(@NonNull View itemView) {
            super(itemView);
            fingger_id = (TextView) itemView.findViewById(R.id.fingger_id);
            hapus_finger = (ImageView) itemView.findViewById(R.id.hapus_finger);
        }
    }
}
