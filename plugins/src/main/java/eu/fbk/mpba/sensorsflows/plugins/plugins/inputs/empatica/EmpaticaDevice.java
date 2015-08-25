package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.empatica;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.delegate.EmpaDataDelegate;

import java.util.Arrays;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

public class EmpaticaDevice implements DevicePlugin<Long, double[]> {

    final String LOG_TAG = "ALE EMP DEV";

    private boolean lastCheckTODO = true;

    final EmpaticaBeam beam;
    final EmpaticaSensor.Accelerometer mAcc;
    final EmpaticaSensor.Battery mBat;
    final EmpaticaSensor.BVP mBvp;
    final EmpaticaSensor.GSR mGsr;
    final EmpaticaSensor.IBI mIbi;
    final EmpaticaSensor.Thermometer mTem;

    // This two values to perform an approximation of the times of the events keeping the order safe
    // also through the values.
    private Long mTimeToDevice = 0L;

    private Long proTime(double seconds) {
        Long nanoTime = System.nanoTime();
        Long nanos = (long) (seconds * 1_000_000_000);
        mTimeToDevice = (mTimeToDevice * 2 - nanoTime + nanos) / 3;
        return nanos;
    }

    private Long proTime() {
        return mTimeToDevice + System.nanoTime();
    }

    public EmpaticaDevice(String key, final Context context, String address, Runnable enableBluetooth) {
        this(key, context, address, enableBluetooth, null);
    }

    public EmpaticaDevice(String key, final Context context, String address, Runnable enableBluetooth, final Runnable connectedStreaming) {
        final EmpaticaDevice _this = this;
        beam = new EmpaticaBeam(context,
                new EmpaDataDelegate() {
                    @Override
                    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
                        if (lastCheckTODO) {
                            lastCheckTODO = false;
                            if (connectedStreaming != null)
                                new Thread(connectedStreaming).start();
                        }
                        mAcc.sensorValue(proTime(timestamp), new double[]{x, y, z});
                    }

                    @Override
                    public void didReceiveBVP(float bvp, double timestamp) {
                        mBvp.sensorValue(proTime(timestamp), new double[]{bvp});
                    }

                    @Override
                    public void didReceiveBatteryLevel(float battery, double timestamp) {
                        mBat.sensorValue(proTime(timestamp), new double[]{battery});
                        Log.v(LOG_TAG, EmpaticaDevice.this.getName() + " didReceiveBatteryLevel: " + battery);
                    }

                    @Override
                    public void didReceiveGSR(float gsr, double timestamp) {
                        mGsr.sensorValue(proTime(timestamp), new double[]{gsr});
                    }

                    @Override
                    public void didReceiveIBI(float ibi, double timestamp) {
                        mIbi.sensorValue(proTime(timestamp), new double[]{ibi});
                    }

                    @Override
                    public void didReceiveTemperature(float temp, double timestamp) {
                        mTem.sensorValue(proTime(timestamp), new double[]{temp});
                    }
                },
                new EmpaticaBeam.ConnectEventHandler() {
                    @Override
                    public void end(EmpaticaBeam sender, Result result) {
                        //noinspection unchecked
                        EmpaticaSensor[] is = new EmpaticaSensor[]{
                                EmpaticaDevice.this.mAcc,
                                EmpaticaDevice.this.mBat,
                                EmpaticaDevice.this.mBvp,
                                EmpaticaDevice.this.mGsr,
                                EmpaticaDevice.this.mIbi,
                                EmpaticaDevice.this.mTem};
                        Long now = proTime();
                        for (EmpaticaSensor s : is) {
                            if (s != null)
                                s.sensorEvent(now, 1, result.toString());
                        }
                        Log.d(LOG_TAG, result.toString() + " " + sender.getName() + " " + sender.getAddress());
                    }
                },
                new EmpaticaBeam.DeviceEventHandler() {
                    @Override
                    public void end(EmpaticaBeam sender, EmpaSensorType type, Result result, boolean status) {
                        switch (type) {
                            case BVP:
                                mBvp.sensorEvent(_this.proTime(), 2, EmpaSensorType.class.getSimpleName() + "." + type.toString());
                                mIbi.sensorEvent(_this.proTime(), 2, EmpaSensorType.class.getSimpleName() + "." + type.toString());
                                break;
                            case GSR:
                                mGsr.sensorEvent(_this.proTime(), 2, EmpaSensorType.class.getSimpleName() + "." + type.toString());
                                break;
                            case ACC:
                                mAcc.sensorEvent(_this.proTime(), 2, EmpaSensorType.class.getSimpleName() + "." + type.toString());
                                break;
                            case TEMP:
                                mTem.sensorEvent(_this.proTime(), 2, EmpaSensorType.class.getSimpleName() + "." + type.toString());
                                break;
                        }
                    }
                }, enableBluetooth);
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
        mTem = new EmpaticaSensor.Thermometer(this);
        if (address != null)
            beam.doNotConnectToTheFirstAvailableButTo(address);
        beam.authenticate(key);
    }

    @Override
    public void inputPluginInitialize() {

    }

    @Override
    public void inputPluginFinalize() {
        beam.destroy();
    }

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return new ReadOnlyIterable<>(Arrays.asList((SensorComponent<Long, double[]>) mAcc, mBat, mBvp, mIbi, mGsr, mTem).iterator());
    }

    @Override
    public String getName() {
        return beam.getName() == null ? EmpaticaDevice.class.getSimpleName() : beam.getName();
    }
}
