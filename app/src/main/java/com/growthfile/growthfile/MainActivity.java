package com.growthfile.growthfile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.provider.Settings.Secure;


public class MainActivity extends AppCompatActivity {
  private static final int FCR = 1;
  private static final String TAG = MainActivity.class.getSimpleName();
  private String mCM;

  private ValueCallback < Uri > mUM;
  private ValueCallback < Uri[] > mUMA;
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
    public boolean onShowFileChooser(WebView webView, ValueCallback < Uri[] > filePathCallback, FileChooserParams fileChooserParams) {
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
          System.out.println("photofile " + photoFile);
        } catch (IOException ex) {
          Log.e(MainActivity.TAG, "Image file creation failed", ex);
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
        Log.isLoggable("result", resultCode);
        if (resultCode == RESULT_OK) {
          Bundle extras = intent.getExtras();
          imageBitmap = (Bitmap) extras.get("data");
          ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
          imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
          byte[] byteArray = byteArrayOutputStream.toByteArray();
          String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
          Log.d("base64", "data:image/jpeg;base64, " + encoded);
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
    System.out.println("ceraeting image name");
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


    Log.d("activity", "start");
    mContext = getApplicationContext();
    Log.d("first load", "oncreate");
    LoadApp(loadTypeInit);

    swipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
    swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        LoadApp(loadTypeUpdate);
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
          Log.d("swipe", "true");
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


  public void LoadApp(String type) {
    Log.d("running", "times");
    this.mWebView = (WebView) findViewById(R.id.activity_main_webview);

    WebSettings webSettings = this.mWebView.getSettings();

    mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "Fetchview");
    mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "FetchCameraForAttachment");
    mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "openAndroidKeyboard");
    mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "FetchHistory");
    mWebView.addJavascriptInterface(new viewLoadJavaInterface(this),  "AndroidId");
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


    mWebView.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("load url", "run");
        view.loadUrl(url);
        return true;
      }

      @Override
      public void onPageFinished(WebView view, String url) {
        System.out.print("url is " + url);

      }

    });

    this.mWebView.setWebChromeClient(new NewWebChromeClient());

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
      Manifest.permission.CAMERA,
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.ACCESS_FINE_LOCATION,

    };

    if (!hasPermissions(this, PERMISSIONS)) {
      ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
    }


    if (type.equals("init")) {
      if (!isNetworkAvailable()) { // loading offline
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
      }

      Log.d("app laod", "new");
      mWebView.loadUrl("https://growthfile-207204.firebaseapp.com");
      // mWebView.loadUrl("https://frontend-testing-9d09e.firebaseapp.com/");
      mWebView.requestFocus(View.FOCUS_DOWN);
    }
    if (type.equals("update")) {
      swipeToRefresh.setRefreshing(true);
      String requestType = "Null";
      mWebView.loadUrl("javascript:requestCreator('" + requestType + "')");
      swipeToRefresh.setRefreshing(false);
    }
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
      Log.d("camera", "start");
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
    System.out.println("AndroidData " + deviceInformation);
    return deviceInformation;
    }
  }

  private boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
      Log.e(TAG, "exception", e);
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