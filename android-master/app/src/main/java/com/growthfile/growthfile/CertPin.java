package com.growthfile.growthfile;

import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;

class CertPin extends AsyncTask<String,Void,Void> {

    @Override
    protected Void doInBackground(String... strings) {
        System.out.println(strings);
        String hostname = "growthfile-207204.firebaseapp.com";

        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add(hostname, "sha256/x98pMm+lpJurypYQ/KhkxTc1uza7xAwvpXR9vkIUDcM=")
                .add(hostname,"sha256/f8NnEFZxQ4ExFOhSN7EiFWtiudZQVD2oY60uauV/n78=")
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

