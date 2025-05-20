package com.example.handwritingrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class Privacy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        // Back Button Functionality
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish()); // Closes the activity

        // Accept Button Functionality
        Button btnAccept = findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(v -> {
            // Redirect to the main screen after accepting
            Intent intent = new Intent(Privacy.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
