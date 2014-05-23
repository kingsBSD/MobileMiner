package com.odo.kcl.mobileminer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.webkit.WebView;

public class MapActivity extends Activity {
	WebView mapWebView;
	String lat,lon,zoom;
	
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

				
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
		

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
	
