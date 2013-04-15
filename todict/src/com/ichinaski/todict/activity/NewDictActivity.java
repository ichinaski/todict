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
    private EditText mLanguageInput1, mLanguageInput2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_dict_activity);

        mLanguageInput1 = (EditText)findViewById(R.id.lang1);
        mLanguageInput2 = (EditText)findViewById(R.id.lang2);
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
                final String lang1 = mLanguageInput1.getText().toString();
                final String lang2 = mLanguageInput2.getText().toString();
                if (validateText(lang1) && validateText(lang1)) {
                    long dictID = addDict(lang1, lang2);
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

    private boolean validateText(String text) {
        return !TextUtils.isEmpty(text);
    }

    private long addDict(String lang1, String lang2) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(DictColumns.LANG_1, lang1);
        values.put(DictColumns.LANG_2, lang2);
        Uri uri = resolver.insert(Dict.CONTENT_URI, values);
        return ContentUris.parseId(uri);
    }

}
