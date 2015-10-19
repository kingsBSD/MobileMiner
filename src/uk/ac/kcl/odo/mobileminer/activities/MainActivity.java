// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package uk.ac.kcl.odo.mobileminer.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import uk.ac.kcl.odo.mobileminer.activities.adapters.SocketAdapter;
import uk.ac.kcl.odo.mobileminer.activities.adapters.TrafficAdapter;
import uk.ac.kcl.odo.mobileminer.cells.CellLocationGetter;
import uk.ac.kcl.odo.mobileminer.data.CellData;
import uk.ac.kcl.odo.mobileminer.data.MinerData;
import uk.ac.kcl.odo.mobileminer.miner.MinerService;
import uk.ac.kcl.odo.mobileminer.R;
import edu.mit.media.funf.FunfManager;
import edu.mit.media.openpds.client.PersonalDataStore;
import edu.mit.media.openpds.client.PreferencesWrapper;
import edu.mit.media.openpds.client.RegistryClient;
import edu.mit.media.openpds.client.UserRegistrationTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.content.SharedPreferences;
import android.util.Log;

public class MainActivity extends BaseActivity {
	
	boolean cellValid;
	Intent miningIntent;
	ViewFlipper displayFlipper;
	Integer displayMode;
	ExpandableListView socketView;
	ListView trafficView;
	SocketAdapter socketAdapter;
	TrafficAdapter trafficAdapter;
	List<String> processHeader;
	HashMap<String, List<String>> socketChild;
	HashMap<String,HashMap <String,String>> traffic;
	TextView networkText;
	Button cellButton, socketButton;
	String Mcc,Mnc,Lac,Id;
	PersonalDataStore pds;
	
	private FunfManager mFunfManager;
	
	private ServiceConnection mPipelineConnection = new ServiceConnection() {
		public void onServiceConnected(android.content.ComponentName name, android.os.IBinder service) {			
			mFunfManager = ((FunfManager.LocalBinder) service).getManager();
		};	
		
		public void onServiceDisconnected(android.content.ComponentName name) {
			mFunfManager = null;
		};		
	};
	
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cellValid = false;
        processHeader = new ArrayList<String>();
    	socketChild = new HashMap<String, List<String>>();
    	displayFlipper = (ViewFlipper) findViewById(R.id.mainFlipper);
    	displayMode = 0;
        socketView = (ExpandableListView) findViewById(R.id.socketView);
        socketAdapter = new SocketAdapter(this,processHeader,socketChild,null);
        socketView.setAdapter(socketAdapter);
        cellButton = (Button) findViewById(R.id.cellLocation);
        cellButton.setEnabled(false);
        socketButton = (Button) findViewById(R.id.socketButton);
        networkText = (TextView) findViewById(R.id.networkName);
        trafficView = (ListView) findViewById(R.id.trafficView);
        traffic = new HashMap<String,HashMap <String,String>>();
                
        LocalBroadcastManager.getInstance(this).registerReceiver(
                socketReceiver, new IntentFilter(MinerService.MINER_SOCKET_UPDATE_INTENT));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(
                cellReceiver, new IntentFilter(MinerService.MINER_CELL_UPDATE_INTENT));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(
                networkReceiver, new IntentFilter(MinerService.MINER_NETWORK_UPDATE_INTENT));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(
                trafficReceiver, new IntentFilter(MinerService.MINER_TRAFFIC_UPDATE_INTENT));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(
                minerReceiver, new IntentFilter(MinerService.STARTED_MINING_INTENT));
                
        MinerData minerHelper = new MinerData(this);
        minerHelper.getReadableDatabase();
        minerHelper.close();
        CellData cellHelper = new CellData(this);
        cellHelper.init();
        cellHelper.close();
        
        //bindService(new Intent(this, FunfManager.class), mPipelineConnection, BIND_AUTO_CREATE);
         
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	//Log.i("MobileMiner","SAVING");
    	super.onSaveInstanceState(savedInstanceState);
    	savedInstanceState.putString("cellText",cellButton.getText().toString());
    	savedInstanceState.putStringArrayList("processHeader",(ArrayList<String>)processHeader);
    	savedInstanceState.putInt("displayMode",displayMode);
    	for (String key: processHeader) savedInstanceState.putStringArrayList(key,(ArrayList<String>)socketChild.get(key));
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	//Log.i("MobileMiner","RESTORING");
    	super.onRestoreInstanceState(savedInstanceState);
    	if (savedInstanceState.getString("cellText") != null) cellButton.setText(savedInstanceState.getString("cellText"));
    	if (savedInstanceState.getStringArrayList("processHeader") != null) {
    		processHeader = savedInstanceState.getStringArrayList("processHeader");
    		for (String key: processHeader) socketChild.put(key,savedInstanceState.getStringArrayList(key));
    	}
    	displayMode = savedInstanceState.getInt("displayMode");
    }
        
    @Override public void onRestart() {
    	//Log.i("MobileMiner","RESTARTING");
    	super.onRestart();
    }
    
    @Override public void onResume() {
    	//Log.i("MobileMiner","RESUMING");
    	super.onResume();
    	if (miningActive()) {
    		getApplicationContext().sendBroadcast(new Intent(MinerService.MINER_UPDATE_QUERY_INTENT));
    		enableMiningButton(true);
    	}
    	else {
    		enableMiningButton(false);
    	}
    	for (int i=0;i<displayMode;i++) displayFlipper.showNext();
    }
    
    public void startMining(View buttonView) {
    	enableMiningButton(true);
    	if (!isAccessibilityEnabled()) accessibilityNag();
        	
    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    	
        if (sharedPref.getBoolean("mobileminer_use_openpds", false)) {
        	
        	try {
        		pds = new PersonalDataStore(this);
  			//textView.setText("User was previously authenticated");
        	} catch (Exception ex) {
        		startActivity(new Intent(this, PdsRegisterActivity.class));
        	}
        	bindService(new Intent(this, FunfManager.class), mPipelineConnection, BIND_AUTO_CREATE);

        }
        else {
        	if (!miningActive()) {
        		miningIntent = new Intent(this, MinerService.class);
        		startService(miningIntent);	
        		getApplicationContext().sendBroadcast(new Intent(MinerService.MINER_UPDATE_QUERY_INTENT));
        	}
        }	
    }
    
    public void stopMining(View buttonView) {
    	enableMiningButton(false);
    	if (miningActive()) {
    		getApplicationContext().sendBroadcast(new Intent(MinerService.STOP_MINING_INTENT));
    	}
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
    
    public void flipSockets(View buttonView) {
    	displayMode = (displayMode + 1) % 2;
    	displayFlipper.showNext();
    }
    
    // http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-in-android
    private boolean miningActive() {
    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    	String serviceClass;
    	if (sharedPref.getBoolean("mobileminer_use_openpds", false)) {
    		serviceClass = FunfManager.class.getName();
    	}
    	else {
    		serviceClass = MinerService.class.getName();
    	}
    	
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.equals(service.service.getClassName())) {
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
    
    private BroadcastReceiver trafficReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			HashMap<String, String> thisProc;
			String tx,rx;
			
			HashMap<String,String> txBytes = (HashMap<String,String>) intent.getSerializableExtra(MinerService.MINER_TRAFFIC_TXBYTES);
			HashMap<String,String> rxBytes = (HashMap<String,String>) intent.getSerializableExtra(MinerService.MINER_TRAFFIC_RXBYTES);
			
			HashSet<String> procSet = new HashSet<String>(txBytes.keySet());
			procSet.addAll(new HashSet<String>(rxBytes.keySet()));
			
			for (String proc: procSet) {
				tx = txBytes.get(proc);
				if (tx == null) tx = "0";
				rx = rxBytes.get(proc);
				if (rx == null) rx = "0";
				thisProc = new HashMap<String, String>();
				thisProc.put("proc", proc);
				thisProc.put("tx", tx);
				thisProc.put("rx", rx);
				Log.i("MobileMiner",proc+" "+tx+" "+rx);
				traffic.put(proc, thisProc);
			}
			
			ArrayList<HashMap<String, String>> trafficMaps = new ArrayList<HashMap<String, String>>();
			for (String proc: traffic.keySet()) {
				trafficMaps.add(traffic.get(proc));
			}
			
			if (trafficMaps.size()>0) {
				trafficAdapter = new TrafficAdapter(context, R.layout.traffic_item, trafficMaps);
				trafficView.setAdapter(trafficAdapter);
			}	
	
		}
    	
    };
    
    private BroadcastReceiver minerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			enableMiningButton(intent.getBooleanExtra("MINER_BUTTON", false));
		}
    };
    
    @SuppressLint("NewApi")
	public boolean isAccessibilityEnabled() {
    	// http://stackoverflow.com/questions/5081145/android-how-do-you-check-if-a-particular-accessibilityservice-is-enabled
    	if (Build.VERSION.SDK_INT >= 17) {
    		AccessibilityManager am = (AccessibilityManager) this.getSystemService(Context.ACCESSIBILITY_SERVICE);
    		List<AccessibilityServiceInfo> runningServices = am.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
    		for (AccessibilityServiceInfo service : runningServices) {
    			//Log.i("MobileMiner",service.getId());
    			if ("uk.ac.kcl.odo.mobileminer/.miner.NotificationService".equals(service.getId())) return true;
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
