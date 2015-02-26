package eu.fbk.mpba.sensorsflows.debugapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import eu.fbk.mpba.sensorsflows.debugapp.util.EXLs3ManagerOld;

public class CupidManager {
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    protected BluetoothAdapter btAda;

    protected EXLs3ManagerOld[] svcs;

    public CupidManager(BluetoothAdapter adapter) {
        btAda = adapter;
        svcs = new EXLs3ManagerOld[16];
    }

    public void connect(BluetoothDevice device) {
        // devices.put(device, new BluetoothService(new IntHandler<>(device)));
        // TODO HEAD store devices
    }

    class IntHandler<T> extends Handler {

        private T ind;

        public T getIndex() {
            return ind;
        }

        public IntHandler(T index) {
            ind = index;
        }
    }
}
