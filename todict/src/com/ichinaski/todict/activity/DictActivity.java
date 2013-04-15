package com.ichinaski.todict.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ichinaski.todict.R;
import com.ichinaski.todict.dao.Dict;
import com.ichinaski.todict.provider.DataProviderContract;
import com.ichinaski.todict.provider.DataProviderContract.Translation;
import com.ichinaski.todict.provider.DataProviderContract.TranslationColumns;
import com.ichinaski.todict.util.Extra;
import com.ichinaski.todict.util.Prefs;

public class DictActivity extends SherlockFragmentActivity implements LoaderCallbacks<Cursor>, 
        OnNavigationListener, OnItemClickListener, OnLongClickListener {
    public static final int REQUEST_ADD_DICT = 100;
    
    private ListView mListView;
    private CursorAdapter mAdapter;
    private ArrayAdapter<String> mNavigationAdapter;
    
    private List<Dict> mDicts;
    private long mDictID;
    private String mLanguage1;
    private String mLanguage2;
    
    private static final int DICT_LOADER = 0;
    private static final int TRANSLATION_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dict_activity);
        
        mListView = (ListView)findViewById(android.R.id.list);
        
        mAdapter = new TranslationAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnLongClickListener(this);
        
        mDictID = Prefs.getDefaultDict(this);// Default to the cached value
        init();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_DICT && resultCode == RESULT_OK) {
            mDictID = Prefs.getDefaultDict(this);
            init();
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        long translationID = Long.parseLong(getIntent().getDataString());
        startTranslationActivity(translationID);
    }
    
    private void setupNavigationMode(List<Dict> dicts) {
        mDicts = dicts;
        
        // List navigation mode
        final ActionBar actionBar = getSupportActionBar();
        final Context context = actionBar.getThemedContext();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
        String[] names = new String[mDicts.size()];
        int currentIndex = 0;
        for (int i=0; i<mDicts.size(); i++) {
            final Dict dict = mDicts.get(i);
            names[i] = dict.toString();
            if (dict.getID() == mDictID) {
                currentIndex = i;
            }
        }
                
        mNavigationAdapter = new ArrayAdapter<String>(context, 
		        com.actionbarsherlock.R.layout.sherlock_spinner_item, 
		        names);
        mNavigationAdapter.setDropDownViewResource(
                com.actionbarsherlock.R.layout.sherlock_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(mNavigationAdapter, this);
        actionBar.setSelectedNavigationItem(currentIndex);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        final Dict dict = mDicts.get(itemPosition);
        mDictID = dict.getID();
        mLanguage1 = dict.getLang1();
        mLanguage2 = dict.getLang2();
        Prefs.setDefaultDict(this, mDictID);
        getSupportLoaderManager().restartLoader(TRANSLATION_LOADER, null, this);
        return true;
    }
    
    private void init() {
        if (mDictID != Prefs.DICT_NONE) {
	        getSupportLoaderManager().restartLoader(DICT_LOADER, null, this);
	        getSupportLoaderManager().restartLoader(TRANSLATION_LOADER, null, this);
        } else {
            Intent intent = new Intent(this, NewDictActivity.class);
            startActivityForResult(intent, REQUEST_ADD_DICT);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.dict_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;
            case R.id.add_translation:
                startTranslationActivity(TranslationActivity.ID_NONE);
                return true;
            case R.id.add_dict:
	            Intent newDictIntent = new Intent(this, NewDictActivity.class);
	            startActivityForResult(newDictIntent, REQUEST_ADD_DICT);
	            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DICT_LOADER:
		        return new CursorLoader(this, DataProviderContract.Dict.CONTENT_URI,
		                DictQuery.PROJECTION, null, null, null);
            case TRANSLATION_LOADER:
		        return new CursorLoader(this, Translation.CONTENT_URI,
		                TranslationQuery.PROJECTION, 
		                TranslationColumns.DICT_ID + " = ?",
		                new String[]{String.valueOf(mDictID)},
		                TranslationColumns.WORD);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case DICT_LOADER:
                List<Dict> dicts = new ArrayList<Dict>();
                while (cursor.moveToNext()) {
                    Dict dict = new Dict(
                            cursor.getLong(DictQuery._ID),
			                cursor.getString(DictQuery.LANG1),
			                cursor.getString(DictQuery.LANG2));
	                dicts.add(dict);
                }
                setupNavigationMode(dicts);
                break;
            case TRANSLATION_LOADER:
		        mAdapter.changeCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == TRANSLATION_LOADER) {
	        mAdapter.changeCursor(null);
        }
    }
    
    class TranslationAdapter extends CursorAdapter {
        
        public TranslationAdapter(Context context) {
            super(context, null, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            //TextView word = (TextView)view.findViewById(R.id.word);
            //TextView translation = (TextView)view.findViewById(R.id.translation);
            TextView word = (TextView)view.findViewById(android.R.id.text1);
            TextView translation = (TextView)view.findViewById(android.R.id.text2);
            
            word.setText(cursor.getString(TranslationQuery.WORD));
            translation.setText(cursor.getString(TranslationQuery.TRANSLATION));
            view.setTag(cursor.getLong(TranslationQuery._ID));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(context);
            //return inflater.inflate(R.layout.translation_row, null);
            return inflater.inflate(android.R.layout.simple_list_item_2, null);
        }
        
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startTranslationActivity((Long)view.getTag());
    }

    @Override
    public boolean onLongClick(View v) {
        // TODO
        return true;
    }
    
    private void startTranslationActivity(long id) {
        Intent intent = new Intent(this, TranslationActivity.class);
        Bundle extras = new Bundle();
        extras.putLong(Extra.TRANSLATION_ID, id);
        extras.putLong(Extra.DICT_ID, mDictID);
        extras.putString(Extra.LANGUAGE1, mLanguage1);
        extras.putString(Extra.LANGUAGE2, mLanguage2);
        intent.putExtras(extras);
        startActivity(intent);
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
		        DataProviderContract.TranslationColumns.TRANSLATION
        };
        
        int _ID = 0;
        int WORD = 1;
        int TRANSLATION = 2;
    }
    
}
