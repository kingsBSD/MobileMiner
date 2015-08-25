package uk.ac.kcl.odo.mobileminer.probes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import edu.mit.media.funf.probe.Probe;

public class wifiProbe extends Probe.Base {
	
	private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
		}
	};

//	@Override
//	public void registerListener(DataListener... listener) {
//		// TODO Auto-generated method stub
//
//	}
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
//		return null;
//	}
//
//	@Override
//	public void addStateListener(StateListener listener) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void removeStateListener(StateListener listener) {
//		// TODO Auto-generated method stub
//
//	}

}
