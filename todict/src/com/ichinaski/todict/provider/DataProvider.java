package com.ichinaski.todict.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.ichinaski.todict.provider.DataProviderContract.Dict;
import com.ichinaski.todict.provider.DataProviderContract.DictColumns;
import com.ichinaski.todict.provider.DataProviderContract.Tables;
import com.ichinaski.todict.provider.DataProviderContract.Translation;
import com.ichinaski.todict.provider.DataProviderContract.TranslationColumns;

public class DataProvider extends ContentProvider{
    private static final String TAG = DataProvider.class.getSimpleName();
    
    private static final int DICT = 1;
    private static final int TRANSLATION = 2;
    private static final int SEARCH_SUGGEST = 3;
    
    // Defines a helper object that matches content URIs to table-specific parameters
    private static final UriMatcher sUriMatcher = createUriMatcher();
    
    private SQLiteOpenHelper mHelper;
    
    private static UriMatcher createUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataProviderContract.AUTHORITY;
        
        matcher.addURI(authority, Tables.DICTIONARY, DICT);
        matcher.addURI(authority, Tables.TRANSLATION, TRANSLATION);
        matcher.addURI(authority, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        
        return matcher;
    }
    
    // Closes the SQLite database helper class, to avoid memory leaks
    public void close() {
        mHelper.close();
    }

    private class DataProviderHelper extends SQLiteOpenHelper {
        
        public DataProviderHelper(Context context) {
            super(context, 
                    DataProviderContract.DATABASE_NAME, 
                    null,
                    DataProviderContract.DATABASE_VERSION);
        }
        
        private void dropTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + Tables.DICTIONARY);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.TRANSLATION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + DataProviderContract.Tables.DICTIONARY + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
                    + DictColumns.LANG_1 + " TEXT NOT NULL," 
                    + DictColumns.LANG_2 + " TEXT NOT NULL)");
            
            db.execSQL("CREATE TABLE " + DataProviderContract.Tables.TRANSLATION + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
                    + TranslationColumns.DICT_ID + " INTEGER NOT NULL "
                    + "REFERENCES " + Tables.DICTIONARY + "(" + DictColumns._ID + ")"
                    + " ON UPDATE CASCADE ON DELETE CASCADE,"
                    + TranslationColumns.WORD + " TEXT NOT NULL," 
                    + TranslationColumns.TRANSLATION + " TEXT NOT NULL)");
            
            db.execSQL("CREATE INDEX translation_word_idx ON "
                    + Tables.TRANSLATION + "(" + TranslationColumns.WORD + ")" );
            
            /*
	         // Using the "porter" tokenizer for simple stemming, so that
            // "frustration" matches "frustrated."
            db.execSQL("CREATE VIRTUAL TABLE " + Tables.TRANSLATION + " USING fts3("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TranslationColumns.DICT_ID + " INTEGER NOT NULL "
                    + "REFERENCES " + Tables.DICTIONARY + "(" + DictColumns._ID + ")"
                    + " ON UPDATE CASCADE ON DELETE CASCADE,"
                    + TranslationColumns.WORD + " TEXT NOT NULL," 
                    + TranslationColumns.TRANSLATION + " TEXT NOT NULL,"
                    + "UNIQUE (" + TranslationColumns.WORD + ") ON CONFLICT REPLACE,"
                    + "tokenize=porter)");
                    */

            // TODO: Create indexes
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "onUpgrade() - Old version: " + oldVersion + ".New Version: " + newVersion);
            dropTables(db);
            onCreate(db);
        }

    }

    @Override
    public boolean onCreate() {
        mHelper = new DataProviderHelper(getContext());
        return true;
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
	        String sortOrder) {
        // Do the query against a read-only version of the database
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = null;
        // Decodes the content URI and maps it to a code
        switch (sUriMatcher.match(uri)) {
            case DICT:
                cursor = db.query(
                    Tables.DICTIONARY, projection, selection, selectionArgs, 
                    null, null, sortOrder);
                break;
            case TRANSLATION:
                cursor = db.query(
                    Tables.TRANSLATION, projection, selection, selectionArgs, 
                    null, null, sortOrder);
                break;
            case SEARCH_SUGGEST:
                // Suggestions search
	            // Adjust incoming query to become SQL text match
                selectionArgs[0] = selectionArgs[0] + "%";
                projection = new String[] {
                        BaseColumns._ID,
                        BaseColumns._ID 
                        + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                        TranslationColumns.WORD 
                        + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1,
                        TranslationColumns.TRANSLATION 
                        + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2};
                cursor = db.query(
                    DataProviderContract.Tables.TRANSLATION, projection,
                    selection, selectionArgs, null, null, sortOrder);
                break;
        }
        
        if (cursor != null) {
            // Sets the ContentResolver to watch this content URI for data changes
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case DICT:
                return Dict.CONTENT_TYPE;
            case TRANSLATION:
                return Translation.CONTENT_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        long id = -1;
        switch (sUriMatcher.match(uri)) {
            case DICT:
                id = db.insertOrThrow(Tables.DICTIONARY, null, values);
                break;
            case TRANSLATION:
                id = db.insertOrThrow(Tables.TRANSLATION, null, values);
                break;
        }
        
        if (id != -1) {
	        // If the insert succeeded, notify a change and return the new row's content URI.
	        getContext().getContentResolver().notifyChange(uri, null);
	        return Uri.withAppendedPath(uri, Long.toString(id));
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int rows = 0;
        switch (sUriMatcher.match(uri)) {
            case DICT:
                rows = db.update(Tables.DICTIONARY, values, selection, selectionArgs);
                break;
            case TRANSLATION:
                rows = db.update(Tables.TRANSLATION, values, selection, selectionArgs);
                break;
        }
        
        if (rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int rows = 0;
        switch (sUriMatcher.match(uri)) {
            case DICT:
                rows = db.delete(Tables.DICTIONARY, selection, selectionArgs);
                break;
            case TRANSLATION:
                rows = db.delete(Tables.TRANSLATION, selection, selectionArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

}