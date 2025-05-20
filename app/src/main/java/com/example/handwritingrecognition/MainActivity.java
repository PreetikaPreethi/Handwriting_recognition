package com.example.handwritingrecognition;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.MenuItem;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CardView cardStartWriting, cardUploadImage;
    private ActionBarDrawerToggle toggle;
    private static final int REQUEST_CAMERA = 1;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        cardStartWriting = findViewById(R.id.cardStartWriting);
        cardUploadImage = findViewById(R.id.cardUploadImage);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable Navigation Drawer Toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Open Writing Activity
        cardStartWriting.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WritingActivity.class);
            startActivity(intent);
        });

        //open upload image
        cardUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ImageProcessingActivity.class);
            startActivity(intent);
        });

        // Initialize Activity Result Launchers

        // Handle Navigation Drawer Item Clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    drawerLayout.closeDrawers();
                    return true;
                } else if (id == R.id.nav_settings) {
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    return true;
                } else if (id == R.id.nav_about) {
                    Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(aboutIntent);
                    return true;
                }
                else if (id == R.id.nav_Contact){
                    Intent contactIntent = new Intent(MainActivity.this, ContactActivity.class);
                    startActivity(contactIntent);
                    return true;
                }
                else if (id == R.id.nav_privacy){
                    Intent privacyIntent = new Intent(MainActivity.this, Privacy.class);
                    startActivity(privacyIntent);
                    return true;
                }
                else if (id == R.id.nav_Report){
                    Intent reportIntent = new Intent(MainActivity.this, Report.class);
                    startActivity(reportIntent);
                    return true;

                }
                return false;
            }
        });
    }

}
