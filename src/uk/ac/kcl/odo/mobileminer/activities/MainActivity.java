// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package uk.ac.kcl.odo.mobileminer.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.kcl.odo.mobileminer.activities.adapters.SocketAdapter;
import uk.ac.kcl.odo.mobileminer.cells.CellLocationGetter;
import uk.ac.kcl.odo.mobileminer.data.CellData;
import uk.ac.kcl.odo.mobileminer.data.MinerData;
import uk.ac.kcl.odo.mobileminer.miner.MinerService;

import uk.ac.kcl.odo.mobileminer.R;

import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

//import android.util.Log;

public class MainActivity extends Activity {
	Boolean miningButtonState,cellValid;
	Intent miningIntent;
	ExpandableListView socketView;
	SocketAdapter socketAdapter;
	List<String> processHeader;
	HashMap<String, List<String>> socketChild;
	TextView networkText;
	Button cellButton;
	String Mcc,Mnc,Lac,Id;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	//Log.i("MobileMiner","CREATING");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        miningButtonState = false;
        cellValid = false;
        processHeader = new ArrayList<String>();
    	socketChild = new HashMap<String, List<String>>();
        socketView = (ExpandableListView) findViewById(R.id.socketView);
        socketAdapter = new SocketAdapter(this,processHeader,socketChild,null);
        socketView.setAdapter(socketAdapter);
        cellButton = (Button) findViewById(R.id.cellLocation);
        cellButton.setEnabled(false);
        networkText = (TextView) findViewById(R.id.networkName);
        
        LocalBroadcastManager.getInstance(this).registerReceiver(
                socketReceiver, new IntentFilter("com.odo.kcl.mobileminer.socketupdate"));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(
                cellReceiver, new IntentFilter("com.odo.kcl.mobileminer.cellupdate"));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(
                networkReceiver, new IntentFilter("com.odo.kcl.mobileminer.networkupdate"));
        
        MinerData minerHelper = new MinerData(this);
        minerHelper.getReadableDatabase();
        minerHelper.close();
        CellData cellHelper = new CellData(this);
        cellHelper.init();
        cellHelper.close();
        
        //new UidGetter(this).getUid();
         
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	//Log.i("MobileMiner","SAVING");
    	super.onSaveInstanceState(savedInstanceState);
    	savedInstanceState.putBoolean("miningButtonState", miningButtonState);
    	savedInstanceState.putString("cellText",cellButton.getText().toString());
    	savedInstanceState.putStringArrayList("processHeader",(ArrayList<String>)processHeader);
    	for (String key: processHeader) savedInstanceState.putStringArrayList(key,(ArrayList<String>)socketChild.get(key));
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	//Log.i("MobileMiner","RESTORING");
    	super.onRestoreInstanceState(savedInstanceState);
    	miningButtonState = savedInstanceState.getBoolean("miningButtonState", false);
    	if (savedInstanceState.getString("cellText") != null) cellButton.setText(savedInstanceState.getString("cellText"));
    	if (savedInstanceState.getStringArrayList("processHeader") != null) {
    		processHeader = savedInstanceState.getStringArrayList("processHeader");
    		for (String key: processHeader) socketChild.put(key,savedInstanceState.getStringArrayList(key));
    	}
    	enableMiningButton(miningButtonState);
    }
        
    @Override public void onRestart() {
    	//Log.i("MobileMiner","RESTARTING");
    	super.onRestart();
    }
    
    @Override public void onResume() {
    	//Log.i("MobileMiner","RESUMING");
    	super.onResume();
    	if (miningActive()) {
    		getApplicationContext().sendBroadcast(new Intent("com.odo.kcl.mobileminer.updatequery"));
    		miningButtonState = true;
    	}
    	else {
    		miningButtonState = false;
    	}
    	enableMiningButton(miningButtonState);
    }
    
    public void startMining(View buttonView) {
    	if (!isAccessibilityEnabled()) accessibilityNag();
    	if (miningActive()) getApplicationContext().sendBroadcast(new Intent("com.odo.kcl.mobileminer.updatequery"));
    	enableMiningButton(true);
    }
    
    public void stopMining(View buttonView) {
    	//miningIntent = new Intent(this, MinerService.class);
    	//stopService(miningIntent);
    	getApplicationContext().sendBroadcast(new Intent("com.odo.kcl.mobileminer.stopmining"));
    	enableMiningButton(false);
    } 
    
    public void launchData(View buttonView) {
    	startActivity(new Intent(this, DataActivity.class));
    }
    
    public void cellMap(View buttonView) {
    	String[] location;
    	cellButton.setEnabled(false);
    	try {
    		location = new CellLocationGetter(this).getCell(Mcc, Mnc, Lac, Id);
    	}
    	catch (Exception e) {
    		location = null;
    	}
    	cellButton.setEnabled(true);
    	if (location != null) {
    		Intent mapIntent = new Intent(this, MapActivity.class);
    		mapIntent.putExtra("lat", location[0]);
    		mapIntent.putExtra("long", location[1]);
    		mapIntent.putExtra("zoom", "15");
    		startActivity(mapIntent);
    	}
    	else {
    		Toast.makeText(this, "Can't find the tower position...", Toast.LENGTH_SHORT).show();
    	}
    }
    
    private void checkMining() {
    	// Should we be mining?
    	if (miningButtonState) {
    		// Are we not mining?
    		if (!miningActive()) {
    	    	miningIntent = new Intent(this, MinerService.class);
    	    	startService(miningIntent);
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
    	Button startButton = (Button)this.findViewById(R.id.startButton);
    	Button stopButton = (Button)this.findViewById(R.id.stopButton);
    	startButton.setEnabled(!mining);
    	stopButton.setEnabled(mining);
    	miningButtonState = mining;
    	checkMining();
    }

    private BroadcastReceiver socketReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	HashMap<String, List<String>> socketMap = (HashMap<String, List<String>>) intent.getSerializableExtra("socketmap");
        	processHeader = new ArrayList<String>();
        	socketChild = new HashMap<String, List<String>>();
        	ArrayList<Boolean> processStatus = (ArrayList<Boolean>)  intent.getSerializableExtra("processstatus");
        	for (String key: socketMap.keySet()) {
        		processHeader.add(key);
        		socketChild.put(key, socketMap.get(key));
        	}
        	socketAdapter = new SocketAdapter(context,processHeader,socketChild,processStatus);
            socketView.setAdapter(socketAdapter);
        }
    }; 
        
    private BroadcastReceiver cellReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			cellButton.setText((CharSequence) (intent.getSerializableExtra("celltext")));
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
			networkText.setText((CharSequence) ("Network: "+intent.getSerializableExtra("networktext")));	
		}
    };
    
    @SuppressLint("NewApi")
	public boolean isAccessibilityEnabled() {
    	// http://stackoverflow.com/questions/5081145/android-how-do-you-check-if-a-particular-accessibilityservice-is-enabled
    	if (Build.VERSION.SDK_INT >= 17) {
    		AccessibilityManager am = (AccessibilityManager) this.getSystemService(Context.ACCESSIBILITY_SERVICE);
    		List<AccessibilityServiceInfo> runningServices = am.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
    		for (AccessibilityServiceInfo service : runningServices) {
    			if ("com.odo.kcl.mobileminer/.NotificationService".equals(service.getId())) return true;
    		}
    	}
    	return false;
    }
    
    private void accessibilityNag() {
    	AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
    	myAlertDialog.setTitle("Store Notifications?"); 
    	myAlertDialog.setMessage("If MobileMiner is to archive notifications from net-enabled apps, "
    		+"you need to authorize it as an Accessibility Service. Do this now?");
    	myAlertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface arg0, int arg1) {
    		startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    	  }});
    	
   	 myAlertDialog.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
	       
   	  public void onClick(DialogInterface arg0, int arg1) {
   	  // do something when the Cancel button is clicked
   	  }});
   	 
   	 myAlertDialog.show();
    	
    }
    

}