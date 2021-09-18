package com.example.findmysenpai2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        Intent switchToMapActivity = new Intent(this, MapActivity.class);
        this.startActivity(switchToMapActivity);
    }

}