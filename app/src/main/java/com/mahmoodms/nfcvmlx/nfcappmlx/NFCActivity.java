package com.mahmoodms.nfcvmlx.nfcappmlx;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

//import com.opencsv.CSVWriter;

import com.androidplot.Plot;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by mahmoodms on 10/18/2016.
 */

public class NFCActivity extends Activity {
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
//    private TextView mTV2;
    private TextView mNFCText;
    private TextView mDataRead;
    private Tag mCurrentTag;
    private Switch mConfigSwitch;
    private Switch mStartDataloggingSwitch;
    private Switch mDatalogSwitch;
    private boolean mConfigure = false;
    private boolean mStartDataLogging = false;
    private boolean mReadDataLogging = false;
    private boolean mReadRegisters = false;
    private XYPlot xyPlot;
    private SimpleXYSeries nfcData;
    private Redrawer redrawer;

    private CSVWriter csvWriter;

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageManager pm = getPackageManager();
        setContentView(R.layout.main_nfc_activity);
        mNFCText = (TextView)findViewById(R.id.nfc_data);
//        mTV2 = (TextView) findViewById(R.id.textView2);
//        mConfigSwitch = (Switch) findViewById(R.id.switch1);
//        mDatalogSwitch = (Switch) findViewById(R.id.switch2);
//        mStartDataloggingSwitch = (Switch) findViewById(R.id.switch3);
//        mStartDataloggingSwitch.setVisibility(View.GONE);
        xyPlot = (XYPlot) findViewById(R.id.dataPlot);
        nfcData = new SimpleXYSeries("pH Sensor Data");
//        xyPlot.setDomainBoundaries(0, 10, BoundaryMode.FIXED);
//        xyPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
//        xyPlot.setDomainStepValue(1);
        nfcData.useImplicitXVals();

        xyPlot.setRangeBoundaries(-32768,32768,BoundaryMode.AUTO);
        LineAndPointFormatter lineAndPointFormatter1 = new LineAndPointFormatter(Color.BLACK, null, null, null);
        lineAndPointFormatter1.getLinePaint().setStrokeWidth(3);
        xyPlot.addSeries(nfcData, lineAndPointFormatter1);

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
        mConfigSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mConfigure = b;
                if(b){
                        Toast toast = Toast.makeText(getApplicationContext(),"Touch to NFC Device to Configure", Toast.LENGTH_LONG);
                        toast.show();
                }
            }
        });
        mStartDataloggingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                mStartDataLogging = b;
                mReadRegisters = b;
                if(b){
                    Toast toast = Toast.makeText(getApplicationContext(),"Touch to NFC Device to Start Data Logging", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
        mDatalogSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mReadDataLogging = b;
                if(b){
                    Toast toast = Toast.makeText(getApplicationContext(),"Touch to NFC Device to Start Data Logging", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        redrawer = new Redrawer(Arrays.asList(new Plot[]{xyPlot}),100,false);
    }
    private File root;
    public void exportData(boolean init, boolean terminate, String fileName, double dataPoint) throws IOException {
        if(init) {
            root = Environment.getExternalStorageDirectory();

        }
    }

    private void delayMS(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Log.e("InterruptedException",e.toString());
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        String action = intent.getAction();
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
                    if(mConfigure) {
                        Toast.makeText(getApplicationContext(), "Configuring Internal Sensor", Toast.LENGTH_LONG).show();
                        //TODO: Internal Device Configuration:

                        /*tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x00, (byte)0x2D,(byte) 0x26);
                        delayMS(18);
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x01, (byte)0x4D,(byte) 0x14);
                        delayMS(18);
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x02, (byte)0x84,(byte) 0xA0);
                        delayMS(18);
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x03, (byte)0x1F,(byte) 0xE0);
                        delayMS(18);*/
                        //TODO Change Everything to Big-endian
//                    Eeprom security
//                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x04, (byte)0xAA,(byte) 0xA8); //LilEndian
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x04, (byte)0xA8,(byte) 0xAA);
                        delayMS(18);
//                    Device security
//                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x05, (byte)0x3F,(byte) 0xF0);
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x05, (byte)0xF0,(byte) 0x3F);
                        delayMS(18);
//                    RFID Password
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x06, (byte)0x00,(byte) 0x00);
                        delayMS(18);
//                    DMA configuration
//                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x09, (byte)0x90,(byte) 0x70);
//                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x09, (byte)0x70,(byte) 0xA0);
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x09, (byte)0x70,(byte) 0x90);
                        delayMS(18);
//                    DMA source start address
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x0A, (byte)0x00,(byte) 0x00);
                        delayMS(18);
//                    DMA destination start address
//                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x0B, (byte)0x00,(byte) 0x29);
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x0B, (byte)0x29,(byte) 0x00);
                        delayMS(18);
//                    DMA processing length
//                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x0C, (byte)0x26,(byte) 0x16);
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x0C, (byte)0xD7,(byte) 0x00);
                        delayMS(18);
//                    SPI-master configuration
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x0D, (byte)0x00,(byte) 0x00);
                        delayMS(18);
//                    SPI-master commands codes
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x0E, (byte)0x00,(byte) 0x00);
                        delayMS(18);
//                    Timer period
//                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x0F, (byte)0x00,(byte) 0x01);
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x0F, (byte)0x01,(byte) 0x00);
                        delayMS(18);
//                    Timer control 0x1c00
//                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x10, (byte)0x00,(byte) 0x14);
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x10, (byte)0x1D,(byte) 0x00);
                        delayMS(18);
//                    Sensor power configuration
//                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x12, (byte)0x00,(byte) 0xFF);
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x12, (byte)0xFF,(byte) 0x00);
                        delayMS(18);

//                    Sensor Trimming
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x14, (byte)0x00,(byte) 0x00);
                        delayMS(18);
//                    Sensor 0 control word
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x15, (byte)0xF1,(byte) 0x50);
                        delayMS(18);
//                    Sensor 0 low threshold
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x16, (byte)0x00,(byte) 0x00);
                        delayMS(18);
//                    Sensor 0 high threshold
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x17, (byte)0x00,(byte) 0x00);
                        delayMS(18);
//                    Sensor 0 conditioner config.
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x18, (byte)0x00,(byte) 0x80);
                        delayMS(18);
//                    Sensor 0 connection config.
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x19, (byte)0x31,(byte) 0x02);
                        delayMS(18);
//                    Sensor 0 resistance network
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x1A, (byte)0x00,(byte) 0x80);
                        delayMS(18);
//                    Sensor 1 control word
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x1B, (byte)0x73,(byte) 0xD0);
                        delayMS(18);
//                    Sensor 1 low threshold
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x1C, (byte)0x00,(byte) 0x00);
                        delayMS(18);
//                    Sensor 1 high threshold
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x1D, (byte)0x00,(byte) 0x00);
                        delayMS(18);
//                    Sensor 1 conditioner config.
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x1E, (byte)0x00,(byte) 0x80);
                        delayMS(18);
//                    Sensor 1 connection config.
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x1F, (byte)0x23,(byte) 0x01);
                        delayMS(18);
//                    Sensor 1 resistance network
                        tranceiveWrite(nfcVTag, (byte)0x43, (byte)0x21, (byte)0x20, (byte)0x40,(byte) 0x00);
                        delayMS(18);
                    }

                    if(mReadRegisters) {
                        String concat = "";
                        byte[] address = {(byte) 0x09};
                        for (int i = 0x09; i < 0x21; i++) {
                            byte[] responseRead = tranceiveReadEEPROM(nfcVTag, (byte)0x02, (byte)0x20, address[0]);
                            delayMS(15);
                            Log.e("#0x"+toHexString(address)+": "+"EEPROM",toHexString(responseRead));
                            if(i>8) {
                                String tempString = "#0x"+toHexString(address)+": "+"EEPROM: " + toHexString(responseRead)+"\n";
                                concat+= tempString;
                            }
                            address[0]++;
                        }
                    }

                    if(mReadDataLogging) {
                        //TODO: Read :
                            //A2
                        Toast.makeText(getApplicationContext(), "Reading Temperature Data (Please Wait)", Toast.LENGTH_LONG).show();
                        byte[] readInternalDevice = tranceiveReadWriteInternal(nfcVTag,(byte)0x03, (byte)0xA2, (byte)0x05, (byte)0x00,(byte) 0x00);
                        Log.e("readInternalDevice:","isDataLogSeq.StartedResponse:"+toHexString(readInternalDevice));
                        String concat = "";
                        if(readInternalDevice.length>1) {
                            Log.e("byte 1:",Integer.toHexString((int) readInternalDevice[0])); //error code
                            Log.e("byte 2:",Integer.toHexString((int) readInternalDevice[1])); //bits 0→7
                            Log.e("byte 3:",Integer.toHexString((int) readInternalDevice[2])); //bits 8→15

                            //TODO: Read from internaldevice: @address: readInternalDevice[1])
                            for (int i = 0; i < 10; i++) {
                                byte[] readEEPROM = tranceiveReadEEPROM(nfcVTag,(byte)0x03, (byte)0x21, readInternalDevice[1]);
                                byte[] tempAddr = {readInternalDevice[1]};
                                delayMS(560);
                                Log.e("Data Log #",String.valueOf(i)+" at address #"+toHexString(tempAddr)+" (Hex): 0x"+toHexString(readEEPROM));
                                /*if((i+1)%2==0) {
                                    int x = 0;
                                    if(readEEPROMTempMeasure.length>2) {
                                        byte[] data = {readEEPROMTempMeasure[1],readEEPROMTempMeasure[2]};
                                        x = parseAsLittleEndianByteArray(data);
                                    }
//                                    double tempC2 = (3*Math.pow(10,-8)*Math.pow(x,2))+0.0022*x-2.4561;
                                    double tempC2 = (-1*Math.pow(10,-8)*Math.pow(x,2))-0.0022*x+44.416;
                                    concat += " as Int: "+String.valueOf(x)+" in °C: "+String.format("%3.2f",tempC2)+"\n";
                                } else {
                                    if (readEEPROMTempMeasure.length>2) {
                                        byte[] bytes = {readEEPROMTempMeasure[1],readEEPROMTempMeasure[2]};
                                        int x = parseAsLittleEndianByteArray(bytes);
                                        concat += ("Time "+String.valueOf(x+16385));
                                    }
                                }*/
                                readInternalDevice[1]++;
                            }
                        } else {
                            concat+= " Error Reading Data!";
                        }
                    }
                }
            }
        }
    }

    private long period = 1000;

    private boolean recordingSensor = false;


    private void startFixedRateRead() {
        executor.scheduleAtFixedRate(new command1(), 0, period, TimeUnit.MILLISECONDS);
        recordingSensor = true;
    }

    private void stopFixedRateRead() {
        recordingSensor = false;
    }

    class command1 implements Runnable {
        @Override
        public void run() {

        }
    }

    public static int parseAsLittleEndianByteArray(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (i == 0) {
                result |= bytes[i] << (8 * i);
            } else {
                result |= bytes[i] << (8 * i);
            }
        }
        return result;
    }

    protected byte[] tranceiveReadEEPROM(NfcV nfcVTag, byte flag, byte actionToTake, byte addr) {
        byte[] response = {(byte)0xFF};
        try
        {
            byte[] WriteSingleBlockFrame = {
                    flag, actionToTake,
                    addr
            };
            response = nfcVTag.transceive(WriteSingleBlockFrame);
            if(response[0] == (byte) 0x00 || response[0] == (byte) 0x01) //response 01 = error sent back by tag (new Android 4.2.2) or BC
            {
                Log.i("*******", "**SUCCESS** Read Data "+String.valueOf(response[0]));
            }
            return response;
        }
        catch(Exception e)
        {
            Log.e("NFCCommand,Tranceive","Some Exception Error");
            //Used for DEBUG : Log.i("NFCCOmmand", "Write Single block command  " + errorOccured);
            return response;
        }
    }

    protected byte[] tranceiveWrite(NfcV nfcVTag, byte flag, byte actionToTake, byte addr, byte command0, byte command1) {
        byte[] response = {(byte)0xFF};
//        byte[] tagID = nfcVTag.getDsfId();
        try
        {
            byte[] WriteSingleBlockFrame = {
                    flag, actionToTake, /*(byte) 0x1F,*/
//                    tagID[0], tagID[1], tagID[2], tagID[3], tagID[4], tagID[5], tagID[6], tagID[7],
                    addr, command0, command1
            };
            response = nfcVTag.transceive(WriteSingleBlockFrame);
            if(response[0] == (byte) 0x00) //response 01 = error sent back by tag (new Android 4.2.2) or BC
            {

                Log.i("*******", "**SUCCESS** Write Data "+String.valueOf(response[0]));
            } else if (response[0] == (byte) 0x01) {
                Log.i("*******", "**FAIL** Write Data "+String.valueOf(response[0]));
            }
            return response;
        }
        catch(Exception e)
        {
            Log.e("NFCCommand,Tranceive","Some Exception Error");
            //Used for DEBUG : Log.i("NFCCOmmand", "Write Single block command  " + errorOccured);
            return response;
        }
    }

    protected byte[] tranceiveReadWriteInternal(NfcV nfcVTag, byte flag, byte readWrite, byte addr, byte command0, byte command1) {
        byte[] response = {(byte)0xFF};
//        byte[] tagID = nfcVTag.getDsfId();
        try
        {
            if(readWrite==(byte)0xA3){

                byte[] WriteSingleBlockFrame = {
                        flag, readWrite, (byte)0x1F, /*(byte) 0x1F,*/
//                    tagID[0], tagID[1], tagID[2], tagID[3], tagID[4], tagID[5], tagID[6], tagID[7],
                        addr, command0, command1
                };
                response = nfcVTag.transceive(WriteSingleBlockFrame);
            } else if (readWrite==(byte)0xA2) {
                byte[] WriteSingleBlockFrame = {
                        flag, readWrite, (byte)0x1F, /*(byte) 0x1F,*/
//                    tagID[0], tagID[1], tagID[2], tagID[3], tagID[4], tagID[5], tagID[6], tagID[7],
                        addr
                };
                response = nfcVTag.transceive(WriteSingleBlockFrame);
            } else if (readWrite==(byte)0xC0) {
                byte[] WriteSingleBlockFrame = {
                        flag, readWrite, (byte)0x1F
                };
                response = nfcVTag.transceive(WriteSingleBlockFrame);
            } else if (readWrite== (byte)0xA1) {
                byte[] WriteSingleBlockFrame = {
                        flag, readWrite, (byte)0x1F, addr, command0, command1
                };
                response = nfcVTag.transceive(WriteSingleBlockFrame);
            } else if (readWrite == (byte)0xA0) {
                byte[] WriteSingleBlockFrame = {
                        flag, readWrite, (byte)0x1F, addr
                };
                response = nfcVTag.transceive(WriteSingleBlockFrame);
            } else if (readWrite == (byte)0xA4) {
                byte[] WriteSingleBlockFrame = {
                        flag, readWrite, (byte)0x1F, addr
                };
                response = nfcVTag.transceive(WriteSingleBlockFrame);
            } else {
                Log.e("CANNOT FIND", "INCORRECT READWRITE REQUEST");
            }
            Log.e("NFCCommand,Response:",toHexString(response));
            if(response[0] == (byte) 0x00) //response 01 = error sent back by tag (new Android 4.2.2) or BC
            {
                String s = "";
                if(readWrite==(byte)0xA2) {
                    Log.i("*******", "**SUCCESS** Read Data "+String.valueOf(response[0]));
                } else {
                    Log.i("*******", "**SUCCESS** Write Data "+String.valueOf(response[0]));
                }
            } else if (response[0] == (byte) 0x01) {
                Log.i("*******", "**FAIL** Write Data "+String.valueOf(response[0]));
            }
            return response;
        }
        catch(Exception e)
        {
            Log.e("NFCCommand,Tranceive","Some Exception Error");
            //Used for DEBUG : Log.i("NFCCOmmand", "Write Single block command  " + errorOccured);
            return response;
        }
    }

    @Override
    protected void onResume()
    {
        redrawer.start();
        // TODO Auto-generated method stub
        mPendingIntent = PendingIntent.getActivity(this, 0,new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        super.onResume();
        //Used for DEBUG : Log.v("NFCappsActivity.java", "ON RESUME NFC APPS ACTIVITY");
    }

    @Override
    protected void onPause()
    {
        redrawer.pause();
        // TODO Auto-generated method stub
        //Used for DEBUG : Log.v("NFCappsActivity.java", "ON PAUSE NFC APPS ACTIVITY");
        mAdapter.disableForegroundDispatch(this);
        super.onPause();
    }


    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
            'B', 'C', 'D', 'E', 'F' };
    public static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            //TODO: Little-Endian????
            hexChars[bytes.length*2-1-(j * 2 + 1)] = HEX_CHARS[v >>> 4];
            hexChars[bytes.length*2-1-(j * 2)] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }
    public static String toHexStringBigEndian(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            //TODO: Big Endian?
            hexChars[j * 2] = HEX_CHARS[v >>> 4];
            hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }
}
