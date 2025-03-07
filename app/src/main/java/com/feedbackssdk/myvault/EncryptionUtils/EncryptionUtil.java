
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
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
/**
 * EncryptionUtil provides utility methods for encrypting and decrypting data using AES/GCM/NoPadding.
 *
 * <p><b>Encryption Process:</b></p>
 * <ol>
 *   <li>Generate (or retrieve) a SecretKey stored in the Android Keystore (for persistent keys) or generate an ephemeral key for sharing.</li>
 *   <li>Create a Cipher instance with "AES/GCM/NoPadding".</li>
 *   <li>Initialize the Cipher in ENCRYPT_MODE. A random IV (Initialization Vector) is automatically generated (recommended length is 12 bytes for GCM).</li>
 *   <li>Encrypt the plaintext data using the cipher. In GCM mode, the authentication tag is appended to the ciphertext automatically.</li>
 *   <li>Prepend the IV to the ciphertext (and header if applicable) so that it can be used during decryption.</li>
 *   <li>Encode the combined IV and ciphertext using Base64 for storage or transmission.</li>
 * </ol>
 *
 * <p><b>Decryption Process:</b></p>
 * <ol>
 *   <li>Decode the Base64-encoded input to retrieve the combined IV and ciphertext.</li>
 *   <li>Extract the IV (first 12 bytes for GCM) and the ciphertext.</li>
 *   <li>Create a GCMParameterSpec with a 128-bit authentication tag using the extracted IV.</li>
 *   <li>Initialize the Cipher in DECRYPT_MODE with the SecretKey and GCMParameterSpec.</li>
 *   <li>Decrypt the ciphertext and verify the authentication tag to ensure data integrity, recovering the original plaintext.</li>
 * </ol>
 *
 * <p><b>Security Considerations:</b></p>
 * <p>
 *   AES/GCM/NoPadding is an authenticated encryption mode, providing both confidentiality and integrity.
 *   By using the Android Keystore for key storage, keys are kept secure. This implementation is robust and
 *   suitable for many applications requiring secure encryption and decryption.
 * </p>
 */
public class EncryptionUtil {
    // Constant alias used to identify the secret key in the Android Keystore.
    private static final String KEY_ALIAS = "MyVaultKey";
    // Constant representing the Android Keystore type.
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    // Recommended IV length for GCM mode in bytes (12 bytes=96BITS is standard).
    private static final int GCM_IV_LENGTH = 12;
    // Authentication tag length for GCM mode in bits.
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * Generates (or retrieves if already exists) a SecretKey stored in the Android Keystore.
     * The key is configured to use AES/GCM/NoPadding for encryption and decryption.
     *
     * @return The SecretKey from the Android Keystore.
     */
    public static SecretKey getSecretKey() {
        try {
            // Obtain an instance of the Android Keystore.
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            // Check if the key already exists in the keystore.
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                // Initialize a KeyGenerator for AES using the Android Keystore.
                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
                // Define key generation parameters: use GCM mode and no padding.
                KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        // Additional parameters (e.g., user authentication) can be set here.
                        .build();
                keyGenerator.init(keyGenParameterSpec);
                // Generate and return the new SecretKey.
                return keyGenerator.generateKey();
            } else {
                // Retrieve the key if it already exists.
                return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            }
        } catch (Exception e) {
            // Log any exceptions.
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Encrypts the provided data string using AES/GCM/NoPadding.
     * The returned string is Base64-encoded, with the IV prepended to the ciphertext.
     *
     * @param data The plaintext string to encrypt.
     * @return A Base64-encoded string containing the IV and ciphertext.
     */
    public static String encryptData(String data) {
        try {
            SecretKey secretKey = getSecretKey();
            if (secretKey == null) {
                throw new RuntimeException("SecretKey not available");
            }
            // Create Cipher for AES/GCM/NoPadding.
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            // Initialize cipher in ENCRYPT_MODE; a random IV is generated.
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();  // IV is typically 12 bytes.
            // Encrypt the UTF-8 bytes of the input string.
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to the ciphertext.
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            // Return Base64-encoded combined IV and ciphertext.
            return Base64.encodeToString(combined, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decrypts the provided Base64-encoded string (with IV prepended) using AES/GCM/NoPadding.
     *
     * @param base64EncryptedData The Base64-encoded string containing the IV and ciphertext.
     * @return The decrypted plaintext string.
     */
    public static String decryptData(String base64EncryptedData) {
        try {
            SecretKey secretKey = getSecretKey();
            if (secretKey == null) {
                throw new RuntimeException("SecretKey not available");
            }
            // Decode the Base64 input.
            byte[] combined = Base64.decode(base64EncryptedData, Base64.NO_WRAP);
            // Extract IV (first GCM_IV_LENGTH bytes).
            byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
            // Extract ciphertext (remainder of the bytes).
            byte[] ciphertext = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);

            // Create Cipher for AES/GCM/NoPadding.
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            // Initialize with a GCMParameterSpec using the IV and tag length.
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            // Decrypt and return the plaintext as a UTF-8 string.
            byte[] decryptedBytes = cipher.doFinal(ciphertext);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Encrypts the provided byte array using AES/GCM/NoPadding.
     * The returned string is Base64-encoded, with the IV prepended to the ciphertext.
     *
     * @param data The plaintext byte array to encrypt.
     * @return A Base64-encoded string containing the IV and ciphertext.
     */
    public static String encryptDataBytes(byte[] data) {
        try {
            SecretKey secretKey = getSecretKey();
            if (secretKey == null) {
                throw new RuntimeException("SecretKey not available");
            }
            // Create Cipher for AES/GCM/NoPadding.
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();
            byte[] encryptedBytes = cipher.doFinal(data);
            // Combine IV and encrypted data.
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
            return Base64.encodeToString(combined, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decrypts the provided Base64-encoded string (with IV prepended) using AES/GCM/NoPadding,
     * and returns the decrypted byte array.
     *
     * @param base64EncryptedData The Base64-encoded string containing the IV and ciphertext.
     * @return The decrypted byte array.
     */
    public static byte[] decryptDataBytes(String base64EncryptedData) {
        try {
            SecretKey secretKey = getSecretKey();
            if (secretKey == null) {
                throw new RuntimeException("SecretKey not available");
            }
            byte[] combined = Base64.decode(base64EncryptedData, Base64.NO_WRAP);
            byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Encrypts the provided data for sharing using an ephemeral AES key with AES/GCM/NoPadding.
     * The current time is quantized to a 30-minute window and used as AAD.
     * The output contains the IV and ciphertext, Base64-encoded.
     *
     * @param data The plaintext byte array to encrypt.
     * @return An EncryptionResult containing the Base64-encoded combined data and the ephemeral key.
     */
    public static EncryptionResult encryptDataForShare(byte[] data) {
        try {
            // 1) Generate a new ephemeral AES key.
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256); // or 128 if 256-bit is not supported.
            SecretKey secretKey = keyGenerator.generateKey();

            // 2) Initialize Cipher for AES/GCM/NoPadding with the ephemeral key.
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();

            // 3) Quantize current time to the nearest 15 minutes.
            long currentTime = System.currentTimeMillis();
            long quantizedTime = (currentTime / 900_000L) * 900_000L; // 1_800_000 ms = 15 minutes
            String aadStr = String.valueOf(quantizedTime);
            byte[] aadBytes = aadStr.getBytes(StandardCharsets.UTF_8);
            // Use the quantized time as AAD.
            cipher.updateAAD(aadBytes);

            // 4) Encrypt the data.
            byte[] encryptedBytes = cipher.doFinal(data);

            // 5) Combine IV and encrypted bytes (no header).
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            // 6) Base64-encode the combined data.
            String encryptedDataBase64 = Base64.encodeToString(combined, Base64.NO_WRAP);

            // 7) Base64-encode the ephemeral key.
            String keyBase64 = Base64.encodeToString(secretKey.getEncoded(), Base64.NO_WRAP);

            // 8) Return the encrypted data and key in an EncryptionResult.
            return new EncryptionResult(encryptedDataBase64, keyBase64);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Decrypts data that was encrypted for sharing using a provided Base64-encoded ephemeral key.
     * The method expects the encrypted data to contain the IV followed by the ciphertext.
     * It uses the current quantized time (30-minute window) as AAD, and decryption will only succeed
     * if the message is decrypted within the same 30-minute window.
     *
     * @param base64EncryptedData The Base64-encoded string containing the IV and ciphertext.
     * @param code The Base64-encoded ephemeral key used during encryption.
     * @return The decrypted byte array, or null if decryption fails.
     */
    public static byte[] decryptDataForShareWithCode(String base64EncryptedData, String code) {
        try {
            // 1) Decode the Base64 string to retrieve IV + ciphertext.
            byte[] combined = Base64.decode(base64EncryptedData, Base64.NO_WRAP);

            // 2) Extract the IV (GCM_IV_LENGTH bytes) and ciphertext.
            if (combined.length < GCM_IV_LENGTH) {
                throw new IllegalStateException("Insufficient data for IV.");
            }
            byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);

            // 3) Compute current quantized time to be used as AAD.
            long currentTime = System.currentTimeMillis();
            long quantizedTime = (currentTime / 900_000L) * 900_000L;
            String aadStr = String.valueOf(quantizedTime);
            byte[] aadBytes = aadStr.getBytes(StandardCharsets.UTF_8);

            // 4) Decode the Base64-encoded ephemeral key.
            byte[] keyBytes = Base64.decode(code, Base64.NO_WRAP);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

            // 5) Initialize Cipher for AES/GCM/NoPadding with the ephemeral key.
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            // 6) Use the quantized time as AAD.
            cipher.updateAAD(aadBytes);

            // 7) Decrypt and return the resulting byte array.
            return cipher.doFinal(cipherText);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
