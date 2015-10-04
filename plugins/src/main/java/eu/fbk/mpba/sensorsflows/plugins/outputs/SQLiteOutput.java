package eu.fbk.mpba.sensorsflows.plugins.outputs;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.OutputPlugin;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

/**
 * SQLite database Output Plug-In
 *
 * This plug-in saves the data in an SQLite database. The table is composed by the timestamp column
 * and a column for each float value in the array (the ValueT type is specified).
 */
public class SQLiteOutput implements OutputPlugin<Long, double[]> {

    String _name;
    String _path;
    SQLiteDatabase _sav;
    private int mCount = 0;

    public SQLiteOutput(String name, String path) {
        _name = name;
        _path = path;
    }

    public List<String> getFiles() {
        return Arrays.asList(_sav.getPath());
    }

    public void outputPluginInitialize(Object sessionTag, List<ISensor> linkedSensors) {
        File f = new File(_path + "/" + sessionTag.toString());
        //noinspection ResultOfMethodCallIgnored
        f.mkdirs();
        _sav = SQLiteDatabase.openOrCreateDatabase(new File(f, getName() + ".db"), null);
        for (ISensor l : linkedSensors) {
            _sav.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + getEventsTblName(l) +
                    " (timestamp INTEGER ASC, code INTEGER, message TEXT)");

            StringBuilder sb = new StringBuilder(100);
            sb.append("CREATE TABLE IF NOT EXISTS ");
            sb.append(getDataTblName(l));
            sb.append(" (timestamp INTEGER ASC");
            for (Object i : l.getValueDescriptor()) {
                sb.append(",[");
                sb.append(i.toString().replace("[", "").replace("]", ""));
                sb.append("] REAL");
            }
            sb.append(")");
            _sav.execSQL(sb.toString());
        }
    }

    public void outputPluginFinalize() {
        _sav.close();
    }

    public void newSensorEvent(SensorEventEntry<Long> event) {
        _sav.execSQL("INSERT INTO " + getEventsTblName(event.sensor) + " VALUES(?,?,?)", Arrays.asList(
                event.timestamp,
                event.code,
                event.message
        ).toArray());
        mCount++;
    }

    public void newSensorData(SensorDataEntry<Long, double[]> data) {
        ArrayList<Object> h = new ArrayList<>();

        h.add(data.timestamp);
        for (double i : data.value)
            h.add(i);

        StringBuilder sb = new StringBuilder("INSERT INTO " + getDataTblName(data.sensor) + " values (?");
        for (Object ignored : data.value)
            sb.append(",?");
        sb.append(")");
        _sav.execSQL(sb.toString(), h.toArray());
        mCount++;
    }

    public static String getDataTblName(ISensor s) {
        return "[data_" + s.getName().replace("[", "").replace("]", "") + "]";
    }

    public static String getEventsTblName(ISensor s) {
        return "[events_" + s.getName().replace("[", "").replace("]", "") + "]";
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public int getReceivedMessagesCount() {
        return getForwardedMessagesCount();
    }

    @Override
    public int getForwardedMessagesCount() {
        return mCount;
    }
}
