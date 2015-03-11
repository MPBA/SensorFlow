package eu.fbk.mpba.sensorsflows.debugapp.plugins.outputs;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.UUID;

import eu.fbk.mpba.sensorsflows.OutputPlugin;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;
import eu.fbk.mpba.sensorsflows.debugapp.util.SkiloProtobuffer;
import eu.fbk.mpba.sensorsflows.debugapp.util.SkiloProtobuffer.SensorInfo;

public class ProtobufferOutput implements OutputPlugin<Long, double[]> {

    protected File mFolder;
    protected long mFlushSize;
    protected Object mSessionTag = "undefined";
    protected List<SensorInfo> mSensorInfo = new ArrayList<>();
    protected List<ISensor> mSensors = new ArrayList<>();
    protected List<SkiloProtobuffer.SensorData> mSensorData = new ArrayList<>();
    protected final String uuid;
    private String mName;
    private UUID uid;
    private Dictionary<Class, SensorInfo.TYPESENSOR> mTypesMap;

    public ProtobufferOutput(String name, File dir, long flushSizeElements, String phoneId, Dictionary<Class, SensorInfo.TYPESENSOR> sensorTypesMap) { // TODO Horrible
        mName = name;
        mFolder = dir;
        mFlushSize = flushSizeElements;
        uuid = phoneId;
        uid = UUID.randomUUID();
        mTypesMap = sensorTypesMap;
    }

    public long currentBacklogSize() {
        return mSensorData.size();
    }

    private int seqNumber = 0;

    public void flushTrackSplit(List<SkiloProtobuffer.SensorData> x, String fileName, boolean last) {
        Log.d("ProtoOut", "Flushing " + x.size() + " SensorData");
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(fileName, false);
        } catch (FileNotFoundException e) {
            Log.e("ProtoOut", "Flush can't open the file");
            e.printStackTrace();
        }
        SkiloProtobuffer.TrackSplit s = SkiloProtobuffer.TrackSplit.newBuilder()
                .addAllInfo(mSensorInfo)
                .addAllDatas(x)
                .setTrackUid(uid.toString())
                .setIsLast(last)
                .setPhoneId(uuid)
                .setSequenceNumber(seqNumber++)
                .setTsStart(x.get(0).getTimestamp())
                .setTsStop(x.get(x.size() - 1).getTimestamp())
                .build();

        try {
            s.writeTo(output);
            if (output != null)
                output.close();
        } catch (IOException e) {
            Log.e("ProtoOut", "Flush can't write or close the file");
            e.printStackTrace();
        }
        int xl = x.size();
        x.clear();
        Log.v("ProtoOut", "Flush cleared list of size:" + xl);
    }

    public String getTrackSplitNameForNow() {
        return new File(mFolder, System.currentTimeMillis() + ".pb").getAbsolutePath();
    }

    // OutputPlugIn implementation

    String join(List<Object> x) {
        StringBuilder r = new StringBuilder();
        if (x.size() > 0) {
            r.append(x.get(0).toString());
            for (int i = 1; i < x.size(); i++)
                r.append(';').append(x.get(i));
        }
        return r.toString();
    }

    @Override
    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {
        mSensors = streamingSensors;
        mSessionTag = sessionTag;
        mFolder = new File(mFolder, mSessionTag.toString() + "/" + toString());
        //noinspection ResultOfMethodCallIgnored
        mFolder.mkdirs();
        for (int s = 0; s < mSensors.size(); s++) {
            SkiloProtobuffer.SensorInfo.TYPESENSOR type = mTypesMap.get(mSensors.get(s).getClass());
            mSensorInfo.add(SensorInfo.newBuilder()
                    .setSensorId(s)
                    .setDesc("data_" + mSensors.get(s).toString())
                    .setType(type == null ? SensorInfo.TYPESENSOR.OTHER : type)
                    .setMeta(join(mSensors.get(s).getValuesDescriptors()))
                    .build());
            mSensorInfo.add(SensorInfo.newBuilder()
                    .setSensorId(mSensors.size() + s)
                    .setDesc("events_" + mSensors.get(s).toString())
                    .setType(type == null ? SensorInfo.TYPESENSOR.OTHER : type)
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
        mSensorData.add(SkiloProtobuffer.SensorData.newBuilder()
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
        List<Double> v = new ArrayList<>(7);
        for (int i = 0; i < data.value.length; i++)    // double conversion to Float
            v.add(data.value[i]);                      // double conversion to Float

        mSensorData.add(SkiloProtobuffer.SensorData.newBuilder()
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

    private void flushAsync() {
        final List<SkiloProtobuffer.SensorData> x = mSensorData;
        mSensorData = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                flushTrackSplit(x, getTrackSplitNameForNow(), false);
            }
        }, "Flush " + x.size() + " from "  + x.get(0).getTimestamp()).start();
    }

    @Override
    public String toString() {
        return ProtobufferOutput.class.getSimpleName() + "-" + mName;
    }
}
