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

    void getDescriptors()
    {
        if(descriptors == null)
        {

        }
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
        int i;
        StringBuilder sb = new StringBuilder();

        while((i = br.read()) != -1)
        {
            char c = (char)i;

            if(c == fs.charAt(0) || c == rs.charAt(0))
            {
                int ind = 0;
                StringBuilder sb2 = new StringBuilder();
                boolean bsrow = true;
                boolean bsfield = true;

                do
                {
                    c = (char)i;
                    if(c!=fs.charAt(ind))
                        bsfield = false;
                    if(c!=rs.charAt(ind))
                        bsrow = false;

                    sb2.append(c);
                    ind++;
                }
                while((bsrow || bsfield) && (i = br.read()) != -1);

                /*if(i == -1 || (!bsfield && !bsrow))
                    sb.append(sb2);*/
            }
            else
                sb.append(c);
        }

        return sb.toString();
    }
}
