// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package uk.ac.kcl.odo.mobileminer.miner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.ac.kcl.odo.mobileminer.data.MinerData;
import uk.ac.kcl.odo.mobileminer.data.WriteCache;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class NotificationService extends AccessibilityService {

	@Override
	public void onServiceConnected() {
		// http://stackoverflow.com/questions/14540394/listen-to-incoming-whatsapp-messages-notifications
		// http://developer.android.com/training/accessibility/service.html#create
		// http://stackoverflow.com/questions/7937794/how-to-get-installed-applications-permissions
		PackageManager pm = getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		ArrayList<String> netEnabledPackageNames = new ArrayList<String>();
		String[] permissions;
				
		for (ApplicationInfo appInfo : packages) {	
			try {
				permissions = pm.getPackageInfo(appInfo.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions;
				
				if (permissions != null) {
					for (String permission: permissions) {
						if (permission.equals("android.permission.INTERNET")) {
							netEnabledPackageNames.add(appInfo.packageName);
							//Log.i("NotificationService",appInfo.packageName);
							break;
						}	
					}
				}
				
			} 
			catch (NameNotFoundException e) {

			}

		}

		AccessibilityServiceInfo info = new AccessibilityServiceInfo();
		info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
		info.packageNames = netEnabledPackageNames.toArray(new String[netEnabledPackageNames.size()]);
		info.feedbackType = AccessibilityServiceInfo.DEFAULT;
	    info.notificationTimeout = 100;
	    this.setServiceInfo(info);
	    
	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		Date rightNow = new Date();
		Intent intent = new Intent(WriteCache.CACHE_NOTIFICATION);
		intent.putExtra(WriteCache.NOTIFICATION_NAME, (String) event.getPackageName());
		intent.putExtra(WriteCache.NOTIFICATION_TIME, MinerData.df.format(rightNow));
		intent.putExtra(WriteCache.NOTIFICATION_DAY, MinerData.dayGetter.format(rightNow));
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);			
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

}