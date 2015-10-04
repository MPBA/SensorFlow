package eu.fbk.mpba.sensorsflows.plugins.outputs.litix;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import eu.fbk.mpba.sensorsflows.OutputPlugin;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;
import eu.fbk.mpba.sensorsflows.plugins.outputs.skilo.SensorsProtobuffer;
import eu.fbk.mpba.sensorsflows.plugins.outputs.skilo.SensorsProtobuffer.SensorInfo;

public class ProtobufferOutput implements OutputPlugin<Long, double[]> {

    protected File mFolder;
    protected long mFlushSize;
    protected Object mSessionTag = "undefined";
    protected List<SensorInfo> mSensorInfo = new ArrayList<>();
    protected List<ISensor> mSensors = new ArrayList<>();
    protected List<SensorsProtobuffer.SensorData> mSensorData = new ArrayList<>();
    protected final String uuid;
    private String mName;
    private UUID uid;
    private Dictionary<Class, SensorInfo.TYPESENSOR> mTypesMap;
    private int mReceived = 0;
    private int mForwarded = 0;
    private int seqNumber = 0;
    private int mTimeOffsetMillis = 0;

    public ProtobufferOutput(String name, File dir, long flushSizeElements, String phoneId, int timeOffsetMillis, Dictionary<Class, SensorInfo.TYPESENSOR> sensorTypesMap) {
        mName = name;
        mFolder = dir;
        mFlushSize = flushSizeElements;
        uuid = phoneId;
        uid = UUID.randomUUID();
        mTypesMap = sensorTypesMap;
        mTimeOffsetMillis = timeOffsetMillis;
    }

    public long currentBacklogSize() {
        return mSensorData.size();
    }

    public void flushTrackSplit(List<SensorsProtobuffer.SensorData> x, String fileName, boolean last) {
        Log.d("ProtoOut", "Flushing " + x.size() + " SensorData");
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(fileName, false);
        } catch (FileNotFoundException e) {
            Log.e("ProtoOut", "Flush can't open the file");
            e.printStackTrace();
        }
        SensorsProtobuffer.TrackSplit s = SensorsProtobuffer.TrackSplit.newBuilder()
                .addAllInfo(mSensorInfo)
                .addAllDatas(x)
                .setTrackUid(uid.toString())
                .setIsLast(last)
                .setPhoneId(uuid)
                .setSequenceNumber(seqNumber++)
                .setTsStart(x.get(0).getTimestamp())
                .setTsStop(x.get(x.size() - 1).getTimestamp())
                .setDelay(mTimeOffsetMillis)
                .setTimezone(TimeZone.getDefault().getID())
                .build();

        try {
            s.writeTo(output);
            if (output != null)
                output.close();
            ProtobufferOutput.this.mForwarded += currentBacklogSize();
        } catch (IOException e) {
            Log.e("ProtoOut", "Flush can't write or close the file");
            e.printStackTrace();
        }
        int xl = x.size();
        x.clear();
        System.gc();
        Log.v("ProtoOut", "Flush cleared list of size:" + xl);
    }

    private long bootUTCNanos = System.currentTimeMillis() * 1_000_000L + System.nanoTime();

    private long getMonoTimeMillis() {
        return (System.nanoTime() + bootUTCNanos) / 1_000_000L;
    }

    public String getTrackSplitNameForNow() {
        return new File(mFolder, getMonoTimeMillis() + ".pb").getAbsolutePath();
    }

    String join(List<Object> x) {
        StringBuilder r = new StringBuilder();
        if (x.size() > 0) {
            r.append(x.get(0).toString());
            for (int i = 1; i < x.size(); i++)
                r.append(';').append(x.get(i));
        }
        return r.toString();
    }

    private void flushAsync() {
        final List<SensorsProtobuffer.SensorData> x = mSensorData;
        mSensorData = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                flushTrackSplit(x, getTrackSplitNameForNow(), false);
            }
        }, "Flush " + x.size() + " from "  + x.get(0).getTimestamp()).start();
    }

    // OutputPlugIn implementation

    @Override
    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {
        mSensors = streamingSensors;
        mSessionTag = sessionTag;
        mFolder = new File(mFolder, mSessionTag.toString() + "/" + getName());
        //noinspection ResultOfMethodCallIgnored
        mFolder.mkdirs();
        for (int s = 0; s < mSensors.size(); s++) {
            SensorInfo.TYPESENSOR type = mTypesMap.get(mSensors.get(s).getClass());
            mSensorInfo.add(SensorInfo.newBuilder()
                    .setSensorId(s)
                    .setDesc("data_" + mSensors.get(s).getName())
                    .setType(type == null ? SensorInfo.TYPESENSOR.OTHER : type)
                    .setMeta(join(mSensors.get(s).getValueDescriptor()))
                    .build());
            mSensorInfo.add(SensorInfo.newBuilder()
                    .setSensorId(mSensors.size() + s)
                    .setDesc("events_" + mSensors.get(s).getName())
                    .setType(SensorInfo.TYPESENSOR.OTHER)
                    .setMeta("timestamp;code;message")
                    .build());
        }
    }

    @Override
    public void outputPluginFinalize() {
        flushTrackSplit(mSensorData, getTrackSplitNameForNow(), true);
    }

    @Override
    public void newSensorEvent(SensorEventEntry<Long> event) {
        mReceived++;
        mSensorData.add(SensorsProtobuffer.SensorData.newBuilder()
                        .setId(dataInc++)
                        .setSensorIdFk(mSensors.indexOf(event.sensor) + mSensors.size())
                        .setTimestamp(event.timestamp / 1000000000.)
                        .addValue(event.code)
                        .addText(event.message)
                        .build()
        );
        if (mSensorData.size() >= mFlushSize) {
            flushAsync();
        }
    }

    private int dataInc = 0;

    @Override
    public void newSensorData(SensorDataEntry<Long, double[]> data) {
        mReceived++;
        List<Double> v = new ArrayList<>(7);
        for (int i = 0; i < data.value.length; i++)    // double conversion to Float
            v.add(data.value[i]);                      // double conversion to Float

        mSensorData.add(SensorsProtobuffer.SensorData.newBuilder()
                        .setId(dataInc++)
                        .setSensorIdFk(mSensors.indexOf(data.sensor))
                        .addAllValue(v)
                        .setTimestamp(data.timestamp / 1000000000.)
                        .build()
        );
        if (mSensorData.size() >= mFlushSize) {
            flushAsync();
        }
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

    public int getTimeOffsetMillis() {
        return mTimeOffsetMillis;
    }

    public void setTimeOffsetMillis(int timeOffsetMillis) {
        this.mTimeOffsetMillis = timeOffsetMillis;
    }
}
