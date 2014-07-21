// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package uk.ac.kcl.odo.mobileminer.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Environment;
//import android.util.Log;

public class CellData extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "CellData.db";
    private Context ctx;
	
	public CellData(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		ctx = context;
	}

    public CellData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = context;
    }
	
	@Override
	public void onCreate(SQLiteDatabase arg0) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
	
	public void init() {
		//Log.i("CellData","CREATING");
		File dataDir = Environment.getDataDirectory();
		String dbPath = "/data/"+ "uk.ac.kcl.odo.mobileminer" +"/databases/"+DATABASE_NAME;
		File dbFile = new File(dataDir, dbPath);
		GZIPInputStream sourceStream;
		FileOutputStream destStream;
		if (!dbFile.exists()) {
			boolean okay;
			try {
				destStream = new FileOutputStream(dbFile);
				okay = true;
			}
			catch (Exception e) {
				// Log.i("CellData","Can't open dest stream.");
				destStream = null;
				okay = false;
			}
			
			try {
				sourceStream = new GZIPInputStream(ctx.getAssets().open("celldata.bin"));
				okay = true;
			}
			catch (Exception e) {
				//Log.i("CellData","Can't open source stream.");
				sourceStream = null;
				okay = false;
			}
			
			if (okay) {
				byte[] buffer = new byte[1024];
				int len;
			    try {
					while ((len = sourceStream.read(buffer)) > 0) destStream.write(buffer, 0, len);
					//Log.i("MinerData","Writing data..."); 			
				} 
			    catch (IOException e) {
			    	 //Log.i("MinerData","Can't copy source stream.");
				}
			    
				try {
					sourceStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					destStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			       
				
			}
			
			
		}
		
	}

}

