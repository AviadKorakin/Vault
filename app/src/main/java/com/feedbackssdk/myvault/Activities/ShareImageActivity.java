package com.feedbackssdk.myvault.Activities;

import static com.feedbackssdk.myvault.EncryptionUtils.EncryptionUtil.encryptData;
import static com.feedbackssdk.myvault.FileUtils.FileHelper.readFileAsString;
import static com.feedbackssdk.myvault.SQLite.VaultDbHelper.insertVaultEntry;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.feedbackssdk.myvault.EncryptionUtils.EncryptionResult;
import com.feedbackssdk.myvault.EncryptionUtils.EncryptionUtil;
import com.feedbackssdk.myvault.R;
import com.feedbackssdk.myvault.SQLite.VaultDbHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class ShareImageActivity extends AppCompatActivity {

    public static final String EXTRA_FILE_PATH = "FILE_PATH";
    private File tempFile; // to hold the temporary encrypted file
    private String shareKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_image);

        // Set up toolbar with back button.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Enable the up button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        String originalFilePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (originalFilePath == null) {
            Toast.makeText(this, "No file provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            File originalFile = new File(originalFilePath);
            if (!originalFile.exists()) {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            String encryptedData = readFileAsString(originalFile);

            //decrypt the file from the system
            byte[] decryptedBytes = EncryptionUtil.decryptDataBytes(encryptedData);
            // then crypt it to send through 3rd party app
            EncryptionResult result = EncryptionUtil.encryptDataForShare(decryptedBytes);

            if (result == null) {
                Toast.makeText(this, "Error encrypting file", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            shareKey = result.getKey();

            // Display the key in the UI.
            TextView tvKey = findViewById(R.id.tvShareKey);
            tvKey.setText("Decryption Key (valid for 15 minutes):\n" + shareKey);

            // Save the new encrypted data to a temporary file in the cache directory.
            String filename = String.format("image_%06d.encshare", (int)(Math.random() * 1000000));
            tempFile = new File(getCacheDir(), filename);
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(result.getEncryptedData().getBytes(StandardCharsets.UTF_8));
            }
            VaultDbHelper dbHelper = new VaultDbHelper(this);
            boolean savedInDb = insertVaultEntry(filename, encryptData(shareKey),dbHelper);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing file", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        // Set up button listeners.
        ImageButton btnCopyKey = findViewById(R.id.btnCopyKey);
        btnCopyKey.setOnClickListener(v -> CopyToClipBoard());

        ImageButton btnShare = findViewById(R.id.btnShare);
        btnShare.setOnClickListener(v -> {
            // Create share intent with the temporary file's URI.
            Uri fileUri = FileProvider.getUriForFile(this, "com.feedbackssdk.myvault.fileprovider", tempFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/octet-stream");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share encrypted image"));
        });

        CardView cardKeyHolder = findViewById(R.id.cardKeyHolder);
        cardKeyHolder.setOnClickListener((View v) -> CopyToClipBoard());
    }
    private void CopyToClipBoard(){
        ClipboardManager clipboard = ContextCompat.getSystemService(this, ClipboardManager.class);
        ClipData clip = ClipData.newPlainText("Decryption Key", shareKey);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Key copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle Up navigation.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (tempFile != null && tempFile.exists()) {
            boolean res=tempFile.delete();
            if(res)
                Log.d("Deleted","The file is deleted");
            else
                Log.d("Deleted","The file is not deleted");
        }
        super.onDestroy();
    }
}
