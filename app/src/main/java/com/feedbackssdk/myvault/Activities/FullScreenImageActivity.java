package com.feedbackssdk.myvault.Activities;

import static com.feedbackssdk.myvault.FileUtils.FileHelper.readFileAsString;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.feedbackssdk.myvault.EncryptionUtils.EncryptionUtil;
import com.feedbackssdk.myvault.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class FullScreenImageActivity extends AppCompatActivity {

    public static final String EXTRA_FILE_PATH = "FILE_PATH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        // Set up toolbar with back button.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        ImageView imageView = findViewById(R.id.fullScreenImageView);

        // Retrieve the file path passed via the intent.
        Intent intent = getIntent();
        String filePath = intent.getStringExtra(EXTRA_FILE_PATH);
        if (filePath != null) {
            File imageFile = new File(filePath);
            if (imageFile.exists()) {
                try {
                    String encryptedData = readFileAsString(imageFile);
                    byte[] decryptedBytes = EncryptionUtil.decryptDataBytes(encryptedData);
                    if (decryptedBytes != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.length);
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        } else {
                            Toast.makeText(this, "Failed to decode image", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to decrypt image", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image file path provided", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle the Up button click.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
