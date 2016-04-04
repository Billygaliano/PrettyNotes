package com.ruben.rma.prettynotes.connectionws;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by inftel12 on 29/3/16.
 */
public class DeleteHttp extends AsyncTask<String, Void, String> {

    private final Context context;
    public DeleteHttp(Context c){
        this.context = c;
    }

    protected void onPreExecute(){
    }

    @Override
    protected String doInBackground(String... param) {

        String result="";
        HttpURLConnection con;

        try {

            URL url = new URL(param[0]);
            con = (HttpURLConnection)url.openConnection();

            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type","application/json");

            // Get JSONObject here
            String jsonParam = param[1];

            // Upload JSON
            OutputStreamWriter out = new   OutputStreamWriter(con.getOutputStream());
            out.write(jsonParam);
            out.flush();
            out.close();

            int responseCode = con.getResponseCode();

            result = "\nSending 'DELETE' request to URL : " + url + "\nResponse Code : " + responseCode;

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
