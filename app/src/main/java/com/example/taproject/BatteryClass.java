package com.example.taproject;

public class BatteryClass {
    private Integer baterai_mulai;
    private Integer baterai_terakhir;
    private String tanggal;
    private String waktu_mulai;
    private String waktu_berjalan;

    public BatteryClass(){

    }

    public BatteryClass(Integer baterai_mulai, Integer baterai_terakhir, String tanggal, String waktu_mulai, String waktu_berjalan) {
        this.waktu_mulai = waktu_mulai;
        this.waktu_berjalan = waktu_berjalan;
        this.tanggal = tanggal;
        this.baterai_mulai = baterai_mulai;
        this.baterai_terakhir = baterai_terakhir;
    }

    public Integer getBaterai_mulai(){
        return baterai_mulai;
    }

    public Integer getBaterai_terakhir(){
        return baterai_terakhir;
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
