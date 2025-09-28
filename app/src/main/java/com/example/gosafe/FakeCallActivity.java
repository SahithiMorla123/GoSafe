package com.example.gosafe;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class FakeCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_fake_call);


        Button btnDeclineCall = findViewById(R.id.btnDeclineCall);


        btnDeclineCall.setOnClickListener(v -> {
            finish();
        });
    }
}