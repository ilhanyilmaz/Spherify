package com.digitalwonders.ilhan.spherify;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

/**
 * Created by ilhan on 21.02.2015.
 */
public class Spherify extends AsyncTask<Bitmap, Integer, Bitmap> {
    private Mat srcImage;
    private Mat spherifiedImage;

    private int genImageSize;
    private int halfGenImageSize;
    private int croppedImageSize = 1080;

    private float topMargin;
    private float footMargin;
    private int smootValue;

    private double scale;
    private int offset;
    private int progress = 0;

    private SpherifyActivity activity;
    private File saveFile;
    private String mSaveFilePath;

    public Spherify() {

        genImageSize= (int)(croppedImageSize/ Math.sin(Math.PI/4.0));
    }

    public Spherify(SpherifyActivity spherifyActivity, float _topMargin, float _footMargin, int _smoothValue) {
        topMargin = _topMargin;
        footMargin = _footMargin;
        //Log.i("Spherify", "top: " + (topMargin));
        //Log.i("Spherify", "foot: " + (footMargin));
        smootValue = _smoothValue;

        activity = spherifyActivity;

        genImageSize= (int)(croppedImageSize/ Math.sin(Math.PI/4.0));
    }

    protected Bitmap doInBackground(Bitmap... bitmaps) {
        return spherifyIt(bitmaps[0]);
    }

    protected void onProgressUpdate(Integer... progress) {
        activity.setProgressPercent(progress[0]);
    }

    /** The system calls this to perform work in the UI thread and delivers
     * the result from doInBackground() */
    protected void onPostExecute(Bitmap result) {
        activity.spherifyDoneActions();
    }


    private Point getXY(int i, int j) {

        return new Point(i-halfGenImageSize,j-halfGenImageSize);
    }

    private Point getAngleDist(int i, int j) {
        Point returnVals = new Point();
        int x = i - halfGenImageSize;
        int y = j - halfGenImageSize;
        double angle = Math.atan2(y, x); ////////////////////////

        if (angle > 0)
            angle = angle * 180.0 / Math.PI;
        else
            angle = 360 + angle * 180.0 / Math.PI;

        if (angle == 360)
            angle = 0;


        double dist = Math.hypot(x, y);
        returnVals.x = angle;
        returnVals.y = dist;
        return returnVals;
    }

    private Point getUniformCoordinates(double angle, double dist) {

        ////////////   BURADA MARGIN 1.0 DONDURMELI
        Point uv = new Point();
        uv.x = (angle/360);
        //uv.y = (dist/(halfGenImageSize));
        uv.y = (dist*scale+offset)/srcImage.rows();

        return uv;
    }
    private Point getUniformCoordinatesInsideCircle(double x, double y) {

        Point uv = new Point();
        uv.y = Math.abs(y / genImageSize);
        //uv.y = uv.y*uv.y;
        uv.x = 0.5 - x/genImageSize;
        return uv;
    }

    public Mat rotateImage(Mat image, int angle, boolean cropEdges) {

        Mat rotatedImage = new Mat();
        Mat M = Imgproc.getRotationMatrix2D(new Point(halfGenImageSize, halfGenImageSize), angle, 1);

        Imgproc.warpAffine(image, rotatedImage, M, new Size(genImageSize, genImageSize));

        if(cropEdges) {
            Rect roi = new Rect((genImageSize - croppedImageSize) / 2, (genImageSize - croppedImageSize) / 2, croppedImageSize, croppedImageSize);
            rotatedImage = new Mat(rotatedImage, roi);
        }
        return rotatedImage;
    }

    private String getDateTimeString() {
        Calendar c = Calendar.getInstance();

        int y = c.get(Calendar.YEAR);
        String m = Integer.toString(c.get(Calendar.MONTH));
        if(m.length() == 1)
            m = "0" + m;
        String d = Integer.toString(c.get(Calendar.DATE));
        if(d.length() == 1)
            d = "0" + d;
        int hi = c.get(Calendar.HOUR);
        if(c.get(Calendar.AM_PM) == Calendar.PM)
            hi=hi+12;
        String h= Integer.toString(hi);
        if(h.length() == 1)
            h = "0" + h;
        String s = Integer.toString(c.get(Calendar.SECOND));
        if(s.length() == 1)
            s = "0" + s;
        String n = y +""+ m+"" + d+"" + h+"" + s;
        return n;
    }


    public void saveSpherified() {
        String n = getDateTimeString();
        mSaveFilePath = "spherified-"+ n +".jpg";
        saveBitmap(mSaveFilePath, convertToBitmap(spherifiedImage));
    }

    public String getSaveFilePath() {
        return mSaveFilePath;
    }

    public String getFullSaveFilePath() {
        return Environment.getExternalStorageDirectory().toString() + "/" + AppConstant.PHOTO_ALBUM + "/" + mSaveFilePath;
    }

    public Uri getUri() {
        if(saveFile != null && saveFile.exists()) {
            //Log.i("Spherify", saveFile.getAbsolutePath());
            return Uri.fromFile(saveFile);
        }
        else
            return null;
    }

    public void saveShareFile(int angle, boolean cropEdges) {
        Bitmap bitmap = getRotatedBitmap(angle, cropEdges);
        saveBitmap("spherify.jpg", bitmap);
    }

    public void saveBitmap(String fname, Bitmap bitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir;

        if(fname.equals("spherify.jpg"))
            myDir = new File(root);
        else
            myDir = new File(root + "/" + AppConstant.PHOTO_ALBUM);

        myDir.mkdirs();


        saveFile = new File (myDir, fname);

        if (saveFile.exists ()) saveFile.delete ();
        try {
            FileOutputStream out = new FileOutputStream(saveFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void seamlessEdges(Mat image) {
        double smoothRange = smootValue;
        double smoothRangeHalf = smoothRange/2;
        double pixel1[];
        double pixel2[];
        double tempPixel[] = new double[4];
        tempPixel[3] = 255;
        int imageHeight = image.height();
        int imageWidth = image.width();
        int i,j;
        for(i=0; i<smoothRangeHalf; i++) {
            for(j=0; j<imageHeight; j++ ) {
                pixel1 = image.get(j,i);
                pixel2 = image.get(j,imageWidth - i - 1);
                tempPixel[0] = pixel1[0] * ((smoothRangeHalf+i)/ smoothRange) +pixel2[0] * ((smoothRangeHalf-i)/ smoothRange);
                tempPixel[1] = pixel1[1] * ((smoothRangeHalf+i)/ smoothRange) +pixel2[1] * ((smoothRangeHalf-i)/ smoothRange);
                tempPixel[2] = pixel1[2] * ((smoothRangeHalf+i)/ smoothRange) +pixel2[2] * ((smoothRangeHalf-i)/ smoothRange);
                pixel2[0] = pixel2[0] * ((smoothRangeHalf+i)/ smoothRange) +pixel1[0] * ((smoothRangeHalf-i)/ smoothRange);
                pixel2[1] = pixel2[1] * ((smoothRangeHalf+i)/ smoothRange) +pixel1[1] * ((smoothRangeHalf-i)/ smoothRange);
                pixel2[2] = pixel2[2] * ((smoothRangeHalf+i)/ smoothRange) +pixel1[2] * ((smoothRangeHalf-i)/ smoothRange);
                image.put(j,i,tempPixel);
                image.put(j,imageWidth - i - 1,pixel2);
            }
        }
    }

    public void setSpherifiedImage(Bitmap bitmap) {

        spherifiedImage = new Mat();

        spherifiedImage.create(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);

        Utils.bitmapToMat(bitmap, spherifiedImage);

        genImageSize= spherifiedImage.cols();
        croppedImageSize= (int)(genImageSize * Math.sin(Math.PI/4.0));
        halfGenImageSize = genImageSize/2;
    }


    public Bitmap spherifyIt(Bitmap bitmap) {

        int insideCircleOutRadius;

        if(srcImage == null) {
            srcImage = new Mat();

            srcImage.create(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);
            Bitmap myBitmap32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(myBitmap32, srcImage);

            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_BGR2RGB, 4);

        }

        Utils.bitmapToMat(bitmap, srcImage);

        //cropImage();

        seamlessEdges(srcImage);
        int srcWidth = srcImage.cols();
        int srcHeight = srcImage.rows();

        /*if(height / 2 > imageSize)
            imageSize = height / 2;
        if(imageSize > maxSize)
            imageSize = maxSize;

        radius=imageSize/2;*/



        halfGenImageSize = genImageSize/2;

        Mat newImg = new Mat();
        newImg.create(genImageSize, genImageSize, CvType.CV_8UC4);
        int i=0, j=0;
        Point angleDist;
        Point uv;
        Point xy;
        int x,y;
        //insideCircleOutRadius = (int)(footMargin*croppedImageSize/2);
        insideCircleOutRadius = (int)(genImageSize/12);
        //outsideCircleRadius = (int)(1+topMargin)*croppedImageSize;
        double distance;
        double color[];

        int topY, footY, withinHeight;
        double ratio;

        topY= (int)(srcHeight*(topMargin));
        footY= (int)(srcHeight*(footMargin));
        withinHeight = topY-footY;
        scale = withinHeight/((double)(croppedImageSize/2-insideCircleOutRadius));
        offset = (int)(footY - scale*insideCircleOutRadius);

        for(j=0; j<genImageSize; j++) {
            progress = j/genImageSize*100;
            publishProgress((int) ((j /(float)genImageSize)*100));
            for(i=0; i<genImageSize; i++) {
                angleDist = getAngleDist(i, j);
                uv = getUniformCoordinates(angleDist.x, angleDist.y);

                if(uv.y < 0)
                    uv.y = -uv.y;
                else if(uv.y > 1)
                    uv.y = 2-uv.y;
                x = (int) (uv.x * srcWidth);
                y = (int) (srcHeight - uv.y * srcHeight - 1);
                newImg.put(j, i, srcImage.get(y, x));

            }
        }

        spherifiedImage = newImg;

        //newImg = rotateImage(newImg, 270);

        return convertToBitmap(spherifiedImage);
    }

    /*public boolean loadImage(String filepath) {
        srcImage = Highgui.imread(filepath);
        if(!srcImage.empty() )
        return !srcImage.empty();

    }*/
    private Bitmap convertToBitmap(Mat img) {
        // convert to bitmap:
        /*if(img == null)
            return null;
        Mat tmp = new Mat();
        Imgproc.cvtColor(img, tmp, Imgproc.COLOR_BGR2RGB);

        Bitmap bm = Bitmap.createBitmap(tmp.cols(), tmp.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp, bm);*/
        Bitmap bm = Bitmap.createBitmap(img.cols(), img.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bm);
        return bm;
    }

    public Bitmap getRotatedBitmap(int angle, boolean cropEdges) {

        Mat newImg = rotateImage(spherifiedImage, angle, cropEdges);
        return convertToBitmap(newImg);
    }

    public int getProgress() {
        return progress;
    }

    public void destroy(){

        if(srcImage != null)
            srcImage.release();
        if(spherifiedImage != null)
            spherifiedImage.release();
    }
    public float getCropToFullRatio() {
        return genImageSize / (float)croppedImageSize;
    }
}