// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package uk.ac.kcl.odo.mobileminer.activities.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.kcl.odo.mobileminer.R;
import uk.ac.kcl.odo.mobileminer.activities.MapActivity;
import uk.ac.kcl.odo.mobileminer.data.GeoIpGetter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

public class SocketAdapter extends android.widget.BaseExpandableListAdapter {
	private Context context;
	private HashMap<String, List<String>> listDataChild;
	private List<String> listDataHeader;
	private ArrayList<Boolean> processStatus;

	public SocketAdapter(Context ctxt, List<String> dataHeader,HashMap<String, List<String>> childData, ArrayList<Boolean> procStatus) {
		this.context = ctxt;
		this.listDataHeader = dataHeader;
		this.listDataChild = childData;
		this.processStatus = procStatus;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,boolean isLastChild, View convertView, ViewGroup parent) {
		final String childText = (String) getChild(groupPosition, childPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.socket_item, null);
		}

		TextView txtListChild = (TextView) convertView.findViewById(R.id.socketItem);
		txtListChild.setText(childText);
		if (processStatus != null) {
			if (processStatus.get(groupPosition)) {
				txtListChild.setTextColor(Color.GREEN);
			}
			else {
				txtListChild.setTextColor(Color.RED);
			}
		}
		
		// ExpandableListView.OnChildClickListener just doesn't want to fire in MainActivity, so override the onClick
		// listener of each view instead. Yes, making every bit of the socket_item view non-clickable or foccusable
		// has been tried;  no, it didn't work.
		convertView.setOnClickListener(new View.OnClickListener() {

	        @Override
	        public void onClick(View v) {
	        	
	        	//Log.i("MobileMiner",childText);
	        	
	        	// Do away with this, the geoIps are somewhat misleading...
	        	String ip = childText.split("\\s+")[1].split(":")[0];
	        	Intent mapIntent = new GeoIpGetter(context).getMapIntent(ip);
            	if (mapIntent == null) {
            		Toast.makeText(context, "Can't get geoIP data...", Toast.LENGTH_SHORT).show();
            	}
            	else {
            		mapIntent = new Intent(context, MapActivity.class);
            		mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            		context.startActivity(mapIntent);
            	}                
	        }
	    });
		
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this.listDataHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return this.listDataHeader.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,View convertView, ViewGroup parent) {
		String headerTitle = (String) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.socket_list, null);
		}
		TextView lblListHeader = (TextView) convertView.findViewById(R.id.processHeader);
		lblListHeader.setTypeface(null, Typeface.BOLD);
		lblListHeader.setText(headerTitle);

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}
}
