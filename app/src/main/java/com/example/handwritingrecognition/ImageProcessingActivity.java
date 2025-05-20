package com.example.handwritingrecognition;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ImageProcessingActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView imageViewPreview;
    private TextView textViewResult;
    private MaterialButton btnUpload, btnSave, btnTranslate, btnRetry;
    private Uri imageUri;
    private String currentPhotoPath;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    // Image container (Holds multiple images)
    private ArrayList<Bitmap> imageContainer = new ArrayList<>();

    // Camera Image Capture
    private final ActivityResultLauncher<Uri> captureImageLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result) {
                    processCapturedImage();
                } else {
                    showToast("Failed to capture image");
                }
            });

    // Gallery Image Selection
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            Bitmap selectedImage = loadImage(selectedImageUri);
                            addImageToContainer(selectedImage);
                        } catch (IOException e) {
                            showToast("Failed to load image");
                            e.printStackTrace();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_processing);

        // Initialize UI elements
        btnBack = findViewById(R.id.btnBack);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        textViewResult = findViewById(R.id.textViewResult);
        btnUpload = findViewById(R.id.btnUpload);
        btnSave = findViewById(R.id.btnSave);
        btnTranslate = findViewById(R.id.btnTranslate);
        btnRetry = findViewById(R.id.btnRetry);

        btnBack.setOnClickListener(view -> finish());
        btnUpload.setOnClickListener(view -> showUploadOptions());
        btnSave.setOnClickListener(view -> saveText());
        btnTranslate.setOnClickListener(view -> translateText());
        btnRetry.setOnClickListener(view -> retryImageCapture());

        imageViewPreview.setOnClickListener(view -> showUploadOptions());

        // Request Camera & Storage Permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CAMERA_PERMISSION);
        }
    }

    // Show Upload Options (Camera / Gallery)
    private void showUploadOptions() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Upload Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        captureImage();
                    } else {
                        selectImageFromGallery();
                    }
                })
                .show();
    }

    // Capture Image Using Camera
    private void captureImage() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                captureImageLauncher.launch(imageUri);
            } else {
                showToast("Error creating image file");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to capture image");
        }
    }

    // Create Image File for Camera Capture
    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            File image = File.createTempFile("IMG_" + timeStamp, ".jpg", storageDir);
            currentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException e) {
            showToast("Error creating image file: " + e.getMessage());
            return null;
        }
    }

    // Process Captured Image and Save in Container
    private void processCapturedImage() {
        if (currentPhotoPath != null) {
            File imgFile = new File(currentPhotoPath);
            if (imgFile.exists()) {
                Bitmap capturedImage;
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        capturedImage = ImageDecoder.decodeBitmap(
                                ImageDecoder.createSource(new File(currentPhotoPath))
                        );
                    } else {
                        capturedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    }

                    // 👇 This line was missing: add the image to the container
                    addImageToContainer(capturedImage);

                } catch (IOException e) {
                    showToast("Failed to decode captured image");
                    e.printStackTrace();
                }

            } else {
                showToast("Image file not found!");
            }
        }
    }

    // Select Image from Gallery
    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    // Load Image from Gallery
    private Bitmap loadImage(Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
        } else {
            return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        }
    }

    // Add Image to Container and Use Latest Image for Recognition
    private void addImageToContainer(Bitmap image) {
        if (image != null) {
            imageContainer.add(image);
            imageViewPreview.setImageBitmap(image); // Show last selected image
            recognizeTextFromLatestImage();
        } else {
            showToast("Error: Image is null");
        }
    }

    // Recognize Text Using the Latest Image in the Container
    private void recognizeTextFromLatestImage() {
        if (imageContainer.isEmpty()) {
            showToast("No images available for recognition");
            return;
        }

        Bitmap latestImage = imageContainer.get(imageContainer.size() - 1); // Get the last image
        InputImage inputImage = InputImage.fromBitmap(latestImage, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(inputImage)
                .addOnSuccessListener(text -> textViewResult.setText(text.getText().isEmpty() ? "No text found" : text.getText()))
                .addOnFailureListener(e -> {
                    showToast("Text recognition failed");
                    e.printStackTrace();
                });
    }

    // Save Recognized Text
    private void saveText() {
        String recognizedText = textViewResult.getText().toString().trim();
        if (!recognizedText.isEmpty()) {
            // Copy to clipboard
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Recognized Text", recognizedText);
            clipboard.setPrimaryClip(clip);

            showToast("Text copied to clipboard!");
        } else {
            showToast("No text to copy!");
        }
    }

    // Retry Image Capture
    private void retryImageCapture() {
        imageContainer.clear();
        imageViewPreview.setImageDrawable(null);
        textViewResult.setText("Recognized text will appear here");
    }

    // Translate Recognized Text (Placeholder)
    private void translateText() {
        String textToTranslate = textViewResult.getText().toString().trim();
        if (!textToTranslate.isEmpty()) {
            // Construct Google Translate URL
            String url = "https://translate.google.com/?sl=auto&tl=en&text=" + Uri.encode(textToTranslate);

            // Open Google Translate in browser
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } else {
            showToast("No text to translate!");
        }
    }


    // Show Toast Messages
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
