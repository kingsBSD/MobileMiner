// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package uk.ac.kcl.odo.mobileminer.miner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import uk.ac.kcl.odo.mobileminer.data.MinerData;
import uk.ac.kcl.odo.mobileminer.data.WriteCache;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
//import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;
//import android.util.Log;
import android.support.v4.content.LocalBroadcastManager;

public class TrafficWatcher {
	Context context;
	ArrayList<Integer> uids;
	ConcurrentHashMap<Integer,String> namesByUid;
	ConcurrentHashMap<Integer,Long> trafficTxByUid;
	ConcurrentHashMap<Integer,Long> trafficRxByUid;
	
	ConcurrentHashMap<Integer,Date> txStartByUid;
	ConcurrentHashMap<Integer,Date> rxStartByUid;
	
	ConcurrentHashMap<Integer,Long> txBytesByUid;
	ConcurrentHashMap<Integer,Long> rxBytesByUid;
	
	public TrafficWatcher(Context ctx) {
		
		uids = new ArrayList<Integer>();
		namesByUid = new ConcurrentHashMap<Integer,String>();
		trafficTxByUid = new ConcurrentHashMap<Integer,Long>();
		trafficRxByUid = new ConcurrentHashMap<Integer,Long>();
		txStartByUid = new ConcurrentHashMap<Integer,Date>();
		rxStartByUid = new ConcurrentHashMap<Integer,Date>();
		txBytesByUid = new ConcurrentHashMap<Integer,Long>();
		rxBytesByUid = new ConcurrentHashMap<Integer,Long>();
		
		PackageManager pm = ctx.getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		String[] permissions;
		
		long txBytes,rxBytes;
		
		for (ApplicationInfo appInfo : packages) {	
			try {
				permissions = pm.getPackageInfo(appInfo.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions;
				
				if (permissions != null) {
					for (String permission: permissions) {
						if (permission.equals("android.permission.INTERNET")) {
							
							txBytes = TrafficStats.getUidTxBytes(appInfo.uid);
							rxBytes = TrafficStats.getUidRxBytes(appInfo.uid);
									
							if (txBytes != -1 && rxBytes != -1) {
								uids.add(appInfo.uid);
								namesByUid.put(appInfo.uid,appInfo.processName);
								trafficTxByUid.put(appInfo.uid, txBytes);
								trafficRxByUid.put(appInfo.uid, rxBytes);
								//Log.i("TrafficWatcher",appInfo.processName);
								//Log.i("TrafficWatcher",new Long(rxBytes).toString());
								
							}
							
							break;
						}	
					}
				}
				
			} 
			catch (NameNotFoundException e) {

			}
		}				
		
	}
	
	private void close(boolean tx, Integer uid, Date stop) {
		long delta,bytes;
		Date start;

		if (tx) {
			bytes = txBytesByUid.get(uid);
			delta = trafficTxByUid.get(uid) - bytes;
			start = txStartByUid.get(uid);
			txStartByUid.remove(uid);
		}
		else {
			bytes = rxBytesByUid.get(uid);
			delta = trafficRxByUid.get(uid) - bytes;
			start = rxStartByUid.get(uid);
			rxStartByUid.remove(uid);
		}
		
		//Log.i("TrafficWatcher","Writing...");
		
		Intent intent = new Intent(WriteCache.CACHE_TRAFFIC);
		intent.putExtra(WriteCache.TRAFFIC_PACKAGE,namesByUid.get(uid));
		intent.putExtra(WriteCache.TRAFFIC_START,MinerData.df.format(start));
		intent.putExtra(WriteCache.TRAFFIC_STOP,MinerData.df.format(stop));
		intent.putExtra(WriteCache.TRAFFIC_DAY,MinerData.dayGetter.format(start));
		intent.putExtra(WriteCache.TRAFFIC_TX,tx);
		intent.putExtra(WriteCache.TRAFFIC_BYTES,delta);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		
	}
	
	private String renderBytes(long bytes) {
		if (bytes < 1024) {
			return Long.toString(bytes)+"B";
		}
		if (bytes >= 1024 && bytes < 1048576) {
			return Long.toString(bytes/1024)+"KB";
		}
		return Long.toString(bytes/1048576)+"MB";
	}
	
	public void scan() {
		ArrayList<Integer> discoveredTx = new ArrayList<Integer>();
		ArrayList<Integer> discoveredRx = new ArrayList<Integer>();
		ArrayList<Integer> closedTx = new ArrayList<Integer>();
		ArrayList<Integer> closedRx = new ArrayList<Integer>();
		Long bytes;
		Date closeTime; 
		
		HashMap<String,String> txByProc = new HashMap<String,String>();
		HashMap<String,String> rxByProc = new HashMap<String,String>();

		//Log.i("TrafficWatcher","Tick...");
		
		for (Integer uid: uids) {
			bytes = TrafficStats.getUidTxBytes(uid);
			if (bytes > trafficTxByUid.get(uid)) {
				//Log.i("TrafficWatcher","tx: "+namesByUid.get(uid));
				if (!txStartByUid.containsKey(uid)) {
					//Log.i("TrafficWatcher","New TX socket: "+namesByUid.get(uid));
					txStartByUid.put(uid, new Date());
					txBytesByUid.put(uid, trafficTxByUid.get(uid));
				}
				trafficTxByUid.put(uid, bytes);
				discoveredTx.add(uid);
				txByProc.put(namesByUid.get(uid),renderBytes(bytes - txBytesByUid.get(uid)));
			}
			
			bytes = TrafficStats.getUidRxBytes(uid);
			if (bytes > trafficRxByUid.get(uid)) {
				//Log.i("TrafficWatcher","rx: "+namesByUid.get(uid));
				if (!rxStartByUid.containsKey(uid)) {
					//Log.i("TrafficWatcher","New RX socket: "+namesByUid.get(uid));
					rxStartByUid.put(uid, new Date());
					rxBytesByUid.put(uid, trafficRxByUid.get(uid));
				}
				trafficRxByUid.put(uid, bytes);
				discoveredRx.add(uid);
				rxByProc.put(namesByUid.get(uid),renderBytes(bytes - rxBytesByUid.get(uid)));		
			}	
		}
		
		for (Integer uid: txStartByUid.keySet()) {
			if (!discoveredTx.contains(uid)) {
				//Log.i("TrafficWatcher","Closed TX socket: "+namesByUid.get(uid));
				closedTx.add(uid);
			}
		}

		for (Integer uid: rxStartByUid.keySet()) {
			if (!discoveredRx.contains(uid)) {
				//Log.i("TrafficWatcher","Closed RX socket: "+namesByUid.get(uid));
				closedRx.add(uid);
			}
		}
		
		if (closedTx.size() + closedRx.size() > 0) {
			//Log.i("TrafficWatcher","Closing...");
			closeTime = new Date();
			for (Integer uid: closedTx) {
				close(true,uid,closeTime);
			}
			for (Integer uid: closedRx) {
				close(false,uid,closeTime);
			}
		}
		
		if (txByProc.size() + rxByProc.size() > 0) {
			Intent intent = new Intent(MinerService.MINER_TRAFFIC_UPDATE_INTENT);
			intent.putExtra(MinerService.MINER_TRAFFIC_TXBYTES, txByProc);
			intent.putExtra(MinerService.MINER_TRAFFIC_RXBYTES, rxByProc);
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);	
		}
	}
	
	public void closeAll() {
		Date closeTime = new Date();
		for (Integer uid: txStartByUid.keySet()) {
			close(true,uid,closeTime);
		}
		for (Integer uid: rxStartByUid.keySet()) {
			close(false,uid,closeTime);
		}
		
	}
	
}
