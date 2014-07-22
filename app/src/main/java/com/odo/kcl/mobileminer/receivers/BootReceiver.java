package com.odo.kcl.mobileminer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.odo.kcl.mobileminer.activities.SettingsActivity;
import com.odo.kcl.mobileminer.miner.MinerService;

/**
 * Created by notrodash on 20/07/2014.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") &&
                context.getSharedPreferences(SettingsActivity.SHARED_PREFERENCE_IDENTIFIER, context.MODE_PRIVATE).getBoolean(SettingsActivity.SHARED_PREFERENCE_MINE_ON_BOOT_IDENTIFIER, false)) {
            context.startService(new Intent(context, MinerService.class));
        }
    }
}
