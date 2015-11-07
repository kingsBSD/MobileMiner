package uk.ac.kcl.odo.mobileminer.activities;

import edu.mit.media.openpds.client.PersonalDataStore;
import edu.mit.media.openpds.client.PreferencesWrapper;
import edu.mit.media.openpds.client.RegistryClient;
import edu.mit.media.openpds.client.UserLoginTask;
import edu.mit.media.openpds.client.UserRegistrationTask;
import uk.ac.kcl.odo.mobileminer.R;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class PdsRegisterActivity extends BaseActivity {
	
	PreferencesWrapper prefs;
	PersonalDataStore pds;
	RegistryClient registryClient;
	
	EditText userText, emailText, pwText, pwRepText;
	String userName, uid, email, pw, pwRep;
	
	String basicAuth = "Basic ODZhY2FiYmQ0MTc3ZjdiZGExNjU2NzI1ZGFjYmMzOmQ2ZTBlYjZmYTZiNTMzMjZkM2FmMWExYzAyMjUzYw==";
	String clientKey = "86acabbd4177f7bda1656725dacbc3";
	String clientSecret = "d6e0eb6fa6b53326d3af1a1c02253c";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        
		prefs = new PreferencesWrapper(this);
			
		String registryUrl = PreferenceManager.getDefaultSharedPreferences(this).getString("mobileminer_openpds_registry","http://10.0.2.2:8000");
				
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pds_register);
        
        userText = (EditText) findViewById(R.id.pdsuser);
        emailText = (EditText) findViewById(R.id.pdsemail);
        pwText = (EditText) findViewById(R.id.pdspassword);
        pwRepText = (EditText) findViewById(R.id.pdspasswordrep);
                            
        registryClient = new RegistryClient(registryUrl, clientKey,clientSecret, "funf_write",basicAuth);
               
	}
	
	private void getRegistrationDetails() {
		userName = userText.getText().toString();
		email = emailText.getText().toString();
		pw = pwText.getText().toString();
		pwRep = pwRepText.getText().toString();
		
		String[] nameParts = userName.split(" ");
		
		try {
			uid = nameParts[0] + nameParts[1];
		} catch (Exception e) {
			uid = nameParts[0];
		}	
	
//		userName = "Bob Smith";
//		email = "bob@smith.org";
//		pw = "bob";
//      uid = BobSmith		
//		pwRep = "bob";
		
	}
	
	public void pdsRegister(View button) {
		getRegistrationDetails();
		
		UserRegistrationTask userRegistrationTask = new UserRegistrationTask(this, prefs, registryClient) {
		@Override
		protected void onComplete() {
			PdsRegisterActivity.this.startActivity(new Intent(PdsRegisterActivity.this, MainActivity.class));
		}
		
		@Override
		protected void onError() {
			//textView.setText("An error occurred while registering");
		}
	};
	userRegistrationTask.execute(userName, email, pw, uid);
	
}
	
	public void pdsLogin(View button) {
		getRegistrationDetails();
		UserLoginTask userLoginTask = new UserLoginTask(this,  prefs, registryClient) {
			@Override
			protected void onComplete() {
				//textView.setText("Login Succeeded");
				try {
					pds = new PersonalDataStore(PdsRegisterActivity.this);
					PdsRegisterActivity.this.startActivity(new Intent(PdsRegisterActivity.this, MainActivity.class));
				} catch (Exception e) {
					Log.w("HelloPDS", "Unable to construct PDS after login");
				}
			}
			
			@Override
			protected void onError() {
				//textView.setText("An error occurred while logging in");
				
				// If an error occurred, maybe the user hasn't been registered yet?
				// For cases where auto-registration is desired, call UserRegistrationTask here
			}
		};
		userLoginTask.execute(uid, pw);
	}
	
	public void pdsReset(View button) {
		SharedPreferences pref;
		pref = this.getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE);
		pref.edit().remove("accessToken").commit();
		pref.edit().remove("pds_location").commit();
		pref.edit().remove("uuid").commit();		
	}

}
