package dtancompany.gallerytest;

import android.os.AsyncTask;

/**
 * Created by david on 2015-07-21.
 */

public class DatabaseTask extends AsyncTask<DatabaseTaskParams, Void, KeywordSearchDbHelper> {

    @Override
    protected KeywordSearchDbHelper doInBackground(DatabaseTaskParams... params) {
        KeywordSearchDbHelper helper = new KeywordSearchDbHelper(params[0].context, params[0].images);
        helper.updateDatabase();
        return helper;
    }
}
