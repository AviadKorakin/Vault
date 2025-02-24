package com.feedbackssdk.myvault.Activities;

import static com.feedbackssdk.myvault.FileUtils.FileHelper.readUriBytes;
import static com.feedbackssdk.myvault.Managers.NotificationsManager.showErrorNotification;
import static com.feedbackssdk.myvault.Managers.NotificationsManager.showSuccessNotification;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.feedbackssdk.myvault.EncryptionUtils.EncryptionUtil;
import com.feedbackssdk.myvault.R;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class ProcessImageActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 101;
    private ProgressBar progressBar;
    private ImageView successIcon;
    private static final String IMAGE_FILE_NAME = "secure_image.enc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_image);

        progressBar = findViewById(R.id.progressBar);
        successIcon = findViewById(R.id.successIcon);

        // Initially, show the progress indicator and hide the success icon.
        progressBar.setVisibility(View.VISIBLE);
        successIcon.setVisibility(View.GONE);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if ((Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action))
                && type != null && type.startsWith("image/")) {
            if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (imageUris != null && !imageUris.isEmpty()) {
                    processMultipleImages(imageUris);
                } else {
                    Toast.makeText(this, "No images received", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                // Single image case.
                Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    processImage(imageUri);
                } else {
                    Toast.makeText(this, "No image received", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        } else {
            Toast.makeText(this, "Invalid intent", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void processMultipleImages(ArrayList<Uri> imageUris) {
        new Thread(() -> {
            boolean allSaved = true;
            for (Uri uri : imageUris) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                    }
                    inputStream.close();
                    byte[] imageBytes = baos.toByteArray();
                    String encryptedData = EncryptionUtil.encryptDataBytes(imageBytes);
                    String filename = getFileName(uri);
                    boolean saved = saveEncryptedImage(filename, encryptedData);
                    if (!saved) {
                        allSaved = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    allSaved = false;
                }
            }
            boolean finalAllSaved = allSaved;
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (finalAllSaved) {
                    showSuccessNotification(ProcessImageActivity.this, "All images saved to Vault");
                    successIcon.setVisibility(View.VISIBLE);
                } else {
                    showErrorNotification(ProcessImageActivity.this, "Error saving one or more images");
                }
            });
            new Handler(getMainLooper()).postDelayed(this::finish, 1500);
        }).start();
    }

    private void processImage(Uri imageUri) {
        new Thread(() -> {
            try {
                byte[] imageBytes = readUriBytes(this,imageUri);

                // Encrypt the image bytes.
                String encryptedData = EncryptionUtil.encryptDataBytes(imageBytes);
                String filename = getFileName(imageUri);
                // Save the encrypted data.
                boolean saved = saveEncryptedImage(filename,encryptedData);

                // Update UI on main thread.
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (saved) {
                        // Instead of Toast, use a notification.
                        showSuccessNotification(this,"Image saved to Vault");
                        // Optionally, display your success icon.
                        successIcon.setVisibility(View.VISIBLE);
                    } else {
                        showErrorNotification(this,"Error saving image");
                    }
                });

                // Delay and finish.
                new Handler(getMainLooper()).postDelayed(this::finish, 1500);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    showErrorNotification(this,"Error processing image");
                });
                finish();
            }
        }).start();

    }

    private boolean saveEncryptedImage(String filename, String encryptedData) {
        if (filename != null && filename.contains(".")) {
            filename = "secure_" + filename.substring(0, filename.lastIndexOf('.')) + ".enc";
        } else {
            filename = "secure_default.enc";
        }
        try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
            fos.write(encryptedData.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                showErrorNotification(this,"Notification permission denied. Notifications may not be displayed.");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex("_display_name");
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
}
