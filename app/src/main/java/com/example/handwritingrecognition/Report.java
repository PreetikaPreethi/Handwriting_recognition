package com.example.handwritingrecognition;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Report extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextIssue;
    private Spinner spinnerIssueType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Initialize UI Elements
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextIssue = findViewById(R.id.editTextIssue);
        Button btnSubmit = findViewById(R.id.btn_submit);
        ImageButton btnBack = findViewById(R.id.btn_back);

        // Back Button Click - Close Activity
        btnBack.setOnClickListener(v -> finish());

        // Setup Spinner Options
        Spinner spinner = findViewById(R.id.spinnerIssueType);

// Load items from strings.xml
        String[] issueTypes = getResources().getStringArray(R.array.issue_types);

// Create an adapter using the custom layouts
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, issueTypes);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item); // Custom dropdown layout

// Apply adapter to Spinner
        spinner.setAdapter(adapter);


        // Submit Report Button Click
        btnSubmit.setOnClickListener(v -> submitReport());
    }

    private void submitReport() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String issueType = spinnerIssueType.getSelectedItem().toString();
        String issueDescription = editTextIssue.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || issueDescription.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send report via email
        String subject = "App Issue Report: " + issueType;
        String message = "Name: " + name + "\nEmail: " + email + "\nIssue Type: " + issueType + "\n\nIssue Details:\n" + issueDescription;

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:support@yourapp.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (Exception e) {
            Toast.makeText(this, "No email apps installed", Toast.LENGTH_SHORT).show();
        }
    }
}
