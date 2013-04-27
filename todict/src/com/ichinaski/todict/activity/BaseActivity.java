package com.ichinaski.todict.activity;

import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.ichinaski.todict.R;

public abstract class BaseActivity extends SherlockFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        BitmapDrawable bg = (BitmapDrawable)getResources().getDrawable(R.drawable.binding_dark);
        bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        getSupportActionBar().setBackgroundDrawable(bg);
    }
}
