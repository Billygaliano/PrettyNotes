package com.ruben.rma.prettynotes.connectionws;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by inftel12 on 29/3/16.
 */
public class GetHttp extends AsyncTask<String, Void, String> {
    private final Context context;


    public GetHttp(Context c){
        this.context = c;
    }
    protected void onPreExecute(){
    }

    @Override
    protected String doInBackground(String... param) {

        String result="";
        StringBuilder sb = new StringBuilder();
        HttpURLConnection con;

        try {


            URL url = new URL(param[0]);
            con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");


            int responseCode = con.getResponseCode();

            result = "\nSending 'POST' request to URL : " + url + "\nResponse Code : " + responseCode;

            System.out.println("\nSending 'POST' request to URL : " + con.getRequestMethod() + " Y la URL: " + url);
            System.out.println("Response Code : " + responseCode);




        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    protected void onPostExecute(String result) {
    }
}
