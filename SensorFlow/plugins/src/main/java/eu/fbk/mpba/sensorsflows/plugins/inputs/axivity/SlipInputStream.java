package eu.fbk.mpba.sensorsflows.plugins.inputs.axivity;

import java.io.IOException;
import java.io.InputStream;

public class SlipInputStream extends InputStream{

    InputStream mInner;
    boolean error;

    public SlipInputStream(InputStream inner, boolean errorOnNonSLIPEscapeSequences) {
        mInner = inner;
        error = errorOnNonSLIPEscapeSequences;
    }

    @Override
    public int read() throws IOException {
        int r = mInner.read();
        if (r < 0)
            return EOS;
        else if (r == 0xDB) {
            r = mInner.read();
            switch (r) {
                case 0xDC:
                    return 0xC0;
                case 0xDD:
                    return 0xDB;
                default:
                    // Here an error should be thrown as a non-SLIP escape sequence have been read.
                    if (error)
                        throw new SLIPCodeViolationException("Non slip sequence read (0xDB + " + Integer.toHexString(r) + ").");
                    return r;
            }
        }
        else if (r == 0xC0)
            return SLIP_END;
        else
            return r;
    }

    public static final int EOS = -1;
    public static final int SLIP_END = -2;

    public class SLIPCodeViolationException extends RuntimeException {
        SLIPCodeViolationException(String s) {
            super(s);
        }
    }
}
