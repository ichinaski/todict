package com.ichinaski.todict.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
    
    private MenuItem mStarItem;
    private boolean mStar;
    
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
        mStarItem = menu.findItem(R.id.star_word);
        displayStar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_word:
                if (saveWord()) {
                    Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT)
                            .show();
	                finish();
                } else {
                    Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT)
                            .show();
                }
                return true;
            case R.id.star_word:
                mStar = !mStar;
                displayStar();
                break;
            case R.id.delete_word:
                if (mWordID == ID_NONE) {
	                finish();
                } else {
			        DialogFragment df = new DeleteWordDialogFragment();
			        df.show(getSupportFragmentManager(), "Delete Word");
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validateText(String text) {
        return !TextUtils.isEmpty(text);
    }

    private void deleteWord() {
        ContentResolver resolver = getContentResolver();
        if (resolver.delete(
                DataProviderContract.Word.CONTENT_URI, 
                WordColumns._ID + "= ?", 
                new String[]{String.valueOf(mWordID)}) == 1) {
            Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT)
                    .show();
            finish();
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private boolean saveWord() {
        final String word = mWordInput.getText().toString();
        final String translation = mTranslationInput.getText().toString();
        if (validateText(word) && validateText(translation)) {
	        ContentResolver resolver = getContentResolver();
	        ContentValues values = new ContentValues();
	        values.put(DataProviderContract.WordColumns.DICT_ID, mDictID);
	        values.put(DataProviderContract.WordColumns.WORD, word);
	        values.put(DataProviderContract.WordColumns.TRANSLATION, translation);
	        values.put(DataProviderContract.WordColumns.STAR, mStar ? 1 : 0);
	        
	        if (mWordID != ID_NONE) {
		        resolver.update(DataProviderContract.Word.CONTENT_URI, values, 
		                WordColumns._ID + "= ?", new String[]{String.valueOf(mWordID)});
	        } else {
	            // New entry
		        resolver.insert(DataProviderContract.Word.CONTENT_URI, values);
	        }
	        return true;
        }
        return false;
    }
    
    private void displayStar() {
        if (mStarItem == null) {
            return;
        }
        
        if (mStar) {
            mStarItem.setIcon(R.drawable.rate_star_big_on_holo_light);
        } else {
            mStarItem.setIcon(R.drawable.ic_menu_star);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case WORD_LOADER:
		        return new CursorLoader(this, Word.CONTENT_URI,
		                WordQuery.PROJECTION, 
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
	                final String word = cursor.getString(WordQuery.WORD);
	                final String translation = cursor.getString(WordQuery.TRANSLATION);
	                final int star = cursor.getInt(WordQuery.STAR);
                    mWordInput.setText(word);
                    mTranslationInput.setText(translation);
                    mStar = star == 0 ? false : true;
                    displayStar();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
    
    public static class DeleteWordDialogFragment extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        return new AlertDialog.Builder(getActivity())
	                .setTitle("Delete Word")
	                .setMessage("Are you sure?")
	                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                        ((WordActivity)getActivity()).deleteWord();
	                        dialog.dismiss();
	                    }
	                })
	                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                        dialog.dismiss();
	                    }
	                }).create();
	    }
    }

    interface DictQuery {
        String[] PROJECTION = {
		        DataProviderContract.DictColumns._ID,
		        DataProviderContract.DictColumns.NAME
        };
        
        int _ID = 0;
        int NAME = 1;
    }
    
    interface WordQuery {
        String[] PROJECTION = {
		        DataProviderContract.WordColumns._ID,
		        DataProviderContract.WordColumns.WORD,
		        DataProviderContract.WordColumns.TRANSLATION,
		        DataProviderContract.WordColumns.STAR,
        };
        
        int _ID = 0;
        int WORD = 1;
        int TRANSLATION = 2;
        int STAR = 3;
    }
    
}
