package com.mahmoodms.nfcvmlx.nfcappmlx;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

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
    CheckBox mCheckBoxSPI;
    //RadioButtons:
    RadioButton mRadioButtonWriteFromExternal;
    RadioButton mRadioButtonWriteToExternal;
    //EditText
    EditText mEditTextTimerCountdown;
    EditText mEditTextSensor0Mux;
    EditText mEditTextSensor0Resistance;
    EditText mEditTextSensor1Mux;
    EditText mEditTextSensor1Resistance;
    //TextViews:
    TextView mTextViewTimerUnits;
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
    private boolean sensor0Chopper = true;
    private boolean sensor0LowPowerMode = true;
    private boolean sensor1Mux = true;
    private boolean sensor1Resistance = true;
    private boolean sensor1Chopper = true;
    private boolean sensor1LowPowerMode = true;
    private boolean timerIRQ = false;
    private boolean writeToDevice = false;

    private int writeFromLocation = 0;
    private int writeToLocation = 1;
    private boolean timerStandby = true;
    private int timerUnits = 2;
    private int sensor0ADC = 2;
    private int sensor1ADC = 2;
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
        mCheckBoxSPI = (CheckBox) findViewById(R.id.checkBoxSPI);
        mCheckBoxSPI.setEnabled(false);
        mRadioButtonWriteFromExternal = (RadioButton) findViewById(R.id.radioButtonWriteFromExternal);
        mRadioButtonWriteFromExternal.setVisibility(View.GONE);
        mRadioButtonWriteToExternal = (RadioButton) findViewById(R.id.radioButtonWriteToExternal);
        mRadioButtonWriteToExternal.setVisibility(View.GONE);
        mEditTextTimerCountdown = (EditText) findViewById(R.id.editTextTimerCountdown);
        mEditTextSensor0Mux = (EditText) findViewById(R.id.editTextSensor0Mux);
        mEditTextSensor0Resistance = (EditText) findViewById(R.id.editTextSensor0Resistance);
        mEditTextSensor1Mux = (EditText) findViewById(R.id.editTextSensor1Mux);
        mEditTextSensor1Resistance = (EditText) findViewById(R.id.editTextSensor1Resistance);
        mTextViewTimerUnits = (TextView) findViewById(R.id.textViewTimerUnits);
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
        byte[] writeCommand09 = new byte[2];
        //Byte B:
        if(enableTimeStamps) {
            writeCommand09[1] = (byte)(writeCommand09[1] | 0b10000000);
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
        Log.e(TAG, "0x09 Command: "+ ViewConfig.toHexStringBigEndian(writeCommand09));
        byte[] writeCommand0A = {(byte)0x00, (byte)0x00};
        byte[] writeCommand0B = {(byte)0x00, (byte)0x00};
        byte[] writeCommand0C = {(byte)0x00, (byte)0x00};
        byte[] writeCommand0D = {(byte)0x00, (byte)0x00};
        byte[] writeCommand0E = {(byte)0x00, (byte)0x00};
        byte[] writeCommand0F = new byte[2];
        byte[] writeCommand10 = {(byte)0x00, (byte)0x00};
        byte[] writeCommand12 = {(byte)0xFF, (byte)0x00}; //Default Sensor Power Config
        byte[] writeCommand14 = {(byte)0x00, (byte)0x00}; //Default Sensor trimming
        byte[] writeCommand15 = {(byte)0x71, (byte)0x00}; //Default Sensor 0 Control word:
        byte[] writeCommand16 = {(byte)0x00, (byte)0x00}; //Default Sensor 0 threshold (0):
        byte[] writeCommand17 = {(byte)0x00, (byte)0x00}; //Default Sensor 0 threshold (0):
        byte[] writeCommand18 = {(byte)0x00, (byte)0x00}; //Default Sensor 0 Conditioner Config
        byte[] writeCommand19 = {(byte)0x00, (byte)0x00}; //Default Sensor 0 Connection Config
        byte[] writeCommand1A = {(byte)0x00, (byte)0x00}; //Default Sensor 0 Resistance Network
        byte[] writeCommand1B = {(byte)0x71, (byte)0x00}; //Default Sensor 1 Control word: (same default config)
        byte[] writeCommand1C = {(byte)0x00, (byte)0x00}; //Default Sensor 1 threshold (0):
        byte[] writeCommand1D = {(byte)0x00, (byte)0x00}; //Default Sensor 1 threshold (0):
        byte[] writeCommand1E = {(byte)0x00, (byte)0x00}; //Default Sensor 1 Conditioner Config
        byte[] writeCommand1F = {(byte)0x00, (byte)0x00}; //Default Sensor 1 Connection Config
        byte[] writeCommand20 = {(byte)0x00, (byte)0x00}; //Default Sensor 1 Resistance Network
        // Store all samples; mean of 2 samples A: [0 1 1 1 0 0 1 1] B: [00000000]
        if(defaultDMASettings) {
            if(writeFromLocation==0) {
                writeCommand0A[0] = (byte) 0x00; writeCommand0A[1] = (byte) 0x00;
            }
            if(writeToLocation==1) {
                writeCommand0B[0] = (byte)0x29; writeCommand0B[1] = (byte) 0x00;
                writeCommand0C[0] = (byte)0xD7; writeCommand0C[1] = (byte) 0x00;
            }
        }
        String s1 = mEditTextTimerCountdown.getText().toString();
        int parsedInt = Integer.parseInt(s1);
        if(parsedInt<65535 && parsedInt>0) {
            writeCommand0F = swap2Bytes(intTo2Bytes(parsedInt)); //SWAPPED FOR PROGRAMMING!
            byte[] byte0 = {writeCommand0F[0]}; //byte A
            byte[] byte1 = {writeCommand0F[1]}; //byte B
            Log.e(TAG, "byte0: "+ ViewConfig.toHexStringBigEndian(byte0));
            Log.e(TAG, "byte1: "+ ViewConfig.toHexStringBigEndian(byte1));
            Log.e(TAG,"bytes[0][1] Hex: parsedBytes: 0x"+ViewConfig.toHexStringBigEndian(writeCommand0F));
        } else {
            Toast.makeText(getApplicationContext(), "Timer Period Invalid\nMust be between 1 and 65535", Toast.LENGTH_SHORT).show();
        }
        //TODO: When I have time: SPI


        //Timer Control
        //standby enable?
        if(timerStandby) {
            writeCommand10[0] |= (byte)0b00001000;
        } //else do nothing
        if(automaticDataLoggingTimer) {
            writeCommand10[0] |= (byte)0b00000100;
        } else {
            writeCommand10[0] |= (byte)0b00000000;
        }
        //Timer IRQ
        if(timerIRQ) {
            writeCommand10[0] |= (byte)0b00000001;
        }
        switch (timerUnits) {
            case 0:
                writeCommand10[0] |= (byte)0b00110000;
                break;
            case 1:
                writeCommand10[0] |= (byte)0b00100000;
                break;
            case 2:
                writeCommand10[0] |= (byte)0b00010000;
                break;
            case 3:
                writeCommand10[0] |= (byte)0b00000000;
                break;
        }

        if(sensor0LowPowerMode) {
            writeCommand15[0] |= 0b10000000;
        }

        switch (sensor0ADC) {
            case 4: //slowest
                writeCommand15[1] |= 0b11000000;
                break;
            case 3:
                writeCommand15[1] |= 0b10000000;
                break;
            case 2:
                writeCommand15[1] |= 0b01000000;
                break;
            case 1: //fastest
//                            writeCommand15[1] |= 0b00000000;
                break;
        }
        if(sensor0Chopper) {
            writeCommand16[1] |= 0b10000000;
        }
        if(sensor0Mux) {
            String s2 = mEditTextSensor0Mux.getText().toString();
            try {
                int parsedHex = Integer.parseInt(s2, 16);//561
                Log.e(TAG,"Int: parsedInt: "+String.valueOf(parsedHex));
                writeCommand19 = swap2Bytes(intTo2Bytes(parsedHex)); //SWAPPED
                byte[] byte2 = {writeCommand19[0]};
                byte[] byte3 = {writeCommand19[1]};
                Log.e(TAG, "Sensor 0 Connection Config byte0(A): "+ ViewConfig.toHexStringBigEndian(byte2));
                Log.e(TAG, "Sensor 0 Connection Config byte1: "+ ViewConfig.toHexStringBigEndian(byte3));
            } catch (NumberFormatException e) {
                Log.e(TAG,"Not Valid Hex (Sensor 0)");
                Toast.makeText(getApplicationContext(), "Not Valid Hex (Sensor 0 Connection Config)", Toast.LENGTH_SHORT).show();
            }
        }
        if(sensor0Resistance) {
            String s2 = mEditTextSensor0Resistance.getText().toString();
            try {
                int parsedHex = Integer.parseInt(s2, 16);//561
                Log.e(TAG,"Int: parsedInt: "+String.valueOf(parsedHex));
                writeCommand1A = swap2Bytes(intTo2Bytes(parsedHex)); //SWAPPED
                byte[] byte2 = {writeCommand1A[0]};
                byte[] byte3 = {writeCommand1A[1]};
                Log.e(TAG, "Sensor 0 Resistance Config byte0(A): "+ ViewConfig.toHexStringBigEndian(byte2));
                Log.e(TAG, "Sensor 0 Resistance Config byte1: "+ ViewConfig.toHexStringBigEndian(byte3));
            } catch (NumberFormatException e) {
                Log.e(TAG,"Not Valid Hex (Sensor 0 Resistance)");
                Toast.makeText(getApplicationContext(), "Not Valid Hex (Sensor 0 Resistance)", Toast.LENGTH_SHORT).show();
            }
        }
        if(enableSensor0) {
            writeCommand1A[1] |= 0b10000000;
        }
        if(sensor1LowPowerMode) {
            writeCommand1B[0] |= 0b10000000;
        }
        switch (sensor1ADC) {
            case 4: //slowest
                writeCommand1B[1] |= 0b11000000;
                break;
            case 3:
                writeCommand1B[1] |= 0b10000000;
                break;
            case 2:
                writeCommand1B[1] |= 0b01000000;
                break;
            case 1: //fastest
//                            writeCommand15[1] |= 0b00000000;
                break;
        }
        if(sensor1Chopper) {
            writeCommand1E[1] |= 0b10000000;
        }
        if(sensor1Mux) {
            String s3 = mEditTextSensor1Mux.getText().toString();
            try {
                int parsedHex = Integer.parseInt(s3, 16);
                Log.e(TAG,"Int: parsedInt: "+String.valueOf(parsedHex));
                writeCommand1F = swap2Bytes(intTo2Bytes(parsedHex));
                byte[] byte2 = {writeCommand1F[0]};
                byte[] byte3 = {writeCommand1F[1]};
                Log.e(TAG, "Sensor 1 Connection Config byte0(A): "+ ViewConfig.toHexStringBigEndian(byte2));
                Log.e(TAG, "Sensor 1 Connection Config byte1: "+ ViewConfig.toHexStringBigEndian(byte3));
            } catch (NumberFormatException e) {
                Log.e(TAG,"Not Valid Hex (Sensor 1)");
                Toast.makeText(getApplicationContext(), "Not Valid Hex (Sensor 1 Connection Config)", Toast.LENGTH_SHORT).show();
            }
        }

        if(sensor1Resistance) {
            String s2 = mEditTextSensor1Resistance.getText().toString();
            try {
                int parsedHex = Integer.parseInt(s2, 16);
                Log.e(TAG,"Int: parsedInt: "+String.valueOf(parsedHex));
                writeCommand20 = swap2Bytes(intTo2Bytes(parsedHex)); //SWAPPED
                byte[] byte2 = {writeCommand20[0]};
                byte[] byte3 = {writeCommand20[1]};
                Log.e(TAG, "Sensor 1 Resistance Config byte0(A): "+ ViewConfig.toHexStringBigEndian(byte2));
                Log.e(TAG, "Sensor 1 Resistance Config byte1: "+ ViewConfig.toHexStringBigEndian(byte3));
                Log.e(TAG, "Sensor 1 Resistance Config Full: "+ViewConfig.toHexStringBigEndian(writeCommand20));
            } catch (NumberFormatException e) {
                Log.e(TAG,"Not Valid Hex (Sensor 1 Resistance)");
                Toast.makeText(getApplicationContext(), "Not Valid Hex (Sensor 1 Resistance)", Toast.LENGTH_SHORT).show();
            }
        }
        //VERIFY VIA LOGGING:
        Log.e(TAG,"Write[0x##] = 0xAABB - In order they are programmed");
        Log.e(TAG,"Write[0x09] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand09));
        Log.e(TAG,"Write[0x0A] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0A));
        Log.e(TAG,"Write[0x0B] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0B));
        Log.e(TAG,"Write[0x0C] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0C));
        Log.e(TAG,"Write[0x0D] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0D));
        Log.e(TAG,"Write[0x0E] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0E));
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
        Log.e(TAG,"Write[0x1B] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1B));
        Log.e(TAG,"Write[0x1C] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1C));
        Log.e(TAG,"Write[0x1D] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1D));
        Log.e(TAG,"Write[0x1E] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1E));
        Log.e(TAG,"Write[0x1F] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1F));
        Log.e(TAG,"Write[0x20] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand20));
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
                    byte[] writeCommand09 = new byte[2];
                    //Byte B:
                    if(enableTimeStamps) {
                        writeCommand09[1] = (byte)(writeCommand09[1] | 0b10000000);
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
                    Log.e(TAG, "0x09 Command: "+ ViewConfig.toHexStringBigEndian(writeCommand09));
                    byte[] writeCommand0A = {(byte)0x00, (byte)0x00};
                    byte[] writeCommand0B = {(byte)0x00, (byte)0x00};
                    byte[] writeCommand0C = {(byte)0x00, (byte)0x00};
                    byte[] writeCommand0D = {(byte)0x00, (byte)0x00};
                    byte[] writeCommand0E = {(byte)0x00, (byte)0x00};
                    byte[] writeCommand0F = new byte[2];
                    byte[] writeCommand10 = {(byte)0x00, (byte)0x00};
                    byte[] writeCommand12 = {(byte)0xFF, (byte)0x00}; //Default Sensor Power Config
                    byte[] writeCommand14 = {(byte)0x00, (byte)0x00}; //Default Sensor trimming
                    byte[] writeCommand15 = {(byte)0x71, (byte)0x00}; //Default Sensor 0 Control word:
                    byte[] writeCommand16 = {(byte)0x00, (byte)0x00}; //Default Sensor 0 threshold (0):
                    byte[] writeCommand17 = {(byte)0x00, (byte)0x00}; //Default Sensor 0 threshold (0):
                    byte[] writeCommand18 = {(byte)0x00, (byte)0x00}; //Default Sensor 0 Conditioner Config
                    byte[] writeCommand19 = {(byte)0x00, (byte)0x00}; //Default Sensor 0 Connection Config
                    byte[] writeCommand1A = {(byte)0x00, (byte)0x00}; //Default Sensor 0 Resistance Network
                    //TODO: FIX THIS CONFIGURATION
                    byte[] writeCommand1B = {(byte)0x71, (byte)0x00}; //Default Sensor 1 Control word: (same default config)
                    byte[] writeCommand1C = {(byte)0x00, (byte)0x00}; //Default Sensor 1 threshold (0):
                    byte[] writeCommand1D = {(byte)0x00, (byte)0x00}; //Default Sensor 1 threshold (0):
                    byte[] writeCommand1E = {(byte)0x00, (byte)0x00}; //Default Sensor 1 Conditioner Config
                    byte[] writeCommand1F = {(byte)0x00, (byte)0x00}; //Default Sensor 1 Connection Config
                    byte[] writeCommand20 = {(byte)0x00, (byte)0x00}; //Default Sensor 1 Resistance Network
                    // Store all samples; mean of 2 samples A: [0 1 1 1 0 0 1 1] B: [00000000]
                    if(defaultDMASettings) {
                        if(writeFromLocation==0) {
                            writeCommand0A[0] = (byte) 0x00; writeCommand0A[1] = (byte) 0x00;
                        }
                        if(writeToLocation==1) {
                            writeCommand0B[0] = (byte)0x29; writeCommand0B[1] = (byte) 0x00;
                            writeCommand0C[0] = (byte)0xD7; writeCommand0C[1] = (byte) 0x00;
                        }
                    }
                    String s1 = mEditTextTimerCountdown.getText().toString();
                    int parsedInt = Integer.parseInt(s1);
                    if(parsedInt<65535 && parsedInt>0) {
                        writeCommand0F = swap2Bytes(intTo2Bytes(parsedInt)); //SWAPPED FOR PROGRAMMING!
                        byte[] byte0 = {writeCommand0F[0]}; //byte A
                        byte[] byte1 = {writeCommand0F[1]}; //byte B
                        Log.e(TAG, "byte0: "+ ViewConfig.toHexStringBigEndian(byte0));
                        Log.e(TAG, "byte1: "+ ViewConfig.toHexStringBigEndian(byte1));
                        Log.e(TAG,"bytes[0][1] Hex: parsedBytes: 0x"+ViewConfig.toHexStringBigEndian(writeCommand0F));
                    } else {
                        Toast.makeText(getApplicationContext(), "Timer Period Invalid\nMust be between 1 and 65535", Toast.LENGTH_SHORT).show();
                    }
                    //TODO: When I have time: SPI


                    //Timer Control
                    //standby enable?
                    if(timerStandby) {
                        writeCommand10[0] |= (byte)0b00001000;
                    } //else do nothing
                    if(automaticDataLoggingTimer) {
                        writeCommand10[0] |= (byte)0b00000100;
                    } else {
                        writeCommand10[0] |= (byte)0b00000000;
                    }
                    //Timer IRQ
                    if(timerIRQ) {
                        writeCommand10[0] |= (byte)0b00000001;
                    }
                    switch (timerUnits) {
                        case 0:
                            writeCommand10[0] |= (byte)0b00110000;
                            break;
                        case 1:
                            writeCommand10[0] |= (byte)0b00100000;
                            break;
                        case 2:
                            writeCommand10[0] |= (byte)0b00010000;
                            break;
                        case 3:
                            writeCommand10[0] |= (byte)0b00000000;
                            break;
                    }

                    if(sensor0LowPowerMode) {
                        writeCommand15[0] |= 0b10000000;
                    }

                    switch (sensor0ADC) {
                        case 4: //slowest
                            writeCommand15[1] |= 0b11000000;
                            break;
                        case 3:
                            writeCommand15[1] |= 0b10000000;
                            break;
                        case 2:
                            writeCommand15[1] |= 0b01000000;
                            break;
                        case 1: //fastest
//                            writeCommand15[1] |= 0b00000000;
                            break;
                    }
                    if(sensor0Chopper) {
                        writeCommand18[1] |= 0b10000000;
                    }
                    if(sensor0Mux) {
                        String s2 = mEditTextSensor0Mux.getText().toString();
                        try {
                            int parsedHex = Integer.parseInt(s2, 16);//561
                            Log.e(TAG,"Int: parsedInt: "+String.valueOf(parsedHex));
                            writeCommand19 = swap2Bytes(intTo2Bytes(parsedHex)); //SWAPPED
                            byte[] byte2 = {writeCommand19[0]};
                            byte[] byte3 = {writeCommand19[1]};
                            Log.e(TAG, "Sensor 0 Connection Config byte0(A): "+ ViewConfig.toHexStringBigEndian(byte2));
                            Log.e(TAG, "Sensor 0 Connection Config byte1: "+ ViewConfig.toHexStringBigEndian(byte3));
                        } catch (NumberFormatException e) {
                            Log.e(TAG,"Not Valid Hex (Sensor 0)");
                            Toast.makeText(getApplicationContext(), "Not Valid Hex (Sensor 0 Connection Config)", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(sensor0Resistance) {
                        String s2 = mEditTextSensor0Resistance.getText().toString();
                        try {
                            int parsedHex = Integer.parseInt(s2, 16);//561
                            Log.e(TAG,"Int: parsedInt: "+String.valueOf(parsedHex));
                            writeCommand1A = swap2Bytes(intTo2Bytes(parsedHex)); //SWAPPED
                            byte[] byte2 = {writeCommand1A[0]};
                            byte[] byte3 = {writeCommand1A[1]};
                            Log.e(TAG, "Sensor 0 Resistance Config byte0(A): "+ ViewConfig.toHexStringBigEndian(byte2));
                            Log.e(TAG, "Sensor 0 Resistance Config byte1: "+ ViewConfig.toHexStringBigEndian(byte3));
                        } catch (NumberFormatException e) {
                            Log.e(TAG,"Not Valid Hex (Sensor 0 Resistance)");
                            Toast.makeText(getApplicationContext(), "Not Valid Hex (Sensor 0 Resistance)", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(enableSensor0) {
                        writeCommand1A[1] |= 0b10000000;
                    }
                    if(sensor1LowPowerMode) {
                        writeCommand1B[0] |= 0b10000000;
                    }
                    switch (sensor1ADC) {
                        case 4: //slowest
                            writeCommand1B[1] |= 0b11000000;
                            break;
                        case 3:
                            writeCommand1B[1] |= 0b10000000;
                            break;
                        case 2:
                            writeCommand1B[1] |= 0b01000000;
                            break;
                        case 1: //fastest
//                            writeCommand15[1] |= 0b00000000;
                            break;
                    }
                    if(sensor1Chopper) {
                        writeCommand1E[1] |= 0b10000000;
//                        writeCommand1E[1] |= 0b00001010;
                    }
                    if(sensor1Mux) {
                        String s3 = mEditTextSensor1Mux.getText().toString();
                        try {
                            int parsedHex = Integer.parseInt(s3, 16);
                            Log.e(TAG,"Int: parsedInt: "+String.valueOf(parsedHex));
                            writeCommand1F = swap2Bytes(intTo2Bytes(parsedHex));
                            byte[] byte2 = {writeCommand1F[0]};
                            byte[] byte3 = {writeCommand1F[1]};
                            Log.e(TAG, "Sensor 1 Connection Config byte0(A): "+ ViewConfig.toHexStringBigEndian(byte2));
                            Log.e(TAG, "Sensor 1 Connection Config byte1: "+ ViewConfig.toHexStringBigEndian(byte3));
                        } catch (NumberFormatException e) {
                            Log.e(TAG,"Not Valid Hex (Sensor 1)");
                            Toast.makeText(getApplicationContext(), "Not Valid Hex (Sensor 1 Connection Config)", Toast.LENGTH_SHORT).show();
                        }
                    }

                    if(sensor1Resistance) {
                        String s2 = mEditTextSensor1Resistance.getText().toString();
                        try {
                            int parsedHex = Integer.parseInt(s2, 16);
                            Log.e(TAG,"Int: parsedInt: "+String.valueOf(parsedHex));
                            writeCommand20 = swap2Bytes(intTo2Bytes(parsedHex)); //SWAPPED
                            byte[] byte2 = {writeCommand20[0]};
                            byte[] byte3 = {writeCommand20[1]};
                            Log.e(TAG, "Sensor 1 Resistance Config byte0(A): "+ ViewConfig.toHexStringBigEndian(byte2));
                            Log.e(TAG, "Sensor 1 Resistance Config byte1: "+ ViewConfig.toHexStringBigEndian(byte3));
                            Log.e(TAG, "Sensor 1 Resistance Config Full: "+ViewConfig.toHexStringBigEndian(writeCommand20));
                        } catch (NumberFormatException e) {
                            Log.e(TAG,"Not Valid Hex (Sensor 1 Resistance)");
                            Toast.makeText(getApplicationContext(), "Not Valid Hex (Sensor 1 Resistance)", Toast.LENGTH_SHORT).show();
                        }
                    }
                    //VERIFY VIA LOGGING:
                    Log.e(TAG,"Write[0x##] = 0xAABB - In order they are programmed");
                    Log.e(TAG,"Write[0x09] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand09));
                    Log.e(TAG,"Write[0x0A] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0A));
                    Log.e(TAG,"Write[0x0B] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0B));
                    Log.e(TAG,"Write[0x0C] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0C));
                    Log.e(TAG,"Write[0x0D] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0D));
                    Log.e(TAG,"Write[0x0E] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand0E));
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
                    Log.e(TAG,"Write[0x1B] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1B));
                    Log.e(TAG,"Write[0x1C] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1C));
                    Log.e(TAG,"Write[0x1D] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1D));
                    Log.e(TAG,"Write[0x1E] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1E));
                    Log.e(TAG,"Write[0x1F] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand1F));
                    Log.e(TAG,"Write[0x20] = 0x"+ViewConfig.toHexStringBigEndian(writeCommand20));
                    //WRITE:                 Tag,    address, commandBytes[A, B]
                    if(writeToDevice) {
                        /*tranceiveWriteEEPROM(nfcVTag, (byte)0x09, writeCommand09); // DMA Configuration
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x0A, writeCommand0A); // DMA Source Start Address
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x0B, writeCommand0B); // DMA Destination Start Address
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x0C, writeCommand0C); // DMA Processing Length
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x0D, writeCommand0D); // SPI Config
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x0E, writeCommand0E); // SPI Command Codes
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
                        MainActivity.delayMS(50);*/
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x1B, writeCommand1B); // Sensor 1 Control Word
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x1C, writeCommand1C); // Sensor 1 ThreshLow
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x1D, writeCommand1D); // Sensor 1 ThreshHigh
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x1E, writeCommand1E); // Sensor 1 Conditioner Config
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x1F, writeCommand1F); // Sensor 1 Connection Config
                        MainActivity.delayMS(50);
                        tranceiveWriteEEPROM(nfcVTag, (byte)0x20, writeCommand20); // Sensor 1 Resistance Network
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
            case R.id.checkBoxEnableSensor1:
                //TODO: IF SENSOR DISABLED; GREY OUT SETTINGS
                enableSensor1 = checked;
                Log.e(TAG, "checkBoxEnableSensor1: "+Boolean.toString(enableSensor1));
                break;
            case R.id.checkBoxTimeStamps:
                enableTimeStamps = checked;
                break;
            case R.id.checkBoxSPI:
                enableSPI = checked;
                Log.e(TAG, "checkBoxSPI: "+Boolean.toString(enableSPI));
                break;
            case R.id.checkBoxDefaultSettingsDMA:
                defaultDMASettings = checked;
                break;
            case R.id.checkBoxManualDMA:
                manualDMA = checked;
                break;
            case R.id.checkBoxAutomaticLoggingTimer:
                automaticDataLoggingTimer = checked;
                break;
            case R.id.timerStandby:
                timerStandby = checked;
                Log.e(TAG, "Timer Standby: "+Boolean.toString(timerStandby));
                break;
            case R.id.timerIRQ:
                timerIRQ = checked;
                break;
            case R.id.checkBoxSensor0Resistance:
                sensor0Resistance = checked;
                break;
            case R.id.checkBoxSensor0InputMux:
                sensor0Mux = checked;
                break;
            case R.id.checkBoxSensor0Chopper:
                sensor0Chopper = checked;
                break;
            case R.id.checkBoxSensor0LowPowerMode:
                sensor0LowPowerMode = checked;
                break;
            case R.id.checkBoxSensor1InputMux:
                sensor1Mux = checked;
                break;
            case R.id.checkBoxSensor1Resistance:
                sensor1Resistance = checked;
                break;
            case R.id.checkBoxSensor1Chopper:
                sensor1Chopper = checked;
                break;
            case R.id.checkBoxSensor1LowPowerMode:
                sensor1LowPowerMode = checked;
                break;

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

    public void onRadioButtonClickedSensor0ADC(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radioButton13:
                if(checked) sensor0ADC = 4; // Slowest Most Accurate
                break;
            case R.id.radioButton14:
                if(checked) sensor0ADC = 3; // Slow Accurate
                break;
            case R.id.radioButton15:
                if(checked) sensor0ADC = 2; // Fast, Accurate
                break;
            case R.id.radioButton16:
                if(checked) sensor0ADC = 1; // Fastest, Most Accurate
                break;
        }
    }

    public void onRadioButtonClickedSensor1ADC(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radioButton17:
                if(checked) sensor1ADC = 4;
                break;
            case R.id.radioButton18:
                if(checked) sensor1ADC = 3;
                break;
            case R.id.radioButton19:
                if(checked) sensor1ADC = 2;
                break;
            case R.id.radioButton20:
                if(checked) sensor1ADC = 1;
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

    /*protected byte[] tranceiveWriteEEPROM(NfcV nfcVTag, byte address, byte commandByte0, byte commandByte1) {
        byte[] response = {(byte) 0xFF};
        try {
            //                                 flag,       action,
            byte[] WriteSingleBlockFrame = {(byte)0x43, (byte)0x21, commandByte0, commandByte1};
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
    }*/
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

    public static byte[] StringToByteArray(String hex) {
        return new BigInteger(hex,16).toByteArray();
    }
}