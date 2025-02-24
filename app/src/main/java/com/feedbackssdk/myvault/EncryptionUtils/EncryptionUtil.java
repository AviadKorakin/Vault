package com.feedbackssdk.myvault.EncryptionUtils;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtil {
    private static final String KEY_ALIAS = "MyVaultKey";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";

    /**
     * Generates (or retrieves if already exists) a SecretKey stored in the Android Keystore.
     */
    public static SecretKey getSecretKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            // Check if the key already exists
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
                KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        // You can set additional parameters here (like user authentication)
                        .build();
                keyGenerator.init(keyGenParameterSpec);
                return keyGenerator.generateKey();
            } else {
                return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Encrypts the provided data string.
     * The returned string is Base64-encoded, with the IV prepended to the ciphertext.
     */
    public static String encryptData(String data) {
        try {
            SecretKey secretKey = getSecretKey();
            if (secretKey == null) {
                throw new RuntimeException("SecretKey not available");
            }
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            // Initialize the cipher for encryption; IV is generated automatically.
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Prepend the IV to the ciphertext
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            // Return the result as a Base64-encoded string
            return Base64.encodeToString(combined, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decrypts the provided Base64-encoded string (with IV prepended).
     */
    public static String decryptData(String base64EncryptedData) {
        try {
            SecretKey secretKey = getSecretKey();
            if (secretKey == null) {
                throw new RuntimeException("SecretKey not available");
            }
            byte[] combined = Base64.decode(base64EncryptedData, Base64.NO_WRAP);
            // Extract the IV (first 16 bytes for AES)
            byte[] iv = Arrays.copyOfRange(combined, 0, 16);
            byte[] ciphertext = Arrays.copyOfRange(combined, 16, combined.length);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] decryptedBytes = cipher.doFinal(ciphertext);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String encryptDataBytes(byte[] data) {
        try {
            SecretKey secretKey = getSecretKey();
            if (secretKey == null) {
                throw new RuntimeException("SecretKey not available");
            }
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();
            byte[] encryptedBytes = cipher.doFinal(data);
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
            return Base64.encodeToString(combined, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static byte[] decryptDataBytes(String base64EncryptedData) {
        try {
            SecretKey secretKey = getSecretKey();
            if (secretKey == null) {
                throw new RuntimeException("SecretKey not available");
            }
            byte[] combined = Base64.decode(base64EncryptedData, Base64.NO_WRAP);
            byte[] iv = Arrays.copyOfRange(combined, 0, 16);
            byte[] ciphertext = Arrays.copyOfRange(combined, 16, combined.length);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static EncryptionResult encryptDataForShare(byte[] data) {
        try {
            // 1) Generate a new AES key (ephemeral key for this encryption)
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256); // or 128 if your environment doesn't support 256
            SecretKey secretKey = keyGenerator.generateKey();

            // 2) Initialize Cipher (AES/CBC/PKCS7Padding)
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();

            // 3) Encrypt the data
            byte[] encryptedBytes = cipher.doFinal(data);

            // 4) Optionally, prepend a header with the current timestamp (in milliseconds) + newline
            long timestamp = System.currentTimeMillis();
            String header = timestamp + "\n";  // e.g., "1697690458086\n"
            byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

            // 5) Combine header, IV, and encrypted bytes into one array
            byte[] combined = new byte[headerBytes.length + iv.length + encryptedBytes.length];
            System.arraycopy(headerBytes, 0, combined, 0, headerBytes.length);
            System.arraycopy(iv, 0, combined, headerBytes.length, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, headerBytes.length + iv.length, encryptedBytes.length);

            // 6) Encode the combined data (header + IV + ciphertext) as Base64
            String encryptedDataBase64 = Base64.encodeToString(combined, Base64.NO_WRAP);

            // 7) Encode the secret key as Base64 so it can be shared separately
            String keyBase64 = Base64.encodeToString(secretKey.getEncoded(), Base64.NO_WRAP);

            // 8) Return the final encrypted data + the random key
            return new EncryptionResult(encryptedDataBase64, keyBase64);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decryptDataForShareWithCode(String base64EncryptedData, String code) {
        try {
            // 1) Decode the entire Base64 string (header + IV + ciphertext)
            byte[] combined = Base64.decode(base64EncryptedData, Base64.NO_WRAP);

            // 2) Locate the newline that marks the end of the header
            int headerEnd = -1;
            for (int i = 0; i < combined.length; i++) {
                if (combined[i] == '\n') {
                    headerEnd = i;
                    break;
                }
            }
            if (headerEnd == -1) {
                throw new IllegalStateException("Missing header delimiter (newline).");
            }

            // 3) Parse timestamp from the header
            String header = new String(combined, 0, headerEnd, StandardCharsets.UTF_8);
            long timestamp = Long.parseLong(header.trim());
            long currentTime = System.currentTimeMillis();

            // (Optional) check if more than 15 minutes have passed
            if (currentTime - timestamp > 900_000) { // 15 minutes
                return null;  // File expired
            }

            // 4) Decode the Base64-encoded key (provided as 'code' parameter)
            //    This keyBase64 must match 'EncryptionResult.getKey()' from encryption.
            byte[] keyBytes = Base64.decode(code, Base64.NO_WRAP);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

            // 5) Extract the IV (assume AES block size is 16 bytes)
            int ivLength = 16;
            int offset = headerEnd + 1;  // move offset to right after the newline
            if (combined.length < offset + ivLength) {
                throw new IllegalStateException("Insufficient data for IV.");
            }
            byte[] iv = new byte[ivLength];
            System.arraycopy(combined, offset, iv, 0, ivLength);
            offset += ivLength;

            // 6) The remainder is the ciphertext
            int cipherTextLength = combined.length - offset;
            byte[] cipherText = new byte[cipherTextLength];
            System.arraycopy(combined, offset, cipherText, 0, cipherTextLength);

            // 7) Initialize Cipher for decryption
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            // 8) Decrypt
            return cipher.doFinal(cipherText);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
