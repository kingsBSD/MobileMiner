package com.odo.kcl.mobileminer.ckan;

import android.os.AsyncTask;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
//import android.util.Log;

public class CkanUrlRequest extends AsyncTask {

    @Override
    protected Object doInBackground(Object... arg0) {

        JSONObject jsonDump;
        String response;

        HttpGet get = new HttpGet("http://kingsbsd.github.io/MobileMiner/phonehome.json");
        HttpClient client = new DefaultHttpClient();

        jsonDump = new JSONObject();

        try {
            //Log.i("MobileMiner","URL request.");
            response = EntityUtils.toString(client.execute(get).getEntity());
            //Log.i("MobileMiner",response);
        } catch (ParseException e) {
            response = null;
        } catch (ClientProtocolException e) {
            response = null;
        } catch (IOException e) {
            response = null;
        }

        if (response != null) {
            try {
                jsonDump = new JSONObject(response);
                //Log.i("MobileMiner",jsonDump.getString("ckan_url"));
                return jsonDump.getString("ckan_url");
            } catch (JSONException e) {
                return null;
            }
        }

        return null;
    }

}
