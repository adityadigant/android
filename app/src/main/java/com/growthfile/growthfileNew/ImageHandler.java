package com.growthfile.growthfileNew;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.impl.utils.Exif;

public class ImageHandler {

    public int deviceWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public int deviceHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public String encodeImage(Bitmap bm) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    private Bitmap decodeBitmapFromFile(File file) {
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }


    public String getImageOutput(File file) {

        Bitmap bitmap = decodeBitmapFromFile(file);
        Log.d("Bitmap dimens","H: "+bitmap.getHeight() + "W: "+bitmap.getWidth());
        Bitmap scaled = getScaledBitmap(bitmap);
        Bitmap changedBit = scaled;

        try {

            ExifInterface ei = new ExifInterface(file.getAbsolutePath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    changedBit = rotate(scaled, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    changedBit = rotate(scaled, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    changedBit = rotate(scaled, 270);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    changedBit = flip(scaled, true, false);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    changedBit = flip(scaled, false, true);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                    changedBit = scaled;
                default:
                    changedBit = scaled;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return encodeImage(changedBit);

    }

    public Bitmap getScaledBitmap(Bitmap bitmap) {

        final int maxWidth = deviceWidth();
        final int maxHeight = deviceHeight();

        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();
        int outWidth = inWidth;
        int outHeight = inHeight;

        if (inWidth > inHeight) {
            if (inWidth > maxWidth) {
                outWidth = maxWidth;
                outHeight = (inHeight * maxWidth) / inWidth;
            }
        } else {
            if (inHeight > maxHeight) {
                outHeight = maxHeight;
                outWidth = (inWidth * maxHeight) / inHeight;
            }
        }
        ;
        Log.d("outWidth", "" + outWidth);
        Log.d("outHeight", "" + outHeight);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
        return  scaled;
    }
}
