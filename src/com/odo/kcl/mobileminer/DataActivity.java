package com.odo.kcl.mobileminer;

import java.util.Date;

import com.odo.kcl.mobileminer.MinerTables.BookKeepingTable;

import android.os.Bundle;
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

public class DataActivity extends Activity {
	private Context context;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data);
		context = this;
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
    		MinerData helper = new MinerData(context);
    		helper.setBookKeepingDate(helper.getReadableDatabase(), BookKeepingTable.DATA_LAST_EXPORTED,
    		new Date());
    		  
    		  
    	  // do something when the OK button is clicked
    	  }});
    	 myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	       
    	  public void onClick(DialogInterface arg0, int arg1) {
    	  // do something when the Cancel button is clicked
    	  }});
    	 myAlertDialog.show();
    }

}
