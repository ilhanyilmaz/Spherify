package com.digitalwonders.ilhan.spherify;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import java.io.File;

public class DisplayActivity extends ActionBarActivity implements CompoundButton.OnCheckedChangeListener, View.OnTouchListener {

    private Spherify spherify;
    private Bitmap bitmap;
    private ImageView imageView;
    private int rotateValue = 0;
    private float viewRotation= 0f;
    private double fingerRotation=0f;
    private double newFingerRotation;

    private ShareActionProvider mShareActionProvider;
    private Intent shareIntent;
    private String mImagePath;
    private boolean cropEdges = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        mImagePath = getIntent().getStringExtra(AppConstant.SPHERIFY_IMAGE_PATH);

        CheckBox cbCrop = (CheckBox)findViewById(R.id.cropCheckBox);
        cbCrop.setOnCheckedChangeListener(this);

        init();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        //mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);


        return true;
    }

    private void initShareIntent() {
        shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri uri = spherify.getUri();
        String caption = getString(R.string.share_caption);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, caption);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // Broadcast the Intent.

    }
    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.menu_item_share) {
            //Log.i("Spherify", "share clicked");

            shareIt();
        }

        else if(id == R.id.menu_item_delete) {

            deleteIt();
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareIt() {
        spherify.saveShareFile(-rotateValue, cropEdges);
        initShareIntent();
        finish();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", getResources().getText(R.string.share_caption));
        clipboard.setPrimaryClip(clip);
        startActivity(Intent.createChooser(shareIntent, "Share to"));
    }

    @Override
    public void onResume() {

        super.onResume();

        spherify = new Spherify();



        if(mImagePath == null) {
            //mImagePath = spherify.getTempFilePath();
            finish();
        }



        try {
            bitmap = AppFunctions.loadImage(mImagePath, getApplicationContext(), true);
        }
        catch (Exception e) {
            AppFunctions.showToast(getApplicationContext(), "No images to display!");
            finish();
            return;
        }

        if(bitmap == null) {
            AppFunctions.showToast(getApplicationContext(), "No images to display!");
            finish();
            return;
        }

        spherify.setSpherifiedImage(bitmap);
        //bitmap = spherify.getRotatedBitmap(rotateValue);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleX(spherify.getCropToFullRatio());
        imageView.setScaleY(spherify.getCropToFullRatio());

        imageView.setOnTouchListener(this);

    }

    @Override
    protected void onStop() {


        spherify.destroy();
        if(bitmap != null)
            bitmap.recycle();

        super.onStop();
    }

    @Override
    public void onCheckedChanged(CompoundButton b, boolean isChecked) {
        if (isChecked) {
            imageView.setScaleX(spherify.getCropToFullRatio());
            imageView.setScaleY(spherify.getCropToFullRatio());
            cropEdges = true;
        }
        else {
            imageView.setScaleX(1);
            imageView.setScaleY(1);
            cropEdges = false;
        }

    }

    private void init(){

        clearNotification();

        imageView = (ImageView) findViewById(R.id.imageView);

    }

    protected void deleteIt() {

        File file = new File(mImagePath);
        boolean deleted = file.delete();
        if(deleted) {
            AppFunctions.showToast(getApplicationContext(), "Deleted!");
            finish();
        }
        else
            AppFunctions.showToast(getApplicationContext(), "Couldn't be deleted!");
        //spherify.saveBitmap(spherify.getRotatedBitmap(-rotateValue));

    }


    private void clearNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(SpherifyActivity.NOTIFICATION_ID);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        final float x = event.getX();
        final float y = event.getY();

        final float xc = imageView.getWidth()/2;
        final float yc = imageView.getHeight()/2;


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                viewRotation = imageView.getRotation();
                fingerRotation = Math.toDegrees(Math.atan2(x - xc, yc - y));
                break;
            case MotionEvent.ACTION_MOVE:
                newFingerRotation = Math.toDegrees(Math.atan2(x - xc, yc - y));
                imageView.setRotation((float)(viewRotation + newFingerRotation - fingerRotation));
                break;
            case MotionEvent.ACTION_UP:
                fingerRotation = newFingerRotation = 0.0f;
                rotateValue = (int)imageView.getRotation();
                //rotateValue = rotateValue % 360;
                break;
        }

        return true;
    }
}
