package dtancompany.gallerytest;


import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by david on 2015-08-11.
 */
public class ImagePageFragment extends android.support.v4.app.Fragment
        implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener,
        View.OnTouchListener{

    String mPath;
    TouchImageView fullImg;
    MainActivity activity;
    private GestureDetectorCompat mDetector;
    private final static String PATH_KEY = "path";

    static ImagePageFragment newInstance(String imagePath) {
        ImagePageFragment f = new ImagePageFragment();

        // Supply image path input as an argument.
        Bundle args = new Bundle();
        args.putString(PATH_KEY, imagePath);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPath = getArguments().getString(PATH_KEY);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pager_item, container, false);
        fullImg = (TouchImageView) v.findViewById(R.id.full_img);
        fullImg.setBackgroundColor(activity.color50);
        final File imageFile = new File (mPath);
        final int width = 1024; //arbitrarily picked
        final int height = 1024;

        Activity activity = getActivity();
        Drawable placeholder = activity.getResources().getDrawable(R.drawable.shape_clear, null);

        Picasso.with(activity)
                .load(imageFile)
                .placeholder(placeholder)
                .resize(width, height)
                .centerInside()
                .into(fullImg, new Callback() {

                    @Override
                    public void onSuccess() {
                        Log.d("Picasso", "Loaded Pager Image File");
                    }

                    @Override
                    public void onError() {
                        Log.d("File", "File length is " + imageFile.length());
                    }
                });

        mDetector = new GestureDetectorCompat(activity, this);
        mDetector.setOnDoubleTapListener(this);
        fullImg.setOnTouchListener(this);

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (activity.currentFragment != null) {
            return activity.currentFragment.onSingleTapUp(e);
        } else {
            return false;
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (activity.currentFragment != null) {
            return activity.currentFragment.onSingleTapConfirmed(e);
        } else {
            return false;
        }
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
}
