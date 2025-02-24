package com.feedbackssdk.myvault.Activities;

import static com.feedbackssdk.myvault.FileUtils.FileHelper.readUriAsString;
import static com.feedbackssdk.myvault.Managers.NotificationsManager.showErrorNotification;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.feedbackssdk.myvault.EncryptionUtils.EncryptionUtil;
import com.feedbackssdk.myvault.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class OpenDecryptImageActivity extends AppCompatActivity {
    private ImageView fullScreenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_decrypt_image);

        fullScreenImageView = findViewById(R.id.fullScreenImageView);
        // Retrieve the file URI from the intent (this works for "open with" actions)
        Uri fileUri = getIntent().getData();
        if (fileUri == null) {
            Toast.makeText(this, "No file received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        showCodeDialog(fileUri);

    }
    private void showCodeDialog(Uri fileUri) {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.custom_code_dialog, null);

        // Retrieve views from the custom layout.
        final EditText input = dialogView.findViewById(R.id.dialogEditText);
        ImageButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        ImageButton btnSave = dialogView.findViewById(R.id.btn_save);

        // Build the dialog using a custom theme (if defined)
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setView(dialogView);
        builder.setCancelable(false); // Prevent dismissal by tapping outside
        final AlertDialog dialog = builder.create();
        dialog.show();

        // Set the Cancel button behavior
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        // Set the Save button behavior
        btnSave.setOnClickListener(v -> {
            String code = input.getText().toString().trim();
            if (code.isEmpty()) {
                showErrorNotification(OpenDecryptImageActivity.this,"Subject cannot be empty.");
                dialog.dismiss();
                return;
            }
            decodeImage(fileUri,code);
            dialog.dismiss();
        });
    }

    private void decodeImage(Uri fileUri, String code) {
       try {
            String encryptedData = readUriAsString(this,fileUri);
            // Use a decryption method that takes the encrypted data and the code.
            // For example, decryptDataForShareWithCode returns a byte[] of the decrypted image.
            byte[] decryptedBytes = EncryptionUtil.decryptDataForShareWithCode(encryptedData, code);
            if (decryptedBytes != null) {
                // Convert decrypted bytes to a Bitmap.
                final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.length);
                if (bitmap != null) {
                    runOnUiThread(() -> {
                        fullScreenImageView.setImageBitmap(bitmap);
                        fullScreenImageView.setVisibility(View.VISIBLE);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(OpenDecryptImageActivity.this, "Error decoding image", Toast.LENGTH_SHORT).show();
                        new android.os.Handler(android.os.Looper.getMainLooper())
                                .postDelayed(() -> finish(), 1500);
                    });
                }
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(OpenDecryptImageActivity.this, "Incorrect code or file expired", Toast.LENGTH_SHORT).show();
                    new android.os.Handler(android.os.Looper.getMainLooper())
                            .postDelayed(() -> finish(), 1500);
                });
            }
       } catch (Exception e) {
           e.printStackTrace();
           runOnUiThread(() -> {
               Toast.makeText(OpenDecryptImageActivity.this, "Error processing file", Toast.LENGTH_SHORT).show();
               new android.os.Handler(android.os.Looper.getMainLooper())
                       .postDelayed(() -> finish(), 1500);
           });
       }
    }
}


