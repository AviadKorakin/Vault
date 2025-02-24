package com.feedbackssdk.myvault.Activities;

import static com.feedbackssdk.myvault.Managers.NotificationsManager.showErrorNotification;
import static com.feedbackssdk.myvault.Managers.NotificationsManager.showSuccessNotification;
import static com.feedbackssdk.myvault.SQLite.VaultDbHelper.insertVaultEntry;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.feedbackssdk.myvault.EncryptionUtils.EncryptionUtil;
import com.feedbackssdk.myvault.R;
import com.feedbackssdk.myvault.SQLite.VaultDbHelper;
public class ProcessTextActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 101;

    private CharSequence selectedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_text);

        // Request POST_NOTIFICATIONS permission on Android 13+ if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }

        Intent intent = getIntent();
        if (Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
            selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            if (selectedText != null) {
                // Instead of auto-saving, show a custom dialog to ask for a subject.
                showSubjectDialog(selectedText);
            }
        } else {
            finish();
        }
    }

    // Shows a custom dialog asking the user to enter a subject using our new layout.
    private void showSubjectDialog(final CharSequence selectedText) {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.custom_subject_dialog, null);

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
            String subject = input.getText().toString().trim();
            if (subject.isEmpty()) {
                showErrorNotification(ProcessTextActivity.this,"Subject cannot be empty.");
                dialog.dismiss();
                finish();
                return;
            }
            processAndSave(subject, selectedText);
            dialog.dismiss();
        });
    }


    // Combines the subject and text, encrypts them, and saves the result.
    private void processAndSave(String subject, CharSequence text) {
        VaultDbHelper dbHelper = new VaultDbHelper(this);
        boolean savedInDb = insertVaultEntry(subject, encryptData(text.toString()),dbHelper);

        if (savedInDb) {
            showSuccessNotification(this,"Vault entry saved successfully with subject: " + subject);
        } else {
            showErrorNotification(this,"Error saving vault entry with subject: " + subject);
        }
        finish();
    }

    private String encryptData(String data) {
        return EncryptionUtil.encryptData(data);
    }



    // Handle the POST_NOTIFICATIONS permission request result.
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
}