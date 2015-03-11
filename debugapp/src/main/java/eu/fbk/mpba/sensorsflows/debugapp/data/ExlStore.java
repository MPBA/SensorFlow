package eu.fbk.mpba.sensorsflows.debugapp.data;

import android.os.Environment;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by toldo on 17/02/2015.
 */
public class ExlStore {
    private static final Exl EXLS3_012 = new Exl("EXLs3_012","00:80:E1:FE:F3:CD",null);
    private static final Exl EXLS3_0126 = new Exl("EXLs3_0126","00:80:E1:B0:B9:1C",null);
    private static final Exl EXLS3_0128 = new Exl("EXLs3_0128","00:80:E1:B0:B9:11",null);
    private static final Exl EXLS3_0174 = new Exl("EXLs3_0174","00:80:E1:B3:4E:B3",null);
    private static final Exl EXLS3_0175 = new Exl("EXLs3_0175","00:80:E1:B3:4E:E0",null);
    private static final Exl EXLS3_0176 = new Exl("EXLs3_0176","00:80:E1:B3:4E:AF",null);
    private static final Exl EXLS3_0183 = new Exl("EXLs3_0183","00:80:E1:B3:4E:C4",null);
    private static final Exl EXLS3_0185 = new Exl("EXLs3_0185","00:80:E1:B3:4E:A9",null);
    private static final Exl EXLS3_0181 = new Exl("EXLs3_0181","00:80:E1:B3:4E:D5",null);
    private static final Exl EXLS3_0182 = new Exl("EXLs3_0182","00:80:E1:B3:4E:CE",null);


    private static final Exl EXLS3_0127 = new Exl("EXLs3_0127","00:80:e1:b0:b9:17",null);
    private static final Exl EXLS3_0177 = new Exl("EXLs3_0177","00:80:e1:b3:4e:89",null);
    private static final Exl EXLS3_0179 = new Exl("EXLs3_0179","00:80:e1:b3:4e:83",null);
    private static final Exl EXLS3_0180 = new Exl("EXLs3_0180","00:80:e1:b3:4e:bd",null);

    public static final Exl[] data = {EXLS3_012,EXLS3_0126,EXLS3_0127,EXLS3_0128,EXLS3_0174,EXLS3_0175,EXLS3_0176,EXLS3_0177,EXLS3_0179,EXLS3_0180,EXLS3_0181,EXLS3_0182,EXLS3_0183,EXLS3_0185};


    public static  Exl[] loadFromFile(){
        File sdcard = Environment.getExternalStorageDirectory();
        File directory = new File(sdcard,"/eu.fbk.mpba.sensorsflows/"); // TODO 6 TRASLATE Convertire in risorse esterne
        directory.mkdir();
        File file = new File(sdcard,"sensor_config.txt");
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            // Parse data
            JSONObject obj = new JSONObject(text.toString());
            JSONArray sensors = obj.getJSONArray("exls");
            Exl[] data = new Exl[sensors.length()];
            for(int i = 0;i<sensors.length();i++)
            {
                JSONObject current_obj = sensors.getJSONObject(i);
                Exl current = new Exl(current_obj.getString("name"),current_obj.getString("mac"),null);
                data[i] = current;
            }
            return data;
        }
        catch (IOException e) {
            Log.d("ERROR", "Errore nella aperture del file di configurazione sensori");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("ERROR","Errore nella aperture del file di configurazione sensori - "+e.getMessage());
        }
        return null;
    }

}
