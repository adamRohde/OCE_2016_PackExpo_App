package com.boschrexroth.indradroid.service;

import com.boschrexroth.indradroid.ActivityLogin;
import com.boschrexroth.indradroid.Connectable;
import com.boschrexroth.indradroid.IndraDroidApplication;
import com.boschrexroth.indradroid.R;
import com.boschrexroth.mlpi.MlpiConnection;
import com.boschrexroth.mlpi.MlpiException;
import com.boschrexroth.mlpi.MlpiGlobal;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Adam on 9/15/2016.
 */
public class HomeActivity extends Activity {

    private static final String TAG = "indra";
    private static final String HomeTag = HomeActivity.class.getSimpleName();
    private static final String sHomeTesterString = GraphingActivity1.class.getSimpleName();
    private static Boolean bHomeTaskTrig = Boolean.FALSE;

    private UpdateTask m_updateTask;
    private Timer myHomeTimer = new Timer();
    int updateRate = 4000;
    boolean m_running;
    private IndraDroidApplication m_app;
    private ProgressDialog m_progressDialog;
    private String ipAddress_settings;
    private String timeOut_settings;
    private Boolean demoMode_settings;
    private int iMachineSpeedTemp;
    private String sMachineSpeedTemp;
    private String sCullState;
    private String sLotNumber_plc;
    private String sUserSystemStatus_plc; //Application.GVL_IM01_HMI.sUserSystemStatus_HMI_gb_dummy
    private String sMlcDiag_plc; //Application.GVL_IM01_HMI.sMLCDiagString_HMI_gb
    private String sCPM_plc; //Application.GVL_IM01_HMI.iCPM_gb
    public String sError_Count_plc;
    public String sGood_Count_plc;
    public String sTotal_Count;
    private String sCullState_plc;
    //private String sTotal_Count_plc;
    //private String sActual_Time_plc;
    //private String sCmd_Time_plc;
    private String error_code;
    private RelativeLayout mainLayout1;
    private PieChart mChart1;  //Green Pie chart
    private ArrayList<HashMap<String, String>> m_listItems;
    private ArrayAdapter myAdapt;
    private ListView myList;
    TextView tv8;
    TextView tv9;
    TextView tv10;
    private String[] myStringArray = {"MLC State - ","Machine State - ","Machine Speed (CPM) - ", "Lot Number - ", "Cull State - "};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        m_app = (IndraDroidApplication) getApplication();

        Log.i(HomeTag, "in method on Create");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        ipAddress_settings = sp.getString("edittext_preference_ip_address", "");
        timeOut_settings = sp.getString("edittext_preference_timeout", "");
        demoMode_settings = sp.getBoolean("checkbox_preference_demonstration_mode", false);
        mainLayout1 = (RelativeLayout) findViewById(R.id.pieChartActivity1);

        final Button home_machine_speed = (Button) findViewById(R.id.home_machine_speed);
        home_machine_speed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View button) {
                ShowPopUpMachineSpeed(button);
            }
        });

        final Button home_cull = (Button) findViewById(R.id.home_cull);
        home_cull.setOnClickListener(new View.OnClickListener() {
            public void onClick(View button) {
                ShowPopUpCull(button);
            }
        });

        final Button home_count_reset = (Button) findViewById(R.id.home_count_reset);
        home_count_reset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View button) {
                ShowPopUpCountReset(button);
            }
        });

        final Button home_clear_error = (Button) findViewById(R.id.home_clear_error);
        home_clear_error.setOnClickListener(new View.OnClickListener() {
            public void onClick(View button) {
                ShowPopUpClearErrors(button);
            }
        });

        if (demoMode_settings) {
            Toast.makeText(getApplicationContext(), "Demonstration mode!", Toast.LENGTH_LONG).show();
            //Todo
        }

        mChart1 = new PieChart(this);
        mainLayout1.addView(mChart1);
        mainLayout1.setBackgroundColor(Color.TRANSPARENT);
        mChart1.setUsePercentValues(true);
        mChart1.valuesToHighlight();
        mChart1.setCenterTextSize(8f);
        mChart1.setDescription("");
        mChart1.setDrawHoleEnabled(true);
        mChart1.setHoleColor(Color.BLACK);
        mChart1.setHoleRadius(45);
        mChart1.setTransparentCircleColor(Color.BLACK);
        mChart1.setTransparentCircleRadius(47);
        mChart1.setCenterTextColor(Color.BLACK);
        mChart1.setHighlightPerTapEnabled(true);

        ViewGroup.LayoutParams params1 = mChart1.getLayoutParams();
        params1.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params1.width = ViewGroup.LayoutParams.MATCH_PARENT;

        //Legend
        Legend L1 = mChart1.getLegend();
        L1.setEnabled(false);

        bHomeTaskTrig = Boolean.TRUE;

        Log.i(HomeTag, "home be Created!");

        tv8 = (TextView) findViewById(R.id.textView8);
        tv9 = (TextView) findViewById(R.id.textView9);
        tv10 = (TextView) findViewById(R.id.textView10);

        //String[] myStringArray={"MLC State - ","Machine State - ","Machine Speed (CPM) - ","Cull State - "};
        myStringArray[0] = ("MLC State - " + "test");
        myStringArray[1] = ("Machine State - " + "test");
        myStringArray[2] = ("Machine Speed (CPM) - " + "test");
        myStringArray[3] = ("Lot Number - " + "test");
        myStringArray[4] = ("Cull State - " + "test");

        ArrayAdapter<String> myAdapter=new
                ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                myStringArray);

        ListView myList=(ListView) findViewById(R.id.listView);
            myList.setAdapter(myAdapter);


        myAdapt = (ArrayAdapter)myList.getAdapter();
        myAdapt.notifyDataSetChanged();

        updateRate = 4000;
        if (updateRate > 0){
            myHomeTimer = new Timer();
            myHomeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    TimerMethod();
                }
            }, updateRate, updateRate );
        }

        addData_good();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_app.isDebug()) Log.d(TAG, "SystemInfo.onDestroy()");
        MlpiConnection device = new MlpiConnection();
        //stop communication
        device.disconnect();
        Log.i(HomeTag, "home be Destroyed!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(HomeTag, "home be Resumed!");
        bHomeTaskTrig = Boolean.TRUE;
        updateRate = 4000;
        if (updateRate > 0) {
            myHomeTimer = new Timer();
            myHomeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    TimerMethod();
                }
            }, updateRate, updateRate);
        }

    }

    // show progress dialog till first update
    //m_progressDialog = ProgressDialog.show(this, getResources().getString(R.string.activity_connectprogesstitle), getResources().getString(R.string.activity_connectprogess), true);}

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(HomeTag, "home be Paused!");
        myHomeTimer.cancel();
        myHomeTimer.purge();
        m_updateTask.cancel(true);

    }

    public void ShowPopUpMachineSpeed(View v) {
        final AlertDialog.Builder sayWindows = new AlertDialog.Builder(HomeActivity.this);

        sayWindows.setPositiveButton("Speed Up", null);
        sayWindows.setNegativeButton("Close", null);
        sayWindows.setNeutralButton("speed Down", null);
        sayWindows.setTitle("                  Machine Speed");
        final AlertDialog mAlertDialog = sayWindows.create();
        MlpiConnection device = m_app.getDevice();
        //device.connect(ActivityLogin.sIPAddress);
        device.connect("192.168.0.5");
        if (device.isConnected()) {
            sCPM_plc = (device.logic().readVariableBySymbolAsString("Application.GVL_IM01_HMI.iDryRun_CPM_Virtual"));
        }
        mAlertDialog.setMessage("                                   " + sCPM_plc);
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button button_Speed_Up = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button_Speed_Up.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            MlpiConnection device = m_app.getDevice();
                            //device.connect(ActivityLogin.sIPAddress);
                            //device.connect("192.168.0.5");
                            if (device.isConnected()) {
                                sMachineSpeedTemp = device.logic().readVariableBySymbolAsString("Application.GVL_IM01_HMI.iDryRun_CPM_Virtual");
                                iMachineSpeedTemp = Integer.parseInt(sMachineSpeedTemp);
                                iMachineSpeedTemp++;
                                sMachineSpeedTemp = Integer.toString(iMachineSpeedTemp);
                                device.logic().writeVariableBySymbolAsString("Application.GVL_IM01_HMI.iDryRun_CPM_Virtual", sMachineSpeedTemp);
                                mAlertDialog.setMessage("                                   " + sMachineSpeedTemp);
                            } else {
                                mAlertDialog.setMessage("bad connection");
                            }
                        } catch (Exception e) {
                            Context c = v.getContext();
                            Toast.makeText(c, e + "Connection Error UP", Toast.LENGTH_LONG).show();
                        }
                    }

                });

                Button button_Speed_Down = mAlertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                button_Speed_Down.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            MlpiConnection device = m_app.getDevice();
                            //device.connect(ActivityLogin.sIPAddress);
                            //device.connect("192.168.0.5");
                            if (device.isConnected()) {
                                sMachineSpeedTemp = (device.logic().readVariableBySymbolAsString("Application.GVL_IM01_HMI.iDryRun_CPM_Virtual"));
                                iMachineSpeedTemp = Integer.parseInt(sMachineSpeedTemp);
                                iMachineSpeedTemp--;
                                sMachineSpeedTemp = Integer.toString(iMachineSpeedTemp);
                                device.logic().writeVariableBySymbolAsString("Application.GVL_IM01_HMI.iDryRun_CPM_Virtual", sMachineSpeedTemp);
                                mAlertDialog.setMessage("                                   " + sMachineSpeedTemp);
                            } else {
                                mAlertDialog.setMessage("bad connection");
                            }
                        } catch (Exception e) {
                            Context c = v.getContext();
                            Toast.makeText(c, e + "Connection Error Down", Toast.LENGTH_LONG).show();
                        }
                    }

                });
            }

        });

        mAlertDialog.show();
        return;
    }

    public void ShowPopUpCull(View v) {
        final AlertDialog.Builder sayWindows = new AlertDialog.Builder(HomeActivity.this);
        sayWindows.setPositiveButton("Set TRUE", null);
        sayWindows.setNegativeButton("Set FALSE", null);

        MlpiConnection device = m_app.getDevice();
        //device.connect(ActivityLogin.sIPAddress);
        device.connect("192.168.0.5");
        if (device.isConnected()) {
            sCullState_plc = (device.logic().readVariableBySymbolAsString("Application.GVL_IM01_HMI.xCull_VirtualPB"));

            if (sCullState_plc.equals("FALSE")) {
                sCullState = "Cull Not Set";
            }
            if (sCullState_plc.equals("TRUE")) {
                sCullState = "Cull Already Set";
            }
        }
        sayWindows.setTitle("                 Cull Settings");  //Title!
        final AlertDialog mAlertDialog = sayWindows.create();
        mAlertDialog.setMessage("                   " + sCullState);

        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button button_Yes = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button_Yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (sCullState_plc.equals("TRUE")) {

                                mAlertDialog.dismiss();

                            }
                            else
                            {
                                MlpiConnection device = m_app.getDevice();
                                //device.connect(ActivityLogin.sIPAddress);
                                //device.connect("192.168.0.5");
                                if (device.isConnected()) {
                                    device.logic().writeVariableBySymbolAsString("Application.GVL_IM01_HMI.xCull_VirtualPB", "1");
                                    mAlertDialog.dismiss();
                                }
                            }
                        } catch (Exception e) {
                            Context c = v.getContext();
                            Toast.makeText(c, e + "Connection Error", Toast.LENGTH_LONG).show();
                        }
                    }

                });

                Button button_No = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                button_No.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (sCullState_plc.equals("FALSE")) {

                                mAlertDialog.dismiss();

                            }
                            else
                            {
                                MlpiConnection device = m_app.getDevice();
                                //device.connect(ActivityLogin.sIPAddress);
                                //device.connect("192.168.0.5");

                                if (device.isConnected()) {
                                    device.logic().writeVariableBySymbolAsString("Application.GVL_IM01_HMI.xCull_VirtualPB", "0");
                                    mAlertDialog.dismiss();
                                }
                            }

                        } catch (Exception e) {
                            Context c = v.getContext();
                            Toast.makeText(c, e + "Connection Error Down", Toast.LENGTH_LONG).show();
                        }
                    }

                });

            }

        });

        mAlertDialog.show();
        return;
    }

    public void ShowPopUpCountReset(View v) {

        final AlertDialog.Builder sayWindows = new AlertDialog.Builder(HomeActivity.this);

        MlpiConnection device = m_app.getDevice();
        //device.connect(ActivityLogin.sIPAddress);
        device.connect("192.168.0.5");
        device.logic().writeVariableBySymbolAsString("Application.GVL_IM01_HMI.xResetProductCounts_gb", "0");
        sayWindows.setPositiveButton("Yes", null);
        sayWindows.setNegativeButton("Close", null);
        sayWindows.setTitle("       Reset Product Counts?");  //Title!
        sayWindows.setMessage("");
        final AlertDialog mAlertDialog = sayWindows.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button button_yes = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            MlpiConnection device = m_app.getDevice();
                            //device.connect(ActivityLogin.sIPAddress);
                            //device.connect("192.168.0.5");

                            if (device.isConnected()) {
                                device.logic().writeVariableBySymbolAsString("Application.GVL_IM01_HMI.xResetProductCounts_gb", "1");
                                addData_good();
                                mAlertDialog.dismiss();
                            } else {
                               // mAlertDialog.setMessage("bad connection");
                            }
                           // mAlertDialog.dismiss();
                        } catch (Exception e) {
                            Context c = v.getContext();
                            Toast.makeText(c, e + "Connection Error", Toast.LENGTH_LONG).show();
                        }
                    }

                });

                Button button_no = mAlertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            }

        });
        mAlertDialog.show();
        return;
    }

    public void ShowPopUpClearErrors(View v) {

        final AlertDialog.Builder sayWindows = new AlertDialog.Builder(HomeActivity.this);
        MlpiConnection device = m_app.getDevice();
        //device.connect(ActivityLogin.sIPAddress);
        device.connect("192.168.0.5");
        sayWindows.setPositiveButton("Yes", null);
        sayWindows.setNegativeButton("Close", null);
        sayWindows.setTitle("                Clear All Errors?");  //Title!
        sayWindows.setMessage("");
        final AlertDialog mAlertDialog = sayWindows.create();

        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button button_yes = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            MlpiConnection device = m_app.getDevice();
                            //device.connect(ActivityLogin.sIPAddress);
                            //device.connect("192.168.0.5");

                            if (device.isConnected()) {
                                device.system().clearError();
                                    mAlertDialog.dismiss();
                                if (error_code != "") {
                                    mAlertDialog.dismiss();
                                } else {
                                    mAlertDialog.setMessage("                         " + "Error Cleared Failed");
                                }

                            } else {
                                mAlertDialog.setMessage("bad connection");
                            }
                        } catch (Exception e) {
                            Context c = v.getContext();
                            Toast.makeText(c, e + "Connection Error", Toast.LENGTH_LONG).show();
                        }
                    }

                });

                Button button_no = mAlertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            }

        });
        mAlertDialog.show();
        return;
    }

    private void TimerMethod() {

        this.runOnUiThread(Timer_Tick);
    }
    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            m_updateTask = new UpdateTask();
            m_updateTask.execute(myStringArray);
        }
    };

    private class UpdateTask extends AsyncTask<String[], Void , String[]> {

        private boolean m_running = true;

        @Override
        protected String[] doInBackground(String[]... params) {
            String[] values = {"MLC State - ", "Machine State - ", "Machine Speed (CPM) - ", "Lot Number - ", "Cull State - "};
            // read data from dupControl

            if (!m_updateTask.isCancelled()) {
                MlpiConnection device = m_app.getDevice();
                //myList.setAdapter(myAdapter);
                device.connect("192.168.0.5");
                try {

                    String[] temp = {"MLC State - ", "Machine State - ", "Machine Speed (CPM) - ", "Lot Number - ", "Cull State - "};
                    if (device.isConnected()) {

                        temp[0] = "MLC State - " + device.logic().readVariableBySymbolAsString("Application.GVL_IM01_HMI.sMLCDiagString_HMI_gb");
                        values[0] = temp[0];

                        temp[1] = "Machine State - " + device.logic().readVariableBySymbolAsString("Application.GVL_IM01_HMI.iMachState_HMI_gb");
                        values[1] = temp[1];

                        temp[2] = "Machine Speed (CPM) - " + device.logic().readVariableBySymbolAsString("Application.GVL_IM01_HMI.iDryRun_CPM_Virtual");
                        values[2] = temp[2];

                        temp[3] = "Lot Number - " + device.logic().readVariableBySymbolAsString("Application.GVL_IM01_HMI.sLotNumber");
                        values[3] = temp[3];

                        temp[4] = "Cull State - " + device.logic().readVariableBySymbolAsString("Application.GVL_IM01_HMI.xCull_VirtualPB");
                        values[4] = temp[4];

                        sGood_Count_plc = device.logic().readVariableBySymbolAsString("Application.GVL_IM01_HMI.iGoodProducts_gb");
                        sError_Count_plc = device.logic().readVariableBySymbolAsString("Application.GVL_IM01_HMI.iBadProducts_gb");

                        int tempGood = Integer.parseInt(sGood_Count_plc);
                        int tempError = Integer.parseInt(sError_Count_plc);
                        int tempTotal = tempGood + tempError;
                        sTotal_Count = Integer.toString(tempTotal);

                    }
                } catch (Exception exc) {
                    Log.e(TAG, "error in doBackground " + exc.toString());
                }
            }
            else{
                return null;
            }

            return values;
        }

        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            // initialize and set the list adapter
            myStringArray[0] = result[0];
            myStringArray[1] = result[1];
            myStringArray[2] = result[2];
            myStringArray[3] = result[3];
            myStringArray[4] = result[4];

            tv8.setText("Good Count - " + sGood_Count_plc);
            tv9.setText("Bad Count - " + sError_Count_plc);
            tv10.setText("Total - " + sTotal_Count);

//            if (error_code != "") {
//                Toast.makeText(getApplicationContext().getApplicationContext(), error_code, Toast.LENGTH_SHORT).show();
//            }

            ListView myList =
                    (ListView) findViewById(R.id.listView);
            ArrayAdapter myAdapt =
                    (ArrayAdapter) myList.getAdapter();
            myAdapt.notifyDataSetChanged();

            m_running = false;

            addData_good();
        }
    }

    private void addData_good() {
            ArrayList<Entry> yVals1 = new ArrayList<Entry>();
            ArrayList<String> xVals = new ArrayList<String>();

       int tempError;
       int tempGood;
        if (sGood_Count_plc != null){
            tempError = Integer.parseInt(sError_Count_plc);
            tempGood = Integer.parseInt(sGood_Count_plc);
       }
        else
        {
            tempError = 0;
            tempGood = 1;
        }

           // int tempError = Integer.parseInt("100");
           // int tempGood = Integer.parseInt("22");

            String[] sNames_good1 = new String[2];
            sNames_good1[0] = "Bad - " + sError_Count_plc;
            sNames_good1[1] = "Good - " + sGood_Count_plc;

            Integer[] fValues_good1 = new Integer[2];
            fValues_good1[0] = tempError;
            fValues_good1[1] = tempGood;

            //These are the Values/Integers
            yVals1.add(new Entry(fValues_good1[0], 0));
            yVals1.add(new Entry(fValues_good1[1], 1));

            //These are the Names/Strings
            xVals.add(sNames_good1[0]);
            xVals.add(sNames_good1[1]);

            //create pie data set
            PieDataSet dataSet = new PieDataSet(yVals1, "Total Counts");
            dataSet.setSliceSpace(4);
            dataSet.setSelectionShift(1);

            // add many colors
            ArrayList<Integer> colors = new ArrayList<Integer>();
            //colors.add(ColorTemplate.rgb("#2196F3")); //Blue
            colors.add(ColorTemplate.rgb("#F44336")); //Red
            //colors.add(ColorTemplate.getColorWithAlphaComponent(0, 0)); //Transparency
            colors.add(ColorTemplate.rgb("#4CAF50")); //Green
            dataSet.setColors(colors);

            //dataSet.setColors(new int[] { R.color.rexroth_red, R.color.rexroth_green});
            //instantiate pie data object now
            PieData data = new PieData(xVals, dataSet);
            data.setValueFormatter(new PercentFormatter());

            data.setValueTextSize(20f);
            data.setValueTypeface(Typeface.DEFAULT);
            data.setValueTextColor(Color.WHITE);
            data.setDrawValues(false);

            mChart1.setData(data);
            mChart1.highlightValues(null);
            mChart1.invalidate();
        }

}

