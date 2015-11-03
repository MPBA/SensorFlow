package eu.fbk.mpba.sensorsflows.plugins.litixcommanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import eu.fbk.mpba.litixcom.core.LitixCom;
import eu.fbk.mpba.litixcom.core.Track;
import eu.fbk.mpba.litixcom.core.exceptions.ConnectionException;
import eu.fbk.mpba.litixcom.core.exceptions.DeadServerDatabaseException;
import eu.fbk.mpba.litixcom.core.exceptions.InternalException;
import eu.fbk.mpba.litixcom.core.exceptions.LoginCancelledException;
import eu.fbk.mpba.litixcom.core.exceptions.MurphySyndromeException;
import eu.fbk.mpba.litixcom.core.exceptions.SecurityException;
import eu.fbk.mpba.litixcom.core.exceptions.TooManyUsersOnServerException;
import eu.fbk.mpba.litixcom.core.mgrs.auth.Certificati;
import eu.fbk.mpba.litixcom.core.mgrs.auth.Credenziali;
import eu.fbk.mpba.litixcom.core.mgrs.messages.Sessione;

public class LitixComManager {
    private SQLiteDatabase buffer = null;
    private final Thread th;
    private Track track;
    private List<Track> tracks;
    protected LitixCom com;

    public LitixComManager(final Activity activity, InetSocketAddress address, Certificati c) {
        com = new LitixCom(address, new Credenziali() {

            public String getDeviceId() {
                return getXDID(activity);
            }

            @Override
            public void setToken(String token) {
                File d = activity.getDir(LitixComManager.class.getSimpleName(), Context.MODE_PRIVATE);
                File t = new File(d, "halo_memory");
                FileOutputStream o = null;
                try {
                    o = new FileOutputStream(t);
                    o.write(token.getBytes());
                } catch (FileNotFoundException e) {
                    Log.e(LitixComManager.class.getSimpleName(), "Cannot create a private file.");
                } catch (IOException e) {
                    Log.e(LitixComManager.class.getSimpleName(), "Cannot write a private file.");
                }
                finally {
                    if (o != null)
                        try {
                            o.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }

            @Override
            public String getToken() {
                File d = activity.getDir(LitixComManager.class.getSimpleName(), Context.MODE_PRIVATE);
                File t = new File(d, "halo_memory");
                StringBuilder token = new StringBuilder();
                FileInputStream i = null;
                try {
                    i = new FileInputStream(t);
                    byte[] buf = new byte[512];
                    int n;
                    while ((n = i.read(buf, 0, 512)) > 0)
                        token.append(new String(buf, 0, n));
                } catch (FileNotFoundException e) {
                    Log.d(LitixComManager.class.getSimpleName(), "No token private file.");
                } catch (IOException e) {
                    Log.e(LitixComManager.class.getSimpleName(), "Cannot read a private file.");
                }
                finally {
                    if (i != null)
                        try {
                            i.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
                return token.toString();
            }

            @Override
            public Triple getUsernamePasswordDeviceId() {
                final Semaphore semaphore = new Semaphore(0);
                final String[] name = new String[1];
                final String[] surname = new String[1];
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        InputDialog.makeLogIn(activity, new InputDialog.ResultCallback<Pair<String, String>>() {
                            @Override
                            public void ok(Pair<String, String> result) {
                                name[0] = result.first;
                                surname[0] = result.second;
                                semaphore.release();
                            }

                            @Override
                            public void cancel() {
                                semaphore.release();
                            }
                        }, "Physiolitix - Login", "Sign in", "Cancel").show();
                    }
                });
                try {
                    semaphore.acquire();
                    String address = getDeviceId() + "-" + activity.getApplicationInfo().packageName;
                    return new Triple(name[0], surname[0], address);
                } catch (InterruptedException e) {
                    return null;
                }
            }

            @Override
            public boolean onWrongLoginGetRetry() {
                final AtomicReference<Boolean> r = new AtomicReference<>(false);
                final Semaphore semaphore = new Semaphore(0);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(activity)
                                .setTitle("Phisiolitix - Login")
                                .setMessage("Login error, wrong name or password.")
                                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        r.set(true);
                                        semaphore.release();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        semaphore.release();
                                    }
                                }).show();

                    }
                });
                try {
                    semaphore.acquire();
                } catch (InterruptedException e1) {
                    return false;
                }
                return r.get();
            }

        }, c);
        th = new Thread(new Runnable() {
            @Override
            public void run() {
                processSplitsQueue(queue);
            }
        }, "LitixCom uploader");
        th.setDaemon(true);
        th.start();
    }

    public int newTrack(Sessione s) throws ConnectionException, DeadServerDatabaseException, InternalException, MurphySyndromeException, eu.fbk.mpba.litixcom.core.exceptions.SecurityException, TooManyUsersOnServerException, LoginCancelledException {
        if (track == null) {
            track = com.newTrack(s);
            return track.getTrackId();
        } else
            throw new NullPointerException("Already set track.");
    }

    public void enqueueUncommittedTracks() throws LoginCancelledException, ConnectionException, InternalException, MurphySyndromeException, SecurityException, TooManyUsersOnServerException, DeadServerDatabaseException {
        if (tracks == null) {
            Cursor x = buffer.query(true, "track", new String[]{"track_id"}, "status == \"pending\"", null, null, null, null, null);
            List<Integer> trackIds = new ArrayList<>();
            x.moveToFirst();
            while (!x.isAfterLast()) {
                trackIds.add(x.getInt(0));
                x.moveToNext();
            }
            x.close();
            tracks = new ArrayList<>();
            for (Integer i : trackIds) {
                Track t = com.continueTrack(i);
                if (t.getNextBlobIdOnContinue() != null) {
                    tracks.add(t);
                    List<Long> splits = new ArrayList<>();
                    Cursor y = buffer.query(true, "split", new String[]{"first_ts"}, "start_ts ==  AND status == \"pending\"", null, null, null, null, null);
                    // TODO ommerda
                    // Variare da timestamping identification a remote identification (split_id = blob id, start_ts = track_id)
                    // Questo if è da finire
                    // Modificare tutto
                    queue.put();
                }
                else
                    Log.i("continueTracks", "DB contains a track that is not registered id:" + t.getTrackId());
            }

        }
    }

    final BlockingQueue<Pair<Track, Long>> queue = new LinkedBlockingQueue<>();

    public void notifySplit(Long id) {
        if (!commitPending) {
            try {
                queue.put(new Pair<>(track, id));
                Log.v("notifySplit", "added " + id);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("notifySplit", "error " + id);
            }
        } else
            throw new NullPointerException("Track already committed.");
    }

    private byte[] loadSplit(Track track, long split) {
        byte[] s = null;
        Cursor x = buffer.query("split", new String[]{"data"}, "track_id == ? AND first_ts == ?", new String[] { "" + track.getTrackId(), "" + split }, null, null, null);
        if (x.moveToLast()) {
            s = x.getBlob(0);
        } else {
            Log.e("DBReader", "Split ID not found in database!");
        }
        x.close();
        return s;
    }

    private void processSplitsQueue(BlockingQueue<Pair<Track, Long>> queue) {
        Log.d("Man", Thread.currentThread().getName());
        try {
            Track trackInfo;
            Long splitId;
            byte[] split;
            boolean pending = true;
            while (pending) {
                Log.d("Man", "Waiting for queue");

                Pair<Track, Long> entry = queue.take();

                trackInfo = entry.first;
                splitId = entry.second;
                split = null;

                while (true) {
                    try {
                        if (splitId == null) {
                            Log.d("Man", "Committing (null id is a command)");

                            trackInfo.commit();
                            buffer.execSQL("UPDATE track SET status = \"committed\" WHERE track_id = ?", new Object[]{trackInfo.getTrackId()});

                            Log.d("Man", "Committed");
                            pending = false;
                        } else {
                            Log.d("Man", "Pushing");

                            if (split == null)
                                split = loadSplit(trackInfo, splitId);

                            trackInfo.put(split);
                            buffer.execSQL("UPDATE split SET status = \"uploaded\" WHERE track_id = ?", new Object[]{trackInfo.getTrackId()});

                            Log.d("Man", "Pushed");
                        }
                        break;
                    } catch (ConnectionException | LoginCancelledException | InternalException e) {
                        Log.d("Man", "Push failed L1, performing no take, sleeping 13370ms", e);
                        Thread.sleep(13370);
                    } catch (DeadServerDatabaseException | TooManyUsersOnServerException | MurphySyndromeException e) {
                        Log.d("Man", String.format("Push failed L2, %s, performing no take, sleeping 1337000/2ms\n%s", e.getClass().getName(), e.getMessage()));
                        Thread.sleep(1337000 / 2);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (InterruptedException ignored) {
            Log.d("Man", Thread.currentThread().getName() + " interrupted");
        }
        Log.d("Man", "Exiting processSplitsQueue");
    }

    public List<Sessione> getAuthorizedSessions() throws ConnectionException, LoginCancelledException, SecurityException, InternalException, TooManyUsersOnServerException, DeadServerDatabaseException {
        return com.getSessionsList();
    }

    /**
     * This only for the foreground track
     */
    private boolean commitPending = false;

    public void enqueueCommit() {
        // this only for insertion
        commitPending = true;
        // null id is the command
        queue.add(new Pair<Track, Long>(track, null));
    }

    public void close() {
        th.interrupt();
    }

    @NonNull
    private String getXDID(Context context) {
        String a = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        if (a == null)
            a = "a-" + Settings.Secure.ANDROID_ID;
        else
            a = "u-" + a;
        return a;
    }

    public void setBufferOnce(SQLiteDatabase buffer) {
        if (this.buffer == null)
            this.buffer = buffer;
        else
            throw new RuntimeException("Already set buffer!");
    }
}
