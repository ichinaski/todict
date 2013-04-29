package com.ichinaski.todict.widget;

import android.appwidget.AppWidgetManager;
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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.ichinaski.todict.R;
import com.ichinaski.todict.provider.DataProviderContract;
import com.ichinaski.todict.util.Prefs;

public class SetupWidgetActivity extends SherlockFragmentActivity 
        implements LoaderCallbacks<Cursor>, OnItemClickListener {
    private ListView mListView;
    private DictAdapter mAdapter;
    private int mAppWidgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        
        mListView = new ListView(this);
        mListView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        setContentView(mListView);
        
        mAdapter = new DictAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        
        if (mAppWidgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final long dictID = (Long)view.getTag();
        
        Prefs.setWidgetDict(this, mAppWidgetID, dictID);
        
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        WidgetProvider.updateWidget(this, manager, mAppWidgetID);
        
        // Pass back the original appWidgetID
        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetID);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, DataProviderContract.Dict.CONTENT_URI,
                DictQuery.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }
    
    class DictAdapter extends CursorAdapter {
        
        public DictAdapter(Context context) {
            super(context, null, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView nameView = (TextView)view.findViewById(android.R.id.text1);
            nameView.setText(cursor.getString(DictQuery.NAME));
            view.setTag(cursor.getLong(DictQuery._ID));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(R.layout.dict_row, null);
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
}
