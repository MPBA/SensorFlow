package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.empatica;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.empatica.empalink.delegate.EmpaDataDelegate;

import java.util.Arrays;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

public class EmpaticaDevice implements DevicePlugin<Long, double[]> {

    final String LOG_TAG = "ALE EMPA DEV";

    final EmpaticaBeam beam;
    final EmpaticaSensor.Accelerometer mAcc;
    final EmpaticaSensor.Battery mBat;
    final EmpaticaSensor.BVP mBvp;
    final EmpaticaSensor.GSR mGsr;
    final EmpaticaSensor.IBI mIbi;
    final EmpaticaSensor.Termometer mTem;

    public EmpaticaDevice(String key, final Context context, String address, Runnable enableBluetooth) {
        beam = new EmpaticaBeam(context, address, data, conn, enableBluetooth);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    beam.assert_web_reachable();
                } catch (EmpaticaBeam.UnreachableWebException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Get an internet connection and restart the app!", Toast.LENGTH_LONG).show();
                }
            }
        });
        mAcc = new EmpaticaSensor.Accelerometer(this);
        mBat = new EmpaticaSensor.Battery(this);
        mBvp = new EmpaticaSensor.BVP(this);
        mGsr = new EmpaticaSensor.GSR(this);
        mIbi = new EmpaticaSensor.IBI(this);
        mTem = new EmpaticaSensor.Termometer(this);
        beam.authenticate(key);
    }

    public String getAddress() {
        return beam.getAddress();
    }

    @Override
    public void inputPluginInitialize() {
        beam.startStreaming();
    }

    @Override
    public void inputPluginFinalize() {
        beam.stopStreaming();
        beam.destroy();
    }

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return new ReadOnlyIterable<>(Arrays.asList((SensorComponent<Long, double[]>) mAcc, mBat, mBvp, mIbi, mGsr, mTem).iterator());
    }

    EmpaticaBeam.ConnectEventHandler conn = new EmpaticaBeam.ConnectEventHandler() {
        @Override
        public void end(EmpaticaBeam sender, Result result) {
            switch (result) {
                case JUST_INITIALIZED:
                    break;
                case NOT_CONNECTED:
                    break;
                case NOT_FOUND:
                    break;
                case NOT_ALLOWED:
                    break;
                case DISCOVERING:
                    break;
                case CONNECTING:
                    break;
                case CONNECTED:
                    break;
                case LOST:
                    break;
            }
            Log.d(LOG_TAG, sender.getAddress() + " " + sender.getCode() + " -> " + result.toString());
        }
    };

    EmpaDataDelegate data = new EmpaDataDelegate() {
        @Override
        public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
            mAcc.sensorValue((long)timestamp, new double[]{x, y, z});
        }

        @Override
        public void didReceiveBVP(float bvp, double timestamp) {
            mBat.sensorValue((long) timestamp, new double[]{bvp});
        }

        @Override
        public void didReceiveBatteryLevel(float battery, double timestamp) {
            Log.v("Empatica", EmpaticaDevice.this.getAddress() + " didReceiveBatteryLevel = " + battery);
            mBat.sensorValue((long)timestamp, new double[]{battery});
        }

        @Override
        public void didReceiveGSR(float gsr, double timestamp) {
            mGsr.sensorValue((long)timestamp, new double[]{gsr});
        }

        @Override
        public void didReceiveIBI(float ibi, double timestamp) {
            mIbi.sensorValue((long)timestamp, new double[]{ibi});
        }

        @Override
        public void didReceiveTemperature(float temp, double timestamp) {
            mTem.sensorValue((long)timestamp, new double[]{temp});
        }
    };
}
