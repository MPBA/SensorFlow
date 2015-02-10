package eu.fbk.mpba.sensorsflows.debugapp.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.fbk.mpba.sensorsflows.OutputPlugin;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;
import eu.fbk.mpba.sensorsflows.debugapp.util.SkiloProtobuffer;
import eu.fbk.mpba.sensorsflows.debugapp.util.SkiloProtobuffer.SensorInfo;

public class ProtobufferOutput implements OutputPlugin<Long, double[]> {

    protected File mFolder;
    protected long mSize;
    protected Object mSessionTag = "undefined";
    protected List<SensorInfo> mSensorInfo = new ArrayList<>();
    protected List<ISensor> mSensors = new ArrayList<>();
    protected final String uuid;
    protected List<SkiloProtobuffer.SensorData> sensorData = new ArrayList<>();
    protected Random r = new Random(500);
    private String mName;

    public ProtobufferOutput(String name, File dir, long flushSizeElements) { // TODO Horrible
        mName = name;
        mFolder = dir;
        mSize = flushSizeElements;
        uuid = "AleB.Test/" + Integer.toHexString(r.nextInt()) + "_" + Integer.toHexString(r.nextInt());
    }

    public long currentBacklogSize() {
        return -1; //mData.size() + mEvents.size();  TODO Add events and auto-flush support
    }

    public void flushTrackSplit(String fileName) {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(fileName, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        SkiloProtobuffer.TrackSplit s = SkiloProtobuffer.TrackSplit.newBuilder()
                .addAllInfo(mSensorInfo)
                .addAllDatas(sensorData)
                .setTrackId(0)
                .setIsLast(true)
                .setPhoneId(uuid)
                .setSequenceNumber(0) // TODO Sequence number
                .setTsStart(sensorData.get(0).getTimestamp())
                .setTsStop(sensorData.get(sensorData.size() - 1).getTimestamp())
                .build();

        try {
            s.writeTo(output);
            if (output != null)
                output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTrackSplitNameForNow() {
        return new File(mFolder, System.currentTimeMillis() + ".pb").getAbsolutePath();
    }

    // OutputPlugIn implementation

    @Override
    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {
        mSensors = streamingSensors;
        mSessionTag = sessionTag;
        mFolder = new File(mFolder, mSessionTag.toString() + "/" + toString());
        //noinspection ResultOfMethodCallIgnored
        mFolder.mkdirs();
        for (int s = 0; s < mSensors.size(); s++) {
            mSensorInfo.add(SensorInfo.newBuilder()
                    .setSensorId(s)
                    .setDesc("data_" + mSensors.get(s).toString())
                    .setType(SensorInfo.TYPESENSOR.OTHER)
                    .build());
//            mSensorInfo.add(SensorInfo.newBuilder()
//                    .setSensorId(s)
//                    .setDesc("events_" + mSensors.get(s).toString())
//                    .setType(SensorInfo.TYPESENSOR.OTHER)
//                    .build()); TODO Add events support
        }
    }

    @Override
    public void outputPluginFinalize() {
        flushTrackSplit(getTrackSplitNameForNow());
    }

    @Override
    public void newSensorEvent(SensorEventEntry<Long> event) {
        // mEvents.add(event); TODO Add events support
    }

    @Override
    public void newSensorData(SensorDataEntry<Long, double[]> data) { // TODO Add auto-flush support
        List<Float> v = new ArrayList<>(7);
        for (int i = 0; i < data.value.length; i++)    // double conversion to Float
            v.add((float)data.value[i]);               // double conversion to Float

        sensorData.add(SkiloProtobuffer.SensorData.newBuilder()
                        .setId(-1)
                        .setSensorIdFk(mSensors.indexOf(data.sensor))
                        .addAllValue(v)
                        .setTimestamp(System.currentTimeMillis()) // TODO Set to a monotonic centralized ts
                        .build()
        );
    }

    @Override
    public String toString() {
        return ProtobufferOutput.class.getSimpleName() + "-" + mName;
    }
}
