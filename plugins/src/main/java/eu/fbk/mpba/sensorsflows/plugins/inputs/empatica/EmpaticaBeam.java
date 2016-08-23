package eu.fbk.mpba.sensorsflows.plugins.inputs.empatica;

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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import eu.fbk.mpba.sensorsflows.plugins.PingMan;

/**
 * Main implementation of the Empatica Beam system.
 * Features:
 *   - MAC connection
 *   - First found connection
 *   - Re-discovery on connection lost (last device's MAC used)
 *   - SensorStatus (we hope they'll support it soon)
 *   - Transitory states events correction
 *   - Internet access detection
 *   - Splitted connection (authentication + connection)
 */
public class EmpaticaBeam implements EmpaStatusDelegate {
    private static final String LOG_TAG = "ALE EMP BEAM";
    protected final EmpaDeviceManager _device;
    protected final Runnable _enableBluetooth;
    protected final ConnectEventHandler _onConnectionChanged;
    protected final DeviceEventHandler _onDeviceEvent;
    private   final Context _context;
    protected String _address = null;
    protected String _devName;
    private   boolean _disconnectionPending = false;
    private   boolean _notReady = true;

    public EmpaticaBeam(Context context, EmpaDataDelegate onData,
                        ConnectEventHandler onConnectionChanged, DeviceEventHandler onDevice,
                        Runnable enableBluetooth) {
        clearLogs(context);
        _enableBluetooth = enableBluetooth;
        _onConnectionChanged = onConnectionChanged;
        _onDeviceEvent = onDevice;
        _device = new EmpaDeviceManager(_context = context, onData, this);
    }

    public String getAddress() {
        return _address;
    }

    public String getName() {
        return _devName;
    }

    public void assert_web_reachable() throws UnreachableWebException {
        if (!PingMan.isNetworkAvailable(_context)) {
            throw new UnreachableWebException("No network available! No manifest permission or no active network available.");
        } else if (!PingMan.isHttpColonSlashSlashWwwDotEmpaticaDotComReachable(_context)) {
            throw new UnreachableWebException("Empatica is not reachable via http!");
        }
    }

    public void doNotConnectToTheFirstAvailableButTo(String address) {
        _address = address;
    }

    public void authenticate(String key) {
        _device.authenticateWithAPIKey(key);
    }

    public void destroy() {
        Log.d(LOG_TAG, "destroy()");
        if (!_disconnectionPending) {
            try {
                _disconnectionPending = true;
                _device.stopScanning();
                _device.disconnect();
                _device.cleanUp();
            }
            catch (IllegalArgumentException | NullPointerException e) {
                Log.e(LOG_TAG, "destroy() " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    // EmpaStatusDelegate overrides

    @Override
    public void didDiscoverDevice(BluetoothDevice device, String label, int rssi, boolean allowed) {
        Log.v(LOG_TAG, "didDiscover: " + label + ", allowed: " + allowed);
        if (_address == null || _address.length() == 0 || device.getAddress().equals(_address)) {
            if (allowed) {
                _device.stopScanning();
                try {
                    _devName = label;
                    _address = device.getAddress();
                    _device.connectDevice(device);
                } catch (ConnectionNotAllowedException e) {
                    // Bisogna aver pazienza con loro...
                    allowed = false;
                    _device.startScanning();
                }
            }
            if (!allowed) {
                _onConnectionChanged.end(this, ConnectEventHandler.Result.NOT_ALLOWED);
            }
        }
    }

    @Override
    public void didRequestEnableBluetooth() {
        _enableBluetooth.run();
    }

    Map<EmpaSensorType, Boolean> mIsDead = new HashMap<>();
    @Override
    public void didUpdateSensorStatus(EmpaSensorStatus status, EmpaSensorType type) {
        Log.e(LOG_TAG, "didUpdateSensorStatus");
        if (status != EmpaSensorStatus.DEAD) { // UnDEAD
            if (!mIsDead.containsKey(type))
                mIsDead.put(type, false);

            if (mIsDead.get(type)) {
                mIsDead.put(type, false);
                _onDeviceEvent.end(this, type, DeviceEventHandler.Result.DEAD, false);
            }

            switch (status) {
                case NOT_ON_WRIST:
                    _onDeviceEvent.end(this, type, DeviceEventHandler.Result.ON_WRIST, false);
                    break;
                case ON_WRIST:
                    _onDeviceEvent.end(this, type, DeviceEventHandler.Result.ON_WRIST, true);
                    break;
            }
        }
        else { // DEAD
            mIsDead.put(type, true);
            _onDeviceEvent.end(this, type, DeviceEventHandler.Result.DEAD, true);
        }
    }

    EmpaStatus _lastStatus = EmpaStatus.DISCONNECTED;
    @Override
    public synchronized void didUpdateStatus(EmpaStatus status) {
        boolean repeated = _lastStatus.equals(status);
        _lastStatus = status;
        Log.d(LOG_TAG,
                "didUpdateStatus " + status.toString() + " (" + getAddress() + ")" +
                (repeated ? " repeated" : "")
        );
        if (!repeated)
            switch (status) {
                case DISCONNECTING:
                    break;
                case DISCONNECTED:
                    // Manager created or sensor dis{appeared|connected}
                    if (_disconnectionPending || _notReady) {
                        _onConnectionChanged.end(this, ConnectEventHandler.Result.NOT_CONNECTED);
                    } else {
                        _onConnectionChanged.end(this, ConnectEventHandler.Result.LOST);
                        _device.startScanning();
                        didUpdateStatus(EmpaStatus.DISCOVERING);// TODO OU: Delete this if used by empa (not yet)
                    }
                    break;
                case READY:
                    // >> authenticate..
                    // Manager authenticating
                    _notReady = false;
                    _onConnectionChanged.end(this, ConnectEventHandler.Result.INITIALIZED);
                    _device.startScanning();
                     didUpdateStatus(EmpaStatus.DISCOVERING);// TODO OU: Delete this if used by empa (not yet)
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
                    _onConnectionChanged.end(this, ConnectEventHandler.Result.CONNECTED);
                    break;
                default:
                    break;
            }
    }

    // FS

    private static void deleteDirTree(File dir) {
        Log.d(LOG_TAG, "Deleting " + dir.getAbsolutePath());
        if (dir.isDirectory())
            for (String s : dir.list())
                deleteDirTree(new File(dir, s));
        boolean deleted = dir.delete();
        Log.d(LOG_TAG, "    " + dir.getName() + " - " + (deleted ? "deleted" : "not deleted"));
    }

    private static void deleteDirTreeChecked(File dir) {
        if (dir.exists())
            deleteDirTree(dir);
    }

    public static void clearLogs(Context context) {
        File appDir = new File(context.getCacheDir().getParent());
        // Empatica data
        deleteDirTreeChecked(new File(appDir, "files/Logs"));
        deleteDirTreeChecked(new File(appDir, "files/Sessions"));
    }

    public interface DeviceEventHandler {
        void end(EmpaticaBeam sender, EmpaSensorType type, Result result, boolean status);

        enum Result {
            DEAD,
            ON_WRIST,
            STREAMING
        }
    }

    public interface ConnectEventHandler {
        void end(EmpaticaBeam sender, Result result);

        enum Result {
            /**
             * Does not happen. It just completes the enumerator.
             */
            INITIALIZED,
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

    public class UnreachableWebException extends IOException {
        public UnreachableWebException(String detailMessage) {
            super(detailMessage);
        }
    }
}