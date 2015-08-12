package com.digitalwonders.ilhan.spherify;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

public class EditActivity extends Activity implements SeekBar.OnSeekBarChangeListener, View.OnTouchListener {

    private Bitmap bitmap;
    private Uri bitmapUri;
    private ImageView imageView;
    private String imagePath;
    private SeekBar seekBarSmooth;
    private ImageView ivMaskBottom;
    private ImageView ivMaskTop;
    private ImageView mCurrentMask;
    private int mScreenWidth;
    private float mImgHeight;
    private float mDefaultScale;


    private float mLastY;
    private float mTopMaskMinY;
    private float mTopMaskMaxY;
    private float mBottomMaskMinY;
    private float mBottomMaskMaxY;

    private int mMaskHeight;

    private int smoothLinePos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        init();
    }

    private void init(){

        seekBarSmooth = (SeekBar) findViewById(R.id.seekBarSmooth);
        seekBarSmooth.setOnSeekBarChangeListener(this);


        ivMaskTop = (ImageView) findViewById(R.id.imageMaskTop);
        ivMaskBottom = (ImageView) findViewById(R.id.imageMaskBottom);

        ivMaskTop.setOnTouchListener(this);
        ivMaskBottom.setOnTouchListener(this);

        Intent intent = getIntent();

        bitmapUri = Uri.parse(intent.getStringExtra(AppConstant.SPHERIFY_IMAGE_PATH));
        imagePath = AppFunctions.getRealPathFromURI(bitmapUri, getContentResolver());

        imageView = (ImageView) findViewById(R.id.imageView);

        Button spherifyButton = (Button) findViewById(R.id.buttonSpherify);
        spherifyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startSpherifyActivity();
            }
        });

    }

    @Override
    protected void onResume() {

        super.onResume();

        if(bitmap == null && !imagePath.equals("")) {
            bitmap = AppFunctions.loadImage(imagePath, getApplicationContext(), false);
        }

        if(bitmap != null)
            displayBitmap(bitmap);
        else {
            AppFunctions.showToast(getApplicationContext(), "Image size is exceeding 4096x4096");
            finish();
        }


        mScreenWidth = Utils.getScreenWidth(getApplicationContext());

        mImgHeight = getResources().getDimension(R.dimen.edit_image_height);

        int maskMinCover = (int) mImgHeight/10;
        mMaskHeight = (int) (mImgHeight/2.5);



        //Log.i("Spherify", "mask size: " + mScreenWidth + " - " + mMaskHeight);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mScreenWidth, mMaskHeight);
        ivMaskTop.setLayoutParams(layoutParams);
        ivMaskBottom.setLayoutParams(layoutParams);

        mTopMaskMinY = -mMaskHeight+maskMinCover;
        mTopMaskMaxY = 0;
        mBottomMaskMinY = mImgHeight-mMaskHeight;
        mBottomMaskMaxY = mImgHeight-maskMinCover;

        ivMaskTop.setY(mTopMaskMinY);
        ivMaskBottom.setY(mBottomMaskMaxY);
        //ivMaskBottom.setY();
        //correctMaskPos(ivMaskBottom);
    }

    @Override
    protected void onStop() {

        imageView.setImageDrawable(null);

        if(bitmap != null)
            bitmap.recycle();
        bitmap = null;

        super.onStop();
    }


    private void correctMaskPos(ImageView iv) {
        if(iv == ivMaskTop) {
            if(ivMaskTop.getY() < mTopMaskMinY)
                ivMaskTop.setY(mTopMaskMinY);
            else if(ivMaskTop.getY() > mTopMaskMaxY)
                ivMaskTop.setY(mTopMaskMaxY);
        } else if(iv == ivMaskBottom) {
            if(ivMaskBottom.getY() < mBottomMaskMinY)
                ivMaskBottom.setY(mBottomMaskMinY);
            else if(ivMaskBottom.getY() > mBottomMaskMaxY)
                ivMaskBottom.setY(mBottomMaskMaxY);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            mLastY = event.getY();
            if(v == ivMaskTop || v == ivMaskBottom)
                mCurrentMask = (ImageView) v;
            else
                mCurrentMask = null;
            return true;
        }
        else if(event.getAction() == MotionEvent.ACTION_MOVE){
            if(mCurrentMask != null)
                mCurrentMask.setY(mCurrentMask.getY()+event.getY()-mLastY);

            correctMaskPos(mCurrentMask);

            mLastY = event.getY();
            return true;
        }
        else if(event.getAction() == MotionEvent.ACTION_UP){

            mCurrentMask = null;
            return true;
        }
        return false;
    }

    @Override
    public void onProgressChanged(SeekBar bar, int value, boolean changed) {

        if(bar == seekBarSmooth)
            smoothLinePos = value;
    }

    @Override
    public void onStartTrackingTouch(SeekBar bar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar bar) {

    }

    protected void displayBitmap(Bitmap bm) {
        // find the imageview and draw it!

        imageView.setImageBitmap(bm);
    }

    private float getTopMaskValue() {
        float maskValue = mMaskHeight + ivMaskTop.getY();
        return 1f-maskValue/mImgHeight;
    }

    private float getBottomMaskValue() {
        return 1f-(ivMaskBottom.getY()/mImgHeight);
    }

    protected void startSpherifyActivity() {


        Intent intent = new Intent(this, SpherifyActivity.class);
        intent.putExtra(AppConstant.SPHERIFY_IMAGE_PATH, bitmapUri.toString());
        intent.putExtra(AppConstant.SPHERIFY_TOP_MARGIN, getTopMaskValue());
        intent.putExtra(AppConstant.SPHERIFY_FOOT_MARGIN, getBottomMaskValue());
        intent.putExtra(AppConstant.SPHERIFY_SMOOTH_VALUE, smoothLinePos*4);

        //finish();
        startActivity(intent);

    }


}
