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
    protected NodePlugin<TimeT, ValueT> _parent = null;
    protected List<ISensorDataCallback<SensorComponent<TimeT, ValueT>, TimeT, ValueT>> _handler = new ArrayList<>();
    protected ArrayList<OutputDecorator<TimeT, ValueT>> _outputs = new ArrayList<>();

    protected SensorComponent(NodePlugin<TimeT, ValueT> parent) {
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

        @Override
        public long getMonoUTCMillis() {
            return getMonoUTCNanos() / 1_000_000;
        }

        @Override
        public long getMonoUTCMillis(long realTimeNanos) {
            return getMonoUTCNanos(realTimeNanos) / 1_000_000;
        }
    };

    void addOutput(OutputDecorator<TimeT, ValueT> _output) {
        _outputs.add(_output);
    }

    void registerManager(ISensorDataCallback<SensorComponent<TimeT, ValueT>, TimeT, ValueT> man) {
        _handler.add(man);
    }

    void unregisterManager(ISensorDataCallback<SensorComponent<TimeT, ValueT>, TimeT, ValueT> man) {
        _handler.remove(man);
    }

    Iterable<OutputDecorator<TimeT, ValueT>> getOutputs() {
        return new ReadOnlyIterable<>(_outputs.iterator());
    }

    // Managed protected getters setters

    protected void changeStatus(SensorStatus state) {
        for (ISensorDataCallback<SensorComponent<TimeT, ValueT>, TimeT, ValueT> i : _handler) {
//            if (i instanceof FlowsMan && ((FlowsMan)i).getStatus() == EngineStatus.CLOSED)
//                _handler.remove(i);
            i.sensorStatusChanged(this, null, mStatus = state);
        }
    }

    /**
     * Unregisters every outputDecorator
     */
    public void close() {
        _handler.clear();
        _outputs.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    // Managed Overrides

    public NodePlugin<TimeT, ValueT> getParentDevicePlugin() {
        return _parent;
    }

    @Override
    public SensorStatus getStatus() {
        return mStatus;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public static IMonoTimestampSource getSTime() {
        return _time;
    }

    public IMonoTimestampSource getTime() {
        return _time;
    }

    // Notify methods

    public void sensorValue(TimeT time, ValueT value) {
        for (ISensorDataCallback<SensorComponent<TimeT, ValueT>, TimeT, ValueT> i : _handler) {
//            if (i instanceof FlowsMan && ((FlowsMan)i).getStatus() == EngineStatus.CLOSED)
//                _handler.remove(i);
            i.sensorValue(this, time, value);
        }
        mForwardedMessages++;
    }

    public void sensorEvent(TimeT time, int type, String message) {
        for (ISensorDataCallback<SensorComponent<TimeT, ValueT>, TimeT, ValueT> i : _handler) {
//            if (i instanceof FlowsMan && ((FlowsMan)i).getStatus() == EngineStatus.CLOSED)
//                _handler.remove(i);
            i.sensorEvent(this, time, type, message);
        }
        mForwardedMessages++;
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
