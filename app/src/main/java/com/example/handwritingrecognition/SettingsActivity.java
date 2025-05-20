package com.example.handwritingrecognition;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "selected_theme";

    private RadioGroup radioGroupTheme;
    private RadioButton radioLight, radioDark;
    private TextView tvHelp, tvInviteFriend, tvCheckUpdate;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load Theme Before Setting the Content View
        loadTheme();
        getSupportActionBar().hide(); // Hide action bar title

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnBack = findViewById(R.id.btn_back);
        radioGroupTheme = findViewById(R.id.radioGroup_theme);
        radioLight = findViewById(R.id.radio_light);
        radioDark = findViewById(R.id.radio_dark);
        tvHelp = findViewById(R.id.tv_help);
        tvInviteFriend = findViewById(R.id.tv_invite_friend);
        tvCheckUpdate = findViewById(R.id.tv_check_update);

        // Set current theme selection
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentTheme = prefs.getString(KEY_THEME, "light"); // Default is Light Mode

        if ("light".equals(currentTheme)) {
            radioLight.setChecked(true);
        } else {
            radioDark.setChecked(true);
        }

        // Back Button Click Listener
        btnBack.setOnClickListener(v -> finish());

        // Theme Change Logic (Save & Restart)
        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_light) {
                saveTheme("light");
            } else if (checkedId == R.id.radio_dark) {
                saveTheme("dark");
            }
            restartApp(); // Restart to apply theme
        });

        // Help - Navigate to Report Activity
        tvHelp.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, Report.class);
            startActivity(intent);
        });

        // Invite a Friend - Share App Link
        tvInviteFriend.setOnClickListener(v -> {
            String shareMessage = "Hey, check out this awesome handwriting recognition app! Download it here: [App Link]";
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Invite via"));
        });

        // Check for Updates (Redirects to Play Store)
        tvCheckUpdate.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + getPackageName())));
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });
    }

    // Save Theme Selection in SharedPreferences
    private void saveTheme(String theme) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_THEME, theme);
        editor.apply();
    }

    // Load Theme Before Setting Content View
    private void loadTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String theme = prefs.getString(KEY_THEME, "light");

        if ("dark".equals(theme)) {
            setTheme(R.style.Theme_Dark);
        } else {
            setTheme(R.style.Theme_Light);
        }
    }

    // Restart App to Apply Theme
    private void restartApp() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
