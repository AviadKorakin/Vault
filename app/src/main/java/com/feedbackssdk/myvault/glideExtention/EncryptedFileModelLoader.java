package com.feedbackssdk.myvault.glideExtention;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

public class EncryptedFileModelLoader implements ModelLoader<EncryptedFileModel, InputStream> {

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull EncryptedFileModel model, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model.getFile()), new EncryptedFileDataFetcher(model));
    }

    @Override
    public boolean handles(@NonNull EncryptedFileModel model) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<EncryptedFileModel, InputStream> {
        @NonNull
        @Override
        public ModelLoader<EncryptedFileModel, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new EncryptedFileModelLoader();
        }

        @Override
        public void teardown() {
            // Nothing to teardown.
        }
    }
}
