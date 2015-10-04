package eu.fbk.mpba.sensorsflows.plugins.inputs.empatica;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import java.io.IOException;

import eu.fbk.mpba.sensorsflows.plugins.PingMan;

public class EmpaticaScanner {
    private Runnable mEnableBT;
    private EmpaDeviceManager mDevMan;
    private String mKey;
    private Context mContext;

    public class Bracelet {
        public BluetoothDevice device;
        public String name;
        private boolean mAllowed;
    }

    public interface NewDeviceFound {
        public void newDevice(Bracelet b, int rssi, boolean allowed);
    }

    public void assert_web_reachable() throws UnreachableWebException {
        if (!PingMan.isNetworkAvailable(mContext)) {
            throw new UnreachableWebException("network");
        } else if (!PingMan.isHttpColonSlashSlashWwwDotEmpaticaDotComReachable(mContext)) {
            throw new UnreachableWebException("http");
        }
    }

    public EmpaticaScanner(final Context context, String key, final NewDeviceFound callback, final Runnable enableBluetooth) {
        EmpaticaBeam.clearLogs(context);
        mContext = context;
        mEnableBT = enableBluetooth;
        mDevMan = new EmpaDeviceManager(context, new EmpaDataDelegate() {
            @Override
            public void didReceiveGSR(float v, double v2) {

            }

            @Override
            public void didReceiveBVP(float v, double v2) {

            }

            @Override
            public void didReceiveIBI(float v, double v2) {

            }

            @Override
            public void didReceiveTemperature(float v, double v2) {

            }

            @Override
            public void didReceiveAcceleration(int i, int i2, int i3, double v) {

            }

            @Override
            public void didReceiveBatteryLevel(float v, double v2) {

            }
        }, new EmpaStatusDelegate() {
            @Override
            public void didUpdateStatus(EmpaStatus empaStatus) {
                if (empaStatus == EmpaStatus.READY)
                    start();
            }

            @Override
            public void didUpdateSensorStatus(EmpaSensorStatus empaSensorStatus, EmpaSensorType empaSensorType) {

            }

            @Override
            public void didDiscoverDevice(BluetoothDevice bluetoothDevice, String s, int i, boolean b) {
                Bracelet x = new Bracelet();
                x.mAllowed = b;
                x.device = bluetoothDevice;
                x.name = s;
                callback.newDevice(x, i, b);
            }

            @Override
            public void didRequestEnableBluetooth() {
                enableBluetooth.run();
            }
        });
        mDevMan.authenticateWithAPIKey(mKey = key);
    }

    public void start() {
        mDevMan.startScanning();
    }

    public void stop() {
        mDevMan.stopScanning();
    }

    public EmpaticaDevice stop(Bracelet b) {
        stop();
        if (!b.mAllowed)
            return null;
        else {
            return new EmpaticaDevice(mKey, mContext, b.device.getAddress(), mEnableBT);
        }
    }

    public class UnreachableWebException extends IOException {
        public UnreachableWebException(String detailMessage) {
            super(detailMessage);
        }
    }
}
