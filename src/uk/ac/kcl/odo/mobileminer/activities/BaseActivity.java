package uk.ac.kcl.odo.mobileminer.activities;

import uk.ac.kcl.odo.mobileminer.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class BaseActivity extends Activity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                this.startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_my_id:
            	this.startActivity(new Intent(this, IdActivity.class));
            	return true;
            case R.id.action_pds:
            	this.startActivity(new Intent(this, PdsRegisterActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
}

