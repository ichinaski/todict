package com.ichinaski.todict.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ichinaski.todict.R;
import com.ichinaski.todict.dao.Dict;
import com.ichinaski.todict.fragment.NewDictDialogFragment;
import com.ichinaski.todict.fragment.NewDictDialogFragment.INewDictionary;
import com.ichinaski.todict.provider.DataProviderContract;
import com.ichinaski.todict.provider.DataProviderContract.DictColumns;
import com.ichinaski.todict.provider.DataProviderContract.Word;
import com.ichinaski.todict.provider.DataProviderContract.WordColumns;
import com.ichinaski.todict.util.Extra;
import com.ichinaski.todict.util.Prefs;

public class DictActivity extends SherlockFragmentActivity implements LoaderCallbacks<Cursor>, 
        OnNavigationListener, INewDictionary {
    
    private ListView mListView;
    private WordAdapter mAdapter;
    private ArrayAdapter<String> mNavigationAdapter;
    
    private List<Dict> mDicts;
    private long mDictID;
    private String mDictName;
    
    private static final int DICT_LOADER = 0;
    private static final int WORD_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dict_activity);
        
        mListView = (ListView)findViewById(android.R.id.list);
        
        mAdapter = new WordAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mAdapter);
        mListView.setOnLongClickListener(mAdapter);
        
        mDictID = Prefs.getDefaultDict(this);// Default to the cached value
        init();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
	        long wordID = Long.parseLong(intent.getDataString());
	        startWordActivity(wordID);
        }
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
            names[i] = dict.getName();
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
        mDictName = dict.getName();
        Prefs.setDefaultDict(this, mDictID);
        getSupportLoaderManager().restartLoader(WORD_LOADER, null, this);
        return true;
    }
    
    private void init() {
        if (mDictID != Prefs.DICT_NONE) {
	        getSupportLoaderManager().restartLoader(DICT_LOADER, null, this);
	        getSupportLoaderManager().restartLoader(WORD_LOADER, null, this);
        } else {
            showNewDictFragment();
        }
    }
    
    private void showNewDictFragment() {
        DialogFragment df = new NewDictDialogFragment();
        df.show(getSupportFragmentManager(), "New Dict");
    }
    
    private void showDeleteDictFragment() {
        DialogFragment df = new DeleteDictDialogFragment();
        df.show(getSupportFragmentManager(), "Delete Dict");
    }

    @Override
    public void onNewDictionary(String name) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(DictColumns.NAME, name);
        Uri uri = resolver.insert(DataProviderContract.Dict.CONTENT_URI, values);
        mDictID = ContentUris.parseId(uri);
        Prefs.setDefaultDict(this, mDictID);
        init();
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
            case R.id.add_word:
                startWordActivity(WordActivity.ID_NONE);
                return true;
            case R.id.add_dict:
                showNewDictFragment();
	            return true;
            case R.id.delete_dict:
                showDeleteDictFragment();
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
            case WORD_LOADER:
		        return new CursorLoader(this, Word.CONTENT_URI,
		                WordQuery.PROJECTION, 
		                WordColumns.DICT_ID + " = ?",
		                new String[]{String.valueOf(mDictID)},
		                WordColumns.WORD);
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
			                cursor.getString(DictQuery.NAME));
	                dicts.add(dict);
                }
                setupNavigationMode(dicts);
                break;
            case WORD_LOADER:
		        mAdapter.changeCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == WORD_LOADER) {
	        mAdapter.changeCursor(null);
        }
    }
    
    class WordAdapter extends CursorAdapter implements OnItemClickListener, 
            OnLongClickListener {
        
        public WordAdapter(Context context) {
            super(context, null, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView word = (TextView)view.findViewById(android.R.id.text1);
            TextView translation = (TextView)view.findViewById(android.R.id.text2);
            
            word.setText(cursor.getString(WordQuery.WORD));
            translation.setText(cursor.getString(WordQuery.TRANSLATION));
            view.setTag(cursor.getLong(WordQuery._ID));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(android.R.layout.simple_list_item_2, null);
        }
	
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        final long wordID = (Long)view.getTag();
	        startWordActivity(wordID);
	    }
	
	    @Override
	    public boolean onLongClick(View v) {
	        // TODO
	        return true;
	    }
        
    }
    
    private void startWordActivity(long id) {
        Intent intent = new Intent(this, WordActivity.class);
        Bundle extras = new Bundle();
        extras.putLong(Extra.WORD_ID, id);
        extras.putLong(Extra.DICT_ID, mDictID);
        extras.putString(Extra.DICT_NAME, mDictName);
        intent.putExtras(extras);
        startActivity(intent);
    }
    
    private void deleteDict() {
        ContentResolver resolver = getContentResolver();
        if (resolver.delete(
                DataProviderContract.Dict.CONTENT_URI, 
                DictColumns._ID + "= ?", 
                new String[]{String.valueOf(mDictID)}) == 1) {
            Toast.makeText(this, "Dict deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
        
    }
    
    public static class DeleteDictDialogFragment extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        return new AlertDialog.Builder(getActivity())
	                .setTitle("Delete Dictionary")
	                .setTitle("Are you sure? All the data will be deleted")
	                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                        // TODO
	                        ((DictActivity)getActivity()).deleteDict();
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
		        DataProviderContract.WordColumns.TRANSLATION
        };
        
        int _ID = 0;
        int WORD = 1;
        int TRANSLATION = 2;
    }
    
}
