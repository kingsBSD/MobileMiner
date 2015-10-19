package uk.ac.kcl.odo.mobileminer.probes;

import com.google.gson.JsonObject;

import uk.ac.kcl.odo.mobileminer.data.WriteCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import edu.mit.media.funf.probe.Probe.Base;

public class NotificationProbe extends Base {
	
	private BroadcastReceiver notificationReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.w("MobileMiner","Notification Ping!");
			String[] notificationKeys = new String[]{WriteCache.NOTIFICATION_NAME,WriteCache.NOTIFICATION_TIME,
      			WriteCache.NOTIFICATION_DAY};
			JsonObject data = new JsonObject();
			for(String key : notificationKeys) {
				data.addProperty(key, intent.getStringExtra(key));
			}
			sendData(data);  
		}
	
	};
	
	@Override
	protected void onEnable() {
		super.onEnable();
		getContext().registerReceiver(notificationReciever, new IntentFilter(WriteCache.CACHE_NOTIFICATION));
//		Log.w("MobileMiner","Enable");
		
	}

	@Override
	protected void onDisable() {
		super.onDisable();
		getContext().unregisterReceiver(notificationReciever);
	}

	@Override
	public void registerListener(DataListener... listener) {
		super.registerListener(listener);		
	}	
	
}
