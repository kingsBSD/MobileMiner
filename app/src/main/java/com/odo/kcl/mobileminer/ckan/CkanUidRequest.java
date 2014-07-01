package com.odo.kcl.mobileminer.ckan;


import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;

import com.odo.kcl.mobileminer.miner.MinerTables;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
//import android.util.Log;

public class CkanUidRequest extends AsyncTask {
    String androidId = Settings.Secure.ANDROID_ID;

    @Override
    protected Object doInBackground(Object... arg0) {

        Context context = (Context) arg0[0];
        String url = new CkanUrlGetter(context).getUrl();

        HttpPost post = new HttpPost(url + "/api/action/miner_register");
        post.setHeader("content-type", "application/x-www-form-urlencoded");

        JSONObject JSONdump = new JSONObject();
        try {
            JSONdump.put("androidid", Settings.Secure.ANDROID_ID);
            JSONdump.put("version", MinerTables.APP_VERSION);
            post.setEntity(new ByteArrayEntity(JSONdump.toString().getBytes("UTF8")));
        } catch (JSONException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        HttpClient client = new DefaultHttpClient();
        String response;

        try {
            response = EntityUtils.toString(client.execute(post).getEntity());
            //Log.i("MobileMiner",response);
        } catch (ParseException e) {
            response = null;
        } catch (ClientProtocolException e) {
            response = null;
        } catch (IOException e) {
            response = null;
        }

        try {
            return (new JSONObject(response).getString("result"));
        } catch (JSONException e) {
            return null;
        }

    }

}
