package com.example.taproject;

public class NotifClass {
    private String jenis_pesan;
    private String isi_pesan;
    private String waktu;
    private String tanggal;
    private String url;

    public NotifClass(){

    }

    public NotifClass(String jenis_pesan, String isi_pesan, String waktu, String tanggal, String url){
        this.isi_pesan = isi_pesan;
        this.jenis_pesan = jenis_pesan;
        this.waktu = waktu;
        this.tanggal = tanggal;
        this.url = url;
    }

    public String getJenis_pesan(){
        return jenis_pesan;
    }
    public String getIsi_pesan(){
        return isi_pesan;
    }
    public String getWaktu(){
        return waktu;
    }
    public String getTanggal(){
        return tanggal;
    }
    public String getUrl() {
        return url;
    }
}
