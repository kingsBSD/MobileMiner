// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package com.odo.kcl.mobileminer.cell;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.odo.kcl.mobileminer.util.ODOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;


public class CellData extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "CellData.db";
    private Context ctx;

    public CellData(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        ctx = context;
    }

    public CellData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    public void init() {
        File dataDir = Environment.getDataDirectory();
        String dbPath = "/data/com.odo.kcl.mobileminer/databases/" + DATABASE_NAME;
        File dbFile = new File(dataDir, dbPath);
        GZIPInputStream sourceStream = null;
        FileOutputStream destStream = null;
        if (!dbFile.exists()) {
            try {
                destStream = new FileOutputStream(dbFile);
                sourceStream = new GZIPInputStream(ctx.getAssets().open("celldata.bin"));

                byte[] buffer = new byte[1024];
                int len;
                while ((len = sourceStream.read(buffer)) > 0) {
                    destStream.write(buffer, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ODOUtil.closeResourceGracefully(destStream);
                ODOUtil.closeResourceGracefully(sourceStream);
            }
        }
    }

}

