package eu.fbk.mpba.sensorsflows.debugapp.plugins.inputs.CSVLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CSVHandler
{
    InputStreamReader is;
    String fs, rs;
    BufferedReader br;
    Object descriptors[] = null;

    void readDescriptors()
    {

    }

    public CSVHandler(InputStreamReader isr, String fieldSeparator, String rowSeparator)
    {
        is = isr;
        fs = fieldSeparator;
        rs = rowSeparator;

        br = new BufferedReader(is);
    }


    /**
     * @return next_field or null nel caso in cui la linea fosse finita
     *
     * Quando ritorna null va nella prossima linea.
     */
    public String getNextField() throws IOException
    {
        int i, indCharSep = 1;
        boolean primoCharSep = false;
        String str = "";
        StringBuilder sb = new StringBuilder();

        while((i = br.read()) != -1)
        {
            char c = (char)i;

            if(c == fs.charAt(0)) { str = fs; primoCharSep = true;}
            if(c == rs.charAt(0)) { str = rs; primoCharSep = true;}

            if(primoCharSep)//sono in un punto in cui potrebbe esserci il separatore
            {
                if(c != str.charAt(indCharSep))
                {
                    primoCharSep = false;
                    indCharSep = 1;
                }

                if(++indCharSep >= str.length())
                {
                    //ok c'e' il separatore, l'ho appena passato
                }
            }
            else
                sb.append(c);
        }

        return sb.toString();
    }
}
