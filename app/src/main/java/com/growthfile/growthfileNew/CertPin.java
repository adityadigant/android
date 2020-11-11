package com.growthfile.growthfileNew;

import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;

class CertPin extends AsyncTask<String,Void,Void> {

    @Override
    protected Void doInBackground(String... strings) {
        String hostname = "app.growthfile.com";

        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add(hostname,"sha256/oqL8L0HOy50gpOp8x0ggPje9tAVfhHFkuJH4ChzbQ8k=")
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

