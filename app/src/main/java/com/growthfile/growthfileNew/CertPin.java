package com.growthfile.growthfileNew;

import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;

class CertPin extends AsyncTask<String,Void,Void> {

    @Override
    protected Void doInBackground(String... strings) {
        String hostname = "growthfile-207204.firebaseapp.com";

        CertificatePinner certificatePinner = new CertificatePinner.Builder()

                .add(hostname, "sha256/LIZGjlRnvobyLt4gIE3du/7GQVU7VSjTD9UQmEv3enU=")
                .add(hostname,"sha256/YZPgTZ+woNCCCIW3LH2CxQeLzB/1m42QcCTBSdgayjs=")
                .add(hostname,"sha256/iie1VXtL7HzAMF+/PVPR9xzT80kQxdZeJ+zduCB3uj0=")
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .certificatePinner(certificatePinner)
                .build();
        Request request = new Request.Builder()
                .url("https://" + hostname)
                .build();

        try {
            client.newCall(request).execute();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

