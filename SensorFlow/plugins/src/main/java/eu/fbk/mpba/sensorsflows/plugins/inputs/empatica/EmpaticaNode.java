package eu.fbk.mpba.sensorsflows.plugins.inputs.empatica;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.delegate.EmpaDataDelegate;

import java.util.Arrays;

import eu.fbk.mpba.sensorsflows.NodePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

public class EmpaticaNode implements NodePlugin<Long, double[]> {

    final String LOG_TAG = "ALE EMP DEV";

    private boolean lastCheckTODO = true;
    private float batteryLevel = -1;

    final EmpaticaBeam beam;
    final EmpaticaSensor.Accelerometer mAcc;
    final EmpaticaSensor.Battery mBat;
    final EmpaticaSensor.BVP mBvp;
    final EmpaticaSensor.GSR mGsr;
    final EmpaticaSensor.IBI mIbi;
    final EmpaticaSensor.Thermometer mTem;

    // This two value to perform an approximation of the times of the events keeping the order safe
    // also through the values.
    private Long mTimeToDevice = 0L;

    /**
     * Converts the seconds in nanos and updates TimeToDevice
     * @param seconds timestamp received in seconds
     * @return timestamp received in nanoseconds
     */
    private Long proTime(double seconds) {
        Long nanoTime = System.nanoTime();
        Long nanos = (long) (seconds * 1_000_000_000);
        mTimeToDevice = (mTimeToDevice * 2 - nanoTime + nanos) / 3;
        return nanos;
    }

    private Long proTime() {
        return mTimeToDevice + System.nanoTime();
    }

    public EmpaticaNode(String key, final Context context, String address, Runnable enableBluetooth) {
        this(key, context, address, enableBluetooth, null);
    }

    public EmpaticaNode(String key, final Context context, String address, Runnable enableBluetooth, final Runnable connectedStreaming) {
        final EmpaticaNode _this = this;
        beam = new EmpaticaBeam(context,
                new EmpaDataDelegate() {
                    @Override
                    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
                        mAcc.sensorValue(proTime(timestamp), new double[]{x, y, z});
                    }

                    @Override
                    public void didReceiveBVP(float bvp, double timestamp) {
                        mBvp.sensorValue(proTime(timestamp), new double[]{bvp});
                    }

                    @Override
                    public void didReceiveBatteryLevel(float battery, double timestamp) {
                        batteryLevel = battery;
                        if (lastCheckTODO) {
                            lastCheckTODO = false;
                            if (connectedStreaming != null)
                                new Handler(context.getMainLooper()).post(connectedStreaming);
                        }
                        mBat.sensorValue(proTime(timestamp), new double[]{battery});
                        Log.v(LOG_TAG, EmpaticaNode.this.getName() + " didReceiveBatteryLevel: " + battery);
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
                                EmpaticaNode.this.mAcc,
                                EmpaticaNode.this.mBat,
                                EmpaticaNode.this.mBvp,
                                EmpaticaNode.this.mGsr,
                                EmpaticaNode.this.mIbi,
                                EmpaticaNode.this.mTem};
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
    public void inputPluginStart() {
        // Nothing to do before the start
    }

    @Override
    public void inputPluginStop() {
        // Device kept connected
    }

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return new ReadOnlyIterable<>(Arrays.asList((SensorComponent<Long, double[]>) mAcc, mBat, mBvp, mIbi, mGsr, mTem).iterator());
    }

    @Override
    public String getName() {
        return beam.getName() == null ? EmpaticaNode.class.getSimpleName() : beam.getName();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
        // TODO 3: Make every plug-in replayable with the same settings and so add finalize method (and better the close one)
        // After an Output/Input Finalize an other Initialize may occur, minimal waste.
    }

    public void close() {
        beam.destroy();
    }

    public float getBatteryLevel() {
        return batteryLevel;
    }
}
