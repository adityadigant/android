package com.growthfile.growthfileNew;

import android.Manifest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.graphics.Matrix;
import android.location.LocationManager;
import android.media.ExifInterface;
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
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

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

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
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
import java.util.List;


import android.provider.Settings.Secure;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    SwipeRefreshLayout swipeToRefresh;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;
    private Context mContext;
    private BroadcastReceiver broadcastReceiver;
    public AlertDialog appAlert;
    public AlertDialog airplaneDialog = null;
    public  Camera_View camera_view = null;

    private static final int CAMERA_ONLY_REQUEST = 111;
    private static final int PHOTO_GALLERY_REQUEST = 112;
    private static final int PHOTO_CAMERA_REQUEST = 113;
    private static final int LOCATION_PERMISSION_CODE = 115;
    private static final int REQUEST_SCAN_ALWAYS_AVAILABLE = 116;
    private  static  final  int GET_CONTACT_REQUEST = 117;
    private  static  final  int GALLERY_REQUEST = 118;
    public static final String BROADCAST_ACTION = "com.growthfile.growthfileNew";
    private static final String TAG = MainActivity.class.getSimpleName();
    public String contactCallbackFunctionName = "";
    private String pictureImagePath = "";
    private  Uri cameraUri;
    private  ValueCallback<Uri[]> mUploadMsg;
    private boolean hasPageFinished = false;
    private boolean nocacheLoadUrl = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate", "started");
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();

        StrictMode.setVmPolicy(builder.build());
        setContentView(R.layout.activity_main);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        new CertPin().execute();

        mContext = getApplicationContext();
//        swipeToRefresh = findViewById(R.id.swipeToRefresh);
//        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//
//                runRead();
//            }
//
//        });
//        swipeToRefresh.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
//            @Override
//            public void onScrollChanged() {
//                if (mWebView.getScrollY() == 0) {
//                    swipeToRefresh.setEnabled(true);
//                } else {
//                    swipeToRefresh.setEnabled(false);
//                }
//            }
//        });

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
            } else {
                String title = "Location Permission Not Granted";
                String message = "You have Not allowed Growthfile to use location permission. Grant Growthfile Location Permission, to continue";
                showPermissionNotAllowedDialog(title, message, false);
            }
        } else {
            LoadApp();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case CAMERA_ONLY_REQUEST:
                if (resultCode == RESULT_OK) {
                    File imgFile = new File(pictureImagePath);
                    if (imgFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        final  int maxWidth = deviceWidth();
                        final int maxHeight = deviceHeight();

                        int inWidth = bitmap.getWidth();
                        int inHeight = bitmap.getHeight();
                        int outWidth=inWidth;
                        int outHeight=inHeight;

                        if(inWidth > inHeight){
                            if(inWidth > maxWidth) {
                                outWidth = maxWidth;
                                outHeight = (inHeight * maxWidth) / inWidth;
                            }
                        } else {
                            if(inHeight > maxHeight) {
                                outHeight = maxHeight;
                                outWidth = (inWidth * maxHeight) / inHeight;
                            }
                        }
                        Log.d("outWidth",""+outWidth);
                        Log.d("outHeight",""+outHeight);
                        Bitmap scaled = Bitmap.createScaledBitmap(bitmap,outWidth,outHeight,false);

                        String callbackName = camera_view.getName();

                        try {
                            Bitmap changedBit = null;
                            ExifInterface ei = new ExifInterface(imgFile.getAbsolutePath());
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                            switch (orientation) {
                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    changedBit =  rotate(scaled, 90);
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
                                default:
                                    changedBit = scaled;
                            }
                            Log.d("script","setFilePath('"+encodeImage(changedBit)+"')");
                            Log.d("script2",callbackName+"('"+encodeImage(changedBit)+"')");

                            mWebView.evaluateJavascript("setFilePath('"+encodeImage(changedBit)+"')",null);

                        } catch (IOException e) {
                            mWebView.evaluateJavascript(callbackName+"('" + encodeImage(scaled) + "')",null);
                            e.printStackTrace();
                        }

                    }
                }
                break;

            case GALLERY_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();

                    if (uri != null) {
                        mUploadMsg.onReceiveValue(new Uri[]{uri});
                        mUploadMsg = null;
                    } else {
                        mUploadMsg.onReceiveValue(null);
                        mUploadMsg = null;
                    }
                }
                else {
                    mUploadMsg.onReceiveValue(null);
                    mUploadMsg = null;

                }

                break;
            case PHOTO_CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {
                    File imgFile = new File(pictureImagePath);
                    if (imgFile.exists()) {
                        if (cameraUri != null) {
                            mUploadMsg.onReceiveValue(new Uri[]{cameraUri});
                            mUploadMsg = null;
                        } else {
                            mUploadMsg.onReceiveValue(null);
                            mUploadMsg = null;
                        }
                    }
                    else  {
                        pictureImagePath = null;
                        Toast.makeText(MainActivity.this,"Please Try Again",Toast.LENGTH_LONG).show();
                        mUploadMsg.onReceiveValue(null);
                        mUploadMsg = null;
                    }
                }
                else {
                    mUploadMsg.onReceiveValue(null);
                    mUploadMsg = null;

                }
                break;
            case GET_CONTACT_REQUEST:
                if(resultCode == RESULT_OK) {
                   Contact contact = null;
                    try {
                        Uri contactUri = intent.getData();


                        contact = fetchAndBuildContact(getApplicationContext(),contactUri);
                        Log.d("Picked Contact",contact.toString());
                        Log.d("displayName",contact.displayName);
                        Log.d("phoneNumber",contact.phoneNumber);
                        Log.d("email",contact.emailId);

                        StringBuilder sb = new StringBuilder();
                        sb.append("displayName=").append(contact.displayName).append("&phoneNumber=").append(contact.phoneNumber)
                                .append("&email=").append(contact.emailId);
                        mWebView.evaluateJavascript("window['"+contactCallbackFunctionName+"']('"+sb+"')",null);


                    }
                    catch (Exception e) {
                        e.printStackTrace();

                        mWebView.evaluateJavascript("window['"+contactCallbackFunctionName+"Failed']('"+e.getMessage()+" at line Number "+ e.getStackTrace()[0].getLineNumber()+"')",null);
                        Toast.makeText(mContext,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(mContext,"Failed To Pick Contact",Toast.LENGTH_LONG).show();
                }

                break;
            case REQUEST_SCAN_ALWAYS_AVAILABLE:
                if (resultCode != RESULT_OK) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                    builder.setTitle("ALLOW WIFI SCANNING");
                    builder.setMessage("Allowing wifi scanning improves location accuracy of your device.");
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
                } else {
                    WifiManager wifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (!wifiManager.isScanAlwaysAvailable()) {

                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                        builder.setTitle("ALLOW WIFI SCANNING");
                        builder.setMessage("Allowing wifi scanning improves location accuracy of your device");
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

                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (requestCode == CAMERA_ONLY_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                try {
                    startActivityForResult(photoCameraIntent(), CAMERA_ONLY_REQUEST);
                } catch (IOException e) {
                    e.printStackTrace();
                    createIntentForCameraOnly();
                }
            }
        }
        else if (requestCode == PHOTO_CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                try {
                    startActivityForResult(photoCameraIntent(), PHOTO_CAMERA_REQUEST);

                } catch (IOException e) {
                    e.printStackTrace();
                    pictureImagePath = null;
                    cameraUri = null;
                    Toast.makeText(MainActivity.this,"Failed To Open Camera. Please Choose From Gallery",Toast.LENGTH_LONG).show();
                }

            }
            else {
                pictureImagePath = null;
                cameraUri = null;
            }
        } else if (requestCode == GALLERY_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(photoGalleryIntent(), GALLERY_REQUEST);
            }
            else {
                mUploadMsg.onReceiveValue(null);
                mUploadMsg = null;
            }
        }

        else if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {

                    String title = "Location Permission";
                    String message = "You have Not allowed Growthfile to use location permission. Grant Growthfile Location Permission, to continue";
                    boolean cancelable = false;

                    showPermissionNotAllowedDialog(title, message, cancelable);
                } else {
                    LoadApp();
                }
            }
        }
        else if(requestCode == GET_CONTACT_REQUEST) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                startActivityForResult(getContactIntent(),GET_CONTACT_REQUEST);
            }
            else {
                Toast.makeText(MainActivity.this,"Permission not granted",Toast.LENGTH_LONG);
            }


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
        registerMyReceiver();


        Log.d("onReumse", "resume");
        if (checkLocationPermission()) {
            try {

                String script = "try { runRead() }catch(e){}";
                mWebView.evaluateJavascript(script, null);
            } catch (Exception e) {
                e.printStackTrace();
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
//        swipeToRefresh.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // make sure to unregister your receiver after finishing of this activity
        unregisterReceiver(broadcastReceiver);
    }



    private Contact fetchAndBuildContact(Context ctx, Uri contactUri) {

        Cursor cursorLookUp = ctx.getContentResolver().query(contactUri,new String[]{ContactsContract.Data.LOOKUP_KEY},null,null,null);
        Contact contact = null;
        String loopUpKey = "";
        if(cursorLookUp.moveToFirst()) {
                loopUpKey = cursorLookUp.getString(cursorLookUp.getColumnIndex(ContactsContract.Data.LOOKUP_KEY));
                if(loopUpKey != null) {
                    contact = new Contact();
                    contact = buildPhoneDetails(contactUri,ctx,contact);
                    contact = buildEmailDetails(loopUpKey,ctx,contact);


                }
        }
        cursorLookUp.close();

        return contact;
    }

    private  Contact buildPhoneDetails(Uri contactUri,Context ctx,Contact contact) {

        ContentResolver contentResolver = ctx.getContentResolver();
//        String contactWhere = ContactsContract.Data.LOOKUP_KEY + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
//        String[] contactWhereParams = new String[]{lookUpKey, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
//        Cursor cursorPhone = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, contactWhere, contactWhereParams, null);
        Cursor cursorPhone = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone._ID + "=?",
                new String[]{contactUri.getLastPathSegment()}, null);

        if(cursorPhone.getCount() > 0) {


            if(cursorPhone.moveToNext()) {
                if(Integer.parseInt(cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
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

    private  Contact buildEmailDetails(String lookUpKey,Context ctx,Contact contact) {
        ContentResolver contentResolver = ctx.getContentResolver();
        String emailWhere = ContactsContract.Data.LOOKUP_KEY+" = ? AND "+ContactsContract.Data.MIMETYPE+" = ? ";
        String[] emailWhereParams = new String[]{lookUpKey,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE};
        Cursor emailCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,null,emailWhere,emailWhereParams,null);
        if(emailCursor.moveToNext()) {
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
    public class Camera_View {
        String callbackFunctionName;

        public  Camera_View(String functionName) {
            this.callbackFunctionName = functionName;
        }
        public String getName(){
            return callbackFunctionName;
        }
    }

    public int deviceWidth(){
        return Resources.getSystem().getDisplayMetrics().widthPixels;

    }
    public int deviceHeight(){
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

    public void showLocationModeChangeDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Dialog_Alert);
        if (VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setTitle("Location Service Not Enabled");
            builder.setMessage("Growthfile requires Location Access. Click go to settings, To enable Location Services");
        } else {
            builder.setTitle("Location Mode Not Set");
            builder.setMessage("Growthfile requires Location Access. Click go to settings, to set location mode to high accuracy.");
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


    private String encodeImage(Bitmap bm) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        Log.d("encoded", encoded);
        return encoded;
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

        String message = "This App is incompatible with your Android Device. Please upgrade your android version to use Growthfile";
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
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        IntentFilter providerChangeIntent = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);

        providerChangeIntent.addAction(Intent.ACTION_PROVIDER_CHANGED);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("broadcastReceiver", "taken");
                if (intent.getAction().equals(BROADCAST_ACTION)) {
                    String fcmBody;
                    try {
                        fcmBody = intent.getStringExtra("fcmNotificationData");

                        mWebView.evaluateJavascript("runRead(" + fcmBody + ")", null);

                    } catch (Exception e) {
                        androidException(e);
                        mWebView.evaluateJavascript("runRead()", null);
                    }
                }


                if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                    boolean isOn = isAirplaneModeOn(context);
                    if (isOn) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Please turn off airplane mode");
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



    private Intent photoCameraIntent() throws IOException {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";

        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(pictureImagePath);
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
    private Intent getContactIntent(){
        Intent getContact  = new Intent(Intent.ACTION_PICK,ContactsContract.CommonDataKinds.Phone.CONTENT_URI);


        return getContact;
    };

    private void LoadApp() {


        this.mWebView = findViewById(R.id.activity_main_webview);
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

        mWebView.loadUrl("https://growthfile-testing.firebaseapp.com/v1/");
        mWebView.setWebContentsDebuggingEnabled(true);
        mWebView.requestFocus(View.FOCUS_DOWN);
        mWebView.setLongClickable(true);
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg,WebChromeClient.FileChooserParams fileChooserParams){
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
                builder.setCancelable(false);
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
                                                String message = "Allow Storage and Camera Permission to get Picture";
                                                showPermissionNotAllowedDialog(title, message, true);
                                            }
                                        } else {
                                            try {
                                                startActivityForResult(photoCameraIntent(), PHOTO_CAMERA_REQUEST);
                                            } catch (IOException e) {
                                                Toast.makeText(MainActivity.this,"Cannot Open Camera. Please Choose from Gallery",Toast.LENGTH_LONG).show();
                                                androidException(e);
                                            }
                                        }

                                        break;
                                    case 1:
                                        if (!hasPermissions(MainActivity.this, PERMISSIONS_PHOTO_GALLERY)) {
                                            if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_PHOTO_GALLERY, GALLERY_REQUEST);
                                            } else {
                                                String title = "Storage and Camera Permission";
                                                String message = "Allow Storage and Camera Permission to get Picture";
                                                showPermissionNotAllowedDialog(title, message, true);
                                            }
                                        } else {
                                            startActivityForResult(photoGalleryIntent(), GALLERY_REQUEST);
                                        }
                                        break;
                                }
                            }
                        });
                builder.create().show();
                return  true;
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (nocacheLoadUrl) return;
                if (!hasPageFinished) {
                    Log.d("onPageFinished", "true");
                    mWebView.evaluateJavascript("native.setName('Android')", null);
                    hasPageFinished = true;
                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w("MainActivity", "getInstanceId failed", task.getException());
                                        return;
                                    }
                                    String token = task.getResult().getToken();
                                    Log.e("FCMToken", token);
                                    mWebView.evaluateJavascript("native.setFCMToken('" + token + "')", null);
                                }
                            });


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
                            mWebView.evaluateJavascript("runRead(" + fcmBody.toString(4) + ")", null);

                        } catch (JSONException e) {
                            androidException(e);
                        }
                    }
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.contains("geo:")) {
                    Intent mapIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                        MainActivity.this.startActivity(mapIntent);
                    }
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
        String message = "This app is incompatible with your Android device. To make your device compatible with this app, Click okay to install/update your System webview from Play store";
        String title = "App Incompatibility Issue";
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

    private void createIntentForCameraOnly() {
        Intent CAMERA_ONLY_INTENT = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (CAMERA_ONLY_INTENT.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(CAMERA_ONLY_INTENT, CAMERA_ONLY_REQUEST);
        }
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
                sb.append("macAddress=").append(bssid).append("&").append("signalStrength=").append(ss).append("&").append("channel=").append(channel(wifiList.get(i).frequency)).append("&").append("ssid=").append(wifiList.get(i).SSID);
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
            int timingAdvance =0;

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
            if(timingAdvance != 0 && timingAdvance != Integer.MAX_VALUE) {
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


    private class viewLoadJavaInterface {
        Context mContext;

        viewLoadJavaInterface(Context c) {
            mContext = c;
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
             camera_view = new Camera_View(functionName);


            if (!hasPermissions(MainActivity.this, PERMISSIONS_CAMERA)) {
                if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_CAMERA, CAMERA_ONLY_REQUEST);

                } else {
                    String title = "Storage and Camera Permission";
                    String message = "Allow Storage and Camera Permission to get Picture";
                    showPermissionNotAllowedDialog(title, message, true);
                }

            } else {
                try {
                    startActivityForResult(photoCameraIntent(), CAMERA_ONLY_REQUEST);
                } catch (final IOException e) {
                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            androidException(e);
                        }
                    });
                    createIntentForCameraOnly();
                }
            }
        }

        ;

        // device Info //
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
                return "12";
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
        public void getContact(String functionName){
            String[] PERMISSIONS = {
                    Manifest.permission.READ_CONTACTS

            };
            contactCallbackFunctionName = functionName;
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS , GET_CONTACT_REQUEST);

            }
            else {
                startActivityForResult(getContactIntent(),GET_CONTACT_REQUEST);
            }

        }

    }

    @Override
    public void onBackPressed() {


        if (mWebView.canGoBack()) {
            mWebView.goBack(); // emulates back history
        } else {
            super.onBackPressed();
        }

    }

}