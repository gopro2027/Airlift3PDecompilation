package com.airliftcompany.alp3.comm;

import android.util.Log;
import com.airliftcompany.alp3.comm.ALP3Protocol;

/* loaded from: classes.dex */
public class RXController {
    private static final int COM_CONNECT_TIMEOUT = 5000;
    private static final String TAG = "RXController";
    public BTRxStatus btRxStatus;
    private Boolean debugPackets;
    private CommService mCommService;
    private RxStateEnum mRxState;
    private long mSyncWord;
    private short[] packetBuffer;
    private int packetBufferCount;

    private enum RxStateEnum {
        RX_WAIT_SYNC,
        RX_WAIT_HEADER,
        RX_WAIT_DATA,
        RX_WAIT_BIG_DATA,
        RX_RESET
    }

    private void logErrorMessage() {
    }

    public RXController(CommService commService) {
        this.mRxState = RxStateEnum.RX_WAIT_SYNC;
        this.packetBuffer = new short[2048];
        this.debugPackets = false;
        this.mSyncWord = 0L;
        this.btRxStatus = new BTRxStatus();
        this.mCommService = commService;
    }

    public RXController() {
        this.mRxState = RxStateEnum.RX_WAIT_SYNC;
        this.packetBuffer = new short[2048];
        this.debugPackets = false;
        this.mSyncWord = 0L;
        this.btRxStatus = new BTRxStatus();
    }

    public class BTRxStatus {
        public int dataInvalidError;
        public int packetsReceived;

        public BTRxStatus() {
        }
    }

    public boolean processRxData(short[] sArr) {
        for (short s : sArr) {
            processNewChar(s);
        }
        return true;
    }

    public void processNewChar(short s) {
        long j = this.mSyncWord << 8;
        this.mSyncWord = j;
        long j2 = j | (s & 255);
        this.mSyncWord = j2;
        long j3 = j2 & 4294967295L;
        this.mSyncWord = j3;
        short s2 = 0;
        if (j3 == 2857740886L) {
            this.mRxState = RxStateEnum.RX_WAIT_HEADER;
            this.packetBufferCount = 0;
        }
        int i = C04291.$SwitchMap$com$airliftcompany$alp3$comm$RXController$RxStateEnum[this.mRxState.ordinal()];
        if (i != 1) {
            if (i == 2) {
                short[] sArr = this.packetBuffer;
                int i2 = this.packetBufferCount;
                int i3 = i2 + 1;
                this.packetBufferCount = i3;
                sArr[i2] = s;
                if (i3 != 6) {
                    if (i3 > 6) {
                        this.btRxStatus.dataInvalidError++;
                        Log.e(TAG, "Data count Error");
                        logErrorMessage();
                        this.mRxState = RxStateEnum.RX_WAIT_SYNC;
                        return;
                    }
                    return;
                }
                if (sArr[1] == 24 && sArr[4] == 0) {
                    this.mRxState = RxStateEnum.RX_WAIT_DATA;
                    return;
                }
                this.btRxStatus.dataInvalidError++;
                logErrorMessage();
                this.mRxState = RxStateEnum.RX_WAIT_SYNC;
                return;
            }
            if (i != 3) {
                if (i == 4) {
                    this.mRxState = RxStateEnum.RX_WAIT_SYNC;
                    return;
                } else {
                    this.mRxState = RxStateEnum.RX_WAIT_SYNC;
                    return;
                }
            }
            short[] sArr2 = this.packetBuffer;
            int i4 = this.packetBufferCount;
            int i5 = i4 + 1;
            this.packetBufferCount = i5;
            sArr2[i4] = s;
            if (i5 == sArr2[5] + 7) {
                short s3 = (short) (sArr2[5] + 5 + 1);
                for (short s4 = 1; s4 < s3; s4 = (short) (s4 + 1)) {
                    s2 = (short) (s2 + this.packetBuffer[s4]);
                }
                short s5 = (short) ((~s2) & 255);
                short[] sArr3 = this.packetBuffer;
                if (s5 == sArr3[s3]) {
                    if (s3 <= 14) {
                        processBtPacket(new ALP3Protocol.BlePacketHeader(sArr3));
                    } else {
                        processLargeBtPacket(new ALP3Protocol.LargeBlePacketHeader(sArr3));
                    }
                    this.btRxStatus.packetsReceived++;
                    this.mRxState = RxStateEnum.RX_WAIT_SYNC;
                    return;
                }
                this.btRxStatus.dataInvalidError++;
                logErrorMessage();
                this.mRxState = RxStateEnum.RX_WAIT_SYNC;
                return;
            }
            if (i5 > sArr2[5] + 10) {
                this.btRxStatus.dataInvalidError++;
                Log.e(TAG, "Data count Error");
                logErrorMessage();
                this.mRxState = RxStateEnum.RX_WAIT_SYNC;
            }
        }
    }

    /* renamed from: com.airliftcompany.alp3.comm.RXController$1 */
    static /* synthetic */ class C04291 {
        static final /* synthetic */ int[] $SwitchMap$com$airliftcompany$alp3$comm$RXController$RxStateEnum;

        static {
            int[] iArr = new int[RxStateEnum.values().length];
            $SwitchMap$com$airliftcompany$alp3$comm$RXController$RxStateEnum = iArr;
            try {
                iArr[RxStateEnum.RX_WAIT_SYNC.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$airliftcompany$alp3$comm$RXController$RxStateEnum[RxStateEnum.RX_WAIT_HEADER.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$airliftcompany$alp3$comm$RXController$RxStateEnum[RxStateEnum.RX_WAIT_DATA.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$airliftcompany$alp3$comm$RXController$RxStateEnum[RxStateEnum.RX_RESET.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    private void processBtPacket(ALP3Protocol.BlePacketHeader blePacketHeader) {
        if (this.mCommService.rxPacketProcessingOff) {
            Log.i(TAG, "Ignored Packet");
            return;
        }
        int i = (blePacketHeader.pngUpper << 8) | blePacketHeader.pngLower;
        if (i == 20013) {
            if (this.debugPackets.booleanValue()) {
                Log.v(TAG, "CAN_PNG_20010_CONNECT_REPLY_ID");
            }
            this.mCommService.alp3Device.canpng65350ReplyRecord.Action = blePacketHeader.data[0];
            this.mCommService.alp3Device.canpng65350ReplyRecord.Valid = (short) 1;
        } else if (i == 65280) {
            if (this.debugPackets.booleanValue()) {
                Log.v(TAG, "CAN_PNG_65280_ECU_FAULT_ID");
            }
            this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorLimit[0] = (short) (blePacketHeader.data[0] & 1);
            this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorLimit[1] = (short) ((blePacketHeader.data[0] & 2) >> 1);
            this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorLimit[2] = (short) ((blePacketHeader.data[0] & 4) >> 2);
            this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorLimit[3] = (short) ((blePacketHeader.data[0] & 8) >> 3);
            this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorNotPresent[0] = (short) ((blePacketHeader.data[0] & 16) >> 4);
            this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorNotPresent[1] = (short) ((blePacketHeader.data[0] & 32) >> 5);
            this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorNotPresent[2] = (short) ((blePacketHeader.data[0] & 64) >> 6);
            this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorNotPresent[3] = (short) ((blePacketHeader.data[0] & 128) >> 7);
            this.mCommService.alp3Device.canpng65280ECUFault.PressureSensor[0] = (short) (blePacketHeader.data[1] & 1);
            this.mCommService.alp3Device.canpng65280ECUFault.PressureSensor[1] = (short) ((blePacketHeader.data[1] & 2) >> 1);
            this.mCommService.alp3Device.canpng65280ECUFault.PressureSensor[2] = (short) ((blePacketHeader.data[1] & 4) >> 2);
            this.mCommService.alp3Device.canpng65280ECUFault.PressureSensor[3] = (short) ((blePacketHeader.data[1] & 8) >> 3);
            this.mCommService.alp3Device.canpng65280ECUFault.PressureSensor[4] = (short) ((blePacketHeader.data[1] & 16) >> 4);
            this.mCommService.alp3Device.canpng65280ECUFault.FactoryCal = (short) (blePacketHeader.data[2] & 1);
            this.mCommService.alp3Device.canpng65280ECUFault.HeightLimitCal = (short) ((blePacketHeader.data[2] & 2) >> 1);
            this.mCommService.alp3Device.canpng65280ECUFault.PressureCal = (short) ((blePacketHeader.data[2] & 4) >> 2);
            this.mCommService.alp3Device.canpng65280ECUFault.HeightCal = (short) ((blePacketHeader.data[2] & 8) >> 3);
            this.mCommService.alp3Device.canpng65280ECUFault.AccelCal = (short) ((blePacketHeader.data[2] & 16) >> 4);
            this.mCommService.alp3Device.canpng65280ECUFault.CompressorFreeze = (short) (blePacketHeader.data[3] & 1);
            this.mCommService.alp3Device.canpng65280ECUFault.CompressorOverrun = (short) ((blePacketHeader.data[3] & 2) >> 1);
            this.mCommService.alp3Device.canpng65280ECUFault.TankPressureLow = (short) ((blePacketHeader.data[3] & 4) >> 2);
            this.mCommService.alp3Device.canpng65280ECUFault.InvaildMount = (short) (blePacketHeader.data[4] & 1);
            this.mCommService.alp3Device.canpng65280ECUFault.HighVoltage = (short) ((blePacketHeader.data[4] & 2) >> 1);
            this.mCommService.alp3Device.canpng65280ECUFault.LowVoltage = (short) ((blePacketHeader.data[4] & 4) >> 2);
            this.mCommService.alp3Device.canpng65280ECUFault.ECUoverTemp = (short) ((blePacketHeader.data[4] & 8) >> 3);
            this.mCommService.alp3Device.canpng65280ECUFault.Valve[0] = (short) (blePacketHeader.data[5] & 1);
            this.mCommService.alp3Device.canpng65280ECUFault.Valve[1] = (short) ((blePacketHeader.data[5] & 2) >> 1);
            this.mCommService.alp3Device.canpng65280ECUFault.Valve[2] = (short) ((blePacketHeader.data[5] & 4) >> 2);
            this.mCommService.alp3Device.canpng65280ECUFault.Valve[3] = (short) ((blePacketHeader.data[5] & 8) >> 3);
            this.mCommService.alp3Device.canpng65280ECUFault.Valve[4] = (short) ((blePacketHeader.data[5] & 16) >> 4);
            this.mCommService.alp3Device.canpng65280ECUFault.Valve[5] = (short) ((blePacketHeader.data[5] & 32) >> 5);
            this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorRange[0] = (short) (blePacketHeader.data[6] & 1);
            this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorRange[1] = (short) ((blePacketHeader.data[6] & 2) >> 1);
            this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorRange[2] = (short) ((blePacketHeader.data[6] & 4) >> 2);
            this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorRange[3] = (short) ((blePacketHeader.data[6] & 8) >> 3);
            this.mCommService.alp3Device.canpng65280ECUFault.LeakDetect[0] = (short) (blePacketHeader.data[7] & 1);
            this.mCommService.alp3Device.canpng65280ECUFault.LeakDetect[1] = (short) ((blePacketHeader.data[7] & 2) >> 1);
            this.mCommService.alp3Device.canpng65280ECUFault.LeakDetect[2] = (short) ((blePacketHeader.data[7] & 4) >> 2);
            this.mCommService.alp3Device.canpng65280ECUFault.LeakDetect[3] = (short) ((8 & blePacketHeader.data[7]) >> 3);
            this.mCommService.alp3Device.canpng65280ECUFault.BTfault = (short) ((blePacketHeader.data[7] & 16) >> 4);
            this.mCommService.alp3Device.canpng65280ECUFault.MinHeight = (short) ((blePacketHeader.data[7] & 32) >> 5);
            this.mCommService.alp3Device.canpng65280ECUFault.VersionFault = (short) ((blePacketHeader.data[7] & 64) >> 6);
        } else if (i == 65513) {
            if (this.debugPackets.booleanValue()) {
                Log.v(TAG, "CAN_PNG_65513_EE_MEMORY_REPLY_ID");
            }
            this.mCommService.alp3Device.canpng65350ReplyRecord.Action = blePacketHeader.data[0];
            this.mCommService.alp3Device.canpng65350ReplyRecord.RecordID = blePacketHeader.data[1];
            this.mCommService.alp3Device.canpng65350ReplyRecord.VariableID = blePacketHeader.data[2];
            this.mCommService.alp3Device.canpng65350ReplyRecord.Variable = blePacketHeader.data[6] << 24;
            this.mCommService.alp3Device.canpng65350ReplyRecord.Variable += blePacketHeader.data[5] << 16;
            this.mCommService.alp3Device.canpng65350ReplyRecord.Variable += blePacketHeader.data[4] << 8;
            this.mCommService.alp3Device.canpng65350ReplyRecord.Variable += blePacketHeader.data[3];
            this.mCommService.alp3Device.canpng65350ReplyRecord.Valid = (short) 1;
        } else if (i != 65533) {
            switch (i) {
                case ALP3Protocol.CAN_PNG_65300_ECU_STATUS_ID /* 65300 */:
                    this.mCommService.alp3Device.canpng65300ECUStatus.StateIsOK = (short) (blePacketHeader.data[0] & 3);
                    this.mCommService.alp3Device.canpng65300ECUStatus.StateIsPressure = (short) ((blePacketHeader.data[0] >> 2) & 3);
                    this.mCommService.alp3Device.canpng65300ECUStatus.StatePCcontrolled = (short) ((blePacketHeader.data[0] >> 4) & 3);
                    this.mCommService.alp3Device.canpng65300ECUStatus.PriMode = blePacketHeader.data[1];
                    this.mCommService.alp3Device.canpng65300ECUStatus.SubMode = blePacketHeader.data[2];
                    this.mCommService.alp3Device.canpng65300ECUStatus.ControlMode = blePacketHeader.data[3];
                    this.mCommService.alp3Device.canpng65300ECUStatus.SubControlMode = blePacketHeader.data[4];
                    this.mCommService.alp3Device.canpng65300ECUStatus.SAwithControl = blePacketHeader.data[5];
                    this.mCommService.alp3Device.canpng65300ECUStatus.ECUControlSequence = blePacketHeader.data[6];
                    this.mCommService.alp3Device.displaySettings.ShowMode = (blePacketHeader.data[7] & 1) > 0;
                    this.mCommService.commStatus.WeHaveControl = this.mCommService.alp3Device.canpng65300ECUStatus.SAwithControl == 3;
                    if (this.mCommService.canpng65400UIStatus.Status == 0) {
                        this.mCommService.canpng65400UIStatus.Status = (short) 16;
                    }
                    this.mCommService.commStatus.ConnectedTick = COM_CONNECT_TIMEOUT;
                    break;
                case ALP3Protocol.CAN_PNG_65301_SPRING_PRESSURE_ID /* 65301 */:
                    if (this.debugPackets.booleanValue()) {
                        Log.v(TAG, "CAN_PNG_65301_SPRING_PRESSURE_ID");
                    }
                    this.mCommService.alp3Device.canpng65301ECUSpringPressure.Pressure[0] = blePacketHeader.data[0] + ((blePacketHeader.data[1] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65301ECUSpringPressure.Pressure[1] = blePacketHeader.data[2] + ((blePacketHeader.data[3] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65301ECUSpringPressure.Pressure[2] = blePacketHeader.data[4] + ((blePacketHeader.data[5] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65301ECUSpringPressure.Pressure[3] = blePacketHeader.data[6] + ((blePacketHeader.data[7] << 8) & 65280);
                    for (byte b = 0; b < 4; b = (byte) (b + 1)) {
                        this.mCommService.alp3Device.canpng65301ECUSpringPressure.filter32_tauX(b, this.mCommService.alp3Device.displaySettings.RefreshRate * 4);
                    }
                    break;
                case ALP3Protocol.CAN_PNG_65302_SPRING_HEIGHT_ID /* 65302 */:
                    if (this.debugPackets.booleanValue()) {
                        Log.v(TAG, "CAN_PNG_65302_SPRING_HEIGHT_ID");
                    }
                    this.mCommService.alp3Device.canpng65302ECUSpringHeight.Height[0] = blePacketHeader.data[0] + ((blePacketHeader.data[1] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65302ECUSpringHeight.Height[1] = blePacketHeader.data[2] + ((blePacketHeader.data[3] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65302ECUSpringHeight.Height[2] = blePacketHeader.data[4] + ((blePacketHeader.data[5] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65302ECUSpringHeight.Height[3] = blePacketHeader.data[6] + ((blePacketHeader.data[7] << 8) & 65280);
                    break;
                case ALP3Protocol.CAN_PNG_65303_ANALOG_STATUS_ID /* 65303 */:
                    if (this.debugPackets.booleanValue()) {
                        Log.v(TAG, "CAN_PNG_65303_ANALOG_STATUS_ID");
                    }
                    this.mCommService.alp3Device.canpng65303ECUAnalog.Tank = blePacketHeader.data[0] + ((blePacketHeader.data[1] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65301ECUSpringPressure.Pressure[4] = blePacketHeader.data[0] + ((blePacketHeader.data[1] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65301ECUSpringPressure.filter32_tauX((byte) 4, this.mCommService.alp3Device.displaySettings.RefreshRate * 4);
                    this.mCommService.alp3Device.canpng65303ECUAnalog.Ext5vSense = blePacketHeader.data[2] + ((blePacketHeader.data[3] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65303ECUAnalog.PCBtemperature = blePacketHeader.data[4] + ((blePacketHeader.data[5] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65303ECUAnalog.Input1 = blePacketHeader.data[6] + ((blePacketHeader.data[7] << 8) & 65280);
                    break;
                case ALP3Protocol.CAN_PNG_65304_IO_STATUS_ID /* 65304 */:
                    if (this.debugPackets.booleanValue()) {
                        Log.v(TAG, "CAN_PNG_65304_IO_STATUS_ID");
                    }
                    this.mCommService.commStatus.lastStatusTimeMs = 0L;
                    if ((blePacketHeader.data[3] & 3) == 1) {
                        this.mCommService.alp3Device.canpng65304ECUIO.Output[0] = 1;
                    } else {
                        this.mCommService.alp3Device.canpng65304ECUIO.Output[0] = 0;
                    }
                    if (((blePacketHeader.data[3] >> 2) & 3) == 1) {
                        this.mCommService.alp3Device.canpng65304ECUIO.Output[1] = 1;
                        break;
                    } else {
                        this.mCommService.alp3Device.canpng65304ECUIO.Output[1] = 0;
                        break;
                    }
                case ALP3Protocol.CAN_PNG_65305_TARGET_HEIGHT_ID /* 65305 */:
                    if (this.debugPackets.booleanValue()) {
                        Log.v(TAG, "CAN_PNG_65305_TARGET_HEIGHT_ID");
                    }
                    this.mCommService.alp3Device.canpng65305ECUTargetHeight.Height[0] = blePacketHeader.data[0] + ((blePacketHeader.data[1] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65305ECUTargetHeight.Height[1] = blePacketHeader.data[2] + ((blePacketHeader.data[3] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65305ECUTargetHeight.Height[2] = blePacketHeader.data[4] + ((blePacketHeader.data[5] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65305ECUTargetHeight.Height[3] = blePacketHeader.data[6] + ((blePacketHeader.data[7] << 8) & 65280);
                    break;
                case ALP3Protocol.CAN_PNG_65306_TARGET_PRESSURE_ID /* 65306 */:
                    if (this.debugPackets.booleanValue()) {
                        Log.v(TAG, "CAN_PNG_65306_TARGET_PRESSURE_ID");
                    }
                    this.mCommService.alp3Device.canpng65306ECUTargetPressure.Pressure[0] = blePacketHeader.data[0] + ((blePacketHeader.data[1] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65306ECUTargetPressure.Pressure[1] = blePacketHeader.data[2] + ((blePacketHeader.data[3] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65306ECUTargetPressure.Pressure[2] = blePacketHeader.data[4] + ((blePacketHeader.data[5] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65306ECUTargetPressure.Pressure[3] = blePacketHeader.data[6] + ((blePacketHeader.data[7] << 8) & 65280);
                    break;
                case ALP3Protocol.CAN_PNG_65307_ACCEL_ID /* 65307 */:
                    if (this.debugPackets.booleanValue()) {
                        Log.v(TAG, "CAN_PNG_65307_ACCEL_ID");
                    }
                    this.mCommService.alp3Device.canpng65307ECUAccel.xAxis = blePacketHeader.data[0] + ((blePacketHeader.data[1] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65307ECUAccel.yAxis = blePacketHeader.data[2] + ((blePacketHeader.data[3] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65307ECUAccel.zAxis = blePacketHeader.data[4] + ((blePacketHeader.data[5] << 8) & 65280);
                    if ((blePacketHeader.data[6] & 3) == 1) {
                        this.mCommService.alp3Device.canpng65307ECUAccel.IsMoving = 1;
                    } else if ((blePacketHeader.data[6] & 3) == 0) {
                        this.mCommService.alp3Device.canpng65307ECUAccel.IsMoving = 0;
                    } else {
                        this.mCommService.alp3Device.canpng65307ECUAccel.IsMoving = 255;
                    }
                    if (((blePacketHeader.data[6] >> 2) & 3) == 1) {
                        this.mCommService.alp3Device.canpng65307ECUAccel.IsAccelerating = 1;
                        break;
                    } else if (((blePacketHeader.data[6] >> 2) & 3) == 0) {
                        this.mCommService.alp3Device.canpng65307ECUAccel.IsAccelerating = 0;
                        break;
                    } else {
                        this.mCommService.alp3Device.canpng65307ECUAccel.IsAccelerating = 255;
                        break;
                    }
                case ALP3Protocol.CAN_PNG_65308_SPRING_HEIGHTAD_ID /* 65308 */:
                    if (this.debugPackets.booleanValue()) {
                        Log.v(TAG, "CAN_PNG_65308_SPRING_HEIGHTAD_ID");
                    }
                    this.mCommService.alp3Device.canpng65308ECUSpringHeightAD.Height[0] = blePacketHeader.data[0] + ((blePacketHeader.data[1] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65308ECUSpringHeightAD.Height[1] = blePacketHeader.data[2] + ((blePacketHeader.data[3] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65308ECUSpringHeightAD.Height[2] = blePacketHeader.data[4] + ((blePacketHeader.data[5] << 8) & 65280);
                    this.mCommService.alp3Device.canpng65308ECUSpringHeightAD.Height[3] = blePacketHeader.data[6] + ((blePacketHeader.data[7] << 8) & 65280);
                    break;
                default:
                    Log.e(TAG, "RX Packet not processed. PNG:" + String.valueOf(i));
                    break;
            }
        } else {
            if (this.debugPackets.booleanValue()) {
                Log.v(TAG, "CAN_PNG_65531_VARIABLE_REPLY_ID");
            }
            this.mCommService.alp3Device.canpng65351ReplyVariable.Action = blePacketHeader.data[0];
            this.mCommService.alp3Device.canpng65351ReplyVariable.VariableID = blePacketHeader.data[1];
            this.mCommService.alp3Device.canpng65351ReplyVariable.Variable = blePacketHeader.data[5] << 24;
            this.mCommService.alp3Device.canpng65351ReplyVariable.Variable += blePacketHeader.data[4] << 16;
            this.mCommService.alp3Device.canpng65351ReplyVariable.Variable += blePacketHeader.data[3] << 8;
            this.mCommService.alp3Device.canpng65351ReplyVariable.Variable += blePacketHeader.data[2];
            this.mCommService.alp3Device.canpng65351ReplyVariable.Valid = (short) 1;
        }
        if (this.mCommService.commServiceListener != null) {
            this.mCommService.commServiceListener.onStatusUpdated();
        }
    }

    private void processLargeBtPacket(ALP3Protocol.LargeBlePacketHeader largeBlePacketHeader) {
        int i = (largeBlePacketHeader.pngUpper << 8) | largeBlePacketHeader.pngLower;
        if (i == 10013) {
            this.mCommService.alp3Device.canpng65350ReplySector.Action = largeBlePacketHeader.data[0];
            this.mCommService.alp3Device.canpng65350ReplySector.RecordID = largeBlePacketHeader.data[1];
            for (int i2 = 0; i2 < 31; i2++) {
                int i3 = i2 * 4;
                this.mCommService.alp3Device.canpng65350ReplySector.data[i2] = largeBlePacketHeader.data[i3 + 5] << 24;
                long[] jArr = this.mCommService.alp3Device.canpng65350ReplySector.data;
                jArr[i2] = jArr[i2] + (largeBlePacketHeader.data[i3 + 4] << 16);
                long[] jArr2 = this.mCommService.alp3Device.canpng65350ReplySector.data;
                jArr2[i2] = jArr2[i2] + (largeBlePacketHeader.data[i3 + 3] << 8);
                long[] jArr3 = this.mCommService.alp3Device.canpng65350ReplySector.data;
                jArr3[i2] = jArr3[i2] + largeBlePacketHeader.data[i3 + 2];
            }
            this.mCommService.alp3Device.canpng65350ReplySector.Valid = (short) 1;
            Log.v(TAG, "RX CAN_PNG_10010_EE_SECTOR_REPLY_ID Command");
            return;
        }
        Log.e(TAG, "RX Large Packet not processed. PNG:" + String.valueOf(i));
    }
}
