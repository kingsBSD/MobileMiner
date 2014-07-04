package com.odo.kcl.mobileminer.ckan;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;

import com.odo.kcl.mobileminer.miner.MinerData;
import com.odo.kcl.mobileminer.miner.MinerTables;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
//import android.util.Log;

public class CkanUpdater extends AsyncTask {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    protected Object doInBackground(Object... arg0) {

        Context context = (Context) arg0[0];
        Field[] tableClassFields;
        String url, uid, tableName, thisName, thisValue, dateKey, dateVal, response;
        Date date, rightNow;
        ArrayList<String> tableFields;
        HashMap<String, String> timeStamps;
        MinerData helper = new MinerData(context);
        SQLiteDatabase dbReader, dbWriter;
        dbReader = helper.getReadableDatabase();
        dbWriter = helper.getWritableDatabase();
        String[] retColumns;
        Cursor c;
        Boolean searching;
        int things;
        JSONArray allTheThings;
        JSONObject thisItem, jsonDump, jsonResponse, error;
        HttpPost post;
        HttpClient client = new DefaultHttpClient();

        timeStamps = new HashMap<String, String>();
        for (int i = 0; i < MinerTables.ExpirableTables.length; i++) {
            timeStamps.put(MinerTables.ExpirableTables[i], MinerTables.ExpirableTimeStamps[i]);
        }

        url = helper.getBookKeepingKey(dbReader, "ckanurl");
        // http://stackoverflow.com/questions/2799097/how-can-i-detect-when-an-android-application-is-running-in-the-emulator
        if ("goldfish".equals(Build.HARDWARE)) url = "http://10.0.2.2:5000";

        uid = helper.getBookKeepingKey(dbReader, "ckanuid");

        if (url == null) return null;

        if (uid == null) return null;

        for (Object table : MinerTables.tableClasses) {
            tableClassFields = ((Class) table).getFields();
            tableFields = new ArrayList<String>();
            tableName = null;
            for (Field tableField : tableClassFields) {
                thisName = tableField.getName();
                //Log.i("MobileMiner",thisName);
                try {
                    thisValue = (String) tableField.get(table);
                } catch (IllegalAccessException e) {
                    thisValue = null;
                } catch (IllegalArgumentException e) {
                    thisValue = null;
                }

                if (thisValue != null && !thisName.startsWith("_")) {
                    //Log.i("MobileMiner",thisValue);
                    if ("TABLE_NAME".equals(thisName)) {
                        tableName = thisValue;
                    } else {
                        tableFields.add(thisValue);
                    }
                }

            }

            if (true) {
                things = 0;
                allTheThings = new JSONArray();
                retColumns = tableFields.toArray(new String[tableFields.size()]);
                dateKey = tableName + "update";
                date = helper.getBookKeepingDate(dbReader, dateKey);
                rightNow = new Date();
                if (date == null) {
                    c = dbReader.query(tableName, retColumns, null, null, null, null, null);
                } else {
                    c = dbReader.query(tableName, retColumns, timeStamps.get(tableName) + " >= ? ", new String[]{df.format(date)}, null, null, null);
                }

                c.moveToFirst();
                searching = true;
                while (searching) {
                    searching = !c.isLast();
                    try {
                        thisItem = new JSONObject();
                        for (String thisCol : retColumns) {
                            thisItem.put(thisCol, c.getString(c.getColumnIndex(thisCol)));
                        }
                        allTheThings.put(thisItem);
                        things += 1;
                    } catch (Exception e) {
                        searching = false;
                    }
                    c.moveToNext();
                }

                //if (things == 0) Log.i("MobileMiner","No new records.");

                if (things > 0) {
                    jsonDump = new JSONObject();
                    try {
                        jsonDump.put("uid", uid);
                        jsonDump.put("table", tableName);
                        jsonDump.put("records", allTheThings);
                        //Log.i("MobileMiner",jsonDump.toString());
                    } catch (JSONException e) {
                        jsonDump = null;
                    }

                    post = new HttpPost(url + "/api/action/miner_update");
                    post.setHeader("content-type", "application/x-www-form-urlencoded");

                    if (jsonDump != null) {
                        try {
                            post.setEntity(new ByteArrayEntity(jsonDump.toString().getBytes("UTF8")));
                        } catch (UnsupportedEncodingException e) {
                            post = null;
                        }
                    }

                    response = null;
                    if (post != null) {
                        try {
                            response = EntityUtils.toString(client.execute(post).getEntity());
                        } catch (ParseException e) {
                            helper.deleteBookKeepingKey(dbWriter, "ckanurl");
                            return null;
                        } catch (ClientProtocolException e) {
                            helper.deleteBookKeepingKey(dbWriter, "ckanurl");
                            return null;
                        } catch (IOException e) {
                            return null;
                        }
                    }

                    if (response != null) {
                        //Log.i("MobileMiner",response);
                        try {
                            jsonResponse = new JSONObject(response);

                            if (jsonResponse.getBoolean("success")) {
                                helper.setBookKeepingDate(dbWriter, dateKey, rightNow);
                            } else {
                                error = jsonResponse.getJSONObject("error");
                                if (error.getString("message").equals("No such user.")) {
                                    //Log.i("MobileMiner","No such user.");
                                    helper.deleteBookKeepingKey(dbWriter, "ckanuid");
                                    return null;
                                }
                            }


                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        //return Integer.toString(new JSONObject(response).getInt("result"));

                    }


                }


            }


        }


        helper.close();
        // TODO Auto-generated method stub
        return null;
    }

}





