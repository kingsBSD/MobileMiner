package uk.ac.kcl.odo.mobileminer.data;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import uk.ac.kcl.odo.mobileminer.data.MinerTables.GSMCellTable;
import uk.ac.kcl.odo.mobileminer.data.MinerTables.MobileNetworkTable;
import uk.ac.kcl.odo.mobileminer.data.MinerTables.SocketTable;
import uk.ac.kcl.odo.mobileminer.data.MinerTables.WifiNetworkTable;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class WriteCache {
	private Context context;
	
	public final static String CACHE_SOCKET = "uk.ac.kcl.odo.mobileminer.cachesocket";
	public final static String SOCKET_NAME = "socket_name";
	public final static String SOCKET_ADDRESS = "socket_addr";
	public final static String SOCKET_PROTOCOL = "socket_protocol";
	public final static String SOCKET_OPENED = "socket_opened";
	public final static String SOCKET_CLOSED = "socket_closed";
	public final static String SOCKET_DAY = "socket_day";
	
	public final static String CACHE_GSMCELL = "uk.ac.kcl.odo.mobileminer.cachegsmcell";
	public final static String GSMCELL_MCC = "gsmcell_mcc";
	public final static String GSMCELL_MNC = "gsmcell_mnc";
	public final static String GSMCELL_LAC = "gsmcell_lac";
	public final static String GSMCELL_CELLID = "gsmcell_cellid";
	public final static String GSMCELL_STRENGTH = "gsmcell_strength";
	public final static String GSMCELL_TIME = "gsmcell_time";
	public final static String GSMCELL_DAY = "gsmcell_day";
	
	public final static String CACHE_MOBILENETWORK = "uk.ac.kcl.odo.mobileminer.mobilenetwork";
	public final static String MOBILENETWORK_NAME = "mobilenetwork_name";
	public final static String MOBILENETWORK_NETWORK = "mobilenetwork_network";
	public final static String MOBILENETWORK_TIME = "mobilenetwork_time";
	
	public final static String CACHE_WIFINETWORK = "uk.ac.kcl.odo.mobileminer.wifinetwork";
	public final static String WIFINETWORK_SSID = "wifinetwork_ssid";
	public final static String WIFINETWORK_BSSID = "wifinetwork_bssid";
	public final static String WIFINETWORK_IP = "wifinetwork_ip";
	public final static String WIFINETWORK_TIME = "wifinetwork_time";
	public final static String WIFINETWORK_DAY = "wifinetwork_day";
	
	private final String[] filters = {CACHE_SOCKET,CACHE_GSMCELL,CACHE_MOBILENETWORK,CACHE_WIFINETWORK};
	
	private ConcurrentLinkedQueue<CachedSocket> socketQueue;
	private ConcurrentLinkedQueue<CachedGSMCell> gsmCellQueue;
	private ConcurrentLinkedQueue<CachedMobileNetwork> mobileNetworkQueue;
	private ConcurrentLinkedQueue<CachedWiFiNetwork> wifiNetworkQueue;
	
	
	private class CachedSocket {
		String name,prot,addr,opened,closed,day;
		private CachedSocket(String nm,String pr,String ad,String op,String cl,String dy) {
			name = nm; prot = pr; addr = ad; opened = op; closed = cl; day = dy;
		}
		private ContentValues getValues() {
			return MinerData.getSocketValues(name, prot, addr, opened, closed, day);
		}
	}
	
	private class CachedGSMCell {
		String mcc, mnc, lac, cellid, strength, time, day;
		private CachedGSMCell(String cc, String nc, String lc, String id, String str, String tm, String dy) {
			mcc = cc; mnc = nc; lac =lc; cellid = id; strength = str; time = tm; day = dy;
		}
		private ContentValues getValues() {
			return MinerData.getGSMCellValues(mcc, mnc, lac, cellid, strength, time, day);
		}	
	}
	
	private class CachedMobileNetwork {
		String name; String network; String time;
		private CachedMobileNetwork(String nm, String net, String tm) {
			name = nm; network = net; time = tm;		
		}
		private ContentValues getValues() {
			return MinerData.getMobileNetworkValues(name, network, time);
		}		
	}
	
	private class CachedWiFiNetwork {
		String ssid, bssid, ip, time, day;
		CachedWiFiNetwork(String ss, String bss, String addr, String tm, String dy) {
			ssid = ss; bssid = bss; ip = addr; time = tm; day = dy;	
		}
		private ContentValues getValues() {
			return MinerData.getWiFiNetworkValues(ssid,bssid,ip,time,day);
		}
	}
	
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context ctx, Intent intent) {

			String action = intent.getAction();
			
			switch (action) {
				case CACHE_SOCKET:
					socketQueue.offer(new CachedSocket(intent.getStringExtra(SOCKET_NAME), 
						intent.getStringExtra(SOCKET_PROTOCOL),
						intent.getStringExtra(SOCKET_ADDRESS),
						intent.getStringExtra(SOCKET_OPENED), intent.getStringExtra(SOCKET_CLOSED),
						intent.getStringExtra(SOCKET_DAY)));
					Log.i("MinerCache","Socket received...");
					break;
				case CACHE_GSMCELL:
					gsmCellQueue.offer(new CachedGSMCell(intent.getStringExtra(GSMCELL_MCC),
						intent.getStringExtra(GSMCELL_MNC),
						intent.getStringExtra(GSMCELL_LAC),
						intent.getStringExtra(GSMCELL_CELLID),
						intent.getStringExtra(GSMCELL_STRENGTH),
						intent.getStringExtra(GSMCELL_TIME),
						intent.getStringExtra(GSMCELL_DAY)));
					Log.i("MinerCache","GSM cell received...");
					break;
				case CACHE_MOBILENETWORK:
					mobileNetworkQueue.offer(new CachedMobileNetwork(intent.getStringExtra(MOBILENETWORK_NAME),
						intent.getStringExtra(MOBILENETWORK_NETWORK),
						intent.getStringExtra(MOBILENETWORK_TIME)));
					Log.i("MinerCache","WiFi network received...");
					break;
				case CACHE_WIFINETWORK:
					wifiNetworkQueue.offer(new CachedWiFiNetwork(intent.getStringExtra(WIFINETWORK_SSID),
						intent.getStringExtra(WIFINETWORK_BSSID),
						intent.getStringExtra(WIFINETWORK_IP),
						intent.getStringExtra(WIFINETWORK_TIME),
						intent.getStringExtra(WIFINETWORK_DAY)));
					break;
					
			}
			
			
		}
		
	};
	
	
	public WriteCache(Context ctx) {
		LocalBroadcastManager manager;
		
		context = ctx;
		
		manager = LocalBroadcastManager.getInstance(context);
		
		socketQueue = new ConcurrentLinkedQueue<CachedSocket>();
		gsmCellQueue = new ConcurrentLinkedQueue<CachedGSMCell>(); 
		mobileNetworkQueue = new ConcurrentLinkedQueue<CachedMobileNetwork>();
		wifiNetworkQueue = new ConcurrentLinkedQueue<CachedWiFiNetwork>();
		
		for (String filter: filters) {
			manager.registerReceiver(receiver,new IntentFilter(filter));
		}
				
	}
	
	public void flush() {
		
		
		int queueTotal = socketQueue.size() + gsmCellQueue.size() + mobileNetworkQueue.size();
				
		if (queueTotal > 0) {
		
			MinerData helper = new MinerData(context);
			SQLiteDatabase db = helper.getWritableDatabase();
		
			try {
				db.beginTransaction();
						
				while (socketQueue.size() != 0) {
					db.insert(SocketTable.TABLE_NAME, null, socketQueue.poll().getValues());
				}
			
				while (gsmCellQueue.size() != 0) {
					db.insert(GSMCellTable.TABLE_NAME, null, gsmCellQueue.poll().getValues());
				}
				
				while (mobileNetworkQueue.size() != 0) {
					db.insert(MobileNetworkTable.TABLE_NAME, null, mobileNetworkQueue.poll().getValues());
				}

				while (wifiNetworkQueue.size() != 0) {
					db.insert(WifiNetworkTable.TABLE_NAME, null, wifiNetworkQueue.poll().getValues());
				}
				
				db.setTransactionSuccessful();
				Log.i("MinerCache","Flushed...");
			
			}
			catch (SQLiteException e) {
				Log.i("MinerCache","SQLite error!");
			}
			finally {
				db.endTransaction();
			}
		
			helper.close();
		}
		

	}
	
	public void ShutDown() {
		LocalBroadcastManager manager;
		manager = LocalBroadcastManager.getInstance(context);
		manager.unregisterReceiver(receiver);
	}
	
	
}
