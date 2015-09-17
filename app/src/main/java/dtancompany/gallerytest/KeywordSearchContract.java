package dtancompany.gallerytest;

import android.provider.BaseColumns;

/**
 * Created by david on 2015-07-13.
 */
public class KeywordSearchContract {

    public KeywordSearchContract() {}

    public static abstract class Image implements BaseColumns {
        public static final String TABLE_NAME = "image";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_PATH = "path";
    }

    public static abstract class Keyword implements BaseColumns {
        public static final String TABLE_NAME = "keyword";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_KEYWORD = "key";
    }

    public static abstract class ImageKeyword implements BaseColumns {
        public static final String TABLE_NAME = "imagekeyword";
        public static final String COLUMN_NAME_IMAGE_ID = "imageid";
        public static final String COLUMN_NAME_KEYWORD_ID = "keywordid";
    }

}
