// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package com.odo.kcl.mobileminer.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.odo.kcl.mobileminer.R;
import com.odo.kcl.mobileminer.cell.CellLocationGetter;
import com.odo.kcl.mobileminer.cell.CountedCell;
import com.odo.kcl.mobileminer.miner.MinerData;
import com.odo.kcl.mobileminer.util.ODOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

//import android.annotation.SuppressLint;
//import android.util.Log;


public class DataActivity extends Activity {
    private Context context;
    private TextView dataText;
    private ListView lv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        context = this;
        dataText = (TextView) findViewById(R.id.dataText);
        setDbSizeLegend();
        lv = (ListView) findViewById(R.id.usedApps);

        List<String> topAppsData = new ArrayList<>();

        MinerData mdata = new MinerData(this);
        SQLiteDatabase db = mdata.getReadableDatabase();

        topAppsData = mdata.getTopApps(db);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, topAppsData);
        lv.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                this.startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void exportData(View buttonView) {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(context);
        myAlertDialog.setTitle("Export Data");
        MinerData helper = new MinerData(context);
        String lastExported = helper.getLastExported(helper.getReadableDatabase());
        helper.close();
        if (lastExported == null) {
            myAlertDialog.setMessage("No data has been exported yet.");
        } else {
            myAlertDialog.setMessage(String.format("Data was last exported %s.", lastExported));
        }
        myAlertDialog.setPositiveButton("Export", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
                dumpDb();
            }
        });
        myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
                // do something when the Cancel button is clicked
            }
        });
        myAlertDialog.show();
    }

    public void expireData(View buttonView) {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(context);
        myAlertDialog.setTitle("Expire Data");
        String message = "Remove exported data from the database?";
        MinerData helper = new MinerData(context);
        String lastExpired = helper.getLastExpired(helper.getReadableDatabase());
        if (lastExpired == null) {
            message += "No data has been expired yet.";
        } else {
            message += "The oldest data is from " + lastExpired + ".";
        }
        myAlertDialog.setMessage(message);
        myAlertDialog.setPositiveButton("Expire", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                MinerData helper = new MinerData(context);
                helper.expireData(helper.getReadableDatabase());
                helper.close();
                setDbSizeLegend();
                Toast.makeText(context, "Data Expired.", Toast.LENGTH_LONG).show();
            }
        });

        myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
                // do something when the Cancel button is clicked
            }
        });

        myAlertDialog.show();
    }

    private void setDbSizeLegend() {
        // http://stackoverflow.com/questions/6364577/how-to-get-the-current-sqlite-database-size-or-package-size-in-android
        long dbSize = context.getDatabasePath(MinerData.DATABASE_NAME).length();
        long divider = 1024;
        String unit = "Kb";
        if (dbSize > 1048576) {
            unit = "Mb";
            divider = 1048576;
        }
        dataText.setText(String.format("Database Size: %d%s", dbSize / divider, unit));
    }

    private void dumpDb() {
        // http://www.techrepublic.com/blog/software-engineer/export-sqlite-data-from-your-android-device/#.
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        File dest = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File source = Environment.getDataDirectory();

        FileChannel sourceStream = null, destStream = null;
        String dbPath = "/data/com.odo.kcl.mobileminer/databases/" + MinerData.DATABASE_NAME;
        Date rightNow = new Date();

        String exportPath = "MobileMiner" + df.format(rightNow) + ".sqlite";
        File dbFile = new File(source, dbPath);
        File exportFile = new File(dest, exportPath);

        try {
            sourceStream = new FileInputStream(dbFile).getChannel();
            destStream = new FileOutputStream(exportFile).getChannel();
            destStream.transferFrom(sourceStream, 0, sourceStream.size());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Couldn't Export Data!", Toast.LENGTH_LONG).show();
        } finally { // We don't want to be leaky
            ODOUtil.closeResourceGracefully(sourceStream);
            ODOUtil.closeResourceGracefully(destStream);
        }

        // http://stackoverflow.com/questions/13737261/nexus-4-not-showing-files-via-mtp
        MediaScannerConnection.scanFile(this, new String[]{exportFile.getAbsolutePath()}, null, null);
        MinerData helper = new MinerData(context);
        helper.setLastExported(helper.getReadableDatabase(), rightNow);
        helper.close();

        Toast.makeText(this, "Data Exported.", Toast.LENGTH_LONG).show();
    }

    public void myLocations(View buttonView) {
        buttonView.setEnabled(false);
        MinerData helper = new MinerData(context);
        ArrayList<CountedCell> cells = MinerData.getMyCells(helper.getReadableDatabase());

        CellLocationGetter cellGetter = new CellLocationGetter(this);
        ArrayList<String[]> locations = new ArrayList<String[]>();
        ArrayList<Integer> counts = new ArrayList<Integer>();
        int thisCount, totalCount = 0;
        double totalLat = 0.0;
        double totalLong = 0.0;
        double maxLat, minLat, maxLong, minLong, lat, lon;
        String[] thisLocation;

        maxLat = maxLong = -1000;
        minLat = minLong = 1000;

        for (CountedCell cell : cells) {
            thisLocation = cellGetter.getCell(cell);
            if (thisLocation != null) {
                thisCount = cell.getCount();
                locations.add(thisLocation);
                counts.add(thisCount);
                totalCount += thisCount;
                lat = new Double(thisLocation[0]);
                lon = new Double(thisLocation[1]);
                totalLat += thisCount * lat;
                totalLong += thisCount * lon;

                if (lat > maxLat) maxLat = lat;
                if (lon > maxLong) maxLong = lon;
                if (lat < minLat) minLat = lat;
                if (lon < minLong) minLong = lon;
            }
        }

        if (locations.size() > 0) {
            String centreLat = String.valueOf(totalLat / totalCount);
            String centreLong = String.valueOf(totalLong / totalCount);

            HashMap<String, ArrayList<String>> markerLat = new HashMap<>(), markerLong = new HashMap<>();
            String[] markerLists = {"red", "yellow", "green", "blue"};

            for (String key : markerLists) {
                markerLat.put(key, new ArrayList<String>());
                markerLong.put(key, new ArrayList<String>());
            }

            int cumulCounts = 0;
            int colour = 0;
            int quart = totalCount / 4;
            int thresh = quart;

            for (int i = 0; i < locations.size(); i++) {
                markerLat.get(markerLists[colour]).add(locations.get(i)[0]);
                markerLong.get(markerLists[colour]).add(locations.get(i)[1]);
                cumulCounts += counts.get(i);
                if (cumulCounts > thresh && colour < 3) {
                    colour += 1;
                    thresh += quart;
                }

            }

            Intent mapIntent = new Intent(this, MapActivity.class);
            mapIntent.putExtra("lat", centreLat);
            mapIntent.putExtra("long", centreLong);
            mapIntent.putExtra("nocentre", true);
            mapIntent.putExtra("redlat", markerLat.get("red"));
            mapIntent.putExtra("redlong", markerLong.get("red"));
            mapIntent.putExtra("yellowlat", markerLat.get("yellow"));
            mapIntent.putExtra("yellowlong", markerLong.get("yellow"));
            mapIntent.putExtra("greenlat", markerLat.get("green"));
            mapIntent.putExtra("greenlong", markerLong.get("green"));
            mapIntent.putExtra("bluelat", markerLat.get("blue"));
            mapIntent.putExtra("bluelong", markerLong.get("blue"));
            mapIntent.putExtra("zoom", "15");
            buttonView.setEnabled(true);
            this.startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Can't find any towers...", Toast.LENGTH_SHORT).show();
            buttonView.setEnabled(true);
        }

    }

}
