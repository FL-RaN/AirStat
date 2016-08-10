package com.qi.airstat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
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
            if (i == 0) {
                return;
            }

            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            // Get the device MAC address, which is the last 17 chars in the View
            Log.d("SCAN ACTIVITY", "" + view.getId());

            LinearLayout parent = (LinearLayout)view;
            TextView text = (TextView)(view.findViewById(R.id.tv_adapter_item_bluetooth_scan));

            CharSequence info = text.getText();
            if (info != null) {
                CharSequence address = info.toString().substring(info.length() - Constants.BLUETOOTH_SCANNED_TEXT_MAC_OFFSET);
                Intent intent = new Intent();
                intent.putExtra(Constants.BLC_SCAN_RESULT_MAC, address);

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
                        scannedDeviceListAdapter.notifyDataSetChanged();
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

        setContentView(R.layout.activity_bluetooth_scan);

        // Set result as CANCELED by default.
        // If user paired device will it will be set as OK.
        setResult(Activity.RESULT_CANCELED);

        pairedDeviceListView = (ListView)findViewById(R.id.lv_bluetooth_scan_paired_devices);
        scannedDeviceListView = (ListView)findViewById(R.id.lv_bluetooth_scan_scanned_devices);

        pairedDeviceListAdapter = new ArrayAdapter<String>(this, R.layout.adapter_item_bluetooth_scan, R.id.tv_adapter_item_bluetooth_scan);
        scannedDeviceListAdapter = new ArrayAdapter<String>(this, R.layout.adapter_item_bluetooth_scan, R.id.tv_adapter_item_bluetooth_scan);

        pairedDeviceListView.setAdapter(pairedDeviceListAdapter);
        scannedDeviceListView.setAdapter(scannedDeviceListAdapter);

        pairedDeviceListView.setOnItemClickListener(onClickDeviceListener);
        scannedDeviceListView.setOnItemClickListener(onClickDeviceListener);

        pairedDeviceListAdapter.add("Paired Devices");
        scannedDeviceListAdapter.add("Scanned Devices");

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice pairedDevice : pairedDevices) {
            String deviceName = pairedDevice.getName();
            String deviceAddress = pairedDevice.getAddress();

            if ((deviceName == null) || deviceName.isEmpty()) {
                deviceName = getString(R.string.lv_bluetooth_scan_paired_devices_empty_entry);
            }

            pairedDeviceListAdapter.add(deviceName + '\n' + deviceAddress);
        }
        pairedDeviceListAdapter.notifyDataSetChanged();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
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

    public void onClickScan(View view) {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            scanButton.setText("Scan devices");
        }
        else {
            scannedDeviceListAdapter.clear();
            scannedDeviceListAdapter.add("Scanned Devices");
            scannedDeviceListAdapter.notifyDataSetChanged();

            bluetoothAdapter.startDiscovery();
            scanButton.setText("Stop scan");
        }
    }
}
