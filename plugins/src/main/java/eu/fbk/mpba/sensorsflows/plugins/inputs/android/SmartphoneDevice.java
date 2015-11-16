package eu.fbk.mpba.sensorsflows.plugins.inputs.android;

import android.content.Context;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

public class SmartphoneDevice implements DevicePlugin<Long, double[]> {

    private final SntpSensor _sntpClient;
    private String name;
    private List<SensorComponent<Long, double[]>> _sensors;
    private TextEventsSensor<double[]> _textSensor;
    private UdpTimeOffsetSensor _udpTimeOffsetSensor;

    public SmartphoneDevice(Context context, String name) {
        this(context, name, true, true, true, true, true, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public SmartphoneDevice(Context context, String name, boolean text, boolean udpTime, boolean sntp, boolean gps, boolean accelerometer, int accSensorDelay) {
        this.name = name;
        _sensors = new ArrayList<>();
        if (gps)
            _sensors.add(new GpsSensor(this, context, 0, 0));
        if (accelerometer)
            _sensors.add(new AccelerometerSensor(this, context, accSensorDelay));
        if (text)
            _sensors.add(_textSensor = new TextEventsSensor<>(this));
        if (udpTime)
            _sensors.add(_udpTimeOffsetSensor = new UdpTimeOffsetSensor(this));
        if (sntp)
            _sensors.add(_sntpClient = new SntpSensor(this));
        else
            _sntpClient = null;
    }

    // Time markers

    public void addNoteNow(String text) {
        _textSensor.addText(text);
    }

    public void logNow(int code, String text) {
        _textSensor.addLog(code, text);
    }

    public interface Note {
        void commit(String text);
    }

    public Note newNote() {
        return new Note() {
            long time = _textSensor.getTime().getMonoUTCNanos();

            @Override
            public void commit(String text) {
                _textSensor.addTimedText(time, text);
            }
        };
    }

    // Time server

    public void setTimeServerEnabled(boolean enabled) {
        if (enabled)
            _udpTimeOffsetSensor.startTimeServer();
        else
            _udpTimeOffsetSensor.stopTimeServer();
    }

    public boolean isTimeServerEnabled() {
        return _udpTimeOffsetSensor.isTimeServerRunning();
    }

    // Time client

    public void computeOffsetBroadcastedAsync(int passes, LanUdpTimeClient.TimeOffsetCallback cb) {
        _udpTimeOffsetSensor.computeOnEveryServer(passes, cb);
    }

    public void clearTimeOffsets() {
        _udpTimeOffsetSensor.clear();
    }

    // SNTP client

    public interface NtpCallback {
        void end(SntpSensor.NtpResp r);
    }

    public void computeNtpAsync(final NtpCallback cb) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                cb.end(_sntpClient.compute());
            }
        }, "AsyncNtpCompute-"+System.currentTimeMillis()).start();
    }

    public void setNtpServers(Collection<String> servers) {
        _sntpClient.setServers(servers);
    }

    // Ov

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return new ReadOnlyIterable<>(_sensors.iterator());
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public void close() {
        for (SensorComponent x : _sensors)
            x.switchOffAsync();
    }

    @Override
    public void inputPluginInitialize() {
        for (SensorComponent<Long, double[]> s : _sensors) {
            s.switchOnAsync();
        }
    }

    @Override
    public void inputPluginFinalize() {
        for (SensorComponent<Long, double[]> s : _sensors) {
            s.switchOffAsync();
        }
    }

    @Override
    public String getName() {
        return name;
    }
}

