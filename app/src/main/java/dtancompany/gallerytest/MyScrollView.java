package dtancompany.gallerytest;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ScrollView;

/**
 * Created by david on 2015-08-04.
 */
public class MyScrollView extends ScrollView {
    // 180dp to allow the user to see the last item in the keyword container when keyboard is up
    // (for the nexus 5 5.0.2 emulator stock keyboard, others may make this incorrect)
    public static final int maxHeight = 90;
    // default constructors
    MyScrollView(Context context) {
        super(context);
    }
    MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    MyScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr,defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(dpToPx(getResources(),maxHeight), MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    private int dpToPx(Resources res, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }
}
