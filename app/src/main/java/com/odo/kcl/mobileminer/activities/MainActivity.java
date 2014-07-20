// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package com.odo.kcl.mobileminer.activities;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.odo.kcl.mobileminer.R;
import com.odo.kcl.mobileminer.SocketAdapter;
import com.odo.kcl.mobileminer.cell.CellData;
import com.odo.kcl.mobileminer.cell.CellLocationGetter;
import com.odo.kcl.mobileminer.miner.MinerData;
import com.odo.kcl.mobileminer.miner.MinerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import android.util.Log;

public class MainActivity extends Activity {
    private boolean miningButtonState = false, cellValid = false;
    private ExpandableListView socketView;
    private SocketAdapter socketAdapter;
    private List<String> processHeader = new ArrayList<String>();
    private HashMap<String, List<String>> socketChild = new HashMap<>();
    private TextView networkText;
    private Button cellButton;
    private String Mcc, Mnc, Lac, Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.socketView = (ExpandableListView) findViewById(R.id.socketView);
        this.cellButton = (Button) findViewById(R.id.cellLocation);
        this.cellButton.setEnabled(false);
        this.networkText = (TextView) findViewById(R.id.networkName);

        this.socketAdapter = new SocketAdapter(this, this.processHeader, this.socketChild, null);
        this.socketView.setAdapter(this.socketAdapter);

        LocalBroadcastManager.getInstance(this).registerReceiver(this.socketReceiver, new IntentFilter("com.odo.kcl.mobileminer.socketupdate"));
        LocalBroadcastManager.getInstance(this).registerReceiver(this.cellReceiver, new IntentFilter("com.odo.kcl.mobileminer.cellupdate"));
        LocalBroadcastManager.getInstance(this).registerReceiver(this.networkReceiver, new IntentFilter("com.odo.kcl.mobileminer.networkupdate"));

        MinerData minerHelper = new MinerData(this);
        minerHelper.getReadableDatabase();
        minerHelper.close();

        CellData cellHelper = new CellData(this);
        cellHelper.init();
        cellHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                this.startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean("miningButtonState", this.miningButtonState);
        savedInstanceState.putString("cellText", this.cellButton.getText().toString());
        savedInstanceState.putStringArrayList("processHeader", (ArrayList<String>) this.processHeader);
        for (String key : this.processHeader) {
            savedInstanceState.putStringArrayList(key, (ArrayList<String>) this.socketChild.get(key));
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        this.miningButtonState = savedInstanceState.getBoolean("miningButtonState", false);

        if (savedInstanceState.getString("cellText") != null) {
            this.cellButton.setText(savedInstanceState.getString("cellText"));
        }

        if (savedInstanceState.getStringArrayList("processHeader") != null) {
            this.processHeader = savedInstanceState.getStringArrayList("processHeader");
            for (String key : this.processHeader) {
                socketChild.put(key, savedInstanceState.getStringArrayList(key));
            }
        }

        enableMiningButton(miningButtonState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (this.miningButtonState = miningActive()) {
            getApplicationContext().sendBroadcast(new Intent("com.odo.kcl.mobileminer.updatequery"));
        }
        enableMiningButton(this.miningButtonState);
    }

    public void startMining(View buttonView) {
        if (!isAccessibilityEnabled()) {
            accessibilityNag();
        }
        if (miningActive()) {
            this.getApplicationContext().sendBroadcast(new Intent("com.odo.kcl.mobileminer.updatequery"));
        }
        enableMiningButton(true);
    }

    public void stopMining(View buttonView) {
        //miningIntent = new Intent(this, MinerService.class);
        //stopService(miningIntent);
        this.getApplicationContext().sendBroadcast(new Intent("com.odo.kcl.mobileminer.stopmining"));
        enableMiningButton(false);
    }

    public void launchData(View buttonView) {
        this.startActivity(new Intent(this, DataActivity.class));
    }

    public void cellMap(View buttonView) {
        cellButton.setEnabled(false);

        String[] location = null;
        try {
            location = new CellLocationGetter(this).getCell(Mcc, Mnc, Lac, Id);
        } catch (Exception e) {
        }

        cellButton.setEnabled(true);

        if (location != null) {
            Intent mapIntent = new Intent(this, MapActivity.class);
            mapIntent.putExtra("lat", location[0]);
            mapIntent.putExtra("long", location[1]);
            mapIntent.putExtra("zoom", "15");
            this.startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Can't find the tower position...", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkMining() {
        // Should we be mining?
        if (this.miningButtonState) {
            // Are we not mining?
            if (!miningActive()) {
                this.startService(new Intent(this, MinerService.class));
            }
        }
    }

    // http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-in-android
    private boolean miningActive() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MinerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void enableMiningButton(boolean mining) {
        this.miningButtonState = mining;

        Button startButton = (Button) this.findViewById(R.id.startButton);
        Button stopButton = (Button) this.findViewById(R.id.stopButton);
        startButton.setEnabled(!mining);
        stopButton.setEnabled(mining);

        checkMining();
    }

    private BroadcastReceiver socketReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HashMap<String, List<String>> socketMap = (HashMap<String, List<String>>) intent.getSerializableExtra("socketmap");
            processHeader = new ArrayList<String>();
            socketChild = new HashMap<String, List<String>>();
            ArrayList<Boolean> processStatus = (ArrayList<Boolean>) intent.getSerializableExtra("processstatus");
            for (String key : socketMap.keySet()) {
                processHeader.add(key);
                socketChild.put(key, socketMap.get(key));
            }
            socketAdapter = new SocketAdapter(context, processHeader, socketChild, processStatus);
            socketView.setAdapter(socketAdapter);
        }
    };

    private BroadcastReceiver cellReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            cellButton.setText((CharSequence) intent.getSerializableExtra("celltext"));
            cellValid = (Boolean) intent.getSerializableExtra("cellvalid");
            cellButton.setEnabled(cellValid);
            if (cellValid) {
                Mcc = (String) intent.getSerializableExtra("mcc");
                Mnc = (String) intent.getSerializableExtra("mnc");
                Lac = (String) intent.getSerializableExtra("lac");
                Id = (String) intent.getSerializableExtra("id");
            }
        }
    };

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            networkText.setText("Network: " + intent.getSerializableExtra("networktext"));
        }
    };

    private boolean isAccessibilityEnabled() {
        // http://stackoverflow.com/questions/5081145/android-how-do-you-check-if-a-particular-accessibilityservice-is-enabled
        if (Build.VERSION.SDK_INT >= 17) {
            AccessibilityManager am = (AccessibilityManager) this.getSystemService(Context.ACCESSIBILITY_SERVICE);
            List<AccessibilityServiceInfo> runningServices = am.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
            for (AccessibilityServiceInfo service : runningServices) {
                if ("com.odo.kcl.mobileminer/.NotificationService".equals(service.getId()))
                    return true;
            }
        }
        return false;
    }

    private void accessibilityNag() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle("Store Notifications?");
        myAlertDialog.setMessage("If MobileMiner is to archive notifications from net-enabled apps, "
                + "you need to authorize it as an Accessibility Service. Do this now?");
        myAlertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    }
                }
        );

        myAlertDialog.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                // do something when the Cancel button is clicked
            }
        });

        myAlertDialog.show();
    }

}
