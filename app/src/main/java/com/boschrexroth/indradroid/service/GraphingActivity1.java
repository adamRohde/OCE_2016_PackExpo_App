package com.boschrexroth.indradroid.service;

import com.boschrexroth.indradroid.ActivityLogin;
import com.boschrexroth.indradroid.Connectable;
import com.boschrexroth.indradroid.IndraDroidApplication;
import com.boschrexroth.indradroid.R;
import com.boschrexroth.mlpi.Logic;
import com.boschrexroth.mlpi.MlpiConnection;
import com.boschrexroth.mlpi.MlpiException;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Adam on 9/15/2016.
 */

public class GraphingActivity1 extends Activity {

    private static final String TAG = "indra";
    private static final String GraphTag=GraphingActivity1.class.getSimpleName();
    private static final String sGraphTesterString=GraphingActivity1.class.getSimpleName();
    private static Boolean bGraphTaskTrig = Boolean.FALSE;
    private static final int UPDATE_INTERVAL = 10000;
    private UpdateTask_graphing m_updateTask_graphing;
    private String error_code;
    private Boolean demoMode_settings;
    private String  ipAddress_settings;
    private String  timeOut_settings;
    private Timer myGraphTimer = new Timer();
    private int updateRate = 500;
    private String sActualTorque_plc;
    private String sActualTemp_plc;
    private float torque;
    private float temperature;

    private GraphViewData[] data1 = new GraphViewData[100];
    private GraphViewData[] data2 = new GraphViewData[100];
    private GraphViewData[] data3 = new GraphViewData[100];
    private float[] array1_float = new float[100];
    private float[] array2_float = new float[100];
    private float[] array3_float = new float[100];
    private byte[] array1 = new byte[400];
    private byte[] array2 = new byte[400];
    private byte[] array3 = new byte[400];
    TextView tv_temp;

    private double test=0;
    View v;
    private IndraDroidApplication m_app;
    private ProgressDialog m_progressDialog;
    //private UpdateTask m_updateTask;
    private ArrayList<HashMap<String, String>> m_listItems;
    private BaseAdapter m_adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Log.i(GraphTag, "in method on Create");

        m_app = (IndraDroidApplication) getApplication();

        tv_temp = (TextView) findViewById(R.id.textView_temp);

        bGraphTaskTrig = Boolean.TRUE;
        if (bGraphTaskTrig) {
            Log.i(sGraphTesterString, "Graph Tester Bit Be Created!");
        }
        updateRate = 500;
        if (updateRate > 0) {
            myGraphTimer = new Timer();
            myGraphTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    TimerMethod();
                }
            }, updateRate, updateRate);
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        myGraphTimer.cancel();
        myGraphTimer.purge();
        Log.i(GraphTag, "Graph be Destroyed!");
        m_updateTask_graphing.cancel(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(sGraphTesterString, "Graph Tester Bit Be Resumed ON!");
        updateRate = 500;
        if (updateRate > 0) {
            myGraphTimer = new Timer();
            myGraphTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    TimerMethod();
                }
            }, updateRate, updateRate);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        myGraphTimer.cancel();
        myGraphTimer.purge();
        Log.i(sGraphTesterString, "Graph Tester Bit Be Paused!");
        m_updateTask_graphing.cancel(true);
    }

    private void TimerMethod()
    {
        this.runOnUiThread(Timer_Tick);
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            m_updateTask_graphing = new UpdateTask_graphing();
            m_updateTask_graphing.execute();
            m_updateTask_graphing.isCancelled();
        }
    };

    private class UpdateTask_graphing extends AsyncTask<Void, Void , Void> {


        protected Void doInBackground(Void... params) {
            error_code = "";
            demoMode_settings = Boolean.FALSE;
//                if (demoMode_settings) {
//                    for (int i = 0; i < 100; i++) {
//                        //array1_float[i] = 35.0f + (float) Math.random();
//                        //array2_float[i] = 35.0f + (float) Math.random();
            if (!m_updateTask_graphing.isCancelled()) {

                try {
                    MlpiConnection device = new MlpiConnection();
                    //Mlc.connect(ActivityLogin.sIPAddress);
                    device.connect("192.168.0.5");
                    if (device.isConnected()) {

                        Logic.ApplicationMemoryArea temp = (Logic.ApplicationMemoryArea.MEMORY_AREA_MARKER);

                        array1 = device.logic().applications("Application").readMemoryAreaAsByteArray(temp, 1000, 400);
                        array2 = device.logic().applications("Application").readMemoryAreaAsByteArray(temp, 3000, 400);
                        array3 = device.logic().applications("Application").readMemoryAreaAsByteArray(temp, 5000, 400);

                        for (int i = 0; i < 100; i++) {
                            array1_float[i] = bytearray2float(array1[4 * i + 3], array1[4 * i + 2], array1[4 * i + 1], array1[4 * i]);
                            array2_float[i] = bytearray2float(array2[4 * i + 3], array2[4 * i + 2], array2[4 * i + 1], array2[4 * i]);
                            array3_float[i] = bytearray2float(array3[4 * i + 3], array3[4 * i + 2], array3[4 * i + 1], array3[4 * i]);
                        }

                        sActualTorque_plc = (device.logic().readVariableBySymbolAsString("Application.GVL_IM01_HMI.DieCutter_Out.rActualTorque"));
                        sActualTemp_plc = (device.logic().readVariableBySymbolAsString("Application.GVL_CM01_DIECUTTER_HMI.rDieCutterMotorTemp"));

                        torque = Float.parseFloat(sActualTorque_plc);
                        temperature = Float.parseFloat(sActualTemp_plc);

                        for (int i = 0; i < 100; i++) {
                            array1_float[i] = 35.0f + (float) Math.random();
                        }
                    }
                } catch (MlpiException e) {
                    error_code = e.getMessage();
                }
            }
            else{
                return null;
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            // initialize and set the list adapter
            try {

                temperature = Float.parseFloat(sActualTemp_plc);
                DrawGraph_1();
                DrawGraph_3(temperature);

                tv_temp.setText(sActualTemp_plc);

                if (error_code != "") {
                    Toast.makeText(v.getContext(), error_code, Toast.LENGTH_SHORT).show();
                }
            } catch (IllegalStateException e) {
                return;
            } catch (NullPointerException e) {
                return;
            }

        }

        ;
    }

//Motor Torque
    private void DrawGraph_1(){
        for (int i=0; i<100; i++) {
            test = (double)array1_float[i];
            data1[i] = new GraphViewData(i, test);
        }
        GraphViewSeries gvs1 = new GraphViewSeries(data1);

        GraphView graph1 = new LineGraphView(this, "Torque, In Process [%]"); //create the object
        graph1.addSeries(gvs1);

        LinearLayout layout1 = (LinearLayout) findViewById(R.id.graph1); //get the view
        graph1.setTitle("Actual Torque %"); //Set Title of object
        //layout1.removeAllViews();
        layout1.addView(graph1);

        //graph1.addSeries(new GraphViewSeries(data1)); // Not sure if I need this
    }


//Motor Temp
    private void DrawGraph_3(float temperature){
        for (int i=0; i<100; i++) {
            test = (double) array1_float[i];
            data3[i] = new GraphViewData(i, temperature);
        }
        GraphViewSeries gvs = new GraphViewSeries(data3);
        //GraphView graphView = new LineGraphView(v.getContext(), "Torque, In Process [%]");
        LinearLayout layout3 = (LinearLayout) findViewById(R.id.graph3);
        GraphView graph3 = new LineGraphView(this, "Torque, In Process [%]");
        graph3.setTitle("Motor Temp");
        graph3.addSeries(new GraphViewSeries(data3));
        //graph1.addSeries(gvs); // data
       // layout3.removeAllViews();
        layout3.addView(graph3);
    }

    public static float bytearray2float(byte b0, byte b1, byte b2, byte b3) {
        byte[] b = new byte[4];
        b[0] = b0;
        b[1] = b1;
        b[2] = b2;
        b[3] = b3;
        ByteBuffer buf = ByteBuffer.wrap(b);
        return buf.getFloat();
    }

}

