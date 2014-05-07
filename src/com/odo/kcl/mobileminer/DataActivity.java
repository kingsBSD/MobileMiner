package com.odo.kcl.mobileminer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.odo.kcl.mobileminer.MinerTables.BookKeepingTable;

import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class DataActivity extends Activity {
	private Context context;
	private TextView dataText;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data);
		context = this;
		dataText  = (TextView) findViewById(R.id.dataText);
		setDbSizeLegend();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.data, menu);
		return true;
	}
	
    public void exportData(View buttonView) {
    	AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(context);
    	myAlertDialog.setTitle("Export Data");
    	MinerData helper = new MinerData(context);
    	String lastExported = helper.getLastExported(helper.getReadableDatabase());
    	helper.close();
    	if (lastExported == null) {
    		myAlertDialog.setMessage("No data has been exported yet.");	
    	}
    	else {
    		myAlertDialog.setMessage("Data was last exported "+lastExported);	
    	}
    	myAlertDialog.setPositiveButton("Export", new DialogInterface.OnClickListener() {

    	public void onClick(DialogInterface arg0, int arg1) {
    		dumpDb();
    	  }});
    	 myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	       
    	  public void onClick(DialogInterface arg0, int arg1) {
    	  // do something when the Cancel button is clicked
    	  }});
    	 myAlertDialog.show();
    }

    public void expireData(View buttonView) {
    	AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(context);
    	myAlertDialog.setTitle("Expire Data");
    	myAlertDialog.setMessage("Remove exported data from the database?");
    	myAlertDialog.setPositiveButton("Expire", new DialogInterface.OnClickListener() {

    	public void onClick(DialogInterface arg0, int arg1) {

    	  }});
    	
   	 myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	       
   	  public void onClick(DialogInterface arg0, int arg1) {
   	  // do something when the Cancel button is clicked
   	  }});
   	 
   	 myAlertDialog.show();
    	
    }
    
    private void setDbSizeLegend() {
    	// http://stackoverflow.com/questions/6364577/how-to-get-the-current-sqlite-database-size-or-package-size-in-android
    	long dbSize = context.getDatabasePath(MinerData.DATABASE_NAME).length();
    	long divider = 1024;
    	String unit = "Kb";
    	if (dbSize > 1048576) {unit = "Mb"; divider = 1048576;}
    	dataText.setText((CharSequence) ("Database Size: "+Long.toString(dbSize/divider)+unit));
    }
    
    private void dumpDb() {
    	// http://www.techrepublic.com/blog/software-engineer/export-sqlite-data-from-your-android-device/#.
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-ddHH-mm-ss");
    	File dest = Environment.getExternalStorageDirectory();
      	File source = Environment.getDataDirectory();
      	FileChannel sourceStream, destStream;
      	String dbPath = "/data/"+ "com.odo.kcl.mobileminer" +"/databases/"+MinerData.DATABASE_NAME;
      	Date rightNow = new Date();
        String exportPath = "MobileMiner" + df.format(rightNow) + ".sqlite";
        File dbFile = new File(source, dbPath);
        File exportFile = new File(dest, exportPath);
        try {
            sourceStream = new FileInputStream(dbFile).getChannel();
            destStream = new FileOutputStream(exportFile).getChannel();
            destStream.transferFrom(sourceStream, 0, sourceStream.size());
            sourceStream.close();
            destStream.close();
            MinerData helper = new MinerData(context);
    		helper.setLastExported(helper.getReadableDatabase(),rightNow);
            Toast.makeText(this, "Data Exported.", Toast.LENGTH_LONG).show();
            helper.close();
        }
        catch(IOException e) {
        	e.printStackTrace();
        	Toast.makeText(this, "Couldn't Export Data!", Toast.LENGTH_LONG).show();
        }
    }
    
}
