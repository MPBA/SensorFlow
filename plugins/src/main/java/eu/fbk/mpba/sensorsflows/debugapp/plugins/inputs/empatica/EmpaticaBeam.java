package eu.fbk.mpba.sensorsflows.debugapp.plugins.inputs.empatica;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import java.io.File;
import java.util.ArrayList;

public class EmpaticaBeam implements EmpaStatusDelegate, EmpaDataDelegate {
    protected final EmpaDeviceManager _device;
    protected final String _address;
    private final String[] ES_NAMES = {
            "Accelerometer",
            "Battery",
            "BVP",
            "GSR",
            "IBI",
            "Temperature"
    };
    private final String[][] ES_HEADERS = {
            {"timestamp", "accX", "accY", "accZ"},
            {"timestamp", "battery"},
            {"timestamp", "BVP"},
            {"timestamp", "GSR"},
            {"timestamp", "IBI"},
            {"timestamp", "temp"}
    };
    private final String LOG_TAG = "ALE EMPA BEAM";
    protected String _code;
    protected Runnable _enableBluetooth;
    protected ConnectEventHandler _onConnectionChanged;
    protected DataCallback _onData;
    protected int __e3_streamed_messages = 0;
    private boolean _recording;
    private boolean _connected;
    private ArrayList<String> _foundE3s;

    public String getAddress() {
        return _address;
    }

    public String getCode() {
        return _code;
    }

    public boolean isRecording() {
        return _recording;
    }

    public EmpaticaBeam(String key, Context context, String address, String code, DataCallback onData, ConnectEventHandler onConnectionChanged, Runnable enableBluetooth) {
        clearLogs(context);
        _foundE3s = new ArrayList<String>();
        _address = address;
        _code = code;
        _onData = onData;
        _enableBluetooth = enableBluetooth;
        _onConnectionChanged = onConnectionChanged;
        _device = new EmpaDeviceManager(context, this, this);
        _device.authenticateWithAPIKey(key);
    }

    public boolean startRecording() {
        if (_connected) {
            if (!_recording) {
                _recording = true;
                return true;
            } else {
                Log.e(LOG_TAG, "Alredy recording");
                return false;
            }
        } else
            return false;
    }

    public void stopRecording() {
        if (_recording)
            _recording = false;
        else
            Log.e(LOG_TAG, "Alredy NOT recording");
    }

    public void disconnect() {
        if (_connected) {
            _connected = false;
            _device.disconnect();
            _device.cleanUp();
        } else
            Log.e(LOG_TAG, "Alredy NOT connected");
    }

    private void received(int type, double[] v) {
        __e3_streamed_messages++;
        if (isRecording())
            _onData.handle(type, v);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EmpaticaBeam &&
                this.getAddress().equals(((EmpaticaBeam) o).getAddress());
    }

    @Override
    public int hashCode() {
        return _address.hashCode();
    }

    // EmpaStatusDelegate overrides

    @Override
    public void didDiscoverDevice(BluetoothDevice device, String label, int rssi, boolean allowed) {
        if (_address == null || device.getAddress().equals(_address)) {
            if (allowed) {
                _device.stopScanning();
                try {
                    _device.connectDevice(device);
                    // Allok
                    _code = device.getName();
                    Log.d(LOG_TAG, "Device name: " + _code + "secondo empalink: " + label);
                } catch (ConnectionNotAllowedException e) {
                    // Bisogna aver pazienza con loro...
                    allowed = false;
                }
            }
            if (!allowed) {
                _onConnectionChanged.end(this, ConnectEventHandler.Result.NOT_ALLOWED);
                _onConnectionChanged.end(this, ConnectEventHandler.Result.NOT_CONNECTED);
            }
        } else {
            if (_foundE3s.contains(device.getAddress())) {
                _onConnectionChanged.end(this, ConnectEventHandler.Result.NOT_FOUND);
                _onConnectionChanged.end(this, ConnectEventHandler.Result.NOT_CONNECTED);
            } else
                _foundE3s.add(device.getAddress());
        }
    }

    @Override
    public void didRequestEnableBluetooth() {
        _enableBluetooth.run();
    }

    @Override
    public void didUpdateSensorStatus(EmpaSensorStatus status, EmpaSensorType type) {
        Log.e(LOG_TAG, "didUpdateSensorStatus");
    }

    @Override
    public void didUpdateStatus(EmpaStatus status) {
        switch (status) {
            case DISCONNECTED:
                // Manager created or sensor dis{appeared|connected}
                // TODO 5 Find a way to give advice on when the signal is missing too many times. (Need E3 for test)
                if (_connected) {
                    _onConnectionChanged.end(this, ConnectEventHandler.Result.LOST);
                    _connected = false;
                    stopRecording();
                }
                _onConnectionChanged.end(this, ConnectEventHandler.Result.NOT_CONNECTED);
                break;
            case READY:
                // >> authenticate..
                // Manager authenticated
                _device.startScanning();
                _onConnectionChanged.end(this, ConnectEventHandler.Result.JUST_INITIALIZED);
                break;
            case DISCOVERING:
                // Manager discovering new devices
                _onConnectionChanged.end(this, ConnectEventHandler.Result.DISCOVERING);
                break;
            case CONNECTING:
                // >> connect device
                // Manager connecting to the found and allowed device
                _onConnectionChanged.end(this, ConnectEventHandler.Result.CONNECTING);
                break;
            case CONNECTED:
                // Manager can start streaming
                _connected = true;
                _onConnectionChanged.end(this, ConnectEventHandler.Result.CONNECTED);
                break;
            default:
                break;
        }
        Log.d(LOG_TAG, "didUpdateStatus " + _address.substring(_address.length() - 6, _address.length() - 1) + ".. " + status.toString());
        Log.d(LOG_TAG, "didUpdateStatus with msgs: " + __e3_streamed_messages);
    }

    // EmpaDataDelegate overrides

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
        received(E3Sensor.ACCELEROMETER.id, new double[]{timestamp, x, y, z});
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        received(E3Sensor.BVP.id, new double[]{timestamp, bvp});
    }

    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {
        Log.v(LOG_TAG, _code + ":didReceiveBatteryLevel = " + battery);
        received(E3Sensor.BATTERY.id, new double[]{timestamp, battery});
    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        received(E3Sensor.GSR.id, new double[]{timestamp, gsr});
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        received(E3Sensor.IBI.id, new double[]{timestamp, ibi});
    }

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
        received(E3Sensor.TEMPERATURE.id, new double[]{timestamp, temp});
    }

    // FS

    private void deleteDirTree(File dir) {
        Log.d(LOG_TAG, dir.getAbsolutePath());
        if (dir.isDirectory())
            for (String s : dir.list())
                deleteDirTree(new File(dir, s));
        boolean deleted = dir.delete();
        if (deleted)
            Log.d(LOG_TAG, "    " + dir.getName() + " - " + true);
        else
            Log.e(LOG_TAG, "    " + dir.getName() + " - " + false);
    }

    private void deleteDirTreeChecked(File dir) {
        if (dir.exists()) {
            Log.d(LOG_TAG, dir.getPath() + ": clearing..");
            deleteDirTree(dir);
            Log.d(LOG_TAG, "..done");
        } else
            Log.d(LOG_TAG, "Folder does not exist: " + dir.getPath());
    }

    private void clearLogs(Context context) {
        File appDir = new File(context.getCacheDir().getParent());
        // Empatica data
        deleteDirTreeChecked(new File(appDir, "files/Logs"));
        deleteDirTreeChecked(new File(appDir, "files/Sessions"));
    }

    private enum E3Sensor {
        ACCELEROMETER(0),
        BATTERY(1),
        BVP(2),
        GSR(3),
        IBI(4),
        TEMPERATURE(5);
        public int id;

        E3Sensor(int v) {
            id = v;
        }
    }

    public interface ConnectEventHandler {
        void end(EmpaticaBeam sender, Result result);

        public enum Result {
            /**
             * Does not happen. It just completes the enumerator.
             */
            JUST_INITIALIZED,
            /**
             * Does not happen. It just completes the enumerator.
             */
            NOT_CONNECTED,
            /**
             * Happens when the specified address can't be reached.
             */
            NOT_FOUND,
            /**
             * Happens when the found device is not registered with the authenticated API.
             */
            NOT_ALLOWED,
            /**
             * Happens when the inner manager is scanning for new devices.
             */
            DISCOVERING,
            /**
             * Happens when the device has been found and is connecting to the manager.
             */
            CONNECTING,
            /**
             * Happens when the device becomes completely connected and the manager can start
             * streaming data.
             */
            CONNECTED,
            /**
             * Happens when the device is connected but the manager sends a DISCONNECTED message.
             */
            LOST
        }
    }

    public interface DataCallback {
        void handle(int type, double[] data);
    }
}