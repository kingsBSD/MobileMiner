package com.odo.kcl.mobileminer;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CellLocationGetter {
	private Context context;
	
	public CellLocationGetter(Context ctx) {
		context = ctx;
	}
	
	public String[] getCell(String Mcc, String Mnc, String Lac, String Id) {
    	MinerData helper = new MinerData(context);
    	SQLiteDatabase db = helper.getReadableDatabase();
    	String[] location = helper.getCellLocation(db,Mcc,Mnc,Lac,Id);
    	String polygon;
    	if (location != null) {
    		polygon = helper.getCellPolygon(db,Mcc,Mnc,Lac,Id);
    	}
    	else {
    		polygon = null;
    	}
    	
    	helper.close();
    	
    	if (location == null) {
    		String[] locRef;
    		db = helper.getWritableDatabase();
    		try {
				locRef = (String[]) new OpenBmapCellRequest().execute(new String[] {Mcc,Mnc,Lac,Id}).get();
			} catch (InterruptedException e) {
				helper.close(); return null;
			} catch (ExecutionException e) {
				helper.close();
				return null;
			}
    		
			//Log.i("MobileMiner","Lat "+locRef[0]);
			//Log.i("MobileMiner","Long "+locRef[1]);
			//Log.i("MobileMiner","Poly "+locRef[2]);
    		
    		helper.putGSMLocation(db,Mcc,Mnc,Lac,Id,locRef[0],locRef[1],"OpenBmap",new Date());
    		helper.putGSMCellPolygon(db,Mcc,Mnc,Lac,Id,locRef[2],"OpenBmap",new Date());
    		
    		return locRef;
    	}
    	else {
    		return new String[]{location[0],location[1],polygon};
    	}
    	
	}
	
}
