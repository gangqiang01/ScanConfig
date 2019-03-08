package com.advantech.edgexdeploy;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;


public class About extends Activity {

	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		initActionBar();
		TextView appName = findViewById(R.id.appName);
		TextView appVersion = findViewById(R.id.appVersion);
		TextView website = findViewById(R.id.website);
		TextView copyRight = findViewById(R.id.copyRight);
		appName.setText(R.string.app_name);
		appVersion.setText("Version V"+getAppVersionName(this));
		appName.setTextColor(Color.BLACK);
		appVersion.setTextColor(Color.GRAY);
		website.setTextColor(Color.GRAY);
		copyRight.setTextColor(Color.GRAY);
	}
	
	private void initActionBar(){
		ActionBar actionBar = this.getActionBar();
		if(actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setTitle("About");
		}
	}
	
	private String getAppVersionName(Context context){
		String versionName = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		if (versionName == null || versionName.length() <= 0) {
			return "";
		}
		return versionName;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home){
			finish();
		}
		return true;
	}
}
