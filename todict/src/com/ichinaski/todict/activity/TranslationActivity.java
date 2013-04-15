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
import com.ichinaski.todict.provider.DataProviderContract.Translation;
import com.ichinaski.todict.provider.DataProviderContract.TranslationColumns;
import com.ichinaski.todict.util.Extra;

public class TranslationActivity extends SherlockFragmentActivity 
        implements LoaderCallbacks<Cursor>{
    public static final long ID_NONE = -1;
    
    private long mDictID;
    private long mTranslationID;
    
    private EditText mWordInput, mTranslationInput;
    
    private static final int DICT_LOADER = 0;
    private static final int TRANSLATION_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_translation_activity);
        
        mWordInput = (EditText)findViewById(R.id.word);
        mTranslationInput = (EditText)findViewById(R.id.translation);
        
        final Bundle extras = getIntent().getExtras();
        mDictID = extras.getLong(Extra.DICT_ID);
        mTranslationID = extras.getLong(Extra.TRANSLATION_ID, ID_NONE);
        final String language1 = extras.getString(Extra.LANGUAGE1);
        final String language2 = extras.getString(Extra.LANGUAGE2);
        
        mWordInput.setHint(language1);
        mTranslationInput.setHint(language2);
        
        setTitle(language1 + " - " + language2);

        if (mTranslationID != ID_NONE) {
            // It already existed. Let's recover it.
            getSupportLoaderManager().restartLoader(TRANSLATION_LOADER, null, this);
        } 
        /*
        else {
	        getSupportLoaderManager().restartLoader(DICT_LOADER, null, this);
        }
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.translation_activity, menu);
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
    
    private void setDict(long id, String lang1, String lang2) {
        mDictID = id;
        mWordInput.setHint(lang1);
        mTranslationInput.setHint(lang2);
        setTitle(lang1 + " - " + lang2);
    }

    private boolean validateText(String text) {
        return !TextUtils.isEmpty(text);
    }

    private void saveTranslation(String word, String translation) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(DataProviderContract.TranslationColumns.DICT_ID, mDictID);
        values.put(DataProviderContract.TranslationColumns.WORD, word);
        values.put(DataProviderContract.TranslationColumns.TRANSLATION, translation);
        
        if (mTranslationID != ID_NONE) {
	        resolver.update(DataProviderContract.Translation.CONTENT_URI, values, 
	                TranslationColumns._ID + "= ?", new String[]{String.valueOf(mTranslationID)});
        } else {
            // New entry
	        resolver.insert(DataProviderContract.Translation.CONTENT_URI, values);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            /*
            case DICT_LOADER:
		        return new CursorLoader(this, DataProviderContract.Dict.CONTENT_URI,
		                DictQuery.PROJECTION, 
		                DictColumns._ID + " = ?",
		                new String[]{String.valueOf(mDictID)},
		                null);
		                */
            case TRANSLATION_LOADER:
		        return new CursorLoader(this, Translation.CONTENT_URI,
		                TranslationQuery.PROJECTION, 
		                TranslationColumns._ID + " = ?",
		                new String[]{String.valueOf(mTranslationID)},
		                null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case DICT_LOADER:
                if (cursor.moveToFirst()) {
	                final String language1 = cursor.getString(DictQuery.LANG1);
	                final String language2 = cursor.getString(DictQuery.LANG2);
	                setDict(mDictID, language1, language2);
                }
                break;
            case TRANSLATION_LOADER:
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
		        DataProviderContract.DictColumns.LANG_1,
		        DataProviderContract.DictColumns.LANG_2
        };
        
        int _ID = 0;
        int LANG1 = 1;
        int LANG2 = 2;
    }
    
    interface TranslationQuery {
        String[] PROJECTION = {
		        DataProviderContract.TranslationColumns._ID,
		        DataProviderContract.TranslationColumns.WORD,
		        DataProviderContract.TranslationColumns.TRANSLATION,
        };
        
        int _ID = 0;
        int WORD = 1;
        int TRANSLATION = 2;
    }
    
}
