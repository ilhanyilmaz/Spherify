package com.digitalwonders.ilhan.spherify;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

public class AppFunctions {

    public AppFunctions() {

    }

    public static String fixPath(String path) {

        String prefix, suffix;
        prefix = path.substring(0, path.lastIndexOf("/"));
        suffix = path.substring(path.lastIndexOf("/"));
        prefix = prefix.replaceAll("%20"," ");
        return prefix.concat(suffix);
    }

    public static String getRealPathFromURI(Uri contentUri, ContentResolver contentResolver) {

        String res = null;
        if(contentUri.getEncodedPath().startsWith("/storage")) {
            res = contentUri.getEncodedPath();
            return res;
        }

        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = contentResolver.query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
    public static Bitmap loadImage(String filename, Context context, boolean quality) {

        Toast toast;
        int duration = Toast.LENGTH_SHORT;
        String text;
        Bitmap bitmap;
        int reqHeight = 256;
        int reqWidth = 512;

        if(quality) {
            reqHeight = 512;
            reqWidth = 1024;
        }


        try {
            bitmap = decodeSampledBitmap(filename, reqWidth, reqHeight);

            if(bitmap != null) {
                /*text = "opened file: " + filename;
                toast = Toast.makeText(context, text, duration);
                toast.show();*/
                //displayBitmap(bitmap);
                return bitmap;
            }
            else {
                text = "couldn't open file" + filename;
                toast = Toast.makeText(context, text, duration);
                toast.show();
                return null;
            }
        }
        catch(Exception e) {
            //text = "couldn't open file" + file.getAbsolutePath();
            text = e.toString();
            toast = Toast.makeText(context, text, duration);
            toast.show();
            return null;
        }

    }
    private static boolean isLandscape(BitmapFactory.Options options) {
        return (options.outWidth>=options.outHeight);
    }
    private static boolean isWithinLimits(BitmapFactory.Options options) {
        return (options.outWidth<=4096 && options.outHeight<=4096);
    }

    public static Bitmap flipVertically(Bitmap sourceBitmap) {
        Bitmap rotatedBitmap;
        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        rotatedBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }

    private static Bitmap fixRotation(Bitmap sourceBitmap) {
        Bitmap rotatedBitmap;
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        rotatedBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }

    private static Bitmap fixSize(Bitmap sourceBitmap, BitmapFactory.Options options) {
        Bitmap scaledBitmap;

        float scale = 4096.0f / ((options.outWidth>options.outHeight)?options.outWidth:options.outHeight);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        scaledBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
        return scaledBitmap;
    }

    public static Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap;

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(path, options);
        if (!isLandscape(options))
            bitmap = fixRotation(bitmap);
        if(!isWithinLimits(options))
            bitmap = fixSize(bitmap, options);
        return bitmap;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static void showToast(Context c, String msg) {
        Toast toast;

        int duration = Toast.LENGTH_SHORT;

        toast = Toast.makeText(c, msg, duration);
        toast.show();
    }

}
