// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt

// Define the database tables. -More boiler-plate than Swan Hunter...

package com.odo.kcl.mobileminer.miner;

import android.provider.BaseColumns;

public final class MinerTables {
    private MinerTables() {
    }

    public static final String APP_VERSION = "0.5";

    public static abstract class SocketTable implements BaseColumns {
        public static final String TABLE_NAME = "socket";
        public static final String COLUMN_NAME_PROCESS = "process";
        public static final String COLUMN_NAME_PROTOCOL = "protocol";
        public static final String COLUMN_NAME_IP = "ip";
        public static final String COLUMN_NAME_PORT = "port";
        public static final String COLUMN_NAME_OPENED = "opened";
        public static final String COLUMN_NAME_CLOSED = "closed";
        public static final String COLUMN_NAME_DAY = "day";
    }

    public static final String CREATE_SOCKET_TABLE =
            "CREATE TABLE " + SocketTable.TABLE_NAME + " (" +
                    SocketTable.COLUMN_NAME_PROCESS + " TEXT, " +
                    SocketTable.COLUMN_NAME_PROTOCOL + " TEXT, " +
                    SocketTable.COLUMN_NAME_IP + " TEXT, " +
                    SocketTable.COLUMN_NAME_PORT + " TEXT, " +
                    SocketTable.COLUMN_NAME_OPENED + " TEXT, " +
                    SocketTable.COLUMN_NAME_CLOSED + " TEXT, " +
                    SocketTable.COLUMN_NAME_DAY + " TEXT );";

    public static final String SOCKET_TABLE_TIMESTAMP = SocketTable.COLUMN_NAME_CLOSED;

    public static abstract class GSMCellTable implements BaseColumns {
        public static final String TABLE_NAME = "gsmcell";
        public static final String COLUMN_NAME_MCC = "mcc";
        public static final String COLUMN_NAME_MNC = "mnc";
        public static final String COLUMN_NAME_LAC = "lac";
        public static final String COLUMN_NAME_CELLID = "cid";
        public static final String COLUMN_NAME_STRENGTH = "strength";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_DAY = "day";
    }

    public static final String CREATE_GSMCELL_TABLE =
            "CREATE TABLE " + GSMCellTable.TABLE_NAME + " (" +
                    GSMCellTable.COLUMN_NAME_MCC + " TEXT, " +
                    GSMCellTable.COLUMN_NAME_MNC + " TEXT, " +
                    GSMCellTable.COLUMN_NAME_LAC + " TEXT, " +
                    GSMCellTable.COLUMN_NAME_CELLID + " TEXT, " +
                    GSMCellTable.COLUMN_NAME_STRENGTH + " TEXT, " +
                    GSMCellTable.COLUMN_NAME_TIME + " TEXT, " +
                    GSMCellTable.COLUMN_NAME_DAY + " TEXT );";

    public static final String GSMCELL_TABLE_TIMESTAMP = GSMCellTable.COLUMN_NAME_TIME;

    public static abstract class GSMLocationTable implements BaseColumns {
        public static final String TABLE_NAME = "gsmlocation";
        public static final String COLUMN_NAME_MCC = "mcc";
        public static final String COLUMN_NAME_MNC = "mnc";
        public static final String COLUMN_NAME_LAC = "lac";
        public static final String COLUMN_NAME_CELLID = "cid";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LONG = "long";
        public static final String COLUMN_NAME_SOURCE = "source";
        public static final String COLUMN_NAME_TIME = "time";
    }

    public static final String GSMLOCATION_TABLE_TIMESTAMP = GSMLocationTable.COLUMN_NAME_TIME;

    public static final String CREATE_GSMLOCATION_TABLE =
            "CREATE TABLE " + GSMLocationTable.TABLE_NAME + " (" +
                    GSMLocationTable.COLUMN_NAME_MCC + " TEXT, " +
                    GSMLocationTable.COLUMN_NAME_MNC + " TEXT, " +
                    GSMLocationTable.COLUMN_NAME_LAC + " TEXT, " +
                    GSMLocationTable.COLUMN_NAME_CELLID + " TEXT, " +
                    GSMLocationTable.COLUMN_NAME_LAT + " TEXT, " +
                    GSMLocationTable.COLUMN_NAME_LONG + " TEXT, " +
                    GSMLocationTable.COLUMN_NAME_SOURCE + " TEXT, " +
                    GSMLocationTable.COLUMN_NAME_TIME + " TEXT );";

    public static abstract class GSMCellPolygonTable implements BaseColumns {
        public static final String TABLE_NAME = "gsmcellpolygon";
        public static final String COLUMN_NAME_MCC = "mcc";
        public static final String COLUMN_NAME_MNC = "mnc";
        public static final String COLUMN_NAME_LAC = "lac";
        public static final String COLUMN_NAME_CELLID = "cid";
        public static final String COLUMN_NAME_JSON = "json";
        public static final String COLUMN_NAME_SOURCE = "source";
        public static final String COLUMN_NAME_TIME = "time";
    }

    public static final String GSMCELLPOLYGON_TABLE_TIMESTAMP = GSMCellPolygonTable.COLUMN_NAME_TIME;

    public static final String CREATE_GSMCELLPOLYGON_TABLE =
            "CREATE TABLE " + GSMCellPolygonTable.TABLE_NAME + " (" +
                    GSMLocationTable.COLUMN_NAME_MCC + " TEXT, " +
                    GSMCellPolygonTable.COLUMN_NAME_MNC + " TEXT, " +
                    GSMCellPolygonTable.COLUMN_NAME_LAC + " TEXT, " +
                    GSMCellPolygonTable.COLUMN_NAME_CELLID + " TEXT, " +
                    GSMCellPolygonTable.COLUMN_NAME_JSON + " TEXT, " +
                    GSMCellPolygonTable.COLUMN_NAME_SOURCE + " TEXT, " +
                    GSMCellPolygonTable.COLUMN_NAME_TIME + " TEXT );";

    public static abstract class MobileNetworkTable implements BaseColumns {
        public static final String TABLE_NAME = "mobilenetwork";
        public static final String COLUMN_NAME_NETWORKNAME = "networkname";
        public static final String COLUMN_NAME_NETWORK = "network";
        public static final String COLUMN_NAME_TIME = "time";
    }

    public static final String CREATE_MOBILENETWORK_TABLE =
            "CREATE TABLE " + MobileNetworkTable.TABLE_NAME + " (" +
                    MobileNetworkTable.COLUMN_NAME_NETWORKNAME + " TEXT, " +
                    MobileNetworkTable.COLUMN_NAME_NETWORK + " TEXT, " +
                    MobileNetworkTable.COLUMN_NAME_TIME + " TEXT );";

    public static final String MOBILENETWORK_TABLE_TIMESTAMP = MobileNetworkTable.COLUMN_NAME_TIME;

    public static abstract class WifiNetworkTable implements BaseColumns {
        public static final String TABLE_NAME = "wifinetwork";
        public static final String COLUMN_NAME_SSID = "ssid";
        public static final String COLUMN_NAME_BSSID = "bssid";
        public static final String COLUMN_NAME_IP = "ip";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_DAY = "day";
    }

    public static final String CREATE_WIFINETWORK_TABLE =
            "CREATE TABLE " + WifiNetworkTable.TABLE_NAME + " (" +
                    WifiNetworkTable.COLUMN_NAME_SSID + " TEXT, " +
                    WifiNetworkTable.COLUMN_NAME_BSSID + " TEXT, " +
                    WifiNetworkTable.COLUMN_NAME_IP + " TEXT, " +
                    WifiNetworkTable.COLUMN_NAME_TIME + " TEXT, " +
                    WifiNetworkTable.COLUMN_NAME_DAY + " TEXT );";

    public static final String WIFINETWORK_TABLE_TIMESTAMP = WifiNetworkTable.COLUMN_NAME_TIME;

    public static abstract class MinerLogTable implements BaseColumns {
        public static final String TABLE_NAME = "minerlog";
        public static final String COLUMN_NAME_START = "start";
        public static final String COLUMN_NAME_STOP = "stop";
    }

    public static final String CREATE_MINERLOG_TABLE =
            "CREATE TABLE " + MinerLogTable.TABLE_NAME + " (" +
                    MinerLogTable.COLUMN_NAME_START + " TEXT, " +
                    MinerLogTable.COLUMN_NAME_STOP + " TEXT );";

    public static final String MINER_TABLE_TIMESTAMP = MinerLogTable.COLUMN_NAME_STOP;

    public static abstract class NotificationTable implements BaseColumns {
        public static final String TABLE_NAME = "notification";
        public static final String COLUMN_NAME_PACKAGE = "package";
        // None of our business...
        // public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_DAY = "day";
    }

    public static final String CREATE_NOTIFICATION_TABLE =
            "CREATE TABLE " + NotificationTable.TABLE_NAME + " (" +
                    NotificationTable.COLUMN_NAME_PACKAGE + " TEXT, " +
                    // None of our business...
                    // NotificationTable.COLUMN_NAME_TEXT + " TEXT, " +
                    NotificationTable.COLUMN_NAME_TIME + " TEXT, " +
                    NotificationTable.COLUMN_NAME_DAY + " TEXT );";

    public static final String NOTIFICATION_TABLE_TIMESTAMP = NotificationTable.COLUMN_NAME_TIME;

    public static abstract class BookKeepingTable implements BaseColumns {
        public static final String TABLE_NAME = "bookkeeping";
        public static final String COLUMN_NAME_KEY = "key";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String DATA_LAST_EXPORTED = "datalastexported";
        public static final String DATA_LAST_EXPIRED = "datalastexpired";
        public static final String NULL_DATE = "nulldate";
    }

    public static final String CREATE_BOOKKEEPING_TABLE =
            "CREATE TABLE " + BookKeepingTable.TABLE_NAME + " (" +
                    BookKeepingTable.COLUMN_NAME_KEY + " TEXT, " +
                    BookKeepingTable.COLUMN_NAME_VALUE + " TEXT );";

    public static final String[] CreateTables = {
            MinerTables.CREATE_SOCKET_TABLE,
            MinerTables.CREATE_GSMCELL_TABLE,
            MinerTables.CREATE_GSMLOCATION_TABLE,
            MinerTables.CREATE_GSMCELLPOLYGON_TABLE,
            MinerTables.CREATE_MOBILENETWORK_TABLE,
            MinerTables.CREATE_WIFINETWORK_TABLE,
            MinerTables.CREATE_MINERLOG_TABLE,
            MinerTables.CREATE_NOTIFICATION_TABLE,
            MinerTables.CREATE_BOOKKEEPING_TABLE};

    public static final String[] ExpirableTables = {
            SocketTable.TABLE_NAME, GSMCellTable.TABLE_NAME, GSMLocationTable.TABLE_NAME, GSMCellPolygonTable.TABLE_NAME,
            MobileNetworkTable.TABLE_NAME, WifiNetworkTable.TABLE_NAME, MinerLogTable.TABLE_NAME, NotificationTable.TABLE_NAME};

    public static final String[] ExpirableTimeStamps = {SOCKET_TABLE_TIMESTAMP, GSMCELL_TABLE_TIMESTAMP,
            GSMLOCATION_TABLE_TIMESTAMP, GSMCELLPOLYGON_TABLE_TIMESTAMP, MOBILENETWORK_TABLE_TIMESTAMP,
            WIFINETWORK_TABLE_TIMESTAMP, MINER_TABLE_TIMESTAMP,
            NOTIFICATION_TABLE_TIMESTAMP
    };

    public static final Object[] tableClasses = {SocketTable.class, GSMCellTable.class, MobileNetworkTable.class, WifiNetworkTable.class,
            MinerLogTable.class, NotificationTable.class};

}


