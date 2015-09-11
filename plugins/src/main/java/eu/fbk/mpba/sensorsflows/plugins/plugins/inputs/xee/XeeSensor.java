package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.xee;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.dquid.xee.driver.DQDriver;
import com.dquid.xee.driver.DQDriverEventListener;
import com.dquid.xee.driver.DQSourceType;
import com.dquid.xee.sdk.DQAccelerometerData;
import com.dquid.xee.sdk.DQData;
import com.dquid.xee.sdk.DQGpsData;
import com.dquid.xee.sdk.DQListenerInterface;
import com.dquid.xee.sdk.DQUnitManager;
import com.dquid.xee.sdk.DQUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;

public class XeeSensor extends SensorComponent<Long, double[]> implements DQListenerInterface, DQDriverEventListener {

    private boolean receivingData;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice deviceToConnect;
    private boolean firmwareUPDRequested;
    private boolean connectionRequested;
    private boolean serialNumberRequested;

    // Device chooser result
    protected void setDeviceToConnect(BluetoothDevice d) {
        // TODO 3: Set deviceToConnect
        deviceToConnect = d;
    }

    protected void setEnvironment(DQUtils.DQuidEnvs e) {
        DQUtils.setEnvironment(e);
        // TODO 3: check docu
//        DQUtils.setEnvironment(DQUtils.DQuidEnvs.test);
//        DQUtils.setEnvironment(DQUtils.DQuidEnvs.stage);
//        DQUtils.setEnvironment(DQUtils.DQuidEnvs.prod);
    }

    // Device Connected Button
    protected void connection(boolean connect) {
        // TODO 3: Call initDriver
        if (connect) {
            // WAS: simulator option
            if (deviceToConnect != null) {
                connectionRequested = true;
                initDriver(false);
            } else {
                // TODO 5: error or choose
            }

        } else {
//            updateUiOnConnection(false, -1);
            disconnectFromBd();
        }
    }

    // Button firmware update clicked
    protected void callFWUpdate(DQUtils.DQuidEnvs e) {
        // TODO 3: Button firmware update clicked
//        if (deviceToConnect != null){
//            firmwareUPDRequested = true;
//            DQDriver.INSTANCE.disableSource(DQSourceType.BLUETOOTH_2_1);
//        }
    }

    protected boolean debug = true;
    protected final String debugTAG = "XeeALE";
    protected DevicePlugin<Long, double[]> parent;

    protected XeeSensor(DevicePlugin < Long,double[]> parent) {
        super(parent);
        this.parent = parent;
        if (debug)
            Log.d(debugTAG, "onCreate");
        bluetoothCheck();
        DQDriver.INSTANCE.setEventListener(this);
        DQUnitManager.INSTANCE.addListener(this);
        setReceivingData(DQUtils.automaticallyReceiveDataAfterConnection);
    }

    public void switchOnAsync(){
        if (debug)
            Log.d(debugTAG, "onResume");

        DQUnitManager.INSTANCE.addListener(this);
    }

    public void switchOffAsync() {
        if (debug)
            Log.d(debugTAG, "onPause");
        DQDriver.INSTANCE.disableSource(DQSourceType.BLUETOOTH_2_1);
    }

    @Override
    public String getName() {
        return ""; // TODO x: Add sense
    }

    @Override
    public List<Object> getValuesDescriptors() {
        return null; // TODO x: Add sense
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void bluetoothCheck() {
        if (debug)
            Log.d(debugTAG, "bluetoothCheck");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            // TODO 5: errorazzz
        } else if (!mBluetoothAdapter.isEnabled()) {
            // If bluetooth is not enabled request to enable it
//            Intent enableBtIntent = new Intent(
//                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            // TODO 5: bt callback or manage in the constructor
        }
    }

    private void setReceivingData(final boolean receiving) {

        if (debug)
            Log.d(debugTAG, "setReceivingData - receiving: " + receiving);

        if (receiving) {
            DQUnitManager.INSTANCE.startReceivingCarData();
        } else {
            DQUnitManager.INSTANCE.stopReceivingCarData();
        }

        receivingData = receiving;
    }

    // Show Progress Wheel
    private void showProgressWheel(final String title, final String message) {
        // TODO 6: translate
        if (debug)
            Log.d(debugTAG, "showProgressWheel");
//        runOnUiThread(new Runnable() {
//            public void run() {
//                progressDialog = ProgressDialog.show(MainActivityElab.this, title, message);
//                progressDialog.setCancelable(false);
//            }
//        });
    }

    // Hide Progress Wheel
    private void hideProgressWheel() {
        // TODO 6: translate
        if (debug)
            Log.d(debugTAG, "hideProgressWheel");

//        if (progressDialog != null)
//            progressDialog.dismiss();
    }

    void initDriver(Boolean simulation) {
        showProgressWheel("Connecting..", "Please Wait");

        if(simulation){
            // DQDriver reads from a can trace (Simulator)
            DQDriver.INSTANCE.enableSource(DQSourceType.SIMULATOR_CAN_TRACE);

        } else {
            // DQDriver reads from bluetooth 2.1 (xee)
            if (deviceToConnect == null)
                Log.wtf(debugTAG, "deviceToConnect not set!");
            DQDriver.INSTANCE.setBtDevice(deviceToConnect);
            DQDriver.INSTANCE.enableSource(DQSourceType.BLUETOOTH_2_1);
        }
    }

    void disconnectFromBd() {
        showProgressWheel("Disconnecting..", "Please Wait");
        DQUnitManager.INSTANCE.disconnect();
    }

    @Override
    public void onConnectionSuccessful() {
        if (debug)
            Log.d(debugTAG, "Connection Successful");

//		updateUiOnConnection(true, -1);
        // TODO 5: event
    }

    @Override
    public void onDisconnection() {
        if (debug)
            Log.d(debugTAG, "Disconnected");

//		updateUiOnConnection(false, -1);
        // TODO 5: event
    }

    @Override
    public void onError(int arg0, String arg1) {
        final String completeDescr = "Error Occurred, code: " + arg0 + ": " + arg1;

        if (debug)
            Log.d(debugTAG, completeDescr);

        if(arg0 == 401){
            Log.d(debugTAG, "ERRORE!!!!");
            DQUnitManager.INSTANCE.disconnect();
            // TODO 7: check this if/code
        }

        // TODO 5: event
        //        runOnUiThread(new Runnable() {
        //
        //            @Override
        //            public void run() {
        //
        //                hideProgressWheel();
        //
        //                Toast.makeText(getApplicationContext(), completeDescr, Toast.LENGTH_LONG).show();
        //            }
        //        });
    }

    @Override
    public void onNewAccelerometerData(DQAccelerometerData arg0) {
        if(debug)
            Log.d(debugTAG, "onNewAccelerometerData - " + arg0.toString());
        // TODO 5: data sensor ACC
    }

    @Override
    public void onNewGpsData(DQGpsData arg0) {
        if(debug)
            Log.d(debugTAG, "onNewGpsData - " + arg0.toString());
        // TODO 5: data sensor GPS
    }

    @Override
    public void onNewData(HashMap<Long, DQData> arg0) {
        //noinspection unused
        HashMap<Long, DQData> dataHashMap = DQUnitManager.INSTANCE.getLastAvailable();
        // TODO 5: data sensor DATA
        // TODO 8: check the difference between arg0 and dataHashMap.
    }

    @Override
    public void onDriverDown(int arg0) {
        if(debug)
            Log.d(debugTAG, "onDriverDown - reason: " + DQDriver.INSTANCE.dqdriverErrorDescriptions.get(arg0));

        hideProgressWheel();

        // TODO 4: understand fw update operation
        if(firmwareUPDRequested)
            initDriver(false);
    }

    @Override
    public void onDriverReady() {
        // TODO 4: understand driver ready/down operation (check docu)
        hideProgressWheel();

        if (debug)
            Log.d(debugTAG, "onDriverReady");

        if (firmwareUPDRequested) {
            firmwareUPDRequested = false;
            DQUnitManager.INSTANCE.updateFirmware();

        } else if (connectionRequested) {
            connectionRequested = false;

            if (serialNumberRequested) {
                hideProgressWheel();
                DQUnitManager.INSTANCE.getSerialNumber();
            } else {
                DQUnitManager.INSTANCE.connect();
                showProgressWheel("Connecting..", "Please Wait");
            }
        } else
            DQUnitManager.INSTANCE.checkFirmwareVersion();
    }

    // unused

    @Override
    public void onDtcCodesAvailable(final ArrayList<String> arg0) {
        if(debug)
            Log.d(debugTAG, "DTC Codes: " + arg0.toString());
    }

    @Override
    public void onDtcNumberAvailable(final int arg0) {
        if (debug)
            Log.d(debugTAG, "DTC Number: " + arg0);
    }

    @Override
    public void onFirmwareUpdateCompleted() {
        if (debug)
            Log.d(debugTAG, "onFirmwareUpdateCompleted");
    }

    @Override
    public void onFirmwareUpdateIncrease(final double arg0) {
        if (debug)
            Log.d(debugTAG, "onFirmwareUpdateIncrease: " + arg0);
    }

    @Override
    public void onFirmwareUpdateNeeded(String versionAvailable) {
        if(debug)
            Log.d(debugTAG, "onFirmwareUpdateNeeded - version " + versionAvailable + " is available");

        firmwareUPDRequested = true;
        DQDriver.INSTANCE.disableSource(DQSourceType.BLUETOOTH_2_1);
    }

    @Override
    public void onFirmwareUpdateNotNeeded() {
        if(debug)
            Log.d(debugTAG, "onFirmwareUpdateNotNeeded");
    }

    @Override
    public void onFirmwareUpdateStarted() {
        if(debug)
            Log.d(debugTAG, "onFirmwareUpdateStarted");
    }

    @Override
    public void onSerialNumberObtained(final String arg0) {
        if(debug)
            Log.d(debugTAG, "onSerialNumberObtained: " + arg0);

        // TODO 8: return it to the user/event
    }

    @Override
    public void onFirmwareVersionObtained(final String arg0) {
        if(debug)
            Log.d(debugTAG, "onFirmwareVersionObtained: " + arg0);

    }

    @Override
    public void onCloseDataSessionAck() {

    }

    @Override
    public void onCloseDataSessionNack() {

    }

    @Override
    public void onOpenDataSessionAck() {

    }

    @Override
    public void onOpenDataSessionNack() {

    }

    @Override
    public void onSettingAck() {

    }

    @Override
    public void onSettingNack() {

    }
}
