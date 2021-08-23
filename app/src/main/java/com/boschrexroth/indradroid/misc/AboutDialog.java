package com.boschrexroth.indradroid.misc;

import java.text.SimpleDateFormat;

import com.boschrexroth.indradroid.R;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutDialog extends Dialog {

	public AboutDialog(Context context){
		super(context);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set gui
        setContentView(R.layout.dialog_about);
        setTitle("About IndraDroid");
        
        
        
        // get app info
        String infoString;
        PackageManager manager = this.getContext().getPackageManager();
        PackageInfo info;
		try {
			info = manager.getPackageInfo(this.getContext().getPackageName(), 0);
			infoString = "PackageName = " + info.packageName 
	        		+ "\nVersionCode = " + info.versionCode 
	        		+ "\nVersionName = " + info.versionName 
	        		+ "\nLast Update = " + new SimpleDateFormat("dd.MM.yyyy").format(info.lastUpdateTime);
		} catch (NameNotFoundException e) {
			infoString = "Failed to get info!";
		}
        
        TextView txtInfo = (TextView) findViewById(R.id.textAppInfo);
        txtInfo.setText(infoString);
        
        
        // close dialog on button click
        Button buttonOk = (Button) findViewById(R.id.button_ok);
        buttonOk.setOnClickListener(new android.view.View.OnClickListener() {
        	public void onClick(View v) {
        		dismiss();
        	}
        });

	}
}
