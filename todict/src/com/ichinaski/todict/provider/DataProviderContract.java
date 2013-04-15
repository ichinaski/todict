package com.ichinaski.todict.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class DataProviderContract {
    public static final String AUTHORITY = "com.ichinaski.todict";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Tables {
        String DICTIONARY = "dictionary";
        String TRANSLATION = "translation";
    }

    public interface DictColumns {
        String _ID = BaseColumns._ID;
        String LANG_1 = "lang_1";// language 1
        String LANG_2 = "lang_2";// language 2
    }

    public interface TranslationColumns {
        String _ID = BaseColumns._ID;
        String DICT_ID = "dict_id";
        String WORD = "word";// language 1
        String TRANSLATION = "translation";// language 2
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

    public static class Translation {
        public static final Uri CONTENT_URI = 
		        Uri.withAppendedPath(BASE_CONTENT_URI, Tables.TRANSLATION);

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE 
                + "/" + Tables.TRANSLATION;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + Tables.TRANSLATION;
    }

    // The content provider database name
    public static final String DATABASE_NAME = "todict";

    // The starting version of the database
    public static final int DATABASE_VERSION = 1;

}
