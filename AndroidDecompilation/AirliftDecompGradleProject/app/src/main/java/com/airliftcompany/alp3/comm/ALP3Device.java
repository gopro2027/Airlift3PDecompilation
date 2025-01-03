package com.airliftcompany.alp3.comm;

import com.airliftcompany.alp3.comm.ALP3Protocol;

/* loaded from: classes.dex */
public class ALP3Device {
    public static final short ALL_DOWN_BUTTON = 5;
    public static final short ALL_UP_BUTTON = 4;
    public static final short DIRECTION_DOWN = 2;
    public static final short DIRECTION_FULL_DOWN = 4;
    public static final short DIRECTION_FULL_UP = 3;
    public static final short DIRECTION_UP = 1;
    public static final short LF_BUTTON = 0;
    public static final short LR_BUTTON = 2;
    public static final short PRESET_ALL_DOWN = 4;
    public static final short PRESET_ALL_UP = 3;
    public static final short PRESET_DOWN = 2;
    public static final short PRESET_MODE_DOUBLE = 0;
    public static final short PRESET_MODE_SINGLE = 1;
    public static final short PRESET_NORMAL = 0;
    public static final short PRESET_UP = 1;
    public static final short RF_BUTTON = 1;
    public static final short RR_BUTTON = 3;
    public boolean haveMacAndSerial;
    public boolean versionChecked;
    public boolean versionMismatch;
    public CANPNG65300ECUStatus canpng65300ECUStatus = new CANPNG65300ECUStatus();
    public CANPNG65403DirectControl canpng65403DirectControl = new CANPNG65403DirectControl();
    public CANPNG65404ManualControl canpng65404ManualControl = new CANPNG65404ManualControl();
    public Presets presets = new Presets();
    public Presets adjustingPresets = new Presets();
    public DisplaySettings displaySettings = new DisplaySettings();
    public CalibrationSettings calibrationSettings = new CalibrationSettings();
    public CANPNG65301ECUSpringPressure canpng65301ECUSpringPressure = new CANPNG65301ECUSpringPressure();
    public CANPNG65302ECUSpringHeight canpng65302ECUSpringHeight = new CANPNG65302ECUSpringHeight();
    public CANPNG65303ECUAnalog canpng65303ECUAnalog = new CANPNG65303ECUAnalog();
    public CANPNG65304ECUIO canpng65304ECUIO = new CANPNG65304ECUIO();
    public CANPNG65305ECUTargetHeight canpng65305ECUTargetHeight = new CANPNG65305ECUTargetHeight();
    public CANPNG65306ECUTargetPressure canpng65306ECUTargetPressure = new CANPNG65306ECUTargetPressure();
    public CANPNG65307ECUAccel canpng65307ECUAccel = new CANPNG65307ECUAccel();
    public CANPNG65308ECUSpringHeightAD canpng65308ECUSpringHeightAD = new CANPNG65308ECUSpringHeightAD();
    public CANPNG65350ReplySector canpng65350ReplySector = new CANPNG65350ReplySector();
    public CANPNG65350ReplyRecord canpng65350ReplyRecord = new CANPNG65350ReplyRecord();
    public CANPNG65351ReplyVariable canpng65351ReplyVariable = new CANPNG65351ReplyVariable();
    public CANPNG65280ECUFault canpng65280ECUFault = new CANPNG65280ECUFault();
    public BleRxStatusStruct bleRxStatusStruct = new BleRxStatusStruct();
    public String ECUVersion = "";
    public String ECUAPIVersion = "";
    public String DisplayVersion = "";
    public String DisplayAPIVersion = "";
    public String Serial = "";
    public String MacAddress = "";

    public enum KeypadAllDownButtonsEnum {
        KEYPAD_ALL_DOWN,
        KEYPAD_FRONT_DOWN,
        KEYPAD_ALL_DOWN_IS_PRESET,
        KEYPAD_AIR_OUT
    }

    public enum KeypadAllUpButtonsEnum {
        KEYPAD_ALL_UP,
        KEYPAD_FRONT_UP,
        KEYPAD_ALL_UP_IS_PRESET
    }

    public class CANPNG65300ECUStatus {
        public short ControlMode;
        public short ECUControlSequence;
        public short PowerUpState;
        public int PowerUpWaitCounter;
        public short PriMode;
        public short SAwithControl;
        public short StateIsOK;
        public short StateIsPressure;
        public short StatePCcontrolled;
        public short SubControlMode;
        public short SubMode;
        public boolean overrideCalibration;

        public CANPNG65300ECUStatus() {
        }

        public ALP3Protocol.ECU_PrimaryControlStateEnum priModeEnum() {
            return ALP3Protocol.ECU_PrimaryControlStateEnum.values()[this.PriMode];
        }

        public ALP3Protocol.HeightMode_NormalModeStateEnum heightModeSubModeEnum() {
            return ALP3Protocol.HeightMode_NormalModeStateEnum.values()[this.SubMode];
        }

        public ALP3Protocol.PressureMode_NormalModeStateEnum pressureModeSubModeEnum() {
            return ALP3Protocol.PressureMode_NormalModeStateEnum.values()[this.SubMode];
        }

        public ALP3Protocol.ECU_PrimaryControlStateEnum controlStateEnum() {
            return ALP3Protocol.ECU_PrimaryControlStateEnum.values()[this.ControlMode];
        }

        public ALP3Protocol.ECU_CalPressureControlStateEnum calPressureControlStateEnum() {
            return ALP3Protocol.ECU_CalPressureControlStateEnum.values()[this.SubMode];
        }

        public ALP3Protocol.ECU_CalLimitsStateEnum calLimitsStateEnum() {
            return ALP3Protocol.ECU_CalLimitsStateEnum.values()[this.SubMode];
        }

        public ALP3Protocol.ECU_ManualCalModeStateEnum manualCalLimitsStateEnum() {
            return ALP3Protocol.ECU_ManualCalModeStateEnum.values()[this.SubMode];
        }

        public boolean pressureMode() {
            return ALP3Device.this.canpng65300ECUStatus.StateIsPressure > 0;
        }
    }

    public class CANPNG65403DirectControl {
        public int TimeOutTick;
        public int[] valve = new int[6];
        public int[] output = new int[4];

        public CANPNG65403DirectControl() {
        }
    }

    public class CANPNG65404ManualControl {
        public int Direction;
        public int[] Spring = new int[4];
        public int TimeOutTick;

        private boolean directionIsUp(int i) {
            return i == 1 || i == 3;
        }

        public CANPNG65404ManualControl() {
        }

        public boolean valvesAreClosed() {
            int[] iArr = this.Spring;
            return iArr[0] == 0 && iArr[1] == 0 && iArr[2] == 0 && iArr[3] == 0;
        }

        public boolean checkDirectionIsAllowed(int i) {
            int i2 = this.Direction;
            return i2 == 0 || i2 == i || directionIsUp(i2) == directionIsUp(i);
        }
    }

    public class CANPNG65301ECUSpringPressure {
        public long[] Pressure = new long[5];
        public long[] PressureFiltered = new long[5];
        public long[] PressureAccm = new long[5];

        public CANPNG65301ECUSpringPressure() {
        }

        public void filter32_tauX(byte b, int i) {
            if (i == 0) {
                i = 4;
            }
            long[] jArr = this.PressureAccm;
            long j = jArr[b];
            long j2 = i;
            long j3 = (j - (j / j2)) + ((this.Pressure[b] << 14) / j2);
            jArr[b] = j3 & (-1);
            this.PressureFiltered[b] = (j3 >> 14) & (-1);
        }

        public void filter32_tauX_PreData(byte b) {
            this.PressureAccm[b] = (this.Pressure[b] << 14) & (-1);
        }
    }

    public class CANPNG65302ECUSpringHeight {
        public int[] Height = new int[4];

        public CANPNG65302ECUSpringHeight() {
        }
    }

    public class CANPNG65303ECUAnalog {
        public int Ext5vSense;
        public int Input1;
        public int PCBtemperature;
        public int Tank;

        public CANPNG65303ECUAnalog() {
        }
    }

    public class CANPNG65304ECUIO {
        public int[] Valve = new int[5];
        public int[] Input = new int[4];
        public int[] Output = new int[2];

        public CANPNG65304ECUIO() {
        }
    }

    public class CANPNG65305ECUTargetHeight {
        public int[] Height = new int[4];

        public CANPNG65305ECUTargetHeight() {
        }
    }

    public class CANPNG65306ECUTargetPressure {
        public int[] Pressure = new int[4];

        public CANPNG65306ECUTargetPressure() {
        }
    }

    public class CANPNG65307ECUAccel {
        public int IsAccelerating;
        public int IsMoving;
        public int xAxis;
        public int yAxis;
        public int zAxis;

        public CANPNG65307ECUAccel() {
        }
    }

    public class CANPNG65308ECUSpringHeightAD {
        public int[] Height = new int[4];

        public CANPNG65308ECUSpringHeightAD() {
        }
    }

    public class CANPNG65350ReplySector {
        public short Action;
        public short RecordID;
        public short Valid;
        public short VariableID;
        public long[] data = new long[32];

        public CANPNG65350ReplySector() {
        }

        public void clearRecord() {
            this.Valid = (short) 0;
            this.VariableID = (short) 0;
            this.RecordID = (short) 0;
            this.Action = (short) 0;
            for (int i = 0; i < 32; i++) {
                this.data[i] = 0;
            }
        }
    }

    public class CANPNG65350ReplyRecord {
        public short Action;
        public short RecordID;
        public short Valid;
        public long Variable;
        public short VariableID;

        public CANPNG65350ReplyRecord() {
        }

        public void clearRecord() {
            this.Valid = (short) 0;
            this.VariableID = (short) 0;
            this.RecordID = (short) 0;
            this.Action = (short) 0;
            this.Variable = 0L;
        }
    }

    public class CANPNG65351ReplyVariable {
        public short Action;
        public short Valid;
        public long Variable;
        public short VariableID;

        public CANPNG65351ReplyVariable() {
        }

        public void clearRecord() {
            this.Valid = (short) 0;
            this.VariableID = (short) 0;
            this.Action = (short) 0;
            this.Variable = 0L;
        }
    }

    public class CANPNG65280ECUFault {
        public short AccelCal;
        public short BTfault;
        public short CompressorFreeze;
        public short CompressorOverrun;
        public short ECUoverTemp;
        public short FactoryCal;
        public short HeightCal;
        public short HeightLimitCal;
        public short HighVoltage;
        public short InvaildMount;
        public short LowVoltage;
        public short MinHeight;
        public short PressureCal;
        public short TankPressureLow;
        public short VersionFault;
        public short[] HeightSensorLimit = new short[4];
        public short[] HeightSensorNotPresent = new short[4];
        public short[] HeightSensorRange = new short[4];
        public short[] PressureSensor = new short[5];
        public short[] LeakDetect = new short[4];
        public short[] Valve = new short[6];

        public CANPNG65280ECUFault() {
        }
    }

    public class BleRxStatusStruct {
        public int bufferMaxError;
        public int currentBufferlength;
        public int dataInvalidError;
        public int packetsReceived;

        public BleRxStatusStruct() {
        }
    }

    public class DisplaySettings {
        public int AllDown;
        public int AllUp;
        public int DisplayBrightness;
        public int DisplayBrightnessInSleep;
        public int KeypadBrightness;
        public int KeypadBrightnessInSleep;
        public int PresetMode;
        public int RefreshRate;
        public int Rotation;
        public int[] SensorIsInverted = new int[4];
        public boolean ShowMode;
        public int Sleeptime;
        public boolean Synced;
        public int Units;

        public DisplaySettings() {
        }

        public KeypadAllUpButtonsEnum allUpButtonsEnum() {
            return KeypadAllUpButtonsEnum.values()[this.AllUp];
        }

        public KeypadAllDownButtonsEnum allDownButtonsEnum() {
            return KeypadAllDownButtonsEnum.values()[this.AllDown];
        }
    }

    public class CalibrationSettings {
        public int Accel;
        public int AccelMount;
        public int HeightAlg;
        public int HeightLimit;
        public int PressureAlg;
        public int PressureOnly;
        public boolean Synced;
        public int calibrationCount;
        public int calibrationStep;
        public boolean needsCalibration;
        public boolean presentCalibration;
        public boolean wizardHeightIsAuto;
        public boolean wizardPressureIsAuto;

        public CalibrationSettings() {
        }
    }

    public class Presets {
        public long[] Height = new long[20];
        public long[] Pressure = new long[20];
        public boolean Synced;
        public boolean amSavingPreset;
        public int presetAdjusting;

        public Presets() {
        }

        public void incrementPresetValue(int i) {
            if (ALP3Device.this.canpng65300ECUStatus.StateIsPressure == 1) {
                long[] jArr = this.Pressure;
                if (jArr[i] < 2000) {
                    jArr[i] = jArr[i] + 10;
                    return;
                } else {
                    jArr[i] = 2000;
                    return;
                }
            }
            long[] jArr2 = this.Height;
            if (jArr2[i] < 1000) {
                jArr2[i] = jArr2[i] + 1;
            } else {
                jArr2[i] = 1000;
            }
        }

        public void decrementPresetValue(int i) {
            if (ALP3Device.this.canpng65300ECUStatus.StateIsPressure == 1) {
                long[] jArr = this.Pressure;
                if (jArr[i] > 10) {
                    jArr[i] = jArr[i] - 10;
                    return;
                } else {
                    jArr[i] = 0;
                    return;
                }
            }
            long[] jArr2 = this.Height;
            if (jArr2[i] > 0) {
                jArr2[i] = jArr2[i] - 1;
            } else {
                jArr2[i] = 0;
            }
        }
    }
}
