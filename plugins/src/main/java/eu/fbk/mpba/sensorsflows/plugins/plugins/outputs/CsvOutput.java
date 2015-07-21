package eu.fbk.mpba.sensorsflows.plugins.plugins.outputs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
    List<ISensor> _linkedSensors = new ArrayList<>();

    public CsvOutput(String name, String path) {
        this(name, path, "timestamp", ".csv", ";", "\n");
    }

    public CsvOutput(String name, String path, String timestampColumnName, String fileSuffix, String separator, String newLine) {
        mName = name;
        mPath = path;
        mTsCol = timestampColumnName;
        mExtension = fileSuffix;
        mSeparator = separator;
        mNewLine = newLine;
    }

    public List<String> getFiles() {
        List<File>
        f = _savData.getSupports();
        f.addAll(_savEvents.getSupports());
        List<String> a = new ArrayList<>(f.size());
        for (int i = 0; i < f.size(); i++)
            a.add(f.get(i).getAbsolutePath());
        return a;
    }

    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {
        List<String> nn = new ArrayList<>(streamingSensors.size());
        for (ISensor s : streamingSensors)
            nn.add( s.getParentDevicePlugin().getClass().getSimpleName() +
                    "-" + s.getParentDevicePlugin().getName() +
                    "/" + s.getClass().getSimpleName() +
                    "-" + s.getName() );
        String p = mPath + "/" + sessionTag.toString() + "/"
                + CsvOutput.class.getSimpleName() + "-" + getName();
        _savData = new CsvDataSaver(p + "/data_", nn.toArray(), mExtension, mSeparator, mNewLine);
        _savEvents = new CsvDataSaver(p + "/events_", nn.toArray(), mExtension, mSeparator, mNewLine);
        _linkedSensors.addAll(streamingSensors);
        List<List<Object>> dataH = new ArrayList<>();
        List<List<Object>> evtH = new ArrayList<>();
        for (ISensor l : streamingSensors) {
            List<Object> h = new ArrayList<>();
            h.add(mTsCol);
            h.addAll(l.getValuesDescriptors());
            dataH.add(h);
            evtH.add(Arrays.asList((Object) mTsCol, "code", "message"));
        }
        _savData.init(dataH);
        _savEvents.init(evtH);
    }

    public void outputPluginFinalize() {
        _savData.close();
        _savEvents.close();
    }

    public void newSensorEvent(SensorEventEntry event) {
        mReceived++;
        List<Object> line = new ArrayList<>();
        line.add(event.timestamp.toString());
        line.add(event.code);
        line.add(event.message);
        _savEvents.save(_linkedSensors.indexOf(event.sensor), line);
        mForwarded++;
    }

    public void newSensorData(SensorDataEntry<Long, double[]> data) {
        mReceived++;
        List<Object> line = new ArrayList<>();
        line.add(data.timestamp.toString());
        for (int i = 0; i < data.value.length; i++)
            line.add(data.value[i]);
        _savData.save(_linkedSensors.indexOf(data.sensor), line);
        mForwarded++;
    }

    @Override
    public String getName() {
        return mName;
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
