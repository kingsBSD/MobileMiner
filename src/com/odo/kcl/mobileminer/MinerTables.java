package com.odo.kcl.mobileminer;

import android.provider.BaseColumns;

public final class MinerTables {
	// To prevent someone from accidentally instantiating the contract class, give it an empty constructor.
	public MinerTables() {}

	public static abstract class SocketTable implements BaseColumns {
		public static final String TABLE_NAME = "socket";
		public static final String COLUMN_NAME_PROCESS = "process";
		public static final String COLUMN_NAME_PROTOCOL = "protocol";
		public static final String COLUMN_NAME_IP = "ip";
		public static final String COLUMN_NAME_PORT = "port";
		public static final String COLUMN_NAME_OPENED = "opened";
		public static final String COLUMN_NAME_CLOSED = "closed";
	}
	
	static final String CREATE_SOCKET_TABLE =
		"CREATE TABLE " + SocketTable.TABLE_NAME + " (" +	
		SocketTable.COLUMN_NAME_PROCESS + " TEXT, " +
		SocketTable.COLUMN_NAME_PROTOCOL + " TEXT, " +
		SocketTable.COLUMN_NAME_IP + " TEXT, " +
		SocketTable.COLUMN_NAME_PORT + " TEXT, " +
		SocketTable.COLUMN_NAME_OPENED + " TEXT, " +
		SocketTable.COLUMN_NAME_CLOSED + " TEXT );";

	public static abstract class GSMCellTable implements BaseColumns {
		public static final String TABLE_NAME = "gsmcell";
		public static final String COLUMN_NAME_MCC = "mcc";
		public static final String COLUMN_NAME_MNC = "mnc";
		public static final String COLUMN_NAME_LAC = "lac";
		public static final String COLUMN_NAME_CELLID = "cellid";
		public static final String COLUMN_NAME_STRENGTH = "strength";
		public static final String COLUMN_NAME_TIME = "time";
	}
	
	static final String CREATE_GSMCELL_TABLE =
		"CREATE TABLE " + GSMCellTable.TABLE_NAME + " (" +
		GSMCellTable.COLUMN_NAME_MCC + " TEXT, " +
		GSMCellTable.COLUMN_NAME_MNC + " TEXT, " +
		GSMCellTable.COLUMN_NAME_LAC + " TEXT, " +
		GSMCellTable.COLUMN_NAME_CELLID + " TEXT, " +
		GSMCellTable.COLUMN_NAME_STRENGTH + " TEXT, " +
		GSMCellTable.COLUMN_NAME_TIME + " TEXT );";
	
	public static abstract class GSMLocationTable implements BaseColumns {
		public static final String TABLE_NAME = "gsmlocation";
		public static final String COLUMN_NAME_MCC = "mcc";
		public static final String COLUMN_NAME_MNC = "mnc";
		public static final String COLUMN_NAME_LAC = "lac";
		public static final String COLUMN_NAME_CELLID = "cellid";
		public static final String COLUMN_NAME_LAT = "lat";
		public static final String COLUMN_NAME_LONG = "long";
	}
	
	static final String CREATE_GSMLOCATION_TABLE =
			"CREATE TABLE " + GSMCellTable.TABLE_NAME + " (" +
			GSMLocationTable.COLUMN_NAME_MCC + " TEXT, " +
			GSMLocationTable.COLUMN_NAME_MNC + " TEXT, " +
			GSMLocationTable.COLUMN_NAME_LAC + " TEXT, " +
			GSMLocationTable.COLUMN_NAME_CELLID + " TEXT, " +
			GSMLocationTable.COLUMN_NAME_LAT + " TEXT, " +
			GSMLocationTable.COLUMN_NAME_LONG + " TEXT );";
	
	public static abstract class MobileNetworkTable implements BaseColumns {
		public static final String TABLE_NAME = "mobilenetwork";
		public static final String COLUMN_NAME_NETWORKNAME = "networkname";
		public static final String COLUMN_NAME_NETWORK = "network";
		public static final String COLUMN_NAME_TIME = "time";
	}
	
	static final String CREATE_MOBILENETWORK_TABLE =
		"CREATE TABLE " + MobileNetworkTable.TABLE_NAME + " (" +
		MobileNetworkTable.COLUMN_NAME_NETWORKNAME + " TEXT, " +
		MobileNetworkTable.COLUMN_NAME_NETWORK + " TEXT, " +
		MobileNetworkTable.COLUMN_NAME_TIME + " TEXT );";
		
	public static abstract class WifiNetworkTable implements BaseColumns {
		public static final String TABLE_NAME = "wifinetwork";
		public static final String COLUMN_NAME_SSID = "ssid";
		public static final String COLUMN_NAME_BSSID = "bssid";
		public static final String COLUMN_NAME_IP = "ip";
		public static final String COLUMN_NAME_TIME = "time";	
	}
		
	static final String CREATE_WIFINETWORK_TABLE =
		"CREATE TABLE " + WifiNetworkTable.TABLE_NAME + " (" +
	    WifiNetworkTable.COLUMN_NAME_SSID + " TEXT, " +
	    WifiNetworkTable.COLUMN_NAME_BSSID + " TEXT, " +
	    WifiNetworkTable.COLUMN_NAME_IP + " TEXT, " +
	    WifiNetworkTable.COLUMN_NAME_TIME + " TEXT );";
	
	public static abstract class MinerLogTable implements BaseColumns {
		public static final String TABLE_NAME = "minerlog";
		public static final String COLUMN_NAME_START = "start";
		public static final String COLUMN_NAME_STOP = "stop";
	}
	
	static final String CREATE_MINERLOG_TABLE =
		"CREATE TABLE " + MinerLogTable.TABLE_NAME + " (" +
		MinerLogTable.COLUMN_NAME_START + " TEXT, " +
		MinerLogTable.COLUMN_NAME_STOP + " TEXT );";
	
	public static abstract class BookKeepingTable implements BaseColumns {
		public static final String TABLE_NAME = "bookkeeping";
		public static final String COLUMN_NAME_KEY = "key";
		public static final String COLUMN_NAME_VALUE = "value";	
	}
	
	static final String CREATE_BOOKKEEPING_TABLE =
		"CREATE TABLE " + BookKeepingTable.TABLE_NAME + " (" +
		BookKeepingTable.COLUMN_NAME_KEY + " TEXT, " +
		BookKeepingTable.COLUMN_NAME_VALUE + " TEXT );";
	
}	


