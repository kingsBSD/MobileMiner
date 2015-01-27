// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package uk.ac.kcl.odo.mobileminer.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import uk.ac.kcl.odo.mobileminer.activities.MapActivity;
import uk.ac.kcl.odo.mobileminer.data.MinerTables.GeoIpTable;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class GeoIpGetter {
	
	Context context;
	public GeoIpGetter(Context ctx) {
		context = ctx;
	}
	
	public HashMap<String,String> get(String ip) {
		SQLiteDatabase dbReader, dbWriter;
		
		MinerData helper = new MinerData(context);
		dbReader = helper.getReadableDatabase();
		Cursor c;
		HashMap<String,String> geoData = new HashMap<String,String>();
		
		String[] retColumns = {GeoIpTable.COLUMN_NAME_COUNTRY,GeoIpTable.COLUMN_NAME_REGION,
			GeoIpTable.COLUMN_NAME_CITY,GeoIpTable.COLUMN_NAME_LAT,GeoIpTable.COLUMN_NAME_LONG};
		
		c = dbReader.query(GeoIpTable.TABLE_NAME,retColumns,GeoIpTable.COLUMN_NAME_IP+" = ? ",new String[]{ip},null,null,null);
		c.moveToFirst();
		try {
			for (String col: retColumns) {
				geoData.put(col,c.getString(c.getColumnIndex(col)));
			}
			dbReader.close();
			return geoData;
			
		}
		catch (Exception e) {
			dbReader.close();
			geoData = null;
		}

		if (geoData == null) {
			try {
				geoData = new FreeGeoIpRequest().execute(new String[]{ip}).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			
		}
		
		if (geoData != null) {
			dbWriter = helper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(GeoIpTable.COLUMN_NAME_IP,ip);
			for (String col: retColumns) {
				values.put(col,geoData.get(col));
			}
			dbWriter.insert(GeoIpTable.TABLE_NAME,null,values);
			dbWriter.close();			
		}
				
		return geoData;
	}
	
	public Intent getMapIntent(String ip) {
		HashMap<String,String> geoData = get(ip);
		
		if (geoData != null) {
			Intent mapIntent = new Intent(context, MapActivity.class);
			mapIntent.putExtra("lat", geoData.get("lat"));
			mapIntent.putExtra("long", geoData.get("long"));
			mapIntent.putExtra("zoom", "15");
			ArrayList<String> tokens = new ArrayList<String>();
			tokens.add(ip);
			String thisToken;
			for (String key: new String [] {GeoIpTable.COLUMN_NAME_CITY,GeoIpTable.COLUMN_NAME_REGION,GeoIpTable.COLUMN_NAME_COUNTRY}) {
				thisToken = geoData.get(key);
				if (thisToken.length() > 0) tokens.add(thisToken);
			}
			mapIntent.putExtra("legend", TextUtils.join(", ", tokens));
			return mapIntent;
		}
		return null;
		
	}
	
}


