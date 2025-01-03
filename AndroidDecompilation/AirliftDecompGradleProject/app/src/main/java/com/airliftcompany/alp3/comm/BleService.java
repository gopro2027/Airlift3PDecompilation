package com.airliftcompany.alp3.comm;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import com.airliftcompany.alp3.firmware.XcpFlashing.Exceptions.XcpTimeoutException;
import com.airliftcompany.alp3.utils.ALP3Preferences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/* loaded from: classes.dex */
public class BleService extends Service {
    public static final String ACTION_DATA_AVAILABLE;
    public static final String ACTION_DATA_WRITTEN;
    public static final String ACTION_GATT_CONNECTED;
    public static final String ACTION_GATT_DISCONNECTED;
    public static final String CHARACTERISTIC_NOTIFICATION_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static final int CONNECTION_RETRY_ATTEMPTS = 2;
    public static final String EXTRA_DATA;
    private static final String INTENT_PREFIX;
    private static final String MLDP_CONTROL_PRIVATE_CHAR = "00035b03-58e6-07dd-021a-08123a0003ff";
    public static final String MLDP_DATA_PRIVATE_CHAR = "00035b03-58e6-07dd-021a-08123a000301";
    public static final String MLDP_PRIVATE_SERVICE = "00035b03-58e6-07dd-021a-08123a000300";
    private static final String TAG = "BleService";
    private static final int TX_RETRY_COUNT = 3;
    public static final UUID UUID_CHARACTERISTIC_NOTIFICATION_CONFIG;
    public static final UUID UUID_MLDP_DATA_PRIVATE_CHARACTERISTIC;
    private boolean didWriteDescriptor;
    public BluetoothAdapter mBluetoothAdapter;
    public String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    public BluetoothGattCharacteristic mDataMDLP;
    private ScanCallback scanCallback;
    public final boolean AUTO_RECONNECT_FLAG = true;
    private final IBinder mBinder = new LocalBinder();
    public Boolean amConnecting = false;
    public Integer connectionRetryAttempts = 0;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() { // from class: com.airliftcompany.alp3.comm.BleService.1
        @Override // android.bluetooth.BluetoothGattCallback
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
            if (i2 == 2) {
                if (BleService.this.mBluetoothGatt != null) {
                    Log.i(BleService.TAG, "Connected to GATT server, starting service discovery");
                    BleService.this.mBluetoothGatt.discoverServices();
                    return;
                }
                return;
            }
            if (i2 == 1) {
                Log.i(BleService.TAG, "STATE_CONNECTING");
                return;
            }
            if (i2 == 3) {
                Log.i(BleService.TAG, "STATE_DISCONNECTING");
                return;
            }
            if (i2 == 0) {
                if (BleService.this.mDataMDLP != null) {
                    Log.e(BleService.TAG, "Client disconnected - cleaning up.");
                    BleService.this.disconnect();
                    if (BleService.this.mBluetoothDeviceAddress != null) {
                        BleService bleService = BleService.this;
                        bleService.startScanningForPairedDevice(bleService.mBluetoothDeviceAddress);
                    }
                }
                Log.i(BleService.TAG, "Disconnected from GATT server.");
                BleService.this.broadcastUpdate(BleService.ACTION_GATT_DISCONNECTED);
            }
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            if (i != 0) {
                Log.w(BleService.TAG, "onServicesDiscovered received: " + i);
                return;
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.airliftcompany.alp3.comm.BleService.1.1
                @Override // java.lang.Runnable
                public void run() {
                    BleService.this.findMldpGattService(BleService.this.getSupportedGattServices());
                }
            });
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            if (i == 0) {
                BleService.this.broadcastUpdate(BleService.ACTION_DATA_AVAILABLE, bluetoothGattCharacteristic);
                Log.d(BleService.TAG, "onCharacteristicRead success");
            } else {
                Log.d(BleService.TAG, "onCharacteristicRead failed");
            }
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            if (i == 0) {
                BleService.this.broadcastUpdate(BleService.ACTION_DATA_WRITTEN, bluetoothGattCharacteristic);
            }
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            BleService.this.broadcastUpdate(BleService.ACTION_DATA_AVAILABLE, bluetoothGattCharacteristic);
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            super.onDescriptorWrite(bluetoothGatt, bluetoothGattDescriptor, i);
            if (i == 0) {
                Log.d(BleService.TAG, "onDescriptorWrite success ");
                BleService.this.didWriteDescriptor = true;
                return;
            }
            Log.d(BleService.TAG, "onDescriptorWrite failed " + i);
        }
    };
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() { // from class: com.airliftcompany.alp3.comm.BleService.3
        @Override // android.bluetooth.BluetoothAdapter.LeScanCallback
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
            BleService.this.connectToDeviceIfPaired(bluetoothDevice);
        }
    };

    static {
        String name = BleService.class.getPackage().getName();
        INTENT_PREFIX = name;
        ACTION_GATT_CONNECTED = name + ".ACTION_GATT_CONNECTED";
        ACTION_GATT_DISCONNECTED = name + ".ACTION_GATT_DISCONNECTED";
        ACTION_DATA_AVAILABLE = name + ".ACTION_DATA_AVAILABLE";
        ACTION_DATA_WRITTEN = name + ".ACTION_DATA_WRITTEN";
        EXTRA_DATA = name + ".EXTRA_DATA";
        UUID_MLDP_DATA_PRIVATE_CHARACTERISTIC = UUID.fromString(MLDP_DATA_PRIVATE_CHAR);
        UUID_CHARACTERISTIC_NOTIFICATION_CONFIG = UUID.fromString(CHARACTERISTIC_NOTIFICATION_CONFIG);
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "BleService bound");
        return this.mBinder;
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "BleService unbound");
        disconnect();
        return super.onUnbind(intent);
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.i(TAG, "BleService destroyed");
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        BleService getService() {
            return BleService.this;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void broadcastUpdate(String str) {
        sendBroadcast(new Intent(str));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void broadcastUpdate(String str, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        Intent intent = new Intent(str);
        if (str.equals(ACTION_DATA_AVAILABLE) && UUID_MLDP_DATA_PRIVATE_CHARACTERISTIC.equals(bluetoothGattCharacteristic.getUuid())) {
            intent.putExtra(EXTRA_DATA, bluetoothGattCharacteristic.getValue());
        }
        sendBroadcast(intent);
    }

    public boolean initialize() {
        String str = TAG;
        Log.i(str, "initialize");
        if (this.mBluetoothManager == null) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService("bluetooth");
            this.mBluetoothManager = bluetoothManager;
            if (bluetoothManager == null) {
                Log.e(str, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        BluetoothAdapter adapter = this.mBluetoothManager.getAdapter();
        this.mBluetoothAdapter = adapter;
        if (adapter == null) {
            Log.e(str, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        if (Build.VERSION.SDK_INT < 21) {
            return true;
        }
        this.scanCallback = new ScanCallback() { // from class: com.airliftcompany.alp3.comm.BleService.2
            @Override // android.bluetooth.le.ScanCallback
            public void onScanResult(int i, ScanResult scanResult) {
                super.onScanResult(i, scanResult);
                if (Build.VERSION.SDK_INT >= 21) {
                    BleService.this.connectToDeviceIfPaired(scanResult.getDevice());
                }
            }
        };
        return true;
    }

    public boolean isAdapterOn() {
        return this.mBluetoothAdapter.isEnabled();
    }

    public void startScanningForPairedDevice(String str) {
        this.mBluetoothDeviceAddress = str;
        Log.i(TAG, "startScanningForPairedDevice");
        scanLeDevice(true);
    }

    public static List<ScanFilter> scanFilters(Context context) {
        if (!ALP3Preferences.bleFiltering(context).booleanValue() || Build.VERSION.SDK_INT < 21) {
            return null;
        }
        ScanFilter build = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID.fromString(MLDP_PRIVATE_SERVICE))).build();
        ArrayList arrayList = new ArrayList(1);
        arrayList.add(build);
        return arrayList;
    }

    private void scanLeDevice(boolean z) {
        if (this.mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (z) {
            this.amConnecting = false;
            Log.i(TAG, "Start Le Scan");
            if (Build.VERSION.SDK_INT >= 21) {
                BluetoothLeScanner bluetoothLeScanner = this.mBluetoothAdapter.getBluetoothLeScanner();
                ScanSettings build = new ScanSettings.Builder().setScanMode(2).build();
                if (bluetoothLeScanner != null) {
                    bluetoothLeScanner.flushPendingScanResults(this.scanCallback);
                    bluetoothLeScanner.stopScan(this.scanCallback);
                    bluetoothLeScanner.startScan(scanFilters(getApplicationContext()), build, this.scanCallback);
                    return;
                }
                return;
            }
            this.mBluetoothAdapter.startLeScan(this.mLeScanCallback);
            return;
        }
        Log.i(TAG, "Stop Le Scan");
        this.mBluetoothAdapter.cancelDiscovery();
        if (Build.VERSION.SDK_INT >= 21) {
            BluetoothLeScanner bluetoothLeScanner2 = this.mBluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothLeScanner2 != null) {
                bluetoothLeScanner2.flushPendingScanResults(this.scanCallback);
                bluetoothLeScanner2.stopScan(this.scanCallback);
                return;
            }
            return;
        }
        this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r4v3, types: [com.airliftcompany.alp3.comm.BleService$4] */
    public boolean connectToDeviceIfPaired(BluetoothDevice bluetoothDevice) {
        String str = TAG;
        Log.i(str, "Discovered Device: " + bluetoothDevice.getAddress());
        Log.i(str, "mBluetoothDeviceAddress: " + this.mBluetoothDeviceAddress);
        if (this.mBluetoothDeviceAddress == null) {
            scanLeDevice(false);
        }
        String str2 = this.mBluetoothDeviceAddress;
        if (str2 == null || str2.length() <= 0 || !bluetoothDevice.getAddress().equalsIgnoreCase(this.mBluetoothDeviceAddress)) {
            return false;
        }
        scanLeDevice(false);
        new Thread() { // from class: com.airliftcompany.alp3.comm.BleService.4
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                BleService bleService = BleService.this;
                bleService.connect(bleService.mBluetoothDeviceAddress);
            }
        }.start();
        return true;
    }

    public boolean connect(String str) {
        if (this.amConnecting.booleanValue()) {
            Log.w(TAG, "Aborted connection attempt because we were already attempting a connection.");
            return false;
        }
        BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
        if (bluetoothAdapter == null || str == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(str);
        if (remoteDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        this.amConnecting = true;
        this.mBluetoothGatt = remoteDevice.connectGatt(this, false, this.mGattCallback, 2);
        if (Build.VERSION.SDK_INT >= 21) {
            this.mBluetoothGatt.requestConnectionPriority(1);
        }
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null) {
            Log.w(TAG, "Failed to create a new connection.");
            return false;
        }
        bluetoothGatt.connect();
        Log.w(TAG, "Trying to create a new connection.");
        this.mBluetoothDeviceAddress = str;
        return true;
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null) {
            return null;
        }
        return bluetoothGatt.getServices();
    }

    public void disconnect() {
        scanLeDevice(false);
        try {
            if (this.mBluetoothGatt != null) {
                ClearNotifications();
                this.mBluetoothGatt.disconnect();
                this.mBluetoothGatt.close();
                this.mBluetoothGatt = null;
            }
            this.mDataMDLP = null;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void findMldpGattService(List<BluetoothGattService> list) {
        if (list == null) {
            Log.d(TAG, "findMldpGattService found no Services");
            return;
        }
        SystemClock.sleep(50L);
        this.mDataMDLP = null;
        for (BluetoothGattService bluetoothGattService : list) {
            if (bluetoothGattService.getUuid().toString().equals(MLDP_PRIVATE_SERVICE)) {
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()) {
                    String uuid = bluetoothGattCharacteristic.getUuid().toString();
                    if (uuid.equals(MLDP_DATA_PRIVATE_CHAR)) {
                        this.mDataMDLP = bluetoothGattCharacteristic;
                        Log.d(TAG, "Found MLDP data characteristics");
                    } else if (uuid.equals(MLDP_CONTROL_PRIVATE_CHAR)) {
                        Log.d(TAG, "Found MLDP control characteristics");
                    }
                    int properties = bluetoothGattCharacteristic.getProperties();
                    if ((properties & 16) > 0) {
                        Log.d(TAG, "findMldpGattService PROPERTY_NOTIFY");
                        this.didWriteDescriptor = false;
                        setCharacteristicNotification(bluetoothGattCharacteristic, true);
                        int i = 0;
                        while (!this.didWriteDescriptor) {
                            SystemClock.sleep(10L);
                            i++;
                            if (i > 20) {
                                this.didWriteDescriptor = true;
                            }
                        }
                    } else if ((properties & 32) > 0) {
                        Log.d(TAG, "findMldpGattService PROPERTY_INDICATE");
                        this.didWriteDescriptor = false;
                        setCharacteristicIndication(bluetoothGattCharacteristic, true);
                        int i2 = 0;
                        while (!this.didWriteDescriptor) {
                            SystemClock.sleep(10L);
                            i2++;
                            if (i2 > 20) {
                                this.didWriteDescriptor = true;
                            }
                        }
                    }
                    if ((properties & 2) > 0) {
                        Log.d(TAG, "findMldpGattService PROPERTY_READ");
                    }
                    if ((properties & 1) > 0) {
                        Log.d(TAG, "findMldpGattService PERMISSION_READ");
                    }
                    if ((properties & 4) > 0) {
                        Log.d(TAG, "findMldpGattService PROPERTY_WRITE_NO_RESPONSE");
                        bluetoothGattCharacteristic.setWriteType(1);
                    }
                    SystemClock.sleep(50L);
                }
            }
        }
        if (this.mDataMDLP == null) {
            Toast.makeText(this, "Error connecting to BLE device", 0).show();
            Log.d(TAG, "findMldpGattService found no MLDP service");
        } else {
            Log.d(TAG, "Connection ready to use");
            broadcastUpdate(ACTION_GATT_CONNECTED);
        }
        this.amConnecting = false;
    }

    public void readCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        BluetoothGatt bluetoothGatt;
        if (this.mBluetoothAdapter == null || (bluetoothGatt = this.mBluetoothGatt) == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
        } else {
            bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
        }
    }

    public void writeCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (this.mBluetoothAdapter == null || this.mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        int properties = bluetoothGattCharacteristic.getProperties();
        if ((properties & 8) == 0 && (properties & 4) == 0) {
            return;
        }
        if (this.mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic)) {
            Log.d(TAG, "writeCharacteristic successful");
        } else {
            Log.d(TAG, "writeCharacteristic failed");
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean z) {
        BluetoothGatt bluetoothGatt;
        if (this.mBluetoothAdapter == null || (bluetoothGatt = this.mBluetoothGatt) == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, z);
        BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(UUID_CHARACTERISTIC_NOTIFICATION_CONFIG);
        descriptor.setValue(z ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        boolean writeDescriptor = this.mBluetoothGatt.writeDescriptor(descriptor);
        Log.d(TAG, "Success: " + writeDescriptor);
    }

    public void setCharacteristicIndication(BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean z) {
        BluetoothGatt bluetoothGatt;
        if (this.mBluetoothAdapter == null || (bluetoothGatt = this.mBluetoothGatt) == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, z);
        BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(UUID_CHARACTERISTIC_NOTIFICATION_CONFIG);
        descriptor.setValue(z ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        boolean writeDescriptor = this.mBluetoothGatt.writeDescriptor(descriptor);
        Log.d(TAG, "Success: " + writeDescriptor);
    }

    public void ClearNotifications() {
        try {
            BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
            if (bluetoothGatt == null) {
                return;
            }
            Iterator<BluetoothGattService> it = bluetoothGatt.getServices().iterator();
            while (it.hasNext()) {
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : it.next().getCharacteristics()) {
                    if ((bluetoothGattCharacteristic.getProperties() & 16) != 0) {
                        boolean characteristicNotification = this.mBluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, false);
                        Log.d(TAG, "result: " + characteristicNotification);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ex: ClearNotifications" + e.toString());
            e.printStackTrace();
        }
    }

    public void writeValue(short[] sArr) {
        if (this.mDataMDLP == null || this.mBluetoothGatt == null) {
            return;
        }
        byte[] bArr = new byte[sArr.length];
        for (int i = 0; i < sArr.length; i++) {
            bArr[i] = (byte) sArr[i];
        }
        try {
            writeValue(bArr, 50);
        } catch (XcpTimeoutException e) {
            Log.e(TAG, "BLE error sending data packet.");
            e.printStackTrace();
        }
    }

    public void writeValue(byte[] bArr, int i) throws XcpTimeoutException {
        long currentTimeMillis = System.currentTimeMillis();
        while (true) {
            if (this.mDataMDLP != null && this.mBluetoothGatt != null) {
                int i2 = 0;
                while (i2 < bArr.length) {
                    int min = Math.min(bArr.length - i2, 20) + i2;
                    byte[] copyOfRange = Arrays.copyOfRange(bArr, i2, min);
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = this.mDataMDLP;
                    if (bluetoothGattCharacteristic == null || this.mBluetoothGatt == null) {
                        throw new XcpTimeoutException("Could not write data, no bluetooth connection.");
                    }
                    if (bluetoothGattCharacteristic.setValue(copyOfRange)) {
                        if (this.mBluetoothGatt.writeCharacteristic(this.mDataMDLP)) {
                            i2 = min;
                        } else {
                            if (System.currentTimeMillis() - currentTimeMillis > i) {
                                throw new XcpTimeoutException("Could not write data.");
                            }
                            SystemClock.sleep(1L);
                        }
                    } else {
                        Log.i(TAG, "Error: setValue!");
                        throw new XcpTimeoutException("Could not write data, failed to set value/");
                    }
                }
                return;
            }
            if (System.currentTimeMillis() - currentTimeMillis > i) {
                throw new XcpTimeoutException("Could not write data, no bluetooth connection.");
            }
            SystemClock.sleep(100L);
        }
    }
}
