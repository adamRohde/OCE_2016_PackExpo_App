package com.boschrexroth.indradroid.service;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.boschrexroth.indradroid.Connectable;
import com.boschrexroth.indradroid.IndraDroidApplication;
import com.boschrexroth.indradroid.R;
import com.boschrexroth.mlpi.MlpiConnection;
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

public class LogbookActivity extends ListActivity implements Connectable {
	private static final String TAG = "indra";
	private static final int UPDATE_INTERVAL = 10000;
	
	private IndraDroidApplication m_app;
	
	private ProgressDialog m_progressDialog;
	private UpdateTask m_updateTask;
	private ArrayList<HashMap<String, String>> m_listItems;
	private BaseAdapter m_adapter;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_logbook);
        
        // get reference to application singleton
        m_app = (IndraDroidApplication)getApplication();
        if (m_app.isDebug()) Log.d(TAG, "ListActivity.onCreate()");
        
        // connect ListView to data
        m_listItems = new ArrayList< HashMap<String, String> >();
               
        m_adapter = new SimpleAdapter(
				this,
				m_listItems,
				R.layout.list_item_logbook,
				new String[]{"Text", "Additional"},
				new int[]{R.id.textName, R.id.textAdditional});

		this.setListAdapter(m_adapter);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
    	if (m_app.isDebug()) Log.d(TAG, "ListActivity.onDestroy()");
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

			// read data from dupControl
			MlpiConnection device = m_app.getDevice();
			try {
				HashMap<String, String> temp;
				
				
				long newestIndex = device.system().getNewestDiagnosisIndex();
				long oldestIndex = device.system().getOldestDiagnosisIndex();
				Diagnosis[] diagnosis = device.system().getDiagnosisLog(newestIndex, newestIndex-oldestIndex);
				
				for (Diagnosis diag : diagnosis) {
					temp = new HashMap<String, String>();
					temp.put("Text", String.format("0x%08X", diag.number) + " - " + diag.text);
					temp.put("Additional", String.format("%02d.%02d.%02d %02d:%02d.%02d"
							, diag.dateTime.day
							, diag.dateTime.month
							, diag.dateTime.year
							, diag.dateTime.hour
							, diag.dateTime.minute
							, diag.dateTime.second)
					+ " - " + diag.despatcher.toString() + " - " + String.valueOf(diag.logicalAddress)		
					);
					
					/*temp.put("Index", String.valueOf(diag.index));
					temp.put("DateAndTime", String.format("%02d.%02d.%02d %02d:%02d.%02d"
							, diag.dateAndTime.day
							, diag.dateAndTime.month
							, diag.dateAndTime.year
							, diag.dateAndTime.hour
							, diag.dateAndTime.minute
							, diag.dateAndTime.second));
					temp.put("Despatcher", diag.despatcher.toString());
					temp.put("LogicalAddress", String.valueOf(diag.logicalAddress));
					*/
					values.add(temp);
				}
					
			        //this.publishProgress(values);
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
