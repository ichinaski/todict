package com.ichinaski.todict.widget;

import java.util.Random;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import com.ichinaski.todict.R;
import com.ichinaski.todict.provider.DataProviderContract;
import com.ichinaski.todict.provider.DataProviderContract.WordColumns;
import com.ichinaski.todict.util.Prefs;

public class WidgetProvider extends AppWidgetProvider {
    private static final String TAG = WidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        final int n = ids.length;

        for (int i = 0; i < n; i++) {
            final int widgetID = ids[i];
            updateWidget(context, manager, widgetID);
        }
    }
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "onDeleted()");
        // Delete the preference associated with this widget
        for (int id : appWidgetIds) {
            Prefs.deleteWidgetInfo(context, id);
        }
    }
    
    static void updateWidget(Context context, AppWidgetManager manager, int widgetID) {
        final long dictID = Prefs.getWidgetDictID(context, widgetID);
        Log.d(TAG, "updateAppWidget() - WidgetID: " + widgetID + ". DictID: " + dictID);
        
        final Cursor cursor = queryDB(context, widgetID, dictID);
        final int rows = cursor.getCount();

        if (rows == 0 || !cursor.moveToFirst()) {
            return;// No data available
        }

        final int position = new Random().nextInt(rows);

        if (cursor.move(position)) {
            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.homescreen_widget);

            views.setTextViewText(R.id.text1, cursor.getString(WordQuery.WORD));
            views.setTextViewText(R.id.text2, cursor.getString(WordQuery.TRANSLATION));

            // Let's update it when the widget gets clicked.
            Intent intent = new Intent(context, WidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetID});

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, widgetID, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            manager.updateAppWidget(widgetID, views);
        }
        
    }

    private static Cursor queryDB(Context context, int widgetID, long dictID) {
        final ContentResolver resolver = context.getContentResolver();
        return resolver.query(DataProviderContract.Word.CONTENT_URI, 
                WordQuery.PROJECTION,
                WordColumns.DICT_ID + " = ? "
                + "AND " + WordColumns.STAR + " = ?", 
                new String[] {String.valueOf(dictID), String.valueOf(1)}, 
                null);
    }

    interface WordQuery {
        String[] PROJECTION = {
                DataProviderContract.WordColumns._ID, 
                DataProviderContract.WordColumns.WORD,
                DataProviderContract.WordColumns.TRANSLATION, 
                DataProviderContract.WordColumns.STAR
        };

        int _ID = 0;
        int WORD = 1;
        int TRANSLATION = 2;
        int STAR = 3;
    }
}
