package dtancompany.gallerytest;

import android.os.AsyncTask;



public class DatabaseTask extends AsyncTask<DatabaseTaskParams, Void, KeywordSearchDbHelper> {

    @Override
    protected KeywordSearchDbHelper doInBackground(DatabaseTaskParams... params) {
        KeywordSearchDbHelper helper = new KeywordSearchDbHelper(params[0].context, params[0].images);
        helper.updateDatabase();

        return helper;
    }


}
