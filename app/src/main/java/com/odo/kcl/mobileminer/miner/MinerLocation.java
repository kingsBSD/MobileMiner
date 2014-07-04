// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package com.odo.kcl.mobileminer.miner;

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
    boolean isValid = false;
    private String Mcc, Mnc, Lac, Id;
    private int signalStrength;

    public MinerLocation(CellLocation location, Context context) {
        TelephonyManager manager;
        String mccmnc;
        int intLac, intId;
        signalStrength = 99;
        Mcc = Mnc = Lac = Id = "None";
        manager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        mccmnc = manager.getNetworkOperator();
        if (mccmnc != null) {
            if (location instanceof GsmCellLocation) {
                try {
                    Mcc = mccmnc.substring(0, 3);
                    Mnc = mccmnc.substring(3);
                    intLac = ((GsmCellLocation) location).getLac();
                    intId = ((GsmCellLocation) location).getCid();
                    Lac = Integer.toString(intLac);
                    Id = Integer.toString(intId);
                    if (intLac >= 0 && intId >= 0) isValid = true;
                    isNone = false;
                } catch (Exception e) {
                    isNone = true;
                }
            }
        }

        //Log.i("MinerService",Mcc+" "+Mnc+" "+Lac+" "+Id);
    }

    @SuppressLint("NewApi")
    public MinerLocation(CellInfo cellInfo) {
        int intMcc, intMnc, intLac, intId;
        if (cellInfo instanceof CellInfoGsm) {
            isNone = false;
            isValid = true;
            intMcc = ((CellInfoGsm) cellInfo).getCellIdentity().getMcc();
            intMnc = ((CellInfoGsm) cellInfo).getCellIdentity().getMnc();
            intLac = ((CellInfoGsm) cellInfo).getCellIdentity().getLac();
            intId = ((CellInfoGsm) cellInfo).getCellIdentity().getCid();
            for (int value : new int[]{intMcc, intMcc, intLac, intId}) {
                if (value < 0) {
                    isValid = false;
                    break;
                }
            }
            Mcc = Integer.toString(intMcc);
            Mnc = Integer.toString(intMnc);
            Lac = Integer.toString(intLac);
            Id = Integer.toString(intId);
            signalStrength = ((CellInfoGsm) cellInfo).getCellSignalStrength().getAsuLevel();
        }
    }

    public String getMcc() {
        return Mcc;
    }

    public String getMnc() {
        return Mnc;
    }

    public String getLac() {
        return Lac;
    }

    public String getId() {
        return Id;
    }

    public String getStrength() {
        return Integer.toString(signalStrength);
    }

    public String dump() {
        if (isNone) {
            return "No Cell";
        } else {
            return "MNC " + Mnc + " LAC " + Lac + " CID " + Id;
        }
    }

    public Boolean isValid() {
        return isValid;
    }

    public MinerLocation compare(MinerLocation location) {
        if (this.signalStrength > location.signalStrength) {
            return this;
        } else {
            return location;
        }
    }
}
