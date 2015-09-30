package com.digitalwonders.ilhan.spherify;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.w3c.dom.Text;

import java.io.File;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class SpherifyActivity extends Activity {

    private Spherify spherify;
    private Bitmap bitmap;
    public static int NOTIFICATION_ID= 1;

    private ProgressBar progressBar;
    private TextView mTimerTW;
    private TextView mTimerInfo;

    private int status = 1;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;

    private InterstitialAd mInterstitialAd;
    private Timer mTimer;

    private int mCountDown = 5;
    private long startTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spherify);
        init();
        initAd();

    }


    @Override
    public void onResume() {
        super.onResume();
        status = 1;

    }

    @Override
    protected void onStop() {

        //spherify.destroy();
        //bitmap.recycle();
        //spherify = null;
        status = 0;
        super.onStop();
    }


    private void init(){

        int smoothValue;
        float topMargin;
        float footMargin;
        String imagePath;
        boolean flipVertical;

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTimerTW = (TextView) findViewById(R.id.ad_info_timer);
        mTimerInfo = (TextView) findViewById(R.id.ad_info_view);
        Intent intent = getIntent();

        topMargin = intent.getFloatExtra(AppConstant.SPHERIFY_TOP_MARGIN, 0);
        footMargin = intent.getFloatExtra(AppConstant.SPHERIFY_FOOT_MARGIN, 0);
        smoothValue = intent.getIntExtra(AppConstant.SPHERIFY_SMOOTH_VALUE, 0);
        flipVertical = intent.getBooleanExtra(AppConstant.SPHERIFY_FLIP_VERTICAL, false);

        Uri imageUri = Uri.parse(intent.getStringExtra(AppConstant.SPHERIFY_IMAGE_PATH));
        imagePath = AppFunctions.getRealPathFromURI(imageUri, getContentResolver());

        spherify = new Spherify(this, topMargin, footMargin, smoothValue);
        bitmap = AppFunctions.loadImage(imagePath, getApplicationContext(), true);
        if(flipVertical)
            bitmap = AppFunctions.flipVertically(bitmap);

        putNotification();
        spherifyIt();
    }


    protected void spherifyIt() {


        Toast toast;
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        String text = "Spherifying it! Please wait...";
        toast = Toast.makeText(context, text, duration);
        toast.show();

        //new SpherifyTask().execute(bitmap);
        spherify.execute(bitmap);
        startTime = getMillis();

    }
    public void setProgressPercent(int progress) {
        progressBar.setProgress(progress);
        updateNotification();
    }

    public void spherifyDoneActions() {
        this.bitmap.recycle();

        spherify.saveSpherified();
        spherify.destroy();

        updateNotificationDone();

        finish();

        if(status == 1)
            startDisplayActivity();

    }


    private void startDisplayActivity() {

        Intent intent = new Intent(this, DisplayActivity.class);
        intent.putExtra(AppConstant.SPHERIFY_IMAGE_PATH, spherify.getFullSaveFilePath());
        startActivity(intent);
    }



    private void putNotification() {

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_mood_black_18dp)
                        .setContentTitle("Spherify")
                        .setContentText("Progress")
                        .setProgress(100, 0, false);


        // Creates an explicit intent for an Activity in your app

        /**/


        // mId allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

    }

    private void updateNotification() {
        mBuilder.setProgress(100, progressBar.getProgress(), false);
        // Displays the progress bar for the first time.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void updateNotificationDone() {

        Intent resultIntent = new Intent(this, DisplayActivity.class);
        resultIntent.putExtra(AppConstant.SPHERIFY_IMAGE_PATH, spherify.getFullSaveFilePath());
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentText("Spherifying Done!")
                .setProgress(0, 0, false)
                .setContentIntent(resultPendingIntent);

        // Displays the progress bar for the first time.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void initAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.banner_ad_unit_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                //beginPlayingGame();
            }
        });

        requestNewInterstitial();
        mTimer = new Timer();
        mTimer.schedule(new DisplayAdTask(), 1000);
    }

    private void requestNewInterstitial() {
        //Log.i("Spherify", AdRequest.DEVICE_ID_EMULATOR);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("B3EEABB8EE11C2BE770B684D95219ECB")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    private String getRemainingTimeStr() {
        long timePassed = getMillis() - startTime;
        int progress = progressBar.getProgress();
        if(progress == 0)
            return "...";
        long totalTime = timePassed * 100 / progress;
        long remTime = (totalTime - timePassed)/1000;
        int remMin = (int) remTime / 60;
        int remSec = (int) remTime % 60;
        if(remMin > 0)
            return remMin + getString(R.string.minute) +  remSec + getString(R.string.second);
        else
            return remSec + getString(R.string.second);
    }


    private class DisplayAdTask extends TimerTask {
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mCountDown>0)
                        mCountDown--;

                    if(mCountDown == -1) {
                        mTimerInfo.setText(getString(R.string.remaining));
                        mTimerTW.setText(getRemainingTimeStr());
                    }
                    else if(mCountDown == 0 && mInterstitialAd.isLoaded()) {
                        mCountDown = -1;
                        mInterstitialAd.show();
                    }
                    else {
                        ((TextView) findViewById(R.id.ad_info_timer)).setText("" + mCountDown);
                    }
                    mTimer.schedule(new DisplayAdTask(), 1000);
                }
            });
        }
    }

    private long getMillis() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }
}
