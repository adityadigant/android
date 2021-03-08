package com.growthfile.growthfileNew;

import android.Manifest;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;


import java.io.StringWriter;
import java.io.PrintWriter;

import android.location.LocationManager;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;

import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;

import androidx.camera.core.Preview;

import androidx.camera.core.TorchState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import bolts.AppLinks;

import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;

import android.telephony.TelephonyManager;

import android.text.TextUtils;

import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;


import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.provider.Settings.Secure;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.installreferrer.BuildConfig;

import com.facebook.appevents.AppEventsLogger;
import com.facebook.applinks.AppLinkData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import com.google.firebase.messaging.FirebaseMessaging;


import org.json.JSONException;
import org.json.JSONObject;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;


public class MainActivity extends AppCompatActivity {

    public static WebView mWebView;
    public static PreviewView cameraView;
    public static FrameLayout cameraLayout;
    public static FrameLayout containerLayout;

    private Context mContext;
    private BroadcastReceiver broadcastReceiver;
    private BroadcastReceiver shareRec;
    public AlertDialog airplaneDialog = null;
    public JsCallbackName jsCallbackName = null;

    private static final int CAMERA_ONLY_REQUEST = 111;
    private static final int PHOTO_CAMERA_REQUEST = 113;
    private static final int LOCATION_PERMISSION_CODE = 115;
    private static final int REQUEST_SCAN_ALWAYS_AVAILABLE = 116;
    private static final int GET_CONTACT_REQUEST = 117;
    private static final int GALLERY_REQUEST = 118;
    private static final int shareIntentCode = 119;
    public static final String BROADCAST_ACTION = "com.growthfile.growthfileNew";
    public static final String FCM_TOKEN_REFRESH = "FCM_TOKEN_REFRESH";
    public String FCM_TOKEN = "";
    private static final String TAG = MainActivity.class.getSimpleName();
    private String pictureImagePath = "";
    private Uri cameraUri;
    private ValueCallback<Uri[]> mUploadMsg;
    private boolean hasPageFinished = false;
    private boolean nocacheLoadUrl = false;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ProcessCameraProvider cameraProvider;
    Camera cameraLifeCycleBound;
    private ImageCapture imageCapture;
    private CameraSelector cameraSelector;
    OrientationEventListener orientationEventListener;
    int lensFacing = CameraSelector.LENS_FACING_BACK;
    int flashMode = ImageCapture.FLASH_MODE_OFF;
    int torchMode = TorchState.OFF;

    Executor executor = Executors.newSingleThreadExecutor();


    AppEventsLogger logger;
    private FirebaseAnalytics mFirebaseAnalytics;
    Uri deepLink = null;
    Uri facebookLink = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate", "started");
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();

        StrictMode.setVmPolicy(builder.build());
        setContentView(R.layout.activity_main);
        makeGooglePlayAvailable();
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);


        CertPin certPin = new CertPin();
        certPin.setHostname(getString(R.string.app_hostname));
        certPin.execute();

        mContext = getApplicationContext();


        if (!checkDeviceOsCompatibility()) {
            showOsUncompatibleDialog();
            return;
        }

        PackageManager pm = getApplicationContext().getPackageManager();
        boolean isWebViewInstalled = isAndroidSystemWebViewInstalled("com.google.android.webview", pm);

        if (!isWebViewInstalled) {
            webviewInstallDialog();
            return;
        }


        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };


        if (!checkLocationPermission()) {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, LOCATION_PERMISSION_CODE);
                return;
            }
            String title = "Location Permission Not Granted";
            String message = "You have Not allowed OnDuty to use location permission. Grant OnDuty Location Permission, to continue";
            showPermissionNotAllowedDialog(title, message, false);
            return;
        }
        LoadApp();
    }


    public void setCameraViewVisibility(int value) {
        findViewById(R.id.camera_view).setVisibility(value);
    }

    public void setWebViewVisibility(int value) {
        findViewById(R.id.container).setVisibility(value);
    }

    void bindPreview() {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraView.getSurfaceProvider());
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        ImageHandler imageHandler = new ImageHandler();

        Size size = new Size(480, 640);

        imageCapture =
                new ImageCapture.Builder()
                        .setTargetRotation(cameraView.getDisplay().getRotation())
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setFlashMode(flashMode)
                        .build();

        orientationEventListener = new OrientationEventListener((Context) this) {
            @Override
            public void onOrientationChanged(int orientation) {

                int rotation;
                // Monitors orientation values to determine the target rotation value
                if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    rotation = Surface.ROTATION_90;
                } else {
                    rotation = Surface.ROTATION_0;
                }
                Log.d("rotation", "deg: " + rotation);
                imageCapture.setTargetRotation(rotation);
            }
        };
        orientationEventListener.enable();


        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                // insert your code here.

                BarcodeAnalyzer barcodeAnalyzer = new BarcodeAnalyzer();
                barcodeAnalyzer.setContext(mContext);
                barcodeAnalyzer.orientation(orientationEventListener);
                barcodeAnalyzer.provider(cameraProvider);
                barcodeAnalyzer.analyze(imageProxy);
            }
        });
        cameraProvider.unbindAll();
        cameraLifeCycleBound = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageCapture, imageAnalysis, preview);


    }

    void initCamera(@NonNull ProcessCameraProvider cameraProvider) {
        containerLayout.setVisibility(View.GONE);
        cameraLayout.setVisibility(View.VISIBLE);
        bindPreview();
        setCameraControls();
    }

    void setCameraControls() {
        ImageButton captureButton = (ImageButton) this.findViewById(R.id.camera_capture_button);
        ImageButton cameraSwitchButton = (ImageButton) this.findViewById(R.id.camera_switch_button);
        ImageButton flashModeButton = (ImageButton) this.findViewById(R.id.camera_flash_mode);
        ImageButton cameraTorch = (ImageButton) this.findViewById(R.id.camera_torch);


        try {
            boolean hasCamera = cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
            cameraSwitchButton.setVisibility(hasCamera ? View.VISIBLE : View.GONE);
            cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onClick(View view) {

                    if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        lensFacing = CameraSelector.LENS_FACING_FRONT;

                    } else {
                        lensFacing = CameraSelector.LENS_FACING_BACK;
                    }
                    bindPreview();
                }
            });
        } catch (CameraInfoUnavailableException cameraInfoUnavailableException) {
            System.out.println(cameraInfoUnavailableException);
        }

        flashModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (flashMode) {
                    case ImageCapture.FLASH_MODE_OFF:
                        flashMode = ImageCapture.FLASH_MODE_ON;
                        flashModeButton.setImageResource(R.drawable.ic_flash_on);
                        break;
                    case ImageCapture.FLASH_MODE_ON:
                        flashMode = ImageCapture.FLASH_MODE_AUTO;
                        flashModeButton.setImageResource(R.drawable.ic_flash_auto);
                        break;
                    case ImageCapture.FLASH_MODE_AUTO:
                        flashMode = ImageCapture.FLASH_MODE_OFF;
                        flashModeButton.setImageResource(R.drawable.ic_flash_off);
                        break;
                }
                bindPreview();
            }
        });

        
        cameraTorch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               
                if (torchMode == TorchState.OFF) {
                    cameraLifeCycleBound.getCameraControl().enableTorch(true);
                    torchMode = TorchState.ON;
                    cameraTorch.setImageResource(R.drawable.ic_torch_on);
                    return;
                }

                cameraLifeCycleBound.getCameraControl().enableTorch(false);
                torchMode = TorchState.OFF;
                cameraTorch.setImageResource(R.drawable.ic_torch_off);


            }
        });

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File file = new File(getImageSavePath());
                ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
                imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.d("Image saved", "saved");
                        orientationEventListener.disable();
                        mWebView.post(new Runnable() {
                            @Override
                            public void run() {
                                ImageHandler imageHandler = new ImageHandler();
                                String callbackName = jsCallbackName.getName();
                                mWebView.loadUrl("javascript:" + callbackName + "('" + imageHandler.getImageOutput(file) + "')");
                                findViewById(R.id.container).setVisibility(View.VISIBLE);
                                findViewById(R.id.camera_view).setVisibility(View.GONE);
                                cameraProvider.unbindAll();

                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        System.out.println(exception);
                    }
                });

            }
        });
    }


    private void setmUploadMsgNull() {
        mUploadMsg.onReceiveValue(null);
        mUploadMsg = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);


        switch (requestCode) {

            case GALLERY_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();
                    if (uri != null) {
                        mUploadMsg.onReceiveValue(new Uri[]{uri});
                        mUploadMsg = null;
                        return;
                    }
                    setmUploadMsgNull();

                    return;
                }
                setmUploadMsgNull();

                break;
            case PHOTO_CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {
                    File imgFile = new File(pictureImagePath);
                    if (imgFile.exists()) {
                        if (cameraUri != null) {
                            mUploadMsg.onReceiveValue(new Uri[]{cameraUri});
                            mUploadMsg = null;
                            return;
                        }
                        setmUploadMsgNull();

                        return;
                    }
                    pictureImagePath = null;
                    Toast.makeText(MainActivity.this, "Please Try Again", Toast.LENGTH_LONG).show();
                    setmUploadMsgNull();

                    return;
                }

                setmUploadMsgNull();

                break;
            case GET_CONTACT_REQUEST:
                if (resultCode == RESULT_OK) {
                    Contact contact = null;
                    String callbackName = jsCallbackName.getName();
                    try {
                        Uri contactUri = intent.getData();


                        contact = fetchAndBuildContact(getApplicationContext(), contactUri);
                        Log.d("Picked Contact", contact.toString());
                        Log.d("displayName", contact.displayName);
                        Log.d("phoneNumber", contact.phoneNumber);
                        Log.d("email", contact.emailId);

                        StringBuilder sb = new StringBuilder();
                        sb.append("displayName=").append(contact.displayName).append("&phoneNumber=").append(contact.phoneNumber)
                                .append("&email=").append(contact.emailId);
                        mWebView.evaluateJavascript(callbackName + "('" + sb + "')", null);


                    } catch (Exception e) {
                        e.printStackTrace();

                        mWebView.evaluateJavascript(callbackName + "Failed('" + e.getMessage() + " at line Number " + e.getStackTrace()[0].getLineNumber() + "')", null);
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                Toast.makeText(mContext, "Failed To Pick Contact", Toast.LENGTH_LONG).show();

                break;
            case REQUEST_SCAN_ALWAYS_AVAILABLE:
                if (resultCode != RESULT_OK) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                    builder.setTitle("ALLOW WIFI SCANNING");
                    builder.setMessage("WiFi scanning improves the location accuracy of your device.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE), 116);
                            dialog.dismiss();
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return;
                }
                WifiManager wifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (!wifiManager.isScanAlwaysAvailable()) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                    builder.setTitle("ALLOW WIFI SCANNING");
                    builder.setMessage("WiFi scanning improves the location accuracy of your device.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE), 116);
                            dialog.dismiss();
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog = builder.create();

                    dialog.show();
                }

                break;
        }
    }

    private void initCameraView() {

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {

            try {
                cameraProvider = cameraProviderFuture.get();

                initCamera(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private Boolean isPermissionGranted(int[] grantResults) {
        boolean granted = true;
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                granted = false;
            }
        }
        return granted;
    }

    private  void makeGooglePlayAvailable() {
        GoogleApiAvailability googleApiAvailability = new GoogleApiAvailability();
        int available = googleApiAvailability.isGooglePlayServicesAvailable(MainActivity.this);
        if (available != ConnectionResult.SUCCESS) {
            googleApiAvailability.makeGooglePlayServicesAvailable(MainActivity.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, @NonNull int[] grantResults) {

        Boolean isGranted = isPermissionGranted(grantResults);
        switch (requestCode) {
            case CAMERA_ONLY_REQUEST:
                if (isGranted) {
                    initCameraView();
                    return;
                }
                Toast.makeText(MainActivity.this, "Permission not granted", Toast.LENGTH_LONG).show();

                break;
            case PHOTO_CAMERA_REQUEST:
                if (isGranted) {
                    startActivityForResult(photoCameraIntent(), PHOTO_CAMERA_REQUEST);
                    return;
                }
                pictureImagePath = null;
                cameraUri = null;
                break;
            case GALLERY_REQUEST:
                if (isGranted) {
                    startActivityForResult(photoGalleryIntent(), GALLERY_REQUEST);
                    return;
                }
                setmUploadMsgNull();

                break;
            case LOCATION_PERMISSION_CODE:
                if (isGranted) {
                    LoadApp();
                    return;
                }
                String title = "Location Permission";
                String message = "You have Not allowed OnDuty to use location permission. Grant OnDuty Location Permission, to continue";
                boolean cancelable = false;
                showPermissionNotAllowedDialog(title, message, cancelable);

                break;
            case GET_CONTACT_REQUEST:
                if (isGranted) {
                    startActivityForResult(getContactIntent(), GET_CONTACT_REQUEST);
                    return;
                }
                Toast.makeText(MainActivity.this, "Permission not granted", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "started");


        if (VERSION.SDK_INT <= Build.VERSION_CODES.P) {

            WifiManager wifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isScanAlwaysAvailable()) {
                startActivityForResult(new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE), 116);
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        makeGooglePlayAvailable();
        registerMyReceiver();
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(MainActivity.this);

        Log.d("onReumse", "resume");
        if (checkLocationPermission()) {
            String script = "try { backgroundTransition() }catch(e){}";
            if (mWebView != null) {
                mWebView.evaluateJavascript(script, null);
            }
        }
        if (!networkProviderEnabled()) {
            showLocationModeChangeDialog();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // make sure to unregister your receiver after finishing of this activity
        unregisterReceiver(broadcastReceiver);

    }


    private Contact fetchAndBuildContact(Context ctx, Uri contactUri) {

        Cursor cursorLookUp = ctx.getContentResolver().query(contactUri, new String[]{ContactsContract.Data.LOOKUP_KEY}, null, null, null);
        Contact contact = null;
        String loopUpKey = "";
        if (cursorLookUp.moveToFirst()) {
            loopUpKey = cursorLookUp.getString(cursorLookUp.getColumnIndex(ContactsContract.Data.LOOKUP_KEY));
            if (loopUpKey != null) {
                contact = new Contact();
                contact = buildPhoneDetails(contactUri, ctx, contact);
                contact = buildEmailDetails(loopUpKey, ctx, contact);


            }
        }
        cursorLookUp.close();

        return contact;
    }

    private Contact buildPhoneDetails(Uri contactUri, Context ctx, Contact contact) {

        ContentResolver contentResolver = ctx.getContentResolver();
//        String contactWhere = ContactsContract.Data.LOOKUP_KEY + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
//        String[] contactWhereParams = new String[]{lookUpKey, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
//        Cursor cursorPhone = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, contactWhere, contactWhereParams, null);
        Cursor cursorPhone = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone._ID + "=?",
                new String[]{contactUri.getLastPathSegment()}, null);

        if (cursorPhone.getCount() > 0) {


            if (cursorPhone.moveToNext()) {
                if (Integer.parseInt(cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    String displayName = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String phoneNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    int contactType = cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
                    contact.displayName = displayName;
                    contact.phoneNumber = phoneNumber;
                    contact.contactType = contactType;

                }
            }
        }
        cursorPhone.close();
        return contact;
    }

    private Contact buildEmailDetails(String lookUpKey, Context ctx, Contact contact) {
        ContentResolver contentResolver = ctx.getContentResolver();
        String emailWhere = ContactsContract.Data.LOOKUP_KEY + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ? ";
        String[] emailWhereParams = new String[]{lookUpKey, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE};
        Cursor emailCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, emailWhere, emailWhereParams, null);
        if (emailCursor.moveToNext()) {
            String emailId = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            contact.emailId = emailId;
        }


        emailCursor.close();
        return contact;
    }

    public class Contact {

        String displayName = "";
        String emailId = "";
        String phoneNumber = "";

        int contactType;
    }

    public class JsCallbackName {
        String functionName;

        public JsCallbackName(String name) {
            this.functionName = name;
        }

        public String getName() {
            return functionName;
        }
    }


    public void showLocationModeChangeDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Dialog_Alert);
        if (VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setTitle("Location Service Not Enabled");
            builder.setMessage("OnDuty requires Location access. Go to Settings to enable Location Services");
        } else {
            builder.setTitle("Location Mode Not Accurate");
            builder.setMessage("OnDuty requires accurate Location access. Go to Settings and set Location mode to High Accuracy");
        }

        builder.setCancelable(false);
        builder.setPositiveButton("Go To Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                dialog.dismiss();
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        Log.d("isShowing", "value " + dialog.isShowing());
        dialog.dismiss();
        dialog.cancel();
        dialog.show();
    }


    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private boolean checkDeviceOsCompatibility() {
        if (VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return false;
        }
        return true;
    }

    private void showOsUncompatibleDialog() {

        String message = "This app is incompatible with your Android device. Please upgrade your Android version to use OnDuty";
        String title = "App Incompatible";

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Dialog_Alert);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        dialog.show();

    }

    private void registerMyReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);
        intentFilter.addAction(FCM_TOKEN_REFRESH);
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(SCAN_RESULTS_AVAILABLE_ACTION);


        IntentFilter providerChangeIntent = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);

        providerChangeIntent.addAction(Intent.ACTION_PROVIDER_CHANGED);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("broadcastReceiver", "taken");
                if (intent.getAction().equals(SCAN_RESULTS_AVAILABLE_ACTION)) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        boolean success = intent.getBooleanExtra(
                                WifiManager.EXTRA_RESULTS_UPDATED, false);
                        if (success) {
                            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

                            List<ScanResult> results = wifiManager.getScanResults();
                            String scanResult = scanNearbyWifi(results);
                            mWebView.evaluateJavascript("updatedWifiScans('" + scanResult + "')", null);
                        }
                    }
                }

                if (intent.getAction().equals(BROADCAST_ACTION)) {
                    String fcmBody;
                    try {
                        fcmBody = intent.getStringExtra("fcmNotificationData");

                        mWebView.evaluateJavascript("navigator.serviceWorker.controller.postMessage("+fcmBody+")", null);
                    } catch (Exception e) {
                        androidException(e);
                        mWebView.evaluateJavascript("navigator.serviceWorker.controller.postMessage({\n" +
                                "            type: 'read'\n" +
                                "          })", null);
                    }
                }

                if (intent.getAction().equals(FCM_TOKEN_REFRESH)) {
                    mWebView.evaluateJavascript("_native.setFCMToken('" + intent.getStringExtra("new_token") + "')", null);
                }


                if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                    boolean isOn = isAirplaneModeOn(context);
                    if (isOn) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Please turn off Airplane mode");
                        builder.setCancelable(false);
                        airplaneDialog = builder.create();
                        airplaneDialog.show();
                    } else {
                        if (airplaneDialog != null) {
                            airplaneDialog.hide();
                        }
                    }

                }
                if (intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                    boolean networkProviderAvailable = networkProviderEnabled();
                    if (!networkProviderAvailable) {
                        showLocationModeChangeDialog();
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
        registerReceiver(broadcastReceiver, providerChangeIntent);

    }

    private String getImageSavePath() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";

        File storageDir = getExternalFilesDir(
                Environment.DIRECTORY_PICTURES);

        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        return pictureImagePath;

    }

    private Intent photoCameraIntent() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(getImageSavePath());
        Uri outputFileUri = Uri.fromFile(file);
        cameraUri = outputFileUri;
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        return takePicture;

    }

    private Intent photoGalleryIntent() {
        Intent choosePicture = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        choosePicture.setType("image/*");
        return choosePicture;
    }

    private Intent getContactIntent() {
        Intent getContact = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);


        return getContact;
    }

    ;

    private void LoadApp() {


        this.mWebView = findViewById(R.id.activity_main_webview);
        this.cameraView = findViewById(R.id.previewView);

        WebSettings webSettings = this.mWebView.getSettings();
        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "AndroidInterface");

        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webSettings.setDomStorageEnabled(true);

        webSettings.setAllowFileAccess(true);
        webSettings.setGeolocationEnabled(true);

        webSettings.setDatabaseEnabled(true);
        webSettings.setUseWideViewPort(true);

        webSettings.setGeolocationDatabasePath(getApplicationContext().getFilesDir().getPath());
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setScrollbarFadingEnabled(true);
        mWebView.loadUrl(getString(R.string.app_url));
        mWebView.requestFocus(View.FOCUS_DOWN);
        mWebView.setWebContentsDebuggingEnabled(true);
        registerForContextMenu(mWebView);
        logger = AppEventsLogger.newLogger(MainActivity.this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        cameraLayout = findViewById(R.id.camera_view);
        containerLayout = findViewById(R.id.container);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            try {
                                mWebView.evaluateJavascript("fcmTokenRegistrationFailed('" + task.getException().getLocalizedMessage() + "')", null);
                            }catch (Exception e) {}
                            Log.w("MainActivity", "getInstanceId failed", task.getException());
                            return;
                        }
                        String token = task.getResult();
                        Log.e("FCMToken", token);
                        FCM_TOKEN = token;
                    }
                });

        Uri targetUrl =
                AppLinks.getTargetUrlFromInboundIntent(this, getIntent());
        if (targetUrl != null) {
            Log.i("Activity", "App Link Target URL: " + targetUrl.toString());
            facebookLink = targetUrl;
        }

        AppLinkData.fetchDeferredAppLinkData(MainActivity.this, new AppLinkData.CompletionHandler() {
            @Override
            public void onDeferredAppLinkDataFetched(@Nullable AppLinkData appLinkData) {
                if (appLinkData != null) {
                    facebookLink = appLinkData.getTargetUri();

                }
            }
        });

        Uri URIdata = getIntent().getData();
        if (URIdata != null) {

            mWebView.evaluateJavascript("localStorage.setItem('deep_link_app', '"+URIdata.toString()+"')",null);
        }

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {

                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            Log.d("deeplink", deepLink.toString());
                        }

                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...
                        // ...
                    }
                })
                .addOnFailureListener(MainActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

                android.util.Log.d("WebView message", consoleMessage.message() + " line number " + consoleMessage.lineNumber() + " of " + consoleMessage.sourceId() + "full" + consoleMessage.toString());
                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
                mUploadMsg = uploadMsg;
                final String[] PERMISSIONS_PHOTO_CAMERA = {
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                };

                final String[] PERMISSIONS_PHOTO_GALLERY = {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                };

                final Context context = MainActivity.this;
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Choose Image Location");
                builder.setCancelable(true);
                builder.setItems(new CharSequence[]
                                {"Camera", "Gallery"},
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        if (!hasPermissions(MainActivity.this, PERMISSIONS_PHOTO_CAMERA)) {

                                            if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_PHOTO_CAMERA, PHOTO_CAMERA_REQUEST);
                                            } else {
                                                String title = "Storage and Camera Permission";
                                                String message = "OnDuty requires access to your phone Camera and Gallery for photo uploads.";
                                                showPermissionNotAllowedDialog(title, message, true);
                                            }
                                        } else {

                                            startActivityForResult(photoCameraIntent(), PHOTO_CAMERA_REQUEST);

                                        }

                                        break;
                                    case 1:
                                        if (!hasPermissions(MainActivity.this, PERMISSIONS_PHOTO_GALLERY)) {
                                            if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_PHOTO_GALLERY, GALLERY_REQUEST);
                                            } else {
                                                String title = "Storage and Camera Permission";
                                                String message = "OnDuty requires access to your phone Camera and Gallery for photo uploads.";
                                                showPermissionNotAllowedDialog(title, message, true);
                                            }
                                        } else {
                                            startActivityForResult(photoGalleryIntent(), GALLERY_REQUEST);
                                        }
                                        break;
                                }
                            }
                        });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mUploadMsg.onReceiveValue(null);
                        mUploadMsg = null;
                    }
                });
                builder.create().show();
                return true;
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (nocacheLoadUrl) return;
                String fcmScript = "if(_native) {_native.setFCMToken('"+FCM_TOKEN+"')}";
                mWebView.evaluateJavascript(fcmScript, null);
                if (!hasPageFinished) {

                    Log.d("onPageFinished", "true");
                    mWebView.evaluateJavascript("_native.setName('Android')", null);
                    hasPageFinished = true;



                    if (getIntent().getExtras() != null) {
                        JSONObject fcmBody = new JSONObject();

                        for (String key : getIntent().getExtras().keySet()) {
                            Object value = getIntent().getExtras().get(key);

                            try {
                                fcmBody.put(key, value);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            fcmBody.put("notification", true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        try {
                            Log.d("fcmBody", fcmBody.toString(4));
                            mWebView.evaluateJavascript("navigator.serviceWorker.controller.postMessage(" + fcmBody.toString(4) + ")", null);

                        } catch (JSONException e) {
                            androidException(e);
                        }
                    }
                    if (deepLink != null) {
                        Log.d("string", deepLink.toString());
                        mWebView.evaluateJavascript("getDynamicLink('" + deepLink.toString() + "')", null);
                        deepLink = null;
                    }
                    if (facebookLink != null) {
                        mWebView.evaluateJavascript("getDynamicLink('" + facebookLink.toString() + "')", null);
                    }
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);

                if (url.contains("geo:")) {
                    Intent mapIntent = new Intent("android.intent.action.VIEW", uri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                        MainActivity.this.startActivity(mapIntent);
                    }
                    return true;
                }
                if (url.startsWith("mailto:") || url.startsWith("sms:")) {

                    startActivity(new Intent(Intent.ACTION_SENDTO, uri));
                    return true;
                }

                if (url.startsWith("tel:")) {
                    startActivity(new Intent(Intent.ACTION_DIAL, uri));
                    return true;
                }

                if (url.startsWith("https://wa.me")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                }

                if (url.startsWith(getString(R.string.app_url))) {
                    view.loadUrl(url);
                    return true;
                }

                if (url.startsWith("https://")) {

                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                }

                view.loadUrl(url);
                return true;
            }


            @RequiresApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                shouldOverrideUrlLoading(view, url);
                return true;
            }

            @Override
            public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
                Toast.makeText(MainActivity.this, description, Toast.LENGTH_LONG).show();
                mWebView.loadUrl("file:///android_asset/nocache.html");
            }
        });


    }


    private void webviewInstallDialog() {
        String message = "This app is incompatible with your Android device. To use this app, click Ok to install/update Android System WebView";
        String title = "App Incompatibility Issue ";
        final String pckgName = "com.google.android.webview";

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Dialog_Alert);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pckgName)));
                } catch (android.content.ActivityNotFoundException noPs) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pckgName)));
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void showPermissionNotAllowedDialog(String title, String message, boolean cancelable) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(cancelable);
        builder.show();
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.isAvailable();

    }

    private boolean isAndroidSystemWebViewInstalled(String pckgname, PackageManager packageManager) {

        if (isDeviceBelowNougat()) {
            try {

                if (packageManager.getApplicationInfo(pckgname, 0).enabled) {
                    //minor hack
                    int stableVersion = 70;

                    PackageInfo pi = packageManager.getPackageInfo(pckgname, 0);
                    String shortenVersionName = pi.versionName.length() < 2 ? pi.versionName : pi.versionName.substring(0, 2);
                    int parsedShortenVersion = Integer.parseInt(shortenVersionName);
                    return parsedShortenVersion >= stableVersion;
                }
                return false;

            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        } else {
            return true;
        }

    }

    private boolean isDeviceBelowNougat() {
        return VERSION.SDK_INT < Build.VERSION_CODES.N && VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    private boolean networkProviderEnabled() {

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        assert service != null;
        if (VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return service.isLocationEnabled();
        }
        return service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    }


    private boolean checkLocationPermission() {

        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };

        return hasPermissions(MainActivity.this, PERMISSIONS);
    }

    public void androidException(final Exception e) {
        Log.d("exception log", e.getMessage());
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        final String stack = sw.getBuffer().toString().replaceAll("\n", "");
        Log.d("stack", stack);

        try {
            mWebView.loadUrl("javascript:jniException('" + e.getMessage() + "','" + stack + "')");
        } catch (Exception webViewEx) {
            webViewEx.printStackTrace();
        }
    }

    public String getMCC(TelephonyManager tm) {
        if (tm == null) return "";
        if (tm.getNetworkOperator().isEmpty()) {
            return "";
        }
        String operator = tm.getNetworkOperator();
        return operator.substring(0, 3);
    }

    ;


    public String getMNC(TelephonyManager tm) {
        if (tm == null) return "";
        if (tm.getNetworkOperator().isEmpty()) {
            return "";
        }
        String operator = tm.getNetworkOperator();
        return operator.substring(3);
    }

    public String getRadioName(TelephonyManager tm) {
        int networkType = tm.getNetworkType();
        String type = "";
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                type = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                type = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                type = "GSM";
                break;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                type = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                type = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                type = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                type = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                type = "GSM";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                type = "WCDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                type = "WCDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                type = "WCDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                type = "WCDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                type = "LTE";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                type = "WCDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                type = "";
                break;

        }
        return type;
    }

    public int channel(int freq) {
        if (freq == 2484)
            return 14;
        if (freq >= 2412 && freq < 2484) {
            return (freq - 2412) / 5 + 1;
        } else if (freq >= 5170 && freq <= 5825) {
            return (freq - 5170) / 5 + 34;
        } else {
            return 0;
        }

    }

    public String scanNearbyWifi(List<ScanResult> wifiList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wifiList.size(); i++) {
            String bssid = wifiList.get(i).BSSID;
            Integer ss = wifiList.get(i).level;

            if (bssid != null) {
                sb.append("macAddress=").append(bssid).append("&").append("signalStrength=").append(ss).append("&").append("channel=").append(channel(wifiList.get(i).frequency));
                sb.append(",");
            }
        }

        if (sb.length() != 0) {
            sb.deleteCharAt(sb.lastIndexOf(","));
            return sb.toString();
        }

        return "";

    }

    public String getAllCelltowerInfo(List<CellInfo> cellInfoList) {


        StringBuilder sb = new StringBuilder();


        for (final CellInfo info : cellInfoList) {
            int mcc = 0;
            int mnc = 0;
            int lac = 0;
            int signalStrength = 0;
            int cid = 0;
            int timingAdvance = 0;

            if (info instanceof CellInfoGsm) {

                final CellSignalStrengthGsm signalStrengthGsm = ((CellInfoGsm) info).getCellSignalStrength();

                final CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();

                if (identityGsm != null) {
                    cid = identityGsm.getCid();
                    lac = identityGsm.getLac();

                    if (VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        mcc = identityGsm.getMcc();
                        mnc = identityGsm.getMnc();
                    } else {
                        if (identityGsm.getMccString() != null && identityGsm.getMncString() != null) {

                            mcc = Integer.parseInt(identityGsm.getMccString());
                            mnc = Integer.parseInt(identityGsm.getMncString());
                        }
                    }

                    if (signalStrengthGsm != null) {
                        signalStrength = signalStrengthGsm.getDbm();
                        if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            timingAdvance = signalStrengthGsm.getTimingAdvance();
                        }
                    }
                }
            }

            if (info instanceof CellInfoWcdma) {
                final CellSignalStrengthWcdma signalStrengthWcdma = ((CellInfoWcdma) info).getCellSignalStrength();

                final CellIdentityWcdma identityWcdma = ((CellInfoWcdma) info).getCellIdentity();
                if (identityWcdma != null) {
                    cid = identityWcdma.getCid();
                    lac = identityWcdma.getLac();


                    if (VERSION.SDK_INT < Build.VERSION_CODES.P) {

                        mcc = identityWcdma.getMcc();
                        mnc = identityWcdma.getMnc();
                    } else {
                        if (identityWcdma.getMccString() != null && identityWcdma.getMncString() != null) {

                            mcc = Integer.parseInt(identityWcdma.getMccString());
                            mnc = Integer.parseInt(identityWcdma.getMncString());
                        }
                    }
                    if (signalStrengthWcdma != null) {
                        signalStrength = signalStrengthWcdma.getDbm();

                    }

                }

            }
            if (info instanceof CellInfoLte) {
                final CellSignalStrengthLte signalStrengthLte = ((CellInfoLte) info).getCellSignalStrength();
                final CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
                if (identityLte != null) {
                    cid = identityLte.getCi();
                    lac = identityLte.getTac();


                    if (VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        mcc = identityLte.getMcc();
                        mnc = identityLte.getMnc();

                    } else {
                        if (identityLte.getMccString() != null && identityLte.getMncString() != null) {

                            mcc = Integer.parseInt(identityLte.getMccString());
                            mnc = Integer.parseInt(identityLte.getMncString());
                        }
                    }
                    if (signalStrengthLte != null) {
                        signalStrength = signalStrengthLte.getDbm();
                        timingAdvance = signalStrengthLte.getTimingAdvance();
                    }

                }
            }

            if (info instanceof CellInfoCdma) {
                final CellSignalStrengthCdma signalStrengthCdma = ((CellInfoCdma) info).getCellSignalStrength();
                final CellIdentityCdma identityCdma = ((CellInfoCdma) info).getCellIdentity();
                if (signalStrengthCdma != null) {
                    cid = identityCdma.getBasestationId();

                    lac = identityCdma.getNetworkId();

                    mnc = identityCdma.getSystemId();
                    TelephonyManager tm = (TelephonyManager) MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
                    mcc = Integer.parseInt(getMCC(tm));

                    signalStrength = signalStrengthCdma.getDbm();


                }

            }


            sb.append("mobileCountryCode=").append(mcc).append("&").append("mobileNetworkCode=").append(mnc).append("&").append("cellId=").append(cid).
                    append("&").append("locationAreaCode=").append(lac).append("&")
                    .append("signalStrength=").append(signalStrength);
            if (timingAdvance != 0 && timingAdvance != Integer.MAX_VALUE) {
                sb.append("&").append("timingAdvance=").append(timingAdvance);
            }
            sb.append(",");

        }
        if (sb.length() != 0) {
            sb.deleteCharAt(sb.lastIndexOf(","));
            return sb.toString();
        }
        return "";
    }

    private void LOGD(String message) {
        // Only log on debug builds, for privacy
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }

    private Bundle bundleFromJson(String json) {
        // [START_EXCLUDE]
        if (TextUtils.isEmpty(json)) {
            return new Bundle();
        }

        Bundle result = new Bundle();
        try {
            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> keys = jsonObject.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObject.get(key);

                if (value instanceof String) {
                    result.putString(key, (String) value);
                } else if (value instanceof Integer) {
                    result.putInt(key, (Integer) value);
                } else if (value instanceof Double) {
                    result.putDouble(key, (Double) value);
                } else {
                    Log.w(TAG, "Value for key " + key + " not one of [String, Integer, Double]");
                }
            }
        } catch (JSONException e) {
            Log.w(TAG, "Failed to parse JSON, returning empty Bundle.", e);
            return new Bundle();
        }

        return result;
        // [END_EXCLUDE]
    }


    public class viewLoadJavaInterface {
        Context mContext;

        viewLoadJavaInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void loadQRPage(String token, String latitude, String longitude, String url) {
            HashMap<String, String> headersMap = new HashMap<>();

            headersMap.put("Authorization", "Bearer " + token);
            headersMap.put("latitude", latitude);
            headersMap.put("longitude", longitude);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(url, headersMap);

                }
            });
        }

        @JavascriptInterface
        public void openGooglePlayStore(String appId) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appId)));
            } catch (android.content.ActivityNotFoundException noPs) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appId)));
            }
        }

        @JavascriptInterface
        public void logFirebaseAnlyticsEvent(String name, String jsonParams) {
            LOGD("logEvent:" + name);
            mFirebaseAnalytics.logEvent(name, bundleFromJson(jsonParams));
        }

        @JavascriptInterface
        public void setFirebaseAnalyticsUserProperty(String name, String value) {
            LOGD("setUserProperty:" + name);
            mFirebaseAnalytics.setUserProperty(name, value);
        }

        @JavascriptInterface
        public void setFirebaseAnalyticsUserId(String id) {
            mFirebaseAnalytics.setUserId(id);
        }

        @JavascriptInterface
        public void setAnalyticsCollectionEnabled(Boolean enable) {
            mFirebaseAnalytics.setAnalyticsCollectionEnabled(enable);
        }


        @JavascriptInterface
        public boolean isWifiOn() {
            WifiManager wifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            return wifiManager.isWifiEnabled();
        }


        @JavascriptInterface
        public void startCamera(String functionName) {
            String[] PERMISSIONS_CAMERA = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            jsCallbackName = new JsCallbackName(functionName);
            if (!hasPermissions(MainActivity.this, PERMISSIONS_CAMERA)) {
                if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_CAMERA, CAMERA_ONLY_REQUEST);
                    return;
                }
                String title = "Storage and Camera Permission";
                String message = "Allow Storage and Camera Permission to get Picture";
                showPermissionNotAllowedDialog(title, message, true);
                return;
            }
            initCameraView();
        }

        // device Info
        @JavascriptInterface
        public String getId() {
            try {
                return Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (SecurityException e) {
                return "";
            }

        }

        @JavascriptInterface
        public String getDeviceBrand() {
            return Build.MANUFACTURER;
        }

        @JavascriptInterface
        public String getDeviceModel() {
            return Build.MODEL;

        }

        @JavascriptInterface
        public String getAppVersion() {
            try {

                PackageInfo packageInfo = MainActivity.this.getPackageManager().getPackageInfo(MainActivity.this.getPackageName(), 0);

                if (VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    return Long.toString((int) packageInfo.getLongVersionCode());
                }

                return Integer.toString(packageInfo.versionCode);

            } catch (PackageManager.NameNotFoundException e) {
                return "51";
            }
        }

        @JavascriptInterface
        public String getOsVersion() {
            return VERSION.RELEASE;
        }

        @JavascriptInterface
        public String getBaseOs() {
            return "android";
        }

        @JavascriptInterface
        public String getRadioVersion() {
            return Build.getRadioVersion();
        }

        @JavascriptInterface
        public boolean isLocationPermissionGranted() {
            return checkLocationPermission();
        }

        // Cellular Interfaces
        @JavascriptInterface
        public String getMobileCountryCode() {
            TelephonyManager tm = (TelephonyManager) MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);

            return getMCC(tm);

        }

        ;

        @JavascriptInterface
        public void loadOffline() {
            mWebView.loadUrl("file:///android_asset/nocache.html");
        }

        @JavascriptInterface
        public String getMobileNetworkCode() {

            TelephonyManager tm = (TelephonyManager) MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);

            return getMNC(tm);

        }

        ;

        @JavascriptInterface
        public String getRadioType() {

            TelephonyManager tm = (TelephonyManager) MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
            return getRadioName(tm);
        }

        ;

        @JavascriptInterface
        public String getCarrier() {

            TelephonyManager tm = (TelephonyManager) MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
            String carrier = tm.getNetworkOperatorName();
            if (carrier != null && !carrier.isEmpty()) {
                return carrier;
            }
            return "";
        }

        @JavascriptInterface
        public String getWifiAccessPoints() {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return "";
            }
            WifiManager wifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) return "";
            List<ScanResult> wifiList = wifiManager.getScanResults();
            return scanNearbyWifi(wifiList);

        }

        @JavascriptInterface
        public String getCellTowerInformation() {

            TelephonyManager tm = (TelephonyManager) MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null) {
                return "";
            }
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return "";
            }


            List<CellInfo> cellInfoList = tm.getAllCellInfo();

            if (cellInfoList == null || cellInfoList.isEmpty()) return "";
            return getAllCelltowerInfo(cellInfoList);

        }

        @JavascriptInterface
        public void getContact(String functionName) {
            String[] PERMISSIONS = {
                    Manifest.permission.READ_CONTACTS

            };

            jsCallbackName = new JsCallbackName(functionName);
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, GET_CONTACT_REQUEST);
            } else {
                startActivityForResult(getContactIntent(), GET_CONTACT_REQUEST);
            }

        }

        @JavascriptInterface
        public void logEvent(String eventName) {
            logger.logEvent(eventName);
        }

        @JavascriptInterface
        public void share(String shareObject) {
            try {
                JSONObject data = new JSONObject(shareObject);
                Intent sendIntent = new Intent();
                String title = data.getString("title");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, data.getString("shareText"));
                sendIntent.setType(data.getString("type"));
                try {
                    JSONObject emailData = new JSONObject(data.getString("email"));
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, emailData.getString("subject"));
                    sendIntent.putExtra(Intent.EXTRA_CC, new String[]{emailData.getString("cc")});
                    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailData.getString("to")});

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                sendIntent.putExtra(Intent.EXTRA_TITLE, title);

                PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, shareIntentCode,
                        new Intent(MainActivity.this, ShareBroadcastReceiver.class),
                        FLAG_UPDATE_CURRENT);


                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                    Intent shareIntent = Intent.createChooser(sendIntent, title, pi.getIntentSender());
                    startActivity(shareIntent);
                }
            } catch (Exception ex) {

            }

        }

    }

    @Override
    public void onBackPressed() {
        if (cameraLayout.getVisibility() == View.VISIBLE) {
            setCameraViewVisibility(View.GONE);
            setWebViewVisibility(View.VISIBLE);
            flashMode = ImageCapture.FLASH_MODE_AUTO;
            lensFacing = CameraSelector.LENS_FACING_BACK;
            cameraProvider.unbindAll();
            orientationEventListener.disable();
            return;
        }
        if (mWebView.canGoBack()) {
            mWebView.goBack(); // emulates back history
            return;
        }
        super.onBackPressed();
    }

}
