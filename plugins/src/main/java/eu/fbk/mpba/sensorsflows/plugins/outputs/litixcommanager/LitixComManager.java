package eu.fbk.mpba.sensorsflows.plugins.outputs.litixcommanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import eu.fbk.mpba.litixcom.core.LitixCom;
import eu.fbk.mpba.litixcom.core.eccezioni.ConnectionException;
import eu.fbk.mpba.litixcom.core.eccezioni.LoginException;
import eu.fbk.mpba.litixcom.core.mgrs.auth.Credenziali;

public class LitixComManager {
    protected LitixCom com;

    public LitixComManager(final Context context, final String xdid, InetSocketAddress address) {
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
                }, "Physiolitix - Login", "Master's username");
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
                }, "Physiolitix - Login", String.format("Password for %s", username.get()));
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
                return xdid;
            }
        });
    }

    public class Session {
        public final Integer ID;
        Session(Integer id) {
            ID = id;
        }
    }

    public List<Session> getAuthorizedSessions() {
        try {
            List<Integer> s = com.getSessionsList();
            List<Session> r = new ArrayList<>(s.size());
            for (Integer i : s)
                r.add(new Session(i));
            return r;
        } catch (ConnectionException | LoginException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
