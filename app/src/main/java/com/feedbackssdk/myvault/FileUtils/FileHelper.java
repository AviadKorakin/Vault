package com.feedbackssdk.myvault.FileUtils;


import android.content.Context;
import android.net.Uri;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileHelper {

    /**
     * Reads the contents of a file and returns it as a String using UTF-8 encoding.
     *
     * @param file The file to read.
     * @return The file's contents as a String.
     * @throws Exception If an error occurs during reading.
     */
    public static String readFileAsString(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return baos.toString(StandardCharsets.UTF_8);
            } else {
                return baos.toString("UTF-8");
            }
        }
    }
    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }
    public static byte[] readUriBytes(Context context, Uri uri) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("Unable to open InputStream for URI: " + uri);
            }
            return readAllBytes(inputStream);
        }
    }
    public static String readUriAsString(Context context, Uri uri) throws IOException {
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            // Convert the file's bytes into a UTF-8 string.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return baos.toString(StandardCharsets.UTF_8);
            } else {
                return baos.toString("UTF-8");
            }
        }
    }
}
