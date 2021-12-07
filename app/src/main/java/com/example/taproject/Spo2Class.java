package com.example.taproject;

public class Spo2Class {
    private Integer spo_min;
    private Integer spo_max;
    private String tanggal;
    private String waktu_mulai;
    private String waktu_berjalan;

    public Spo2Class(){

    }

    public Spo2Class(Integer spo_min, Integer spo_max, String tanggal, String waktu_mulai, String waktu_berjalan) {
        this.waktu_mulai = waktu_mulai;
        this.waktu_berjalan = waktu_berjalan;
        this.tanggal = tanggal;
        this.spo_min = spo_min;
        this.spo_max = spo_max;
    }

    public Integer getSpo_max(){
        return spo_max;
    }

    public Integer getSpo_min(){
        return spo_min;
    }

    public String getTanggal(){
        return tanggal;
    }

    public String getWaktu_mulai(){
        return waktu_mulai;
    }

    public String getWaktu_berjalan(){
        return waktu_berjalan;
    }

}
