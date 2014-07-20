// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package com.odo.kcl.mobileminer;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.odo.kcl.mobileminer.miner.MinerData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationService extends AccessibilityService {

    @Override
    public void onServiceConnected() {
        // http://stackoverflow.com/questions/14540394/listen-to-incoming-whatsapp-messages-notifications
        // http://developer.android.com/training/accessibility/service.html#create
        // http://stackoverflow.com/questions/7937794/how-to-get-installed-applications-permissions
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<String> netEnabledPackageNames = new ArrayList<String>();

        for (ApplicationInfo appInfo : packages) {
            String[] permissions;
            try {
                permissions = pm.getPackageInfo(appInfo.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions;

                if (permissions != null) {
                    for (String permission : permissions) {
                        if (permission.equals("android.permission.INTERNET")) {
                            netEnabledPackageNames.add(appInfo.packageName);
                            break;
                        }
                    }
                }

            } catch (NameNotFoundException e) {

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
        MinerData helper = new MinerData(this);
        helper.putNotification(helper.getWritableDatabase(), (String) event.getPackageName(), new Date());
        // None of our business...
//        helper.putNotification(helper.getWritableDatabase(), (String) event.getPackageName(),
//            TextUtils.join(" ", event.getText()),new Date());
        helper.close();
    }

    @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub

    }

}
