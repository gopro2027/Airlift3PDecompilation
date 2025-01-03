package com.airliftcompany.alp3.comm;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import com.airliftcompany.alp3.comm.ALP3Device;
import com.airliftcompany.alp3.comm.ALP3Protocol;
import com.airliftcompany.alp3.comm.BleService;
import com.airliftcompany.alp3.utils.ALP3Preferences;
import com.google.common.primitives.UnsignedBytes;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

/* loaded from: classes.dex */
public class CommService extends Service {
    private static final int COM_BLE_CONNECT_DELAY_MS = 150;
    private static final int COM_STATUS_UPDATE_RATE_MS = 250;
    private static final int COM_SYSTEM_TIMING_MS = 10;
    private static final String TAG = "CommService";
    public boolean RXThreadRunning;
    public boolean TXThreadRunning;
    private Timer TXTimer;
    public ALP3Device alp3Device;
    public boolean applicationCommOff;
    public BleService bleService;
    public com100msEnum com100msState;
    public CommServiceListener commServiceListener;
    public RXController rxController;
    public boolean rxPacketProcessingOff;
    public boolean sendControllerStatus;
    public boolean sendManualMode;
    public TXController txController;
    private final IBinder mBinder = new LocalBinder();
    public CommStatus commStatus = new CommStatus();
    public CANPNG65400UIStatus canpng65400UIStatus = new CANPNG65400UIStatus();
    private Thread TXThread = new Thread() { // from class: com.airliftcompany.alp3.comm.CommService.1
        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (CommService.this.TXThreadRunning) {
                try {
                    if (!CommService.this.applicationCommOff && CommService.this.commStatus.BlueToothAccConnected) {
                        if (CommService.this.sendControllerStatus) {
                            CommService.this.sendControllerStatus = false;
                            CommService.this.txController.txControllerStatus();
                        }
                        if (CommService.this.sendManualMode) {
                            CommService.this.sendManualMode = false;
                            CommService.this.txController.txManualMode();
                        }
                        if (System.currentTimeMillis() - CommService.this.commStatus.lastStatusTimeMs > 250) {
                            CommService.this.commStatus.lastStatusTimeMs = System.currentTimeMillis();
                            CommService.this.txController.txHandler();
                        }
                    }
                    sleep(1L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    };
    private Thread RXThread = new Thread() { // from class: com.airliftcompany.alp3.comm.CommService.2
        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (CommService.this.RXThreadRunning) {
                if (!CommService.this.applicationCommOff && CommService.this.commStatus.BlueToothAccConnected) {
                    try {
                        Short take = CommService.this.AppRawDataBuffer.take();
                        if (take != null) {
                            CommService.this.rxController.processNewChar(take.shortValue());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
    private TimerTask TXTimerTask = new TimerTask() { // from class: com.airliftcompany.alp3.comm.CommService.3
        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            CommService.this.commStatus.WaitTick += 10;
            if (!CommService.this.applicationCommOff && CommService.this.commStatus.DeviceAuthorized == DeviceAuthorizedEnum.DeviceIsAuthorized) {
                CommStatus commStatus = CommService.this.commStatus;
                int i = commStatus.RxTimeoutTick;
                commStatus.RxTimeoutTick = i + 1;
                if (i > 400) {
                    Log.i(CommService.TAG, "BT Disconnect forced by application due to no received data");
                    CommService.this.disconnectBleSession();
                    CommService.this.commStatus.RxTimeoutTick = 0;
                    CommService.this.bleService.getClass();
                    CommService.this.bleService.startScanningForPairedDevice(ALP3Preferences.deviceAddress(CommService.this.getApplicationContext()));
                }
            }
            if (CommService.this.applicationCommOff) {
                CommService.this.commStatus.RxTimeoutTick = 0;
            }
            if (CommService.this.commStatus.ConnectedTick > 0) {
                CommStatus commStatus2 = CommService.this.commStatus;
                commStatus2.ConnectedTick--;
            } else {
                CommService.this.alp3Device.canpng65300ECUStatus.StateIsOK = (short) 255;
                CommService.this.alp3Device.canpng65300ECUStatus.StateIsPressure = (short) 255;
                CommService.this.alp3Device.canpng65300ECUStatus.StatePCcontrolled = (short) 255;
            }
        }
    };
    private final ServiceConnection mServiceConnection = new ServiceConnection() { // from class: com.airliftcompany.alp3.comm.CommService.4
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            CommService.this.bleService = ((BleService.LocalBinder) iBinder).getService();
            if (!CommService.this.bleService.initialize()) {
                Log.e(CommService.TAG, "Unable to initialize Bluetooth");
                if (CommService.this.commServiceListener != null) {
                    CommService.this.commServiceListener.onBleInitializationFailed();
                    return;
                }
                return;
            }
            Log.i(CommService.TAG, "Bluetooth Initialized");
            if (CommService.this.commServiceListener != null) {
                CommService.this.commServiceListener.onBleInitialized();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            CommService.this.bleService = null;
        }
    };
    public ArrayBlockingQueue<Byte> RawDataBuffer = new ArrayBlockingQueue<>(2056);
    public ArrayBlockingQueue<Short> AppRawDataBuffer = new ArrayBlockingQueue<>(2056);
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() { // from class: com.airliftcompany.alp3.comm.CommService.5
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            byte[] byteArrayExtra;
            String action = intent.getAction();
            if (CommService.this.applicationCommOff) {
                if (!BleService.ACTION_DATA_AVAILABLE.equals(action) || (byteArrayExtra = intent.getByteArrayExtra(BleService.EXTRA_DATA)) == null) {
                    return;
                }
                for (byte b : byteArrayExtra) {
                    CommService.this.RawDataBuffer.add(Byte.valueOf(b));
                }
                return;
            }
            if (BleService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.i(CommService.TAG, "Connection ready for use");
                new Handler().postDelayed(new Runnable() { // from class: com.airliftcompany.alp3.comm.CommService.5.1
                    @Override // java.lang.Runnable
                    public void run() {
                        CommService.this.commStatus.BlueToothAccConnected = true;
                    }
                }, 150L);
                return;
            }
            if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i(CommService.TAG, "ACTION_GATT_DISCONNECTED");
                CommService.this.clearDeviceFlags();
            } else {
                if (BleService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] byteArrayExtra2 = intent.getByteArrayExtra(BleService.EXTRA_DATA);
                    if (byteArrayExtra2 != null) {
                        for (byte b2 : byteArrayExtra2) {
                            CommService.this.AppRawDataBuffer.add(Short.valueOf((short) (b2 & UnsignedBytes.MAX_VALUE)));
                        }
                    }
                    CommService.this.commStatus.RxTimeoutTick = 0;
                    return;
                }
                BleService.ACTION_DATA_WRITTEN.equals(action);
            }
        }
    };

    public enum DeviceAuthorizedEnum {
        DeviceAuthorizedUnknown,
        DeviceIsAuthorized,
        DeviceUnauthorized,
        DeviceIsInBootloader,
        DeviceAuthorizationError
    }

    public enum com100msEnum {
        COM100mS_ECU_NOT_FOUND,
        COM100mS_SYNC,
        COM100mS_STATUS,
        COM100mS_CONTROL,
        COM100mS_PAUSE
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        return 1;
    }

    public void setCommServiceListener(CommServiceListener commServiceListener) {
        this.commServiceListener = commServiceListener;
    }

    public class CommStatus {
        public boolean BlueToothAccConnected;
        public int ConnectedTick;
        public DeviceAuthorizedEnum DeviceAuthorized;
        public int RxTimeoutTick;
        public int WaitTick;
        public boolean WeHaveControl;
        public long lastStatusTimeMs;

        public CommStatus() {
        }
    }

    public class CANPNG65400UIStatus {
        public short CompressorDisabled;
        public short CompressorForceOn;
        public short ControlMode;
        public short ControlSequence;
        public short Status;
        public short SubControlMode;
        public int TimeOutTick;

        public CANPNG65400UIStatus() {
        }

        public ALP3Protocol.ECU_PrimaryControlStateEnum controlStateEnum() {
            if (this.ControlMode >= ALP3Protocol.ECU_PrimaryControlStateEnum.values().length) {
                return ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_ERROR;
            }
            return ALP3Protocol.ECU_PrimaryControlStateEnum.values()[this.ControlMode];
        }
    }

    public CommService() {
        Log.i(TAG, TAG);
    }

    @Override // android.app.Service
    public void onCreate() {
        registerReceiver(this.mGattUpdateReceiver, makeGattUpdateIntentFilter());
        this.txController = new TXController(this);
        this.rxController = new RXController(this);
        this.alp3Device = new ALP3Device();
        clearDeviceFlags();
        bindService(new Intent(this, (Class<?>) BleService.class), this.mServiceConnection, 1);
        Timer timer = new Timer();
        this.TXTimer = timer;
        timer.scheduleAtFixedRate(this.TXTimerTask, 10L, 10L);
        this.TXThreadRunning = true;
        this.TXThread.setPriority(10);
        this.TXThread.start();
        this.RXThreadRunning = true;
        this.RXThread.setPriority(10);
        this.RXThread.start();
        super.onCreate();
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.i(TAG, "CommService destroyed");
        disconnectBleSession();
        unregisterReceiver(this.mGattUpdateReceiver);
        unbindService(this.mServiceConnection);
        stopService(new Intent(this, (Class<?>) BleService.class));
        this.bleService = null;
        this.TXTimer.cancel();
        this.TXThreadRunning = false;
        this.RXThreadRunning = false;
        super.onDestroy();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "CommService bound");
        return this.mBinder;
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "CommService unbound");
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        public CommService getService() {
            return CommService.this;
        }
    }

    public void openValve(short s, short s2, boolean z) {
        if (!this.alp3Device.canpng65404ManualControl.checkDirectionIsAllowed(s2)) {
            Log.e(TAG, "openValve request denied");
            return;
        }
        if (s == 4) {
            int i = C04286.f55xbea6593f[this.alp3Device.displaySettings.allUpButtonsEnum().ordinal()];
            if (i == 1) {
                this.alp3Device.canpng65404ManualControl.Spring[0] = 1;
                this.alp3Device.canpng65404ManualControl.Spring[1] = 1;
                this.alp3Device.canpng65404ManualControl.Spring[2] = 1;
                this.alp3Device.canpng65404ManualControl.Spring[3] = 1;
            } else if (i == 2) {
                this.alp3Device.canpng65404ManualControl.Spring[0] = 1;
                this.alp3Device.canpng65404ManualControl.Spring[1] = 1;
                this.alp3Device.canpng65404ManualControl.Spring[2] = 0;
                this.alp3Device.canpng65404ManualControl.Spring[3] = 0;
            } else if (i == 3) {
                Log.e(TAG, "Logic error in openValve");
            }
        } else if (s == 5) {
            int i2 = C04286.f54xa7143ad8[this.alp3Device.displaySettings.allDownButtonsEnum().ordinal()];
            if (i2 == 1) {
                this.alp3Device.canpng65404ManualControl.Spring[0] = 1;
                this.alp3Device.canpng65404ManualControl.Spring[1] = 1;
                this.alp3Device.canpng65404ManualControl.Spring[2] = 1;
                this.alp3Device.canpng65404ManualControl.Spring[3] = 1;
            } else if (i2 == 2) {
                this.alp3Device.canpng65404ManualControl.Spring[0] = 1;
                this.alp3Device.canpng65404ManualControl.Spring[1] = 1;
                this.alp3Device.canpng65404ManualControl.Spring[2] = 0;
                this.alp3Device.canpng65404ManualControl.Spring[3] = 0;
            } else if (i2 == 3 || i2 == 4) {
                Log.e(TAG, "Logic error in openValve");
            }
        } else {
            this.alp3Device.canpng65404ManualControl.Spring[s] = 1;
        }
        if (this.canpng65400UIStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_HEIGHT.ordinal() && this.canpng65400UIStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_PRESSURE.ordinal() && this.canpng65400UIStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_MANUAL_CONTROL.ordinal()) {
            this.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_MANUAL_CONTROL.ordinal();
            CANPNG65400UIStatus cANPNG65400UIStatus = this.canpng65400UIStatus;
            cANPNG65400UIStatus.ControlSequence = (short) (cANPNG65400UIStatus.ControlSequence + 1);
            forceSendControllerStatus();
        }
        if (this.canpng65400UIStatus.ControlMode != ((short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_HEIGHT.ordinal()) && this.canpng65400UIStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_PRESSURE.ordinal()) {
            this.canpng65400UIStatus.SubControlMode = (short) 1;
        }
        if (this.alp3Device.canpng65404ManualControl.Direction != s2 && s2 > this.alp3Device.canpng65404ManualControl.Direction) {
            this.alp3Device.canpng65404ManualControl.Direction = s2;
            forceSendManualMode();
        }
        if (z) {
            forceSendManualMode();
            if (Build.VERSION.SDK_INT >= 21) {
                SystemClock.sleep(5L);
            } else {
                SystemClock.sleep(20L);
            }
        }
    }

    /* renamed from: com.airliftcompany.alp3.comm.CommService$6 */
    static /* synthetic */ class C04286 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$comm$ALP3Device$KeypadAllDownButtonsEnum */
        static final /* synthetic */ int[] f54xa7143ad8;

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$comm$ALP3Device$KeypadAllUpButtonsEnum */
        static final /* synthetic */ int[] f55xbea6593f;

        static {
            int[] iArr = new int[ALP3Device.KeypadAllDownButtonsEnum.values().length];
            f54xa7143ad8 = iArr;
            try {
                iArr[ALP3Device.KeypadAllDownButtonsEnum.KEYPAD_ALL_DOWN.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f54xa7143ad8[ALP3Device.KeypadAllDownButtonsEnum.KEYPAD_FRONT_DOWN.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f54xa7143ad8[ALP3Device.KeypadAllDownButtonsEnum.KEYPAD_ALL_DOWN_IS_PRESET.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f54xa7143ad8[ALP3Device.KeypadAllDownButtonsEnum.KEYPAD_AIR_OUT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            int[] iArr2 = new int[ALP3Device.KeypadAllUpButtonsEnum.values().length];
            f55xbea6593f = iArr2;
            try {
                iArr2[ALP3Device.KeypadAllUpButtonsEnum.KEYPAD_ALL_UP.ordinal()] = 1;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                f55xbea6593f[ALP3Device.KeypadAllUpButtonsEnum.KEYPAD_FRONT_UP.ordinal()] = 2;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                f55xbea6593f[ALP3Device.KeypadAllUpButtonsEnum.KEYPAD_ALL_UP_IS_PRESET.ordinal()] = 3;
            } catch (NoSuchFieldError unused7) {
            }
        }
    }

    public void closeValve(short s) {
        if (s == 4) {
            int i = C04286.f55xbea6593f[this.alp3Device.displaySettings.allUpButtonsEnum().ordinal()];
            if (i == 1 || i == 2) {
                this.alp3Device.canpng65404ManualControl.Spring[0] = 0;
                this.alp3Device.canpng65404ManualControl.Spring[1] = 0;
                this.alp3Device.canpng65404ManualControl.Spring[2] = 0;
                this.alp3Device.canpng65404ManualControl.Spring[3] = 0;
            } else if (i == 3) {
                Log.e(TAG, "Logic error in openValve");
            }
        } else if (s == 5) {
            int i2 = C04286.f54xa7143ad8[this.alp3Device.displaySettings.allDownButtonsEnum().ordinal()];
            if (i2 == 1 || i2 == 2) {
                this.alp3Device.canpng65404ManualControl.Spring[0] = 0;
                this.alp3Device.canpng65404ManualControl.Spring[1] = 0;
                this.alp3Device.canpng65404ManualControl.Spring[2] = 0;
                this.alp3Device.canpng65404ManualControl.Spring[3] = 0;
            } else if (i2 == 3 || i2 == 4) {
                Log.e(TAG, "Logic error in openValve");
            }
        } else {
            this.alp3Device.canpng65404ManualControl.Spring[s] = 0;
        }
        if (this.canpng65400UIStatus.ControlMode != ((short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_HEIGHT.ordinal()) && this.canpng65400UIStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_PRESSURE.ordinal()) {
            this.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_MANUAL_CONTROL.ordinal();
        }
        if (this.alp3Device.canpng65404ManualControl.valvesAreClosed()) {
            if (this.canpng65400UIStatus.ControlMode != ((short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_HEIGHT.ordinal()) && this.canpng65400UIStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_PRESSURE.ordinal()) {
                this.canpng65400UIStatus.SubControlMode = (short) 0;
            }
            this.alp3Device.canpng65404ManualControl.Direction = 0;
        }
        forceSendManualMode();
        if (Build.VERSION.SDK_INT >= 21) {
            SystemClock.sleep(5L);
        } else {
            SystemClock.sleep(20L);
        }
    }

    public void recallPreset(short s) {
        if (this.alp3Device.canpng65300ECUStatus.pressureMode()) {
            this.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_PRESET_PRESSURE.ordinal();
        } else {
            this.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_PRESET_HEIGHT.ordinal();
        }
        this.canpng65400UIStatus.SubControlMode = s;
        CANPNG65400UIStatus cANPNG65400UIStatus = this.canpng65400UIStatus;
        cANPNG65400UIStatus.ControlSequence = (short) (cANPNG65400UIStatus.ControlSequence + 1);
    }

    public void forceSendManualMode() {
        Log.i(TAG, "forceSendManualMode");
        this.sendManualMode = true;
    }

    public void forceSendControllerStatus() {
        Log.i(TAG, "forceSendControllerStatus");
        this.sendControllerStatus = true;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BleService.ACTION_DATA_WRITTEN);
        return intentFilter;
    }

    public void disconnectBleSession() {
        Log.i(TAG, "disconnectBleSession()");
        clearDeviceFlags();
        try {
            BleService bleService = this.bleService;
            if (bleService != null) {
                bleService.disconnect();
                this.bleService.mBluetoothDeviceAddress = null;
            }
            CommServiceListener commServiceListener = this.commServiceListener;
            if (commServiceListener != null) {
                commServiceListener.onStatusUpdated();
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    public void clearDeviceFlags() {
        Log.i(TAG, "clearDeviceFlags()");
        this.alp3Device.versionMismatch = false;
        this.alp3Device.versionChecked = false;
        this.alp3Device.haveMacAndSerial = false;
        this.commStatus.ConnectedTick = 0;
        this.commStatus.BlueToothAccConnected = false;
        this.com100msState = com100msEnum.COM100mS_ECU_NOT_FOUND;
        this.commStatus.DeviceAuthorized = DeviceAuthorizedEnum.DeviceAuthorizedUnknown;
        this.alp3Device.presets.Synced = false;
        this.alp3Device.displaySettings.Synced = false;
        this.alp3Device.calibrationSettings.Synced = false;
        this.commStatus.ConnectedTick = 0;
        this.alp3Device.displaySettings.ShowMode = false;
    }
}
