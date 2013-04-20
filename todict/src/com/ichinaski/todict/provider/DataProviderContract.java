package com.ichinaski.todict.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class DataProviderContract {
    public static final String AUTHORITY = "com.ichinaski.todict";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Tables {
        String DICTIONARY = "dictionary";
        String WORD = "word";
    }

    public interface DictColumns {
        String _ID = BaseColumns._ID;
        String NAME = "name";
    }

    public interface WordColumns {
        String _ID = BaseColumns._ID;
        String DICT_ID = "dict_id";
        String WORD = "word";
        String TRANSLATION = "translation";
        String STAR = "star";
        // String NOTES = "notes";
        // String DATE = "date";
    }

    public static class Dict {
        public static final Uri CONTENT_URI = 
		        Uri.withAppendedPath(BASE_CONTENT_URI, Tables.DICTIONARY);

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE 
                + "/" + Tables.DICTIONARY;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + Tables.DICTIONARY;
    }

    public static class Word {
        public static final Uri CONTENT_URI = 
		        Uri.withAppendedPath(BASE_CONTENT_URI, Tables.WORD);

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE 
                + "/" + Tables.WORD;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + Tables.WORD;
    }

    // The content provider database name
    public static final String DATABASE_NAME = "todict";

    // The starting version of the database
    public static final int DATABASE_VERSION = 1;

}
