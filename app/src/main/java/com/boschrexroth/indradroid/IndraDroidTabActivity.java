package com.boschrexroth.indradroid;

import com.boschrexroth.indradroid.service.GraphingActivity1;
import com.boschrexroth.indradroid.service.HomeActivity;
import com.boschrexroth.indradroid.service.LogbookActivity;
//import com.boschrexroth.indradroid.service.SymbolEditorActivity;
//import com.boschrexroth.indradroid.service.SystemInfoActivity;
import com.boschrexroth.indradroid.misc.AboutDialog;
import com.boschrexroth.indradroid.misc.PreferencesActivity;
import com.boschrexroth.indradroid.service.SystemInfoActivity;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

public class IndraDroidTabActivity extends TabActivity  {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    	setContentView(R.layout.activity_indradroid_tab);

	    Resources res = getResources();
	    TabHost tabHost = getTabHost();
	    TabHost.TabSpec spec; 
	    Intent intent;

		// add tab
		intent = new Intent().setClass(this, HomeActivity.class);
		spec = tabHost.newTabSpec("Home").setIndicator("Home", res.getDrawable(R.drawable.ic_tab_info)).setContent(intent);
		tabHost.addTab(spec);

		// add tab
		intent = new Intent().setClass(this, GraphingActivity1.class);
		spec = tabHost.newTabSpec("Graph").setIndicator("Graph", res.getDrawable(R.drawable.ic_tab_editor)).setContent(intent);
		tabHost.addTab(spec);

		// add tab
	    intent = new Intent().setClass(this, SystemInfoActivity.class);
	    spec = tabHost.newTabSpec("systeminfo").setIndicator("SystemInfo", res.getDrawable(R.drawable.ic_tab_info)).setContent(intent);
	    tabHost.addTab(spec);

		// add tab
	    intent = new Intent().setClass(this, LogbookActivity.class);
	    spec = tabHost.newTabSpec("logbookeditor").setIndicator("Logbook", res.getDrawable(R.drawable.ic_tab_logbook)).setContent(intent);
	    tabHost.addTab(spec);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_preferences:
			// Launch Preference activity
			Intent i = new Intent(this, PreferencesActivity.class);
			startActivity(i);
			break;

		case R.id.item_info:
			// show info dialog
			AboutDialog dialog = new AboutDialog(this);
			dialog.show();
			break;
		}
		return true;
	}
}
