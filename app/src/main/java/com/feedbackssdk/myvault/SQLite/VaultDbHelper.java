package com.feedbackssdk.myvault.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VaultDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "vault.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "vault_entries";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SUBJECT = "subject";
    public static final String COLUMN_VALUE = "value";

    public VaultDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SUBJECT + " TEXT NOT NULL, "
                + COLUMN_VALUE + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For simplicity, drop the old table and create a new one.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public static boolean insertVaultEntry(String subject, String value, VaultDbHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(VaultDbHelper.COLUMN_SUBJECT, subject);
        values.put(VaultDbHelper.COLUMN_VALUE, value);

        long newRowId = db.insert(VaultDbHelper.TABLE_NAME, null, values);
        db.close();
        return newRowId != -1;
    }
}
