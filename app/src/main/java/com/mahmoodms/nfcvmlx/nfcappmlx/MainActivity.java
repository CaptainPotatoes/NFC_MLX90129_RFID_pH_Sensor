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
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Date;
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
    private final int HISTORY_SECONDS = 30;
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
    private Button mButtonTempMain;

    public final static ColorDrawable actionBarTheme = new ColorDrawable(Color.parseColor("#A9A9A9"));
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
        mButtonTempMain = (Button) findViewById(R.id.buttonTempMain);
        //Plot stuff:
        xyPlot = (XYPlot) findViewById(R.id.dataPlot);
        nfcDataSensor0 = new SimpleXYSeries("Temperature Sensor Data");
        nfcDataSensor1 = new SimpleXYSeries("pH Sensor Data");
        //Todo: Using explicit x values: (over 30s)
        xyPlot.setDomainBoundaries(0, HISTORY_SECONDS, BoundaryMode.AUTO);
        xyPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
        xyPlot.setDomainStepValue(2);
        //nfcData.useImplicitXVals();
        xyPlot.setRangeBoundaries(0,16384,BoundaryMode.AUTO);
        xyPlot.setRangeStepValue(16384/5);

        xyPlot.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
        xyPlot.setDomainLabel("Time (seconds)");
        xyPlot.getDomainLabelWidget().pack();
        xyPlot.setRangeLabel("Voltage (mV)");
        xyPlot.getRangeLabelWidget().pack();
        xyPlot.setRangeValueFormat(new DecimalFormat("#.###"));
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
        LineAndPointFormatter lineAndPointFormatter2 = new LineAndPointFormatter(Color.BLUE, null, null, null);
        lineAndPointFormatter1.getLinePaint().setStrokeWidth(3);
        xyPlot.addSeries(nfcDataSensor1, lineAndPointFormatter2);

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
        mButtonTempMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testFunction();
            }
        });
    }

    @Override
    protected void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        //Used for DEBUG : Log.v("NFCappsActivity.java", "ON RESUME NFC APPS ACTIVITY");
        mPendingIntent = PendingIntent.getActivity(this, 0,new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        if(!logFileInitialized) {
            exportLogFile(true,"");
        }
    }

    @Override
    protected void onPause()
    {
        // TODO Auto-generated method stub
        //Used for DEBUG : Log.v("NFCappsActivity.java", "ON PAUSE NFC APPS ACTIVITY");
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
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
            logFile = new File(dir,"Log_"+getTimeStamp()+".txt");
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
    private int timeStampIndex;
    private int[] sensor0Data = new int[100];
    private int sensor0Index;
    private int sensor1Index;
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
                    NfcV nfcVTag = NfcV.get(mCurrentTag);
                    try {
                        nfcVTag.connect();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Could Not Connect To NFC Device", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //TODO: Schedule at fixed rate: (Enable on connect, disable on disconnect)
                    //Read register 0x09 to see which sensors are enabled
                    /*byte[] dmaConfig = tranceiveReadEEPROM(nfcVTag, (byte)0x09);
                    if(dmaConfig.length>2) {
                        timestampsEnabled = ( (dmaConfig[2] & 0b10000000) == 0b10000000);
                        sensor0Enabled = ( (dmaConfig[2] & 0b00010000) == 0b00010000);
                        sensor1Enabled = ( (dmaConfig[2] & 0b00100000) == 0b00100000);
                        Log.e(TAG,Boolean.toString(timestampsEnabled)+" "+Boolean.toString(sensor0Enabled)+" "+Boolean.toString(sensor1Enabled));
                    }*/
                    exportLogFile(false, "Connected at: "+getTimeStamp()+"\r\n");
                    if(!timestampsEnabled) {
                        //TODO: USE IMPLICIT X VALUES!
                    }
                    //Check which address they are being stored at:
                    //delayMS(20);
                    byte[] readDataAddress = tranceiveReadInternal(nfcVTag, (byte)0x05);
                    delayMS(20);
                    Log.e(TAG, "readDataAddress = 0x"+ViewConfig.toHexStringBigEndian(readDataAddress));
                    if(readDataAddress.length>2) {
                        Log.e("byte 1:",Integer.toHexString((int) readDataAddress[0]));
                        Log.e("byte 2:",Integer.toHexString((int) readDataAddress[1]));
                        Log.e("byte 3:",Integer.toHexString((int) readDataAddress[2]));
                    }
                    timeStampIndex = 0;
                    sensor0Index = 0;
                    sensor1Index = 0;
                    lastPlottedIndex = 0;

                    while (nfcVTag.isConnected()) {
                        if(readDataAddress.length>2) {

                            byte[] readEEPROM = tranceiveReadEEPROM(nfcVTag, readDataAddress[1]);
                            if(readEEPROM.length>2) {
                                byte[] relevantData = {readEEPROM[1],readEEPROM[2]};
                                if((readEEPROM[2] & 0b11000000) == 0b11000000) {
                                    //timestamp //Todo: use 0b00 bitmask to convert to correct values:
//                                    byte unmaskedMSB = (byte)(readEEPROM[2] & 0b00111111);
//                                    byte[] timeStamp = {readEEPROM[1], unmaskedMSB};
                                    byte[] timeStamp = {readEEPROM[1], ((byte)(readEEPROM[2] & 0b00111111))};
//                                    int timeStampInt = bytesWordToIntAlt(timeStamp);
                                    timeStampData[timeStampIndex] = bytesWordToIntAlt(timeStamp);
                                    Log.e(TAG,"INTVAL TS: "+String.valueOf(timeStampData[timeStampIndex]));
                                    exportLogFile(false, "T = "+String.valueOf(timeStampData[timeStampIndex])+"\r\n");
                                    timeStampIndex++;
                                } else if ((readEEPROM[2] & 0b11000000) == 0b01000000) {
                                    // Sensor 1 (external)
                                    byte[] sensor1Datapoint = {readEEPROM[1], ((byte)(readEEPROM[2] & 0b00111111))};
                                    sensor1Data[sensor1Index] = bytesWordToIntAlt(sensor1Datapoint);
                                    Log.e(TAG,"INTVAL S1: "+String.valueOf(sensor1Data[sensor1Index]));
                                    exportLogFile(false, "S1: "+String.valueOf(sensor1Data[sensor1Index])+"\r\n");
                                    sensor1Index++;
                                } else {
                                    //Sensor 0 (internal)
                                    byte[] sensor0Datapoint = {readEEPROM[1], ((byte)(readEEPROM[2] & 0b00111111))};
                                    sensor0Data[sensor0Index] = bytesWordToIntAlt(sensor0Datapoint);
                                    Log.e(TAG,"INTVAL S0: "+String.valueOf(sensor0Data[sensor0Index]));
                                    exportLogFile(false, "S0: "+String.valueOf(sensor0Data[sensor0Index])+"\r\n");
                                    sensor0Index++;
                                }
//                                Log.e(TAG, "ReadData["+ViewConfig.toHexStringLittleEndian(readDataAddress) +" ]= 0x"+ ViewConfig.toHexStringLittleEndian(relevantData));
                            } else {
                                Log.e(TAG, "ReadData = 0x"+ ViewConfig.toHexStringBigEndian(readEEPROM));
                                exportLogFile(false, "Data Error: 0x"+ViewConfig.toHexStringBigEndian(readEEPROM)+"\r\n");
                            }
                            readDataAddress[1]++;
                            //TODO: PLOT DATA:
                            //turn values into array
                            /*runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int[] arrayIndicies = {timeStampIndex, sensor0Index, sensor1Index};
                                    int smallestIndex = findSmallestValue(arrayIndicies);
                                    int largestIndex = findLargestValue(arrayIndicies);
                                    if(smallestIndex>0) {
                                        //have values to plot; proceed.
                                        if(nfcDataSensor0.size()>HISTORY_SECONDS) {
                                            nfcDataSensor0.removeFirst();
                                        }
                                        if(nfcDataSensor1.size()>HISTORY_SECONDS) {
                                            nfcDataSensor1.removeFirst();
                                            double[] maxes = {findGraphMax(nfcDataSensor0), findGraphMax(nfcDataSensor1)};
                                            double max = findLargestValue(maxes);
                                            double[] mins = {findGraphMin(nfcDataSensor0), findGraphMin(nfcDataSensor1)};
                                            double min = findSmallestValue(mins);
                                            if(max-min!=0) {
                                                if(currentBM!=BoundaryMode.AUTO) {
                                                    xyPlot.setRangeBoundaries(-16384, 16384, BoundaryMode.AUTO);
                                                    currentBM = BoundaryMode.AUTO;
                                                }
                                            } else {
                                                if(currentBM!=BoundaryMode.FIXED) {
                                                    xyPlot.setRangeBoundaries(min-10, max+10, BoundaryMode.FIXED);
                                                }
                                                xyPlot.setRangeStepValue(20/5);
                                            }
                                        }
                                        for (int i = lastPlottedIndex; i < smallestIndex; i++) {
                                            if(i<99) {
                                                nfcDataSensor0.addLast(timeStampData[i], sensor0Data[i]);
                                                nfcDataSensor1.addLast(timeStampData[i], sensor1Data[i]);
                                            }
                                        }
                                    }
                                    lastPlottedIndex = smallestIndex;
                                }
                            });*/
                        }
                        delayMS(400);
                    }
                    //Get values (Timestamp, Sensor 0, Sensor 1)
                    //Write to Drive (use code from ECG)
                    //Sequentially plot points as received [timestamp, sensor]
                    //Can plot both Sensor 0 and Sensor 1 separately.
                }
            }
        }
    }

    private boolean startedGraphingData = false;
    private boolean initExecutor = false;
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private void stopReceiveDataAndGraph() {
        startedGraphingData = false;
    }

    private void startReceiveDataAndGraph() {
        executor.scheduleAtFixedRate(new retrieveDataAndGraph(), 0, 1000, TimeUnit.MILLISECONDS);
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

//    protected byte[] tranceiveWriteInternal(NfcV nfcVTag, byte addr, byte[] data) {
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