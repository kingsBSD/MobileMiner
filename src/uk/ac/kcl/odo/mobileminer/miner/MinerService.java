// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt

// This is the service that grabs most of the data.

package uk.ac.kcl.odo.mobileminer.miner;

import java.util.ArrayList;
import java.util.Date;

import uk.ac.kcl.odo.mobileminer.cells.MinerLocation;
import uk.ac.kcl.odo.mobileminer.ckan.CkanUidGetter;
import uk.ac.kcl.odo.mobileminer.ckan.CkanUpdater;
import uk.ac.kcl.odo.mobileminer.ckan.CkanUrlGetter;
import uk.ac.kcl.odo.mobileminer.data.MinerData;
import uk.ac.kcl.odo.mobileminer.data.MinerData.WifiData;
import uk.ac.kcl.odo.mobileminer.data.WriteCache;
import uk.ac.kcl.odo.mobileminer.R;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
//import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
//import android.telephony.CellInfo;
//import android.telephony.CellInfoGsm;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;
//import android.util.Log;

/**
 * An Android service that logs connections to mobile and wireless networks, changes in cell location,
 * and the opening and closing of network sockets.
*/
public class MinerService extends Service {

	private Date startTime;
	private WriteCache cache;
	private ProcSocketSet socketSet;
	private TrafficWatcher watcher;
	private IntentFilter filter;
	private Handler cacheHandle,scanHandle,updateHandle;
	private boolean scanning,updating;
	private Runnable cacheWorker,mineWorker,ckanWorker;
	private Context context;
	private String networkName;
	//private String cellLocation;
	private ArrayList<MinerLocation> cells;
	//private ArrayList<String> cellIds;
	//private Boolean mobileData, wifiData;
	private WifiData wirelessData;
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
		    if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) { // We've switched mobile/wireless networks.
		    	connectivityChanged();
		    	//Log.i("MinerService","CONNECTIVITY_CHANGE");
		    }
   
		    if (action.equals("com.odo.kcl.mobileminer.updatequery")) { // The MainActivity wants an update.
		        socketSet.broadcast();
		        cellBroadcast();
		        networkBroadcast();
		        //Log.i("MinerService","RECEIVED SOCKET QUERY");
		     }

		    if (action.equals("com.odo.kcl.mobileminer.stopmining")) {
		    	scanning = false;
		    	stopForeground(true);
		    	stopSelf();
		    }
		}
	};
		
	private final PhoneStateListener phoneListener = new PhoneStateListener() {
		@Override
		public void onDataActivity(int direction) {
			// Is it worth scanning for sockets?
			if (direction != TelephonyManager.DATA_ACTIVITY_NONE || direction != TelephonyManager.DATA_ACTIVITY_DORMANT) {
				//Log.i("MinerService","DATA_ACTIVE");
				startScan();
			}
			else {
				scanning = false;
			}
		}

		// What a swiz:
		// http://code.google.com/p/android/issues/detail?id=43467
		// http://stackoverflow.com/questions/20049510/oncellinfochanged-callback-is-always-null
		// It would be nice if we could get a set of active cell towers and their signal strengths with the newer API. Oh well.
		
		/**
		@Override
		public void onCellInfoChanged (List<CellInfo> cellInfo) {
			ArrayList<CellInfoGsm> cellInfoGsm = new ArrayList<CellInfoGsm>();
			ArrayList<String> activeCells = new ArrayList<String>();
			MinerLocation newLocation;
			Boolean newCell = false;
			String cellId;
			if (cellInfo != null) {
				cells = new ArrayList<MinerLocation>();
				for (CellInfo info: cellInfo) cellInfoGsm.add((CellInfoGsm) info);
				cells = new ArrayList<MinerLocation>();
				for (CellInfoGsm info: cellInfoGsm) {
					newLocation = new MinerLocation(info);
					cells.add(newLocation);
					cellId = newLocation.dump();
					activeCells.add(cellId);
					if (!cellIds.contains(cellId)) newCell = true;
					cells.add(new MinerLocation(info));
				}
				cellIds = activeCells;
			}
			
			if (newCell) {
				MinerData helper = new MinerData(context);
				SQLiteDatabase db = helper.getWritableDatabase();
				for (MinerLocation cell: cells) helper.putGSMCell(db,cell,new Date());
				helper.close();
			}
			cellBroadcast();
		}
		**/

		@Override
		public void onCellLocationChanged (CellLocation location) {
			cells = new ArrayList<MinerLocation>();
			if (location != null) {
				Date rightNow = new Date();
				MinerLocation loc = new MinerLocation(location,context);
				cells.add(loc);
								
				Intent intent = new Intent(WriteCache.CACHE_GSMCELL);
				intent.putExtra(WriteCache.GSMCELL_MCC, loc.getMcc());
				intent.putExtra(WriteCache.GSMCELL_MNC, loc.getMnc());
				intent.putExtra(WriteCache.GSMCELL_LAC, loc.getLac());
				intent.putExtra(WriteCache.GSMCELL_CELLID, loc.getId());
				intent.putExtra(WriteCache.GSMCELL_STRENGTH, loc.getStrength());
				intent.putExtra(WriteCache.GSMCELL_STRENGTH, MinerData.df.format(rightNow));
				intent.putExtra(WriteCache.GSMCELL_STRENGTH, MinerData.dayGetter.format(rightNow));
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
				
				
			}
			cellBroadcast();
			//Log.i("MinerService","CELL_LOCATION_CHANGED");
		}	
	};
	
	@Override
	public void onCreate() {
		startTime = new Date();
		networkName = "null";
		//mobileData = false;
		//wifiData = false;
		context = this;
		wirelessData = new WifiData();
		cells = new ArrayList<MinerLocation>();
		//cellIds = new ArrayList<String>();
		cache = new WriteCache(this);
		socketSet = new ProcSocketSet(this);
		watcher = new TrafficWatcher(this);
		cacheHandle = new Handler(); 
		scanHandle = new Handler();
		updateHandle = new Handler();
		
		filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		filter.addAction("com.odo.kcl.mobileminer.updatequery");
		filter.addAction("com.odo.kcl.mobileminer.stopmining");
		registerReceiver(receiver, filter);
	 
		cacheWorker = new Runnable() {

			@Override
			public void run() {
				cache.flush();
				cacheHandle.postDelayed(this, 120000);
			}
			
		};
		
		
		
		mineWorker = new Runnable() { // Check for new network sockets every half-second.
			@Override
			public void run() {
				try {
					//Log.i("MobileMiner","Tick...");
					socketSet.scan();
					watcher.scan();
				}
				catch (Exception e) {}
				finally {
					if (scanning) {
						scanHandle.postDelayed(this, 500);
					}
					else {
						socketSet.close();
						watcher.closeAll();
					}
				}
			}	 
		 };
		 
		ckanWorker = new Runnable() {
			@Override
			public void run() {
				//Log.i("MinerService","Updating...");
				new CkanUpdater().execute(new Context[] {context});
				if (updating) updateHandle.postDelayed(this, 600000);				
			}				
		};
		 
	 }
	 	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startTime = new Date();
		
		cacheHandle.post(cacheWorker);
		
		//Log.i("MinerService","started mining");
		int phoneFlags;
		
		phoneFlags = PhoneStateListener.LISTEN_DATA_ACTIVITY|PhoneStateListener.LISTEN_CELL_LOCATION;
		
		/**
		if (Build.VERSION.SDK_INT >= 17) {
			// http://code.google.com/p/android/issues/detail?id=43467
			// http://stackoverflow.com/questions/20049510/oncellinfochanged-callback-is-always-null
			phoneFlags = PhoneStateListener.LISTEN_DATA_ACTIVITY|PhoneStateListener.LISTEN_CELL_INFO
				|PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
		}
		else {
			phoneFlags = PhoneStateListener.LISTEN_DATA_ACTIVITY|PhoneStateListener.LISTEN_CELL_LOCATION;
		}
		**/
		 
		((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE)).listen(phoneListener,phoneFlags);
		Toast.makeText(this, "Started Mining...", Toast.LENGTH_SHORT).show();
		moveToForeground();
		return START_STICKY;
	}
	 
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		scanning = false;
		updating = false;
		MinerData helper = new MinerData(context);
		helper.putMinerLog(helper.getWritableDatabase(), startTime, new Date());
		helper.close();
		//Log.i("MinerService","stopped mining");
		unregisterReceiver(receiver);
		cache.ShutDown();
	    Toast.makeText(this, "Stopped Mining...", Toast.LENGTH_SHORT).show();
	}
	
	private void moveToForeground() {
		// https://github.com/commonsguy/cw-android/blob/master/Notifications/FakePlayer/src/com/commonsware/android/fakeplayerfg/PlayerService.java
		// http://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html
		Intent minerIntent = new Intent(this, MinerService.class);
		PendingIntent pendingMinerIntent = PendingIntent.getActivity(this,0,minerIntent,0);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.addAction(R.drawable.ic_launcher, "Started mining", pendingMinerIntent);
		Notification note = builder.build();
		startForeground(23, note);
	}
	Intent intent = new Intent(WriteCache.CACHE_GSMCELL);
	private void connectivityChanged() {
		String name = "None";
		ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = manager.getActiveNetworkInfo();
		if (netInfo != null) {
			if (netInfo.getState() ==  NetworkInfo.State.CONNECTED ) {
				switch (netInfo.getType()) {
					case ConnectivityManager.TYPE_WIFI:
						//wifiData = true;
						//mobileData = false;
						WifiManager wifiMgr = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
				 		WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
				 		name = wifiInfo.getSSID();
				 		if (!networkName.equals(name)) {
				 			Date rightNow = new Date();
				 			wirelessData = new WifiData(wifiInfo);
				 			Intent intent = new Intent(WriteCache.CACHE_WIFINETWORK);
				 			intent.putExtra(WriteCache.WIFINETWORK_SSID,wirelessData.getSSID());
				 			intent.putExtra(WriteCache.WIFINETWORK_BSSID,wirelessData.getBSSID());
				 			intent.putExtra(WriteCache.WIFINETWORK_IP,wirelessData.getIP());
				 			intent.putExtra(WriteCache.WIFINETWORK_TIME,MinerData.df.format(rightNow));
				 			intent.putExtra(WriteCache.WIFINETWORK_DAY,MinerData.dayGetter.format(rightNow));
				 			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
							networkName = name; networkBroadcast();	
				 		}
				 		startScan(); // Always scan when we've got WIFI.
				 		startUpdating();
				 		//Log.i("MinerService","CONNECTED: WIFI");
				 		break;
				 	case ConnectivityManager.TYPE_MOBILE:
						//wifiData = false;
						//mobileData = true;
						if ("goldfish".equals(Build.HARDWARE)) {
							if (!updating) startUpdating();
						}
						else {
							updating = false;	
						}
						
				 		// https://code.google.com/p/android/issues/detail?id=24227
				 		//String name; Cursor c;
				 		//c = this.getContentResolver().query(Uri.parse("content://telephony/carriers/preferapn"), null, null, null, null);
				 		//name = c.getString(c.getColumnIndex("name"));
				 		TelephonyManager telephonyManager = ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE));
				 		name = telephonyManager.getNetworkOperatorName();
				 		if (!networkName.equals(name)) {
				 					
				 			Intent intent = new Intent(WriteCache.CACHE_MOBILENETWORK);
				 			intent.putExtra(WriteCache.MOBILENETWORK_NAME,name);
				 			intent.putExtra(WriteCache.MOBILENETWORK_NETWORK,telephonyManager.getNetworkOperator());
				 			intent.putExtra(WriteCache.MOBILENETWORK_TIME,MinerData.df.format(new Date()));
				 			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

							networkName = name;
							networkBroadcast();	
				 		}
				 		startScan();
				 		//Log.i("MinerService","CONNECTED MOBILE: "+name);
				 		break;
				 	default:
				 		//Log.i("MinerService",netInfo.getTypeName());
				 		break;
				 }	 
			 }
			 else {
				 scanning = false;
				 updating = false;
			 }	 
		}
		else {
			 scanning = false;
			 updating = false;
			 networkName = "null";
		}
		
	}
	
	private void startScan() {
		if (!scanning) {
			scanning = true;
			scanHandle.post(mineWorker);
		}
	}
	
	private void startUpdating() {
		if (!updating) {
			new CkanUrlGetter(context).getUrl();
			new CkanUidGetter(context).getUid();
			updating = true;
			updateHandle.post(ckanWorker);
		}
	}
	
	private void cellBroadcast() {
		//Log.i("MinerService","Cellbroadcast");
		MinerLocation cell = null;
		String cellText = "No Cell";
		Boolean cellValid = false;
		Intent intent = new Intent("com.odo.kcl.mobileminer.cellupdate");
			
		switch (cells.size()) {
			case 0: cellValid = false; break;
			case 1: cell = cells.get(0); break;
			default:
				cell = cells.get(0);
				for (MinerLocation location: cells.subList(1,cells.size())) cell = cell.compare(location);
		}
		
		if (cell != null) {
			cellValid = cell.isValid();
			cellText = cell.dump();
			intent.putExtra("mcc", cell.getMcc());
			intent.putExtra("mnc", cell.getMnc());
			intent.putExtra("lac", cell.getLac());
			intent.putExtra("id", cell.getId());
		}
		
		intent.putExtra("celltext", cellText);
		intent.putExtra("cellvalid", cellValid);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
	
    private void networkBroadcast() {
    	Intent intent = new Intent("com.odo.kcl.mobileminer.networkupdate");
    	if (!networkName.equals("null")) {
    		intent.putExtra("networktext",networkName);
    	}
    	else {
    		intent.putExtra("networktext","None");
    	}
    	LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}

