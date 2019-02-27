package com.growthfile.growthfileNew;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;

public class fetchCellularInAsync extends AsyncTask<Void,Void,String> {
    private Context context;
    private WebView webView;

    public fetchCellularInAsync(Context context,WebView webView){
        this.context = context;
        this.webView = webView;
    }

    @Override
    protected  void onPreExecute(){

    };

    @Override
    protected String doInBackground(Void... voids) {
        CellularInformation cellularInformation = new CellularInformation(context);
        return cellularInformation.fullCellularInformation();
    }


    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        Log.d("response",response);
        webView.loadUrl("javascript:manageLocation('"+response+"')");
    }
}
