package com.futureskyltd.app.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.futureskyltd.app.fantacy.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageStorage {

    private static Context context;

    public ImageStorage(Context context){
        this.context = context;
    }

    public String saveToSdCard(Bitmap bitmap, String filename) {

        String stored = "";

        File sdcard = Environment.getExternalStorageDirectory() ;

        String path = "/" + context.getString(R.string.app_name) + "Images";

        File folder = new File(sdcard.getAbsoluteFile(), path);
        folder.mkdirs();
        File nomediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
        try {
            if (!nomediaFile.exists()) {
                nomediaFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(folder.getAbsoluteFile(), filename) ;
        if (file.exists())
            return "success";

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stored;
    }

    public String saveThumbNail(Bitmap bitmap, String filename){
        String stored = "";

        String path = "/" + context.getString(R.string.app_name) + "Images/.thumbnails";
        File sdcard = Environment.getExternalStorageDirectory() ;
        File folder = new File(sdcard.getAbsoluteFile(), path);
        folder.mkdirs();

        File nomediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
        try {
            if (!nomediaFile.exists()) {
                nomediaFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(folder.getAbsoluteFile(), filename) ;
        if (file.exists())
            return "success";

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            stored = "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stored;
    }

    public File getImage(String imagename) {

        File mediaImage = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            String path = "/" + context.getString(R.string.app_name) + "Images/";
            mediaImage = new File(myDir.getPath() + path + imagename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

    public boolean checkifImageExists(String from, String imagename) {
        Bitmap b = null ;
        File file = getImage(imagename);
        String path = file.getAbsolutePath();

        if (path != null)
            b = BitmapFactory.decodeFile(path);

        if(b == null ||  b.equals(""))
        {
            return false ;
        }
        return true ;
    }
}
