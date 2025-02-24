package com.feedbackssdk.myvault.glideExtention;

import java.io.File;

public class EncryptedFileModel {
    private final File file;

    public EncryptedFileModel(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
