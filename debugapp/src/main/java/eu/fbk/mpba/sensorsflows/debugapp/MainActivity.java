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

import java.io.File;
import java.util.UUID;

import eu.fbk.mpba.sensorsflows.AutoLinkMode;
import eu.fbk.mpba.sensorsflows.FlowsMan;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.CsvOutput;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.ProtobufferOutput;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.SQLiteOutput;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.SmartphoneDevice;
import eu.fbk.mpba.sensorsflows.debugapp.util.EXLs3Manager;


public class MainActivity extends Activity {

    final String TAG = "€€A";

    FlowsMan<Long, double[]> m = new FlowsMan<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        s = new EXLs3Manager(btsStatus, btsData);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void onMStart(View v) {
        m.addDevice(new SmartphoneDevice(this, "Smartphone"));

        m.addOutput(new CsvOutput("CSV",
                Environment.getExternalStorageDirectory().getPath()
                        + "/eu.fbk.mpba.sensorsflows/"));

        m.addOutput(new SQLiteOutput("DB",
                Environment.getExternalStorageDirectory().getPath()
                        + "/eu.fbk.mpba.sensorsflows/"));

        m.addOutput(new ProtobufferOutput("Protobuf", new File(
                Environment.getExternalStorageDirectory().getPath()
                        + "/eu.fbk.mpba.sensorsflows/"), 100, UUID.randomUUID().toString()));

        m.setAutoLinkMode(AutoLinkMode.PRODUCT);

        m.start();
    }

    public void onMClose(View v) {
        m.close();
    }

    EXLs3Manager s;

    public void onBTSTest(View v) {
        s.connect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:80:E1:B0:B9:11"), false);
    }

    public void onWriteTest(View v) {
        s.sendStart();
    }

    public void onStopTest(View v) {
        s.sendStop();
    }

    public void onBTCloseTest(View v) {
        s.stop();
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

    private final EXLs3Manager.StatusDelegate btsStatus = new EXLs3Manager.StatusDelegate() {

        public void idle(EXLs3Manager sender) {
            Log.i(TAG, "+++++++++++ IDLE");
        }

        public void listening(EXLs3Manager sender) {
            Log.i(TAG, "+++++++++++ LISTENING");
        }

        public void connecting(EXLs3Manager sender, BluetoothDevice device, boolean secureMode) {
            Log.i(TAG, "+++++++++++ CONNECTING to " + device.getName() + "@" + device.getAddress() + (secureMode ? " secure" : " insecure"));
        }

        public void connected(EXLs3Manager sender, String deviceName) {
            Log.i(TAG, "+++++++++++ CONNECTED to " + deviceName);
        }

        public void connectionFailed(EXLs3Manager sender) {
            Log.i(TAG, "+++++++++++ Conn FAILED");
        }

        public void connectionLost(EXLs3Manager sender) {
            Log.i(TAG, "+++++++++++ Conn LOST");
        }
    };

    private final EXLs3Manager.DataDelegate btsData = new EXLs3Manager.DataDelegate() {
        @Override
        public void receive(EXLs3Manager sender, EXLs3Manager.Packet p) {
            Log.v(TAG, String.format("+ DATA %s %s %s %s", p.counter, sender.packetCounterTotal, sender.packetsReceived, sender.lostBytes));
        }
    };
}
