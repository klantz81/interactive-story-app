package net.keithlantz.interactivestory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by keith on 12/9/17.
 */

public class ChoicesDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ChoicesContract.ChoicesEntry.TABLE_NAME + " (" +
                    ChoicesContract.ChoicesEntry._ID + " INTEGER PRIMARY KEY," +
                    ChoicesContract.ChoicesEntry.COLUMN_NAME_CHOICE_ID + " TEXT," +
                    ChoicesContract.ChoicesEntry.COLUMN_NAME_CHOICE_TEXT + " TEXT," +
                    ChoicesContract.ChoicesEntry.COLUMN_NAME_TIMESTAMP + " DATETIME)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ChoicesContract.ChoicesEntry.TABLE_NAME;

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "choices.db";

    public ChoicesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void truncate(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
