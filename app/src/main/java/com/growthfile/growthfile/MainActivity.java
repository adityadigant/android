package com.growthfile.growthfile;

import android.Manifest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;

import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;

import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.GeolocationPermissions;

import java.io.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.provider.Settings.Secure;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private static final int FCR = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String mCM;

    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private Bitmap imageBitmap;
    private static final int profile_photo_code = 999;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private WebView mWebView;
    SwipeRefreshLayout swipeToRefresh;
    private static final String loadTypeInit = "init";
    private static final String loadTypeUpdate = "update";
    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;
    private Context mContext;
    private static final int CAMERA_ONLY_REQUEST = 1888;
    private static final int CAMERA_ONLY_PERMISSION_CODE = 100;
    private boolean background_app = false;

    public class NewWebChromeClient extends WebChromeClient {

        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
            Log.i(TAG, "onGeolocationPermissionsShowPrompt()");

            final boolean remember = false;
            callback.invoke(origin, true, remember);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            Intent[] intentArray;
            File photoFile = null;
            if (MainActivity.this.mUMA != null) {
                MainActivity.this.mUMA.onReceiveValue(null);
            }
            MainActivity.this.mUMA = filePathCallback;
            Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                try {
                    photoFile = MainActivity.this.createImageFile();
                    takePictureIntent.putExtra("PhotoPath", MainActivity.this.mCM);
                } catch (IOException ex) {
                }
                if (photoFile != null) {
                    MainActivity mainActivity = MainActivity.this;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("file:");
                    stringBuilder.append(photoFile.getAbsolutePath());
                    mainActivity.mCM = stringBuilder.toString();
                    takePictureIntent.putExtra("output", Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }


            Intent contentSelectionIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (takePictureIntent != null) {
                Intent[] intentArr = new Intent[MainActivity.FCR];
                intentArr[0] = takePictureIntent;
                intentArray = intentArr;
            } else {
                intentArray = new Intent[0];
            }
            Intent chooserIntent = new Intent("android.intent.action.CHOOSER");
            chooserIntent.putExtra("android.intent.extra.INTENT", contentSelectionIntent);
            chooserIntent.putExtra("android.intent.extra.TITLE", "Image Chooser");
            chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", intentArray);

            MainActivity.this.startActivityForResult(chooserIntent, MainActivity.FCR);
            return true;
        }

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
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case CAMERA_ONLY_REQUEST:
                if (resultCode == RESULT_OK) {
                    Bundle extras = intent.getExtras();
                    imageBitmap = (Bitmap) extras.get("data");
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    mWebView.loadUrl("javascript:setFilePath('" + encoded + "')");

                }
                break;


            case FCR:
                if (VERSION.SDK_INT >= 21) {
                    Uri[] results = null;
                    if (resultCode == -1 && requestCode == FCR) {
                        if (this.mUMA != null) {
                            Uri[] uriArr;
                            if (intent != null) {
                                String dataString = intent.getDataString();
                                if (dataString != null) {
                                    uriArr = new Uri[FCR];
                                    uriArr[0] = Uri.parse(dataString);
                                    results = uriArr;
                                }
                            } else if (this.mCM != null) {
                                uriArr = new Uri[FCR];
                                uriArr[0] = Uri.parse(this.mCM);
                                results = uriArr;
                            }
                        } else {
                            return;
                        }
                    }
                    this.mUMA.onReceiveValue(results);
                    this.mUMA = null;
                } else if (requestCode == FCR && this.mUM != null) {
                    Uri result;
                    if (intent != null) {
                        if (resultCode == -1) {
                            result = intent.getData();
                            this.mUM.onReceiveValue(result);
                            this.mUM = null;
                        }
                    }
                    result = null;
                    this.mUM.onReceiveValue(result);
                    this.mUM = null;
                }
        }


    }

    //     Create an image file
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }


    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        new CertPin().execute();


        mContext = getApplicationContext();
        try {
            LoadApp(loadTypeInit);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        swipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    LoadApp(loadTypeUpdate);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        swipeToRefresh.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (mWebView.getScrollY() == 0) {
                    swipeToRefresh.setEnabled(true);
                } else {
                    swipeToRefresh.setEnabled(false);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        swipeToRefresh.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
    }


    public void LoadApp(String type) throws JSONException {
        this.mWebView = (WebView) findViewById(R.id.activity_main_webview);

        WebSettings webSettings = this.mWebView.getSettings();

        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "Fetchview");
        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "FetchCameraForAttachment");
        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "openAndroidKeyboard");
        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "FetchHistory");
        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "AndroidId");
        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "IsGpsEnabled");
        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this),"Towers");

        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webSettings.setDomStorageEnabled(true);

        webSettings.setAllowFileAccess(true);
        webSettings.setGeolocationEnabled(true);

        webSettings.setDatabaseEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webSettings.setAppCacheEnabled(true);
        webSettings.setGeolocationDatabasePath(getApplicationContext().getFilesDir().getPath());
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setScrollbarFadingEnabled(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        if (type.equals("init")) {

            if (!isNetworkAvailable()) { // loading offline
                webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            }

            mWebView.loadUrl("https://growthfile-207204.firebaseapp.com");
            // mWebView.loadUrl("https://frontend-testing-9d09e.firebaseapp.com/");
            mWebView.requestFocus(View.FOCUS_DOWN);
        }
        if (type.equals("update")) {

            swipeToRefresh.setRefreshing(true);
            String requestType = "Null";
            mWebView.loadUrl("javascript:requestCreator('" + requestType + "',true)");
            swipeToRefresh.setRefreshing(false);
        }

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,

        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }




        mWebView.setWebViewClient(new WebViewClient() {

      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
      }

      @Override
      public void onPageFinished(WebView view, String url) {
      }


    });

    this.mWebView.setWebChromeClient(new NewWebChromeClient());

  
  }

    private String networkType() {
        TelephonyManager teleMan = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = teleMan.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT: return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA: return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE: return "EDGE";
            case TelephonyManager.NETWORK_TYPE_EHRPD: return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0: return "EVDO rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A: return "EVDO rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B: return "EVDO rev. B";
            case TelephonyManager.NETWORK_TYPE_GPRS: return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA: return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA: return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP: return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_HSUPA: return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN: return "iDen";
            case TelephonyManager.NETWORK_TYPE_LTE: return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS: return "UMTS";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN: return "Unknown";
        }
        throw new RuntimeException("New type of network");
    }

  private boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  public class viewLoadJavaInterface {
    Context mContext;
    viewLoadJavaInterface(Context c) {
      mContext = c;
    }

    @JavascriptInterface
    public void startConversation(final String view) {
      runOnUiThread(new Runnable() {
        public void run() {

          if (view.equals("conversation") || view.equals("selector") || view.equals("drawer")) {
            swipeToRefresh.setEnabled(false);
          } else {
            swipeToRefresh.setEnabled(true);
          }
        }
      });
    }

    @JavascriptInterface
    public void startCamera() {
      Intent CAMERA_ONLY_INTENT = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      if (CAMERA_ONLY_INTENT.resolveActivity(getPackageManager()) != null) {
        startActivityForResult(CAMERA_ONLY_INTENT, CAMERA_ONLY_REQUEST);
      }
    }
    @JavascriptInterface
    public void startKeyboard() {
      InputMethodManager inputMethodManager = (InputMethodManager)
      getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.showSoftInput(findViewById(R.id.activity_main_webview),
        InputMethodManager.SHOW_FORCED);
    }

    @JavascriptInterface
    public String getDeviceId(){
    String androidId = Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    String deviceBrand  = Build.MANUFACTURER;
    String deviceModel = Build.MODEL;
    String osVersion = VERSION.RELEASE;
    String deviceInformation = ""+androidId+"&"+deviceBrand+"&"+deviceModel+"&"+osVersion;
    return deviceInformation;
    }

    @JavascriptInterface
    public boolean gpsEnabled(){
      LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);

      assert service != null;
      return service.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    @JavascriptInterface
      public String getCellularData() throws JSONException {
        JSONObject json = new JSONObject();

        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getNetworkOperator();

        if (!TextUtils.isEmpty(networkOperator)) {
            int mcc = Integer.parseInt(networkOperator.substring(0, 3));
            int mnc = Integer.parseInt(networkOperator.substring(3));

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String [] CoarsePerm = {
                        Manifest.permission.ACCESS_COARSE_LOCATION
                };
                ActivityCompat.requestPermissions(MainActivity.this,CoarsePerm,1);
                String NotAllowed = json.toString(4);
                return NotAllowed;
            }

            GsmCellLocation cellLocation = (GsmCellLocation)tel.getCellLocation();
            final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            assert wifiManager != null;

            WifiInfo wifiInfo = wifiManager.getConnectionInfo();


            int cellid= cellLocation.getCid();
            int celllac = cellLocation.getLac();


            List<ScanResult> apList = wifiManager.getScanResults();


            json.put("homeMobileCountryCode",mcc);
            json.put("homeMobileNetworkCode",mnc);
            json.put("radioType",networkType());
            json.put("considerIp","true");


            if(!apList.isEmpty()) {
                JSONArray wifi = new JSONArray();

                for (int i=0;i<apList.size();i++){
                    JSONObject aps = new JSONObject();

                    aps.put("macAddress",apList.get(i).BSSID);
                    aps.put("signalStrength",new Integer(apList.get(i).level));
                    wifi.put(aps);
                }
                json.put("wifiAccessPoints",wifi);

            }

            json.put("carrier",tel.getNetworkOperatorName());
            JSONArray towers = new JSONArray();
            JSONObject cells = new JSONObject();
            cells.put("cellId",cellid);
            cells.put("locationAreaCode",celllac);
            cells.put("mobileCountryCode",mcc);
            cells.put("mobileNetworkCode",mnc);
            towers.put(cells);
            json.put("cellTowers",towers);

        }
        String apiRequest = json.toString(4);
        return apiRequest;
    }

  }

  public static boolean hasPermissions(Context context, String...permissions) {
    if (context != null && permissions != null) {
      for (String permission: permissions) {
        if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
          return false;
        }
      }
    }
    return true;
  }

  public static Certificate getCertificateForRawResource(int resourceId, Context context) {
    CertificateFactory cf = null;
    Certificate ca = null;
    Resources resources = context.getResources();
    InputStream caInput = resources.openRawResource(resourceId);

    try {
      cf = CertificateFactory.getInstance("X.509");
      ca = cf.generateCertificate(caInput);
    } catch (CertificateException e) {
    } finally {
      try {
        caInput.close();
      } catch (IOException e) {
        Log.e(TAG, "exception", e);
      }
    }

    return ca;
  }

  public Uri getImageUri(Context inContext, Bitmap inImage) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
    String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
    return Uri.parse(path);
  }

  public String getRealPathFromURI(Uri uri) {
    String path = "";
    if (getContentResolver() != null) {
      Cursor cursor = getContentResolver().query(uri, null, null, null, null);
      if (cursor != null) {
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        path = cursor.getString(idx);
        cursor.close();
      }
    }
    return path;
  }

  @Override
  public void onBackPressed() {

    if (mWebView.canGoBack()) {
      mWebView.goBack(); // emulats back history
    } else {
      super.onBackPressed();
    }
  };
}