package com.ufc.com.googlemapslocalizacaoeplayservices;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class TesteActivity extends AppCompatActivity {
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste);

        textView = findViewById(R.id.tvTexto);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("location");

        textView.setText(bundle.getString("location"));




    }
}
