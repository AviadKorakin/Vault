package com.feedbackssdk.myvault.EncryptionUtils;

public class EncryptionResult {
    private final String encryptedData; // Base64 encoded encrypted content
    private final String key;           // Base64 encoded key

    public EncryptionResult(String encryptedData, String key) {
        this.encryptedData = encryptedData;
        this.key = key;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public String getKey() {
        return key;
    }
}
