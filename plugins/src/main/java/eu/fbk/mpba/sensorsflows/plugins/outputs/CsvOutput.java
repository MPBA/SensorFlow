package eu.fbk.mpba.sensorsflows.plugins.outputs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import eu.fbk.mpba.sensorsflows.OutputPlugin;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

/**
 * Comma Separated Values Output Plug-In
 *
 * This plug-in saves the data in a CSV file. The table is composed by the timestamp column and a
 * column for each float value in the array (the ValueT type is specified).
 */
public class CsvOutput implements OutputPlugin<Long, double[]> {

    private final String mTsCol;
    private final String mExtension;
    private final String mNewLine;
    private final String mSeparator;
    private int mReceived = 0;
    private int mForwarded = 0;
    String mName;
    String mPath;
    CsvDataSaver _savData;
    CsvDataSaver _savEvents;
    HashMap<ISensor, Integer> _reverseSensors = new HashMap<>();
    Callback call;
    private List<String> files;

    public interface Callback {
        void finalization(CsvOutput sender);
    }

    public CsvOutput(String name, String path) {
        this(name, path, "timestamp", ".csv", ";", "\n", null);
    }

    public CsvOutput(String name, String path, String timestampColumnName, String fileSuffix, String separator, String newLine, Callback c) {
        mName = name;
        mPath = path;
        mTsCol = timestampColumnName;
        mExtension = fileSuffix;
        mSeparator = separator;
        mNewLine = newLine;
        call = c;
    }

    public List<String> getFiles() {
        return files;
    }

    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {
        List<String> nn = new ArrayList<>(streamingSensors.size());
        _reverseSensors = new HashMap<>(streamingSensors.size(), 1f);
        for (int i = 0; i < streamingSensors.size(); i++) {
            nn.add(streamingSensors.get(i).getParentDevicePlugin().getClass().getSimpleName() +
                    "-" + streamingSensors.get(i).getParentDevicePlugin().getName() +
                    "/" + streamingSensors.get(i).getClass().getSimpleName() +
                    "-" + streamingSensors.get(i).getName());
            _reverseSensors.put(streamingSensors.get(i), i);
        }
        String p = mPath + "/" + sessionTag.toString() + "/"
                + CsvOutput.class.getSimpleName() + "-" + getName();
        _savData = new CsvDataSaver(p + "/data_", nn.toArray(), mExtension, mSeparator, mNewLine);
        _savEvents = new CsvDataSaver(p + "/events_", nn.toArray(), mExtension, mSeparator, mNewLine);
        List<List<Object>> dataH = new ArrayList<>();
        List<List<Object>> evtH = new ArrayList<>();
        for (ISensor l : streamingSensors) {
            List<Object> h = new ArrayList<>();
            h.add(mTsCol);
            h.addAll(l.getValueDescriptor());
            dataH.add(h);
            evtH.add(Arrays.asList((Object) mTsCol, "code", "message"));
        }
        _savData.init(dataH);
        _savEvents.init(evtH);

        files = new ArrayList<>(_savEvents.getSupports().size());
        for (File f : _savEvents.getSupports())
            files.add(f.getAbsolutePath());
    }

    public void outputPluginFinalize() {
        close();
    }

    Long mEventsT = 0L, mDataT = 0L;

    public void newSensorEvent(SensorEventEntry event) {
        mReceived++;
        List<Object> line = new ArrayList<>();
        line.add((Long)event.timestamp - mEventsT);
        // mEventsT = (Long)event.timestamp;
        line.add(event.code);
        line.add(event.message);
        _savEvents.save(_reverseSensors.get(event.sensor), line);
        mForwarded++;
    }

    public void newSensorData(SensorDataEntry<Long, double[]> data) {
        mReceived++;
        List<Object> line = new ArrayList<>();
        line.add(data.timestamp - mDataT);
        // mDataT = data.timestamp;
        for (int i = 0; i < data.value.length; i++)
            line.add(data.value[i]);
        _savData.save(_reverseSensors.get(data.sensor), line);
        mForwarded++;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public void close() {
        if (_savData != null) {
            if (call != null) {
                call.finalization(this); // once!!
                call = null;
            }
            _savData.close();
            _savData = null;
        }
        if (_savEvents != null) {
            _savEvents.close();
            _savEvents = null;
        }
    }

    @Override
    public int getReceivedMessagesCount() {
        return mReceived;
    }

    @Override
    public int getForwardedMessagesCount() {
        return mForwarded;
    }
}
