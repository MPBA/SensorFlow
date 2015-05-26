package eu.fbk.mpba.sensorsflows.testapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import eu.fbk.mpba.sensorsflows.AutoLinkMode;
import eu.fbk.mpba.sensorsflows.FlowsMan;
import eu.fbk.mpba.sensorsflows.OutputPlugin;
import eu.fbk.mpba.sensorsflows.base.EngineStatus;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.inputs.EXLs3.EXLs3Device;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.inputs.EXLs3.EXLs3ToFile;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.inputs.android.SmartphoneDevice;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.outputs.CsvDataSaver;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.outputs.CsvOutput;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.outputs.ProtobufferOutput;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.outputs.SQLiteOutput;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.outputs.SensorsProtobuffer;


public class MainActivity extends Activity {

    FlowsMan<Long, double[]> m = new FlowsMan<>();
    SmartphoneDevice smartphoneDevice;
    LinearLayout selection;

    void addPluginChoice(boolean input, String name, Runnable initialization) {
        LinearLayout sel = (LinearLayout) findViewById(R.id.pluginSelection);
        CheckBox x = new CheckBox(this);
        x.setChecked(false);
        x.setTag(initialization);
        x.setText((input ? "(in)" : "(out)") + name);
        sel.addView(x);
    }

    private void runSelectedInitializations() {
        for (int i = 0; i < selection.getChildCount(); i++) {
            CheckBox x =  (CheckBox)selection.getChildAt(i);
            if (x.isChecked())
                ((Runnable) x.getTag()).run();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selection = (LinearLayout) findViewById(R.id.pluginSelection);
        final Context _this = this;
        addPluginChoice(true, "EXLs3", new Runnable() {
            @Override
            public void run() {
                m.addDevice(new EXLs3Device(BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:80:e1:b3:4e:B3".toUpperCase()), BluetoothAdapter.getDefaultAdapter(), "EXL_174", 0, 300));
            }
        });
        addPluginChoice(true, "Smartphone", new Runnable() {
            @Override
            public void run() {
                m.addDevice(smartphoneDevice = new SmartphoneDevice(_this, "Smartphone"));
            }
        });
        addPluginChoice(true, "Empatica", new Runnable() {
            @Override
            public void run() {

            }
        });
        addPluginChoice(false, "CSV", new Runnable() {
            @Override
            public void run() {
                m.addOutput(new CsvOutput("CSV",
                        Environment.getExternalStorageDirectory().getPath()
                                + "/eu.fbk.mpba.sensorsflows/"));
            }
        });
        addPluginChoice(false, "SQLite", new Runnable() {
            @Override
            public void run() {
                m.addOutput(new SQLiteOutput("DB",
                        Environment.getExternalStorageDirectory().getPath()
                                + "/eu.fbk.mpba.sensorsflows/"));
            }
        });
        addPluginChoice(false, "Protobuffer", new Runnable() {
            @Override
            public void run() {
                Hashtable<Class, SensorsProtobuffer.SensorInfo.TYPESENSOR> types = new Hashtable<>();
                //        types.put(GpsSensor.class, SkiloProtobuffer.SensorInfo.TYPESENSOR.GPS);
                //        types.put(AccelerometerSensor.class, SkiloProtobuffer.SensorInfo.TYPESENSOR.ACC);
                //        types.put(TextEventsSensor.class, SkiloProtobuffer.SensorInfo.TYPESENSOR.MARKER);
                types.put(EXLs3Device.EXLAccelerometer.class, SensorsProtobuffer.SensorInfo.TYPESENSOR.ACC);
                types.put(EXLs3Device.EXLGyroscope.class, SensorsProtobuffer.SensorInfo.TYPESENSOR.GYRO);
                types.put(EXLs3Device.EXLMagnetometer.class, SensorsProtobuffer.SensorInfo.TYPESENSOR.MAGNE);
                types.put(EXLs3Device.EXLQuaternion.class, SensorsProtobuffer.SensorInfo.TYPESENSOR.QUAT);
                types.put(EXLs3Device.EXLBattery.class, SensorsProtobuffer.SensorInfo.TYPESENSOR.BATTERY);

                m.addOutput(new ProtobufferOutput("Protobuf", new File(
                        Environment.getExternalStorageDirectory().getPath()
                                + "/eu.fbk.mpba.sensorsflows/"), 1000, UUID.randomUUID().toString(), types));
            }
        });
        addPluginChoice(false, "User", new Runnable() {
            @Override
            public void run() {
                m.addOutput(new OutputPlugin<Long, double[]>() {
                    @Override
                    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {
                        // Executed on the last part of the start
                        // sessionTag       : the name of the session (.toString())
                        // streamingSensors : List with every sensor of the session
                    }

                    @Override
                    public void outputPluginFinalize() {
                        // When close is called
                    }

                    @Override
                    public void newSensorEvent(SensorEventEntry<Long> event) {
                        // Events like I said
                        // event
                        //     .sensor      : sender sensor, useful with the instanceof to filter the events
                        //     .timestamp   : event's
                        //     .code        : numeric or flags, see the sensor's code
                        //     .message     : descriptive string
                    }

                    @Override
                    public void newSensorData(SensorDataEntry<Long, double[]> data) {
                        // Data linke I said
                        // data
                        //     .sensor      : sender sensor, useful with the instanceof to filter the events
                        //     .timestamp   : data's
                        //     .value       : value of double[] type, see the sensor's code
                    }
                });
            }
        });
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void onMStart(View v) {
        if (m.getStatus() == EngineStatus.STANDBY) {
            runSelectedInitializations();
            m.setAutoLinkMode(AutoLinkMode.PRODUCT);
            m.start(CsvDataSaver.getHumanDateTimeString());
        }
        else
            Toast.makeText(this, "m.close() before", Toast.LENGTH_SHORT).show();
    }

    public void onMClose(View v) {
        m.close();
        m = new FlowsMan<>();
    }

    EXLs3ToFile d;

    public void onBTDStart(View v) {
        if (d == null) {
            d = new EXLs3ToFile(null, BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:80:E1:B3:4E:E0".toUpperCase()), BluetoothAdapter.getDefaultAdapter());
        }
        else {
            d.start();
        }
    }

    public void onBTDClose(View v) {
        if (d != null)
            d.stop();
    }

    public void onAddText(View v) {
        smartphoneDevice.addNoteNow(((TextView) findViewById(R.id.editText)).getText().toString());
        ((TextView) findViewById(R.id.editText)).setText("");
    }
}
