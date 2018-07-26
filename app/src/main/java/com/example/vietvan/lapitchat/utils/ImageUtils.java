package com.example.vietvan.lapitchat.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by qklahpita on 2/4/18.
 */

public class ImageUtils {
    private static final String TAG = "ImageUtils";

    public static String folderName = "DrawImage";
    public static File tempFile;

    public static void saveImage(Bitmap bitmap, Context context) {
        //1. create new folder to save image
        String root = Environment.getExternalStorageDirectory().toString();
        Log.d(TAG, "saveImage: " + root);

        File folder = new File(root, folderName);
        folder.mkdirs();

        //2. create empty file (.png)
        String imageName = Calendar.getInstance().getTime().toString() + ".png";
        Log.d(TAG, "saveImage: " + imageName);
        File imageFile = new File(folder.toString(), imageName);

        //3. use fileOutputStream to save image
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();

            Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show();

            MediaScannerConnection.scanFile(
                    context,
                    new String[]{imageFile.getAbsolutePath()},
                    null,
                    null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Uri getUri(Context context){
        try {

            tempFile = File.createTempFile(Calendar.getInstance().getTime().toString(),
                    ".jpg",
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            tempFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri uri = null;
        uri = FileProvider.getUriForFile(context,
                context.getPackageName() + ".provider",
                tempFile);

        return uri;
    }

    public static Uri get(){
        return Uri.fromFile(tempFile);
    }

    public static Bitmap getBitmap(Context context){
        Bitmap bitmap = BitmapFactory.decodeFile(tempFile.getPath());

        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        double ratio = (double)bitmap.getWidth() / bitmap.getHeight();

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, (int)(screenWidth/ratio), false);

        return scaledBitmap;
    }

}
