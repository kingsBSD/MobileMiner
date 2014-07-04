package uk.ac.kcl.odo.mobileminer.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class MinerDataProvider extends ContentProvider {

	public static final String PROVIDER_NAME = "uk.ac.kcl.odo.mobileminer"; 
    public static final Uri SOCKETPROC_URI = Uri.parse("content://" + PROVIDER_NAME + "/socketsbyproc" );
    
    private static final int SOCKETPROC = 1;
    
    private static final UriMatcher uriMatcher ;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "socketsbyproc", SOCKETPROC);
    }
    
    private MinerData helper;
    
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		helper = new MinerData(getContext());
        return true;
	}

	 @Override
	    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
	 
	        if (uriMatcher.match(uri) == SOCKETPROC){
	        	return helper.socketsByProc(projection,selection,selectionArgs);
	        } else {
	            return null;
	        }
	    }

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

}
