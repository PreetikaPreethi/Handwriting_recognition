package com.example.handwritingrecognition;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ContactActivity extends AppCompatActivity {

    private EditText etName, etEmail, etMessage;
    private Button btnSubmit;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        // Initialize Views
        btnBack = findViewById(R.id.btn_back);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etMessage = findViewById(R.id.et_message);
        btnSubmit = findViewById(R.id.btn_submit);

        // Back Button Click
        btnBack.setOnClickListener(v -> finish());

        // Submit Button Click
        btnSubmit.setOnClickListener(v -> sendEmail());
    }

    private void sendEmail() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        String mailto = "mailto:support@yourapp.com" +
                "?subject=" + Uri.encode("Support Request from " + name) +
                "&body=" + Uri.encode("Name: " + name + "\nEmail: " + email + "\n\nMessage:\n" + message);

        Intent emailIntent = new Intent(Intent.ACTION_VIEW);
        emailIntent.setData(Uri.parse(mailto));

        try {
            startActivity(emailIntent);
        } catch (Exception e) {
            Toast.makeText(this, "No email client found", Toast.LENGTH_SHORT).show();
        }
    }
}
