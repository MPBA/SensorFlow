package eu.fbk.mpba.sensorsflows.testapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
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
import eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.EXLs3.EXLs3Device;
import eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.EXLs3.EXLs3ToFile;
import eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.android.SmartphoneDevice;
import eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.empatica.EmpaticaDevice;
import eu.fbk.mpba.sensorsflows.plugins.plugins.outputs.CsvDataSaver;
import eu.fbk.mpba.sensorsflows.plugins.plugins.outputs.CsvOutput;
import eu.fbk.mpba.sensorsflows.plugins.plugins.outputs.ProtobufferOutput;
import eu.fbk.mpba.sensorsflows.plugins.plugins.outputs.SQLiteOutput;
import eu.fbk.mpba.sensorsflows.plugins.plugins.outputs.SensorsProtobuffer;
import eu.fbk.mpba.sensorsflows.plugins.plugins.outputs.TCPClientOutput;
import eu.fbk.mpba.sensorsflows.plugins.plugins.outputs.TCPServerOutput;
import eu.fbk.mpba.sensorsflows.testapp.CSVLoader.CSVLoader;


public class MainActivity extends Activity {

    FlowsMan<Long, double[]> m = new FlowsMan<>();
    SmartphoneDevice smartphoneDevice;
    LinearLayout selection;

    CheckBox addPluginChoice(boolean input, String name, Runnable initialization) {
        LinearLayout sel = (LinearLayout) findViewById(R.id.pluginSelection);
        CheckBox x = new CheckBox(this);
        x.setChecked(false);
        x.setTag(initialization);
        x.setText((input ? "(in)" : "(out)") + name);
        sel.addView(x);
        return x;
    }

    private void runSelectedInitializations() {
        for (int i = 0; i < selection.getChildCount(); i++) {
            CheckBox x =  (CheckBox)selection.getChildAt(i);
            if (x.isChecked())
                runOnUiThread((Runnable) x.getTag());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        CSVLoader.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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
                m.addDevice(new EmpaticaDevice("e250d5fdb4644d7bbd8cbbcd4acfb860", _this, null, new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), -1);
                            }
                        });
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ignored) { }
                    }
                }));
            }
        });

        /** CSVLoader */
        //TODO guardar cio che causa eccezione!!!
        /*CSVLoader.setCheckboxListener(addPluginChoice(true, "CSVLoader", CSVLoader.getRunnable(m,this)));
        CSVLoader.TemporaneallyDrawGraphics((LinearLayout) findViewById(R.id.pluginSelection), this);*/

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
                types.put(EXLs3Device.EXLAccelerometer.class, SensorsProtobuffer.SensorInfo.TYPESENSOR.ACC);
                types.put(EXLs3Device.EXLGyroscope.class, SensorsProtobuffer.SensorInfo.TYPESENSOR.GYRO);
                types.put(EXLs3Device.EXLMagnetometer.class, SensorsProtobuffer.SensorInfo.TYPESENSOR.MAGNE);
                types.put(EXLs3Device.EXLQuaternion.class, SensorsProtobuffer.SensorInfo.TYPESENSOR.QUAT);
                types.put(EXLs3Device.EXLBattery.class, SensorsProtobuffer.SensorInfo.TYPESENSOR.BATTERY);

                m.addOutput(new ProtobufferOutput("Protobuf", new File(
                        Environment.getExternalStorageDirectory().getPath()
                                + "/eu.fbk.mpba.sensorsflows/"), 1000, UUID.randomUUID().toString(), 0, types));
            }
        });
        addPluginChoice(false, "TCPServer", new Runnable() {
            @Override
            public void run() {
                try {
                    TCPServerOutput x = new TCPServerOutput(2000);
                    m.addOutput(x);
                } catch (IOException e) {
                    Toast.makeText(_this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        addPluginChoice(false, "TCPClient", new Runnable() {
            @Override
            public void run() {
                try {
                    String[] t = ((EditText)findViewById(R.id.tcpServerPort)).getText().toString().split(":");
                    String username = ((EditText)findViewById(R.id.editTextUsername)).getText().toString();
                    String password = ((EditText)findViewById(R.id.editTextPassword)).getText().toString();
                    TCPClientOutput x = new TCPClientOutput(InetAddress.getByName(t[0]), Integer.parseInt(t[1]), username, password);
                    m.addOutput(x);
                } catch (IOException e) {
                    Toast.makeText(_this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
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
                        Log.v("UD PLUGIN", String.format("INIT %s - %d sensors", sessionTag, streamingSensors.size()));
                    }

                    @Override
                    public void outputPluginFinalize() {
                        // When close is called
                        Log.v("UD PLUGIN", "FINAL");
                    }

                    @Override
                    public void newSensorEvent(SensorEventEntry<Long> event) {
                        // Events like I said
                        // event
                        //     .sensor      : sender sensor, useful with the instanceof to filter the events
                        //     .timestamp   : event's
                        //     .code        : numeric or flags, see the sensor's code
                        //     .message     : descriptive string
                        Log.v("UD PLUGIN", String.format("%d %s: %d: %s", event.timestamp, event.sensor, event.code, event.message));
                    }

                    @Override
                    public void newSensorData(SensorDataEntry<Long, double[]> data) {
                        // Data linke I said
                        // data
                        //     .sensor      : sender sensor, useful with the instanceof to filter the events
                        //     .timestamp   : data's
                        //     .value       : value of double[] type, see the sensor's code
                    }

                    @Override
                    public String getName() {
                        return "UserOutput-MainActivity";
                    }

                    @Override
                    public int getReceivedMessagesCount() {
                        return 0;
                    }

                    @Override
                    public int getForwardedMessagesCount() {
                        return 0;
                    }
                });
            }
        });
    }

    public void onCreatePlugins(View view) {
        if (m.getStatus() == EngineStatus.STANDBY) {
            findViewById(R.id.btn_create).setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runSelectedInitializations();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.btn_start).setEnabled(true);
                        }
                    });
                }
            }, "PluginsInitialization").start();
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void onMStart(View v) {
        if (m.getStatus() == EngineStatus.STANDBY) {
            findViewById(R.id.btn_start).setEnabled(false);
            m.setAutoLinkMode(AutoLinkMode.PRODUCT);
            m.start(CsvDataSaver.getHumanDateTimeString());
            findViewById(R.id.btn_stop).setEnabled(true);
        }
    }

    public void onMClose(View v) {
        findViewById(R.id.btn_stop).setEnabled(false);
        m.close();
        m = new FlowsMan<>();
        findViewById(R.id.btn_create).setEnabled(true);
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
