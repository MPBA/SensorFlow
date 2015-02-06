package eu.fbk.mpba.sensorsflows.debugapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
        s = new BluetoothService(btsStatus, btsData);
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
        s.connect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:80:E1:B3:4E:A9"), false);
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

    private final BluetoothService.StatusDelegate btsStatus = new BluetoothService.StatusDelegate() {

        public void idle(BluetoothService sender) {
            Log.i(TAG, "+++++++++++ IDLE");
        }

        public void listening(BluetoothService sender) {
            Log.i(TAG, "+++++++++++ LISTENING");
        }

        public void connecting(BluetoothService sender, BluetoothDevice device, boolean secureMode) {
            Log.i(TAG, "+++++++++++ CONNECTING to " + device.getName() + "@" + device.getAddress() + (secureMode ? " secure" : " insecure"));
        }

        public void connected(BluetoothService sender, String deviceName) {
            Log.i(TAG, "+++++++++++ CONNECTED to " + deviceName);
        }

        public void connectionFailed(BluetoothService sender) {
            Log.i(TAG, "+++++++++++ Conn FAILED");
        }

        public void connectionLost(BluetoothService sender) {
            Log.i(TAG, "+++++++++++ Conn LOST");
        }
    };

    private final BluetoothService.DataDelegate btsData = new BluetoothService.DataDelegate() {
        @Override
        public void receive(BluetoothService sender, BluetoothService.Packet p) {
            Log.v(TAG, String.format("+ DATA %s %s %s %s", p.counter, sender.packetCounterTotal, sender.packetsReceived, sender.lostPackets));
        }
    };
}
