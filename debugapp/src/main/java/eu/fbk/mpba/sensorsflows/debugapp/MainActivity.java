package eu.fbk.mpba.sensorsflows.debugapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.Writer;

import eu.fbk.mpba.sensorsflows.AutoLinkMode;
import eu.fbk.mpba.sensorsflows.FlowsMan;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.CsvOutput;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.SQLiteOutput;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.SmartphoneDevice;
import eu.fbk.mpba.sensorsflows.debugapp.util.BluetoothService;


public class MainActivity extends Activity {

    final String TAG = "€€A";

    FlowsMan<Long, double[]> m = new FlowsMan<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onMStart(View v) {
        m.addDevice(new SmartphoneDevice(this, "Smartphone"));
        m.addOutput(new CsvOutput("CSV",
                Environment.getExternalStorageDirectory().getPath()
                        + "/eu.fbk.mpba.sensorsflows/"));
        m.addOutput(new SQLiteOutput("DB",
                Environment.getExternalStorageDirectory().getPath()
                        + "/eu.fbk.mpba.sensorsflows/"));

        m.setAutoLinkMode(AutoLinkMode.PRODUCT);

        m.start();
    }

    public void onMClose(View v) {
        m.close();
    }

    BluetoothService s;

    public void onBTSTest(View v) {
        s = new BluetoothService(new Handler(new Handler.Callback() {
            public String mConnectedDeviceName;

            // Message types sent from the BluetoothChatService Handler
            public static final int MESSAGE_STATE_CHANGE = 1;
            public static final int MESSAGE_READ = 2;
            public static final int MESSAGE_WRITE = 3;
            public static final int MESSAGE_DEVICE_NAME = 4;
            public static final int MESSAGE_TOAST = 5;
            private String[] ConnectedDevicesNames = new String[7];
            private int ConnectedDevices = 0;

            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
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
//                        SensorsData[0] = rcvData[0];
                        Log.v(TAG, " - " + rcvData[0] + " - " + rcvData[1] + " - " + rcvData[2]);

//            	textLostPck0.setText(" "+ rcvData[9]);
//            	aprLevelsSeries.setModel(Arrays.asList(SensorsData), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
//                aprLevelsPlot.redraw();
                        break;
                    case MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        mConnectedDeviceName = msg.getData().getString(BluetoothService.DEVICE_NAME);
                        Toast.makeText(getApplicationContext(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                        break;
                    case MESSAGE_TOAST:
                        Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothService.TOAST),
                                Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        }),  });
        BluetoothAdapter ba = ((BluetoothManager) getSystemService(BLUETOOTH_SERVICE)).getAdapter();
        s.connect(ba.getRemoteDevice("00:80:E1:B3:4E:A9"), false);
    }

    public void onWriteTest(View v) {
        s.sendStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
