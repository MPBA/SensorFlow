package eu.fbk.mpba.sensorsflows.plugins.litixcommanager;

import android.util.Log;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.litixcom.core.LitixCom;
import eu.fbk.mpba.litixcom.core.Track;
import eu.fbk.mpba.litixcom.core.eccezioni.ConnectionException;
import eu.fbk.mpba.litixcom.core.eccezioni.LoginException;
import eu.fbk.mpba.litixcom.core.mgrs.auth.Credenziali;
import eu.fbk.mpba.litixcom.core.mgrs.connection.Certificati;
import eu.fbk.mpba.litixcom.core.mgrs.messages.Sessione;

public class LitixComWrapper {
    private static boolean simulation = true;
    private static int faultInjection = 0;

    private LitixCom instance = null;

    public LitixComWrapper(InetSocketAddress endpoint, Credenziali crendenziali) {
        this(endpoint, crendenziali, null);
    }

    public LitixComWrapper(InetSocketAddress endpoint, Credenziali crendenziali, Certificati certificati) {
        if (!simulation)
            instance = new LitixCom(endpoint, crendenziali, certificati);
        else
            Log.i("LitixComWrapper", "Simulation: ctor");
    }

    public List<Sessione> getSessionsList() throws ConnectionException, LoginException {
        if (!simulation)
            return instance.getSessionsList();
        else {
            Log.i("LitixComWrapper", "Simulation: sessions");
            return Arrays.asList(new Sessione(-1005, "Test 1"), new Sessione(-1006, "Test 2"), new Sessione(-1007, "Test 3"));
        }
    }

    public Track newTrack(Sessione sessione) throws ConnectionException, LoginException {
        if (!simulation)
            return instance.newTrack(sessione);
        else {
            Log.i("LitixComWrapper", "Simulation: newTrack");
            return new FakeTrack(sessione);
        }
    }

    public void InvalidateToken() throws ConnectionException, LoginException {
        if (!simulation)
            instance.InvalidateToken();
        else {
            Log.i("LitixComWrapper", "Simulation: InvalidateToken");
        }
    }

    public static class FakeTrack extends Track {

        private static int tid = 0;
        public final int trackID = tid++;
        public final Sessione sessione;
        private boolean comm = false;

        FakeTrack(Sessione session) throws ConnectionException, LoginException {
            this.sessione = session;
        }

        @Override
        public int getSessionId() {
            Log.i("LitixComWrapper", "Simulation: FakeTrack.getSessionId");
            return sessione.id;
        }

        @Override
        public int getTrackId() {
            Log.i("LitixComWrapper", "Simulation: FakeTrack.getTrackId");
            return trackID;
        }

        @Override
        public void put(byte[] data) throws LoginException, ConnectionException {
            Log.i("LitixComWrapper", "Simulation: FakeTrack.put " + data.length + " bytes");
            if (comm)
                Log.e("LitixComWrapper", "Simulation: FakeTrack illegal after-commit put call.");
        }

        @Override
        public void commit() throws ConnectionException, LoginException {
            Log.i("LitixComWrapper", "Simulation: FakeTrack.commit");
            comm = true;
        }
    }
}