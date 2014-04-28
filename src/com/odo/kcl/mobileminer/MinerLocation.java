// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package com.odo.kcl.mobileminer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
//import android.util.Log;

public class MinerLocation {
	boolean isNone = true;
	String Mcc,Mnc,Lac,Id;
	public int signalStrength;
	
	public MinerLocation(CellLocation location, Context context) {
		TelephonyManager manager;
		String mccmnc;
		signalStrength = 0;
		manager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
		mccmnc = manager.getNetworkOperator();
		if (mccmnc != null) {			
			if (location instanceof GsmCellLocation) {
				try {
					Mcc = mccmnc.substring(0,3);
					Mnc = mccmnc.substring(3);
					Lac = Integer.toString(((GsmCellLocation) location).getLac());
					Id = Integer.toString(((GsmCellLocation) location).getCid());
					isNone = false;
				}
				catch(Exception e) {
					isNone = true;
				}
			}
		}		

		//Log.i("MinerService",Mcc+" "+Mnc+" "+Lac+" "+Id);
	}
	
	@SuppressLint("NewApi")
	public MinerLocation(CellInfo cellInfo) {
		if (cellInfo instanceof CellInfoGsm) {
			Mcc = Integer.toString(((CellInfoGsm) cellInfo).getCellIdentity().getMcc());
			Mnc = Integer.toString(((CellInfoGsm) cellInfo).getCellIdentity().getMnc());
			Lac = Integer.toString(((CellInfoGsm) cellInfo).getCellIdentity().getLac());
			Id = Integer.toString(((CellInfoGsm) cellInfo).getCellIdentity().getCid());
			signalStrength = ((CellInfoGsm) cellInfo).getCellSignalStrength().getAsuLevel();
			if (signalStrength > 31) signalStrength = 0;
			isNone = false;
		}
	}
	
	public String dump() {
		if (isNone) {
			return "None";
		}
		else {
			return "MNC "+Mnc+" LAC "+Lac+" CID "+Id;
		}
	}
	
	public MinerLocation compare(MinerLocation location) {
		if (this.signalStrength > location.signalStrength) {
			return this;	
		}
		else {
			return location;
		}		
	}
}
