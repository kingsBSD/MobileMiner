package com.odo.kcl.mobileminer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.odo.kcl.mobileminer.ckan.CkanUidRequest;
import com.odo.kcl.mobileminer.miner.MinerData;
import com.odo.kcl.mobileminer.miner.MinerTables.BookKeepingTable;

import java.util.concurrent.ExecutionException;

public class UidGetter {
    private Context context;

    public UidGetter(Context ctx) {
        context = ctx;
    }

    public String getUid() {
        return getUid(false);
    }

    public String getUid(boolean forceRefresh) {
        String uid;
        String[] retColumns = {BookKeepingTable.COLUMN_NAME_VALUE};
        String[] whereValues = {"ckanuid"};

        MinerData helper = new MinerData(context);

        if (!forceRefresh) {
            SQLiteDatabase dbReader = helper.getReadableDatabase();
            Cursor c = dbReader.query(BookKeepingTable.TABLE_NAME, retColumns, BookKeepingTable.COLUMN_NAME_KEY + " = ?", whereValues, null, null, null);

            try {
                c.moveToFirst();
                uid = c.getString(c.getColumnIndex(BookKeepingTable.COLUMN_NAME_VALUE));
            } catch (Exception e) {
                Log.i("MobileMiner", "No uid in the DB.");
                uid = null;
            }
        } else {
            Log.i("MobileMiner", "Force new uid.");
            uid = null;
        }

        if (uid == null) {
            try {
                Log.i("MobileMiner", "Getting new uid.");
                uid = (String) new CkanUidRequest().execute().get();
            } catch (InterruptedException e) {
                Log.i("MobileMiner", "Interrupted.");
                uid = null;
            } catch (ExecutionException e) {
                uid = null;
            }

            if (uid != null) {
                Log.i("MobileMiner", "New uid.");
                SQLiteDatabase dbWriter = helper.getWritableDatabase();
                dbWriter.delete(BookKeepingTable.TABLE_NAME, BookKeepingTable.COLUMN_NAME_KEY + " = ? ", new String[]{"ckanuid"});
                ContentValues values = new ContentValues();
                values.put(BookKeepingTable.COLUMN_NAME_KEY, "ckanuid");
                values.put(BookKeepingTable.COLUMN_NAME_VALUE, uid);
                dbWriter.insert(BookKeepingTable.TABLE_NAME, null, values);
            }
        }

        helper.close();

        return uid;
    }


}
