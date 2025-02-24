package com.feedbackssdk.myvault.Fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.feedbackssdk.myvault.EncryptionUtils.EncryptionUtil;
import com.feedbackssdk.myvault.R;
import com.feedbackssdk.myvault.SQLite.VaultDbHelper;

public class EntriesFragment extends Fragment {

    public EntriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View root = inflater.inflate(R.layout.fragment_entries, container, false);
        displayVaultEntries(root);
        return root;
    }

    private void displayVaultEntries(View root) {
        LinearLayout container = root.findViewById(R.id.entriesContainer);
        container.removeAllViews();

        VaultDbHelper dbHelper = new VaultDbHelper(requireContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(VaultDbHelper.TABLE_NAME, null, null, null, null, null, null);
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        while (cursor.moveToNext()) {
            // Get the unique id of the entry.
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(VaultDbHelper.COLUMN_ID));
            String subject = cursor.getString(cursor.getColumnIndexOrThrow(VaultDbHelper.COLUMN_SUBJECT));
            String encryptedValue = cursor.getString(cursor.getColumnIndexOrThrow(VaultDbHelper.COLUMN_VALUE));

            String decryptedValue = EncryptionUtil.decryptData(encryptedValue);
            if (decryptedValue == null) {
                decryptedValue = "Decryption error";
            }

            // Choose layout based on language (if needed)
            int layoutResId = startsWithHebrew(subject) ? R.layout.item_vault_entry_rtl : R.layout.item_vault_entry;
            View rowView = inflater.inflate(layoutResId, container, false);

            TextView tvSubject = rowView.findViewById(R.id.tvSubject);
            TextView tvValue = rowView.findViewById(R.id.tvValue);
            ImageButton btnCancel = rowView.findViewById(R.id.btnCancel);

            tvSubject.setText(subject);
            tvValue.setText(decryptedValue);

            // Enable copy-to-clipboard on the value.
            final String finalDecryptedValue = decryptedValue;
            tvValue.setOnClickListener(v -> {
                ClipboardManager clipboard = ContextCompat.getSystemService(requireContext(), ClipboardManager.class);
                ClipData clip = ClipData.newPlainText("Copied Value", finalDecryptedValue);
                if(clipboard==null)return;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), "Value copied", Toast.LENGTH_SHORT).show();
            });

            // Set the cancel button to delete the row from SQLite and remove it from the UI.
            btnCancel.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete \"" + subject + "\"? This action cannot be undone.")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Open writable database and delete the entry with the given ID.
                            SQLiteDatabase writableDb = dbHelper.getWritableDatabase();
                            int rowsDeleted = writableDb.delete(VaultDbHelper.TABLE_NAME,
                                    VaultDbHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
                            writableDb.close();
                            if (rowsDeleted > 0) {
                                container.removeView(rowView);
                                Toast.makeText(requireContext(), "Entry deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Error deleting entry", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            });


            container.addView(rowView);
        }
        cursor.close();
        db.close();
    }

    private boolean startsWithHebrew(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        char firstChar = text.charAt(0);
        // Hebrew letters range from U+05D0 (א) to U+05EA (ת)
        return firstChar >= '\u05D0' && firstChar <= '\u05EA';
    }

}
