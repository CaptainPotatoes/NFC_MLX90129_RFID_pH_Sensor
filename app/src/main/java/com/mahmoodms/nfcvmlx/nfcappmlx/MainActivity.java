package com.mahmoodms.nfcvmlx.nfcappmlx;

import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by mahmoodms on 11/26/2016.
 */

public class MainActivity extends Activity {
    private String TAG = "MainActivity";
    //Plot Stuff:
    private XYPlot xyPlot;
    private SimpleXYSeries nfcDataSensor0;
    private SimpleXYSeries nfcDataSensor1;
    private Redrawer redrawer;
    private final int HISTORY_DATAPOINTS = 150;
    // NFC Stuff:
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private TextView mNFCText;
    private Tag mCurrentTag;
    // Constants:
    public static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    // Buttons:
    private Button mViewConfigButton;
    private Button mSetConfigButton;

    public final static ColorDrawable actionBarTheme = new ColorDrawable(Color.parseColor("#A9A9A9"));
    private int timerPeriod = 530;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageManager pm = getPackageManager();
        setContentView(R.layout.main_nfc_activity);
        //Flag to keep screen on (stay-awake):
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (getActionBar()!=null) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            ActionBar actionBar = getActionBar();
            actionBar.setBackgroundDrawable(actionBarTheme);
        }
        mNFCText = (TextView)findViewById(R.id.nfc_data);
        mViewConfigButton = (Button) findViewById(R.id.viewConfig);
        mSetConfigButton = (Button) findViewById(R.id.writeConfig);
        //Plot stuff:
        xyPlot = (XYPlot) findViewById(R.id.dataPlot);
        nfcDataSensor0 = new SimpleXYSeries("pH Sensor Data");
//        nfcDataSensor1 = new SimpleXYSeries("pH Sensor Data");
        nfcDataSensor0.useImplicitXVals();
//        nfcDataSensor1.useImplicitXVals();
        //Todo: Using explicit x values: (over 30s)

        xyPlot.setRangeBoundaries(0,1.2, BoundaryMode.FIXED);
        xyPlot.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
        xyPlot.setRangeStepValue(0.3);

        xyPlot.setDomainBoundaries(0, HISTORY_DATAPOINTS, BoundaryMode.FIXED);
        xyPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
        xyPlot.setDomainStepValue(HISTORY_DATAPOINTS/10);

        xyPlot.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
        xyPlot.setDomainLabel("Time (seconds)");
        xyPlot.getDomainLabelWidget().pack();
        xyPlot.setRangeLabel("Voltage (V)");
        xyPlot.getRangeLabelWidget().pack();
        xyPlot.setRangeValueFormat(new DecimalFormat("#.#"));
        xyPlot.setDomainValueFormat(new DecimalFormat("#"));
        xyPlot.getDomainLabelWidget().getLabelPaint().setColor(Color.BLACK);
        xyPlot.getDomainLabelWidget().getLabelPaint().setTextSize(20);
        xyPlot.getRangeLabelWidget().getLabelPaint().setColor(Color.BLACK);
        xyPlot.getRangeLabelWidget().getLabelPaint().setTextSize(20);
        xyPlot.getGraphWidget().getDomainTickLabelPaint().setColor(Color.BLACK);
        xyPlot.getGraphWidget().getRangeTickLabelPaint().setColor(Color.BLACK);
        xyPlot.getGraphWidget().getDomainTickLabelPaint().setTextSize(28); //TODO: was 36
        xyPlot.getGraphWidget().getRangeTickLabelPaint().setTextSize(28);
        xyPlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.WHITE);
        xyPlot.getGraphWidget().getRangeGridLinePaint().setColor(Color.WHITE);
        xyPlot.getLegendWidget().getTextPaint().setColor(Color.BLACK);
        xyPlot.getLegendWidget().getTextPaint().setTextSize(20);
        xyPlot.getTitleWidget().getLabelPaint().setTextSize(20);
        xyPlot.getTitleWidget().getLabelPaint().setColor(Color.BLACK);
        
        LineAndPointFormatter lineAndPointFormatter1 = new LineAndPointFormatter(Color.BLACK, null, null, null);
        lineAndPointFormatter1.getLinePaint().setStrokeWidth(3);
        xyPlot.addSeries(nfcDataSensor0, lineAndPointFormatter1);

//        LineAndPointFormatter lineAndPointFormatter2 = new LineAndPointFormatter(Color.BLUE, null, null, null);
//        lineAndPointFormatter1.getLinePaint().setStrokeWidth(3);
//        xyPlot.addSeries(nfcDataSensor1, lineAndPointFormatter2);

        redrawer = new Redrawer(Arrays.asList(new Plot[]{xyPlot}),100,false);

        if(!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            mNFCText.setText("NFC UNAVAILABLE!");
        } else {
            mAdapter = NfcAdapter.getDefaultAdapter(this);
            mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
            if(mAdapter.isEnabled()) {
                IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
                mFilters = new IntentFilter[] {ndef,};
                mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };
                mNFCText.setText("NFC enabled");
            } else {
                mNFCText.setText("NFC disabled");
            }
        }
        mViewConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                Intent intent = new Intent(context, ViewConfig.class);
                startActivity(intent);
            }
        });
        mSetConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                Intent intent = new Intent(context, WriteConfig.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume()
    {
        // TODO Auto-generated method stub
        redrawer.start();
        //Used for DEBUG : Log.v("NFCappsActivity.java", "ON RESUME NFC APPS ACTIVITY");
        mPendingIntent = PendingIntent.getActivity(this, 0,new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        if(!logFileInitialized) {
            exportLogFile(true,"");
        }
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        // TODO Auto-generated method stub
        //Used for DEBUG : Log.v("NFCappsActivity.java", "ON PAUSE NFC APPS ACTIVITY");
        redrawer.pause();
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onDestroy() {
        redrawer.finish();
        super.onDestroy();
    }

    private void testFunction() {
        byte[] relevantData = {(byte) 0x04, (byte) 0xC0};
        byte unmaskedMSB = (byte)(relevantData[1] & 0b00111111);
        byte[] fixedTimeStamp = {relevantData[0],unmaskedMSB};
        int int1 = bytesWordToInt(fixedTimeStamp);
        int int2 = bytesWordToIntAlt(fixedTimeStamp);
        Log.e(TAG, "Test1: "+String.valueOf(int1));
        Log.e(TAG, "Test2: "+String.valueOf(int2));
    }

    public String getTimeStamp() {
        return new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date());
    }

    private boolean logFileInitialized = false;
    private File root;
    private File logFile;

    private void exportLogFile(boolean init, String data) {
        if(init) {
            Log.i("exportLogFile", "generated Log file");
            root = Environment.getExternalStorageDirectory();
            File dir = new File(root+"/RFIDDataLogs");
            boolean mkdirsA = dir.mkdirs();
            logFile = new File(dir,"Log_"+getTimeStamp()+".csv");
//            logFile = new File(dir,"Log_"+getTimeStamp()+".txt");
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            logFileInitialized = true;
        } else {
            if(logFile.exists()) {
                try {
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile,true));
                    bufferedWriter.append(data);
                    bufferedWriter.newLine();
                    bufferedWriter.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean timestampsEnabled = false;
    private boolean sensor0Enabled = false;
    private boolean sensor1Enabled = false;
    private int[] timeStampData = new int[100];
    private int[] sensor0Data = new int[256];
    private int sensor0Index;
    private int lastPlottedIndex;
    private int[] sensor1Data = new int[100];
    private BoundaryMode currentBM = BoundaryMode.AUTO;
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            mCurrentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] id = mCurrentTag.getId();
            String hexID = "TAGID: "+toHexString(id);
            Log.e("LOG HEX ID",hexID);
            mNFCText.setText(hexID);
            for (String s:mCurrentTag.getTechList()) {
                if(s.equals(NfcV.class.getName())) {
                    final NfcV nfcVTag = NfcV.get(mCurrentTag);
                    try {
                        nfcVTag.connect();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Could Not Connect To NFC Device", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //TODO: Read Timer Config and Adjust timerPeriod AAR.
                    //Read register 0x09 to see which sensors are enabled
                    byte[] dmaConfig = tranceiveReadEEPROM(nfcVTag, (byte)0x09);
                    if(dmaConfig.length>2) {
                        timestampsEnabled = ( (dmaConfig[2] & 0b10000000) == 0b10000000);
                        sensor0Enabled = ( (dmaConfig[2] & 0b00010000) == 0b00010000);
                        sensor1Enabled = ( (dmaConfig[2] & 0b00100000) == 0b00100000);
                        Log.e(TAG,"Enabled: "+Boolean.toString(timestampsEnabled)+" "+Boolean.toString(sensor0Enabled)+" "+Boolean.toString(sensor1Enabled));
                    }
//                    exportLogFile(false, "Connected at: "+getTimeStamp()+"\r\n");
                    /*if(!timestampsEnabled) {
                        //TODO: USE IMPLICIT X VALUES!
                    }*/
                    //Check which address they are being stored at:


                    sensor0Index = 0;
                    //TODO: CHECK IF BITMASKS ENABLED (needs to have 2+ of either Timestamps, S0, S1 or S2 enabled)
                    class graphData implements Runnable {
                        @Override
                        public void run() {
                            if(nfcVTag.isConnected()) {
                                final byte[] readDataAddress = tranceiveReadInternal(nfcVTag, (byte)0x05);
                                delayMS(20);
                                Log.e(TAG, "readDataAddress = 0x"+ViewConfig.toHexStringBigEndian(readDataAddress));
                                if(readDataAddress.length>2) {
                                    Log.e("byte 1:",Integer.toHexString((int) readDataAddress[0]));
                                    Log.e("byte 2:",Integer.toHexString((int) readDataAddress[1]));
                                    Log.e("byte 3:",Integer.toHexString((int) readDataAddress[2]));
                                }
                                if(readDataAddress.length>2) {
                                    byte[] readEEPROM = tranceiveReadEEPROM(nfcVTag, readDataAddress[1]);
                                    if(readEEPROM.length>2) {
                                        byte[] sensor0Datapoint = {readEEPROM[1], readEEPROM[2]};
                                        sensor0Data[sensor0Index] = bytesWordToIntAlt(sensor0Datapoint);
                                        updateIonSensorData(0, sensor0Data[sensor0Index]);
                                        Log.e(TAG,"INTVAL S0: "+String.valueOf(sensor0Data[sensor0Index]));
                                        sensor0Index++;
                                    } else {
                                        Log.e(TAG, "ReadData = 0x"+ ViewConfig.toHexStringBigEndian(readEEPROM));
                                    }
                                    //TODO: Add condition: only if LOOP enabled; otherwise just stop @ 0xD7
                                    Log.e(TAG,"CurrAddr:[0x"+ ViewConfig.toHexStringLittleEndian(readDataAddress)+"]");
                                    if(readDataAddress[1]!=(byte)0xFF) {
//                                        final byte[] readDataAddress0 = tranceiveReadInternal(nfcVTag, (byte)0x05);
//                                        readDataAddress[1] = readDataAddress0[1];
//                                        Log.e("byte 2:","readDataAddress: "+ Integer.toHexString((int) readDataAddress[1]));
//                                        readDataAddress[1]++;
                                    } else {
//                                        final byte[] readDataAddress0 = tranceiveReadInternal(nfcVTag, (byte)0x05);
//                                        readDataAddress[1] = readDataAddress0[1];
//                                        Log.e("byte 2:","readDataAddress: "+ Integer.toHexString((int) readDataAddress[1]));
                                        sensor0Index=0;
//                                        readDataAddress[1] = (byte)0x29;
                                    }
                                }
                            }
                        }
                    }
                    if(!initExecutor) {
                        //TODO: Change (350) to a variable, so the app is more modular.
                        executor.scheduleAtFixedRate(new graphData(), 0, timerPeriod, TimeUnit.MILLISECONDS);
                        initExecutor = true;
                    }
                }
            }
        }
    }

    private double dataVoltage = 0;
    private static final int MAXVAL = 65536;//16384

    private void updateIonSensorData(final int sensor, final int value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(sensor==0) {
                    if(nfcDataSensor0.size()>HISTORY_DATAPOINTS) {
                        nfcDataSensor0.removeFirst();
                    }
                    double temp = (double)value/MAXVAL;
                    dataVoltage = (temp*1.2);
                    exportLogFile(false, String.valueOf(dataVoltage));
                    nfcDataSensor0.addLast(null, dataVoltage);
                } else if(sensor==1) {
                    if(nfcDataSensor1.size()>HISTORY_DATAPOINTS) {
                        nfcDataSensor1.removeFirst();
                    }
                    double temp = (double)value/MAXVAL;
                    dataVoltage = (temp*1.2);
                    nfcDataSensor1.addLast(null, dataVoltage);
                } else {
                    //3 is timestamps.
                    //Unused for the timebeing.
                }
            }
        });
    }
    private boolean startedGraphingData = false;
    private boolean initExecutor = false;
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private void stopReceiveDataAndGraph() {
        startedGraphingData = false;
    }

    private void startGraph() {
//        executor.scheduleAtFixedRate(new graphData(), 0, 1000, TimeUnit.MILLISECONDS);
        startedGraphingData = true;
    }

    public static void delayMS(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Log.e("InterruptedException",e.toString());
        }
    }

    class retrieveDataAndGraph implements Runnable {
        @Override
        public void run() {
            NfcV nfcVTag = NfcV.get(mCurrentTag);
            if(startedGraphingData) {
                Log.i(TAG, "Started Graphing Data");
                if(nfcVTag.isConnected()) {
                    byte[] id = mCurrentTag.getId();
                    String hexID = "TAGID: "+toHexString(id);
                    Log.e("SubClass","retrieveDataAndGraph HEX ID:"+hexID);
                } else {
                    stopReceiveDataAndGraph();
                }
            }
        }
    }

    /**
     * @param nfcVTag Unique NFC Tag
     * @param address Memory Address to Read
     * @return response (response from tranceive function)
     */
    protected byte[] tranceiveReadEEPROM(NfcV nfcVTag, byte address) {
        byte[] addr = {address};
        byte[] response = {(byte)0xFF};
        try {
            byte[] WriteSingleBlockFrame = { (byte) 0x02, (byte) 0x20, address };
            response = nfcVTag.transceive(WriteSingleBlockFrame);
            if(response[0] == (byte) 0x00 /*|| response[0] == (byte) 0x01*/) {
                //response 01 = error sent back by tag (new Android 4.2.2) or BC
//                Log.i(TAG,"SUCCESS: Read EEPROM[0x"+toHexStringLittleEndian(addr)+"] = "+String.valueOf(response[0]));
            }
            return response;
        } catch(Exception e) {
            Log.e(TAG,"Fail: Read EEPROM[0x"+ViewConfig.toHexStringLittleEndian(addr)+"]");
            return response;
        }
    }

    protected byte[] tranceiveReadInternal(NfcV nfcVTag, byte addr) {
        byte[] response = {(byte)0xFF};
        try{
            byte[] ReadSingleBlockFrame = {(byte)0x03, (byte)0xA2, (byte) 0x1F, addr};
            response = nfcVTag.transceive(ReadSingleBlockFrame);
            if(response[0] == (byte)0x00) {
                Log.i(TAG, "Success: Read Internal Memory");
            } else {
                Log.i(TAG, "Fail: Read Internal Memory");
            }
            return response;
        } catch (Exception e) {
            Log.e(TAG, "NFC Tranceive Error");
            return response;
        }
    }

    private int bytesWordToInt(byte[] b) {
        return ((b[0] & 0xFF) << 8) | (b[1] & 0xFF);
    }

    private int bytesWordToIntAlt(byte[] b) {
        return ((b[1] & 0xFF) << 8) | (b[0] & 0xFF);
    }

    private int findSmallestValue(int[] array) {
        int min = array[0];
        for (int i = 1; i < array.length; i++) {
            if(array[i]<min) {
                min = array[i];
            }
        }
        return min;
    }

    private double findSmallestValue(double[] array) {
        double min = array[0];
        for (int i = 1; i < array.length; i++) {
            if(array[i]<min) {
                min = array[i];
            }
        }
        return min;
    }

    private int findLargestValue(int[] array) {
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    private double findLargestValue(double[] array) {
        double max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    private double findGraphMax(SimpleXYSeries s) {
        double max = (double)s.getY(0);
        for (int i = 1; i < s.size(); i++) {
            double a = (double)s.getY(i);
            if(a>max) {
                max = a;
            }
        }
        return max;
    }

    private double findGraphMin(SimpleXYSeries s) {
        double min = (double)s.getY(0);
        for (int i = 1; i < s.size(); i++) {
            double a = (double)s.getY(i);
            if(a<min) {
                min = a;
            }
        }
        return min;
    }

    //Helper Functions:
    private static String toHexString(byte[] bytes) {
        //Little-Endian for use with NFC devices
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[bytes.length*2-1-(j * 2 + 1)] = HEX_CHARS[v >>> 4];
            hexChars[bytes.length*2-1-(j * 2)] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }
}
