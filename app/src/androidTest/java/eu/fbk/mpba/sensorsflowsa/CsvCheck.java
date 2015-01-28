package eu.fbk.mpba.sensorsflowsa;

import java.io.FileReader;
import java.io.IOException;

public class CsvCheck {

    public static boolean checkStrongMonotonicityOfTheFirstColumnLong(String file) throws IOException {
        FileReader r = new FileReader(file);
        String l; readLine(r);
        Long last = 0L;
        while ((l = readLine(r)) != null) {
            String[] s = l.split(";(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            Long b = Long.parseLong(s[0]);
            if (last >= b)
                return false;
            last = b;
        }
        return true;
    }

    static String readLine(FileReader reader) throws IOException {
        StringBuilder b = new StringBuilder(100);
        int a;
        while ((a = reader.read()) >= 0) {
            if (a == '\n') {
                return b.toString();
            }
            else if (a != '\r')
                b.append((char)a);
        }
        return null;
    }
}
