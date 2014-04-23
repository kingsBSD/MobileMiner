// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package com.odo.kcl.mobileminer;

import java.util.ArrayList;
import java.util.List;

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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class MinerService extends Service {

	private ProcSocketSet socketSet;
	private IntentFilter filter;
	private Handler scanHandle;
	private boolean scanning;
	private Runnable mineWorker;
	private Context context;
	private String networkName;
	private ArrayList<MinerLocation> cells;
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
		    	if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
		    		connectivityChanged();
		    		//Log.i("MinerService","CONNECTIVITY_CHANGE");
		    	}
   
		    	if (action.equals("com.odo.kcl.mobileminer.updatequery")) {
		        	socketSet.broadcast();
		        	cellBroadcast();
		        	networkBroadcast();
		        	//Log.i("MinerService","RECEIVED SOCKET QUERY");
		      }		      	      		      
		   }
		};
		
	private final PhoneStateListener phoneListener = new PhoneStateListener() {
		@Override
		public void onDataActivity(int direction) {
			if (direction != TelephonyManager.DATA_ACTIVITY_NONE || direction != TelephonyManager.DATA_ACTIVITY_DORMANT) {
				//Log.i("MinerService","DATA_ACTIVE");
				startScan();
			}
			else {
				scanning = false;
			}
		}

		@Override
		public void onCellInfoChanged (List<CellInfo> cellInfo) {
			//Log.i("MinerService","CELL_INFO_CHANGED");
			cells = new ArrayList<MinerLocation>();
			if (cellInfo != null) {
				for (CellInfo info: cellInfo) {
					cells.add(new MinerLocation(info));
				}
			}
			cellBroadcast();
		}

		@Override
		public void onCellLocationChanged (CellLocation location) {
			cells = new ArrayList<MinerLocation>();
			if (location != null) cells.add(new MinerLocation(location,context));
			cellBroadcast();
			//Log.i("MinerService","CELL_LOCATION_CHANGED");
		}	
	};
	
	@Override
	public void onCreate() {
		networkName = "None";
		context = this;
		cells = new ArrayList<MinerLocation>();
		socketSet = new ProcSocketSet(this);
		scanHandle = new Handler();
		
		filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		filter.addAction("com.odo.kcl.mobileminer.updatequery");
		registerReceiver(receiver, filter);
	 
		mineWorker = new Runnable() {
			@Override
			public void run() {
				try {
					socketSet.scan();
				}
				catch (Exception e) {}
				finally {
					if (scanning) {
						scanHandle.postDelayed(this, 500);
					}
					else {
						socketSet.close();
					}
				}
			}	 
		 };				 
	 }
	 
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("MinerService","started mining");
		int phoneFlags;
		 
		if (Build.VERSION.SDK_INT >= 17) {
			phoneFlags = PhoneStateListener.LISTEN_DATA_ACTIVITY|PhoneStateListener.LISTEN_CELL_INFO;
		}
		else {
			phoneFlags = PhoneStateListener.LISTEN_DATA_ACTIVITY|PhoneStateListener.LISTEN_CELL_LOCATION;
		}
		 
		((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE)).listen(phoneListener,phoneFlags);
		Toast.makeText(this, "Started mining...", Toast.LENGTH_SHORT).show();  
		return START_STICKY;
	}
	 
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		Log.i("MinerService","stopped mining");
		unregisterReceiver(receiver);
		scanning = false;
	    Toast.makeText(this, "Stopped mining...", Toast.LENGTH_SHORT).show();
	}
	
	private void connectivityChanged() {
		String name = "None";
		ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = manager.getActiveNetworkInfo();
		if (netInfo != null) {
			if (netInfo.getState() ==  NetworkInfo.State.CONNECTED ) {
				switch (netInfo.getType()) {
					case ConnectivityManager.TYPE_WIFI:
						WifiManager wifiMgr = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
				 		WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
				 		name = wifiInfo.getSSID();
				 		startScan();
				 		//Log.i("MinerService","CONNECTED: WIFI");
				 		break;
				 	case ConnectivityManager.TYPE_MOBILE:
				 		// https://code.google.com/p/android/issues/detail?id=24227
				 		//String name; Cursor c;
				 		//c = this.getContentResolver().query(Uri.parse("content://telephony/carriers/preferapn"), null, null, null, null);
				 		//name = c.getString(c.getColumnIndex("name"));
				 		TelephonyManager telephonyManager = ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE));
				 		name = telephonyManager.getNetworkOperatorName();
				 		startScan();
				 		Log.i("MinerService","CONNECTED MOBILE: "+name);
				 		break;
				 	default:
				 		Log.i("MinerService",netInfo.getTypeName());
				 		break;
				 }	 
			 }
			 else {
				 scanning = false;
			 }	 
		 }
		 else {
			 scanning = false;
		 }	 
		networkName = name;
		networkBroadcast(); 
	}
	
	private void startScan() {
		if (!scanning) {
			scanning = true;
			scanHandle.post(mineWorker);
		}
	}
	
	private void cellBroadcast() {
		String cellText = "None";
		Intent intent = new Intent("com.odo.kcl.mobileminer.cellupdate");
			
		switch (cells.size()) {
			case 0: cellText = "None"; break;
			case 1: cellText = cells.get(0).dump(); break;
			default:
				MinerLocation cell = cells.get(0);
				for (MinerLocation location: cells.subList(1,cells.size())) cell = cell.compare(location);
				cellText = cell.dump();
		}
		intent.putExtra("celltext", cellText);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
	
    private void networkBroadcast() {
    	Intent intent = new Intent("com.odo.kcl.mobileminer.networkupdate");
    	intent.putExtra("networktext",networkName);
    	LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}




