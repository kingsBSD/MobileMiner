// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package uk.ac.kcl.odo.mobileminer.data;

import java.io.IOException;
import java.util.HashMap;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.kcl.odo.mobileminer.data.MinerTables.GeoIpTable;
import android.os.AsyncTask;
//import android.util.Log;

public class FreeGeoIpRequest extends AsyncTask<String, Object, HashMap<String,String>> {

	@Override
	protected HashMap<String,String> doInBackground(String... arg0) {
				
		String ip = (String) arg0[0];
		JSONObject jsonDump;
		String response;
		
		HttpGet get = new HttpGet("http://freegeoip.net/json/"+ip);
		HttpClient client = new DefaultHttpClient();
		
		try {
			response = EntityUtils.toString(client.execute(get).getEntity());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		} catch (ClientProtocolException e) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		//{"ip":"6.14.7.4","country_code":"US","country_name":"United States","region_code":"AZ","region_name":"Arizona","city":"Fort Huachuca","zipcode":"85613","latitude":31.5273,"longitude":-110.3607,"metro_code":"789","area_code":"520"}
		
		String[] mapKeys = {GeoIpTable.COLUMN_NAME_COUNTRY,GeoIpTable.COLUMN_NAME_REGION,
			GeoIpTable.COLUMN_NAME_CITY,GeoIpTable.COLUMN_NAME_LAT,GeoIpTable.COLUMN_NAME_LONG};
		
		String[] jsonKeys = {"country_name","region_name","city","latitude","longitude"};
		
		if (response != null) {
			try {
				HashMap<String,String> geoData = new HashMap<String,String>();
				jsonDump = new JSONObject(response);
				for (int i=0;i<5;i++) {
					geoData.put(mapKeys[i],jsonDump.getString(jsonKeys[i]));
				}
				return geoData;
			}
			catch (JSONException e) {
				return null; 
			}
		}
				
		return null;
	}

}
