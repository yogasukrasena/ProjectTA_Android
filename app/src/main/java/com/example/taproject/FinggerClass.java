package com.example.taproject;

import android.widget.ImageView;

public class FinggerClass {
    private String fingger_id;

    public FinggerClass(String fingger_id)
    {
        this.fingger_id = fingger_id;
    }

    public String getFingger_id(){
        return fingger_id;
    }

    public void setFingger_id(){
        this.fingger_id = fingger_id;
    }

}
