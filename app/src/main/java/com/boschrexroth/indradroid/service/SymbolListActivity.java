package com.boschrexroth.indradroid.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.boschrexroth.indradroid.Connectable;
import com.boschrexroth.indradroid.IndraDroidApplication;
import com.boschrexroth.indradroid.R;
import com.boschrexroth.mlpi.MlpiConnection;
import com.boschrexroth.mlpi.Logic.ApplicationInfo;
import com.boschrexroth.mlpi.System.Diagnosis;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SymbolListActivity extends ListActivity implements Connectable {
	private static final String TAG = "indra";
	private static final int UPDATE_INTERVAL = 1000;
	
	private IndraDroidApplication m_app;
	
	private ProgressDialog m_progressDialog;
	private UpdateTask m_updateTask;
	private ArrayList<HashMap<String, String>> m_listItems;
	private BaseAdapter m_adapter;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_symbollist);
        
        // get reference to application singleton
        m_app = (IndraDroidApplication)getApplication();
        if (m_app.isDebug()) Log.d(TAG, "SymbolListActivity.onCreate()");
        
        // connect ListView to data
        m_listItems = new ArrayList< HashMap<String, String> >();
               
        m_adapter = new SimpleAdapter(
				this,
				m_listItems,
				R.layout.list_item_symbollist,
				new String[]{"symbolName"},
				new int[]{R.id.symbolName});

		this.setListAdapter(m_adapter);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
    	if (m_app.isDebug()) Log.d(TAG, "SymbolListActivity.onDestroy()");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
        // start communication
        m_app.connect(this);
        
        // show progress dialog till first update
        m_progressDialog = ProgressDialog.show(this, getResources().getString(R.string.activity_connectprogesstitle), getResources().getString(R.string.activity_connectprogess), true);

	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		
        // stop update task
        if (m_updateTask!=null) {
        	m_updateTask.stop();
        	m_updateTask = null;
        }
        
        // stop communication
        m_app.disconnect(this);
	}
	
	
	public void onConnected(boolean success) {
		
		if (success) {
	        // start update task
	        m_updateTask = new UpdateTask();
	        m_updateTask.execute(m_listItems);
		} else {
			if (m_progressDialog != null)
				if (m_progressDialog.isShowing())
					m_progressDialog.dismiss();
			
			Toast.makeText(this, "Connection Error!", Toast.LENGTH_LONG).show();
		}
		
	}

	/*
     * update task for reading data from control 
     */
    private class UpdateTask extends AsyncTask< List<HashMap<String, String>>, List<HashMap<String, String>>, List<HashMap<String, String>>> {
    	
    	private boolean m_running = true;
    	
		@Override
		protected List<HashMap<String, String>> doInBackground( List<HashMap<String, String>> ... params) {
			List<HashMap<String, String>> values = new ArrayList<HashMap<String, String>>();

			// read data from device
			MlpiConnection device = m_app.getDevice();
			try {
				HashMap<String, String> temp;
				
				String name = device.logic().getNameOfApplication(0);
				String[] symbolList = device.logic().applications(name).getSymbolsOfApplication();

				for (String symbol : symbolList) {
					temp = new HashMap<String, String>();
					temp.put("symbolName", symbol);
					values.add(temp);
				}
			}
			catch (Exception exc) {
				Log.e(TAG, "error in doBackground " + exc.toString());
			}
				
			return values;
		}
		
		@Override
		protected void onPostExecute(List<HashMap<String, String>> result) {
			super.onPostExecute(result);
			
			m_listItems.clear();
			m_listItems.addAll(result);
			
			m_adapter.notifyDataSetChanged();
			
			if (m_progressDialog != null)
				if (m_progressDialog.isShowing())
					m_progressDialog.dismiss();
		}			
				
		@Override
		protected void onProgressUpdate( List<HashMap<String, String>>[] values) {
			if (!m_running) {
				return;
			}
			
			//m_adapter.notifyDataSetChanged();
		};
		
		public void stop() {
	        m_running = false;
	    }

	}
    
}
