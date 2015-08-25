package uk.ac.kcl.odo.mobileminer.probes;

import uk.ac.kcl.odo.mobileminer.data.WriteCache;
import uk.ac.kcl.odo.mobileminer.miner.TrafficWatcher;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.probe.Probe.Base;
import edu.mit.media.funf.probe.Probe.DataListener;
import edu.mit.media.funf.probe.Probe.DisplayName;
import edu.mit.media.funf.probe.Probe.RequiredFeatures;
import edu.mit.media.funf.probe.Probe.RequiredPermissions;

@Schedule.DefaultSchedule(interval=1)
@DisplayName("Network Traffic Probe")
@RequiredPermissions({})
@RequiredFeatures("")
public class TrafficProbe extends Base {
	
	private TrafficWatcher watcher;
	
	private BroadcastReceiver trafficReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.w("MobileMiner","Traffic Ping!");
		}
	
	};
	
	@Override
	protected void onEnable() {
		super.onEnable();
		getContext().registerReceiver(trafficReciever, new IntentFilter(WriteCache.CACHE_TRAFFIC));
		watcher = new TrafficWatcher(getContext());
		Log.w("MobileMiner","Enable");
		
	}

	@Override
	protected void onDisable() {
		super.onDisable();
		getContext().unregisterReceiver(trafficReciever);
	}
	
	@Override
	public void registerListener(DataListener... listener) {
		super.registerListener(listener);
		//int i = 1/0;
		if (watcher != null) {
			//Log.w("MobileMiner","Hello!");
			watcher.scan();
		}
		else {
			Log.w("MobileMiner","Ouch!");
		}
		
	}
}
