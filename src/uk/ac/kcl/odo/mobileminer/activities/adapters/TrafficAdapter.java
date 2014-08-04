package uk.ac.kcl.odo.mobileminer.activities.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import uk.ac.kcl.odo.mobileminer.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TrafficAdapter extends ArrayAdapter<HashMap<String, String>> {

	private Context context;
	private int resourceId;
	private ArrayList<HashMap<String, String>> data;
	
	public TrafficAdapter(Context context, int resource,
			ArrayList<HashMap<String, String>> trafficMaps) {
		super(context, resource, trafficMaps);
		
        this.resourceId = resource;
        this.context = context;
        this.data = trafficMaps;
				
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View item = convertView;
		ItemHolder holder = null;
		
		if (item == null) {
			
			//LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			LayoutInflater inflater = LayoutInflater.from(context);
            item = inflater.inflate(resourceId, parent, false);
            
            holder = new ItemHolder();
            holder.procTxt = (TextView)item.findViewById(R.id.trafficProc);
            holder.txTxt = (TextView)item.findViewById(R.id.trafficTx);
            holder.rxTxt = (TextView)item.findViewById(R.id.trafficRx);
            
            item.setTag(holder);
			
		}
		else {
			holder = (ItemHolder) item.getTag();
		}
		
        holder.procTxt.setText(data.get(position).get("proc"));
        holder.txTxt.setText(data.get(position).get("tx"));
        holder.rxTxt.setText(data.get(position).get("rx"));
        
		
		return item;
	}
	
	static class ItemHolder {
		TextView procTxt, txTxt, rxTxt;
	}
}
