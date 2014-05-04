package com.odo.kcl.mobileminer;

import java.math.BigInteger;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.odo.kcl.mobileminer.MinerTables.GSMCellTable;
import com.odo.kcl.mobileminer.MinerTables.MinerLogTable;
import com.odo.kcl.mobileminer.MinerTables.MobileNetworkTable;
import com.odo.kcl.mobileminer.MinerTables.SocketTable;
import com.odo.kcl.mobileminer.MinerTables.WifiNetworkTable;

import android.content.ContentValues;
import android.content.Context;
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
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    
    
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
		for (String sql: new String[]{
			MinerTables.CREATE_SOCKET_TABLE,
			MinerTables.CREATE_GSMCELL_TABLE,
			MinerTables.CREATE_GSMLOCATION_TABLE,
			MinerTables.CREATE_MOBILENETWORK_TABLE,
			MinerTables.CREATE_WIFINETWORK_TABLE,
			MinerTables.CREATE_MINERLOG_TABLE,
			MinerTables.CREATE_BOOKKEEPING_TABLE}) {
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
		try {
			db.insert(table,null,values);
		}
		catch(Exception e) {
			
		}
	}
		
}
