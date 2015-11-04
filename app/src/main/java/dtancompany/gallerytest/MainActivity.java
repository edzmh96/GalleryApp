package dtancompany.gallerytest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.FacebookSdk;
import com.nineoldandroids.animation.Animator;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends FragmentActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, SwipeRefreshLayout.OnRefreshListener, RemoveIIMDialogFragment.NoticeDialogListener{

    protected PhotosFragment currentFragment;
    ArrayList<String> images;
    Drawable fullPlaceholder;
    boolean openFromHere = true;

    Activity activity;
    HashSet<File> selected = new HashSet<>();
    int imageSize;
    SearchAdapter searchAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    KeywordSearchDbHelper helper;
    DatabaseTask task;
    private static final int CONTENT_VIEW_ID = 10101010;

    private static final int MAX_WIDTH = 1024; //arbitrarily picked
    private static final int MAX_HEIGHT = 768;
    final int SHORT_ANIM_DURATION = 200;
    final String THEME_COLOR = "THEME_COLOR";
    int color50;
    int color100;
    int color500;
    int color500clear;
    int color2transparent;

    private Set<String> suggestions ;
    int spaceSize;
    LayoutInflater inflater;
    View selectLayout;
    View searchLayout;
    GridView gridView;
    Drawable placeholder;
    ColorDrawable cyan;
    ColorDrawable onPressedColor;
    View root;
    PorterDuffColorFilter filter;
    SlidingUpPanelLayout slidingUpPanelLayout;
    ImageButton addKeywordButton;
    EditText addKeywordField;
    ImageButton addKeywordPrompt;
    ImageButton removeKeywordButton;
    ImageButton shareButton;
    ActionBar actionBar;
    AdapterView.OnItemClickListener selectModeItemClickListener;
    boolean selectMode;
    boolean searched;
    AutoCompleteTextView textView;
    View selectedViewOfGridView;
    View fragmentView;
    boolean isRight;
    TextView numberSelected;
    boolean singleKeywordSearch;
    int chosenThemeColor;
    SharedPreferences settings;
    TextView searchItem;
    RelativeLayout searchItemLayout;
    RelativeLayout addKeywordPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        activity = MainActivity.this;
        inflater = getLayoutInflater();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        */


        // Gets keywords with asynctask


        activity = this;
        //get Theme color, default is 0(cyan)
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        chosenThemeColor = settings.getInt(THEME_COLOR, 0);
        Log.d("Preferences", String.valueOf(chosenThemeColor));

        if(settings.getBoolean("first_time", true)){
            Log.d("TEST", "first time");

            settings.edit().putBoolean("first_time", false).commit();
        }

        inflater = getLayoutInflater();
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
        root = inflater.inflate(R.layout.activity_main, null, false);
    //    root.setBackgroundColor(color50);
        setContentView(root);
        selectMode = false;
        searched = false;

        gridView = (GridView) findViewById(R.id.grid_view);
     //   gridView.setBackgroundColor(color50);

        //Finds the sliding panel
        slidingUpPanelLayout = (SlidingUpPanelLayout) root.findViewById(R.id.sliding_add_panel);
        // Sets panel listener
        slidingUpPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {

            }

            @Override
            public void onPanelCollapsed(View view) {
                hideSoftKeyboard(root);
                gridView.setOnItemClickListener(selectModeItemClickListener);
                gridView.setOnTouchListener(null);
            }


            @Override
            public void onPanelExpanded(View view) {
                gridView.setOnItemLongClickListener(null);
                gridView.setOnItemClickListener(null);
                gridView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                        return true;
                    }
                });
                showSoftKeyboard(addKeywordField);
            }

            @Override
            public void onPanelAnchored(View view) {

            }

            @Override
            public void onPanelHidden(View view) {

            }
        });

        //Finds the necessary panel views
        addKeywordPanel = (RelativeLayout) slidingUpPanelLayout.findViewById(R.id.add_keyword_panel);
        addKeywordButton = (ImageButton) slidingUpPanelLayout.findViewById(R.id.add);
        addKeywordButton.setOnClickListener(this);
        addKeywordField = (EditText) slidingUpPanelLayout.findViewById(R.id.edit_text);
        setFilters(addKeywordField);
        actionBar = getActionBar();


        FacebookSdk.sdkInitialize(getApplicationContext());
        TwitterAuthConfig authConfig = new TwitterAuthConfig("consumerKey", "consumerSecret");
        Fabric.with(this, new Twitter(authConfig));
        inflater = getLayoutInflater();

        images = getAllShownImagesPath(activity);
        Collections.reverse(images);

        try {
            task = (DatabaseTask) new DatabaseTask().execute(new DatabaseTaskParams(activity, images));
            helper = task.get();
        } catch (Exception e) {
            Log.e("DatabaseTask", "stack trace", e);
        }

        suggestions = helper.getKeywords();




        selected.clear();

        searchItemLayout = (RelativeLayout) inflater.inflate(R.layout.search_item, null);
      //  searchItemLayout.setBackgroundColor(color500);
        searchItem = (TextView) searchItemLayout.findViewById(R.id.search_item);
      //  searchItem.setBackgroundColor(color500);
        searchItem.setOnClickListener(this);
        //Sets gray on image click filter
        filter = new PorterDuffColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.MULTIPLY);

        //Sets image size/space sizes for gridview
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        imageSize = (int) (displayMetrics.widthPixels / displayMetrics.density); //(image size is in dips)
        final int MIN_PIXELS = 2;
        spaceSize = (imageSize % 3)/2 + MIN_PIXELS;
        imageSize = (imageSize - MIN_PIXELS*2)/3; //16dp is padding
        imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, imageSize,
                displayMetrics); //(image size now in pixels)
        spaceSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, spaceSize,
                displayMetrics); //(space width now in pixels)

        //sets adapters/listeners for the gridview
        gridView.setAdapter(new ImageAdapter(this));
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);
        gridView.setColumnWidth(imageSize);
        gridView.setNumColumns(3);
        gridView.setHorizontalSpacing(spaceSize);
        gridView.setVerticalSpacing(spaceSize);



        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO
                        | ActionBar.DISPLAY_SHOW_HOME
        );
        selectLayout = inflater.inflate(R.layout.select_mode_actionbar, null);
        searchLayout = inflater.inflate(R.layout.search_box, null);

        addKeywordPrompt = (ImageButton) selectLayout.findViewById(R.id.add_keyword_button);
        addKeywordPrompt.setOnClickListener(this);
        removeKeywordButton = (ImageButton) selectLayout.findViewById(R.id.remove_iim_button);
        removeKeywordButton.setOnClickListener(this);
        shareButton = (ImageButton) selectLayout.findViewById(R.id.multi_share);
        shareButton.setOnClickListener(this);


        textView = (AutoCompleteTextView) searchLayout.findViewById(R.id.search_box);
        textView.setFocusable(true);
        textView.setClickable(true);
        textView.setFocusableInTouchMode(true);
        textView.setEnabled(true);



        searchAdapter = new SearchAdapter(this, android.R.layout.simple_list_item_1, suggestions);
        textView.setAdapter(searchAdapter);

        actionBar.setCustomView(searchLayout);

        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("AutoComplete", "touched");
                AutoCompleteTextView view = (AutoCompleteTextView) v;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (view.getCompoundDrawables()[2] != null) {
                        if (event.getX() > view.getWidth() - view.getPaddingRight() - view.getCompoundDrawables()[2].getIntrinsicWidth()) {
                            toggleSearch(true, view);
                            Log.d("AutoComplete", "touched the x");
                        } else {
                            toggleSearch(false, view);
                            Log.d("AutoComplete", "touched the searchbar");
                        }
                    } else {
                        toggleSearch(false, view);
                        Log.d("AutoComplete", "touched the searchbar");
                    }
                }
                return true;
            }
        });

        textView.setOnKeyListener(enterKeyListener(textView, gridView));
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        fragmentView = inflater.inflate(R.layout.fragment_photos, null, false);
        numberSelected = (TextView) selectLayout.findViewById(R.id.number_selected);

        //new LoadViewTask().execute();
        // set the colors
        setThemeColors(chosenThemeColor);
        onPressedColor.setColorFilter(filter);





    }

    private void setThemeColors(int i) {
        Log.d("Preferences", "theme value is " + String.valueOf(i));
        switch(i){
            //cyan
            case 0:
                color50 = getResources().getColor(R.color.cyan50);
                color100 = getResources().getColor(R.color.cyan100);
                color500 = getResources().getColor(R.color.cyan500);
                color500clear = getResources().getColor(R.color.cyan500clear);
                color2transparent = getResources().getColor(R.color.cyan2transparent);
                textView.setDropDownBackgroundResource(R.drawable.cyan500drawable);
                break;

            //indigo
            case 1:
                color50 = getResources().getColor(R.color.indigo50);
                color100 = getResources().getColor(R.color.indigo100);
                color500 = getResources().getColor(R.color.indigo500);
                color500clear = getResources().getColor(R.color.indigo500clear);
                color2transparent = getResources().getColor(R.color.indigo2transparent);
                textView.setDropDownBackgroundResource(R.drawable.indigo500drawable);
                break;
            //red
            case 2:
                color50 = getResources().getColor(R.color.red50);
                color100 = getResources().getColor(R.color.red100);
                color500 = getResources().getColor(R.color.red500);
                color500clear = getResources().getColor(R.color.red500clear);
                color2transparent = getResources().getColor(R.color.red2transparent);
                textView.setDropDownBackgroundResource(R.drawable.red500drawable);
                break;
            //amber
            case 3:
                color50 = getResources().getColor(R.color.amber50);
                color100 = getResources().getColor(R.color.amber100);
                color500 = getResources().getColor(R.color.amber500);
                color500clear = getResources().getColor(R.color.amber500clear);
                color2transparent = getResources().getColor(R.color.amber2transparent);
                textView.setDropDownBackgroundResource(R.drawable.amber500drawable);
                break;

            //orange
            case 4:
                color50 = getResources().getColor(R.color.orange50);
                color100 = getResources().getColor(R.color.orange100);
                color500 = getResources().getColor(R.color.orange500);
                color500clear = getResources().getColor(R.color.orange500clear);
                color2transparent = getResources().getColor(R.color.orange2transparent);
                textView.setDropDownBackgroundResource(R.drawable.orange500drawable);
                break;

            //black
            case 5:
                color50 = getResources().getColor(R.color.black50);
                color100 = getResources().getColor(R.color.black100);
                color500 = getResources().getColor(R.color.black500);
                color500clear = getResources().getColor(R.color.black500clear);
                color2transparent = getResources().getColor(R.color.black2transparent);
                textView.setDropDownBackgroundResource(R.drawable.black500drawable);
                break;

        }

        //sets the colors for every item after colors are loaded
        root.setBackgroundColor(color50);
        gridView.setBackgroundColor(color50);
        addKeywordField.setTextColor(color50);
        addKeywordField.setHintTextColor(color50);
        searchItemLayout.setBackgroundColor(color500);
        searchItem.setBackgroundColor(color500);
        placeholder = new ColorDrawable(color100);
        cyan = new ColorDrawable(color500);
        onPressedColor = new ColorDrawable(color500);
        textView.setTextColor(color50);
        textView.setHintTextColor(color50);
        textView.setHighlightColor(color50);
        textView.setLinkTextColor(color50);
        numberSelected.setTextColor(color50);
        actionBar.setBackgroundDrawable(new ColorDrawable(color500));
        addKeywordPanel.setBackgroundColor(color500);
        if(currentFragment != null){
            currentFragment.actionBar.setBackgroundDrawable(new ColorDrawable(color500clear));
            //currentFragment.getView().refreshDrawableState();
            ((ImagePageFragment) currentFragment.getAdapter().getItem(currentFragment.getPosition())).fullImg.setBackgroundColor(color50);
            currentFragment.panelRoot.setBackgroundColor(color100);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //Hides the panel if its open
        if(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }
        switch(id) {
            case R.id.action_rebuild_database:
                //gets images/keywords again

                //regular dialog
               /* final AlertDialog.Builder buildr = new AlertDialog.Builder(this);
                buildr.setTitle("Rebuilding Database");
                final AlertDialog dialog = buildr.create();
                dialog.setCancelable(false);
                dialog.show();*/

                final ProgressDialog dialog = ProgressDialog.show(this, "Rebuilding Database", "Reading Image Keywords...", true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        helper.rebuildDatabase();
                        // dismiss
                        //TODO: maybe make it so that it displays number of images read
                        dialog.dismiss();
                        //For changing background during update
                        /*MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        });*/
                    }
                }).start();
                //helper.rebuildDatabase();
                break;

            case R.id.action_choose_theme:
                //Choses theme color
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.choose_color_dialog)
                        .setSingleChoiceItems(R.array.theme_color_choices, chosenThemeColor, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                chosenThemeColor = which;
                                settings.edit().remove(THEME_COLOR).putInt(THEME_COLOR, which).commit();
                                setThemeColors(which);
                            }
                        });
                builder.create();
                builder.show();
                break;


            case android.R.id.home:
                if(actionBar.getCustomView().equals(selectLayout)) {
                    endItemSelectMode();
                }else if(getFragmentManager().getBackStackEntryCount() != 0){
                    returnToMain();
                }else if(searched){
                    returnFromSearchResults();
                }
                gridView.refreshDrawableState();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //UI option setting when you return from search results
    public void returnFromSearchResults(){
        searched = false;
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |  ActionBar.DISPLAY_USE_LOGO);
        updateGridView(gridView);
        toggleSearch(true, textView);
    }

    //UI options/ animations when you return to main screen
    public void returnToMain(){
        getFragmentManager().popBackStack();
        YoYo.with(Techniques.ZoomIn).duration(SHORT_ANIM_DURATION).playOn(gridView);
        if (!searched) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
        } else {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME);
        }
        actionBar.setBackgroundDrawable(cyan);
        if(!actionBar.isShowing()){ actionBar.show(); }
        //root.setPadding(0, actionBar.getHeight(), 0, 0);
        actionBar.setCustomView(searchLayout);
        gridView.setOnItemLongClickListener((AdapterView.OnItemLongClickListener) activity);
        gridView.setOnItemClickListener((AdapterView.OnItemClickListener) activity);

        updateFilter();

        Log.d("FragmentTransaction", String.valueOf(getFragmentManager().getBackStackEntryCount()));

    }


    //Refreshing updates database
    @Override
    public void onRefresh() {



        final Thread refresh = new Thread(new Runnable(){
            public void run(){
                helper.updateDatabase();
                updateFilter();
                selected.clear();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateGridView(gridView);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

            }

        });

        refresh.start();
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh.start();
            }
        }, 1000);*/


        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateGridView(gridView);
                helper.updateDatabase();
                updateFilter();
                selected.clear();
            }
        }, 1000);*/






    }


    public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e("MainActivity", "stack trace", ex);
        }
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Log.i("MainActivityFragment", "external storage is mounted");
            return true;
        }
        else {
            Log.i("MainActivityFragment", "external storage is not mounted");
            return false;
        }
    }

    @Override
    public void onBackPressed(){
        if(getFragmentManager().getBackStackEntryCount()!= 0){
            returnToMain();
            openFromHere = true;
        }else if(selectMode) {
            endItemSelectMode();
        }else if(searched){
            returnFromSearchResults();
        }else{
            finish();
        }
    }



    /*@Override
    public void onStart(){
        super.onStart();
        try {
            task = (DatabaseTask) new DatabaseTask().execute(new DatabaseTaskParams(activity, images));
            helper = task.get();
        } catch (Exception e) {
            Log.e("DatabaseTask", "stack trace", e);
        }
    }*/

    /*@Override
    public void onStop() {
        super.onStop();
        helper.close();
    }*/

    @Override
    public void onDestroy(){
        super.onDestroy();
        helper.close();
    }


    // LISTENERS
    private View.OnKeyListener enterKeyListener(final AutoCompleteTextView textView, final GridView gridView){
        View.OnKeyListener onKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    if(keyCode == KeyEvent.KEYCODE_ENTER){
                        hideSoftKeyboard(gridView);
                        SearchAdapter sAdapter = (SearchAdapter) textView.getAdapter();
                        sAdapter.clear();
                        searched = true;
                        //actionBar.getCustomView().findViewById(R.id.up).setVisibility(View.VISIBLE);
                        String uneditedSearchText =  textView.getText().toString();
                        String[] keywords = uneditedSearchText.split("\\s+");
                        for(String string : keywords){
                            Log.d("keywords", "search keyword includes " + string);
                        }

                        images = new ArrayList<>(helper.getMatchingImages( Arrays.asList(keywords)));
                        Collections.reverse(images);
                        BaseAdapter adapter = (BaseAdapter) gridView.getAdapter();
                        adapter.notifyDataSetChanged();
                        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME);




                    }
                }
                return false;
            }
        };
        return onKeyListener;
    }

    //^^^^^^^^^^^end of listeners

    //from stackoverflow
    public static ArrayList<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data;
        //int column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        //column_index_folder_name = cursor
        //        .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        cursor.close();
        return listOfAllImages;
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;

        public ImageAdapter(Context c) {
            mContext = c;
            mInflater = ((Activity) mContext).getLayoutInflater();
        }
        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object getItem(int position) {
            return images.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ImageView imageView;
            final ImageView checkmark;
            final File file;
            ViewHolder holder;
            if (convertView == null ) {
                // if it's not recycled, initialize some attributes
                convertView = mInflater.inflate(R.layout.grid_item, parent, false);
                holder  = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                holder.checkmark = (ImageView) convertView.findViewById(R.id.checkmark);
                holder.image.setLayoutParams(new RelativeLayout.LayoutParams(imageSize, imageSize));
                //int id = View.generateViewId();
                //ids.add(id);

                convertView.setTag(holder);
                /*
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(imageSize, imageSize));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                int id = View.generateViewId();
                imageView.setId(id);
                */
                //imageView.setPadding(8, 8, 8, 8);
            } else {
                holder = (ViewHolder) convertView.getTag();

            }
            imageView = holder.image;
            checkmark = holder.checkmark;
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            file = new File(images.get(position));


            Picasso.with(activity)
                    .load(file)
                    .placeholder(placeholder)
                            //.transform(new BitmapTransform(480, 480))
                    //240p is it faster?
                    .resize(240, 240)
                    .centerInside()
                    .into(imageView, new com.squareup.picasso.Callback() {

                        @Override
                        public void onSuccess() {
                            if (selected.contains(file)) {

                                imageView.getDrawable().setColorFilter(filter);
                                checkmark.setVisibility(View.VISIBLE);
                                imageView.invalidate();

                            } else {
                                imageView.getDrawable().clearColorFilter();
                                checkmark.setVisibility(View.INVISIBLE);
                                imageView.invalidate();
                            }

                        }

                        @Override
                        public void onError() {
                        }
                    });

            return convertView;
        }
    }

    public static class ViewHolder{
        ImageView image;
        ImageView checkmark;

    }

    //from stackoverflow
    public class myInputFilter implements InputFilter {

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            boolean keepOriginal = true;
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);

                if ( isCharAllowed(c) )
                    sb.append(c);
                else
                    keepOriginal = false;
            }
            if (keepOriginal)
                return null;
            else {
                if (source instanceof Spanned) {
                    SpannableString sp = new SpannableString(sb);
                    TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                    return sp;
                } else {
                    return sb;
                }
            }
        }

        private boolean isCharAllowed(char c) {
            return Character.isLetterOrDigit(c) ;
        }
    }




    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }
        switch (id) {
            case R.id.add_keyword_button:

                Log.d("Panel", slidingUpPanelLayout.getPanelState().toString());
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);


                break;
            case R.id.add:
                String keyword = addKeywordField.getText().toString();
                addKeywordField.setText(null);
                if (!keyword.equals("")) {
                    for (File file : selected) {
                        try {
                            if (IIMUtility.addKeyword(activity, file, keyword)) {
                                helper.addKeywordtoDatabaseEntry(keyword, file.getAbsolutePath());

                            }
                        } catch (Exception e) {
                            Log.e("DatabaseSearch", "stack trace", e);
                        }
                    }
                }
                updateFilter();
                searchAdapter.notifyDataSetChanged();
                break;

            case R.id.multi_share:

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                try {
                    for(File imageFile : selected) {
                        IIMUtility.addMandatoryData(activity, imageFile);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
                    }

                } catch (Exception e) {
                    Log.e("IIM", "stack trace", e);
                }
                shareIntent.setType("image/*");
                startActivity(Intent.createChooser(shareIntent, "Share this image to..."));
                break;




            case R.id.remove_iim_button:
                createRemoveIIMDialog();
                break;


        }
    }


    public void createRemoveIIMDialog() {
        RemoveIIMDialogFragment fragment = new RemoveIIMDialogFragment();
        fragment.show(getFragmentManager(), "RemoveIIMDialogFragment");
    }



    private void updateGridView(GridView gridView){
        images = getAllShownImagesPath(this);
        Collections.reverse(images);
        BaseAdapter adapter = (BaseAdapter) gridView.getAdapter();
        adapter.notifyDataSetChanged();
    }


    private void toggleSearch(boolean reset, AutoCompleteTextView view) {


        if(reset){
            view.setText("");
            view.setCompoundDrawables(null, null, null, null);
            hideSoftKeyboard(getCurrentFocus());
            view.setCursorVisible(false);

        }else{
            view.setCursorVisible(true);
            showSoftKeyboard(view);
            view.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_action_cancel,null), null);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //all contents of grid view are image views
        ViewHolder holder = (ViewHolder) view.getTag();
        ImageView imageView = holder.image;
        ImageView checkmark = holder.checkmark;
        File file = new File(images.get(position));
        selected.add(file);
        /*
        if (selected.contains(file)) {
            selected.remove(file);
            imageView.getDrawable().clearColorFilter();
            imageView.invalidate();
        } else {
        */
        imageView.getDrawable().setColorFilter(filter);
        Log.d("Filter", "Filtered to gray");
        checkmark.setVisibility(View.VISIBLE);
        initiateItemSelectMode();
        imageView.invalidate();
        return true;


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
        Log.d("FragmentTransaction", String.valueOf(position));
        isRight = !(position % 3 == 0);
        parent.setOnItemLongClickListener(null);
        parent.setOnItemClickListener(null);
        selectedViewOfGridView = v;
        fullPlaceholder = ((ImageView) v.findViewById(R.id.image)).getDrawable().getConstantState().newDrawable();
        YoYo.with(Techniques.ZoomOut).duration(SHORT_ANIM_DURATION).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                startPhotosFragment(position);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).playOn(gridView);
        //gridView.setVisibility(View.GONE);


    }



    public void updateFilter(){
        searchAdapter.updateFilter(helper.getKeywords());
    }

    private class SearchAdapter extends ArrayAdapter<String> {
        private Filter filter;
        private List<String> data;
        private List<String> filteredData;

        public SearchAdapter(Context context, int resource, Set<String> data) {
            super(context, resource);
            this.data = new ArrayList<>(data);;
        }

        public void updateFilter(Set<String> suggestions){
            ((NameFilter) filter).updateData(suggestions);
        }


        class ViewHolder {
            public TextView nameView;


            ViewHolder() {

            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
            TextView nameView;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.search_item, parent, false);
                viewHolder.nameView = (TextView) convertView.findViewById(R.id.search_item);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            nameView = viewHolder.nameView;
            if (position < filteredData.size()){
                nameView.setText(filteredData.get(position));
            }
            return convertView;
        }

        @Override
        public Filter getFilter() {
            if (filter == null) {
                filter = new NameFilter(data);
            }
            return filter;
        }

    }

    private class NameFilter extends Filter {
        List<String> names;
        public NameFilter(List<String> data){
            this.names = data;
        }
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            singleKeywordSearch = true;
            Log.d("Filter", "Constraint = " + String.valueOf(constraint));
            String[] chars = null;
            FilterResults results = new FilterResults();
            if(constraint == null || constraint.length() == 0){
                results.count = names.size();
                results.values = names;

            }else{
                chars = constraint.toString().toLowerCase().split("\\s+");
                Log.d("Filter", "constraint is " + String.valueOf(constraint));
                singleKeywordSearch = chars.length == 1;
                Log.d("Filter", "chars are " + String.valueOf(chars));

                Set<String> filteredList = getSuggestedKeywords(Arrays.asList(chars));

                results.values = new ArrayList<>(filteredList);
                results.count = filteredList.size();
            }
            Log.d("Filter", results.values.toString());
            return results;

        }

        private Set<String> getSuggestedKeywords(List<String> chars) {
            List<String> keywords = new ArrayList<>();
            Set<String> suggestions = new HashSet<>();
            String constraint;
            Set<String> filteredSuggestions;

            if(singleKeywordSearch) {
                constraint = chars.get(0).toLowerCase();
                for(String name : names){
                    if(name.toLowerCase().contains(constraint)){
                        suggestions.add(name);
                    }
                }
                return suggestions;

            }else {
                constraint = chars.get(chars.size() - 1).toLowerCase();

                // put words that match the last keyword in the constraint
                for (String name : names) {
                    if (name.toLowerCase().contains(constraint)) {
                        suggestions.add(name);
                    }
                }
                for (int i = 0; i < chars.size() - 1; i++) {
                    keywords.add(chars.get(i));
                    suggestions.remove(chars.get(i));
                }
                filteredSuggestions = new HashSet<>();
                for (String string : suggestions) {
                    keywords.add(string);
                    if (helper.getMatchingImages(keywords).size() != 0) {
                        Log.d("Filter", "suggested phrase is : " + StringUtils.join(keywords, " "));

                        // use if want suggest the whole phrase instead of next keyword
                        filteredSuggestions.add(StringUtils.join(keywords, " "));

                        // use if want to suggest next keyword
                        //filteredSuggestions.add(string);
                    }
                    keywords.remove(string);
                }
                return filteredSuggestions;

            }

        }

        public void updateData(Set<String> suggestions){
            this.names = new ArrayList<String>(suggestions);
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<String> names = new ArrayList<>();
            searchAdapter.clear();
            if (results.count > 0) {
                Log.d("Filter", "Results count =" + String.valueOf(results.count));
                Log.d("Filter", "searchAdapter list =" + String.valueOf(searchAdapter.getCount()));

                for (String name : (ArrayList<String>) results.values) {
                    searchAdapter.add(name);
                    names.add(name);
                }
            }

            searchAdapter.filteredData = names;
            Log.d("Filter", "searchAdapter final =" + String.valueOf(searchAdapter.filteredData));
            searchAdapter.notifyDataSetChanged();
        }
    }

    private void  initiateItemSelectMode(){
        selectMode = true;
        YoYo.with(Techniques.ZoomOut)
                .duration(SHORT_ANIM_DURATION)
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if(selectModeItemClickListener == null){
                            selectModeItemClickListener = new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    final File file = new File(images.get(position));
                                    ViewHolder holder = (ViewHolder) view.getTag();
                                    final ImageView imageView = holder.image;
                                    final ImageView checkmark = holder.checkmark;
                                    if(selected.contains(file)){
                                        selected.remove(file);
                                        imageView.getDrawable().clearColorFilter();
                                        checkmark.setVisibility(View.INVISIBLE);
                                        imageView.invalidate();
                                    }else{
                                        selected.add(file);
                                        imageView.getDrawable().setColorFilter(filter);
                                        checkmark.setVisibility(View.VISIBLE);
                                        imageView.invalidate();
                                    }
                                    if(selected.size() == 0){
                                        endItemSelectMode();
                                    }
                                    numberSelected.setText(String.valueOf(selected.size()) + " selected");

                                }
                            };
                        }

                        gridView.setOnItemClickListener(selectModeItemClickListener);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME);
                        actionBar.setCustomView(selectLayout);
                        YoYo.with(Techniques.ZoomIn)
                                .duration(SHORT_ANIM_DURATION)
                                .playOn(addKeywordPrompt);
                        YoYo.with(Techniques.ZoomIn)
                                .duration(SHORT_ANIM_DURATION)
                                .playOn(removeKeywordButton);
                        YoYo.with(Techniques.ZoomIn)
                                .duration(SHORT_ANIM_DURATION)
                                .playOn(shareButton);
                        YoYo.with(Techniques.ZoomIn).duration(SHORT_ANIM_DURATION).playOn(numberSelected);
                        numberSelected.setText("1 selected");
                        gridView.setOnItemLongClickListener(null);
                        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .playOn(textView);



    }


    private void endItemSelectMode() {

        YoYo.with(Techniques.ZoomOut)
                .duration(SHORT_ANIM_DURATION)
                .playOn(addKeywordPrompt);
        YoYo.with(Techniques.ZoomOut)
                .duration(SHORT_ANIM_DURATION)
                .playOn(removeKeywordButton);
        YoYo.with(Techniques.ZoomOut).duration(SHORT_ANIM_DURATION).playOn(numberSelected);
        YoYo.with(Techniques.ZoomOut)
                .duration(SHORT_ANIM_DURATION)
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        YoYo.with(Techniques.ZoomIn)
                                .duration(SHORT_ANIM_DURATION).playOn(textView);
                        selectMode = false;
                        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME);
                        actionBar.setCustomView(searchLayout);
                        swipeRefreshLayout.setOnRefreshListener(MainActivity.this);
                        gridView.setOnItemLongClickListener((AdapterView.OnItemLongClickListener) activity);
                        gridView.setOnItemClickListener((AdapterView.OnItemClickListener) activity);

                        BaseAdapter adapter = (BaseAdapter) gridView.getAdapter();
                        selected.clear();
                        gridView.refreshDrawableState();
                        adapter.notifyDataSetChanged();
                        Log.d("SelectMode", "ended select mode");
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .playOn(shareButton);

    }

    protected void setFilters(EditText editText){
        InputFilter[] oldFilters = editText.getFilters();
        InputFilter[] newFilters = Arrays.copyOf(oldFilters, oldFilters.length + 1);
        newFilters[newFilters.length - 1] = new myInputFilter();
        editText.setFilters(newFilters);
    }


    public void startPhotosFragment(int position ){
        hideSoftKeyboard(getCurrentFocus());
        //((FrameLayout)findViewById(R.id.fragment_photos_root)).removeAllViews();
        currentFragment = PhotosFragment.newInstance(position);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if(isRight){
            transaction.setCustomAnimations(R.animator.photos_fragment_set_right, 0, R.animator.photos_fragment_set_right, 0);
        }else{
            transaction.setCustomAnimations(R.animator.photos_fragment_set_left, 0, R.animator.photos_fragment_set_left, 0);
        }
        transaction.replace(R.id.listFragment, currentFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        Log.d("FragmentTransaction" , "before " + String.valueOf(getFragmentManager().getBackStackEntryCount()));
    }

    /**
     * Hides the soft keyboard
     */
    //view is any view that exists in the same window
    public void hideSoftKeyboard(View view ) {
        if(view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        for (File file : selected) {
            try {
                IIMUtility.removeIIM(file);
            } catch (Exception e) {
                Log.e("PhotosFragment", "Stack trace: ", e);
            }
            helper.removeAllKeywordsfromDatabaseEntry(file.getAbsolutePath());
        }

        updateFilter();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //do nothing
    }

}
