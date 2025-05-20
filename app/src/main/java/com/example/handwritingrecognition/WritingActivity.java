package com.example.handwritingrecognition;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.vision.digitalink.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions;
import com.google.mlkit.vision.digitalink.Ink;
import com.google.mlkit.vision.digitalink.RecognitionCandidate;
import com.google.mlkit.vision.digitalink.RecognitionResult;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;

import java.util.List;

public class WritingActivity extends AppCompatActivity {
    private static final String TAG = "WritingActivity";
    private DrawView drawView;
    private TextView reflectionText;
    private StringBuilder fullText = new StringBuilder();
    private DigitalInkRecognizer recognizer;
    private boolean isEraserEnabled = false;
    private ImageButton btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        drawView = findViewById(R.id.drawView);
        reflectionText = findViewById(R.id.reflectionText);

        MaterialButton clearButton = findViewById(R.id.clearButton);
        MaterialButton eraserButton = findViewById(R.id.eraserButton);
        MaterialButton continueButton = findViewById(R.id.continueButton);
        MaterialButton predictButton = findViewById(R.id.predictButton);
        MaterialButton saveButton = findViewById(R.id.saveButton);
        MaterialButton searchButton = findViewById(R.id.searchButton);
        MaterialButton translateButton = findViewById(R.id.translateButton);
        btn = findViewById(R.id.backButton);

        initializeRecognizer();

        btn.setOnClickListener(v -> onBackPressed());
        clearButton.setOnClickListener(v -> clearEverything());
        eraserButton.setOnClickListener(v -> toggleEraser());
        predictButton.setOnClickListener(v -> predictHandwriting());
        continueButton.setOnClickListener(v -> clearCanvasOnly());
        saveButton.setOnClickListener(v -> saveToClipboard());
        searchButton.setOnClickListener(v -> searchTextOnline());
        translateButton.setOnClickListener(v -> translateText());
    }

    private void initializeRecognizer() {
        try {
            DigitalInkRecognitionModelIdentifier modelIdentifier =
                    DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US");

            if (modelIdentifier != null) {
                DigitalInkRecognitionModel model =
                        DigitalInkRecognitionModel.builder(modelIdentifier).build();

                DigitalInkRecognizerOptions options =
                        DigitalInkRecognizerOptions.builder(model).build();

                recognizer = DigitalInkRecognition.getClient(options); // Correct usage

                // Download the model before using it
                RemoteModelManager.getInstance()
                        .download(model, new DownloadConditions.Builder().build())
                        .addOnSuccessListener(unused -> Log.d("DEBUG", "Model downloaded successfully."))
                        .addOnFailureListener(e -> Log.e("ERROR", "Model download failed", e)); // Corrected Log.e() usage

            } else {
                Toast.makeText(this, "Model identifier is null!", Toast.LENGTH_SHORT).show();
                Log.e("ERROR", "Model identifier is null");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing recognizer", Toast.LENGTH_SHORT).show();
            Log.e("ERROR", "Exception in initializing recognizer: " + e.getMessage(), e); // Corrected Log.e()
        }
    }


    private void predictHandwriting() {
        if (recognizer == null) {
            showToast("Recognizer not initialized!");
            Log.e(TAG, "Recognizer is null");
            return;
        }

        Ink ink = drawView.getInk();
        if (ink == null || ink.getStrokes().isEmpty()) {
            showToast("Draw something first!");
            Log.w(TAG, "No strokes in ink");
            return;
        }

        recognizer.recognize(ink)
                .addOnSuccessListener(result -> {
                    List<RecognitionCandidate> candidates = result.getCandidates();
                    if (candidates.isEmpty()) {
                        showToast("No text recognized!");
                        Log.w(TAG, "No candidates recognized");
                        return;
                    }

                    String recognizedText = candidates.get(0).getText();
                    Log.d(TAG, "Recognized text: " + recognizedText);

                    fullText.append(recognizedText).append(" ");
                    reflectionText.setText(fullText.toString());

                    drawView.clearCanvas();
                })
                .addOnFailureListener(e -> {
                    showToast("Recognition failed!");
                    Log.e(TAG, "Recognition failed", e);
                });
    }

    private void toggleEraser() {
        isEraserEnabled = !isEraserEnabled;
        drawView.enableEraser(isEraserEnabled);
        showToast(isEraserEnabled ? "Eraser Enabled" : "Eraser Disabled");
    }

    private void clearEverything() {
        drawView.clearCanvas();
        fullText.setLength(0);
        reflectionText.setText("");
        showToast("Canvas and text cleared");
    }

    private void clearCanvasOnly() {
        drawView.clearCanvas();
        showToast("Canvas cleared, text remains");
    }

    private void saveToClipboard() {
        String textToSave = reflectionText.getText().toString();
        if (textToSave.isEmpty()) {
            showToast("No text to save!");
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Recognized Text", textToSave);
        clipboard.setPrimaryClip(clip);
        showToast("Saved to clipboard");
    }

    private void searchTextOnline() {
        String query = reflectionText.getText().toString();
        if (query.isEmpty()) {
            showToast("No text to search");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra("query", query);
        startActivity(intent);
    }

    private void translateText() {
        String text = reflectionText.getText().toString();
        if (text.isEmpty()) {
            showToast("No text to translate!");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://translate.google.com/?sl=auto&tl=es&text=" + Uri.encode(text)));
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (fullText.length() > 0) {
            clearEverything();
        } else {
            super.onBackPressed();
        }
    }
}
