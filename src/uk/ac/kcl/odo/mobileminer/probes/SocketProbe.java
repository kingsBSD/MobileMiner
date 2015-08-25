package uk.ac.kcl.odo.mobileminer.probes;

import uk.ac.kcl.odo.mobileminer.miner.ProcSocketSet;
import uk.ac.kcl.odo.mobileminer.data.WriteCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.probe.Probe.Base;
import edu.mit.media.funf.probe.Probe.DisplayName;
import edu.mit.media.funf.probe.Probe.RequiredFeatures;
import edu.mit.media.funf.probe.Probe.RequiredPermissions;

@Schedule.DefaultSchedule(interval=2)
@DisplayName("Network Sockets Probe")
@RequiredPermissions({})
@RequiredFeatures("")
public class SocketProbe extends Base {

	private ProcSocketSet socketSet;
	
	private BroadcastReceiver  socketProbeReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
       	Log.w("MobileMiner","Socket Ping!");			
		}
	
	};	
		
	@Override
	protected void onEnable() {
		super.onEnable();
		getContext().registerReceiver(socketProbeReciever, new IntentFilter(WriteCache.CACHE_SOCKET));
		//socketSet = new ProcSocketSet(getContext());
		socketSet = new ProcSocketSet(getContext());
		Log.w("MobileMiner","Enable");
		
	}	
	
	@Override
	protected void onStart() {
		super.onStart();
		
		Log.w("MobileMiner","Start");

	}

	@Override
	protected void onDisable() {
		super.onDisable();
		getContext().unregisterReceiver(socketProbeReciever);
	}
	
	@Override
	public void registerListener(DataListener... listener) {
		super.registerListener(listener);
		//int i = 1/0;
		if (socketSet != null) {
			//Log.w("MobileMiner","Hello!");
			socketSet.scan();
		}
		else {
			Log.w("MobileMiner","Ouch!");
		}
		// TODO Auto-generated method stub
		
	}
//
//	@Override
//	public void destroy() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public State getState() {
//		// TODO Auto-generated method stub
//		Log.w("MobileMiner","Hello Again");
//		return null;
//	}
//
//	@Override
//	public void addStateListener(StateListener listener) {
//		Log.w("MobileMiner","Sod Off");
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void removeStateListener(StateListener listener) {
//		// TODO Auto-generated method stub
//		Log.w("MobileMiner","Hello Again");
//		
//	}





}
