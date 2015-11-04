package dtancompany.gallerytest;

import android.content.Context;

import java.util.List;

/**
 * Created by Edward on 2015-07-21.
 */
public class DatabaseTaskParams {
    Context context;
    List<String> images;

    DatabaseTaskParams(Context context, List<String> images) {
        this.context = context;
        this.images = images;
    }
}
