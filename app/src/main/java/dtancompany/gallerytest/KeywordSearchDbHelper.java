package dtancompany.gallerytest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import dtancompany.gallerytest.KeywordSearchContract.Image;
import dtancompany.gallerytest.KeywordSearchContract.ImageKeyword;
import dtancompany.gallerytest.KeywordSearchContract.Keyword;


/**
 * Created by Edward on 2015-07-14.
 *
 */
public class KeywordSearchDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "KeywordSearch.db";
    public Context context;
    public List<String> images;
    public SQLiteDatabase db;

    public KeywordSearchDbHelper(Context context, List<String> images ) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.images = images;
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_IMAGE_ENTRIES);
        db.execSQL(SQL_CREATE_KEYWORD_ENTRIES);
        db.execSQL(SQL_CREATE_IMAGE_KEYWORD_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //just start over
        db.execSQL(SQL_DELETE_IMAGE);
        db.execSQL(SQL_DELETE_KEYWORD);
        db.execSQL(SQL_DELETE_IMAGE_KEYWORD);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public synchronized void close() {
        db.close();
        super.close();
    }

    private static final String NUM_TYPE = " LONG";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_IMAGE_ENTRIES =
            "CREATE TABLE " + Image.TABLE_NAME + " (" +
                    Image._ID + " INTEGER PRIMARY KEY," +
                    Image.COLUMN_NAME_ID + NUM_TYPE + COMMA_SEP +
                    Image.COLUMN_NAME_PATH + TEXT_TYPE + " )";

    private static final String SQL_CREATE_KEYWORD_ENTRIES =
            "CREATE TABLE " + Keyword.TABLE_NAME + " (" +
                    Keyword._ID + " INTEGER PRIMARY KEY," +
                    Keyword.COLUMN_NAME_ID + NUM_TYPE + COMMA_SEP +
                    Keyword.COLUMN_NAME_KEYWORD + TEXT_TYPE + " )";

    private static final String SQL_CREATE_IMAGE_KEYWORD_ENTRIES =
            "CREATE TABLE " + ImageKeyword.TABLE_NAME + " (" +
                    ImageKeyword._ID + " INTEGER PRIMARY KEY," +
                    ImageKeyword.COLUMN_NAME_IMAGE_ID + NUM_TYPE + COMMA_SEP +
                    ImageKeyword.COLUMN_NAME_KEYWORD_ID + NUM_TYPE + " )";

    private static final String SQL_DELETE_IMAGE_KEYWORD =
            "DROP TABLE IF EXISTS " + ImageKeyword.TABLE_NAME;
    private static final String SQL_DELETE_IMAGE =
            "DROP TABLE IF EXISTS " + Image.TABLE_NAME;
    private static final String SQL_DELETE_KEYWORD =
            "DROP TABLE IF EXISTS " + Keyword.TABLE_NAME;


    //this function gets all previously unseen images and adds them and their keywords to the tables
    public void updateDatabase() {

        String[] projection = { Image.COLUMN_NAME_ID };
        String selection = Image.COLUMN_NAME_PATH + "=?";
        SQLiteStatement statement = db.compileStatement("SELECT COUNT(*) FROM " + Image.TABLE_NAME);
        long newImageId = statement.simpleQueryForLong(); // id for next new image is number of ids in image table

        for (String path : images) {
            String[] selectionArgs = { path };
            Cursor c = db.query( Image.TABLE_NAME, projection,
                    selection, selectionArgs, null, null, null );

            // if it doesn't exist, add a new row to image table
            if (!c.moveToFirst()) {

                ContentValues values = new ContentValues();
                values.put(Image.COLUMN_NAME_ID, newImageId);
                values.put(Image.COLUMN_NAME_PATH, path);
                //Insert the new row into the image table
                db.insert( Image.TABLE_NAME, "null", values);

                //note: keywords are only added here if it is a new image
                try {
                    ArrayList<String> keywords = IIMUtility.getKeywords(new File(path));

                    for (String keyword : keywords) {
                        addKeywordtoDatabaseEntry(keyword, newImageId);
                    }

                } catch (Exception e) {
                    Log.e("writeToDatabase", "stack trace", e);
                }

                newImageId++;
            }
            c.close();
        }
    }

    //this function returns images that have all keywords in list
    public HashSet<String> getMatchingImages( List<String> keywords ) {
        HashSet<String> ret = getMatchingImages(keywords.get(0));
        Log.d("dbMethods", "getMatchingImages keyword is " + keywords.get(0));
        for (int i = 1; i < keywords.size(); i++) {
            // remove images that don't match both first set and ith set
            ret.retainAll(getMatchingImages(keywords.get(i)));
            Log.d("dbMethods", "getMatchingImages keyword is " + keywords.get(i));
        }
        for(String path : ret){
            Log.d("dbMethods", "Filtered path is " +  path);
        }
        Log.d("dbMethods", String.valueOf(ret.size()));
        return ret;
    }

    // returns images that have keyword
    public HashSet<String> getMatchingImages( String keyword) {
        HashSet<String> ret = new HashSet<>();
        // the target table is join of the imagekeyword table with both the other tables
        String table = ImageKeyword.TABLE_NAME +
                " JOIN " + Image.TABLE_NAME + " ON " + ImageKeyword.TABLE_NAME + "." + ImageKeyword.COLUMN_NAME_IMAGE_ID +
                "=" + Image.TABLE_NAME + "." + Image.COLUMN_NAME_ID +
                " JOIN " + Keyword.TABLE_NAME + " ON " + ImageKeyword.TABLE_NAME + "." + ImageKeyword.COLUMN_NAME_KEYWORD_ID +
                "=" + Keyword.TABLE_NAME + "." + Keyword.COLUMN_NAME_ID;
        Log.d("dbMethods", table);
        String[] projection = { Image.COLUMN_NAME_PATH }; // desired result is path of the image
        String selection = Keyword.COLUMN_NAME_KEYWORD + "=?"; // we want results that have the keyword
        String[] selectionArgs = { keyword };
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, null); //TODO: possibly sort results here (by alpha) to speed up retainall
        while (c.moveToNext()) {
            String path = c.getString(0);
            ret.add(path);
            Log.d("dbMethods", "Path is " + path);
        }
        c.close();
        return ret;
    }

    //returns keywords of an image in random order
    public ArrayList<String> getMatchingKeywords(String imagePath) {
        ArrayList<String> ret = new ArrayList<>();
        String table = ImageKeyword.TABLE_NAME +
                " JOIN " + Image.TABLE_NAME + " ON " + ImageKeyword.TABLE_NAME + "." + ImageKeyword.COLUMN_NAME_IMAGE_ID +
                "=" + Image.TABLE_NAME + "." + Image.COLUMN_NAME_ID +
                " JOIN " + Keyword.TABLE_NAME + " ON " + ImageKeyword.TABLE_NAME + "." + ImageKeyword.COLUMN_NAME_KEYWORD_ID +
                "=" + Keyword.TABLE_NAME + "." + Keyword.COLUMN_NAME_ID;
        String[] projection = { Keyword.COLUMN_NAME_KEYWORD }; // desired result is keyword
        String selection = Image.COLUMN_NAME_PATH + "=?"; // we want results associated with the image
        String[] selectionArgs = { imagePath };
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, null); //TODO: sort somehow to keep order added
        while (c.moveToNext()) {
            String keyword = c.getString(0);
            ret.add(keyword);
            Log.d("dbMethods", "Path is " + keyword);
        }
        c.close();
        return ret;
    }

    //returns all keywords in Keyword table in random order
    public HashSet<String> getKeywords(){
        HashSet<String> ret = new HashSet<>();
        String table = Keyword.TABLE_NAME;
        Log.d("dbMethods", table);
        String[] projection = {Keyword.COLUMN_NAME_KEYWORD};
        Cursor c = db.query(table, projection, null, null, null, null, null, null);
        while(c.moveToNext()){
            String keyword = c.getString(0);
            ret.add(keyword);
            Log.d("dbMethods", "Keyword is " + keyword);
        }

        c.close();
        return ret;

    }



    public HashSet<String> getKeywords(HashSet<String> imagePaths, ArrayList<String> keywords){
        //get keywords of the images
        HashSet<String> containedKeywords = new HashSet<String>();
        for(String imagePath : imagePaths){
            containedKeywords.addAll(getKeywords(imagePath));
        }

        //remove the previously found keywords

        for(String keyword : keywords){
            containedKeywords.remove(keyword);
        }

        return containedKeywords;
    }

    private HashSet<String> getKeywords(String imagePath) {
        HashSet<String> ret = new HashSet<>();
        // the target table is join of the imagekeyword table with both the other tables
        String table = ImageKeyword.TABLE_NAME +
                " JOIN " + Image.TABLE_NAME + " ON " + ImageKeyword.TABLE_NAME + "." + ImageKeyword.COLUMN_NAME_IMAGE_ID +
                "=" + Image.TABLE_NAME + "." + Image.COLUMN_NAME_ID +
                " JOIN " + Keyword.TABLE_NAME + " ON " + ImageKeyword.TABLE_NAME + "." + ImageKeyword.COLUMN_NAME_KEYWORD_ID +
                "=" + Keyword.TABLE_NAME + "." + Keyword.COLUMN_NAME_ID;
        Log.d("dbMethods", table);
        String[] projection = { Keyword.COLUMN_NAME_KEYWORD }; // desired result is keyword of the image
        String selection = Image.COLUMN_NAME_PATH + "=?"; // we want results that have the path
        String[] selectionArgs = { imagePath };
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, null); //TODO: possibly sort results here (by alpha) to speed up retainall
        while (c.moveToNext()) {
            String path = c.getString(0);
            ret.add(path);
            Log.d("dbMethods", "Keyword is " + path);
        }
        c.close();
        return ret;
    }

    //adds keyword image pair to image-keyword table, and keyword table if it does not already exist
    public void addKeywordtoDatabaseEntry( String keyword, long imageId) {
        // first, find id of keyword
        final String[] projection = { Image.COLUMN_NAME_ID };
        String[] selectionArgs = new String[] { keyword };
        String selection = Keyword.COLUMN_NAME_KEYWORD + "=?";

        Cursor c = db.query( Keyword.TABLE_NAME, projection,
                selection, selectionArgs, null, null, null );

        long keywordId;
        ContentValues values;

        // if it doesn't exist, add a new row to keyword table
        if (!c.moveToFirst()) {
            SQLiteStatement statement = db.compileStatement("SELECT COUNT(*) FROM " + Keyword.TABLE_NAME);
            keywordId = statement.simpleQueryForLong(); // id for next new keyword is number of ids in keyword table

            values = new ContentValues();
            values.put(Keyword.COLUMN_NAME_ID, keywordId);
            values.put(Keyword.COLUMN_NAME_KEYWORD, keyword);
            db.insert(Keyword.TABLE_NAME, "null", values);
        } else {
            keywordId = c.getLong(c.getColumnIndex(Keyword.COLUMN_NAME_ID));
        }
        c.close();

        // then, add a new row to the image-keyword lookup table
        values = new ContentValues();
        values.put(ImageKeyword.COLUMN_NAME_IMAGE_ID, imageId);
        values.put(ImageKeyword.COLUMN_NAME_KEYWORD_ID, keywordId);
        db.insert(ImageKeyword.TABLE_NAME, "null", values);
    }

    public void addKeywordtoDatabaseEntry( String keyword, String imagePath) {
        final String[] projection = { Image.COLUMN_NAME_ID };
        String[] selectionArgs = { imagePath };
        String selection = Image.COLUMN_NAME_PATH + "=?";
        Cursor c = db.query(Image.TABLE_NAME, projection,
                selection, selectionArgs, null, null, null);
        c.moveToFirst();
        long imageId = c.getLong(0);
        c.close();
        addKeywordtoDatabaseEntry(keyword, imageId);
    }

    // removes the row in the image-keyword table with this pair, if it exists
    public void removeKeywordfromDatabaseEntry( String keyword, String imagePath) {

        String table = ImageKeyword.TABLE_NAME;
        String whereClause = ImageKeyword.TABLE_NAME + "." + ImageKeyword.COLUMN_NAME_IMAGE_ID +
                " in (SELECT " + Image.COLUMN_NAME_ID + " FROM " + Image.TABLE_NAME +
                " WHERE " + Image.COLUMN_NAME_PATH + "=?) AND " +
                ImageKeyword.TABLE_NAME + "." + ImageKeyword.COLUMN_NAME_KEYWORD_ID +
                " in (SELECT " + Keyword.COLUMN_NAME_ID + " FROM " + Keyword.TABLE_NAME +
                " WHERE " + Keyword.COLUMN_NAME_KEYWORD + "=?)";

        String[] whereArgs = new String[] { imagePath, keyword };
        db.delete(table, whereClause, whereArgs);

        if (getMatchingImages(keyword).size() == 0) {
            table = Keyword.TABLE_NAME;
            whereClause = Keyword.COLUMN_NAME_KEYWORD + "=?";
            whereArgs = new String[] { keyword };
            db.delete(table, whereClause, whereArgs);
        }
    }

    public void removeAllKeywordsfromDatabaseEntry(String imagePath) {

        ArrayList<String> keywords = getMatchingKeywords(imagePath);

        String table = ImageKeyword.TABLE_NAME;
        String whereClause = ImageKeyword.TABLE_NAME + "." + ImageKeyword.COLUMN_NAME_IMAGE_ID +
                " in (SELECT " + Image.COLUMN_NAME_ID + " FROM " + Image.TABLE_NAME +
                " WHERE " + Image.COLUMN_NAME_PATH + "=?)";
        String[] whereArgs = new String[] { imagePath };
        db.delete(table, whereClause, whereArgs);

        // if removing the entries left no images with these keywords, remove them,
        // so search suggestions has no false entries
        for (String keyword : keywords) {
            if (getMatchingImages(keyword).size() == 0) {
                table = Keyword.TABLE_NAME;
                whereClause = Keyword.COLUMN_NAME_KEYWORD + "=?";
                whereArgs = new String[] { keyword };
                db.delete(table, whereClause, whereArgs);
            }
        }

    }


    //prints all tables to log
    public void testDatabase() {
        rebuildDatabase();

        Cursor c = db.rawQuery("SELECT "+ Image.COLUMN_NAME_ID + "," + Image.COLUMN_NAME_PATH + " FROM " + Image.TABLE_NAME, null );
        Log.d("DatabaseTest","getting image table");
        while ( c.moveToNext()) {
            Log.d("DatabaseTest","id: " + String.valueOf(c.getLong(0)) + " path: " + c.getString(1));
        }
        c = db.rawQuery("SELECT "+ Keyword.COLUMN_NAME_ID + "," + Keyword.COLUMN_NAME_KEYWORD + " FROM " + Keyword.TABLE_NAME, null );
        Log.d("DatabaseTest","getting keyword table");
        while ( c.moveToNext()) {
            Log.d("DatabaseTest","id: " + String.valueOf(c.getLong(0)) + " keyword: " + c.getString(1));
        }
        c = db.rawQuery("SELECT "+ ImageKeyword.COLUMN_NAME_IMAGE_ID + "," + ImageKeyword.COLUMN_NAME_KEYWORD_ID + " FROM " + ImageKeyword.TABLE_NAME, null );
        Log.d("DatabaseTest", "getting image-keyword table");
        while ( c.moveToNext()) {
            Log.d("DatabaseTest","i/id: " + String.valueOf(c.getLong(0)) + " k/id: " + String.valueOf(c.getLong(1)));
        }

        c.close();
    }

    // completely rebuilds the table
    public void rebuildDatabase() {
        this.onUpgrade(db, DATABASE_VERSION, DATABASE_VERSION);
        updateDatabase();
    }

    // adds keyword to image and database //todo: IIMUtility should be unknown, move these test functions into main activity
    private boolean addKeyword(HashSet<File> images, String keyword ) {
        boolean allAdded = true;
        for (File file : images) {
            try {
                if (IIMUtility.addKeyword(context, file, keyword)) {
                    addKeywordtoDatabaseEntry(keyword, file.getAbsolutePath());
                }
                else {
                    allAdded = false;
                }
            } catch (Exception e) {
                Log.e("DatabaseSearch", "stack trace", e);
                allAdded = false;
            }
        }

        return allAdded;
    }

    // tests getMatchingImages, requires at least 10 images to work
    public void testDatabaseSearch(SQLiteDatabase db) {

        if (images.size() < 10) {
            return;
        }

        HashSet<File> testimages = new HashSet<>();
        testimages.add(new File(images.get(0)));
        testimages.add(new File(images.get(1))); //both
        testimages.add(new File(images.get(3)));
        testimages.add(new File(images.get(4))); //both
        testimages.add(new File(images.get(6)));
        testimages.add(new File(images.get(10)));

        rebuildDatabase();

        String test1 = "foo test 1";
        String test2 = "bar test 2";
        String test3 = "qot test 3";

        addKeyword(testimages,test1);

        HashSet<File> testimages2 = new HashSet<>();
        testimages2.add(new File(images.get(2)));
        testimages2.add(new File(images.get(7)));
        testimages2.add(new File(images.get(8)));
        testimages2.add(new File(images.get(9)));
        testimages2.add(new File(images.get(1))); //both
        testimages2.add(new File(images.get(4))); //both

        addKeyword(testimages2, test2);

        ArrayList<String> keywords = new ArrayList<>();
        keywords.add(test1);
        keywords.add(test2);

        HashSet<String> match1 = getMatchingImages(test1);
        for (String string : match1) {
            Log.d("DatabaseSearch","1) keyword 1 match: " + string);
        }

        HashSet<String> match2 = getMatchingImages(test2);
        for (String string : match2) {
            Log.d("DatabaseSearch","2) keyword 2 match: " + string);
        }

        HashSet<String> match3 = getMatchingImages(keywords);
        for (String string : match3) {
            Log.d("DatabaseSearch","3) both keywords match: " + string);
        }


        for (String string : match1) {
            try {
                IIMUtility.removeKeyword(context, new File(string), test1);
                removeKeywordfromDatabaseEntry(test1, string);
            } catch (Exception e) {
                Log.e("DatabaseSearch", "stack trace", e);
            }
        }

        HashSet<String> match4 = getMatchingImages(test1);
        for (String string : match4) {
            Log.d("DatabaseSearch","4) keyword 1 match: " + string);
        }

        for (String string : match2) {
            try {
                IIMUtility.removeKeyword(context, new File(string), test2);
                removeKeywordfromDatabaseEntry(test2, string);
            } catch (Exception e) {
                Log.e("DatabaseSearch", "stack trace", e);
            }
        }

        HashSet<String> match5 = getMatchingImages(test2);
        for (String string : match5) {
            Log.d("DatabaseSearch","5) keyword 2 match: " + string);
        }

        HashSet<String> match6 = getMatchingImages(keywords);
        for (String string : match6) {
            Log.d("DatabaseSearch","6) both keywords match: " + string);
        }

    }
}
