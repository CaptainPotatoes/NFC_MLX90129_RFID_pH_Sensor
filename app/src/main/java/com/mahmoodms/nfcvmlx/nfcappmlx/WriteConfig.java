package com.mahmoodms.nfcvmlx.nfcappmlx;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mahmoodms on 11/26/2016.
 */

public class WriteConfig extends Activity {
    //NFC Stuff:
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private Tag mCurrentTag;
    //Buttons:
    private Button mViewConfig;
    private Button mTestConfig;
    private String TAG = "WriteConfigActivity";
    //Checkboxes:

    //RadioButtons:
    RadioButton mRadioButtonWriteFromExternal;
    RadioButton mRadioButtonWriteToExternal;
    //Spinners:
    private Spinner spinner, spinner2;

    //EditText
    EditText mEditTextTimerCountdown;
    EditText mEditTextSensor0Mux;
    EditText mEditTextSensor0Resistance;
    EditText mEditTextSensor0DacOffset;
    EditText mEditTextSensor1Mux;
    EditText mEditTextSensor1Resistance;
    //TextViews:
    TextView mTextViewTimerUnits;
    TextView mTotalGain;
    //Booleans and Options:
    private boolean enableSensor0 = true;
    private boolean enableSensor1 = true;
    private boolean enableTimeStamps = true;
    private boolean enableSPI = false;
    private boolean defaultDMASettings = true;
    private boolean manualDMA = true;
    private boolean automaticDataLoggingTimer = true;
    private boolean sensor0Mux = true;
    private boolean sensor0Resistance = true;
    private boolean sensor0Chopper = false;
    private boolean sensor0LowPowerMode = true;
    private boolean sensor1Mux = true;
    private boolean sensor1Resistance = true;
    private boolean sensor1Chopper = true;
    private boolean sensor1LowPowerMode = true;
    private boolean timerIRQ = false;
    private boolean writeToDevice = false;

    private int sensor0Settings = 2;
    private int writeFromLocation = 0;
    private int writeToLocation = 1;
    private boolean timerStandby = true;
    private int timerUnits = 3;
    private int sensor0ADC = 2;
    private int sensor1ADC = 2;
    private int pga1Gain = 0;
    private int pga2Gain = 0;

    private double pga1 = 8;
    private double pga2 = 1;
//    private RadioGroup mRadioGroupWriteDataFrom;

    private TextView mNfcId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageManager pm = getPackageManager();
        setContentView(R.layout.write_config);
        //Flag to keep screen on (stay-awake):
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (getActionBar()!=null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            ActionBar actionBar = getActionBar();
            actionBar.setBackgroundDrawable(MainActivity.actionBarTheme);
        }
        mViewConfig = (Button) findViewById(R.id.view_config);
        if(!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            Toast toast = Toast.makeText(getApplicationContext(),"NFC Unavailable", Toast.LENGTH_LONG);
            toast.show();
        } else {
            mAdapter = NfcAdapter.getDefaultAdapter(this);
            mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
            if(mAdapter.isEnabled()) {
                IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
                mFilters = new IntentFilter[] {ndef,};
                mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };
                Toast toast = Toast.makeText(getApplicationContext(),"NFC Enabled", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),"NFC Disabled, Please Enable", Toast.LENGTH_LONG);
                toast.show();
            }
        }
        mNfcId = (TextView) findViewById(R.id.nfc_id_write);
//        mCheckBoxSPI.setEnabled(false);
        mRadioButtonWriteFromExternal = (RadioButton) findViewById(R.id.radioButtonWriteFromExternal);
        mRadioButtonWriteFromExternal.setVisibility(View.GONE);
        mRadioButtonWriteToExternal = (RadioButton) findViewById(R.id.radioButtonWriteToExternal);
        mRadioButtonWriteToExternal.setVisibility(View.GONE);
        mEditTextTimerCountdown = (EditText) findViewById(R.id.editTextTimerCountdown);
        mEditTextSensor0Mux = (EditText) findViewById(R.id.editTextSensor0Mux);
        mEditTextSensor0Resistance = (EditText) findViewById(R.id.editTextSensor0Resistance);
        mEditTextSensor0DacOffset = (EditText) findViewById(R.id.editTextSensor0DacOffset);
        mTextViewTimerUnits = (TextView) findViewById(R.id.textViewTimerUnits);
        mTotalGain = (TextView) findViewById(R.id.totalGain);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner2 = (Spinner) findViewById(R.id.spinner2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { //PGA 1
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pga1Gain = i;
                switch (i) {
                    case 0: //Default
                        pga1 = 8;
                        break;
                    case 1:
                        pga1 = 10;
                        break;
                    case 2:
                        pga1 = 12.6;
                        break;
                    case 3:
                        pga1 = 15.5;
                        break;
                    case 4:
                        pga1 = 19.6;
                        break;
                    case 5:
                        pga1 = 24.5;
                        break;
                    case 6:
                        pga1 = 30.8;
                        break;
                    case 7:
                        pga1 = 38.1;
                        break;
                    case 8:
                        pga1 = 47.6;
                        break;
                    case 9:
                        pga1 = 59.4;
                        break;
                    case 10:
                        pga1 = 75;
                        break;
                    default:
                        break;
                }
                Log.e(TAG,"pga1Gain = "+String.valueOf(pga1Gain));
                //Calculate total gain after change:
                mTotalGain.setText(String.valueOf(pga1*pga2));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                pga1Gain = 0;
                pga1 = 8.0;
                Log.e(TAG,"pga1Gain (Default) = "+String.valueOf(pga1Gain));
                //default
            }
        });
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pga2Gain = i;
                switch (i) {
                    case 0: //Default
                        pga2 = 1;
                        break;
                    case 1:
                        pga2 = 2;
                        break;
                    case 2:
                        pga2 = 3;
                        break;
                    case 3:
                        pga2 = 4;
                        break;
                    case 4:
                        pga2 = 5;
                        break;
                    case 5:
                        pga2 = 6;
                        break;
                    case 6:
                        pga2 = 7;
                        break;
                    case 7:
                        pga2 = 8;
                        break;
                }
                Log.e(TAG,"pga2Gain = "+String.valueOf(pga2Gain));
                //Calculate total gain after change:
                mTotalGain.setText(String.valueOf(pga1*pga2));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                pga2Gain = 0;
                pga2 = 8;
                Log.e(TAG,"pga2Gain = "+String.valueOf(pga2Gain));
            }
        });
        mViewConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ViewConfig.class);
                startActivity(intent);
            }
        });
        // (ALL TEMPORARY STUFF)
        mTestConfig = (Button) findViewById(R.id.buttonTestConfig);
        mTestConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testConfig();
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
    }

    @Override
    protected void onPause()
    {
        // TODO Auto-generated method stub
        //Used for DEBUG : Log.v("NFCappsActivity.java", "ON PAUSE NFC APPS ACTIVITY");
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    protected void testConfig() {
        //TODO: TEST HERE, THEN COPY TO onNewIntent()
        //Paste code here to test with onCreate:

        //VERIFY VIA LOGGING:
        Log.e(TAG,"Write[0x##] = 0xAABB - In order they are programmed");
//        Log.e(TAG,"Write[0x09] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand09));
//        Log.e(TAG,"Write[0x0A] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0A));
//        Log.e(TAG,"Write[0x0B] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0B));
//        Log.e(TAG,"Write[0x0C] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0C));
//        Log.e(TAG,"Write[0x0D] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0D));
//        Log.e(TAG,"Write[0x0E] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0E));
//        Log.e(TAG,"Write[0x0F] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0F));
//        Log.e(TAG,"Write[0x10] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand10));
//        Log.e(TAG,"Write[0x12] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand12));
//        Log.e(TAG,"Write[0x14] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand14));
//        Log.e(TAG,"Write[0x15] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand15));
//        Log.e(TAG,"Write[0x16] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand16));
//        Log.e(TAG,"Write[0x17] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand17));
//        Log.e(TAG,"Write[0x18] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand18));
//        Log.e(TAG,"Write[0x19] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand19));
//        Log.e(TAG,"Write[0x1A] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1A));
//        Log.e(TAG,"Write[0x1B] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1B));
//        Log.e(TAG,"Write[0x1C] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1C));
//        Log.e(TAG,"Write[0x1D] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1D));
//        Log.e(TAG,"Write[0x1E] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1E));
//        Log.e(TAG,"Write[0x1F] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1F));
//        Log.e(TAG,"Write[0x20] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand20));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        String action = intent.getAction();
        if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            mCurrentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] id = mCurrentTag.getId();
            String hexID = "TAGID: "+ViewConfig.toHexStringLittleEndian(id);
            Log.e("LOG HEX ID",hexID);
            mNfcId.setText(hexID);
            for (String s:mCurrentTag.getTechList()) {
                if(s.equals(NfcV.class.getName())) {
                    NfcV nfcVTag = NfcV.get(mCurrentTag);
                    try {
                        nfcVTag.connect();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Could Not Connect To NFC Device", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //TODO: CONFIGURATION:
                    /*byte[] writeCommand09 = new byte[2];
                    //Byte B:
                    Log.e(TAG,"ENABLE TIMESTAMPS = "+String.valueOf(enableTimeStamps));
                    if(enableTimeStamps) {
                        writeCommand09[1] |= 0b10000000;
                    } else {
                        writeCommand09[1] |= 0b00000000;
                    }
                    if(enableSensor0) {
                        writeCommand09[1] = (byte)(writeCommand09[1] | 0b00010000);
                    }
                    if(enableSensor1) {
                        writeCommand09[1] = (byte)(writeCommand09[1] | 0b00100000);
                    }
                    //Byte A:
                    if(writeToLocation==0) {
                        writeCommand09[0] = (byte)(writeCommand09[0] | 0b11000000);
                    } else if (writeToLocation==1) {
                        writeCommand09[0] = (byte)(writeCommand09[0] | 0b01000000);
                    } else if (writeToLocation==3) {
                        writeCommand09[0] = (byte)(writeCommand09[0] | 0b10000000);
                    } //else do nothing

                    if(writeFromLocation==0) {
                        writeCommand09[0] = (byte)(writeCommand09[0] | 0b00110000);
                    } else if (writeFromLocation==1) {
                        writeCommand09[0] = (byte)(writeCommand09[0] | 0b00010000);
                    } else if (writeFromLocation==3) {
                        writeCommand09[0] = (byte)(writeCommand09[0] | 0b00100000);
                    } //else do nothing

                    if(!manualDMA) {
                        writeCommand09[0] = (byte)(writeCommand09[0] | 0b00000001);
                    }
                    //command: [0] is bits 0→7, and [1] is bits 8→15
                    */
                    byte[] writeCommand09 = {(byte)0x78, (byte)0x10}; // DMA CONFIGS:
                    byte[] writeCommand0A = {(byte)0x00, (byte)0x00};
                    byte[] writeCommand0B = {(byte)0x29, (byte)0x00};
                    byte[] writeCommand0C = {(byte)0xD7, (byte)0x00};
                    //TIMER CONTROL:
                    byte[] writeCommand0F = {(byte)0x01, (byte)0x00}; //1
//                    byte[] writeCommand0F = {(byte)0xFA, (byte)0x00}; //250
//                    byte[] writeCommand0F = {(byte)0xF4, (byte)0x01}; //500
//                    byte[] writeCommand10 = {(byte)0x0C, (byte)0x00}; //ms
                    byte[] writeCommand10 = {(byte)0x1C, (byte)0x00}; //s
                    //TODO: SENSOR POWER CONFIG & TRIMMING
                    byte[] writeCommand12 = {(byte)0xFF, (byte)0x00}; //Default Sensor Power Config
                    byte[] writeCommand14 = {(byte)0x00, (byte)0x00}; //Default Sensor trimming max kOhm

                    byte[] writeCommand15 = {(byte)0x70, (byte)0xC0}; //Default Sensor 0 Control word:
                    byte[] writeCommand16 = {(byte)0x00, (byte)0x00}; //Default Sensor 0 threshold (0):
                    byte[] writeCommand17 = {(byte)0x00, (byte)0x00}; //Default Sensor 0 threshold (0):
                    byte[] writeCommand18 = {(byte)0x00, (byte)0x00}; //Default Sensor 0 Conditioner Config
                    byte[] writeCommand19 = {(byte)0x00, (byte)0x00}; //Default Sensor 0 Connection Config
                    byte[] writeCommand1A = {(byte)0x00, (byte)0x00}; //Default Sensor 0 Resistance Network.
                    writeCommand19[0] = (byte)0x80; writeCommand19[1] = (byte)0x00;
                    writeCommand1A[0] = (byte)0x10; writeCommand1A[1] = (byte)0x00;
                    /*if(sensor0Settings==0) { //Light sensor config
                        writeCommand19[0] = (byte)0x00; writeCommand19[1] = (byte)0x00;
                        writeCommand1A[0] = (byte)0x02; writeCommand1A[1] = (byte)0x00;
                    } else if (sensor0Settings==1) { //Ext Temp Sensor
                        writeCommand19[0] = (byte)0x23; writeCommand19[1] = (byte)0x01;
                        writeCommand1A[0] = (byte)0x40; writeCommand1A[1] = (byte)0x00;
                    } else if (sensor0Settings==2) { // Potentiometer Sensor
                        writeCommand19[0] = (byte)0x69; writeCommand19[1] = (byte)0x00;
                        writeCommand1A[0] = (byte)0x40; writeCommand1A[1] = (byte)0x00;
                    } else if (sensor0Settings==3) { //Internal Temp
                        writeCommand19[0] = (byte)0x31; writeCommand19[1] = (byte)0x02;
                        writeCommand1A[0] = (byte)0x00; writeCommand1A[1] = (byte)0x80;
                    }*/
                    switch (pga1Gain) { //[8→75g] bits 11:8 (see xls) 0b0000XXXX of byte[B]
                        case 0: //Default
                            writeCommand18[1] |= 0b00000000;
                            break;
                        case 1:
                            writeCommand18[1] |= 0b00000001;
                            break;
                        case 2:
                            writeCommand18[1] |= 0b00000010;
                            break;
                        case 3:
                            writeCommand18[1] |= 0b00000011;
                            break;
                        case 4:
                            writeCommand18[1] |= 0b00000100;
                            break;
                        case 5:
                            writeCommand18[1] |= 0b00000101;
                            break;
                        case 6:
                            writeCommand18[1] |= 0b00000110;
                            break;
                        case 7:
                            writeCommand18[1] |= 0b00000111;
                            break;
                        case 8:
                            writeCommand18[1] |= 0b00001000;
                            break;
                        case 9:
                            writeCommand18[1] |= 0b00001001;
                            break;
                        case 10:
                            writeCommand18[1] |= 0b00001111;
                            break;
                        default:
                            break;
                    }
                    switch (pga2Gain) {
                        case 0: //Default
                            writeCommand18[1] |= 0b00000000;
                            break;
                        case 1:
                            writeCommand18[1] |= 0b00010000;
                            break;
                        case 2:
                            writeCommand18[1] |= 0b00100000;
                            break;
                        case 3:
                            writeCommand18[1] |= 0b00110000;
                            break;
                        case 4:
                            writeCommand18[1] |= 0b01000000;
                            break;
                        case 5:
                            writeCommand18[1] |= 0b01010000;
                            break;
                        case 6:
                            writeCommand18[1] |= 0b01100000;
                            break;
                        case 7:
                            writeCommand18[1] |= 0b01110000;
                            break;
                    }
                    if(sensor0Chopper) {
                        writeCommand18[1] |= 0b10000000;
                        writeCommand18[0] |= 0b11111111;
                    }
                    //18[0]
                    /*String dacOffset = mEditTextSensor0DacOffset.getText().toString();
//                    int parsedHex = Integer.parseInt(dacOffset);
                    int parsedHex = Integer.parseInt(dacOffset,16);
                    Log.e(TAG,"Int: parsedInt: "+String.valueOf(parsedHex));
                    writeCommand18[0] = intToSingleByte(parsedHex);*/

                    //TODO: FIX THIS CONFIGURATION
//                    byte[] writeCommand1B = {(byte)0xF0, (byte)0x40}; //Default Sensor 1 Control word: (same default config)
//                    byte[] writeCommand1C = {(byte)0x00, (byte)0x00}; //Default Sensor 1 threshold (0):
//                    byte[] writeCommand1D = {(byte)0x00, (byte)0x00}; //Default Sensor 1 threshold (0):
//                    byte[] writeCommand1E = {(byte)0x00, (byte)0x00}; //Default Sensor 1 Conditioner Config
//                    byte[] writeCommand1F = {(byte)0x00, (byte)0x00}; //Default Sensor 1 Connection Config ?????????????
//                    byte[] writeCommand20 = {(byte)0x02, (byte)0x00}; //Default Sensor 1 Resistance Network
                    // Store all samples; mean of 2 samples A: [0 1 1 1 0 0 1 1] B: [00000000]
                    //VERIFY VIA LOGGING:
                    Log.e(TAG,"Write[0x##] = 0xAABB - In order they are programmed");
                    Log.e(TAG,"Write[0x09] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand09));
                    Log.e(TAG,"Write[0x0A] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0A));
                    Log.e(TAG,"Write[0x0B] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0B));
                    Log.e(TAG,"Write[0x0C] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0C));
                    Log.e(TAG,"Write[0x0D] = IGNORE");
                    Log.e(TAG,"Write[0x0E] = IGNORE");
                    Log.e(TAG,"Write[0x0F] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0F));
                    Log.e(TAG,"Write[0x10] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand10));
                    Log.e(TAG,"Write[0x12] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand12));
                    Log.e(TAG,"Write[0x14] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand14));
                    Log.e(TAG,"Write[0x15] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand15));
                    Log.e(TAG,"Write[0x16] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand16));
                    Log.e(TAG,"Write[0x17] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand17));
                    Log.e(TAG,"Write[0x18] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand18));
                    Log.e(TAG,"Write[0x19] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand19));
                    Log.e(TAG,"Write[0x1A] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1A));
                    /*Log.e(TAG,"Write[0x1B] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1B));
                    Log.e(TAG,"Write[0x1C] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1C));
                    Log.e(TAG,"Write[0x1D] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1D));
                    Log.e(TAG,"Write[0x1E] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1E));
                    Log.e(TAG,"Write[0x1F] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1F));
                    Log.e(TAG,"Write[0x20] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand20));*/
                    //WRITE:                 Tag,    address, commandBytes[A, B]
                    if(writeToDevice) {
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x09, writeCommand09); // DMA Configuration
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x0A, writeCommand0A); // DMA Source Start Address
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x0B, writeCommand0B); // DMA Destination Start Address
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x0C, writeCommand0C); // DMA Processing Length
                        MainActivity.delayMS(50);

                        tranceiveWriteEEPROM(nfcVTag, (byte)0x0F, writeCommand0F); // Timer Period
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x10, writeCommand10); // Timer Control
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x12, writeCommand12); // Sensor Power Configuration
                        MainActivity.delayMS(50);

                        tranceiveWriteEEPROM(nfcVTag, (byte)0x14, writeCommand14); // Sensor  Trimming
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x15, writeCommand15); // Sensor 0 Control Word
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x16, writeCommand16); // Sensor 0 ThreshLow
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x17, writeCommand17); // Sensor 0 ThreshHigh
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x18, writeCommand18); // Sensor 0 Conditioner Config
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x19, writeCommand19); // Sensor 0 Connection Config
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x1A, writeCommand1A); // Sensor 0 Resistance Network
                        MainActivity.delayMS(50);
                        /*tranceiveWriteEEPROM(nfcVTag, (byte)0x1B, writeCommand1B); // Sensor 1 Control Word
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x1C, writeCommand1C); // Sensor 1 ThreshLow
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x1D, writeCommand1D); // Sensor 1 ThreshHigh
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x1E, writeCommand1E); // Sensor 1 Conditioner Config
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x1F, writeCommand1F); // Sensor 1 Connection Config
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x20, writeCommand20); // Sensor 1 Resistance Network*/
                    }
                    //TODO: Set condition for checking if SPI is available, then ungrey using [mCheckBoxSPI.setEnabled(true);]
                    //: If either sensor is disabled, grey out its settings and change the color of TV to Grey.
                }
            }
        }
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        switch (view.getId()) {
            case R.id.checkBoxWriteNewSettings:
                writeToDevice = checked;
                break;
            case R.id.checkBoxEnableSensor0:
                //TODO: IF SENSOR DISABLED; GREY OUT SETTINGS
                enableSensor0 = checked;
                Log.e(TAG, "checkBoxEnableSensor0: "+Boolean.toString(enableSensor0));
                break;
            case R.id.checkBoxSensor0Chopper:
                sensor0Chopper = checked;
                break;
        }
    }

    public void onRadioButtonClickedSensorConfig(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radioButton7:
                if(checked) sensor0Settings = 0;
                break;
            case R.id.radioButton8:
                if(checked) sensor0Settings = 1;
                break;
            case R.id.radioButton13:
                if(checked) sensor0Settings = 2;
                break;
            case R.id.radioButton14:
                if(checked) sensor0Settings = 3;
        }
    }

    public void onRadioButtonClickedWriteFrom(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radioButton:
                if(checked) writeFromLocation = 0;//ADC 0 (DEFAULT)
                Log.i(TAG, "writeFromLocation: "+String.valueOf(writeFromLocation));
                break;
            case R.id.radioButton2:
                if(checked) writeFromLocation = 1;//Internal 1
                Log.i(TAG, "writeFromLocation: "+String.valueOf(writeFromLocation));
                break;
            case R.id.radioButton3:
                if(checked) writeFromLocation = 2;//Register 2
                Log.i(TAG, "writeFromLocation: "+String.valueOf(writeFromLocation));
                break;
            case R.id.radioButtonWriteFromExternal:
                if(checked) writeFromLocation = 3;//External 3
                Log.i(TAG, "writeFromLocation: "+String.valueOf(writeFromLocation));
                break;
        }
    }

    public void onRadioButtonClickedWriteTo(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radioButton4:
                if(checked) writeToLocation = 0;//ADC
                break;
            case R.id.radioButton5: //Internal
                if(checked) writeToLocation = 1; //(DEFAULT, internal)
                break;
            case R.id.radioButton6:
                if(checked) writeToLocation = 2; //reg file.
                break;
            case R.id.radioButtonWriteToExternal:
                if(checked) writeToLocation = 3;//External/ SPI
                break;
        }
    }

    public void onRadioButtonClickedTimerUnits(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radioButton9:
                if(checked) timerUnits = 0; //hrs
                mTextViewTimerUnits.setText("h");
                break;
            case R.id.radioButton10:
                if(checked) timerUnits = 1; //mins
                mTextViewTimerUnits.setText("min");
                break;
            case R.id.radioButton11:
                if(checked) timerUnits = 2; //secs
                mTextViewTimerUnits.setText("s");
                break;
            case R.id.radioButton12:
                if(checked) timerUnits = 3; //ms
                mTextViewTimerUnits.setText("ms");
                break;
        }
    }

    public static final int byteA = 0;
    public static final int byteB = 1;

    protected byte[] tranceiveWriteEEPROM(NfcV nfcVTag, byte address, byte[] command) {
        byte[] response = {(byte) 0xFF};
        try {
            //                                 flag,       action,
            byte[] WriteSingleBlockFrame = {(byte)0x43, (byte)0x21, address, command[byteA], command[byteB]};
            response = nfcVTag.transceive(WriteSingleBlockFrame);
            if(response[0]==(byte)0x00) {
                Log.i(TAG, "Success: Write EEPROM");
            } else {
                Log.e(TAG, "Fail: Write EEPROM, error code: " + String.valueOf((response[0] & 0xFF)));
            }
            return response;
        } catch (Exception e) {
            Log.e(TAG, "Fail: Write EEPROM Exception");
            return response;
        }
    }

    byte[] swap2Bytes(byte[] b) {
        byte[] switched = new byte[2];
        if(b.length==2) {
            switched[1] = b[0];
            switched[0] = b[1];
        }
        return switched;
    }

    byte[] intTo2Bytes(int i)
    {
        byte[] result = new byte[2];

        result[0] = (byte) (i>>8);
        result[1] = (byte) (i);

//        result[0] = (byte) (i >> 24);
//        result[1] = (byte) (i >> 16);
//        result[2] = (byte) (i >> 8);
//        result[3] = (byte) (i /*>> 0*/);

        return result;
    }

    byte intToSingleByte(int i) {
        byte result = (byte)(i);
        return result;
    }

    public static byte[] StringToByteArray(String hex) {
        return new BigInteger(hex,16).toByteArray();
    }
}
