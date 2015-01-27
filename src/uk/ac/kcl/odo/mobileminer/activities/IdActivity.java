package uk.ac.kcl.odo.mobileminer.activities;

import uk.ac.kcl.odo.mobileminer.R;
import uk.ac.kcl.odo.mobileminer.ckan.CkanUidGetter;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class IdActivity extends Activity {

	private CkanUidGetter idGetter;
	private TextView idText;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_id);
		
		idText = (TextView) findViewById(R.id.user_id_text);
		idGetter = new CkanUidGetter(this);
		idText.setText(idGetter.getUid());
	}
	
	public void idToClipboard(View buttonView) {
		// http://developer.android.com/guide/topics/text/copy-paste.html
		ClipboardManager clipboard = (ClipboardManager)
		getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("simple text",idGetter.getUid());
		clipboard.setPrimaryClip(clip);
	}
}
