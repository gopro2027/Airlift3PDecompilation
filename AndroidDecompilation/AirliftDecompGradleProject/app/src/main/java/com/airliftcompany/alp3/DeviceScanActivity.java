package com.airliftcompany.alp3;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.airliftcompany.alp3.comm.BleService;
import com.airliftcompany.alp3.comm.CommService;
import com.airliftcompany.alp3.comm.CommServiceListener;
import com.airliftcompany.alp3.firmware.FirmwareUpdateActivity;
import com.airliftcompany.alp3.firmware.FirmwareVersionDownloadUtility;
import com.airliftcompany.alp3.utils.ALP3Preferences;
import com.airliftcompany.alp3.utils.CognitoService;
import com.airliftcompany.alp3.utils.Util;
import java.util.ArrayList;
import java.util.UUID;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class DeviceScanActivity extends ListActivity implements CommServiceListener {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "DeviceScanActivity";
    private String deviceAddress;
    private BluetoothAdapter mBluetoothAdapter;
    private CommService mCommService;
    private ProgressDialog mDialog;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private boolean mScanning;
    private ScanCallback scanCallback;
    private final ServiceConnection mServiceConnection = new ServiceConnection() { // from class: com.airliftcompany.alp3.DeviceScanActivity.4
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            DeviceScanActivity.this.mCommService = ((CommService.LocalBinder) iBinder).getService();
            DeviceScanActivity.this.mCommService.setCommServiceListener(DeviceScanActivity.this);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            DeviceScanActivity.this.mCommService = null;
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.airliftcompany.alp3.DeviceScanActivity.6
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            Toast.makeText(DeviceScanActivity.this, "BT change received !", 0).show();
            if ("android.bluetooth.device.action.FOUND".equals(action)) {
                Toast.makeText(DeviceScanActivity.this, bluetoothDevice.getName() + " Device found", 0).show();
            } else if ("android.bluetooth.device.action.ACL_CONNECTED".equals(action)) {
                Toast.makeText(DeviceScanActivity.this, bluetoothDevice.getName() + " Device is now connected", 0).show();
            } else if ("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED".equals(action)) {
                Toast.makeText(DeviceScanActivity.this, bluetoothDevice.getName() + " Device is about to disconnect", 0).show();
            } else if ("android.bluetooth.device.action.ACL_DISCONNECTED".equals(action)) {
                Toast.makeText(DeviceScanActivity.this, bluetoothDevice.getName() + " Device has disconnected", 0).show();
            }
            DeviceScanActivity.this.mLeDeviceListAdapter.notifyDataSetChanged();
        }
    };
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() { // from class: com.airliftcompany.alp3.DeviceScanActivity.11
        @Override // android.bluetooth.BluetoothAdapter.LeScanCallback
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
            DeviceScanActivity.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.DeviceScanActivity.11.1
                @Override // java.lang.Runnable
                public void run() {
                    DeviceScanActivity.this.mLeDeviceListAdapter.addDevice(bluetoothDevice);
                    DeviceScanActivity.this.mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onStatusUpdated() {
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(C0380R.string.device_pairing);
        }
        setContentView(C0380R.layout.activity_device_scan);
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(C0380R.string.permission_request));
            builder.setMessage(getString(C0380R.string.this_app_needs_location_access_to_detect_ble_devices));
            builder.setPositiveButton(C0380R.string.OK, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.DeviceScanActivity.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    if (Build.VERSION.SDK_INT >= 23) {
                        DeviceScanActivity.this.requestPermissions(new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}, 1);
                    }
                }
            });
            builder.create().show();
        }
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.mDialog = progressDialog;
        progressDialog.getWindow().setFlags(8, 8);
        this.mDialog.setMessage(getString(C0380R.string.reconnecting));
        ((Button) findViewById(C0380R.id.cancelButton)).setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.DeviceScanActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DeviceScanActivity.this.scanLeDevice(false);
                DeviceScanActivity.this.setResult(0, new Intent());
                DeviceScanActivity.this.finish();
            }
        });
        if (!getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            Toast.makeText(this, C0380R.string.ble_is_unsupported_with_this_device, 0).show();
            setResult(0, new Intent());
            finish();
        }
        bindService(new Intent(this, (Class<?>) CommService.class), this.mServiceConnection, 1);
        BluetoothAdapter adapter = ((BluetoothManager) getSystemService("bluetooth")).getAdapter();
        this.mBluetoothAdapter = adapter;
        if (adapter == null) {
            Toast.makeText(this, C0380R.string.ble_is_unsupported_with_this_device, 0).show();
            setResult(0, new Intent());
            finish();
        }
        if (Build.VERSION.SDK_INT >= 21) {
            this.scanCallback = new ScanCallback() { // from class: com.airliftcompany.alp3.DeviceScanActivity.3
                @Override // android.bluetooth.le.ScanCallback
                public void onScanResult(int i, ScanResult scanResult) {
                    super.onScanResult(i, scanResult);
                    if (Build.VERSION.SDK_INT >= 21) {
                        DeviceScanActivity.this.mLeDeviceListAdapter.addDevice(scanResult.getDevice());
                        DeviceScanActivity.this.mLeDeviceListAdapter.notifyDataSetChanged();
                    }
                }
            };
        }
        BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
        if (bluetoothAdapter != null) {
            bluetoothAdapter.startDiscovery();
        }
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        if (!this.mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
        }
        LeDeviceListAdapter leDeviceListAdapter = new LeDeviceListAdapter();
        this.mLeDeviceListAdapter = leDeviceListAdapter;
        setListAdapter(leDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        this.mLeDeviceListAdapter.clear();
    }

    @Override // android.app.ListActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        ProgressDialog progressDialog = this.mDialog;
        if (progressDialog != null && progressDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        unbindService(this.mServiceConnection);
        this.mCommService = null;
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(C0380R.menu.progress_menu, menu);
        menu.findItem(C0380R.id.menu_spinner).setActionView(C0380R.layout.indeterminate_progress);
        return true;
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 1 && iArr[0] != 0) {
            runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.DeviceScanActivity.5
                @Override // java.lang.Runnable
                public void run() {
                    new AlertDialog.Builder(DeviceScanActivity.this).setTitle(DeviceScanActivity.this.getString(C0380R.string.Error)).setMessage(DeviceScanActivity.this.getString(C0380R.string.this_app_will_be_unable_to_communicate_with_the_manifold)).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.DeviceScanActivity.5.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i2) {
                            dialogInterface.cancel();
                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert).show();
                }
            });
        }
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1 && i2 == 0) {
            this.mCommService.setCommServiceListener(null);
            setResult(0, new Intent());
            finish();
            return;
        }
        super.onActivityResult(i, i2, intent);
    }

    @Override // android.app.ListActivity
    protected void onListItemClick(ListView listView, View view, int i, long j) {
        BluetoothDevice device = this.mLeDeviceListAdapter.getDevice(i);
        if (device == null || this.mDialog.isShowing()) {
            return;
        }
        Log.i(TAG, "User selected device: " + device.getAddress());
        scanLeDevice(false);
        this.mBluetoothAdapter.cancelDiscovery();
        if (device.getAddress().length() > 0) {
            this.mCommService.bleService.connectionRetryAttempts = 0;
            this.mCommService.bleService.amConnecting = false;
            this.deviceAddress = device.getAddress();
            this.mCommService.bleService.connect(this.deviceAddress);
            this.mDialog.setMessage(getString(C0380R.string.connecting));
            this.mDialog.setCancelable(true);
            this.mDialog.show();
            return;
        }
        Toast.makeText(this, "Error - device address invalid", 1).show();
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onBleInitialized() {
        Log.i(TAG, "onAuthorizationFailed");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onBleInitializationFailed() {
        Log.i(TAG, "onBleInitializationFailed");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onAuthorizationFailed() {
        Log.i(TAG, "onAuthorizationFailed");
        runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.DeviceScanActivity.7
            @Override // java.lang.Runnable
            public void run() {
                if (DeviceScanActivity.this.mDialog != null && DeviceScanActivity.this.mDialog.isShowing()) {
                    DeviceScanActivity.this.mDialog.dismiss();
                }
                DeviceScanActivity.this.mCommService.setCommServiceListener(null);
                Intent intent = new Intent(DeviceScanActivity.this, (Class<?>) DevicePairActivity.class);
                intent.putExtra("deviceAddress", DeviceScanActivity.this.deviceAddress);
                DeviceScanActivity.this.finish();
                DeviceScanActivity.this.startActivity(intent);
            }
        });
    }

    /* renamed from: com.airliftcompany.alp3.DeviceScanActivity$8 */
    class RunnableC03408 implements Runnable {
        RunnableC03408() {
        }

        @Override // java.lang.Runnable
        public void run() {
            DeviceScanActivity.this.mDialog.dismiss();
            if (DeviceScanActivity.this.mCommService.commStatus.DeviceAuthorized != CommService.DeviceAuthorizedEnum.DeviceIsInBootloader) {
                if (DeviceScanActivity.this.mDialog != null && DeviceScanActivity.this.mDialog.isShowing()) {
                    DeviceScanActivity.this.mDialog.dismiss();
                }
                Toast.makeText(DeviceScanActivity.this, "Error connecting to manifold", 1).show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(DeviceScanActivity.this);
            builder.setMessage(DeviceScanActivity.this.getString(C0380R.string.detected_unit_without_firmware_program_firmware_now_an_internet_connection_is_required));
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.DeviceScanActivity.8.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.i(DeviceScanActivity.TAG, "Start firmware download");
                    new FirmwareVersionDownloadUtility().checkForFirmwareUpdate(DeviceScanActivity.this, DeviceScanActivity.this.mCommService, new FirmwareVersionDownloadUtility.DownloadFirmwareListener() { // from class: com.airliftcompany.alp3.DeviceScanActivity.8.1.1
                        @Override // com.airliftcompany.alp3.firmware.FirmwareVersionDownloadUtility.DownloadFirmwareListener
                        public void onTaskProgressUpdate(Integer num) {
                        }

                        @Override // com.airliftcompany.alp3.firmware.FirmwareVersionDownloadUtility.DownloadFirmwareListener
                        public void onTaskCompleted(Boolean bool, Boolean bool2, JSONObject jSONObject, Exception exc) {
                            if (!bool.booleanValue()) {
                                Log.i(DeviceScanActivity.TAG, "Failed firmware download");
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(DeviceScanActivity.this);
                                builder2.setTitle(DeviceScanActivity.this.getString(C0380R.string.Error));
                                builder2.setMessage(DeviceScanActivity.this.getString(C0380R.string.error_downloading_firmware_from_internet));
                                builder2.setNeutralButton("OK", new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.DeviceScanActivity.8.1.1.1
                                    @Override // android.content.DialogInterface.OnClickListener
                                    public void onClick(DialogInterface dialogInterface2, int i2) {
                                        dialogInterface2.dismiss();
                                    }
                                });
                                builder2.create().show();
                                return;
                            }
                            Log.i(DeviceScanActivity.TAG, "Firmware download success");
                            Intent intent = new Intent(DeviceScanActivity.this, (Class<?>) FirmwareUpdateActivity.class);
                            intent.putExtra("firmwareJSON", jSONObject.toString());
                            DeviceScanActivity.this.startActivity(intent);
                        }
                    });
                    if (DeviceScanActivity.this.mDialog == null || !DeviceScanActivity.this.mDialog.isShowing()) {
                        return;
                    }
                    DeviceScanActivity.this.mDialog.dismiss();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.DeviceScanActivity.8.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onConnectionFailed() {
        Log.i(TAG, "onConnectionFailed");
        runOnUiThread(new RunnableC03408());
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onAuthorized() {
        Log.i(TAG, "onAuthorized");
        runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.DeviceScanActivity.9
            @Override // java.lang.Runnable
            public void run() {
                if (Util.isChinese()) {
                    return;
                }
                ALP3Preferences.setDeviceAddress(DeviceScanActivity.this.mCommService.bleService.mBluetoothDeviceAddress, DeviceScanActivity.this.getApplicationContext());
                if (DeviceScanActivity.this.mDialog != null && DeviceScanActivity.this.mDialog.isShowing()) {
                    DeviceScanActivity.this.mDialog.dismiss();
                }
                DeviceScanActivity.this.mCommService.setCommServiceListener(null);
                Toast.makeText(DeviceScanActivity.this, "Connected", 0).show();
                DeviceScanActivity.this.finish();
            }
        });
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onCalibrationUpdated() {
        Log.i(TAG, "onCalibrationUpdated");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onSettingsUpdated() {
        Log.i(TAG, "onSettingsUpdated");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onVersionUpdated() {
        Log.i(TAG, "onVersionUpdated");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onSerialUpdated() {
        CognitoService.getInstance().checkDeviceAuthorization(this.mCommService.alp3Device.Serial, this.mCommService.alp3Device.MacAddress, this, new CognitoService.CallbackInterface() { // from class: com.airliftcompany.alp3.DeviceScanActivity.10
            @Override // com.airliftcompany.alp3.utils.CognitoService.CallbackInterface
            public void completeCallback(final CognitoService.AuthResponse authResponse) {
                DeviceScanActivity.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.DeviceScanActivity.10.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (DeviceScanActivity.this.mDialog != null && DeviceScanActivity.this.mDialog.isShowing()) {
                            DeviceScanActivity.this.mDialog.dismiss();
                        }
                        int i = C033312.f26x9eda8419[authResponse.ordinal()];
                        if (i == 1) {
                            ALP3Preferences.setAuthorized(true, DeviceScanActivity.this.getApplicationContext());
                            DeviceScanActivity.this.mCommService.setCommServiceListener(null);
                            ALP3Preferences.setDeviceAddress(DeviceScanActivity.this.deviceAddress, DeviceScanActivity.this.getApplicationContext());
                            Toast.makeText(DeviceScanActivity.this, "Connected", 0).show();
                            DeviceScanActivity.this.finish();
                            return;
                        }
                        if (i == 2) {
                            ALP3Preferences.setAuthorized(false, DeviceScanActivity.this.getApplicationContext());
                            Util.displayAlert(DeviceScanActivity.this.getString(C0380R.string.device_has_not_been_authorized), DeviceScanActivity.this);
                            DeviceScanActivity.this.mCommService.bleService.mBluetoothDeviceAddress = null;
                            DeviceScanActivity.this.mCommService.disconnectBleSession();
                            return;
                        }
                        ALP3Preferences.setAuthorized(false, DeviceScanActivity.this.getApplicationContext());
                        Util.displayAlert(DeviceScanActivity.this.getString(C0380R.string.error_checking_device_authorization), DeviceScanActivity.this);
                        DeviceScanActivity.this.mCommService.bleService.mBluetoothDeviceAddress = null;
                        DeviceScanActivity.this.mCommService.disconnectBleSession();
                    }
                });
            }
        });
    }

    /* renamed from: com.airliftcompany.alp3.DeviceScanActivity$12 */
    static /* synthetic */ class C033312 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$utils$CognitoService$AuthResponse */
        static final /* synthetic */ int[] f26x9eda8419;

        static {
            int[] iArr = new int[CognitoService.AuthResponse.values().length];
            f26x9eda8419 = iArr;
            try {
                iArr[CognitoService.AuthResponse.AuthSuccess.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f26x9eda8419[CognitoService.AuthResponse.AccessDenied.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f26x9eda8419[CognitoService.AuthResponse.UnknownError.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scanLeDevice(boolean z) {
        if (z) {
            UUID.fromString(BleService.MLDP_PRIVATE_SERVICE);
            this.mScanning = true;
            if (Build.VERSION.SDK_INT >= 21) {
                this.mBluetoothAdapter.getBluetoothLeScanner().startScan(BleService.scanFilters(getApplicationContext()), new ScanSettings.Builder().setScanMode(2).build(), this.scanCallback);
                return;
            }
            this.mBluetoothAdapter.startLeScan(this.mLeScanCallback);
            return;
        }
        this.mScanning = false;
        this.mBluetoothAdapter.cancelDiscovery();
        if (Build.VERSION.SDK_INT >= 21) {
            this.mBluetoothAdapter.getBluetoothLeScanner().stopScan(this.scanCallback);
        } else {
            this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
        }
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private final LayoutInflater mInflator;
        private final ArrayList<BluetoothDevice> mLeDevices = new ArrayList<>();

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        public LeDeviceListAdapter() {
            this.mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice bluetoothDevice) {
            if (this.mLeDevices.contains(bluetoothDevice)) {
                return;
            }
            Log.i(DeviceScanActivity.TAG, "Discovered device: " + bluetoothDevice.toString() + bluetoothDevice.getName() + bluetoothDevice.getAddress());
            this.mLeDevices.add(bluetoothDevice);
        }

        public BluetoothDevice getDevice(int i) {
            return this.mLeDevices.get(i);
        }

        public void clear() {
            this.mLeDevices.clear();
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.mLeDevices.size();
        }

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            return this.mLeDevices.get(i);
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = this.mInflator.inflate(C0380R.layout.listitem_device, (ViewGroup) null);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) view.findViewById(C0380R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            BluetoothDevice bluetoothDevice = this.mLeDevices.get(i);
            String name = bluetoothDevice.getName();
            if (name != null) {
                name.trim();
                name = name.replaceAll("[^A-Za-z0-9()\\[\\]]", "");
            }
            if (name == null || name.length() == 0) {
                name = bluetoothDevice.getAddress();
            }
            if (name != null && name.length() > 0) {
                viewHolder.deviceName.setText(name);
            } else {
                viewHolder.deviceName.setText(C0380R.string.unknown_device);
            }
            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;

        ViewHolder() {
        }
    }
}
