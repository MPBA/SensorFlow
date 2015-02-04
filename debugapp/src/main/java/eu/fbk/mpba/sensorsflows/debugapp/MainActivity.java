package eu.fbk.mpba.sensorsflows.debugapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import eu.fbk.mpba.sensorsflows.AutoLinkMode;
import eu.fbk.mpba.sensorsflows.FlowsMan;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.CsvOutput;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.SQLiteOutput;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.SmartphoneDevice;


public class MainActivity extends Activity {

    FlowsMan<Long, double[]> m = new FlowsMan<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m.addDevice(new SmartphoneDevice(this, "Smartphone"));
        m.addOutput(new CsvOutput("CSV",
                Environment.getExternalStorageDirectory().getPath()
                        + "/eu.fbk.mpba.sensorsflows/"));
        m.addOutput(new SQLiteOutput("DB",
                Environment.getExternalStorageDirectory().getPath()
                        + "/eu.fbk.mpba.sensorsflows/"));

        m.setAutoLinkMode(AutoLinkMode.PRODUCT);

        m.start();
    }

    public void onMClose(View v) {
        m.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
