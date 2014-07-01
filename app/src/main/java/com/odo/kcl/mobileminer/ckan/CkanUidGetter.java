package com.odo.kcl.mobileminer.ckan;

import android.content.Context;

import com.odo.kcl.mobileminer.miner.MinerData;

import java.util.concurrent.ExecutionException;

public class CkanUidGetter {
    Context context;

    public CkanUidGetter(Context ctx) {
        context = ctx;
    }

    public String getUid() {
        return getUid(false);
    }

    public String getUid(boolean forceRefresh) {
        String uid;

        MinerData helper = new MinerData(context);
        uid = null;

        if (!forceRefresh) {
            uid = helper.getBookKeepingKey(helper.getReadableDatabase(), "ckanuid");
        }

        if (uid == null) {
            try {
                uid = (String) new CkanUidRequest().execute(new Context[]{context}).get();
            } catch (InterruptedException e) {
                uid = null;
            } catch (ExecutionException e) {
                uid = null;
            }

            if (uid != null) {
                helper.deleteBookKeepingKey(helper.getWritableDatabase(), "ckanuid");
                helper.setBookKeepingKey(helper.getWritableDatabase(), "ckanuid", uid);
            }

        }

        helper.close();
        return uid;

    }

}
