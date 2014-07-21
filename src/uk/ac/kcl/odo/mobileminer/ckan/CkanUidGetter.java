// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package uk.ac.kcl.odo.mobileminer.ckan;

import java.util.concurrent.ExecutionException;

import uk.ac.kcl.odo.mobileminer.data.MinerData;
import uk.ac.kcl.odo.mobileminer.data.MinerTables.BookKeepingTable;
import uk.ac.kcl.odo.mobileminer.data.MinerTables.MinerLogTable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CkanUidGetter {
	Context context;
	
	public CkanUidGetter(Context ctx) {
		context = ctx;
	}
	
	public String getUid() {
		return getUid(false);
	}
	
	public String getUid(boolean forceRefresh) {
		String uid;	
		
		MinerData helper = new MinerData(context);
		uid = null;
		
		if (!forceRefresh) {
			uid = helper.getBookKeepingKey(helper.getReadableDatabase(), "ckanuid");
		}
		
		if (uid == null) {
			try {
				uid = (String) new CkanUidRequest().execute(new Context[]{context}).get();
			}
			catch (InterruptedException e) {
				uid = null;
			}
			catch (ExecutionException e) {
				uid = null;
			}
			
			if (uid != null) {
				helper.deleteBookKeepingKey(helper.getWritableDatabase(), "ckanuid");
				helper.setBookKeepingKey(helper.getWritableDatabase(), "ckanuid", uid);
			}
			
		}
		
		helper.close();
		return uid;
		
	}	
		
}
