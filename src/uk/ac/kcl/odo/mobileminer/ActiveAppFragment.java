package uk.ac.kcl.odo.mobileminer;

import uk.ac.kcl.odo.mobileminer.data.MinerDataProvider;
import uk.ac.kcl.odo.mobileminer.data.MinerTables;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

public class ActiveAppFragment extends ListFragment implements LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter activeAppAdapter;
	private LoaderManager appLoaderManager;
	
	 @Override  
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,  
	    Bundle savedInstanceState) {
	
		 appLoaderManager = getLoaderManager();
		 
		 String[] appColumns = {MinerTables.SocketTable.COLUMN_NAME_PROCESS};
		

		 int[] activeAppViews = {android.R.id.text1};
		 
		 activeAppAdapter = new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_1, null, appColumns, activeAppViews,0);
		 
		 
		 setListAdapter(activeAppAdapter);
		 
		 appLoaderManager.initLoader(1, null, this);
		 
		 return super.onCreateView(inflater, container, savedInstanceState);	 	 
	 }


	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		Uri uri = MinerDataProvider.SOCKETPROC_URI;		
		String[] retCols = new String[]{MinerTables.SocketTable.COLUMN_NAME_PROCESS};
		
		return new CursorLoader(getActivity(),uri,retCols,null,null,"COUNT(*) DESC");

	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		activeAppAdapter.changeCursor(arg1);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		activeAppAdapter.changeCursor(null);
		
		// TODO Auto-generated method stub
		
	}

}

