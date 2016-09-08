package eu.fbk.mpba.sensorsflows.plugins.inputs.CSVLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


/**
 * PRIMA DI TUTTO:
 *              - Non mi assumo nessuna responsabilita' riguardo ad un uso scorretto del plugin,
 *                      ad esempio:
 *                          Se il file termina le righe con '\r\n' e voi settate come delimitatore solo '\n'
 *                          mi sembra piu' che naturale e ovvio che salteranno fuori i caratteri '\r' da qualche parte.
 *                      Detto questo: Buona Visione, grazie e arrivederci.
 *
 * ASSUNZIONI: assumo che...
 *
 *        INTESTAZIONE
 *              1) Il file CSV contenga un'intestazione.
 *              2) Nell'intestazione un campo coincida con la stringa [toLower] 'ts' oppure 'timestamp'.
 *              3) Non ci siano campi duplicati, viene lanciata un'eccezione se se ne trovano.
 *
 *        RIGHE
 *              4) Una riga sia fatta cosi': campo[sep]campo[sep]campo[finelinea]
 *              5) Il timestamp sia in nanosecondi (il campo 'timestampScale' in questo caso varra' 1),
 *                      se cosi' non fosse si dovra' settare opportunamente il campo 'timestampScale'
 *                      per rendere il timestamp fornito in nanosecondi,
 *                      ovviamente si puo' inserire valori decimali nel timestamp che saranno poi convertite in non decimali
 *                      attraverso la scala.
 *                      Cifre al di sotto del nanosecondo verranno prettamente ignorate.
 *                      Ricordarsi che con un double si perde un po' di precisione rispetto ad un long, letto da qui:
 *                          http://ubuntuforums.org/showthread.php?t=1520796
 *              6) Tutti i campi siano numerici (righe con campi non validi verranno ignorate e riportate come errore).
 *              7) Tutti i numeri decimali siano rappresentati col punto, non con la virgola: ###.##### e non ###,#####
 *              8) I campi devono essere in numero uguale all'intestazione, righe malformate saranno ignorate
 *                      e verra' riportato un errore.
 *              9) Righe vuote verranno riportate come errate ed ignorate.
 *
 *        SEPARATORI
 *              10) I due separatori non siano l'uno conenuto nell'altro.
 *                      Mettiamo che Gino abbia settato i separatori ";" e ";@",
 *                      cosi' facendo toglie pero' a Gesualdo (che costruisce il file csv)
 *                      l'opportunita' di scrivere un campo in questo modo: @campo
 *                      perche' verrebbe interpretato in maniera errata come una fine di riga;
 *                      stessa cosa per "@;@" oppure "@;".
 *              11) [buon senso] Chi sceglie i separatori li scelga in modo coerente con i dati, esempio:
 *                      fieldSep = ".0"; rowSep = "5.";
 *                      Se provate a scrivere questi dati con quei separatori:
 *                                                                  ts  x    y  z
 *                                                                  5   5.0  5  5.0
 *                      ne esce una cosa incomprensibile:
 *                                                                  ts.0x.0y.0z5.5.05.0.05.05.0
 *                      I controlli per evitare cose simili sono difficili da fare, quindi delego il buon senso all'utente.
 *
 *
 * GESTIONE ERRORI:
 *              - Lancio un'eccezione se si verifica un errore nel Costruttore, altrimenti ritorno una classe CSVRow
 *                      contenente il campo 'error' settato a 'true' nel metodo getNextRow.
 *
 *
 * NOTE:
 *              - Se avviene un errore grave le successive azioni saranno come se file fosse finito.
 */
public class CSVHandler {
    public enum FieldType {NORMAL, ENDLINE, ENDFILE}

    public class Field {
        public String value;
        public FieldType type;

        public Field(String v, FieldType t) {
            value = v;
            type = t;
        }
    }

    public class CSVRow {
        public long timestamp;
        private boolean error2;
        private String errorMsg2;
        public boolean endfile;
        private int charcount;//se e' zero vuol dire che c'e' un fineriga e poi un finefile.

        public boolean isValid()
        {
            return charcount != 0;
        }

        public void setError(String errorMessage) {
            error2 = true;
            errorMsg2 = errorMessage;
        }

        public boolean getError() {
            return error2;
        }

        public String getErrorMsg() {
            return errorMsg2;
        }

        public void addErrorInfo(String str) {
            errorMsg2 += str;
        }

        public double[] fields;

        public CSVRow(int numFields) {
            fields = new double[numFields];
            error2 = endfile = false;
            errorMsg2 = "";
            charcount = 0;
        }
    }

    private InputStreamReader is;
    private String fs, rs;
    private BufferedReader br;
    private LinkedList<Object> descriptors;
    private boolean endoffile = false;
    private int tsIndex = -1;
    private int rowIndex = 2;
    private long tsScale;
    Field f = null;

    public LinkedList<Object> getDescriptors()
    {
        return descriptors;
    }

    /**
     * @return Row class che contiene i campi double.
     * @throws IOException nel caso non riesca a leggere un campo correttamente dal file
     *                     Nel caso in cui il file sia finito ritorna una riga nulla con il tipo: ENDFILE.
     *                     Se una riga è invalida setta il tipo: ERROR e imposta il messaggio.
     */
    public CSVRow getNextRow() throws IOException
    {
        CSVRow r = new CSVRow(descriptors.size());
        int i = 0, j = 0;

        StringBuilder sb = new StringBuilder();

        if (endoffile)
        {
            r.endfile = true;
            return r;
        }

        do
        {
            if(f == null)
                f = getNextField();

            sb.append(f.value);
            if (f.type == FieldType.NORMAL) {sb.append(fs);sb.append(" ");}

            if (i++ == tsIndex)
            {
                try{r.timestamp = Long.parseLong(f.value) * tsScale;}
                catch (Exception e2)
                {
                    try {r.timestamp = (long) (Double.parseDouble(f.value) * tsScale);}
                    catch (Exception e) {r.setError("Errore linea " + rowIndex + ": Timestamp non valido.");}
                }
            }
            else
                try{r.fields[j++] = Double.parseDouble(f.value);}
                catch (Exception e) {r.setError("Errore linea " + rowIndex + ": campi non validi (Caratteri non validi || Virgola anziche' il punto).");}

            r.charcount += f.value.length();

            if(f.type == FieldType.NORMAL)
                f = null;
        }
        while (f == null);

        FieldType tipoUltimoCampo = f.type;

        if (j != descriptors.size())
        {
            f = getNextField();
            if(f.type == FieldType.ENDFILE && tipoUltimoCampo == FieldType.ENDFILE && r.charcount == 0)
            {
                r.endfile = true;
                r.error2 = false;
                f = null;
            }
            else
                r.setError("Errore linea " + rowIndex + ": Il numero dei campi non è conforme all'intestazione");
        }
        else
            f = null;

        if (r.getError())
            r.addErrorInfo(" [linea: '" + sb.toString() + "']");

        if (tipoUltimoCampo == FieldType.ENDFILE)
            r.endfile = true;

        rowIndex++;

        return r;
    }

    public CSVHandler(InputStreamReader isr, String fieldSeparator, String rowSeparator, long timestampScale) throws Exception {
        is = isr;
        fs = fieldSeparator;
        rs = rowSeparator;
        descriptors = new LinkedList<>();
        tsScale = timestampScale;

        if (fs.contains(rs) || rs.contains(fs)) {
            endoffile = true;
            throw new Exception("Un separatore e' contenuto nell'altro, questo causa errori di formattazione nel file CSV.\nLeggere le assunzioni di CSVHandler per piu' informazioni.");
        }


        br = new BufferedReader(is);

        tsIndex = -1;
        int i = 0;
        Field f;
        do {
            f = getNextField();

            if (!f.value.toLowerCase().equals("ts") && !f.value.toLowerCase().equals("timestamp"))
                descriptors.add(f.value);
            else if (tsIndex == -1)
                tsIndex = i;
            i++;
        }
        while (f.type == CSVHandler.FieldType.NORMAL);

        //Controllo dei duplicati
        //noinspection unchecked
        LinkedList<Object> d2 = (LinkedList<Object>) descriptors.clone();
        Collections.sort(d2, new Comparator<Object>(){public int compare(Object o1, Object o2){return ((String)o1).compareTo((String)o2);}});
        boolean duplicati = false;
        Iterator<Object> it = d2.listIterator();
        if (it.hasNext())
        {
            Object previous = it.next();
            while(it.hasNext())
            {
                Object current = it.next();
                if (previous.equals(current))
                {
                    duplicati = true;
                    break;
                }
                previous = current;
            }
        }
        if(duplicati)
        {
            endoffile = true;
            throw new Exception("Campi duplicati nell'intestazione.");
        }

        if (tsIndex == -1) {
            endoffile = true;
            throw new Exception("Non e' presente un campo che [toLower] corrisponda alla stringa 'ts' oppure 'timestamp'.\nLeggere le assunzioni di CSVLoaderSensor per piu' informazioni.");
        }
    }


    /**
     * @return [next field]
     * La classe Field contiene info a seconda se e' un campo di tipo:
     * - Normale
     * - Fine Linea
     * - Fine File
     */
    private Field getNextField() throws IOException {
        int i;
        StringBuilder sb = new StringBuilder();

        while ((i = br.read()) != -1)
        {
            char c = (char) i;

            if (c == fs.charAt(0) || c == rs.charAt(0)) {
                int indF = 0;
                int indR = 0;
                StringBuilder sb2 = new StringBuilder();
                boolean bsfield = true;
                boolean bsrow = true;

                do {
                    c = (char) i;
                    if (indF < fs.length() && c != fs.charAt(indF)) {
                        if (c != fs.charAt(0))
                            bsfield = false;
                        else if (!bsrow) {
                            sb.append(sb2.toString());
                            sb2.setLength(0);
                            indF = 0;
                        }
                    }
                    if (indR < rs.length() && c != rs.charAt(indR)) {
                        if (c != rs.charAt(0))
                            bsrow = false;
                        else if (!bsfield) {
                            sb.append(sb2.toString());
                            sb2.setLength(0);
                            indR = 0;
                        }
                    }

                    sb2.append(c);
                    indF++;
                    indR++;

                    if (bsfield && indF == fs.length())
                        break;
                    if (bsrow && indR == rs.length())
                        break;
                }
                while ((bsfield || bsrow) && (i = br.read()) != -1);

                if (i == -1 || (!bsfield && !bsrow))
                    sb.append(sb2.toString());
                else
                    if (bsfield)
                        return new Field(sb.toString(), FieldType.NORMAL);
                    else
                        return new Field(sb.toString(), FieldType.ENDLINE);
            } else
                sb.append(c);
        }

        endoffile = true;
        return new Field(sb.toString(), FieldType.ENDFILE);
    }
}

