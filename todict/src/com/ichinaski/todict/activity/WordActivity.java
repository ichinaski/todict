package com.ichinaski.todict.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ichinaski.todict.R;
import com.ichinaski.todict.provider.DataProviderContract;
import com.ichinaski.todict.provider.DataProviderContract.Word;
import com.ichinaski.todict.provider.DataProviderContract.WordColumns;
import com.ichinaski.todict.util.Extra;

public class WordActivity extends SherlockFragmentActivity 
        implements LoaderCallbacks<Cursor>{
    public static final long ID_NONE = -1;
    
    private long mDictID;
    private long mWordID;
    
    private EditText mWordInput, mTranslationInput;
    
    private static final int WORD_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_activity);
        
        mWordInput = (EditText)findViewById(R.id.word);
        mTranslationInput = (EditText)findViewById(R.id.translation);
        
        final Bundle extras = getIntent().getExtras();
        mDictID = extras.getLong(Extra.DICT_ID);
        mWordID = extras.getLong(Extra.WORD_ID, ID_NONE);
        final String dictName = extras.getString(Extra.DICT_NAME);
        setTitle(dictName);

        if (mWordID != ID_NONE) {
            // It already existed. Let's recover it.
            getSupportLoaderManager().restartLoader(WORD_LOADER, null, this);
        } 
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.word_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_translation:
                final String word = mWordInput.getText().toString();
                final String translation = mTranslationInput.getText().toString();
                if (validateText(word) && validateText(translation)) {
                    saveTranslation(word, translation);
                } else {
                    Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT)
                            .show();
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean validateText(String text) {
        return !TextUtils.isEmpty(text);
    }

    private void saveTranslation(String word, String translation) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(DataProviderContract.WordColumns.DICT_ID, mDictID);
        values.put(DataProviderContract.WordColumns.WORD, word);
        values.put(DataProviderContract.WordColumns.TRANSLATION, translation);
        
        if (mWordID != ID_NONE) {
	        resolver.update(DataProviderContract.Word.CONTENT_URI, values, 
	                WordColumns._ID + "= ?", new String[]{String.valueOf(mWordID)});
        } else {
            // New entry
	        resolver.insert(DataProviderContract.Word.CONTENT_URI, values);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case WORD_LOADER:
		        return new CursorLoader(this, Word.CONTENT_URI,
		                TranslationQuery.PROJECTION, 
		                WordColumns._ID + " = ?",
		                new String[]{String.valueOf(mWordID)},
		                null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case WORD_LOADER:
                if (cursor.moveToFirst()) {
	                final String word = cursor.getString(TranslationQuery.WORD);
	                final String translation = cursor.getString(TranslationQuery.TRANSLATION);
                    mWordInput.setText(word);
                    mTranslationInput.setText(translation);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    interface DictQuery {
        String[] PROJECTION = {
		        DataProviderContract.DictColumns._ID,
		        DataProviderContract.DictColumns.NAME
        };
        
        int _ID = 0;
        int NAME = 1;
    }
    
    interface TranslationQuery {
        String[] PROJECTION = {
		        DataProviderContract.WordColumns._ID,
		        DataProviderContract.WordColumns.WORD,
		        DataProviderContract.WordColumns.TRANSLATION,
        };
        
        int _ID = 0;
        int WORD = 1;
        int TRANSLATION = 2;
    }
    
}
