// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package uk.ac.kcl.odo.mobileminer.activities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.kcl.odo.mobileminer.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class MapActivity extends BaseActivity {
	WebView mapWebView;
	String lat,lon,zoom;
	boolean noCentre;
	ArrayList<String> redLat,redLong,yellowLat,yellowLong,greenLat,greenLong,blueLat,blueLong;
	String legend;
	
	public class MapInterface {
		 Context context;

		    public MapInterface() {
		   
		    } 
		 
		    public MapInterface(Context c) {
		    	context = c;
		    }

		 @android.webkit.JavascriptInterface   
		 public String mapDump() {
			 
				JSONObject JSONdump = new JSONObject();
				
				try {
					JSONdump.put("long",lon);
					JSONdump.put("lat",lat);
					JSONdump.put("zoom",zoom);
					JSONdump.put("nocentre",noCentre);
					if (redLat != null) {
						JSONdump.put("redlat",redLat);
						JSONdump.put("redlong",redLong);	
					}
					if (yellowLat != null) {
						JSONdump.put("yellowlat",yellowLat);
						JSONdump.put("yellowlong",yellowLong);	
					}
					if (greenLat != null) {
						JSONdump.put("greenlat",greenLat);
						JSONdump.put("greenlong",greenLong);	
					}
					if (blueLat != null) {
						JSONdump.put("bluelat",blueLat);
						JSONdump.put("bluelong",blueLong);	
					}
					if (legend != null) {
						JSONdump.put("legend",legend);
					}
					
					//Log.i("MinerMap",JSONdump.toString());
					
					return JSONdump.toString();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					return null;
				}
			 
		 }
		    

		}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent mapIntent = this.getIntent();
		
		setContentView(R.layout.activity_map);
		mapWebView = (WebView)findViewById(R.id.mapWebView);
		mapWebView.addJavascriptInterface(new MapInterface(), "MapActivity");
		mapWebView.getSettings().setJavaScriptEnabled(true);
		mapWebView.loadUrl("file:///android_asset/viewer.html");
		
		lat = mapIntent.getStringExtra("lat");
		lon = mapIntent.getStringExtra("long");
		zoom = mapIntent.getStringExtra("zoom");
		noCentre = mapIntent.getBooleanExtra("nocentre",false);
		redLat = mapIntent.getStringArrayListExtra("redlat");
		redLong = mapIntent.getStringArrayListExtra("redlong");
		yellowLat = mapIntent.getStringArrayListExtra("yellowlat");
		yellowLong = mapIntent.getStringArrayListExtra("yellowlong");
		greenLat = mapIntent.getStringArrayListExtra("greenlat");
		greenLong = mapIntent.getStringArrayListExtra("greenlong");
		blueLat = mapIntent.getStringArrayListExtra("bluelat");
		blueLong = mapIntent.getStringArrayListExtra("bluelong");
		legend = mapIntent.getStringExtra("legend");
	}


	private String readHtml(String remoteUrl) {
	    String out = "";
	    BufferedReader in = null;
	    try {
	        URL url = new URL(remoteUrl);
	        in = new BufferedReader(new InputStreamReader(url.openStream()));
	        String str;
	        while ((str = in.readLine()) != null) {
	            out += str;
	        }
	        return out;
	    }
	    catch (Exception e) {return null;}
	}

}
	
