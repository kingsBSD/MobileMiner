// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package com.odo.kcl.mobileminer.cell;

//import java.util.Date;
//import java.util.concurrent.ExecutionException;

import android.content.Context;

import com.odo.kcl.mobileminer.miner.MinerData;
//import android.database.sqlite.SQLiteDatabase;

public class CellLocationGetter {
    private Context context;

    public CellLocationGetter(Context ctx) {
        context = ctx;
    }

    public String[] getCell(CountedCell cell) {
        return this.getCell(cell.getMcc(), cell.getMnc(), cell.getLac(), cell.getCellId());
    }

    public String[] getCell(String Mcc, String Mnc, String Lac, String Id) {

        //Log.i("LocationGetter","Mcc "+Mcc);
        //Log.i("LocationGetter","Mnc "+Mnc);
        //Log.i("LocationGetter","Lac "+Lac);
        //Log.i("LocationGetter","Id "+Id);

        //Log.i("MinerData","Looking for cell...");

        CellData helper = new CellData(context);
        String[] location = MinerData.getCellLocation(helper.getReadableDatabase(), Mcc, Mnc, Lac, Id);

        return location;

//        if (location != null) return location;
//
//        //Log.i("LocationGetter","Can't find cell");
//
//        MinerData minerHelper = new MinerData(context);
//        SQLiteDatabase db = helper.getReadableDatabase();
//        location = minerHelper.getCellLocation(db,Mcc,Mnc,Lac,Id);
//        String polygon;
//        if (location != null) {
//            polygon = minerHelper.getCellPolygon(db,Mcc,Mnc,Lac,Id);
//        }
//        else {
//            polygon = null;
//        }
//
//        helper.close();
//
//        if (location == null) {
//            String[] locRef;
//            db = helper.getWritableDatabase();
//            try {
//                locRef = (String[]) new OpenBmapCellRequest().execute(new String[] {Mcc,Mnc,Lac,Id}).get();
//            } catch (InterruptedException e) {
//                helper.close(); return null;
//            } catch (ExecutionException e) {
//                helper.close();
//                return null;
//            }
//
//            //Log.i("LocationGetter","Lat "+locRef[0]);
//            //Log.i("LocationGetter","Long "+locRef[1]);
//            //Log.i("LocationGetter","Poly "+locRef[2]);
//
//            minerHelper.putGSMLocation(db,Mcc,Mnc,Lac,Id,locRef[0],locRef[1],"OpenBmap",new Date());
//            minerHelper.putGSMCellPolygon(db,Mcc,Mnc,Lac,Id,locRef[2],"OpenBmap",new Date());
//
//            return locRef;
//        }
//        else {
//            return new String[]{location[0],location[1]};
//        }

    }

}


