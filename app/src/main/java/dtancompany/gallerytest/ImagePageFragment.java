package dtancompany.gallerytest;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
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

        final int width = 2048; //arbitrarily picked
        final int height = 2048;

        Activity activity = getActivity();

        Drawable b;
        if(this.activity.openFromHere){
            b = this.activity.fullPlaceholder;
            this.activity.openFromHere = false;

        }else{
            Bitmap d = BitmapFactory.decodeFile(mPath);
            d = resizeImage(d, 240);
            b = new BitmapDrawable(d);

        }


        Picasso.with(activity)
                .load(imageFile)
                .placeholder(b)
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

    private Drawable resize(Drawable image) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 50, 50, false);
        return new BitmapDrawable(getResources(), bitmapResized);
    }
    private Bitmap resize(Bitmap b){
        return Bitmap.createScaledBitmap(b, 50, 50, false);
    }private Bitmap resizeImage(Bitmap bitmap, int newSize){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int newWidth = 0;
        int newHeight = 0;

        if(width > height){
            newWidth = newSize;
            newHeight = (newSize * height)/width;
        } else if(width < height){
            newHeight = newSize;
            newWidth = (newSize * width)/height;
        } else if (width == height){
            newHeight = newSize;
            newWidth = newSize;
        }

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                width, height, matrix, true);

        return resizedBitmap;
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
