package eu.fbk.mpba.sensorsflows.plugins.outputs.litix;

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
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import eu.fbk.mpba.sensorsflows.OutputPlugin;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;
import eu.fbk.mpba.sensorsflows.plugins.outputs.litix.Litix.SensorInfo;

public class ProtobufferOutput implements OutputPlugin<Long, double[]> {

    public static final String TS_PACKAGES =      "Packets         ";
    public static final String TS_TOTAL_KB =      "Total       [KB]";
    public static final String TS_COMPRESSED_KB = "Compressed  [KB]";
    public static final String TS_COMPRESSED =    "Compressed   [%]";
    public static final String TS_PACKTIMEOUT =   "Max buf time [s]";
    private final SplitEvent mOnSplit;
    private long mSessionID;
    private long mTrackID;

    private class Queries {
        final static String i1 =
                "create table if not exists split (\n" +
                        " first_ts INTEGER PRIMARY KEY,\n" +
                        " start_ts INTEGER,\n" +
                        " status TEXT check(status in (\"local\", \"uploaded\")) NOT NULL DEFAULT \"local\",\n" +
                        " data BLOB NOT NULL,\n" +
                        " foreign key (start_ts) references track(start_ts)\n" +
                        ");";
        final static String i2 =
                "create table if not exists track (\n" +
                        " start_ts INTEGER check(start_ts > 1444444444) PRIMARY KEY,\n" +
                        " session_id INTEGER,\n" +
                        " track_id INTEGER,\n" +
                        " name TEXT,\n" +
                        " status TEXT check(status in (\"local\", \"pending\", \"committed\")) NOT NULL DEFAULT \"local\",\n" +
                        ");";
        final static String t = "insert into track (start_ts, session_id, track_id, name) values(?, ?, ?, ?)";
        final static String s = "insert into split (first_ts, start_ts, data) values(?, ?, ?)";
    }

    private final SplitterParams mSplitter;
    protected SQLiteDatabase buffer;
    protected List<SensorInfo> mSensorInfo = new ArrayList<>();
    protected HashMap<ISensor, Integer> mReverseSensors = new HashMap<>();
    protected List<Litix.SensorData> mSensorData = new ArrayList<>();
    protected List<Litix.SensorEvent> mSensorEvent = new ArrayList<>();
    protected List<Litix.SessionMeta> mSessionMeta = new ArrayList<>();
    protected Object mSessionTag = "undefined";
    protected long mTrackStart = 0;
    protected int splits = 0;
    private String mName;
    private int mReceived = 0;
    private int mForwarded = 0;
    private long mForwardedBytes = 0;
    private long mReceivedBytes = 0;

    private TextStatusUpdater mUpd;
    HashMap<String, Object> tsParams = new HashMap<>();
    long tsLastUpd = 0;

    public interface TextStatusUpdater {
        void updateTextStatus(String text);
    }

    public void setTextStatusUpdater(TextStatusUpdater upd) {
        this.mUpd = upd;
    }

    void textStatusPut(String k, Object v) {
        if (mUpd != null) {
            Object x = tsParams.put(k, v);
            if (x != null &&  x != v)
                if (SystemClock.elapsedRealtime() - tsLastUpd > 33) {
                    tsLastUpd = SystemClock.elapsedRealtime();
                    StringBuilder text = new StringBuilder();
                    for (Map.Entry<String, Object> e : tsParams.entrySet())
                        text
                                .append(e.getKey())
                                .append(": \t")
                                .append(e.getValue())
                                .append('\n');
                    mUpd.updateTextStatus(text.toString());
                }
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
            return "-\nratio=" + compressionRatio + "\nflushSize=" + getFlushSize() + "\nadjust=" + adjust;
        }
    }

    public interface SplitEvent {
        void newSplit(ProtobufferOutput sender, Long id, SplitterParams params);

        void noMoreBuffers(ProtobufferOutput sender, SplitterParams params);
    }

    public ProtobufferOutput(String name, SQLiteDatabase database, SplitterParams params, @Nullable SplitEvent callback) {
        mName = name;
        buffer = database;
        mSplitter = params;
        mOnSplit = callback;

        //noinspection ResultOfMethodCallIgnored
//        mDatabaseFile.mkdirs();
//        buffer = SQLiteDatabase.openOrCreateDatabase(mDatabaseFile, null);
        buffer.execSQL(Queries.i1);
        buffer.execSQL(Queries.i2);
    }

    public long currentBacklogSize() {
        return mSensorData.size() + mSensorEvent.size() + mSessionMeta.size();
    }

    public void setLitixID(long session, long track) {
        if (finalized) {
            mTrackID = track;
            mSessionID = session;
        } else
            throw new NullPointerException("ProtobufferOutput already initialized.");
    }

    public void flushTrackSplit() {
        Log.d("ProtoOut", "Flushing " + currentBacklogSize() + " SensorData/Event/Meta");
        final Long split_id = getMonoTimeMillis();

        Litix.TrackSplit.Builder sb = Litix.TrackSplit.newBuilder();
        sb.setTrackName(mSessionTag.toString());
        sb.addAllData(mSensorData);
        sb.addAllEvents(mSensorEvent);
        sb.addAllMeta(mSessionMeta);
        if (splits == 0)
            sb.addAllSensors(mSensorInfo);

        final Litix.TrackSplit ts = sb.build();
        final long bks = currentBacklogSize();

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
                    textStatusPut(TS_COMPRESSED, Math.round((1.-mForwardedBytes/(double)mReceivedBytes)*100));

                    SQLiteStatement s = buffer.compileStatement(Queries.s);
                    s.clearBindings();
                    s.bindLong(1, split_id);
                    s.bindLong(2, mTrackStart);
                    s.bindBlob(3, compressed.toByteArray());
                    s.executeInsert();

                    compressed.close();

                    splits++;
                    ProtobufferOutput.this.mForwarded += bks;

                    if (mOnSplit != null) {
                        mOnSplit.newSplit(ProtobufferOutput.this, split_id, mSplitter);
                        if (lastFlush)
                            mOnSplit.noMoreBuffers(ProtobufferOutput.this, mSplitter);
                    }
                } catch (Exception e) {
                    Log.e("ProtoOut", "Flush error");
                    e.printStackTrace();
                }
            }
        }, "Flush").start();
    }

    private long bootUTCNanos = System.currentTimeMillis() * 1_000_000L - System.nanoTime();

    private long getMonoTimeMillis() {
        return (System.nanoTime() + bootUTCNanos) / 1_000_000L;
    }

    // OutputPlugIn implementation

    @Override
    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {
        lastFlush = false;
        finalized = false;
        mReverseSensors = new HashMap<>(streamingSensors.size(), 1);
        mSessionTag = sessionTag;
        mTrackStart = getMonoTimeMillis();

        for (int s = 0; s < streamingSensors.size(); s++) {
            SensorInfo.Builder db = SensorInfo.newBuilder()
                    .setSensorId(s)
                    .setDevice(streamingSensors.get(s).getParentDevicePlugin().getClass().getName())
                    .setType(getClass().getName())
                    .setName(streamingSensors.get(s).getName());
            for (Object x : streamingSensors.get(s).getValueDescriptor())
                db.addChannels(x.toString());
            mSensorInfo.add(db.build());
            mReverseSensors.put(streamingSensors.get(s), s);
        }

        SQLiteStatement stmt = buffer.compileStatement(Queries.t);
        stmt.bindLong(1, mTrackStart);
        stmt.bindLong(2, mSessionID);
        stmt.bindLong(3, mTrackID);
        stmt.bindString(4, mSessionTag.toString());
        stmt.executeInsert();


        textStatusPut(TS_PACKAGES, splits);
        textStatusPut(TS_TOTAL_KB, 0);
        textStatusPut(TS_COMPRESSED_KB, 0);
        textStatusPut(TS_COMPRESSED, 0);
        textStatusPut(TS_PACKTIMEOUT, mSplitter.maxSplitTime / 1000.);
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
        mReceived++;
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
        mReceived++;
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

    @Override
    public int getReceivedMessagesCount() {
        return mReceived;
    }

    @Override
    public int getForwardedMessagesCount() {
        return mForwarded;
    }
}
