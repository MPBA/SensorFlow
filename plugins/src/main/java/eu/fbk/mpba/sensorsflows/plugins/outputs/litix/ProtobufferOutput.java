package eu.fbk.mpba.sensorsflows.plugins.outputs.litix;

import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import eu.fbk.mpba.sensorsflows.OutputPlugin;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;
import eu.fbk.mpba.sensorsflows.plugins.outputs.litix.Litix.SensorInfo;

public class ProtobufferOutput implements OutputPlugin<Long, double[]> {

    public static final String TS_PACKAGES =      "splits-buffered   ";
    public static final String TS_TOTAL_KB =      "data-raw      [KB]";
    public static final String TS_COMPRESSED_KB = "data-gzipped  [KB]";
    public static final String TS_COMPRESSED =    "data-gz-ratio  [%]";
    public static final String TS_PACKTIMEOUT =   "split-time-max [s]";
    private final SplitEvent mOnSplit;
    private Integer mSessionID;
    private Integer mTrackID;

    private class Queries {
        final static String i1 =
                "create table if not exists split (\n" +
                        " blob_id INTEGER,\n" +
                        " track_id INTEGER,\n" +
                        " uploaded INTEGER NOT NULL DEFAULT 0,\n" +
                        " data BLOB NOT NULL,\n" +
                        " foreign key (track_id) references track(track_id),\n" +
                        " primary key (blob_id, track_id)" +
                        ");";
        final static String i2 =
                "create table if not exists track (\n" +
                        " track_id INTEGER PRIMARY KEY,\n" +
                        " session_id INTEGER,\n" +              // Non usato
                        " name TEXT,\n" +
                        " committed INTEGER NOT NULL DEFAULT 0\n" +
                        ");";
        final static String t = "insert into track (track_id, session_id, name) values(?, ?, ?)";
        final static String s = "insert into split (blob_id, track_id, data) values(?, ?, ?)";
    }

    private final SplitterParams mSplitter;
    protected final SQLiteDatabase buffer;
    protected List<SensorInfo> mSensorInfo = new ArrayList<>();
    protected HashMap<ISensor, Integer> mReverseSensors = new HashMap<>();
    protected List<Litix.SensorData> mSensorData = new ArrayList<>();
    protected List<Litix.SensorEvent> mSensorEvent = new ArrayList<>();
    protected List<Litix.SessionMeta> mSessionMeta = new ArrayList<>();
    protected Object mSessionTag = "undefined";
    protected int splits = 0;
    private String mName;
    private long mForwardedBytes = 0;
    private long mReceivedBytes = 0;

    private TextStatusUpdater mUpd;

    public void setTextStatusUpdater(TextStatusUpdater upd) {
        upd.textStatusPut(TS_PACKAGES, splits);
        upd.textStatusPut(TS_TOTAL_KB, 0);
        upd.textStatusPut(TS_COMPRESSED_KB, 0);
        upd.textStatusPut(TS_COMPRESSED, 0);
        upd.textStatusPut(TS_PACKTIMEOUT, mSplitter.maxSplitTime / 1000.);
        this.mUpd = upd;
    }

    void textStatusPut(String k, Object v) {
        if (mUpd != null) {
            mUpd.textStatusPut(k, v);
        }
    }

    public static class SplitterParams {
        private final float targetCompressedSize;
        private final float maxSplitTime;
        private final float minSplitSize;
        private final float ratioBalance = .3f;
        private final float adjustBalance = .7f;
        private float compressionRatio;
        private float adjust = 1;
        private long lastSplitTime;
        private int size = 0;
        private boolean timeout = false;

        public SplitterParams(float targetCompressedSize, float maxSplitTime, float minSplitSize, float initialCompressionRatio) {
            this.targetCompressedSize = targetCompressedSize;
            this.minSplitSize = minSplitSize;
            this.maxSplitTime = maxSplitTime * 1000f;
            compressionRatio = initialCompressionRatio;
            lastSplitTime = SystemClock.elapsedRealtime();
        }

        public void updateSize(float compressed, float raw) {
            Log.d("ProtoOut", "Updating size" + (timeout ? " after timeout" : ""));
            if (!timeout)
                adjust *= (float) Math.pow(targetCompressedSize / compressed, adjustBalance);
            compressionRatio = Math.min(1.f, compressionRatio * (1 - ratioBalance) + ratioBalance * compressed / raw);
        }

        public float getFlushSize() {
            return targetCompressedSize * adjust / compressionRatio;
        }

        public boolean addAndPopFlushSuggested(int newSize) {
            size += newSize;
            if (size >= getFlushSize() || size >= minSplitSize && SystemClock.elapsedRealtime() - lastSplitTime > maxSplitTime) {
                size = 0;
                timeout = SystemClock.elapsedRealtime() - lastSplitTime > maxSplitTime;
                lastSplitTime = SystemClock.elapsedRealtime();
                return true;
            } else
                return false;
        }

        @Override
        public String toString() {
            return "- ratio=" + compressionRatio + " flushSize=" + getFlushSize() + " adjust=" + adjust;
        }
    }

    public interface SplitEvent {
        void newSplit(ProtobufferOutput sender, int id, SplitterParams params);

        void noMoreBuffers(ProtobufferOutput sender, SplitterParams params);
    }

    public ProtobufferOutput(String name, SQLiteDatabase database, SplitterParams params, @Nullable SplitEvent callback) {
        mName = name;
        buffer = database;
        mSplitter = params;
        mOnSplit = callback;

        // sync for db
        synchronized (buffer) {
            buffer.execSQL(Queries.i1);
            buffer.execSQL(Queries.i2);
        }
    }

    public long currentBacklogSize() {
        return mSensorData.size() + mSensorEvent.size() + mSessionMeta.size();
    }

    public void setLitixID(int session, int track) {
        if (finalized) {
            mTrackID = track;
            mSessionID = session;
        } else
            throw new NullPointerException("ProtobufferOutput already initialized.");
    }

    public void flushTrackSplit() {
        Log.d("ProtoOut", "Flushing " + currentBacklogSize() + " SensorData/Event");
        final int split_id = splits;

        Litix.TrackSplit.Builder sb = Litix.TrackSplit.newBuilder();
        sb.setTrackName(mSessionTag.toString());
        sb.addAllData(mSensorData);
        sb.addAllEvents(mSensorEvent);
        sb.addAllMeta(mSessionMeta);
        if (splits == 0)
            sb.addAllSensors(mSensorInfo);

        final Litix.TrackSplit ts = sb.build();

        textStatusPut(TS_PACKAGES, splits);

        mSensorData.clear();
        mSensorEvent.clear();
        mSessionMeta.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream compressed = new ByteArrayOutputStream();
                ByteArrayOutputStream raw = new ByteArrayOutputStream();
                try {
                    long debugTime = -System.nanoTime();
                    ts.writeTo(raw);
                    debugTime += System.nanoTime();

                    Log.d("ProtoOut", "Async serialized " + raw.size() / 1000f + "K in " + debugTime / 1000_000.0 + "ms");

                    debugTime = -System.nanoTime();
                    GZIPOutputStream zos = new GZIPOutputStream(compressed);
                    raw.writeTo(zos);
                    zos.close();
                    debugTime += System.nanoTime();

                    Log.d("ProtoOut", "Async compressed " + compressed.size() / 1000f + "K (ratio " + (100.0 * compressed.size() / raw.size()) + "%) in " + debugTime / 1000_000.0 + "ms");

                    mSplitter.updateSize(compressed.size(), raw.size());

                    Log.v("ProtoOut", "\n" + mSplitter.toString());

                    textStatusPut(TS_TOTAL_KB, (mReceivedBytes+=raw.size())/1000.);
                    textStatusPut(TS_COMPRESSED_KB, (mForwardedBytes+=compressed.size())/1000.);
                    textStatusPut(TS_COMPRESSED, Math.round((mForwardedBytes / (double) mReceivedBytes) * 100));

                    // sync for db
                    synchronized (buffer) {
                        SQLiteStatement s = buffer.compileStatement(Queries.s);
                        s.clearBindings();
                        s.bindLong(1, split_id);
                        s.bindLong(2, mTrackID);
                        s.bindBlob(3, compressed.toByteArray());
                        s.executeInsert();
                        s.close();
                    }

                    compressed.close();

                    splits++;

                    if (mOnSplit != null) {
                        mOnSplit.newSplit(ProtobufferOutput.this, split_id, mSplitter);
                        if (lastFlush)
                            mOnSplit.noMoreBuffers(ProtobufferOutput.this, mSplitter);
                    }
                } catch (SQLiteConstraintException e) {
                    Log.e("ProtoOut", "SQL error, splitID:" + split_id + " trackID:" + mTrackID);
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e("ProtoOut", "Flush error");
                    e.printStackTrace();
                }
            }
        }, "Flush").start();
    }

    // OutputPlugIn implementation

    @Override
    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {
        lastFlush = false;
        finalized = false;
        mReverseSensors = new HashMap<>(streamingSensors.size(), 1);
        mSessionTag = sessionTag;

        for (int s = 0; s < streamingSensors.size(); s++) {
            SensorInfo.Builder db = SensorInfo.newBuilder()
                    .setSensorId(s)
                    .setDevice(streamingSensors.get(s).getParentDevicePlugin().getClass().getName())
                    .setType(streamingSensors.get(s).getClass().getName())
                    .setName(streamingSensors.get(s).getName());
            for (Object x : streamingSensors.get(s).getValueDescriptor())
                db.addChannels(x.toString());
            mSensorInfo.add(db.build());
            mReverseSensors.put(streamingSensors.get(s), s);
        }

        if (mTrackID == null || mSessionID == null)
            mTrackID = mSessionID = 0;
        else {
            // sync for db
            synchronized (buffer) {
                SQLiteStatement
                        stmt = buffer.compileStatement(Queries.t);
                stmt.bindLong(1, mTrackID);
                stmt.bindLong(2, mSessionID);
                stmt.bindString(3, mSessionTag.toString());
                stmt.executeInsert();
            }
        }
    }

    private boolean finalized = true;
    private boolean lastFlush = false;

    @Override
    public void outputPluginFinalize() {
        lastFlush = true;
        flushTrackSplit();
        finalized = true;
    }

    @Override
    public void newSensorEvent(SensorEventEntry<Long> event) {
        mSensorEvent.add(Litix.SensorEvent.newBuilder()
                        .setTimestamp(event.timestamp)
                        .setCode(event.code)
                        .setMessage(event.message)
                        .setSensorId(mReverseSensors.get(event.sensor))
                        .build()
        );
        if (mSplitter.addAndPopFlushSuggested((Long.SIZE + Integer.SIZE + Character.SIZE * event.message.length()) / 8))
            flushTrackSplit();
    }

    // TODO Remove timestamp freedom degrees
    @Override
    public void newSensorData(SensorDataEntry<Long, double[]> data) {
        Double[] boxed = new Double[data.value.length];
        for (int i = 0; i < data.value.length; i++)
            boxed[i] = data.value[i];
        mSensorData.add(Litix.SensorData.newBuilder()
                        .setTimestamp(data.timestamp)
                        .addAllValue(Arrays.asList(boxed))
                        .setSensorId(mReverseSensors.get(data.sensor))
                        .build()
        );
        if (mSplitter.addAndPopFlushSuggested((Long.SIZE + Double.SIZE * data.value.length) / 8))
            flushTrackSplit();
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public void close() {
        if (!finalized)
            outputPluginFinalize();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
