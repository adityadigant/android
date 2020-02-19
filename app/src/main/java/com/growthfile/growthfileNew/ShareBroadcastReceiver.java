package com.growthfile.growthfileNew;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.net.URLDecoder;
import java.util.Objects;

public class ShareBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d("INFORMATION", "Received intent after selection: "+intent.getExtras().get(Intent.EXTRA_CHOSEN_COMPONENT));
            for (String key : Objects.requireNonNull(intent.getExtras()).keySet()) {
                try {
                    ComponentName componentInfo = (ComponentName) intent.getExtras().get(key);
                    PackageManager packageManager = context.getPackageManager();
                    assert componentInfo != null;
                    String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(componentInfo.getPackageName(), PackageManager.GET_META_DATA));

                    MainActivity.mWebView.evaluateJavascript("linkSharedComponent('"+appName+"')",null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
