package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.base.IMonoTimestampSource;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.ISensorDataCallback;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class SensorComponent<TimeT, ValueT> implements ISensor {
    protected DevicePlugin<TimeT, ValueT> _parent = null;
    protected ISensorDataCallback<SensorComponent<TimeT, ValueT>, TimeT, ValueT> _handler;
    protected ArrayList<OutputDecorator<TimeT, ValueT>> _outputs = new ArrayList<>();

    protected SensorComponent(DevicePlugin<TimeT, ValueT> parent) {
        _parent = parent;
    }

    private int mForwardedMessages = 0;
    private boolean mListened = true;
    protected SensorStatus mStatus = SensorStatus.OFF;

    private static long _bootTime = System.currentTimeMillis() * 1_000_000L - System.nanoTime();

    private static IMonoTimestampSource _time = new IMonoTimestampSource() {

        @Override
        public long getMonoUTCNanos() {
            return System.nanoTime() + _bootTime;
        }

        @Override
        public long getMonoUTCNanos(long realTimeNanos) {
            return realTimeNanos + _bootTime;
        }
    };

    void addOutput(OutputDecorator<TimeT, ValueT> _output) {
        _outputs.add(_output);
    }

    void setManager(ISensorDataCallback<SensorComponent<TimeT, ValueT>, TimeT, ValueT> man) {
        _handler = man;
    }

    Iterable<OutputDecorator<TimeT, ValueT>> getOutputs() {
        return new ReadOnlyIterable<>(_outputs.iterator());
    }

    // Managed protected getters setters

    protected void changeStatus(SensorStatus state) {
        if (_handler != null)
            _handler.sensorStateChanged(this, null, mStatus = state);
    }

    // Managed Overrides

    public int getForwardedMessagesCount() {
        return mForwardedMessages;
    }

    public int getReceivedMessagesCount() {
        return getForwardedMessagesCount();
    }

    public DevicePlugin<TimeT, ValueT> getParentDevicePlugin() {
        return _parent;
    }

    @Override
    public SensorStatus getState() {
        return mStatus;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public IMonoTimestampSource getTime() {
        return _time;
    }

    // Notify methods

    public void sensorValue(TimeT time, ValueT value) {
        if (_handler != null) {
            _handler.sensorValue(this, time, value);
            mForwardedMessages++;
        }
    }

    public void sensorEvent(TimeT time, int type, String message) {
        if (_handler != null) {
            _handler.sensorEvent(this, time, type, message);
            mForwardedMessages++;
        }
    }

    // Listenage

    public boolean isListened() {
        return mListened;
    }

    public void setListened(boolean listened) {
        this.mListened = listened;
    }

    // To implement

    public abstract List<Object> getValueDescriptor();
}
