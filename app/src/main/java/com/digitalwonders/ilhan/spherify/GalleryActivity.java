package com.digitalwonders.ilhan.spherify;

import java.util.ArrayList;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.opencv.android.OpenCVLoader;

public class GalleryActivity extends AppCompatActivity {

    private Utils utils;
    private GridViewImageAdapter adapter;
    private GridView gridView;
    private int columnWidth;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
            AppFunctions.showToast(getApplicationContext(), "OpenGL initialization error!");
            finish();
        }


        // Get the intent that started this activity
        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            if (extras.containsKey(Intent.EXTRA_STREAM)) {

                Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                Intent intent2 = new Intent(this, EditActivity.class);
                intent2.putExtra(AppConstant.SPHERIFY_IMAGE_PATH, uri.toString());

                startActivity(intent2);
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // inside your activity (if you did not enable transitions in your theme)
            //getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

            // set an exit transition
            //getWindow().setExitTransition(new Explode());
            mFab = (FloatingActionButton) findViewById(R.id.fab);
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectAnImage();
                }
            });
        }



        /*// Check if we're running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

        } else {
            // Implement this feature without material design
        }*/


        gridView = (GridView) findViewById(R.id.grid_view);

        utils = new Utils(this);

        // Initilizing Grid View
        InitializeGridLayout();


    }

    @Override
    public void onResume() {

        super.onResume();
        // loading all image paths from SD card
        ArrayList<String> imagePaths;

        imagePaths = utils.getFilePaths();

        if(imagePaths.size() == 0) {
            finish();
            startHelpActivity();
        }

        // Gridview adapter
        adapter = new GridViewImageAdapter(GalleryActivity.this, imagePaths,
                columnWidth);

        // setting grid view adapter
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(mMessageClickedHandler);


    }

    @Override
    public void onStop() {

        super.onStop();
        gridView.setAdapter(null);
        adapter = null;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        // Locate MenuItem with ShareActionProvider


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if(id == R.id.menu_item_new) {
            //Log.i("Spherify", "share clicked");

            selectAnImage();
        }

        else if(id == R.id.menu_item_help) {

            startHelpActivity();
        }

        return super.onOptionsItemSelected(item);
    }


    // Create a message handling object as an anonymous class.
    private GridView.OnItemClickListener mMessageClickedHandler = new GridView.OnItemClickListener() {

        public void onItemClick(AdapterView parent, View v, int position, long id)
        {
            String filePath = (String) adapter.getItem(position);
            // Display a messagebox.
            //Log.i("Spherify", "path: " + filePath);
            startDisplayActivity(filePath, v);
        }
    };

    private void selectAnImage(){
        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.setAction(Intent.ACTION_PICK);

        startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), AppConstant.SELECT_PICTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == RESULT_OK){

            final Uri imageUri = data.getData();

            Intent intent = new Intent(GalleryActivity.this, EditActivity.class);
            intent.putExtra(AppConstant.SPHERIFY_IMAGE_PATH, imageUri.toString());
            //Log.i("Spherify", imageUri.toString());

            startActivity(intent);

        }
    }

    private void startHelpActivity() {

        Intent intent = new Intent(GalleryActivity.this, HelpActivity.class);
        startActivity(intent);

    }

    private void startDisplayActivity(String filepath, View v) {

        Intent intent = new Intent(GalleryActivity.this, DisplayActivity.class);
        intent.putExtra(AppConstant.SPHERIFY_IMAGE_PATH, filepath);
        /*intent.putExtra(AppConstant.VIEW_X, v.getX());
        intent.putExtra(AppConstant.VIEW_Y, v.getY());
        intent.putExtra(AppConstant.VIEW_WIDTH, v.getWidth());
        intent.putExtra(AppConstant.VIEW_HEIGHT, v.getHeight());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            v.setTransitionName(getResources().getString(R.string.transition_name));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.setTransitionName(getResources().getString(R.string.transition_name));
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(this, v, getResources().getString(R.string.transition_name));
            startActivity(intent, options.toBundle());

        } else {
            startActivity(intent);
        }*/

        startActivity(intent);


    }


    private void InitializeGridLayout() {
        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                AppConstant.GRID_PADDING, r.getDisplayMetrics());

        columnWidth = (int) ((utils.getScreenWidth() - ((AppConstant.NUM_OF_COLUMNS + 1) * padding)) / AppConstant.NUM_OF_COLUMNS);

        gridView.setNumColumns(AppConstant.NUM_OF_COLUMNS);
        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding((int) padding, (int) padding, (int) padding,
                (int) padding);
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);
    }

}