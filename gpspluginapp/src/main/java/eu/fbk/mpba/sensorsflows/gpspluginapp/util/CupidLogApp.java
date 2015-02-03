package eu.fbk.mpba.sensorsflows.gpspluginapp.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import java.io.File;


/**
 * This is the main Activity that displays the current chat session.
 */
public class CupidLogApp extends Activity {
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Name of the connected device
    private String mConnectedDeviceName = null;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Member object for the chat services
    private BluetoothService[] mChatService = new BluetoothService[7];
    private String[] ConnectedDevicesNames = new String[7];

    private Number[] SensorsData = {-16384, -16384, -16384, -16384, -16384, -16384, -16384};

    private int ConnectedDevices = 0;
    int intTrialID = 0, intPatientId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
        else
            for (int i = 0; i < 7; i++)
                ConnectedDevicesNames[i] = "";
    }

    @Override
    public void onStart() {
        // If BT is not on, require to enable it.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService[0] == null)
                setupChat();
        }
    }

    // FIXME Useful???
    @Override
    public synchronized void onResume() {
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService[0] != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService[0].getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
//FIXME                mChatService[0].start();
            }
        }
    }

    private void setupChat() {
        mChatService[0] = new BluetoothService(mHandler0);
    }

    public void onSendStartClick(View v) {
        for (int i = 0; i < ConnectedDevices; i++)
            createLogFile(ConnectedDevicesNames[i], i);
        String message = "= =";
        sendMessage(message);
    }

    public void onSendStopClick(View v) {
        intTrialID++;
        String message = ": :";
        sendMessage(message);

    }

    public void onStartLogClick(View v) {
        String message = "% SET PATIENTID " + intTrialID + " " + intPatientId + "\r\n  - -";
        sendMessage(message);
    }

    @Override
    public void onDestroy() {
        // Stop the Bluetooth chat services
        for (int i = 0; i < 7; i++)
            if (mChatService[i] != null)
                mChatService[i].stop();
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
////        switch (item.getItemId()) {
////            case R.id.insecure_connect_scan:
//        // Launch the DeviceListActivity to see devices and do scan
//        Intent serverIntent = new Intent(this, DeviceListActivity.class);
//        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
//        return true;
////        }
////        return false;
//    }

    private void sendMessage(String message) {
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            for (int i = 0; i < 7; i++) {
                if (mChatService[i].getState() == BluetoothService.STATE_CONNECTED) {
                    mChatService[i].StartStreamingTime = android.os.SystemClock.elapsedRealtime();
                    mChatService[i].write(send);
                }
            }
//            if (ConnectedDevices == 0){
//                // Check that we're actually connected to any node
//                Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
//            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        if (resultCode != 0) {
            switch (requestCode) {
//                case REQUEST_CONNECT_DEVICE_INSECURE:
//                    // When DeviceListActivity returns with a device to connect
//                    int currState;
//                    String address = data.getExtras()
//                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
//                    // Get the BLuetoothDevice object
//                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//                    for (int i = 0; i < 7; i++) {
//                        currState = mChatService[i].getState();
//                        if (currState < 2) {
//                            mChatService[i].connect(device, false);
//                            return;
//                        }
//                    }

                case REQUEST_ENABLE_BT:
                    // When the request to enable Bluetooth returns
                    if (resultCode == Activity.RESULT_OK) {
                        // Bluetooth is now enabled, so set up a chat session
                        setupChat();
                    } else {
                        // User did not enable Bluetooth or an error occured
//                        Log.d(TAG, "BT not enabled");
//                        Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                        finish();
                    }
            }
        }
    }

    public void createLogFile(String node_id, int service_id) {

        if (mChatService[service_id].getState() == BluetoothService.STATE_CONNECTED) {

            try {
                File newFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CUPID_data/" + CsvDataSaver.getHumanDateTimeString() + "/log_" + CsvDataSaver.getHumanDateTimeString() + "-" + node_id + "-" + service_id + ".txt");
                //noinspection ResultOfMethodCallIgnored
                newFile.mkdirs();

//                try {
//                    FileWriter log_file_wr = new FileWriter(newFile);
//                    out_log = new BufferedWriter(log_file_wr);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                mChatService[service_id].out_log = out_log;

            } catch (Exception e) {
                Toast.makeText(this, "Exception creating folders " + e, Toast.LENGTH_LONG).show();
            }
        }
    }

    private final Handler mHandler0 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
//                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            ConnectedDevices++;
                            //                    mTitle.setText(R.string.title_connected_to);
                            ConnectedDevicesNames[0] = mConnectedDeviceName;
                            //                    for (int i=0; i<7; i++){
                            //                    	mTitle.append(ConnectedDevicesNames[i] + " ");
                            //                    }
                            //                    mConversationArrayAdapter.clear();
                            break;
                        case BluetoothService.STATE_DISCONNECTED:
                            ConnectedDevices--;
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            //                    mTitle.setText(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            //                    mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
//            	final TextView textCounter0 = (TextView) findViewById(R.id.textCount0);
//            	final TextView textLostPck0 = (TextView) findViewById(R.id.textLostPck0);
                    int[] rcvData = (int[]) msg.obj;

//            	textCounter0.setText(" "+ rcvData[0]);
                    SensorsData[0] = rcvData[0];
//            	textLostPck0.setText(" "+ rcvData[9]);
//            	aprLevelsSeries.setModel(Arrays.asList(SensorsData), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
//                aprLevelsPlot.redraw();
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

//    private final Handler mHandler1 = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//            case MESSAGE_STATE_CHANGE:
//                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
//                switch (msg.arg1) {
//                case BluetoothService.STATE_CONNECTED:
//                	ConnectedDevices++;
//                    mTitle.setText(R.string.title_connected_to);
//                    ConnectedDevicesNames[1] = mConnectedDeviceName;
//                    for (int i=0; i<7; i++){
//                    	mTitle.append(ConnectedDevicesNames[i] + " ");
//                    }
//                    mConversationArrayAdapter.clear();
//                    break;
//                case BluetoothService.STATE_DISCONNECTED:
//                	//ConnectedDevices--;
//                	break;
//                case BluetoothService.STATE_CONNECTING:
//                    mTitle.setText(R.string.title_connecting);
//                    break;
//                case BluetoothService.STATE_LISTEN:
//                case BluetoothService.STATE_NONE:
//                    mTitle.setText(R.string.title_not_connected);
//                    break;
//                }
//                break;
//            case MESSAGE_WRITE:
//                break;
//            case MESSAGE_READ:
//            	final TextView textCounter1 = (TextView) findViewById(R.id.textCount1);
//            	final TextView textLostPck1 = (TextView) findViewById(R.id.textLostPck1);
//            	int [] rcvData;
//            	rcvData = (int[]) msg.obj;
//
//            	textCounter1.setText(" "+ rcvData[0]);
//            	SensorsData[1]= rcvData[0];
//            	textLostPck1.setText(" "+ rcvData[9]);
//                break;
//            case MESSAGE_DEVICE_NAME:
//                // save the connected device's name
//                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                Toast.makeText(getApplicationContext(), "Connected to "
//                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                break;
//            case MESSAGE_TOAST:
//                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
//                               Toast.LENGTH_SHORT).show();
//                break;
//            }
//        }
//    };
//
//    private final Handler mHandler2 = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//            case MESSAGE_STATE_CHANGE:
//                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
//                switch (msg.arg1) {
//                case BluetoothService.STATE_CONNECTED:
//                	ConnectedDevices++;
//                    mTitle.setText(R.string.title_connected_to);
//                    ConnectedDevicesNames[2] = mConnectedDeviceName;
//                    for (int i=0; i<7; i++){
//                    	mTitle.append(ConnectedDevicesNames[i] + " ");
//                    }
//                    mConversationArrayAdapter.clear();
//                    break;
//                case BluetoothService.STATE_DISCONNECTED:
//                	//ConnectedDevices--;
//                	break;
//                case BluetoothService.STATE_CONNECTING:
//                    mTitle.setText(R.string.title_connecting);
//                    break;
//                case BluetoothService.STATE_LISTEN:
//                case BluetoothService.STATE_NONE:
//                    mTitle.setText(R.string.title_not_connected);
//                    break;
//                }
//                break;
//            case MESSAGE_WRITE:
//                break;
//            case MESSAGE_READ:
//            	final TextView textCounter2 = (TextView) findViewById(R.id.textCount2);
//            	final TextView textLostPck2 = (TextView) findViewById(R.id.textLostPck2);
//            	int [] rcvData;
//            	rcvData = (int[]) msg.obj;
//
//            	textCounter2.setText(" "+ rcvData[0]);
//            	SensorsData[2]= rcvData[0];
//            	textLostPck2.setText(" "+ rcvData[9]);
//                break;
//            case MESSAGE_DEVICE_NAME:
//                // save the connected device's name
//                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                Toast.makeText(getApplicationContext(), "Connected to "
//                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                break;
//            case MESSAGE_TOAST:
//                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
//                               Toast.LENGTH_SHORT).show();
//                break;
//            }
//        }
//    };
//
//    private final Handler mHandler3 = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//            case MESSAGE_STATE_CHANGE:
//                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
//                switch (msg.arg1) {
//                case BluetoothService.STATE_CONNECTED:
//                	ConnectedDevices++;
//                    mTitle.setText(R.string.title_connected_to);
//                    ConnectedDevicesNames[3] = mConnectedDeviceName;
//                    for (int i=0; i<7; i++){
//                    	mTitle.append(ConnectedDevicesNames[i] + " ");
//                    }
//                    mConversationArrayAdapter.clear();
//                    break;
//                case BluetoothService.STATE_DISCONNECTED:
//                	//ConnectedDevices--;
//                	break;
//                case BluetoothService.STATE_CONNECTING:
//                    mTitle.setText(R.string.title_connecting);
//                    break;
//                case BluetoothService.STATE_LISTEN:
//                case BluetoothService.STATE_NONE:
//                    mTitle.setText(R.string.title_not_connected);
//                    break;
//                }
//                break;
//            case MESSAGE_WRITE:
//                break;
//            case MESSAGE_READ:
//            	final TextView textCounter3 = (TextView) findViewById(R.id.textCount3);
//            	final TextView textLostPck3 = (TextView) findViewById(R.id.textLostPck3);
//            	int [] rcvData;
//            	rcvData = (int[]) msg.obj;
//
//            	textCounter3.setText(" "+ rcvData[0]);
//            	SensorsData[3]= rcvData[0];
//            	textLostPck3.setText(" "+ rcvData[9]);
//                break;
//            case MESSAGE_DEVICE_NAME:
//                // save the connected device's name
//                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                Toast.makeText(getApplicationContext(), "Connected to "
//                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                break;
//            case MESSAGE_TOAST:
//                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
//                               Toast.LENGTH_SHORT).show();
//                break;
//            }
//        }
//    };
//
//    private final Handler mHandler4 = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//            case MESSAGE_STATE_CHANGE:
//                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
//                switch (msg.arg1) {
//                case BluetoothService.STATE_CONNECTED:
//                	ConnectedDevices++;
//                    mTitle.setText(R.string.title_connected_to);
//                    ConnectedDevicesNames[4] = mConnectedDeviceName;
//                    for (int i=0; i<7; i++){
//                    	mTitle.append(ConnectedDevicesNames[i] + " ");
//                    }
//                    mConversationArrayAdapter.clear();
//                    break;
//                case BluetoothService.STATE_DISCONNECTED:
//                	//ConnectedDevices--;
//                	break;
//                case BluetoothService.STATE_CONNECTING:
//                    mTitle.setText(R.string.title_connecting);
//                    break;
//                case BluetoothService.STATE_LISTEN:
//                case BluetoothService.STATE_NONE:
//                    mTitle.setText(R.string.title_not_connected);
//                    break;
//                }
//                break;
//            case MESSAGE_WRITE:
//                break;
//            case MESSAGE_READ:
//            	final TextView textCounter4 = (TextView) findViewById(R.id.textCount4);
//            	final TextView textLostPck4 = (TextView) findViewById(R.id.textLostPck4);
//            	int [] rcvData;
//            	rcvData = (int[]) msg.obj;
//
//            	textCounter4.setText(" "+ rcvData[0]);
//            	SensorsData[4]= rcvData[0];
//            	textLostPck4.setText(" "+ rcvData[9]);
//                break;
//            case MESSAGE_DEVICE_NAME:
//                // save the connected device's name
//                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                Toast.makeText(getApplicationContext(), "Connected to "
//                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                break;
//            case MESSAGE_TOAST:
//                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
//                               Toast.LENGTH_SHORT).show();
//                break;
//            }
//        }
//    };
//
//    private final Handler mHandler5 = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//            case MESSAGE_STATE_CHANGE:
//                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
//                switch (msg.arg1) {
//                case BluetoothService.STATE_CONNECTED:
//                	ConnectedDevices++;
//                    mTitle.setText(R.string.title_connected_to);
//                    ConnectedDevicesNames[5] = mConnectedDeviceName;
//                    for (int i=0; i<7; i++){
//                    	mTitle.append(ConnectedDevicesNames[i] + " ");
//                    }
//                    mConversationArrayAdapter.clear();
//                    break;
//                case BluetoothService.STATE_DISCONNECTED:
//                	//ConnectedDevices--;
//                	break;
//                case BluetoothService.STATE_CONNECTING:
//                    mTitle.setText(R.string.title_connecting);
//                    break;
//                case BluetoothService.STATE_LISTEN:
//                case BluetoothService.STATE_NONE:
//                    mTitle.setText(R.string.title_not_connected);
//                    break;
//                }
//                break;
//            case MESSAGE_WRITE:
//                break;
//            case MESSAGE_READ:
//            	final TextView textCounter5 = (TextView) findViewById(R.id.textCount5);
//            	final TextView textLostPck5 = (TextView) findViewById(R.id.textLostPck5);
//            	int [] rcvData;
//            	rcvData = (int[]) msg.obj;
//
//            	textCounter5.setText(" "+ rcvData[0]);
//            	SensorsData[5]= rcvData[0];
//            	textLostPck5.setText(" "+ rcvData[9]);
//                break;
//            case MESSAGE_DEVICE_NAME:
//                // save the connected device's name
//                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                Toast.makeText(getApplicationContext(), "Connected to "
//                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                break;
//            case MESSAGE_TOAST:
//                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
//                               Toast.LENGTH_SHORT).show();
//                break;
//            }
//        }
//    };
//
//    private final Handler mHandler6 = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//            case MESSAGE_STATE_CHANGE:
//                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
//                switch (msg.arg1) {
//                case BluetoothService.STATE_CONNECTED:
//                	ConnectedDevices++;
//                    mTitle.setText(R.string.title_connected_to);
//                    ConnectedDevicesNames[6] = mConnectedDeviceName;
//                    for (int i=0; i<7; i++){
//                    	mTitle.append(ConnectedDevicesNames[i] + " ");
//                    }
//                    mConversationArrayAdapter.clear();
//                    break;
//                case BluetoothService.STATE_DISCONNECTED:
//                	//ConnectedDevices--;
//                	break;
//                case BluetoothService.STATE_CONNECTING:
//                    mTitle.setText(R.string.title_connecting);
//                    break;
//                case BluetoothService.STATE_LISTEN:
//                case BluetoothService.STATE_NONE:
//                    mTitle.setText(R.string.title_not_connected);
//                    break;
//                }
//                break;
//            case MESSAGE_WRITE:
//                break;
//            case MESSAGE_READ:
//            	final TextView textCounter6 = (TextView) findViewById(R.id.textCount6);
//            	final TextView textLostPck6 = (TextView) findViewById(R.id.textLostPck6);
//            	int [] rcvData;
//            	rcvData = (int[]) msg.obj;
//
//            	textCounter6.setText(" "+ rcvData[0]);
//            	SensorsData[6]= rcvData[0];
//            	textLostPck6.setText(" "+ rcvData[9]);
//                break;
//            case MESSAGE_DEVICE_NAME:
//                // save the connected device's name
//                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                Toast.makeText(getApplicationContext(), "Connected to "
//                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                break;
//            case MESSAGE_TOAST:
//                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
//                               Toast.LENGTH_SHORT).show();
//                break;
//            }
//        }
//    };
}
