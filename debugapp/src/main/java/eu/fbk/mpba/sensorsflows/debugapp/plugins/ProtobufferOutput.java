package eu.fbk.mpba.sensorsflows.debugapp.plugins;

import java.io.File;
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
    protected ArrayList<SensorDataEntry<Long, double[]>> mData;
    protected ArrayList<SensorEventEntry<Long>> mEvents;
    protected final String uuid;
    private int tsSeq = 0;

    public ProtobufferOutput(File dir, long flushSizeElements) { // TODO Horrible
        mFolder = dir;
        mSize = flushSizeElements;
        Random r = new Random(500);
        uuid = "AleB.Test/" + Integer.toHexString(r.nextInt()) + "_" + Integer.toHexString(r.nextInt());
    }

    public long currentBacklogSize() {
        return mData.size() + mEvents.size();
    }

    public void flushTrackSplit(String fileName) {
        List<SkiloProtobuffer.SensorData> l = new ArrayList<>();
        for (SensorDataEntry<Long, double[]> d : mData){

            List<Float> v = new ArrayList<>();          // double conversion to Float
            for (int i = 0; i < d.value.length; i++)    // double conversion to Float
                v.add((float)d.value[i]);               // double conversion to Float

            l.add(SkiloProtobuffer.SensorData.newBuilder()
                            .setId(-1)
                            .setSensorIdFk(mSensors.indexOf(d.sensor))
                            .addAllValue(v)
                            .build()
            );
        }

        SkiloProtobuffer.TrackSplit s = SkiloProtobuffer.TrackSplit.newBuilder()
                .addAllInfo(mSensorInfo)
                .addAllDatas(l)
                .setIsLast(false)
                .setPhoneId(uuid)
                .setSequenceNumber(tsSeq++)
                .setTsStart(Math.max(mData.get(0).time, mEvents.get(0).timestamp))
                .build();
    }

    public String getTrackSplitNameForNow() {
        return mSessionTag.toString() + "/" + System.currentTimeMillis() + ".pb";
    }

    // OutputPlugIn implementation

    @Override
    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {
        mSessionTag = sessionTag;
        mSensors = streamingSensors;
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
//                    .build());
        }
    }

    @Override
    public void outputPluginFinalize() {
        flushTrackSplit(getTrackSplitNameForNow());
    }

    @Override
    public void newSensorEvent(SensorEventEntry<Long> event) {
        mEvents.add(event);
    }

    @Override
    public void newSensorData(SensorDataEntry<Long, double[]> data) {
        mData.add(data);
    }
}
