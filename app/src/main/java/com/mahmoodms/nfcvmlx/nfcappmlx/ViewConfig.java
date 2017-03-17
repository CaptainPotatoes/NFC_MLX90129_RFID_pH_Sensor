package com.mahmoodms.nfcvmlx.nfcappmlx;

import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by mahmoodms on 11/26/2016.
 */

public class ViewConfig extends Activity {
    //NFC Stuff:
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private Tag mCurrentTag;

    //ListView Stuff:
    private ListView listViewConfig;
    private String[] listA = {"Touch NFC to View Configuration"};
    //TODO: (Probably) replace this with an ArrayList, which is added to and converted to an Array.
    private String[] eepromDataStrings = new String[22]; //[0→21]
    private TextView mNfcId;

    //Temp
    private Button mTempButton;
    private Button mSetConfig;
    private String TAG = "ViewConfigActivity";
    //NFC Tranceive Data:
    byte[][] allEEPROMData = new byte[24][];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageManager pm = getPackageManager();
        setContentView(R.layout.view_config);
        //Flag to keep screen on (stay-awake):
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (getActionBar()!=null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            ActionBar actionBar = getActionBar();
            actionBar.setBackgroundDrawable(MainActivity.actionBarTheme);
        }
        mTempButton = (Button) findViewById(R.id.button);
        mSetConfig = (Button) findViewById(R.id.set_config);
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

        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, R.layout.activity_listview, listA);
        listViewConfig = (ListView) findViewById(R.id.listView1);
        listViewConfig.setAdapter(arrayAdapter);
        mNfcId = (TextView) findViewById(R.id.nfc_id);
        final byte[] aaaaa = {(byte) 0x06, (byte) 0x80};
        //Initialize string var:
        for (int i = 0; i < eepromDataStrings.length; i++) {
            //TODO: If it does not get filled (i.e. scan does not go through)
            eepromDataStrings[i] = "";
        }
        //TODO: TEMPORARY Init Byte Array:
        for (int i = 0; i < allEEPROMData.length; i++) {
            allEEPROMData[i] = new byte[]{(byte)0x00,(byte)0x00,(byte)0x00};
        }
        mTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < eepromDataStrings.length; i++) {
                    eepromDataStrings[i] = "";
                    Context context = getApplicationContext();
                    ArrayAdapter arrayAdapter = new ArrayAdapter(context, R.layout.activity_listview, eepromDataStrings);
                    listViewConfig.setAdapter(arrayAdapter);
                }
            }
        });
//        mTempButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //TEMP:
//                allEEPROMData[1][0] = (byte) 0x00;allEEPROMData[1][1] = (byte) 0x00;allEEPROMData[1][2] = (byte) 0x00;
//                allEEPROMData[2][0] = (byte) 0x00;allEEPROMData[2][1] = (byte) 0x29;allEEPROMData[2][2] = (byte) 0x00;
//                allEEPROMData[3][0] = (byte) 0x00;allEEPROMData[3][1] = (byte) 0xD7;allEEPROMData[3][2] = (byte) 0x00;
//                allEEPROMData[6][0] = (byte) 0x00;allEEPROMData[6][1] = (byte) 0x01;allEEPROMData[6][2] = (byte) 0x00;
//                allEEPROMData[16][0] = (byte) 0x00;allEEPROMData[16][1] = (byte) 0x31;allEEPROMData[16][2] = (byte) 0x02;
//                allEEPROMData[22][0] = (byte) 0x00;allEEPROMData[22][1] = (byte) 0x23;allEEPROMData[22][2] = (byte) 0x01;
//                Log.e("BIG ENDIAN", toHexStringBigEndian(allEEPROMData[22]));
//                Log.e("LITTLE ENDIAN", toHexStringLittleEndian(allEEPROMData[22]));
//                eepromDataStrings[0] = "Time Stamps Enabled";
//                eepromDataStrings[1] = "Sensor 0 Enabled in DMA";
//                eepromDataStrings[2] = "Sensor 1 Enabled in DMA";
//                eepromDataStrings[3] = "Writing Data to Internal EEPROM";
//                eepromDataStrings[3] += " From Sensor ADC Buffer";
//                eepromDataStrings[4] = "IRQ Disabled";
//                eepromDataStrings[5] = "Manual DMA Stop: For Automatic Datalogging";
//                byte[] dmaSourceStartAddress = {allEEPROMData[1][1],allEEPROMData[1][2]};
//                eepromDataStrings[6] = "DMA Source Start Address: 0x"+toHexStringLittleEndian(dmaSourceStartAddress); //Address 0x0A or "10"
//                byte[] dmaDestinationStartAddress = {allEEPROMData[2][1],allEEPROMData[2][2]};
//                eepromDataStrings[7] = "DMA Destination Start Address: 0x"+toHexStringLittleEndian(dmaDestinationStartAddress); //Address 0x0B or "11"
//                byte[] dmaProcessingLength = {allEEPROMData[3][1],allEEPROMData[3][2]};
//                eepromDataStrings[8] = "DMA Processing Length: 0x"+toHexStringLittleEndian(dmaProcessingLength); //Address 0x0C or "12"
//                eepromDataStrings[9] = "SPI Disabled";
//                eepromDataStrings[10] = "Automatic Logging Mode Enabled";
//                String unit = "SECONDS";
//                eepromDataStrings[11] = "Timer Countdown Period: 0x"+toHexStringLittleEndian(allEEPROMData[6])+" "+unit;
//                eepromDataStrings[12] = "Sensor 0 Enabled";
//                eepromDataStrings[13] = "Sensor 0 Low Power Mode Enabled";
//                eepromDataStrings[14] = "Sensor 0 Level 3 ADC Conversion (Slow, Accurate)";
//                eepromDataStrings[15] = "Sensor 0 Chopper Enabled";
//                byte[] mux = {allEEPROMData[16][1],allEEPROMData[16][2]};
//                eepromDataStrings[16] = "Sensor 0 Input Multiplexer Set: 0x"+toHexStringLittleEndian(mux);
//
//                eepromDataStrings[17] = "Sensor 1 Enabled";
//                eepromDataStrings[18] = "Sensor 1 Low Power Mode Enabled";
//                eepromDataStrings[19] = "Sensor 1 Level 4 ADC Conversion (Slowest, Most Accurate)";
//                eepromDataStrings[20] = "Sensor 1 Chopper Enabled";
//                byte[] mux1 = {allEEPROMData[22][1],allEEPROMData[22][2]};
//                eepromDataStrings[21] = "Sensor 1 Input Multiplexer Set: 0x"+toHexStringLittleEndian(mux1);
//                Context context = getApplicationContext();
//                ArrayAdapter arrayAdapter1 = new ArrayAdapter(context, R.layout.activity_listview, eepromDataStrings);
//                listViewConfig.setAdapter(arrayAdapter1);
//            }
//        });

        mSetConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WriteConfig.class);
                startActivity(intent);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            mCurrentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] id = mCurrentTag.getId();
            String hexID = "NFC TAG ID: "+toHexStringLittleEndian(id);
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
                    //Read Registers 0x09→0x020 for sensors 0 and 1:
                    //Data Matrix from 0→23 (i-9)
//                    String concat = "";
                    Log.e(TAG, "EEPROM Read ["+"0x##"+"] = 0xAABB");
                    byte[] address = {(byte) 0x09};
                    for (int i = address[0]; i < 0x21; i++) {
                        byte[] responseRead = tranceiveReadEEPROM(nfcVTag, address[0]);
                        allEEPROMData[i-0x09] = responseRead; //Add data to matrix
                        delayMS(120);
                        if(responseRead.length>2){
                            byte[] show = {responseRead[1], responseRead[2]};
                            //as stored on the device
                            Log.e(TAG, "EEPROM Read [0x"+toHexStringBigEndian(address)+"] = 0x"+toHexStringBigEndian(show));
                        }
                        /*String tempString = "#0x"+MainActivity.toHexString(address)+": "+"EEPROM: " + MainActivity.toHexString(responseRead)+"\n";
                        concat+= tempString;*/
                        address[0]++;
                    }
                    //Check if all transactions worked; If error, try again. (check length?)
                    //TODO: Re: above; add else conditions, to show transaction failed.
                    //TODO: Process and display data:
                    //Each response should be 3 bytes. First should be 0x00
                    //TODO: Format of data: [Error Code; Byte A (LSBits 0→7); Byte B (MSBits 8-15)]

                    // DMA (Direct Memory Access) Configuration (Byte B):
                    if(allEEPROMData[0].length>2) { //No error:
                        if( (allEEPROMData[0][2] & 0b10000000) == 0b10000000) {
                            eepromDataStrings[0] = "Time Stamps Enabled";
                        } else {
                            eepromDataStrings[0] = "Time Stamps Disabled";
                        }
                        if( (allEEPROMData[0][2] & 0b00110000) == 0b00110000) {
                            //If enabled, it means the sensor value will be saved.
                            eepromDataStrings[1] = "Sensor 0 Enabled in DMA";
                            eepromDataStrings[2] = "Sensor 1 Enabled in DMA";
                        } else if ( (allEEPROMData[0][2] & 0b00110000) == 0b00010000) {
                            eepromDataStrings[1] = "Sensor 0 Enabled in DMA";
                            eepromDataStrings[2] = "Sensor 1 Disabled in DMA";
                        } else if ( (allEEPROMData[0][2] & 0b00110000) == 0b00100000) {
                            eepromDataStrings[1] = "Sensor 0 Disabled in DMA";
                            eepromDataStrings[2] = "Sensor 1 Enabled in DMA";
                        } else {
                            eepromDataStrings[1] = "Sensor 0 Disabled in DMA";
                            eepromDataStrings[2] = "Sensor 1 Disabled in DMA";
                        }
                        // DMA Configuration (Byte A):
                        //TODO: Combine into one String.
                        if ((allEEPROMData[0][1] & 0b11000000) == 0b11000000) {
                            eepromDataStrings[3] = "Writing Data to Sensor ADC Buffer";
                        } else if ((allEEPROMData[0][1] & 0b11000000) == 0b10000000) {
                            eepromDataStrings[3] = "Writing Data to External EEPROM (SPI)";
                        } else if( (allEEPROMData[0][1] & 0b11000000) == 0b01000000) {
                            eepromDataStrings[3] = "Writing Data to Internal EEPROM";
                        } else {
                            eepromDataStrings[3] = "Writing Data to Register File";
                        }
                        // DMA Configuration Byte A:
                        if((allEEPROMData[0][1] & 0b00110000) == 0b00110000) {
                            eepromDataStrings[3] += " From Sensor ADC Buffer";
                        } else if((allEEPROMData[0][1] & 0b00110000) == 0b00010000) {
                            eepromDataStrings[3] += " From Internal EEPROM";
                        } else if((allEEPROMData[0][1] & 0b00110000) == 0b00100000) {
                            eepromDataStrings[3] += " From External EEPROM";
                        } else {
                            eepromDataStrings[3] += " From Register File";
                        }
                        if((allEEPROMData[0][1] & 0b00000100) == 0b00000100) {
                            eepromDataStrings[4] = "IRQ Enabled";
                        } else {
                            eepromDataStrings[4] = "IRQ Disabled";
                        }

                        if((allEEPROMData[0][1] & 0b00000001) == 0b00000001) {
                            eepromDataStrings[5] = "Manual DMA Start: Not for Automatic Datalogging";
                        } else {
                            eepromDataStrings[5] = "Manual DMA Stop: For Automatic Datalogging";
                        }
                    }
                    //TODO: May still need to switch endianness to show correct addresses (depending on responses from device)
                    if(allEEPROMData[1].length>2) {
                        byte[] dmaSourceStartAddress = {allEEPROMData[1][1],allEEPROMData[1][2]};
                        eepromDataStrings[6] = "DMA Source Start Address: 0x"+toHexStringLittleEndian(dmaSourceStartAddress); //Address 0x0A or "10"
                    }
                    if(allEEPROMData[2].length>2) {
                        byte[] dmaDestinationStartAddress = {allEEPROMData[2][1],allEEPROMData[2][2]};
                        eepromDataStrings[7] = "DMA Destination Start Address: 0x"+toHexStringLittleEndian(dmaDestinationStartAddress); //Address 0x0B or "11"
                    }
                    if(allEEPROMData[3].length>2) {
                        byte[] dmaProcessingLength = {allEEPROMData[3][1],allEEPROMData[3][2]};
                        eepromDataStrings[8] = "DMA Processing Length: 0x"+toHexStringLittleEndian(dmaProcessingLength); //Address 0x0C or "12"
                    }
                    if(allEEPROMData[4].length>2) {
                        if((allEEPROMData[4][1]==(byte)0x00 && allEEPROMData[4][2]==(byte)0x00)) { //Address 0x0D or "13"
                            //Address 0x0D or "13"
                            eepromDataStrings[9] = "SPI Disabled"; //if its 0x0000
                        } else {
                            eepromDataStrings[9] = "SPI Enabled"; // if it is not.
                        }
                    }
                    String unit = "";
                        //Skip address 0x0E (SPI-master command codes)
                    // Timer Control (0x15, 21): (Byte A)
                    //TODO: ADD IN STANDBY MODE TOGGLE; OTHERWISE DEFAULT.
                    if(allEEPROMData[7].length>2) {
                        if((allEEPROMData[7][1] & 0b00000100) == 0b00000100) {
                            eepromDataStrings[10] = "Timer Automatic Logging Mode Enabled";
                        } else {
                            eepromDataStrings[10] = "Timer Automatic Logging Mode Disabled";
                        }
                        //Timer Precision/Units (Byte A)
                        if((allEEPROMData[7][1] & 0b00110000) == 0b00110000) {
                            unit = "HOURS";
                        } else if((allEEPROMData[7][1] & 0b00110000) == 0b00100000) {
                            unit = "MINUTES";
                        }else if((allEEPROMData[7][1] & 0b00110000) == 0b00010000) {
                            unit = "SECONDS";
                        } else {
                            unit = "MILLISECONDS";
                        }
                    }
                    if(allEEPROMData[11].length>2) {
                        //Timer Period: (0x0F, 15)
                        eepromDataStrings[11] = "Timer Countdown Period: 0x"+toHexStringLittleEndian(allEEPROMData[6])+" "+unit;
                    }
                        //Skip address 0x11, 0x13, 0x14 (RFID User Register, Empty, Sensor Trimming)
                        //TODO: Sensor 0
                    //Sensor 0 Resistance Network Byte B:
                    if(allEEPROMData[17].length>2) {
                        if((allEEPROMData[17][2] & 0b10000000)==0b10000000) {
                            eepromDataStrings[12] = "Sensor 0 Enabled";
                        } else {
                            eepromDataStrings[12] = "Sensor 0 Disabled";
                        }
                    }
                    if(allEEPROMData[12].length>2) {
                        //If Sensor 0 is enabled:
                        //Sensor 0: Control Word (Byte A)
                        if((allEEPROMData[12][1] & 0b10000000) == 0b10000000) {
                            eepromDataStrings[13] = "Sensor 0 Low Power Mode Enabled";
                        } else {
                            eepromDataStrings[13] = "Sensor 0 Low Power Mode Disabled";
                        }
                        // Sensor 0: Control Word Byte B
                        if((allEEPROMData[12][2] & 0b11000000) == 0b11000000) {
                            eepromDataStrings[14] = "Sensor 0 Level 4 ADC Conversion (Slowest, Most Accurate)";
                        } else if((allEEPROMData[12][2] & 0b11000000) == 0b10000000) {
                            eepromDataStrings[14] = "Sensor 0 Level 3 ADC Conversion (Slow, Accurate)";
                        } else if((allEEPROMData[12][2] & 0b11000000) == 0b01000000) {
                            eepromDataStrings[14] = "Sensor 0 Level 2 ADC Conversion (Fast, Less Accurate)";
                        } else {
                            eepromDataStrings[14] = "Sensor 0 Level 1 ADC Conversion (Fastest, Least Accurate)";
                        }
                        //Sensor 0 conditioner config: (Byte B)
                        if((allEEPROMData[12][2] & 0b10000000) == 0b10000000) {
                            eepromDataStrings[15] = "Sensor 0 Chopper Enabled";
                        } else {
                            eepromDataStrings[15] = "Sensor 0 Chopper Disabled";
                        }
                    }
                    if(allEEPROMData[16].length>2) {
                        if((allEEPROMData[16][1]==(byte)0x00 && allEEPROMData[16][2]==(byte)0x00)) {
                            eepromDataStrings[16] = "Sensor 0 Input Multiplexer Not Set (0x0000)";
                        } else {
                            byte[] mux = {allEEPROMData[16][1],allEEPROMData[16][2]};
                            eepromDataStrings[16] = "Sensor 0 Input Multiplexer Set: 0x"+toHexStringLittleEndian(mux);
                        }
                    }



                        //TODO: Sensor 1:
                    if(allEEPROMData[23].length>2) {
                        if((allEEPROMData[23][2] & 0b10000000)==0b10000000) {
                            eepromDataStrings[17] = "Sensor 1 Enabled";
                        } else {
                            eepromDataStrings[17] = "Sensor 1 Disabled";
                        }
                    }
                    //If Sensor 1 is enabled:
                    //Sensor 1: Control Word (Byte A)
                    if(allEEPROMData[18].length>2) {
                        if((allEEPROMData[18][1] & 0b10000000) == 0b10000000) {
                            eepromDataStrings[18] = "Sensor 1 Low Power Mode Enabled";
                        } else {
                            eepromDataStrings[18] = "Sensor 1 Low Power Mode Disabled";
                        }
                        // Sensor 1: Control Word Byte B
                        if((allEEPROMData[18][2] & 0b11000000) == 0b11000000) {
                            eepromDataStrings[19] = "Sensor 1 Level 4 ADC Conversion (Slowest, Most Accurate)";
                        } else if((allEEPROMData[18][2] & 0b11000000) == 0b10000000) {
                            eepromDataStrings[19] = "Sensor 1 Level 3 ADC Conversion (Slow, Accurate)";
                        } else if((allEEPROMData[18][2] & 0b11000000) == 0b01000000) {
                            eepromDataStrings[19] = "Sensor 1 Level 2 ADC Conversion (Fast, Less Accurate)";
                        } else {
                            eepromDataStrings[19] = "Sensor 1 Level 1 ADC Conversion (Fastest, Least Accurate)";
                        }
                        //Sensor 1 conditioner config: (Byte B)
                        if((allEEPROMData[18][2] & 0b10000000) == 0b10000000) {
                            eepromDataStrings[20] = "Sensor 1 Chopper Enabled";
                        } else {
                            eepromDataStrings[20] = "Sensor 1 Chopper Disabled";
                        }
                    }
                    if(allEEPROMData[22].length>2) {
                        //Sensor 1 connection config
                        if((allEEPROMData[22][1]==(byte)0x00 && allEEPROMData[22][2]==(byte)0x00)) {
                            eepromDataStrings[21] = "Sensor 1 Input Multiplexer Not Set";
                        } else {
                            byte[] mux = {allEEPROMData[22][1],allEEPROMData[22][2]};
                            eepromDataStrings[21] = "Sensor 1 Input Multiplexer Set: 0x"+toHexStringLittleEndian(mux);
                        }
                    }
                    //At the end: display results:
                    Context context = getApplicationContext();
                    ArrayAdapter arrayAdapter = new ArrayAdapter(context, R.layout.activity_listview, eepromDataStrings);
                    listViewConfig.setAdapter(arrayAdapter);
                }
            }
        }
    }

    private void delayMS(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Log.e("InterruptedException",e.toString());
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
                Log.i(TAG,"SUCCESS: Read EEPROM[0x"+toHexStringLittleEndian(addr)+"] = "+toHexStringBigEndian(response));
            } else {
                Log.i(TAG,"Fail2: Read EEPROM[0x"+toHexStringLittleEndian(addr)+"] = "+toHexStringBigEndian(response));
            }
            return response;
        } catch(Exception e) {
            Log.e(TAG,"Fail: Read EEPROM[0x"+toHexStringLittleEndian(addr)+"]");
            return response;
        }
    }

    public static String toHexStringLittleEndian(byte[] bytes) {
        //Little Endian = Reverse order of how they are stored in array:
        // [n, n-1, ..., 1, 0]
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            //TODO: is Little-Endian
            hexChars[bytes.length*2-1-(j * 2 + 1)] = MainActivity.HEX_CHARS[v >>> 4];
            hexChars[bytes.length*2-1-(j * 2)] = MainActivity.HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String toHexStringBigEndian(byte[] bytes) {
        //Big Endian = As they are assigned in byte array order:
        // [0, 1, ..., n]
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = MainActivity.HEX_CHARS[v >>> 4];
            hexChars[j * 2 + 1] = MainActivity.HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * @param nfcVTag Unique NFC Tag
     * @param flag Request Flags
     * @param actionToTake Action (read/write EEPROM, Internal, etc.)
     * @param addr Memory Address to Read
     * @return response (from tranceive())
     *
     * @deprecated (use tranceiveReadEEPROM or tranceiveReadInternal)
     */
    @Deprecated
    protected byte[] tranceiveRead(NfcV nfcVTag, byte flag, byte actionToTake, byte addr) {
        byte[] response = {(byte)0xFF};
        try
        {
            byte[] WriteSingleBlockFrame = { flag, actionToTake, addr
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
}
