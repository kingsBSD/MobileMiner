package com.odo.kcl.mobileminer.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.odo.kcl.mobileminer.R;

import java.util.concurrent.Callable;

public class SettingsActivity extends Activity {

    private SettingsItemAdapter adapter;
    public static final String SHARED_PREFERENCE_IDENTIFIER = "MobileMinerSettings";
    public static final String SHARED_PREFERENCE_MINE_ON_BOOT_IDENTIFIER = "MineOnBoot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ListView listView = (ListView) findViewById(R.id.settingsListView);
        this.adapter = new SettingsItemAdapter(this, new SettingsItem[]{
                new SettingsCheckItem(getString(R.string.element_title_activity_settings_mine_on_boot), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CheckedTextView v = (CheckedTextView) view;
                        v.toggle();
                        SharedPreferences sp = getSharedPreferences(SettingsActivity.SHARED_PREFERENCE_IDENTIFIER, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean(SettingsActivity.SHARED_PREFERENCE_MINE_ON_BOOT_IDENTIFIER, v.isChecked());
                        editor.apply();
                    }
                }, new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        SharedPreferences sp = getSharedPreferences(SettingsActivity.SHARED_PREFERENCE_IDENTIFIER, MODE_PRIVATE);
                        return sp.getBoolean(SettingsActivity.SHARED_PREFERENCE_MINE_ON_BOOT_IDENTIFIER, false);
                    }
                })
        });
        listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        listView.setAdapter(this.adapter);
    }

    protected class SettingsItemAdapter extends ArrayAdapter<SettingsItem> {

        public SettingsItemAdapter(Context context, SettingsItem[] items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return this.getItem(position).setupView(convertView, parent);
        }
    }

    protected interface SettingsItem {
        public View setupView(View convertView, ViewGroup parent);
    }

    protected class SettingsCheckItem implements SettingsItem {
        private String title;
        private View.OnClickListener clickListener;
        private Callable<Boolean> computeValue;

        public SettingsCheckItem(String title, View.OnClickListener clicklistener, Callable<Boolean> computeValue) {
            this.title = title;
            this.clickListener = clicklistener;
            this.computeValue = computeValue;
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public View setupView(View convertView, ViewGroup parent) {
            CheckedTextView textView;
            if (convertView == null) {
                textView = (CheckedTextView) getLayoutInflater().inflate(R.layout.checkbox_row, parent, false);
            } else {
                textView = (CheckedTextView) convertView;
            }
            textView.setText(this.title);
            try {
                textView.setChecked(computeValue.call());
            } catch (Exception e) {
                e.printStackTrace();
            }
            textView.setOnClickListener(this.clickListener);
            return textView;
        }
    }
}