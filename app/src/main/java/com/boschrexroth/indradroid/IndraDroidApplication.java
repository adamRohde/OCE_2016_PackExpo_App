package com.boschrexroth.indradroid;


import com.boschrexroth.indradroid.R;
import com.boschrexroth.mlpi.MlpiConnection;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class IndraDroidApplication extends Application {
	private static final String TAG = "indra";
	private static final boolean D = true;
	
	private MlpiConnection m_device;

    //
    //
    // settings
    //  
    //
    public String getAddress() {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		//return settings.getString("device_address", ActivityLogin.sIPAddress);
		return settings.getString("device_address", "192.168.0.5");
    }
    
    public void setAddress(String address) {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.edit().putString("device_address", address).commit();
    }
    
    public MlpiConnection getDevice() {
    	return m_device;
    }
    
	
    public boolean isDebug() {
    	return true;
    }    

	public boolean isAutoConnect() {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean("device_autoconnect", false);
	}

    //
    //
    // Events
    //
    //
    
    @Override
    public void onCreate() {
    	if (D) Log.d(TAG, "APP: onCreate()");
		
		//
		// Create Interface
		//
		m_device = new MlpiConnection();
    }

    @Override
    public void onTerminate() {
    	if (D) Log.d(TAG, "APP: onTerminate()");
    }
    //
    //
    // Communication
    //
    //
    
    public void connect(Connectable connectable) {
    	//
		// load settings
		//
		String address = ActivityLogin.sIPAddress;
		//
		// establish connection
		//
		//
		// connect backend in own thread
		//
		ConnectTask connectTask = new ConnectTask(m_device, connectable);
		connectTask.execute("192.168.0.5");
    }
    
    public void disconnect(Connectable connectable) {
    	//
		// disconnect backend
		//
    	Log.d(TAG, "Disconnect");
    	m_device.disconnect();
    }

    private class ConnectTask extends AsyncTask<String, Void, Boolean> {
		private MlpiConnection m_device;
		private Connectable m_connectable;
    	
    	public ConnectTask(MlpiConnection backend, Connectable connectable) {
    		m_device = backend;
			m_connectable = connectable;
		}
    	
    	@Override
		protected Boolean doInBackground(String... params) {
    		String address = ActivityLogin.sIPAddress;
    		try {
    			//m_device.connect(address);
				m_device.connect("192.168.0.5");

    		} catch(Exception exc) {
    			Log.d(TAG, "Connect failed with: " + exc.getMessage());
    			return false;
    		}
   			return true;
		}
		@Override
		protected void onPreExecute() {
		};
		@Override
		protected void onPostExecute(Boolean result) {
			m_connectable.onConnected(result);
		};
	}

}
