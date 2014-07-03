// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package uk.ac.kcl.odo.mobileminer.miner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import uk.ac.kcl.odo.mobileminer.data.MinerData;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;
import android.util.Log;

public class TrafficWatcher {
	MinerData helper;
	ArrayList<Integer> uids;
	ConcurrentHashMap<Integer,String> namesByUid;
	ConcurrentHashMap<Integer,Long> trafficTxByUid;
	ConcurrentHashMap<Integer,Long> trafficRxByUid;
	
	ConcurrentHashMap<Integer,Date> txStartByUid;
	ConcurrentHashMap<Integer,Date> rxStartByUid;
	
	ConcurrentHashMap<Integer,Long> txBytesByUid;
	ConcurrentHashMap<Integer,Long> rxBytesByUid;
	
	public TrafficWatcher(Context ctx) {
		helper = new MinerData(ctx);
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
		
		for (ApplicationInfo appInfo : packages) {	
			try {
				permissions = pm.getPackageInfo(appInfo.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions;
				
				if (permissions != null) {
					for (String permission: permissions) {
						if (permission.equals("android.permission.INTERNET")) {
							uids.add(appInfo.uid);
							namesByUid.put(appInfo.uid,appInfo.processName);
							trafficTxByUid.put(appInfo.uid, TrafficStats.getUidTxBytes(appInfo.uid));
							trafficRxByUid.put(appInfo.uid, TrafficStats.getUidRxBytes(appInfo.uid));
							break;
						}	
					}
				}
				
			} 
			catch (NameNotFoundException e) {

			}
		}				
		
	}
	
	private void close(SQLiteDatabase db, boolean tx, Integer uid) {
		long delta,bytes;
		if (tx) {
			bytes = txBytesByUid.get(uid);
			delta = bytes - trafficTxByUid.get(uid);
			trafficTxByUid.put(uid, bytes);
			txStartByUid.remove(uid);
		}
		else {
			bytes = rxBytesByUid.get(uid);
			delta = bytes - trafficRxByUid.get(uid);
			trafficRxByUid.put(uid, bytes);
			rxStartByUid.remove(uid);
		}
		
	}
	
	public void scan() {
		ArrayList<Integer> discoveredTx = new ArrayList<Integer>();
		ArrayList<Integer> discoveredRx = new ArrayList<Integer>();
		ArrayList<Integer> closedTx = new ArrayList<Integer>();
		ArrayList<Integer> closedRx = new ArrayList<Integer>();
		Long bytes;
		
		for (Integer uid: uids) {
			bytes = trafficTxByUid.get(uid);
			if (bytes > trafficTxByUid.get(uid)) {
				txBytesByUid.put(uid, bytes);
				if (txStartByUid.get(uid) == null) {
					txStartByUid.put(uid, new Date());
				}
				discoveredTx.add(uid);
			}
			bytes = TrafficStats.getUidRxBytes(uid);
			if (bytes > trafficRxByUid.get(uid)) {
				rxBytesByUid.put(uid, bytes);
				if (rxStartByUid.get(uid) == null) {
					rxStartByUid.put(uid, new Date());
				}
				discoveredRx.add(uid);
			}	
		}
		
		for (Integer uid: txStartByUid.keySet()) {
			if (!discoveredTx.contains(uid)) {
				closedTx.add(uid);
			}
		}

		for (Integer uid: rxStartByUid.keySet()) {
			if (!discoveredRx.contains(uid)) {
				closedRx.add(uid);
			}
		}
		
		if (closedTx.size() + closedRx.size() > 0) {
			SQLiteDatabase db = helper.getWritableDatabase();
			for (Integer uid: closedTx) {
				close(db,true,uid);
			}
			for (Integer uid: closedRx) {
				close(db,false,uid);
			}
			db.close();
		}
	}
	
	public void closeAll() {
		SQLiteDatabase db = helper.getWritableDatabase();
		for (Integer uid: txStartByUid.keySet()) {
			close(db,true,uid);
		}
		for (Integer uid: rxStartByUid.keySet()) {
			close(db,false,uid);
		}
		db.close();
		
	}
	
}
