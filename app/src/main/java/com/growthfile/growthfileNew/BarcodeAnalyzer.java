package com.growthfile.growthfileNew;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;

public class BarcodeAnalyzer implements ImageAnalysis.Analyzer {

    private ProcessCameraProvider mCameraProvider;
    private OrientationEventListener morientationEventListener;
    private Context mContext;
    public void provider(ProcessCameraProvider cameraProvider) {
        mCameraProvider = cameraProvider;
    }

    public void orientation(OrientationEventListener orientationEventListener) {
        morientationEventListener = orientationEventListener;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public boolean isValidUrl(String url) {
        Resources res = mContext.getResources();
        String[] vals = res.getStringArray(R.array.app_urls);
        for(String val:vals) {
            if(url.startsWith(val)) {
                return  true;
            }
        }
        return  false;
    }

    @Override
    public void analyze(ImageProxy imageProxy) {
        @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = imageProxy.getImage();


        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            System.out.println("image: " + image.getHeight());
            BarcodeScanner scanner = BarcodeScanning.getClient();
            Task<List<Barcode>> result = scanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            // Task completed successfully
                            // ...

                            for (Barcode barcode : barcodes) {
                                Rect bounds = barcode.getBoundingBox();
                                Point[] corners = barcode.getCornerPoints();

                                String rawValue = barcode.getRawValue();

                                int valueType = barcode.getValueType();
                                // See API reference for complete list of supported types
                                switch (valueType) {
                                    case Barcode.TYPE_WIFI:
                                        String ssid = barcode.getWifi().getSsid();
                                        String password = barcode.getWifi().getPassword();
                                        int type = barcode.getWifi().getEncryptionType();
                                        break;
                                    case Barcode.TYPE_URL:
                                        String title = barcode.getUrl().getTitle();
                                        String url = barcode.getUrl().getUrl();
                                        if(isValidUrl(url)) {
                                            mCameraProvider.unbindAll();
                                            morientationEventListener.disable();
                                            MainActivity.containerLayout.setVisibility(View.VISIBLE);
                                            MainActivity.cameraLayout.setVisibility(View.GONE);
                                            MainActivity.mWebView.evaluateJavascript("handleQRUrl('" + url + "')", null);
                                        }
                                        break;
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @SuppressLint("ShowToast")
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                            Toast.makeText(mContext,"Failed to read qr code",Toast.LENGTH_LONG);
                            System.out.println(e);
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Barcode>> task) {
                            System.out.println("Task completed");
                            imageProxy.close();
                        }
                    });
        }
    }
}
