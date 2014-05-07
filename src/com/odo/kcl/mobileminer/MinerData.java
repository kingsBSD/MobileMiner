// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package com.odo.kcl.mobileminer;

import java.math.BigInteger;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import com.odo.kcl.mobileminer.MinerTables.BookKeepingTable;
import com.odo.kcl.mobileminer.MinerTables.GSMCellTable;
import com.odo.kcl.mobileminer.MinerTables.MinerLogTable;
import com.odo.kcl.mobileminer.MinerTables.MobileNetworkTable;
import com.odo.kcl.mobileminer.MinerTables.SocketTable;
import com.odo.kcl.mobileminer.MinerTables.WifiNetworkTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.wifi.WifiInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;


public class MinerData extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MobileMiner.db";
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    public static class WifiData {
    	private String ssid,bssid,ip;
    	public WifiData() {
    		ssid = "null"; bssid = "null"; ip = "null";
    	}
    	public WifiData(WifiInfo info) {
    		String[] chunks;
			ssid = info.getSSID();
			if (ssid == null) ssid = "null";
			bssid = info.getBSSID();
			if (bssid == null) bssid = "null";
			try {
				// http://stackoverflow.com/questions/17055946/android-formatter-formatipaddress-deprecation-with-api-12
				ip = InetAddress.getByAddress(BigInteger.valueOf(info.getIpAddress()).toByteArray()).getHostAddress();
				chunks = ip.split("\\.");
				Collections.reverse(Arrays.asList(chunks));
				ip = TextUtils.join(".",chunks);
			}
			catch (Exception e) {
				ip = "null";
			}			
    	}
    	public String getSSID() {return ssid;}
    	public String getBSSID() {return bssid;}
    	public String getIP() {return ip;}
    	
    }
    
	public MinerData(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

    public MinerData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
	
	public void onCreate(SQLiteDatabase db) {
		for (String sql: MinerTables.CreateTables)
			 {
			try {
				db.execSQL(sql);
			}
			catch (Exception e) {
			}
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

	public void putSocket(SQLiteDatabase db, String proc, String prot, String addr, Date opened, Date closed) {
		String[] chunks;
		ContentValues values = new ContentValues();
		values.put(SocketTable.COLUMN_NAME_PROCESS,proc);
		values.put(SocketTable.COLUMN_NAME_PROTOCOL,prot);
		chunks = addr.split(":");
		values.put(SocketTable.COLUMN_NAME_IP,chunks[0]);
		values.put(SocketTable.COLUMN_NAME_PORT,chunks[1]);
		values.put(SocketTable.COLUMN_NAME_OPENED,df.format(opened));
		values.put(SocketTable.COLUMN_NAME_CLOSED,df.format(closed));
		putRow(db,SocketTable.TABLE_NAME,values);
	}

	public void putGSMCell(SQLiteDatabase db, MinerLocation location, Date time) {
		ContentValues values = new ContentValues();
		values.put(GSMCellTable.COLUMN_NAME_MCC,location.getMcc());
		values.put(GSMCellTable.COLUMN_NAME_MNC,location.getMnc());
		values.put(GSMCellTable.COLUMN_NAME_LAC,location.getLac());
		values.put(GSMCellTable.COLUMN_NAME_CELLID,location.getId());
		values.put(GSMCellTable.COLUMN_NAME_STRENGTH,location.getStrength());
		values.put(GSMCellTable.COLUMN_NAME_TIME,df.format(time));
		putRow(db,GSMCellTable.TABLE_NAME,values);
	}

	public void putMobileNetwork(SQLiteDatabase db, TelephonyManager manager, Date time) {
		ContentValues values = new ContentValues();
		values.put(MobileNetworkTable.COLUMN_NAME_NETWORKNAME, manager.getNetworkOperatorName());
		values.put(MobileNetworkTable.COLUMN_NAME_NETWORK, manager.getNetworkOperator());
		values.put(MobileNetworkTable.COLUMN_NAME_TIME,df.format(time));
		putRow(db,MobileNetworkTable.TABLE_NAME,values);	
	}
	
	public void putWifiNetwork(SQLiteDatabase db, WifiData data, Date time ) {
		ContentValues values = new ContentValues();
		values.put(WifiNetworkTable.COLUMN_NAME_SSID,data.getSSID());
		values.put(WifiNetworkTable.COLUMN_NAME_BSSID,data.getBSSID());
		values.put(WifiNetworkTable.COLUMN_NAME_IP,data.getIP());
		values.put(WifiNetworkTable.COLUMN_NAME_TIME,df.format(time));
		putRow(db,WifiNetworkTable.TABLE_NAME,values);
	}
	
	public void putMinerLog(SQLiteDatabase db, Date start, Date stop) {
		ContentValues values = new ContentValues();
		values.put(MinerLogTable.COLUMN_NAME_START,df.format(start));
		values.put(MinerLogTable.COLUMN_NAME_STOP,df.format(stop));
		putRow(db,MinerLogTable.TABLE_NAME,values);
	}	
		
	private void putRow(SQLiteDatabase db, String table, ContentValues values ) {
		// http://developer.android.com/training/basics/data-storage/databases.html#WriteDbRow
		try {
			db.insert(table,null,values);
		}
		catch(Exception e) {
			
		}
	}
	
	private String howLongAgo(Date date) {
		long divider,count;
		long howLong = new Date().getTime() - date.getTime();
		String unit;

		divider = 1000; unit = "Second";
		if (howLong > 60000 && howLong < 3600000) {divider = 60000; unit = "Minute";}
		if (howLong > 3600000 && howLong < 86400000) {divider = 3600000; unit = "Hour";}
		if (howLong > 86400000) {divider = 86400000; unit = "Day";}
		
		count = howLong / divider;
		
		if (count < 2) {
			return "one "+unit+" ago.";
		}
		else {
			return Long.toString(count)+" "+unit+"s ago";
		}

	}
	
	private void initBookKeepingDate(SQLiteDatabase db,String key) {
		ContentValues values = new ContentValues();
		values.put(BookKeepingTable.COLUMN_NAME_KEY, key);
		values.put(BookKeepingTable.COLUMN_NAME_VALUE, BookKeepingTable.NULL_DATE);
		putRow(db,BookKeepingTable.TABLE_NAME,values);
	}
	
	public Date getBookKeepingDate(SQLiteDatabase db,String key) {
		String[] retColumns = {BookKeepingTable.COLUMN_NAME_VALUE};
		String[] whereValues = {key};
		Cursor c = db.query(BookKeepingTable.TABLE_NAME,retColumns,BookKeepingTable.COLUMN_NAME_KEY+" = ?",
			whereValues,null,null,null);
		c.moveToFirst();
		String dateString;	
		try {
			dateString = c.getString(c.getColumnIndex(BookKeepingTable.COLUMN_NAME_VALUE));
		}
		catch (Exception e) {
			initBookKeepingDate(db,key);
			dateString = BookKeepingTable.NULL_DATE;
		}
		
		if (dateString.equals(BookKeepingTable.NULL_DATE)) {
			return null;
		}
		else {
			try {
				return df.parse(dateString);
			} catch (ParseException e) {
				return null;
			}
		}	
	}
	
	public void setBookKeepingDate(SQLiteDatabase db,String key,Date date) {
		// http://developer.android.com/training/basics/data-storage/databases.html#UpdateDbRow
		ContentValues values = new ContentValues();
		String[] whereArgs = {key};
		values.put(BookKeepingTable.COLUMN_NAME_VALUE, df.format(date));
		db.update(BookKeepingTable.TABLE_NAME, values, BookKeepingTable.COLUMN_NAME_KEY+" = ?", whereArgs);
	}
	
	public String getLastExported(SQLiteDatabase db) {
		Date exported = getBookKeepingDate(db,BookKeepingTable.DATA_LAST_EXPORTED);
		if (exported != null) {
			return howLongAgo(exported);
		}
		return null;
	}
	
	public void setLastExported(SQLiteDatabase db, Date date) {
		setBookKeepingDate(db, BookKeepingTable.DATA_LAST_EXPORTED,date);		
	}
	
	public String getLastExpired(SQLiteDatabase db) {
		Date expired = getBookKeepingDate(db,BookKeepingTable.DATA_LAST_EXPIRED);
		if (expired != null) {
			return howLongAgo(expired);
		}
		return null;
	}
	
	public void expireData(SQLiteDatabase db) {
		Date expiryDate = getBookKeepingDate(db,BookKeepingTable.DATA_LAST_EXPORTED);
		if (expiryDate == null) return;
		String[] expiryValues = {df.format(expiryDate)};
		int i;
		for (i=0;i<MinerTables.ExpirableTables.length;i++) {
			db.delete(MinerTables.ExpirableTables[i], MinerTables.ExpirableTimeStamps[i]+ " < ?", expiryValues);
		}
		getBookKeepingDate(db,BookKeepingTable.DATA_LAST_EXPIRED);
		setBookKeepingDate(db,BookKeepingTable.DATA_LAST_EXPIRED,expiryDate);
	}
	
	
}
