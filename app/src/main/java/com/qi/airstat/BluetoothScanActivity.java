package com.qi.airstat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

public class BluetoothScanActivity extends Activity {
    final private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private ListView pairedDeviceListView = null;
    private ListView scannedDeviceListView = null;
    private Button scanButton = null;

    private ArrayAdapter<String> pairedDeviceListAdapter = null;
    private ArrayAdapter<String> scannedDeviceListAdapter = null;

    private final Set<String> pairedDeviceSet = new HashSet<String>();
    private final Set<String> scannedDeviceSet = new HashSet<String>();

    final private OnItemClickListener onClickDeviceListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            bluetoothAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            CharSequence info = ((TextView) view).getText();
            if (info != null) {
                CharSequence address = info.toString().substring(info.length() - 17);
                Intent intent = new Intent();
                intent.putExtra(Constants.BLUETOOTH_SCAN_ACTIVITY_EXTRA_MAC, address);

                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device != null) {
                    String address = device.getAddress();

                    if ((!scannedDeviceSet.contains(address)) && (!pairedDeviceSet.contains(address))) {
                        String name = device.getName();
                        scannedDeviceListView.setEnabled(true);
                        scannedDeviceSet.add(address);

                        if ((name == null) || name.isEmpty()) {
                            name = getString(R.string.lv_bluetooth_scan_paired_devices_empty_entry);
                        }

                        scannedDeviceListAdapter.add(name + '\n' + device.getAddress());
                    }
                }
                else {
                    // Handle error: could not get parcelable extra from device
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setTitle(R.string.activity_bluetooth_scan_select_device);

                if (scannedDeviceSet.isEmpty()) {
                    String noDevices = getResources().getText(R.string.lv_bluetooth_scan_scanned_devices_not_found).toString();
                    scannedDeviceListAdapter.add(noDevices);
                    scannedDeviceListView.setEnabled(false);
                }

                scanButton.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Dialog customDialog;
        LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
        View customView = inflater.inflate(R.layout.activity_bluetooth_scan, null);

        customDialog = new Dialog(this, R.style.DialogTheme);
        customDialog.setContentView(customView);
        customDialog.show();*/
        setContentView(R.layout.activity_bluetooth_scan);

        // Set result as CANCELED by default.
        // If user paired device will it will be set as OK.
        setResult(Activity.RESULT_CANCELED);

        pairedDeviceListView = (ListView)findViewById(R.id.lv_bluetooth_scan_paired_devices);
        scannedDeviceListView = (ListView)findViewById(R.id.lv_bluetooth_scan_scanned_devices);

        pairedDeviceListAdapter = new ArrayAdapter<String>(this, R.layout.activity_bluetooth_scan);
        scannedDeviceListAdapter = new ArrayAdapter<String>(this, R.layout.activity_bluetooth_scan);

        pairedDeviceListView.setAdapter(pairedDeviceListAdapter);
        scannedDeviceListView.setAdapter(scannedDeviceListAdapter);

        pairedDeviceListView.setOnItemClickListener(onClickDeviceListener);
        scannedDeviceListView.setOnItemClickListener(onClickDeviceListener);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }

        this.unregisterReceiver(broadcastReceiver);
    }
}
