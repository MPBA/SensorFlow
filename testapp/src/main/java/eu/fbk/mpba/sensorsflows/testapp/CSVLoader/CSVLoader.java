package eu.fbk.mpba.sensorsflows.testapp.CSVLoader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;

import eu.fbk.mpba.sensorsflows.FlowsMan;
import eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.CSVLoader.CSVLoaderDevice;

public class CSVLoader
{
    static class FiledTextView extends TextView
    {
        File f;
        public FiledTextView(Context context) {
            super(context);
        }
    }

    private static final int FILE_SELECT_CODE = 0;
    static LinearLayout fc;
    static LinearLayout lall;

    static HashMap<String, Long> scale = new HashMap<>();
    private static TextView getNewFileListEntry(File f, final Activity _this){
        FiledTextView tw = new FiledTextView(_this);
        tw.setText(f.getName());
        tw.setTextSize(20);
        tw.f = f;
        tw.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ((LinearLayout) view.getParent()).removeView(view);
                return true;
            }
        });
        return tw;
    }
    private static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    private static void showFileChooser(Activity a) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(DocumentsContract.Document.MIME_TYPE_DIR);

        try{a.startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);}
        catch (android.content.ActivityNotFoundException ex){Toast.makeText(a, "Please install a File Manager.", Toast.LENGTH_SHORT).show();}
    }


    public static void drawGraphics(CheckBox c, LinearLayout where, final Activity _this)
    {
        lall = new LinearLayout(_this);
        lall.setOrientation(LinearLayout.VERTICAL);
        lall.setVisibility(View.GONE);
        where.addView(lall);

        LinearLayout.LayoutParams lp;
        LinearLayout ltemp = new LinearLayout(_this);
        ltemp.setOrientation(LinearLayout.HORIZONTAL);
        lall.addView(ltemp);

        LinearLayout fline = new LinearLayout(_this);
        fline.setBackgroundColor(0xff82C215);
        lp = new LinearLayout.LayoutParams(20, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.rightMargin = 20;
        fline.setLayoutParams(lp);
        ltemp.addView(fline);

        fc = new LinearLayout(_this);
        fc.setOrientation(LinearLayout.VERTICAL);
        lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fc.setLayoutParams(lp);
        ltemp.addView(fc);

        //Prendo la scala timestamp per alcuni files.
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory().getPath() + "/eu.fbk.mpba.sensorsflows/inputCSVLoader/input_config.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.replaceAll("\\s", "").charAt(0) != '#') {
                    String[] parts = line.split(";");
                    scale.put(parts[0], Long.parseLong(parts[1]));
                }
            }
        } catch (Exception e) {}
        finally
        {
            try {
                if (br != null)
                    br.close();
            } catch (Exception e) {}
        }
        //Carico i files dalla cartella di input
        final File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/eu.fbk.mpba.sensorsflows/inputCSVLoader");
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile() && !fileEntry.getName().equals("input_config.txt")) {
                long tsScale = 1;
                Long tmp = scale.get(fileEntry.getName());
                if (tmp != null)
                    tsScale = tmp;

                fc.addView(getNewFileListEntry(fileEntry, _this));
            }
        }

        Button b = new Button(_this);
        lp = new LinearLayout.LayoutParams(150, 150);
        lp.topMargin = 20;
        lp.leftMargin = 20;
        b.setLayoutParams(lp);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 75 );
        shape.setColor(0xff82C215);
        b.setTextColor(0xffFFFFFF);
        b.setBackground(shape);
        b.setText("+");
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser(_this);
            }
        });
        lall.addView(b);

        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b)
                    lall.setVisibility(View.GONE);
                else
                    lall.setVisibility(View.VISIBLE);
            }
        });
    }

    public static void onActivityResult(Activity _this,int requestCode, int resultCode, Intent data)
    {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                //if (resultCode == RESULT_OK)
                {
                    Uri uri = data.getData();
                    String path = "";
                    try{ path = getPath(_this, uri); } catch(Exception e){}
                    if(!path.equals(""))
                        fc.addView(getNewFileListEntry(new File(path), _this));
                }
                break;
        }
    }

    public static Runnable getRunnable(final FlowsMan<Long, double[]> m, final Activity _this, final String nomeDevice)
    {
        return new Runnable() {
            @Override
            public void run() {

                //Creo il device
                CSVLoaderDevice cl = new CSVLoaderDevice(nomeDevice);
                cl.setAsyncActionOnFinish(new Runnable() {
                    public void run() {
                        _this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(_this, "FINITOOOOO :D", Toast.LENGTH_LONG).show();
                                Log.i("CSVLoader", "FINITOOOOO :D");
                            }
                        });
                    }
                });

                for (int i = 0; i < fc.getChildCount(); i++)
                {
                    FiledTextView ftw = (FiledTextView)fc.getChildAt(i);
                    try{cl.addFile(new InputStreamReader(new FileInputStream(ftw.f)), ";", "\n", !scale.containsKey(ftw.f.getName())?1 : scale.get(ftw.f.getName()), ftw.f.getName());}
                    catch(Exception e){Log.i("CSVL", "Errore add file '"+ftw.f.getName()+"' msg: " + e.getMessage());}
                }

                m.addDevice(cl);
            }
        };
    }
}
