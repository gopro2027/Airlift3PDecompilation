package com.airliftcompany.alp3.comm;

import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import com.airliftcompany.alp3.comm.ALP3Protocol;
import com.airliftcompany.alp3.comm.CommService;
import com.airliftcompany.alp3.firmware.ALP3Programmer;
import java.nio.charset.StandardCharsets;

/* loaded from: classes.dex */
public class TXController {
    private static final int COM_REPLY_TIME = 550;
    private static final String TAG = "TXController";
    private CommService mCommService;

    public interface AsyncTaskListener {
        void onTaskCompleted(Boolean bool);

        void onTaskProgressUpdate(Integer num);
    }

    public TXController(CommService commService) {
        this.mCommService = commService;
    }

    public static class WriteVariableAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        private AsyncTaskListener listener;
        private short recordID;
        private TXController txController;
        private long value;
        private short variableID;

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
        }

        public WriteVariableAsyncTask(TXController tXController, short s, short s2, long j, AsyncTaskListener asyncTaskListener) {
            this.txController = tXController;
            this.recordID = s;
            this.variableID = s2;
            this.value = j;
            this.listener = asyncTaskListener;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Boolean doInBackground(Void... voidArr) {
            publishProgress(25);
            if (!this.txController.txWriteVariable(this.recordID, this.variableID, this.value)) {
                return false;
            }
            publishProgress(100);
            return true;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Integer... numArr) {
            this.listener.onTaskProgressUpdate(numArr[0]);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Boolean bool) {
            this.listener.onTaskCompleted(bool);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onCancelled(Boolean bool) {
            this.listener.onTaskCompleted(bool);
        }
    }

    public static class FactoryResetAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        private AsyncTaskListener listener;
        private TXController txController;

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
        }

        public FactoryResetAsyncTask(TXController tXController, AsyncTaskListener asyncTaskListener) {
            this.txController = tXController;
            this.listener = asyncTaskListener;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Boolean doInBackground(Void... voidArr) {
            short[] sArr = {1, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 16};
            int i = 0;
            for (int i2 = 0; i2 < 14; i2++) {
                if (!this.txController.txWriteVariable(sArr[i2], (short) 0, 255L)) {
                    return false;
                }
                i += 7;
                publishProgress(Integer.valueOf(i));
            }
            SystemClock.sleep(5000L);
            return true;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Integer... numArr) {
            this.listener.onTaskProgressUpdate(numArr[0]);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Boolean bool) {
            this.listener.onTaskCompleted(bool);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onCancelled(Boolean bool) {
            this.listener.onTaskCompleted(bool);
        }
    }

    public void txHandler() {
        if (this.mCommService.alp3Device.versionMismatch) {
            txControllerStatus();
            return;
        }
        int i = C04301.$SwitchMap$com$airliftcompany$alp3$comm$CommService$com100msEnum[this.mCommService.com100msState.ordinal()];
        if (i == 1) {
            String str = TAG;
            Log.i(str, "COM100mS_ECU_NOT_FOUND");
            this.mCommService.canpng65400UIStatus.Status = (short) 0;
            this.mCommService.canpng65400UIStatus.ControlMode = (short) 255;
            this.mCommService.canpng65400UIStatus.SubControlMode = (short) 255;
            this.mCommService.canpng65400UIStatus.ControlSequence = (short) 0;
            if (txCheckAuthorization()) {
                if (this.mCommService.commStatus.DeviceAuthorized == CommService.DeviceAuthorizedEnum.DeviceUnauthorized) {
                    if (this.mCommService.commServiceListener != null) {
                        this.mCommService.commServiceListener.onAuthorizationFailed();
                    }
                    this.mCommService.disconnectBleSession();
                } else {
                    if (this.mCommService.commServiceListener != null) {
                        this.mCommService.commServiceListener.onAuthorized();
                    }
                    this.mCommService.com100msState = CommService.com100msEnum.COM100mS_SYNC;
                }
            } else {
                BleService bleService = this.mCommService.bleService;
                Integer valueOf = Integer.valueOf(bleService.connectionRetryAttempts.intValue() + 1);
                bleService.connectionRetryAttempts = valueOf;
                if (valueOf.intValue() > 2) {
                    this.mCommService.bleService.disconnect();
                    this.mCommService.clearDeviceFlags();
                    this.mCommService.com100msState = CommService.com100msEnum.COM100mS_PAUSE;
                    this.mCommService.commStatus.DeviceAuthorized = CommService.DeviceAuthorizedEnum.DeviceAuthorizationError;
                    this.mCommService.commServiceListener.onConnectionFailed();
                    return;
                }
                if (ALP3Programmer.CheckBootloader(this.mCommService)) {
                    this.mCommService.com100msState = CommService.com100msEnum.COM100mS_PAUSE;
                    this.mCommService.commStatus.DeviceAuthorized = CommService.DeviceAuthorizedEnum.DeviceIsInBootloader;
                    this.mCommService.commServiceListener.onConnectionFailed();
                } else {
                    SystemClock.sleep(3000L);
                    if (Build.VERSION.SDK_INT < 21) {
                        this.mCommService.bleService.mBluetoothAdapter.disable();
                        SystemClock.sleep(1000L);
                        this.mCommService.bleService.mBluetoothAdapter.enable();
                        SystemClock.sleep(500L);
                    }
                    CommService commService = this.mCommService;
                    if (commService == null || commService.bleService == null) {
                        return;
                    }
                    String str2 = this.mCommService.bleService.mBluetoothDeviceAddress;
                    this.mCommService.bleService.disconnect();
                    this.mCommService.clearDeviceFlags();
                    SystemClock.sleep(1000L);
                    if (this.mCommService.bleService != null) {
                        this.mCommService.bleService.getClass();
                        Log.i(str, "TXController reconnection request");
                        this.mCommService.bleService.startScanningForPairedDevice(str2);
                    }
                }
            }
            if (this.mCommService.commServiceListener != null) {
                this.mCommService.commServiceListener.onStatusUpdated();
                return;
            }
            return;
        }
        if (i == 2) {
            String str3 = TAG;
            Log.i(str3, "COM100mS_SYNC");
            if (this.mCommService.canpng65400UIStatus.Status == 16) {
                if (!this.mCommService.alp3Device.canpng65300ECUStatus.overrideCalibration && this.mCommService.alp3Device.canpng65300ECUStatus.ControlMode >= ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_LIMITS.ordinal() && this.mCommService.alp3Device.canpng65300ECUStatus.ControlMode <= ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_PRESSURE.ordinal()) {
                    this.mCommService.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATION_IN_PROGRESS.ordinal();
                    this.mCommService.canpng65400UIStatus.SubControlMode = (short) 0;
                    this.mCommService.canpng65400UIStatus.ControlSequence = (short) 0;
                    txControllerStatus();
                    Log.i(str3, "Detected calibration mode");
                    return;
                }
                this.mCommService.canpng65400UIStatus.ControlMode = this.mCommService.alp3Device.canpng65300ECUStatus.ControlMode;
                this.mCommService.canpng65400UIStatus.SubControlMode = this.mCommService.alp3Device.canpng65300ECUStatus.SubControlMode;
                this.mCommService.canpng65400UIStatus.ControlSequence = this.mCommService.alp3Device.canpng65300ECUStatus.ECUControlSequence;
                txControllerStatus();
                if (this.mCommService.commStatus.WeHaveControl) {
                    this.mCommService.com100msState = CommService.com100msEnum.COM100mS_STATUS;
                    return;
                }
                return;
            }
            return;
        }
        if (i != 3) {
            if (i != 4) {
                if (i != 5) {
                    this.mCommService.com100msState = CommService.com100msEnum.COM100mS_STATUS;
                    return;
                }
                return;
            }
            if (!this.mCommService.alp3Device.presets.Synced && this.mCommService.commStatus.BlueToothAccConnected) {
                syncPresets();
            }
            int i2 = C04301.f56xc2ea46ec[this.mCommService.canpng65400UIStatus.controlStateEnum().ordinal()];
            if (i2 == 3 || i2 == 4 || i2 == 5 || i2 == 6) {
                txManualMode();
            }
            this.mCommService.com100msState = CommService.com100msEnum.COM100mS_STATUS;
            return;
        }
        if (this.mCommService.alp3Device.canpng65300ECUStatus.overrideCalibration) {
            this.mCommService.alp3Device.canpng65300ECUStatus.overrideCalibration = false;
            this.mCommService.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_MANUAL_CONTROL.ordinal();
            this.mCommService.canpng65400UIStatus.SubControlMode = (short) 0;
        }
        txControllerStatus();
        if (!this.mCommService.alp3Device.displaySettings.Synced && this.mCommService.commStatus.BlueToothAccConnected) {
            if (!syncSettings().booleanValue()) {
                return;
            } else {
                txControllerStatus();
            }
        }
        if (!this.mCommService.alp3Device.presets.Synced && this.mCommService.commStatus.BlueToothAccConnected) {
            if (!syncPresets().booleanValue()) {
                return;
            } else {
                txControllerStatus();
            }
        }
        if (!this.mCommService.alp3Device.versionChecked && this.mCommService.commStatus.BlueToothAccConnected) {
            if (!checkVersion()) {
                return;
            } else {
                txControllerStatus();
            }
        }
        if (!this.mCommService.alp3Device.calibrationSettings.Synced && this.mCommService.commStatus.BlueToothAccConnected) {
            if (!syncCalibration().booleanValue()) {
                return;
            } else {
                txControllerStatus();
            }
        }
        if (!this.mCommService.alp3Device.haveMacAndSerial && this.mCommService.commStatus.BlueToothAccConnected) {
            if (!syncMacAndSerial()) {
                return;
            } else {
                txControllerStatus();
            }
        }
        this.mCommService.com100msState = CommService.com100msEnum.COM100mS_CONTROL;
    }

    /* renamed from: com.airliftcompany.alp3.comm.TXController$1 */
    static /* synthetic */ class C04301 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$comm$ALP3Protocol$ECU_PrimaryControlStateEnum */
        static final /* synthetic */ int[] f56xc2ea46ec;
        static final /* synthetic */ int[] $SwitchMap$com$airliftcompany$alp3$comm$CommService$com100msEnum;

        static {
            int[] iArr = new int[CommService.com100msEnum.values().length];
            $SwitchMap$com$airliftcompany$alp3$comm$CommService$com100msEnum = iArr;
            try {
                iArr[CommService.com100msEnum.COM100mS_ECU_NOT_FOUND.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$airliftcompany$alp3$comm$CommService$com100msEnum[CommService.com100msEnum.COM100mS_SYNC.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$airliftcompany$alp3$comm$CommService$com100msEnum[CommService.com100msEnum.COM100mS_STATUS.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$airliftcompany$alp3$comm$CommService$com100msEnum[CommService.com100msEnum.COM100mS_CONTROL.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$airliftcompany$alp3$comm$CommService$com100msEnum[CommService.com100msEnum.COM100mS_PAUSE.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            int[] iArr2 = new int[ALP3Protocol.ECU_PrimaryControlStateEnum.values().length];
            f56xc2ea46ec = iArr2;
            try {
                iArr2[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_PRESET_HEIGHT.ordinal()] = 1;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                f56xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_DIRECT_CONTROL.ordinal()] = 2;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                f56xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_MANUAL_CONTROL.ordinal()] = 3;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                f56xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_SHOW_MODE.ordinal()] = 4;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                f56xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_HEIGHT.ordinal()] = 5;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                f56xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_PRESSURE.ordinal()] = 6;
            } catch (NoSuchFieldError unused11) {
            }
        }
    }

    private Boolean syncSettings() {
        Log.w(TAG, "Starting Settings Download");
        long[] txGetMemorySector = txGetMemorySector(12);
        if (txGetMemorySector == null) {
            return false;
        }
        this.mCommService.alp3Device.displaySettings.Rotation = (int) txGetMemorySector[6];
        this.mCommService.alp3Device.displaySettings.Units = (int) txGetMemorySector[7];
        this.mCommService.alp3Device.displaySettings.AllUp = (int) txGetMemorySector[8];
        this.mCommService.alp3Device.displaySettings.AllDown = (int) txGetMemorySector[9];
        this.mCommService.alp3Device.displaySettings.RefreshRate = (int) txGetMemorySector[10];
        this.mCommService.alp3Device.displaySettings.PresetMode = (int) txGetMemorySector[12];
        for (byte b = 0; b < 4; b = (byte) (b + 1)) {
            this.mCommService.alp3Device.canpng65301ECUSpringPressure.filter32_tauX_PreData(b);
        }
        for (int i = 0; i < 4; i++) {
            if (!txGetRecord(1, i + 14)) {
                return false;
            }
            this.mCommService.alp3Device.displaySettings.SensorIsInverted[i] = (int) this.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
        }
        this.mCommService.alp3Device.displaySettings.Synced = true;
        if (this.mCommService.commServiceListener != null) {
            this.mCommService.commServiceListener.onSettingsUpdated();
        }
        Log.w(TAG, "Settings Downloaded");
        return true;
    }

    private Boolean syncPresets() {
        Log.w(TAG, "Starting Presets Download");
        long[] txGetMemorySector = txGetMemorySector(6);
        if (txGetMemorySector == null) {
            return false;
        }
        if (txGetMemorySector.length >= 20) {
            for (int i = 0; i < 20; i++) {
                this.mCommService.alp3Device.presets.Height[i] = (int) txGetMemorySector[r6];
            }
        }
        txControllerStatus();
        long[] txGetMemorySector2 = txGetMemorySector(8);
        if (txGetMemorySector2 == null) {
            return false;
        }
        if (txGetMemorySector2.length >= 20) {
            for (int i2 = 0; i2 < 20; i2++) {
                this.mCommService.alp3Device.presets.Pressure[i2] = (int) txGetMemorySector2[r3];
            }
        }
        Log.w(TAG, "Presets Downloaded");
        this.mCommService.alp3Device.presets.Synced = true;
        return true;
    }

    /* JADX WARN: Code restructure failed: missing block: B:30:0x013a, code lost:
    
        if (r9 != 3) goto L27;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private boolean checkVersion() {
        /*
            Method dump skipped, instructions count: 439
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airliftcompany.alp3.comm.TXController.checkVersion():boolean");
    }

    private boolean syncMacAndSerial() {
        Log.w(TAG, "Starting Mac and Serial Download");
        byte[] bArr = new byte[12];
        int i = 0;
        while (i < 12) {
            int i2 = i + 1;
            if (txGetRecord(18, i2)) {
                byte b = (byte) this.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                if (b >= 32 && b <= 126) {
                    bArr[i] = b;
                } else {
                    bArr[i] = 32;
                }
            }
            i = i2;
        }
        this.mCommService.alp3Device.Serial = new String(bArr, StandardCharsets.US_ASCII).replace(" ", "");
        byte[] bArr2 = new byte[4];
        for (int i3 = 0; i3 < 4; i3++) {
            if (txGetRecord(18, i3 + 17)) {
                byte b2 = (byte) this.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                if (b2 >= 32 && b2 <= 126) {
                    bArr2[i3] = b2;
                } else {
                    bArr[i3] = 32;
                }
            }
        }
        this.mCommService.alp3Device.MacAddress = new String(bArr2, StandardCharsets.US_ASCII).replace(" ", "");
        this.mCommService.alp3Device.haveMacAndSerial = true;
        if (this.mCommService.commServiceListener != null) {
            this.mCommService.commServiceListener.onSerialUpdated();
        }
        Log.w(TAG, "Mac and Serial Downloaded");
        return true;
    }

    private Boolean syncCalibration() {
        String str = TAG;
        Log.w(str, "Starting Calibration Download");
        if (txGetRecord(5, 14)) {
            this.mCommService.alp3Device.calibrationSettings.Accel = (int) this.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
            txControllerStatus();
            if (txGetRecord(5, 15)) {
                this.mCommService.alp3Device.calibrationSettings.AccelMount = (int) this.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                txControllerStatus();
                if (txGetRecord(9, 1)) {
                    this.mCommService.alp3Device.calibrationSettings.PressureOnly = (int) this.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                    txControllerStatus();
                    if (txGetRecord(1, 9)) {
                        this.mCommService.alp3Device.calibrationSettings.HeightLimit = (int) this.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                        txControllerStatus();
                        if (txGetRecord(11, 17)) {
                            this.mCommService.alp3Device.calibrationSettings.HeightAlg = (int) this.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                            txControllerStatus();
                            if (txGetRecord(7, 26)) {
                                this.mCommService.alp3Device.calibrationSettings.PressureAlg = (int) this.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                                boolean z = this.mCommService.alp3Device.calibrationSettings.Accel != 1;
                                if (this.mCommService.alp3Device.calibrationSettings.AccelMount != 1) {
                                    z = true;
                                }
                                if (this.mCommService.alp3Device.calibrationSettings.HeightAlg != 1 && this.mCommService.alp3Device.calibrationSettings.PressureOnly == 0) {
                                    z = true;
                                }
                                if (this.mCommService.alp3Device.calibrationSettings.HeightLimit != 1 && this.mCommService.alp3Device.calibrationSettings.PressureOnly == 0) {
                                    z = true;
                                }
                                if (this.mCommService.alp3Device.calibrationSettings.PressureAlg != 1) {
                                    z = true;
                                }
                                this.mCommService.alp3Device.calibrationSettings.presentCalibration = z;
                                this.mCommService.alp3Device.calibrationSettings.needsCalibration = z;
                                this.mCommService.alp3Device.calibrationSettings.Synced = true;
                                if (this.mCommService.commServiceListener != null) {
                                    this.mCommService.commServiceListener.onCalibrationUpdated();
                                }
                                Log.w(str, "Calibration Downloaded");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public void txControllerStatus() {
        ALP3Protocol.BlePacketHeader blePacketHeader = new ALP3Protocol.BlePacketHeader();
        blePacketHeader.messageId = (short) 24;
        blePacketHeader.pngLower = (short) 120;
        blePacketHeader.pngUpper = (short) 255;
        blePacketHeader.sourceId = (short) 3;
        blePacketHeader.length = (short) 5;
        if (this.mCommService.canpng65400UIStatus.ControlMode == ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATION_IN_PROGRESS.ordinal()) {
            blePacketHeader.data[0] = 255;
            blePacketHeader.data[1] = 255;
            blePacketHeader.data[2] = 255;
            blePacketHeader.data[3] = 255;
            blePacketHeader.data[4] = 255;
            blePacketHeader.data[5] = 255;
            blePacketHeader.data[6] = 255;
            blePacketHeader.data[7] = 255;
        } else {
            blePacketHeader.data[0] = this.mCommService.canpng65400UIStatus.Status;
            blePacketHeader.data[1] = this.mCommService.canpng65400UIStatus.ControlMode;
            blePacketHeader.data[2] = this.mCommService.canpng65400UIStatus.SubControlMode;
            blePacketHeader.data[3] = this.mCommService.canpng65400UIStatus.ControlSequence;
            blePacketHeader.data[4] = (short) (((short) (3 & this.mCommService.canpng65400UIStatus.CompressorDisabled)) | ((this.mCommService.canpng65400UIStatus.CompressorForceOn << 2) & 12));
            blePacketHeader.data[5] = 255;
            blePacketHeader.data[6] = 255;
            blePacketHeader.data[7] = 255;
        }
        sendPacket(blePacketHeader);
    }

    public void txManualMode() {
        ALP3Protocol.BlePacketHeader blePacketHeader = new ALP3Protocol.BlePacketHeader();
        blePacketHeader.messageId = (short) 24;
        blePacketHeader.pngLower = (short) 124;
        blePacketHeader.pngUpper = (short) 255;
        blePacketHeader.sourceId = (short) 3;
        blePacketHeader.length = (short) 2;
        blePacketHeader.data[0] = (short) (((short) (((short) (((short) (this.mCommService.alp3Device.canpng65404ManualControl.Spring[0] & 3)) | ((short) ((this.mCommService.alp3Device.canpng65404ManualControl.Spring[1] << 2) & 12)))) | ((short) ((this.mCommService.alp3Device.canpng65404ManualControl.Spring[2] << 4) & 48)))) | ((short) ((this.mCommService.alp3Device.canpng65404ManualControl.Spring[3] << 6) & 192)));
        blePacketHeader.data[1] = (short) this.mCommService.alp3Device.canpng65404ManualControl.Direction;
        blePacketHeader.data[2] = 255;
        blePacketHeader.data[3] = 255;
        blePacketHeader.data[4] = 255;
        blePacketHeader.data[5] = 255;
        blePacketHeader.data[6] = 255;
        blePacketHeader.data[7] = 255;
        sendPacket(blePacketHeader);
    }

    /* JADX WARN: Code restructure failed: missing block: B:19:0x0091, code lost:
    
        android.util.Log.e(com.airliftcompany.alp3.comm.TXController.TAG, "Retry authorization sent");
        r8.mCommService.commStatus.WaitTick = 0;
        r3 = r3 + 1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private boolean txCheckAuthorization() {
        /*
            r8 = this;
            java.lang.String r0 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r1 = "txCheckAuthorization"
            android.util.Log.v(r0, r1)
            com.airliftcompany.alp3.comm.CommService r0 = r8.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r0 = r0.commStatus
            r1 = 0
            r0.WaitTick = r1
            com.airliftcompany.alp3.comm.ALP3Protocol$BlePacketHeader r0 = new com.airliftcompany.alp3.comm.ALP3Protocol$BlePacketHeader
            r0.<init>()
            r2 = 24
            r0.messageId = r2
            r2 = 32
            r0.pngLower = r2
            r2 = 78
            r0.pngUpper = r2
            r2 = 3
            r0.sourceId = r2
            r3 = 8
            r0.length = r3
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            android.content.ContentResolver r4 = r4.getContentResolver()
            java.lang.String r5 = "android_id"
            java.lang.String r4 = android.provider.Settings.Secure.getString(r4, r5)
            byte[] r4 = r4.getBytes()
            r5 = 0
        L37:
            if (r5 >= r3) goto L43
            short[] r6 = r0.data
            r7 = r4[r5]
            short r7 = (short) r7
            r6[r5] = r7
            int r5 = r5 + 1
            goto L37
        L43:
            com.airliftcompany.alp3.comm.CommService r3 = r8.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r3 = r3.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplyRecord r3 = r3.canpng65350ReplyRecord
            r3.clearRecord()
            r3 = 0
        L4d:
            if (r3 >= r2) goto Lb0
            r8.sendPacket(r0)
        L52:
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r4 = r4.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplyRecord r4 = r4.canpng65350ReplyRecord
            short r4 = r4.Valid
            r5 = 1
            if (r4 != r5) goto L87
            com.airliftcompany.alp3.comm.CommService r0 = r8.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r0 = r0.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplyRecord r0 = r0.canpng65350ReplyRecord
            short r0 = r0.Action
            if (r0 != r5) goto L77
            java.lang.String r0 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r1 = "Authorized"
            android.util.Log.v(r0, r1)
            com.airliftcompany.alp3.comm.CommService r0 = r8.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r0 = r0.commStatus
            com.airliftcompany.alp3.comm.CommService$DeviceAuthorizedEnum r1 = com.airliftcompany.alp3.comm.CommService.DeviceAuthorizedEnum.DeviceIsAuthorized
            r0.DeviceAuthorized = r1
            goto L86
        L77:
            java.lang.String r0 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r1 = "Not Authorized"
            android.util.Log.v(r0, r1)
            com.airliftcompany.alp3.comm.CommService r0 = r8.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r0 = r0.commStatus
            com.airliftcompany.alp3.comm.CommService$DeviceAuthorizedEnum r1 = com.airliftcompany.alp3.comm.CommService.DeviceAuthorizedEnum.DeviceUnauthorized
            r0.DeviceAuthorized = r1
        L86:
            return r5
        L87:
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r4 = r4.commStatus
            int r4 = r4.WaitTick
            r5 = 550(0x226, float:7.71E-43)
            if (r4 <= r5) goto La1
            java.lang.String r4 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r5 = "Retry authorization sent"
            android.util.Log.e(r4, r5)
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r4 = r4.commStatus
            r4.WaitTick = r1
            int r3 = r3 + 1
            goto L4d
        La1:
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r4 = r4.commStatus
            boolean r4 = r4.BlueToothAccConnected
            if (r4 != 0) goto Laa
            return r1
        Laa:
            r4 = 1
            android.os.SystemClock.sleep(r4)
            goto L52
        Lb0:
            java.lang.String r0 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r2 = "Failed authorization request"
            android.util.Log.v(r0, r2)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airliftcompany.alp3.comm.TXController.txCheckAuthorization():boolean");
    }

    /* JADX WARN: Code restructure failed: missing block: B:22:0x008e, code lost:
    
        android.util.Log.e(com.airliftcompany.alp3.comm.TXController.TAG, "Retry get variable sent");
        r8.mCommService.commStatus.WaitTick = 0;
        r3 = r3 + 1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean txGetVariable(int r9) {
        /*
            r8 = this;
            java.lang.String r0 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r1 = "txGetVariable"
            android.util.Log.v(r0, r1)
            com.airliftcompany.alp3.comm.CommService r0 = r8.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r0 = r0.commStatus
            r1 = 0
            r0.WaitTick = r1
            com.airliftcompany.alp3.comm.ALP3Protocol$BlePacketHeader r0 = new com.airliftcompany.alp3.comm.ALP3Protocol$BlePacketHeader
            r0.<init>()
            r2 = 24
            r0.messageId = r2
            r2 = 240(0xf0, float:3.36E-43)
            r0.pngLower = r2
            r2 = 255(0xff, float:3.57E-43)
            r0.pngUpper = r2
            r2 = 3
            r0.sourceId = r2
            r3 = 8
            r0.length = r3
            short[] r3 = r0.data
            r3[r1] = r1
            short[] r3 = r0.data
            short r4 = (short) r9
            r5 = 1
            r3[r5] = r4
            short[] r3 = r0.data
            r4 = 2
            r3[r4] = r1
            short[] r3 = r0.data
            r3[r2] = r1
            short[] r3 = r0.data
            r4 = 4
            r3[r4] = r1
            short[] r3 = r0.data
            r4 = 5
            r3[r4] = r1
            short[] r3 = r0.data
            r4 = 6
            r3[r4] = r1
            short[] r3 = r0.data
            r4 = 7
            r3[r4] = r1
            com.airliftcompany.alp3.comm.CommService r3 = r8.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r3 = r3.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65351ReplyVariable r3 = r3.canpng65351ReplyVariable
            r3.clearRecord()
            r3 = 0
        L57:
            if (r3 >= r2) goto Lad
            r8.sendPacket(r0)
        L5c:
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r4 = r4.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65351ReplyVariable r4 = r4.canpng65351ReplyVariable
            short r4 = r4.Valid
            if (r4 != r5) goto L84
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r4 = r4.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65351ReplyVariable r4 = r4.canpng65351ReplyVariable
            short r4 = r4.VariableID
            if (r4 != r9) goto L84
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r4 = r4.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65351ReplyVariable r4 = r4.canpng65351ReplyVariable
            short r4 = r4.Action
            r6 = 16
            if (r4 != r6) goto L84
            java.lang.String r9 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r0 = "txGetVariable complete"
            android.util.Log.v(r9, r0)
            return r5
        L84:
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r4 = r4.commStatus
            int r4 = r4.WaitTick
            r6 = 550(0x226, float:7.71E-43)
            if (r4 <= r6) goto L9e
            java.lang.String r4 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r6 = "Retry get variable sent"
            android.util.Log.e(r4, r6)
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r4 = r4.commStatus
            r4.WaitTick = r1
            int r3 = r3 + 1
            goto L57
        L9e:
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r4 = r4.commStatus
            boolean r4 = r4.BlueToothAccConnected
            if (r4 != 0) goto La7
            return r1
        La7:
            r6 = 1
            android.os.SystemClock.sleep(r6)
            goto L5c
        Lad:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airliftcompany.alp3.comm.TXController.txGetVariable(int):boolean");
    }

    /* JADX WARN: Code restructure failed: missing block: B:24:0x0099, code lost:
    
        android.util.Log.e(com.airliftcompany.alp3.comm.TXController.TAG, "Retry get record sent");
        r8.mCommService.commStatus.WaitTick = 0;
        r3 = r3 + 1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean txGetRecord(int r9, int r10) {
        /*
            r8 = this;
            java.lang.String r0 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r1 = "txGetRecord"
            android.util.Log.i(r0, r1)
            com.airliftcompany.alp3.comm.CommService r0 = r8.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r0 = r0.commStatus
            r1 = 0
            r0.WaitTick = r1
            com.airliftcompany.alp3.comm.ALP3Protocol$BlePacketHeader r0 = new com.airliftcompany.alp3.comm.ALP3Protocol$BlePacketHeader
            r0.<init>()
            r2 = 24
            r0.messageId = r2
            r2 = 220(0xdc, float:3.08E-43)
            r0.pngLower = r2
            r2 = 255(0xff, float:3.57E-43)
            r0.pngUpper = r2
            r2 = 3
            r0.sourceId = r2
            r3 = 8
            r0.length = r3
            short[] r3 = r0.data
            r3[r1] = r1
            short[] r3 = r0.data
            short r4 = (short) r9
            r5 = 1
            r3[r5] = r4
            short[] r3 = r0.data
            short r4 = (short) r10
            r6 = 2
            r3[r6] = r4
            short[] r3 = r0.data
            r3[r2] = r1
            short[] r3 = r0.data
            r4 = 4
            r3[r4] = r1
            short[] r3 = r0.data
            r4 = 5
            r3[r4] = r1
            short[] r3 = r0.data
            r4 = 6
            r3[r4] = r1
            short[] r3 = r0.data
            r4 = 7
            r3[r4] = r1
            com.airliftcompany.alp3.comm.CommService r3 = r8.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r3 = r3.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplyRecord r3 = r3.canpng65350ReplyRecord
            r3.clearRecord()
            r3 = 0
        L58:
            if (r3 >= r2) goto Lb8
            r8.sendPacket(r0)
        L5d:
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r4 = r4.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplyRecord r4 = r4.canpng65350ReplyRecord
            short r4 = r4.Valid
            if (r4 != r5) goto L8f
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r4 = r4.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplyRecord r4 = r4.canpng65350ReplyRecord
            short r4 = r4.RecordID
            if (r4 != r9) goto L8f
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r4 = r4.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplyRecord r4 = r4.canpng65350ReplyRecord
            short r4 = r4.VariableID
            if (r4 != r10) goto L8f
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r4 = r4.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplyRecord r4 = r4.canpng65350ReplyRecord
            short r4 = r4.Action
            r6 = 16
            if (r4 != r6) goto L8f
            java.lang.String r9 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r10 = "Get record RX"
            android.util.Log.i(r9, r10)
            return r5
        L8f:
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r4 = r4.commStatus
            int r4 = r4.WaitTick
            r6 = 550(0x226, float:7.71E-43)
            if (r4 <= r6) goto La9
            java.lang.String r4 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r6 = "Retry get record sent"
            android.util.Log.e(r4, r6)
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r4 = r4.commStatus
            r4.WaitTick = r1
            int r3 = r3 + 1
            goto L58
        La9:
            com.airliftcompany.alp3.comm.CommService r4 = r8.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r4 = r4.commStatus
            boolean r4 = r4.BlueToothAccConnected
            if (r4 != 0) goto Lb2
            return r1
        Lb2:
            r6 = 1
            android.os.SystemClock.sleep(r6)
            goto L5d
        Lb8:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airliftcompany.alp3.comm.TXController.txGetRecord(int, int):boolean");
    }

    /* JADX WARN: Code restructure failed: missing block: B:23:0x008e, code lost:
    
        android.util.Log.e(com.airliftcompany.alp3.comm.TXController.TAG, "Retry memory sectory sent");
        r9.mCommService.commStatus.WaitTick = 0;
        r4 = r4 + 1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public long[] txGetMemorySector(int r10) {
        /*
            r9 = this;
            java.lang.String r0 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r1 = "txGetMemorySector"
            android.util.Log.v(r0, r1)
            com.airliftcompany.alp3.comm.CommService r0 = r9.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r0 = r0.commStatus
            r1 = 0
            r0.WaitTick = r1
            com.airliftcompany.alp3.comm.ALP3Protocol$BlePacketHeader r0 = new com.airliftcompany.alp3.comm.ALP3Protocol$BlePacketHeader
            r0.<init>()
            r2 = 24
            r0.messageId = r2
            r2 = 16
            r0.pngLower = r2
            r3 = 39
            r0.pngUpper = r3
            r3 = 3
            r0.sourceId = r3
            r4 = 8
            r0.length = r4
            short[] r4 = r0.data
            r4[r1] = r1
            short[] r4 = r0.data
            short r5 = (short) r10
            r6 = 1
            r4[r6] = r5
            short[] r4 = r0.data
            r5 = 2
            r4[r5] = r1
            short[] r4 = r0.data
            r4[r3] = r1
            short[] r4 = r0.data
            r5 = 4
            r4[r5] = r1
            short[] r4 = r0.data
            r5 = 5
            r4[r5] = r1
            short[] r4 = r0.data
            r5 = 6
            r4[r5] = r1
            short[] r4 = r0.data
            r5 = 7
            r4[r5] = r1
            com.airliftcompany.alp3.comm.CommService r4 = r9.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r4 = r4.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplySector r4 = r4.canpng65350ReplySector
            r4.clearRecord()
            r4 = 0
        L57:
            r5 = 0
            if (r4 >= r3) goto Lad
            r9.sendPacket(r0)
        L5d:
            com.airliftcompany.alp3.comm.CommService r7 = r9.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r7 = r7.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplySector r7 = r7.canpng65350ReplySector
            short r7 = r7.Valid
            if (r7 != r6) goto L84
            com.airliftcompany.alp3.comm.CommService r7 = r9.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r7 = r7.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplySector r7 = r7.canpng65350ReplySector
            short r7 = r7.RecordID
            if (r7 != r10) goto L84
            com.airliftcompany.alp3.comm.CommService r7 = r9.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r7 = r7.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplySector r7 = r7.canpng65350ReplySector
            short r7 = r7.Action
            if (r7 != r2) goto L84
            com.airliftcompany.alp3.comm.CommService r10 = r9.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r10 = r10.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplySector r10 = r10.canpng65350ReplySector
            long[] r10 = r10.data
            return r10
        L84:
            com.airliftcompany.alp3.comm.CommService r7 = r9.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r7 = r7.commStatus
            int r7 = r7.WaitTick
            r8 = 550(0x226, float:7.71E-43)
            if (r7 <= r8) goto L9e
            java.lang.String r5 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r7 = "Retry memory sectory sent"
            android.util.Log.e(r5, r7)
            com.airliftcompany.alp3.comm.CommService r5 = r9.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r5 = r5.commStatus
            r5.WaitTick = r1
            int r4 = r4 + 1
            goto L57
        L9e:
            com.airliftcompany.alp3.comm.CommService r7 = r9.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r7 = r7.commStatus
            boolean r7 = r7.BlueToothAccConnected
            if (r7 != 0) goto La7
            return r5
        La7:
            r7 = 1
            android.os.SystemClock.sleep(r7)
            goto L5d
        Lad:
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airliftcompany.alp3.comm.TXController.txGetMemorySector(int):long[]");
    }

    /* JADX WARN: Code restructure failed: missing block: B:23:0x0093, code lost:
    
        android.util.Log.e(com.airliftcompany.alp3.comm.TXController.TAG, "Retry txWriteVariable sent");
        r10.mCommService.commStatus.WaitTick = 0;
        r3 = r3 + 1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean txWriteRecord(short r11, short r12) {
        /*
            r10 = this;
            java.lang.String r0 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r1 = "txWriteVariable"
            android.util.Log.v(r0, r1)
            com.airliftcompany.alp3.comm.CommService r0 = r10.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r0 = r0.commStatus
            r1 = 0
            r0.WaitTick = r1
            com.airliftcompany.alp3.comm.ALP3Protocol$BlePacketHeader r0 = new com.airliftcompany.alp3.comm.ALP3Protocol$BlePacketHeader
            r0.<init>()
            r2 = 24
            r0.messageId = r2
            r2 = 240(0xf0, float:3.36E-43)
            r0.pngLower = r2
            r2 = 255(0xff, float:3.57E-43)
            r0.pngUpper = r2
            r2 = 3
            r0.sourceId = r2
            r3 = 8
            r0.length = r3
            short[] r3 = r0.data
            r4 = 1
            r3[r1] = r4
            short[] r3 = r0.data
            r3[r4] = r11
            short[] r3 = r0.data
            r5 = 2
            r3[r5] = r12
            short[] r3 = r0.data
            r3[r2] = r1
            short[] r3 = r0.data
            r5 = 4
            r3[r5] = r1
            short[] r3 = r0.data
            r5 = 5
            r3[r5] = r1
            short[] r3 = r0.data
            r5 = 6
            r3[r5] = r1
            short[] r3 = r0.data
            r5 = 7
            r3[r5] = r1
            com.airliftcompany.alp3.comm.CommService r3 = r10.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r3 = r3.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65351ReplyVariable r3 = r3.canpng65351ReplyVariable
            r3.clearRecord()
            r3 = 0
        L56:
            if (r3 >= r2) goto Lb2
            r10.sendPacket(r0)
        L5b:
            com.airliftcompany.alp3.comm.CommService r5 = r10.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r5 = r5.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65351ReplyVariable r5 = r5.canpng65351ReplyVariable
            short r5 = r5.Valid
            if (r5 != r4) goto L89
            com.airliftcompany.alp3.comm.CommService r5 = r10.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r5 = r5.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65351ReplyVariable r5 = r5.canpng65351ReplyVariable
            short r5 = r5.VariableID
            if (r5 != r11) goto L89
            com.airliftcompany.alp3.comm.CommService r5 = r10.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r5 = r5.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65351ReplyVariable r5 = r5.canpng65351ReplyVariable
            long r5 = r5.Variable
            long r7 = (long) r12
            int r9 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r9 != 0) goto L89
            com.airliftcompany.alp3.comm.CommService r5 = r10.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r5 = r5.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65351ReplyVariable r5 = r5.canpng65351ReplyVariable
            short r5 = r5.Action
            r6 = 17
            if (r5 != r6) goto L89
            return r4
        L89:
            com.airliftcompany.alp3.comm.CommService r5 = r10.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r5 = r5.commStatus
            int r5 = r5.WaitTick
            r6 = 550(0x226, float:7.71E-43)
            if (r5 <= r6) goto La3
            java.lang.String r5 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r6 = "Retry txWriteVariable sent"
            android.util.Log.e(r5, r6)
            com.airliftcompany.alp3.comm.CommService r5 = r10.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r5 = r5.commStatus
            r5.WaitTick = r1
            int r3 = r3 + 1
            goto L56
        La3:
            com.airliftcompany.alp3.comm.CommService r5 = r10.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r5 = r5.commStatus
            boolean r5 = r5.BlueToothAccConnected
            if (r5 != 0) goto Lac
            return r1
        Lac:
            r5 = 1
            android.os.SystemClock.sleep(r5)
            goto L5b
        Lb2:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airliftcompany.alp3.comm.TXController.txWriteRecord(short, short):boolean");
    }

    /* JADX WARN: Code restructure failed: missing block: B:23:0x00a8, code lost:
    
        android.util.Log.e(com.airliftcompany.alp3.comm.TXController.TAG, "Retry txWriteVariable sent");
        r11.mCommService.commStatus.WaitTick = 0;
        r14 = r14 + 1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean txWriteVariable(short r12, short r13, long r14) {
        /*
            Method dump skipped, instructions count: 200
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airliftcompany.alp3.comm.TXController.txWriteVariable(short, short, long):boolean");
    }

    /* JADX WARN: Code restructure failed: missing block: B:19:0x007c, code lost:
    
        android.util.Log.e(com.airliftcompany.alp3.comm.TXController.TAG, "Retry txStartFlashWrite sent");
        r7.mCommService.commStatus.WaitTick = 0;
        r2 = r2 + 1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean txStartFlashWrite() {
        /*
            r7 = this;
            java.lang.String r0 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r1 = "txWriteVariable"
            android.util.Log.v(r0, r1)
            com.airliftcompany.alp3.comm.CommService r0 = r7.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r0 = r0.commStatus
            r1 = 0
            r0.WaitTick = r1
            com.airliftcompany.alp3.comm.ALP3Protocol$BlePacketHeader r0 = new com.airliftcompany.alp3.comm.ALP3Protocol$BlePacketHeader
            r0.<init>()
            r2 = 24
            r0.messageId = r2
            r2 = 220(0xdc, float:3.08E-43)
            r0.pngLower = r2
            r2 = 255(0xff, float:3.57E-43)
            r0.pngUpper = r2
            r3 = 3
            r0.sourceId = r3
            r4 = 8
            r0.length = r4
            short[] r4 = r0.data
            r5 = 2
            r4[r1] = r5
            short[] r4 = r0.data
            r6 = 1
            r4[r6] = r1
            short[] r4 = r0.data
            r4[r5] = r1
            short[] r4 = r0.data
            r4[r3] = r2
            short[] r4 = r0.data
            r5 = 4
            r4[r5] = r2
            short[] r4 = r0.data
            r5 = 5
            r4[r5] = r2
            short[] r4 = r0.data
            r5 = 6
            r4[r5] = r2
            short[] r4 = r0.data
            r5 = 7
            r4[r5] = r2
            com.airliftcompany.alp3.comm.CommService r2 = r7.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r2 = r2.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplyRecord r2 = r2.canpng65350ReplyRecord
            r2.clearRecord()
            r2 = 0
        L56:
            if (r2 >= r3) goto L9b
            r7.sendPacket(r0)
        L5b:
            com.airliftcompany.alp3.comm.CommService r4 = r7.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r4 = r4.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplyRecord r4 = r4.canpng65350ReplyRecord
            short r4 = r4.Valid
            if (r4 != r6) goto L72
            com.airliftcompany.alp3.comm.CommService r4 = r7.mCommService
            com.airliftcompany.alp3.comm.ALP3Device r4 = r4.alp3Device
            com.airliftcompany.alp3.comm.ALP3Device$CANPNG65350ReplyRecord r4 = r4.canpng65350ReplyRecord
            short r4 = r4.Action
            r5 = 18
            if (r4 != r5) goto L72
            return r6
        L72:
            com.airliftcompany.alp3.comm.CommService r4 = r7.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r4 = r4.commStatus
            int r4 = r4.WaitTick
            r5 = 550(0x226, float:7.71E-43)
            if (r4 <= r5) goto L8c
            java.lang.String r4 = com.airliftcompany.alp3.comm.TXController.TAG
            java.lang.String r5 = "Retry txStartFlashWrite sent"
            android.util.Log.e(r4, r5)
            com.airliftcompany.alp3.comm.CommService r4 = r7.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r4 = r4.commStatus
            r4.WaitTick = r1
            int r2 = r2 + 1
            goto L56
        L8c:
            com.airliftcompany.alp3.comm.CommService r4 = r7.mCommService
            com.airliftcompany.alp3.comm.CommService$CommStatus r4 = r4.commStatus
            boolean r4 = r4.BlueToothAccConnected
            if (r4 != 0) goto L95
            return r1
        L95:
            r4 = 1
            android.os.SystemClock.sleep(r4)
            goto L5b
        L9b:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airliftcompany.alp3.comm.TXController.txStartFlashWrite():boolean");
    }

    private void sendPacket(ALP3Protocol.BlePacketHeader blePacketHeader) {
        blePacketHeader.preamble[0] = 0;
        blePacketHeader.preamble[1] = 0;
        blePacketHeader.startByte[0] = 170;
        blePacketHeader.startByte[1] = 85;
        blePacketHeader.startByte[2] = 170;
        blePacketHeader.startByte[3] = 86;
        this.mCommService.bleService.writeValue(blePacketHeader.getData());
    }
}
