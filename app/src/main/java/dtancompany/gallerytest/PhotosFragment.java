package dtancompany.gallerytest;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.nmote.iim4j.stream.IIMNotFoundException;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhotosFragment extends android.app.Fragment implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, View.OnTouchListener {

    MainActivity activity;

    private RelativeLayout layout;
    private RelativeLayout keywordContainer;
    EditTextOnTouchListener editTextTouchListener;

    private int position;
    private ArrayList<String> images;
    private File currentImageFile;
    ActionBar actionBar;
    ColorDrawable actionBarColor;
    private GestureDetectorCompat mDetector;
    LinearLayout panelRoot;
    SlidingUpPanelLayout slidingPanel;

    KeywordNode latest = null;
    Animator mCurrentAnimator;

    private ViewPager pager;
    private ImagePagerAdapter adapter;

    public static PhotosFragment newInstance(int position){
        PhotosFragment pf = new PhotosFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        pf.setArguments(bundle);
        return pf;
    }

    public PhotosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        actionBar = activity.getActionBar();
        position = getArguments().getInt("position");
        actionBarColor = new ColorDrawable(activity.color500clear);
        images = activity.images;
        //actionBarColor.setAlpha(200);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.root.setPadding(0, actionBar.getHeight(), 0, 0);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        final Animator animator;
        if (nextAnim == R.animator.photos_fragment_set_left ||
                nextAnim == R.animator.photos_fragment_set_right) {
            animator = AnimatorInflater.loadAnimator(getActivity(), nextAnim);

            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    //
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    activity.root.setPadding(0, 0, 0, 0);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        }
                    }, 10);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    //
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    //
                }
            });
        } else {
            animator = null;
        }
        return animator;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // sets actionbar/layout
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO);
        actionBar.setBackgroundDrawable(actionBarColor);
        layout = (RelativeLayout) inflater.inflate(R.layout.fragment_photos, container, false);

        //sets reference to pager view
        pager = (ViewPager) layout.findViewById(R.id.pager);

        //sets up adapter for pager
        adapter = new ImagePagerAdapter(activity.getSupportFragmentManager(),images);
        pager.setAdapter(adapter);
        pager.setCurrentItem(position);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentImageFile = new File(images.get(position));
                keywordContainer.removeAllViews();
                latest = null;
                populateKeywordContainer();
            }
        });

        //image path of selected activity from image is saved
        final String imagePath = images.get(position);
        currentImageFile = new File(imagePath);

        mDetector = new GestureDetectorCompat(activity, this);
        mDetector.setOnDoubleTapListener(this);

        // programmatically adding custom scroll view
        final MyScrollView scrollView = new MyScrollView(activity);
        LinearLayout.LayoutParams scrollViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, activity.getResources().getDisplayMetrics());
        scrollViewParams.setMargins(0,margin,0,0);
        scrollView.setLayoutParams(scrollViewParams);

        //final RelativeLayout keywordContainer = (RelativeLayout) layout.findViewById(R.id.keyword_container);
        keywordContainer = new RelativeLayout(activity);
        keywordContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //keywordContainer.setBackgroundColor(getResources().getColor(R.color.panel_color_1));
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, activity.getResources().getDisplayMetrics());
        int paddingTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, activity.getResources().getDisplayMetrics());
        keywordContainer.setPaddingRelative(padding, paddingTop, padding, padding);
        scrollView.addView(keywordContainer);

        panelRoot = (LinearLayout) layout.findViewById(R.id.panel_root);
        panelRoot.addView(scrollView, 2);
        panelRoot.setBackgroundColor(activity.color100);
        keywordContainer.setClipToPadding(false);

        keywordContainer.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        keywordContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        populateKeywordContainer();
                    }
                }
        );

        final EditText editText = (EditText) layout.findViewById(R.id.keyword_edit_text);
        /*
        //this is custom as well
        final MyEditText editText = new MyEditText(activity);
        editText.setElevation(15);
        editText.setHint(R.string.keyword_edit_text_hint);
        editText.setHintTextColor(getResources().getColor(R.color.black));
        editText.setGravity(Gravity.BOTTOM);
        editText.setBackground(null);
        editText.setTextSize(18); //sp
        editText.setTextColor(Color.BLACK);
        editText.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, activity.getResources().getDisplayMetrics());
        params.setMargins(0, 0, 0, margin);
        editText.setLayoutParams(params);

        panelRoot.addView(editText, 1);
        */


        //scale drawable
        final Resources resources = getResources();
        BitmapDrawable drawableFull = (BitmapDrawable) resources.getDrawable(R.drawable.ic_action_new, null);
        Bitmap bitmap = drawableFull.getBitmap();
        BitmapDrawable drawableScaled = new BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 60, 60, true));
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawableScaled, null);

        activity.setFilters(editText);
        editTextTouchListener = new EditTextOnTouchListener(editText, keywordContainer);
        editText.setOnTouchListener(editTextTouchListener);

        //todo: bug: typing just after pressing the + will cause the editText to lose focus, hiding the keyboard
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideSoftKeyboard(v);
                }
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
                        addKeyword(editText, keywordContainer, currentImageFile);
                    }
                }
                return false;
            }

        });

        slidingPanel = (SlidingUpPanelLayout) layout.findViewById(R.id.sliding_layout);
        slidingPanel.setDragView(R.id.drag_view);
        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        slidingPanel.setPanelSlideListener(new SlidingUpPanelLayout.SimplePanelSlideListener() {

            ImageView clickable = (ImageView) layout.findViewById(R.id.clickable_image);

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                int rotation = (int) (slideOffset * 180); //rotate halfway tilting toward user
                clickable.setRotationX(rotation);
            }
        });

/*
        final int width = 2048; //arbitrarily picked
        final int height = 2048;
        final TouchImageView fullImg = (TouchImageView) layout.findViewById(R.id.full_img);
        fullImg.setOnTouchListener(this);


        c.with(activity)
                .load(imageFile)
                .placeholder(activity.fullPlaceholder)
                            //.transform(new BitmapTransform(width, height))
                .resize(width, height)
                .centerInside()
                .into(fullImg, new Callback() {

                    @Override
                    public void onSuccess() {
                        Log.d("Picasso", "Loaded Image File");
                    }

                    @Override
                    public void onError() {
                        Log.d("File", "File length is " + imageFile.length());
                    }
                });
                */

        View.OnClickListener shareButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    IIMUtility.addMandatoryData(activity, currentImageFile);
                } catch (Exception e) {
                    Log.e("IIM", "stack trace", e);
                }
                switch(v.getId()) {
                    case R.id.share_button:
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(currentImageFile));
                        shareIntent.setType("image/*");
                        startActivity(Intent.createChooser(shareIntent, "Share this image to..."));
                        break;
                    case R.id.facebook_button:
                        facebookShareImage(BitmapFactory.decodeFile(currentImageFile.getPath()));
                        break;
                    case R.id.twitter_button:
                        twitterShareStatus(currentImageFile);
                        break;
                    case R.id.instagram_button:
                        instagramShareImage("image/*", currentImageFile);
                        break;

                }
            }
        };


        // General Share button
        ImageButton shareButton = (ImageButton) layout.findViewById(R.id.share_button);
        shareButton.setOnClickListener(shareButtonListener);
        // Sharing to facebook
        ImageButton facebookButton = (ImageButton) layout.findViewById(R.id.facebook_button);
        facebookButton.setOnClickListener(shareButtonListener);

        // Sharing to Twitter
        ImageButton twitterButton = (ImageButton) layout.findViewById(R.id.twitter_button);
        twitterButton.setOnClickListener(shareButtonListener);

        // Sharing to Instagram
        ImageButton instagramButton = (ImageButton) layout.findViewById(R.id.instagram_button);
        instagramButton.setOnClickListener(shareButtonListener);


        ImageButton detectFaces = (ImageButton) layout.findViewById(R.id.detect_faces_button);
        detectFaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //todo: action bar does not reappear after button clicked
                ImagePageFragment fragment = (ImagePageFragment) adapter.posFragmentMap.get(position);
                if (fragment == null) { return;}

                final ImageView fullImg = fragment.fullImg;
                final ImageView faceImg = (TouchImageView) layout.findViewById(R.id.face_img);

                // only face detect if image is done loading
                if (fullImgDoneLoading(fullImg)) {

                    Bitmap b = ((BitmapDrawable) fullImg.getDrawable()).getBitmap();
                    if (b.getWidth() % 2 == 1) {
                        b = Bitmap.createBitmap(b, 0, 0, b.getWidth() - 1, b.getHeight());
                    }

                    final Bitmap mFaceBitmap = b.copy(Bitmap.Config.RGB_565, true);
                    final Bitmap mFaceBitmap1 = b.copy(Bitmap.Config.RGB_565, true);

                    final float imgWidth = mFaceBitmap.getWidth();
                    final float imgHeight = mFaceBitmap.getHeight();
                    Log.d("faceDetect", "width: " + String.valueOf(imgWidth) + " height: " + String.valueOf(imgHeight));
                    final FaceDetect faceDetect = new FaceDetect(20, mFaceBitmap);
                    final List<FaceDetector.Face> faces = faceDetect.detectFaces();
                    Log.d("FacesNumber", String.valueOf(faces.size()));
                    faceDetect.drawFaces(mFaceBitmap, faces);
                    fullImg.setImageBitmap(mFaceBitmap);

                    // ---------------------------------------- FaceClickStuff----------------------

                    fullImg.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            ImageView imageView = (ImageView) v;

                            // get image matrix values and place in array
                            float[] f = new float[9];
                            imageView.getImageMatrix().getValues(f);

                            //get scale constants from matrix
                            final float scaleX = f[Matrix.MSCALE_X];
                            final float scaleY = f[Matrix.MSCALE_Y];

                            //multiply scale constants by the original height/width
                            float realWidth = imgWidth * scaleX;
                            float realHeight = imgHeight * scaleY;


                            Log.v("OnTouch", "motion event " + event.toString());
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                Log.d("EventX", String.valueOf(event.getX()));
                                Log.d("EventY", String.valueOf(event.getY()));
                                Log.d("ViewLeft", String.valueOf(v.getLeft()));
                                Log.d("ViewTop", String.valueOf(v.getTop()));
                                Log.d("ImgWidth", Float.toString(realWidth));
                                Log.d("ImgHeight", Float.toString(realHeight));
                                Log.d("ViewHeight", Float.toString(v.getHeight()));
                                Log.d("ViewWidth", Float.toString(v.getWidth()));
                                float diffY = (v.getHeight() - realHeight) / 2;
                                float diffX = (v.getWidth() - realWidth) / 2;
                                Log.d("DiffX", Float.toString(diffX));
                                Log.d("DiffY", Float.toString(diffY));
                                float viewX = event.getX() - diffX;
                                float viewY = event.getY() - diffY;
                                for (FaceDetector.Face face : faces) {


                                    if (withinBounds(viewX, viewY, face, scaleX, scaleY)) {
                                        Log.d("OnTouch", "touched within the picture");
                                        PointF midpoint = new PointF();
                                        face.getMidPoint(midpoint);
                                        float pointX = midpoint.x + v.getLeft();
                                        float pointY = midpoint.y + v.getTop();
                                        float eyeDistance = face.eyesDistance();
                                        int firstX = Math.round(midpoint.x - eyeDistance * 2);
                                        int firstY = Math.round(midpoint.y - eyeDistance * 2);

                                        if (firstX < 0) {
                                            firstX = 0;
                                        }
                                        if (firstY < 0) {
                                            firstY = 0;
                                        }

                                        Bitmap personFace = CropFace(mFaceBitmap1, firstX, firstY, Math.round(eyeDistance * 4));
                                        // enlarge the face
                                        fullImg.setImageBitmap(personFace);
                                        fullImg.setOnTouchListener(null);
                                        editText.setVisibility(View.GONE);
                                        keywordContainer.setVisibility(View.GONE);

                                        //TODO: zoom effect
                                        faceImg.setImageBitmap(personFace);

                                        //final Rect startBounds = new Rect()




                                    }
                                }
                            }
                            return true;
                        }
                    });

                    // ------------------------------------------------------




                }
            }
        });

        //actionBar.hide();
        //slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        return layout;
    }


    public class textViewOnTouchListener implements View.OnTouchListener {

        Context context;
        KeywordNode keywordNode;
        File imageFile;

        textViewOnTouchListener(Context context, KeywordNode keywordNode, File imageFile) {
            this.context = context;
            this.keywordNode = keywordNode;
            this.imageFile = imageFile;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                //Log.v("OnTouch", "motion event " + event.toString());
                Rect rect = new Rect();
                keywordNode.textView.getGlobalVisibleRect(rect);
                //rect.right should be the right side of the text part (excludes x drawable)
                Log.v("OnTouch","event raw x " + event.getRawX() + " edit text right " + rect.right);
                Log.v("OnTouch","event x " + event.getX() + " view width " + v.getWidth());
                if (event.getRawX() >= (rect.right - keywordNode.textView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()) ) {
                    Log.v("OnTouch", "clicked the X");

                    YoYo.with(Techniques.FadeOut).duration(80).withListener(new com.nineoldandroids.animation.Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
                            removeKeyword();
                        }

                        @Override
                        public void onAnimationCancel(com.nineoldandroids.animation.Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(com.nineoldandroids.animation.Animator animation) {

                        }
                    }).playOn(keywordNode.textView);

                    return false; //consume event
                }
            }
            return true;
        }

        /* TODO: currently the remove and add keyword functions are spread out all over the place
                In the future, somehow group them
        */

        private boolean removeKeyword() {
            try {

                if (keywordNode .equals( latest) ){
                    latest = keywordNode.parent;
                    if (latest != null && keywordNode.isRowStart) {
                        while (latest.childRight != null) {
                            latest = latest.childRight;
                        }
                    }
                }

                //find the start of the row keywordNode is in, after it is removed
                KeywordNode rowStart = null;
                if (keywordNode.isRowStart ) {
                    if (keywordNode.childRight != null) {
                        rowStart = keywordNode.childRight;
                    }
                } else { //parent != null
                    rowStart = keywordNode;
                    while(!rowStart.isRowStart) {
                        rowStart = rowStart.parent;
                    }
                }

                String keyword = keywordNode.textView.getText().toString();
                IIMUtility.removeKeyword(context, imageFile, keyword); //remove keyword from image
                activity.helper.removeKeywordfromDatabaseEntry(keyword, imageFile.getAbsolutePath()); //remove from database
                keywordNode.destroy(); // call this last, remove from relative layout

                if (rowStart != null) {
                    moveNodesUp(rowStart);
                }
            } catch (Exception e) {
                Log.e("PhotosFragment", "Stack trace: ", e);
            }
            return false;
        }
    }

    public class EditTextOnTouchListener implements View.OnTouchListener {

        EditText editText;
        RelativeLayout keywordContainer;

        EditTextOnTouchListener(EditText editText, RelativeLayout keywordContainer) {
            this.editText = editText;
            this.keywordContainer = keywordContainer;
        }
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                Log.v("OnTouch", "motion event " + event.toString());
                editText.setCursorVisible(true);

                Rect rect = new Rect();
                editText.getGlobalVisibleRect(rect);
                showSoftKeyboard(v);

                Log.v("OnTouch", "event raw x " + event.getRawX() + " edit text right " + rect.right);
                if (event.getRawX() >= (rect.right) - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()) {
                    Log.v("OnTouch", "clicked the +");
                    addKeyword(editText,keywordContainer,currentImageFile);
                }
                return false;
            }
            return true;
        }
    }

    private void addKeyword(EditText editText, RelativeLayout keywordContainer, File imageFile) {
        String keyword = editText.getText().toString();
        editText.setText(null);
        if (!keyword.equals("")) {
            try {
                if (IIMUtility.addKeyword(activity, imageFile, keyword)) {
                    addKeywordTextView(keyword, keywordContainer, imageFile);
                    //we know that image is already in database
                    activity.helper.addKeywordtoDatabaseEntry(keyword, imageFile.getAbsolutePath());

                    //scroll down so user can see the last keyword they added
                    final MyScrollView scroll = (MyScrollView) keywordContainer.getParent();
                    scroll.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            scroll.fullScroll(View.FOCUS_DOWN);
                        }
                    }, 40);
                }
            } catch (Exception e) {
                Log.e("PhotosFragment", "Stack trace: ", e);
            }
        }
    }

    public class MyEditText extends EditText {

        MyEditText(Context context) {
            super(context);
        }

        MyEditText(Context context, AttributeSet attrs) {
            super(context, attrs);

        }

        /*
        //todo: sometimes back button press happens twice
        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                hideSoftKeyboard(this);
            } else {
                super.onKeyPreIme(keyCode, event);
            }
            return false;

        }
        */
    }

    private KeywordNode findExtraBlockStart(KeywordNode rowStart) {
        KeywordNode curr = rowStart;
        TextView currentText = curr.textView;
        int innerMargin = rowStart.margin;
        RelativeLayout container = (RelativeLayout) rowStart.textView.getParent();
        int containerPadding = container.getPaddingStart();
        int containerWidth = container.getWidth();
        int sumWidth = 2*containerPadding + currentText.getMeasuredWidth();

        while (sumWidth <= containerWidth && curr.childRight != null) {
            sumWidth += (curr.childRight.textView.getMeasuredWidth() + innerMargin);
            curr = curr.childRight;
        }
        if (sumWidth > containerWidth) {
            return curr;
        } else {
            return null;
        }
    }

    private void moveOverBlocks(KeywordNode rowStart) {
        while (rowStart != null) {
            // blockStart is the starting node of the block to be moved
            KeywordNode blockStart = findExtraBlockStart(rowStart);
            //the first keyword node should never be moved ie rowStart != blockStart
            if (blockStart != null) {
                //blockEnd is the end node of the block to be moved
                KeywordNode blockEnd = blockStart;
                while (blockEnd.childRight != null) {
                    blockEnd = blockEnd.childRight;
                }
                //handle old parent of blockStart (definitely exists)
                blockStart.parent.childRight = null;
                blockStart.parent.update();

                //handle new childBelow of blockStart
                blockStart.isRowStart = true;
                blockStart.parent = rowStart;
                if (blockStart.childBelow != null) {
                    blockStart.childBelow = rowStart.childBelow.childBelow;
                    blockStart.childBelow.parent = blockStart;
                    blockStart.childBelow.update();
                }
                blockStart.update();

                //handle new childRight of blockEnd
                blockEnd.childRight = blockStart.parent.childBelow;
                if (blockEnd.childRight != null) {
                    blockEnd.childRight.isRowStart = false;
                    blockEnd.childRight.parent = blockEnd;
                    blockEnd.childRight.update();
                }
                blockEnd.update();

                //handle new parent of blockStart (definitely exists)
                blockStart.parent.childBelow = blockStart;
                blockStart.parent.update();

            }
            rowStart = blockStart;
        }
    }


    private void moveExtraDown(KeywordNode rowStart) {
        while (rowStart != null) {
            KeywordNode blockStart = findExtraBlockStart(rowStart);
            //the first keyword node should never be moved ie rowStart != blockStart
            if (blockStart != null) {
                //blockEnd is the end node of the block to be moved
                KeywordNode blockEnd = blockStart;
                while (blockEnd.childRight != null) {
                    blockEnd = blockEnd.childRight;
                }

                moveBlock(blockStart,blockEnd,rowStart.childBelow,true);
            }
            rowStart = blockStart;
        }
    }

    //do not target the same row , especially not itself;
    //if not attachToStart, then attachToEnd

    //todo: this is still broken if attachToStart == true (and elsewhere)
    private void moveBlock(KeywordNode blockStart, KeywordNode blockEnd, KeywordNode target, boolean attachToStart ) {
        //handle old parent of blockStart
        if (blockStart.isRowStart) {
            if (blockStart.parent != null) {

                KeywordNode replacement = null;
                if (blockEnd.childRight != null) {
                    replacement = blockEnd.childRight;
                    if (blockStart.childBelow != null) {
                        // handle child below if it is not the replacement
                        replacement.childBelow = blockStart.childBelow;
                        blockStart.childBelow.parent = replacement;
                        blockStart.childBelow.update();
                    }
                } else if (blockStart.childBelow != null) {
                    replacement = blockStart.childBelow;
                }
                blockStart.parent.childBelow = replacement;
                blockStart.parent.update();

                //handle replacement
                if (replacement != null) {
                    replacement.parent = blockStart.parent;
                    replacement.isRowStart = true;
                    replacement.update();
                }

            }
        } else {
            blockStart.parent.childRight = blockEnd.childRight;
            blockStart.parent.update(); //parent must exist
        }

        if (attachToStart) {
            // handle blockEnd
            blockEnd.childRight = target;
            blockEnd.update();
            // handle blockStart
            blockStart.parent = target.parent;
            if (target.isRowStart) {
                blockStart.childBelow = target.childBelow;
                blockStart.isRowStart = true;
            }
            blockStart.update();

            //handle new parent of blockstart
            if (blockStart.parent != null) {
                if (target.isRowStart) {
                    blockStart.parent.childBelow = blockStart;
                } else {
                    blockStart.parent.childRight = blockStart;
                }
                blockStart.parent.update();
            }

            //handle target
            target.parent = blockEnd;
            target.isRowStart = false;
            target.update();

        } else {

            blockStart.parent = target;
            blockStart.isRowStart = false;
            blockStart.update();

            blockEnd.childRight = target.childRight;
            blockEnd.update();

            target.childRight = blockStart;
            target.update();

            if (blockEnd.childRight != null) {
                blockEnd.childRight.parent = blockEnd;
                blockEnd.childRight.update();
            }
        }
    }

    //todo maybe combine these 2 function groups ^, into a more generic one
    //this finds the rightmost block that can be added to the target row while keeping under max width
    private KeywordNode findBlockEnd(KeywordNode targetRowStart, KeywordNode senderRowStart) {
        KeywordNode curr = targetRowStart;
        TextView currentText = curr.textView;
        int innerMargin = targetRowStart.margin;
        RelativeLayout container = (RelativeLayout) targetRowStart.textView.getParent();
        int containerPadding = container.getPaddingStart();
        int containerWidth = container.getWidth();
        int sumWidth = 2*containerPadding + currentText.getMeasuredWidth();

        //find width of target row
        while (sumWidth <= containerWidth && curr.childRight != null) {
            sumWidth += (curr.childRight.textView.getMeasuredWidth() + innerMargin);
            curr = curr.childRight;
        }
        //try adding the width of the first node of sender row
        curr = senderRowStart;
        sumWidth += curr.textView.getMeasuredWidth() + innerMargin;
        if (sumWidth > containerWidth) {
            return null; //if the target row cannot take at least one node, return null
        }

        //try adding the width of next node
        while (sumWidth <= containerWidth && curr.childRight != null) {
            sumWidth += (curr.childRight.textView.getMeasuredWidth() + innerMargin);
            curr = curr.childRight;
        }
        //stop when you run out of nodes or the latest node made the width too big
        if (sumWidth > containerWidth) {
            return curr.parent; //return the node before the latest if the latest went over
        } else {
            return curr; // or return the latest if the width did not go over
        }
    }

    //this moves any nodes up that can be moved while staying under the max width,
    // starting from the target row and going down
    private void moveNodesUp(KeywordNode targetRowStart) {
        KeywordNode senderRowStart = targetRowStart.childBelow;
        if (senderRowStart != null) {
            KeywordNode blockEnd = findBlockEnd(targetRowStart, senderRowStart);
            if (blockEnd != null) {

                KeywordNode blockStart = blockEnd;
                while (!(blockStart.isRowStart)) {
                    blockStart = blockStart.parent;
                }

                KeywordNode targetRowEnd = targetRowStart;
                while (targetRowEnd.childRight != null) {
                    targetRowEnd = targetRowEnd.childRight;
                }

                moveBlock(blockStart, blockEnd, targetRowEnd, false);

                if (targetRowStart.childBelow != null) {
                    moveNodesUp(targetRowStart.childBelow);
                }
            }
        }
    }

    //todo: fix bug - sometimes crashes when adding (probably related to the ongloballayout part)
    public void addKeywordTextView(String keyword, final RelativeLayout container, final File imageFile) {

        final TextView newText = createKeywordTextView(activity, keyword);
        container.addView(newText);

        newText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                newText.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
                int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, metrics); // 8dp margin

                KeywordNode newNode;

                if (latest == null) {
                    newNode = new KeywordNode(newText,margin);
                } else {
                    newNode = new KeywordNode(newText,latest,false,margin);

                    KeywordNode rowStart = latest;
                    while (!rowStart.isRowStart) {
                        rowStart = rowStart.parent;
                    }
                    moveOverBlocks(rowStart);

                }
                newNode.textView.setOnTouchListener(new textViewOnTouchListener(activity, newNode, imageFile));
                latest = newNode;

                newText.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn).duration(80).playOn(newText);
            }
        });
    }

    private void populateKeywordContainer() {
        try {
            //all contents of grid view are image views
            ArrayList<String> keywords = IIMUtility.getKeywords(currentImageFile);

            for (int i = 0; i < keywords.size(); i++) {
                addKeywordTextView(keywords.get(i), keywordContainer, currentImageFile);
            }
        } catch (Exception e) {
            Log.e("PhotosFragment", "Stack trace: ", e);
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent event){
        hideSoftKeyboard(getView());
        return true;

    }
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e){

        // change action bar/ panel hidden state

        if(actionBar.isShowing()){
            actionBar.hide();
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        }else{
            actionBar.show();
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        return true;
    }



    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
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


    public TextView createKeywordTextView(Activity activity, String keyword) {

        LayoutInflater inflater = activity.getLayoutInflater();
        TextView textView = (TextView) inflater.inflate(R.layout.keyword_text_view, null);
        textView.setText(keyword);

        // scale the drawable
        Resources resources = getResources();
        BitmapDrawable drawableFull = (BitmapDrawable) resources.getDrawable(R.drawable.ic_action_cancel, null);
        Bitmap bitmap = drawableFull.getBitmap();
        BitmapDrawable drawableScaled = new BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 60, 60, true));
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawableScaled, null);

        return textView;
    }

    // this implementation is reliant on placeholder image not being a bitmap
    public boolean fullImgDoneLoading(ImageView fullImg) {
        return fullImg.getDrawable() instanceof BitmapDrawable;
    }

    private Bitmap CropFace(Bitmap mFaceBitmap, int firstX, int firstY, int v) {
        int distX = v;
        int distY = v;
        if (firstY + v > mFaceBitmap.getHeight()){
            distY = mFaceBitmap.getHeight() - firstY;
        }
        if(firstX + v > mFaceBitmap.getWidth()){
            distX = mFaceBitmap.getWidth() - firstX;
        }

        return Bitmap.createBitmap(mFaceBitmap, firstX, firstY, distX, distY);
    }

    private boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {

            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void instagramShareImage(String type, File imageFile) {
        final String INSTAGRAM_PACKAGE = "com.instagram.android";
        if (isPackageInstalled(INSTAGRAM_PACKAGE, activity)) {
            // Create the new Intent using the 'Send' action.
            Intent share = new Intent(Intent.ACTION_SEND);
            share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Set the MIME type
            share.setType(type);

            // Create the URI from the media
            Uri uri = Uri.fromFile(imageFile);

            // Add the URI and the caption to the Intent.
            share.putExtra(Intent.EXTRA_STREAM, uri);

            ArrayList<String> keywords = null;
            try {
                keywords = IIMUtility.getKeywords(imageFile);
            } catch (Exception e) {
                Log.e("InstagramShare", "stack trace", e);
            }

            if (keywords != null) {
                String caption = "";
                for (String keyword : keywords) {
                    caption = caption + "#" + keyword + " ";
                    Log.d("Instagram", keyword);
                }
                share.putExtra(Intent.EXTRA_TEXT, caption);
            }

            share.setPackage(INSTAGRAM_PACKAGE);

            // Broadcast the Intent.
            startActivity(share);
        } else {
            Toast.makeText(activity, "Instagram is not installed", Toast.LENGTH_SHORT)
                    .show();
        }
    }


    // Method for sharing to facebook
    public void facebookShareImage(Bitmap image) {
        final String FACEBOOK_PACKAGE = "com.facebook.katana";
        if (isPackageInstalled(FACEBOOK_PACKAGE, activity)) {
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(image)
                    .build();
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build();
            ShareDialog.show(activity, content);
        } else {
            Toast.makeText(activity, "Facebook is not installed", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void twitterShareStatus(File file) {
        ArrayList<String> keywords;
        StringBuilder hashtags = new StringBuilder();
        try {
            keywords = IIMUtility.getKeywords(file);

            for (String s : keywords) {
                hashtags.append("#").append(s).append(" ");
            }
            TweetComposer.Builder builder = new TweetComposer.Builder(activity)
                    .text(hashtags.toString())
                    .image(Uri.fromFile(file));
            builder.show();
        } catch (IIMNotFoundException e) {
            TweetComposer.Builder builder = new TweetComposer.Builder(activity)
                    .image(Uri.fromFile(file));
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean withinBounds(float inX, float inY, FaceDetector.Face face, float scaleX, float scaleY) {
        PointF midpoint = new PointF();
        face.getMidPoint(midpoint);
        float eyeDistance = face.eyesDistance() * scaleX;
        float x = midpoint.x * scaleX;
        float y = midpoint.y * scaleY;
        /*
        Log.d("Touch X", String.valueOf(inX));
        Log.d("Touch Y", String.valueOf(inY));
        Log.d("Boundary Left", String.valueOf((x - eyeDistance * 2)));
        Log.d("Boundary Right", String.valueOf((x + eyeDistance * 2)));
        Log.d("Boundary Top", String.valueOf((y - eyeDistance * 2)));
        Log.d("Boundary Bottom", String.valueOf((y + eyeDistance * 2)));
        Log.d("Truth Value", String.valueOf((inX >= (x - eyeDistance * 2) &&
                inY >= (y - eyeDistance * 2) &&
                inX <= (x + eyeDistance * 2) &&
                inY <= (y + eyeDistance * 2))));
        */

        return (inX >= (x - eyeDistance * 2) &&
                inY >= (y - eyeDistance * 2) &&
                inX <= (x + eyeDistance * 2) &&
                inY <= (y + eyeDistance * 2));
    }

    /**
     * Hides the soft keyboard
     */

    public void hideSoftKeyboard(View view ) {
        activity.hideSoftKeyboard(view);
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard(View view) {

        activity.showSoftKeyboard(view);
    }

    public ImagePagerAdapter getAdapter(){
        return adapter;
    }

    public int getPosition(){
        return position;
    }
}
