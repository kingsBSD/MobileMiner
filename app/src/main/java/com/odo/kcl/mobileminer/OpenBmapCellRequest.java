// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package com.odo.kcl.mobileminer;

import android.os.AsyncTask;
import android.text.TextUtils;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenBmapCellRequest extends AsyncTask {

    static Pattern latPattern = Pattern.compile("lat=\"([\\-0-9\\.]+)\"");
    static Pattern longPattern = Pattern.compile("lng=\"([\\-0-9\\.]+)\"");
    static Pattern polyPattern = Pattern.compile("\\(\\(([\\-0-9\\.,\\s]+)\\)\\)");

    @Override
    protected Object doInBackground(Object... cellSpec) {
        if (cellSpec.length < 4) return null;
        String Mcc, Mnc, Lac, Id;
        Mcc = (String) cellSpec[0];
        Mnc = (String) cellSpec[1];
        Lac = (String) cellSpec[2];
        Id = (String) cellSpec[3];
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://www.openbmap.org/api/getGPSfromGSM.php");
        List<NameValuePair> postData = new ArrayList<NameValuePair>(4);
        postData.add(new BasicNameValuePair("mcc", Mcc));
        postData.add(new BasicNameValuePair("mnc", Mnc));
        postData.add(new BasicNameValuePair("lac", Lac));
        postData.add(new BasicNameValuePair("cell_id", Id));
        try {
            String XMLdump, Lat, Long, poly, polyDump;
            Lat = null;
            Long = null;
            poly = null;
            post.setEntity(new UrlEncodedFormEntity(postData));
            XMLdump = EntityUtils.toString(client.execute(post).getEntity());
            //Log.i("MobileMiner",XMLdump);
            Matcher latMatch = latPattern.matcher(XMLdump);
            while (latMatch.find()) Lat = latMatch.group(1);
            Matcher longMatch = longPattern.matcher(XMLdump);
            while (longMatch.find()) Long = longMatch.group(1);
            Matcher polyMatch = polyPattern.matcher(XMLdump);
            while (polyMatch.find()) poly = polyMatch.group(1);
            //Log.i("MobileMiner","Lat "+Lat);
            //Log.i("MobileMiner","Long "+Long);
            //Log.i("MobileMiner","Poly "+poly);
            if (poly != null) {
                List<String> points = new ArrayList<String>();
                String[] point;
                String[] polyChunks = poly.split(",");
                for (String chunk : polyChunks) {
                    point = chunk.split("\\s+");
                    points.add("[" + point[1] + "," + point[0] + "]");
                }
                polyDump = "[" + TextUtils.join(",", points.subList(0, points.size() - 1)) + "]";
                //Log.i("MobileMiner",polyDump);
            } else {
                polyDump = null;
            }
            if (Lat != null && Long != null) {
                return new String[]{Lat, Long, polyDump};
            } else {
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

        // TODO Auto-generated method stub
        return null;
    }
}
