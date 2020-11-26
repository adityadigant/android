package com.growthfile.growthfileNew;

import android.annotation.SuppressLint;
import android.graphics.Camera;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;

public class BarcodeAnalyzer  implements ImageAnalysis.Analyzer {

    private ProcessCameraProvider mCameraProvider;
    public void provider(ProcessCameraProvider cameraProvider) {
        mCameraProvider = cameraProvider;
    }

    @Override
    public void analyze(ImageProxy imageProxy) {
            @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = imageProxy.getImage();
            if(mediaImage != null ){
                InputImage image = InputImage.fromMediaImage(mediaImage,imageProxy.getImageInfo().getRotationDegrees());
                System.out.println("image: "+image.getHeight());
                BarcodeScanner scanner = BarcodeScanning.getClient();
                Task<List<Barcode>> result = scanner.process(image)
                        .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                            @Override
                            public void onSuccess(List<Barcode> barcodes) {
                                // Task completed successfully
                                // ...

                                for (Barcode barcode: barcodes) {
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
                                            mCameraProvider.unbindAll();
                                            if(url.contains("https://shauryamuttreja.com")) {
                                                MainActivity.mWebView.setVisibility(View.VISIBLE);
                                                MainActivity.cameraView.setVisibility(View.GONE);

                                                MainActivity.mWebView.evaluateJavascript("handleQRUrl",null);
//                                                MainActivity.mWebView.evaluateJavascript("loadQRUrl('"+url+"')",null);

                                            }
                                            break;
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
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
