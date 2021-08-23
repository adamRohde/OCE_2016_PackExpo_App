package com.boschrexroth.indradroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.boschrexroth.indradroid.service.HomeActivity;


public class ActivityLogin extends Activity implements Connectable {
	private static final String TAG = "indra";
	public static String sIPAddress;
	private IndraDroidApplication m_app = null;
	private ProgressDialog m_progressDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_login);
    	
        // get reference to application singleton
        m_app = (IndraDroidApplication)getApplication();
        if (m_app.isDebug()) Log.d(TAG, "LoginActivity.onCreate()");

        // init gui
        EditText editAddress = (EditText)findViewById(R.id.editAddress);
        editAddress .setText(m_app.getAddress());

    	// init callbacks
    	Button button = (Button)findViewById(R.id.buttonConnect);
        button.setOnClickListener(new OnClickListener() {
    	    public void onClick(View v) {
    	    	EditText editAddress  = (EditText)findViewById(R.id.editAddress);
    	    	//String address = editAddress.getText().toString();
				//sIPAddress = editAddress.getText().toString();
				m_app.setAddress("192.168.0.5");
				//m_app.setAddress(sIPAddress);
    	    	connect();
   	    	}
   	    });

        if (m_app.isAutoConnect()) {
        	connect();
        }
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	if (m_app.isDebug()) Log.d(TAG, "LoginActivity.onDestroy()");
    }
    
	@Override
	protected void onPause() {
		super.onPause();
		
        // stop communication
        m_app.disconnect(this);
	}
	

    private void connect() {
        // show progress dialog till first update
        m_progressDialog = ProgressDialog.show(this, getResources().getString(R.string.activity_connectprogesstitle), getResources().getString(R.string.activity_connectprogess), true);
		m_app.setAddress("192.168.0.5");
		m_app.connect(ActivityLogin.this);
		//m_app.connect("192.168.0.5");

    }
    
    public void onConnected(boolean success) {
    	// dismiss the progress dialog
		if (m_progressDialog != null)
			if (m_progressDialog.isShowing())
				m_progressDialog.dismiss();

		// start visualization activity if connected
		if (success){
	    	Intent intent = new Intent(ActivityLogin.this, IndraDroidTabActivity.class);
	    	startActivity(intent);
		}else{
			new AlertDialog.Builder(ActivityLogin.this).setMessage( getResources().getString(R.string.activity_login_connecterror)).show();
		}
    	
    }

}