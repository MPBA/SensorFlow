package eu.fbk.mpba.sensorsflows.debugapp.plugins.inputs.CSVLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * ASSUNZIONI: assumo che...
 *              - I due separatori non siano l'uno conenuto nell'altro.
 *                      Mettiamo che Gino abbia settato i separatori ";" e ";@",
 *                      cosi' facendo toglie pero' a Gesualdo (che costruisce il file csv)
*                       l'opportunita' di scrivere un campo in questo modo: @campo
 *                      perche' verrebbe interpretato in maniera errata come una fine di riga.
 *
 * GESTIONE ERRORI: - Lancio un'eccezione se si verifica un errore.
 */
public class CSVHandler
{
    public enum FieldType {NORMAL, ENDLINE, ENDFILE}
    public class Field{
        public String value;
        public FieldType type;
        public Field(String v, FieldType t)
        {
            value = v;
            type = t;
        }
    }
    public class CSVRow{
        public long timestamp;
        public boolean error;
        public String errorMsg;
        public double[] fields;
        public CSVRow(int numFields)
        {
            fields = new double[numFields];
            error = false;
            errorMsg = "";
        }
    }

    private InputStreamReader is;
    private String fs, rs;
    private BufferedReader br;
    private LinkedList<Object> descriptors;
    private boolean endoffile = false;
    private int tsIndex = -1;
    private int dID, rowIndex = 2;

    public LinkedList<Object> getDescriptors() {
        return descriptors;
    }

    /**
     * @return Row class che contiene i campi double.
     * @throws IOException nel caso non riesca a leggere un campo correttamente dal file
     * Nel caso in cui il file sia finito ritorna null.
     */
    public CSVRow getNextRow() throws IOException {
        if(endoffile)
            return null;

        CSVRow r = new CSVRow(descriptors.size());
        int i = 0, j=0;
        Field f;
        StringBuilder sb = new StringBuilder();
        do
        {
            f = getNextField();

            sb.append(f.value);
            if(f.type == FieldType.NORMAL) { sb.append(fs); sb.append(" "); }

            if(i++ == tsIndex)
                try {r.timestamp = Long.parseLong(f.value);}catch(Exception e){r.error = true;}
            else
                try {r.fields[j++] = Double.parseDouble(f.value);}catch(Exception e){r.error = true;}
        }
        while(!r.error && f.type == FieldType.NORMAL);

        if(j != descriptors.size())
            r.error = true;

        if(r.error)
            r.errorMsg = "["+dID+"] Errore nella linea numero "+rowIndex+"; linea: '" + sb.toString() + "'";

        rowIndex++;

        return r;
    }

    public CSVHandler(int debugID, InputStreamReader isr, String fieldSeparator, String rowSeparator) throws Exception
    {
        is = isr;
        fs = fieldSeparator;
        rs = rowSeparator;
        descriptors = new LinkedList<>();
        dID = debugID;

        if(fs.contains(rs) || rs.contains(fs)) {
            endoffile = true;
            throw new Exception("[SID"+dID+"] Un separatore e' contenuto nell'altro, questo causa errori di formattazione nel file CSV.\nLeggere le assunzioni di CSVHandler per piu' informazioni.");
        }


        br = new BufferedReader(is);

        tsIndex = -1;
        int i = 0;
        Field f;
        do
        {
            f = getNextField();

            if(!f.value.toLowerCase().equals("ts") && !f.value.toLowerCase().equals("timestamp"))
                descriptors.add(f.value);
            else if(tsIndex == -1)
                tsIndex = i;
            i++;
        }
        while(f.type == CSVHandler.FieldType.NORMAL);

        if(tsIndex == -1) {
            endoffile = true;
            throw new Exception("[SID"+dID+"] Non e' presente un campo che [toLower] corrisponda alla stringa 'ts' oppure 'timestamp'.\nLeggere le assunzioni di CSVLoaderSensor per piu' informazioni.");
        }
    }


    /**
     * @return [next field]
     * La classe Field contiene info a seconda se e' un campo di tipo:
     *     - Normale
     *     - Fine Linea
     *     - Fine File
     */
    private Field getNextField() throws IOException {
        int i;
        StringBuilder sb = new StringBuilder();

        while((i = br.read()) != -1)
        {
            char c = (char)i;

            if(c == fs.charAt(0) || c == rs.charAt(0))
            {
                int indF = 0;
                int indR = 0;
                StringBuilder sb2 = new StringBuilder();
                boolean bsfield = true;
                boolean bsrow = true;

                do
                {
                    c = (char)i;
                    if(indF < fs.length() && c!=fs.charAt(indF))
                    {
                        if(c != fs.charAt(0))
                            bsfield = false;
                        else
                        {
                            sb.append(sb2.toString());
                            sb2.setLength(0);
                            indF = 0;
                        }
                    }
                    if(indR < rs.length() && c!=rs.charAt(indR))
                    {
                        if(c != rs.charAt(0))
                            bsrow = false;
                        else
                        {
                            sb.append(sb2.toString());
                            sb2.setLength(0);
                            indR = 0;
                        }
                    }

                    sb2.append(c);
                    indF++;
                    indR++;

                    if(bsfield && indF == fs.length())
                        break;
                    if(bsrow && indR == rs.length())
                        break;
                }
                while((bsfield || bsrow) && (i = br.read()) != -1);

                if(i == -1 || (!bsfield && !bsrow))
                    sb.append(sb2.toString());
                else if(bsfield) return new Field(sb.toString(), FieldType.NORMAL);
                else if(bsrow) return new Field(sb.toString(), FieldType.ENDLINE);
            }
            else
                sb.append(c);
        }

        endoffile = true;
        return new Field(sb.toString(), FieldType.ENDFILE);
    }
}

