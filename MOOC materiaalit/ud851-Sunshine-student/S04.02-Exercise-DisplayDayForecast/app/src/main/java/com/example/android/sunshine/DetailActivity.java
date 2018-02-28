package com.example.android.sunshine;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class DetailActivity extends AppCompatActivity {

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);



        String weatherForDay = getIntent().getStringExtra("weather").toString();
        Toast.makeText(this , weatherForDay, Toast.LENGTH_SHORT)
                .show();
        // TODO (2) Display the weather forecast that was passed from MainActivity
    }
}