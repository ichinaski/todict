package com.ichinaski.todict.activity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ichinaski.todict.R;
import com.ichinaski.todict.provider.DataProviderContract.Dict;
import com.ichinaski.todict.provider.DataProviderContract.DictColumns;
import com.ichinaski.todict.util.Prefs;

public class NewDictActivity extends SherlockFragmentActivity {
    private EditText mNameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_dict_activity);

        mNameInput = (EditText)findViewById(R.id.dict_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.new_dict_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_dict:
                final String name = mNameInput.getText().toString();
                if (!TextUtils.isEmpty(name)) {
                    long dictID = addDict(name);
                    Prefs.setDefaultDict(this, dictID);
	                setResult(RESULT_OK);
	                finish();
                } else {
                    Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT)
                            .show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private long addDict(String name) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(DictColumns.NAME, name);
        Uri uri = resolver.insert(Dict.CONTENT_URI, values);
        return ContentUris.parseId(uri);
    }

}
