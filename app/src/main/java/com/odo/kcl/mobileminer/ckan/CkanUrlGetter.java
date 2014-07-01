package com.odo.kcl.mobileminer.ckan;

import android.content.Context;
import android.os.Build;

import com.odo.kcl.mobileminer.miner.MinerData;

import java.util.concurrent.ExecutionException;
//import android.util.Log;

public class CkanUrlGetter {
    Context context;

    public CkanUrlGetter(Context ctx) {
        context = ctx;
    }

    public String getUrl() {
        return getUrl(false);
    }

    public String getUrl(Boolean forceRefresh) {
        MinerData helper;
        String url;

        //if (true) return "http://10.0.2.2:5000";


        helper = new MinerData(context);
        url = null;

        if (!forceRefresh) {
            url = helper.getBookKeepingKey(helper.getReadableDatabase(), "ckanurl");
        }

        if (url == null) {

            //Log.i("MobileMiner","No existing URL.");

            try {
                url = (String) new CkanUrlRequest().execute(new Context[]{context}).get();
            } catch (InterruptedException e) {
                //Log.i("MobileMiner","Interrupt.");
                url = null;
            } catch (ExecutionException e) {
                //Log.i("MobileMiner","Exception.");
                url = null;
            }

            if (url != null) {
                //Log.i("MobileMiner","CKAN URL: "+url);
                if (forceRefresh)
                    helper.deleteBookKeepingKey(helper.getWritableDatabase(), "ckanurl");
                helper.setBookKeepingKey(helper.getWritableDatabase(), "ckanurl", url);

            }

        }

        // http://stackoverflow.com/questions/2799097/how-can-i-detect-when-an-android-application-is-running-in-the-emulator
        if ("goldfish".equals(Build.HARDWARE)) url = "http://10.0.2.2:5000";

        helper.close();
        return url;
    }

}
