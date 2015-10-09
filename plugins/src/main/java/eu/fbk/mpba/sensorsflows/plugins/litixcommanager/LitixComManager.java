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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import eu.fbk.mpba.litixcom.core.LitixCom;
import eu.fbk.mpba.litixcom.core.Track;
import eu.fbk.mpba.litixcom.core.eccezioni.ConnectionException;
import eu.fbk.mpba.litixcom.core.eccezioni.LoginException;
import eu.fbk.mpba.litixcom.core.mgrs.auth.Credenziali;

public class LitixComManager {
    private final SQLiteDatabase buffer;
    private final Thread th;
    protected LitixCom com;

    public class Session {
        private final Integer ID;
        public int getSessionID() {
            return ID;
        }
        Session(Integer id) {
            ID = id;
        }
    }

    private Track track;

    public int newTrack(Session s) throws ConnectionException, LoginException {
        this.track = com.newTrack(s.ID);
        return track.getTrackId();
    }

    final Queue<Integer> queue = new ArrayDeque<>();
    final Semaphore queueSemaphore = new Semaphore(0);
    byte[] lastSplit = null;

    public synchronized void notifySplit(int id) {
        if (!commitPending) {
            queue.add(id);
            queueSemaphore.release();
        }
        else
            throw new NullPointerException("Track already committed.");
    }

    public byte[] loadSplit() {
        if (lastSplit == null) {
            Cursor x = buffer.query("split", new String[]{"data"}, "first_ts == " + queue.peek(), null, null, null, null);
            if (x.getCount() == 0)
                Log.e("DBReader", "Split ID not found in database!");
            else
                lastSplit = x.getBlob(0);
            x.close();
        }
        return lastSplit;
    }

    private synchronized void processSplitsQueue() {
        try {
            while (true) {
                try {
                    queueSemaphore.acquire();
                    track.put(loadSplit());

                    queue.remove();
                    // Uploaded, cleaning cache
                    lastSplit = null;

                    if (readyToClose()) {
                        track.commit();
                        break;
                    }
                } catch (ConnectionException | LoginException ignored) {
                    queueSemaphore.release();
                    Thread.sleep(5000);
                }
            }
        } catch (InterruptedException ignored) { }
    }

    public LitixComManager(final Activity context, InetSocketAddress address, SQLiteDatabase database) {
        buffer = database;
        com = new LitixCom(address, new Credenziali() {

            final AtomicReference<String> username = new AtomicReference<>(null);

            @Override
            public String getUsername() {
                final Semaphore semaphore = new Semaphore(0);
                InputDialog.makeText(context, new InputDialog.ResultCallback<String>() {
                    @Override
                    public void ok(String result) {
                        username.set(result);
                        semaphore.release();
                    }

                    @Override
                    public void cancel() {
                        semaphore.release();
                    }
                }, "Physiolitix - Login", "Master's username").show();
                try {
                    semaphore.acquire();
                    return username.get();
                } catch (InterruptedException e) {
                    return null;
                }
            }

            @Override
            public String getPassword() {
                final Semaphore semaphore = new Semaphore(0);
                final AtomicReference<String> password = new AtomicReference<>(null);
                InputDialog.makePassword(context, new InputDialog.ResultCallback<String>() {
                    @Override
                    public void ok(String result) {
                        password.set(result);
                        semaphore.release();
                    }

                    @Override
                    public void cancel() {
                        semaphore.release();
                    }
                },
                "Physiolitix - Login", String.format("Password for %s", username.get())).show();
                try {
                    semaphore.acquire();
                    return password.get();
                } catch (InterruptedException e) {
                    return null;
                }
            }

            @Override
            public void setToken(String token) {
                File d = context.getDir(LitixComManager.class.getSimpleName(), Context.MODE_PRIVATE);
                File t = new File(d, "halo_memory");
                try {
                    FileOutputStream o = new FileOutputStream(t);
                    o.write(token.getBytes());
                } catch (FileNotFoundException e) {
                    Log.e(LitixComManager.class.getSimpleName(), "Cannot create a private file.");
                } catch (IOException e) {
                    Log.e(LitixComManager.class.getSimpleName(), "Cannot write a private file.");
                }
            }

            @Override
            public String getToken() {
                File d = context.getDir(LitixComManager.class.getSimpleName(), Context.MODE_PRIVATE);
                File t = new File(d, "halo_memory");
                String token = null;
                try {
                    FileInputStream o = new FileInputStream(t);
                    byte[] buf = new byte[512];
                    if (o.read(buf,0,512) > 0)
                        token = new String(buf);
                } catch (FileNotFoundException ignore) {
                } catch (IOException e) {
                    Log.e(LitixComManager.class.getSimpleName(), "Cannot read a private file.");
                }
                return token;
            }

            @Override
            public boolean onErrorGetRetry(Exception e) {
                final AtomicReference<Boolean> r = new AtomicReference<>(false);
                final Semaphore semaphore = new Semaphore(0);
                new AlertDialog.Builder(context)
                        .setTitle("Phisiolitix - Login")
                        .setMessage(e == null ?
                                "Login error, wrong name or password." :
                                String.format("Login error.\n(Error: %s)", e.getMessage()))
                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                r.set(true);
                                semaphore.release();
                            }
                        })
                        .setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                semaphore.release();
                            }
                        }).show();
                try {
                    semaphore.acquire();
                } catch (InterruptedException e1) {
                    return false;
                }
                return r.get();
            }

            @Override
            public String getDeviceId() {
                return getXDID(context);
            }
        });
        th = new Thread(new Runnable() {
            @Override
            public void run() {
                processSplitsQueue();
            }
        }, "LitixCom uploader");
        th.setDaemon(true);
        th.start();
    }

    public List<Session> getAuthorizedSessions() throws ConnectionException, LoginException {
        List<Integer> s = com.getSessionsList();
        List<Session> r = new ArrayList<>(s.size());
        for (Integer i : s)
            r.add(new Session(i));
        return r;
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

    public synchronized int queuedUploads() {
        return queue.size();
    }

    private boolean commitPending = false;

    public void enqueueCommit() {
        commitPending = true;
    }

    public synchronized boolean readyToClose() {
        return queue.size() == 0 && commitPending;
    }

    public boolean close() {
        if (readyToClose()) {
            th.interrupt();
            buffer.close();
            return true;
        } else
            return false;
    }
}
