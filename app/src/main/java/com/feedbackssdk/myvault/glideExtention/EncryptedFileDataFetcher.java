package com.feedbackssdk.myvault.glideExtention;

import android.os.Build;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.feedbackssdk.myvault.EncryptionUtils.EncryptionUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EncryptedFileDataFetcher implements DataFetcher<InputStream> {
    private final EncryptedFileModel model;

    public EncryptedFileDataFetcher(EncryptedFileModel model) {
        this.model = model;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        try {
            FileInputStream fis = new FileInputStream(model.getFile());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            fis.close();
            String encryptedData = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                encryptedData = baos.toString(StandardCharsets.UTF_8);
            }
            byte[] decryptedBytes = EncryptionUtil.decryptDataBytes(encryptedData);
            if (decryptedBytes != null) {
                InputStream resultStream = new ByteArrayInputStream(decryptedBytes);
                callback.onDataReady(resultStream);
            } else {
                callback.onLoadFailed(new Exception("Decryption returned null"));
            }
        } catch (Exception e) {
            callback.onLoadFailed(e);
        }
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}
