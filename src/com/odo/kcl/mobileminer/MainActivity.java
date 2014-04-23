// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package com.odo.kcl.mobileminer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	Intent miningIntent;
	ExpandableListView socketView;
	SocketAdapter socketAdapter;
	List<String> processHeader;
	HashMap<String, List<String>> socketChild;
	TextView cellText,networkText;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      
        processHeader = new ArrayList<String>();
    	socketChild = new HashMap<String, List<String>>();
    	  
        socketView = (ExpandableListView) findViewById(R.id.socketView);
        socketAdapter = new SocketAdapter(this,processHeader,socketChild);
        socketView.setAdapter(socketAdapter);
        cellText = (TextView) findViewById(R.id.cellLocation);
        networkText = (TextView) findViewById(R.id.networkName);
        
        LocalBroadcastManager.getInstance(this).registerReceiver(
                socketReceiver, new IntentFilter("com.odo.kcl.mobileminer.socketupdate"));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(
                cellReceiver, new IntentFilter("com.odo.kcl.mobileminer.cellupdate"));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(
                networkReceiver, new IntentFilter("com.odo.kcl.mobileminer.networkupdate"));
        
        enableMiningButton(miningActive());  
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	savedInstanceState.putString("cellText",(String)cellText.getText());
    	savedInstanceState.putStringArrayList("processHeader",(ArrayList<String>)processHeader);
    	for (String key: processHeader) savedInstanceState.putStringArrayList(key,(ArrayList<String>)socketChild.get(key));
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	if (savedInstanceState.getString("cellText") != null) cellText.setText(savedInstanceState.getString("cellText"));
    	if (savedInstanceState.getStringArrayList("processHeader") != null) {
    		processHeader = savedInstanceState.getStringArrayList("processHeader");
    		for (String key: processHeader) socketChild.put(key,savedInstanceState.getStringArrayList(key));
    	}
    	miningActive();
    }
    
    public void startMining(View buttonView) {
    	miningIntent = new Intent(this, MinerService.class);
    	startService(miningIntent);
    	enableMiningButton(true);
    }
    
    public void stopMining(View buttonView) {
    	miningIntent = new Intent(this, MinerService.class);
    	stopService(miningIntent);
    	enableMiningButton(false);
    }
    
    // http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-in-android
    private boolean miningActive() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MinerService.class.getName().equals(service.service.getClassName())) {
            	getApplicationContext().sendBroadcast(new Intent("com.odo.kcl.mobileminer.updatequery"));
            	//Log.i("MobileMiner","SENT SOCKET QUERY");
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
    }

    private BroadcastReceiver socketReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	HashMap<String, List<String>> socketMap = (HashMap<String, List<String>>)intent.getSerializableExtra("socketmap");
        	processHeader = new ArrayList<String>();
        	socketChild = new HashMap<String, List<String>>();
        	for (String key: socketMap.keySet()) {
        		processHeader.add(key);
        		socketChild.put(key, socketMap.get(key));
        	}
        	socketAdapter = new SocketAdapter(context,processHeader,socketChild);
            socketView.setAdapter(socketAdapter);
        }
    }; 
        
    private BroadcastReceiver cellReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			cellText.setText((CharSequence) ("Cell ID: "+intent.getSerializableExtra("celltext")));
		}
    };
    
    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			networkText.setText((CharSequence) ("Network: "+intent.getSerializableExtra("networktext")));	
		}
    };

}
