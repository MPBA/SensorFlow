package eu.fbk.mpba.sensorsflows.debugapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.Hashtable;
import java.util.UUID;

import eu.fbk.mpba.sensorsflows.AutoLinkMode;
import eu.fbk.mpba.sensorsflows.FlowsMan;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.AccelerometerSensor;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.EXLs3Device;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.GpsSensor;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.SmartphoneDevice;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.TextEventsSensor;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.outputs.ProtobufferOutput;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.outputs.SQLiteOutput;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.outputs.UserOutput;
import eu.fbk.mpba.sensorsflows.debugapp.util.CsvDataSaver;
import eu.fbk.mpba.sensorsflows.debugapp.util.EXLs3Dumper;
import eu.fbk.mpba.sensorsflows.debugapp.util.SkiloProtobuffer;


public class MainActivity extends Activity {

    FlowsMan<Long, double[]> m = new FlowsMan<>();
    SmartphoneDevice smartphoneDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void onMStart(View v) {
        Hashtable<Class, SkiloProtobuffer.SensorInfo.TYPESENSOR> types = new Hashtable<>();
        types.put(GpsSensor.class, SkiloProtobuffer.SensorInfo.TYPESENSOR.GPS);
        types.put(AccelerometerSensor.class, SkiloProtobuffer.SensorInfo.TYPESENSOR.ACC);
        types.put(TextEventsSensor.class, SkiloProtobuffer.SensorInfo.TYPESENSOR.MARKER);
        types.put(EXLs3Device.EXLAccelerometer.class, SkiloProtobuffer.SensorInfo.TYPESENSOR.ACC);
        types.put(EXLs3Device.EXLGyroscope.class, SkiloProtobuffer.SensorInfo.TYPESENSOR.GYRO);
        types.put(EXLs3Device.EXLMagnetometer.class, SkiloProtobuffer.SensorInfo.TYPESENSOR.MAGNE);
        types.put(EXLs3Device.EXLQuaternion.class, SkiloProtobuffer.SensorInfo.TYPESENSOR.QUAT);
        types.put(EXLs3Device.EXLBattery.class, SkiloProtobuffer.SensorInfo.TYPESENSOR.BATTERY);

        m.addDevice(smartphoneDevice = new SmartphoneDevice(this, "Smartphone"));

        m.addDevice(new EXLs3Device(BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:80:e1:b3:4e:e0".toUpperCase()), BluetoothAdapter.getDefaultAdapter(), "EXL_175"));

        m.addDevice(new EXLs3Device(BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:80:e1:b3:4e:af".toUpperCase()), BluetoothAdapter.getDefaultAdapter(), "EXL_176"));

//        m.addOutput(new CsvOutput("CSV",
//                Environment.getExternalStorageDirectory().getPath()
//                        + "/eu.fbk.mpba.sensorsflows/"));

        m.addOutput(new SQLiteOutput("DB",
                Environment.getExternalStorageDirectory().getPath()
                        + "/eu.fbk.mpba.sensorsflows/"));

        m.addOutput(new UserOutput());

        m.addOutput(new ProtobufferOutput("Protobuf", new File(
                Environment.getExternalStorageDirectory().getPath()
                        + "/eu.fbk.mpba.sensorsflows/"), 1000, UUID.randomUUID().toString(), types));

        m.setAutoLinkMode(AutoLinkMode.PRODUCT);

        m.start(CsvDataSaver.getHumanDateTimeString());

    }

    public void onMClose(View v) {
        m.close();
        m = new FlowsMan<>();
    }

    EXLs3Dumper d = new EXLs3Dumper(null, null, BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:80:e1:b0:b9:11".toUpperCase()), BluetoothAdapter.getDefaultAdapter()); // 0128

    public void onBTDStart(View v) {
        d.connect();
        d.startStream();
    }

    public void onBTDClose(View v) {
        d.stopStream();
        d.close();
    }

    public void onAddText(View v) {
        smartphoneDevice.addNoteNow(((Button)v).getText().toString());
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
