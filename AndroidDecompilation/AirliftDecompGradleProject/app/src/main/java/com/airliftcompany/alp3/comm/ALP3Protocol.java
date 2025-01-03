package com.airliftcompany.alp3.comm;

/* loaded from: classes.dex */
public interface ALP3Protocol {
    public static final short ALP3_PROTOCOL_VERSION_MAJOR_LOWER = 3;
    public static final short ALP3_PROTOCOL_VERSION_MAJOR_UPPER = 1;
    public static final short ALP3_PROTOCOL_VERSION_MINOR = 0;
    public static final short BT_ECU_SA_ID = 0;
    public static final short BT_HEADER_LENGTH = 5;
    public static final short BT_IOS_SA_ID = 3;
    public static final short BT_MESSAGE_ID = 24;
    public static final short BT_PCC_SA_ID = 2;
    public static final short CAN_BIT_ERROR = 2;
    public static final short CAN_BIT_INVALID = 3;
    public static final short CAN_BIT_OFF = 0;
    public static final short CAN_BIT_ON = 1;
    public static final int CAN_GLOBAL_PACKET_TIMOUT = 3000;
    public static final int CAN_PNG_10000_EE_SECTOR_ID = 10000;
    public static final int CAN_PNG_10010_EE_SECTOR_REPLY_ID = 10013;
    public static final int CAN_PNG_20000_CONNECT_BT_ID = 20000;
    public static final int CAN_PNG_20010_CONNECT_REPLY_ID = 20013;
    public static final int CAN_PNG_65280_ECU_FAULT_ID = 65280;
    public static final int CAN_PNG_65300_ECU_STATUS_ID = 65300;
    public static final int CAN_PNG_65301_SPRING_PRESSURE_ID = 65301;
    public static final int CAN_PNG_65302_SPRING_HEIGHT_ID = 65302;
    public static final int CAN_PNG_65303_ANALOG_STATUS_ID = 65303;
    public static final int CAN_PNG_65304_IO_STATUS_ID = 65304;
    public static final int CAN_PNG_65305_TARGET_HEIGHT_ID = 65305;
    public static final int CAN_PNG_65306_TARGET_PRESSURE_ID = 65306;
    public static final int CAN_PNG_65307_ACCEL_ID = 65307;
    public static final int CAN_PNG_65308_SPRING_HEIGHTAD_ID = 65308;
    public static final int CAN_PNG_65400_CONTROL_STATUS_ID = 65400;
    public static final int CAN_PNG_65401_PRESSURE_CONTROL_ID = 65401;
    public static final int CAN_PNG_65402_HEIGHT_CONTROL_ID = 65402;
    public static final int CAN_PNG_65403_DIRECT_CONTROL_ID = 65403;
    public static final int CAN_PNG_65404_MANUAL_CONTROL_ID = 65404;
    public static final int CAN_PNG_65500_EE_MEMORY_ID = 65500;
    public static final int CAN_PNG_65513_EE_MEMORY_REPLY_ID = 65513;
    public static final int CAN_PNG_65520_VARIABLE_ID = 65520;
    public static final int CAN_PNG_65531_VARIABLE_REPLY_ID = 65533;
    public static final short PREAMBLE_LENGTH = 4;

    public enum ECU_CalLimitsStateEnum {
        OP_MODE_CAL_LIMIT_WAIT_GO,
        OP_MODE_CAL_LIMIT_START,
        OP_MODE_CAL_LIMIT_MOVE_UP,
        OP_MODE_CAL_LIMIT_LOWER,
        OP_MODE_CAL_LIMIT_CALCULATE,
        OP_MODE_CAL_LIMIT_CHECK_CORNER_PRESSURIZE,
        OP_MODE_CAL_LIMIT_CHECK_LFCORNER,
        OP_MODE_CAL_LIMIT_CHECK_RFCORNER,
        OP_MODE_CAL_LIMIT_CHECK_LRCORNER,
        OP_MODE_CAL_LIMIT_CHECK_RRCORNER,
        OP_MODE_CAL_LIMIT_SAVE,
        OP_MODE_CAL_LIMIT_FINISHED,
        OP_MODE_CAL_LIMIT_NOT_PRESENT_ERROR,
        OP_MODE_CAL_LIMIT_LIMIT_ERROR,
        OP_MODE_CAL_LIMIT_WIRE_ERROR,
        OP_MODE_CAL_LIMIT_RANGE_ERROR,
        OP_MODE_CAL_LIMIT_TIMEOUT_ERROR
    }

    public enum ECU_CalPressureControlStateEnum {
        OP_MODE_PCAL_LIMIT_WAIT_GO,
        OP_MODE_PCAL_START,
        OP_MODE_PCAL_ZERO_ALL_FRONT,
        OP_MODE_PCAL_STEP_FILL_FRONT,
        OP_MODE_PCAL_ZERO_DUMP_FRONT,
        OP_MODE_PCAL_STEP_DUMP_FRONT,
        OP_MODE_PCAL_ZERO_ALL_REAR,
        OP_MODE_PCAL_STEP_FILL_REAR,
        OP_MODE_PCAL_ZERO_DUMP_REAR,
        OP_MODE_PCAL_STEP_DUMP_REAR,
        OP_MODE_PCAL_SAVE,
        OP_MODE_PCAL_SUCCESS,
        OP_MODE_PCAL_FAULT,
        OP_MODE_PCAL_MEASURE_FILL_FRONT
    }

    public enum ECU_ManualCalModeStateEnum {
        SUB_MANUALCAL_START,
        SUB_MANUALCAL_GO_TOP,
        SUB_MANUALCAL_SAVE_TOP,
        SUB_MANUALCAL_GO_BOTTOM,
        SUB_MANUALCAL_SAVE_BOTTOM,
        SUB_MANUALCAL_FINISHED
    }

    public enum ECU_PrimaryControlStateEnum {
        OP_MODE_PRESET_HEIGHT,
        OP_MODE_MANUAL_HEIGHT,
        OP_MODE_PRESET_PRESSURE,
        OP_MODE_MANUAL_PRESSURE,
        OP_MODE_DIRECT_CONTROL,
        OP_MODE_MANUAL_CONTROL,
        OP_MODE_CALIBRATE_LIMITS,
        OP_MODE_CALIBRATE_PRESSURE,
        OP_MODE_CALIBRATE_HEIGHT,
        OP_MODE_MANUFACTURING_PRESSURE_CAL,
        OP_MODE_CALIBRATE_ACC,
        OP_MODE_INIT,
        OP_MODE_CALIBRATE_MANUAL_HEIGHT,
        OP_MODE_SHOW_MODE,
        OP_MODE_CALIBRATE_LEAK_DETECT,
        OP_MODE_CALIBRATE_MANUAL_PRESSURE,
        OP_MODE_CALIBRATION_IN_PROGRESS,
        OP_MODE_ERROR
    }

    public enum HeightMode_NormalModeStateEnum {
        SUB_HT_START,
        SUB_HT_ACCUMALATE,
        SUB_HT_MODE_PRESET_NOT_MAINTAIN,
        SUB_HT_MODE_START_FAST_ADJUST,
        SUB_HT_MODE_FAST_FILL,
        SUB_HT_MODE_FAST_DUMP,
        SUB_HT_MODE_FAST_COMPLETE,
        SUB_HT_MODE_FAST_SETTLE,
        SUB_HT_MODE_FAST_ITERATION_SHORT_WAIT,
        SUB_HT_MODE_FAST_ITERATION_LONG_WAIT,
        SUB_HT_MODE_START_SLOW_ADJUST,
        SUB_HT_MODE_SLOW_FILL,
        SUB_HT_MODE_SLOW_DUMP,
        SUB_HT_MODE_SLOW_COMPLETE,
        SUB_HT_MODE_SLOW_SETTLE,
        SUB_HT_MODE_ACCELERATING,
        SUB_HT_MODE_AXLE_EQUAL_START,
        SUB_HT_MODE_AXLE_EQUAL_FRONT,
        SUB_HT_MODE_AXLE_EQUAL_FRONT_MINHEIGHT,
        SUB_HT_MODE_AXLE_EQUAL_REAR,
        SUB_HT_MODE_AXLE_EQUAL_REAR_MINHEIGHT,
        SUB_HT_MODE_AXLE_EQUAL_COMPLETE,
        SUB_HT_MODE_CHECK_ATTAINED,
        SUB_HM_MODE_AIROUT_START,
        SUB_HM_MODE_AIROUT_SETTLE
    }

    public enum PressureMode_NormalModeStateEnum {
        SUB_PM_START,
        SUB_PM_ACCUMALATE,
        SUB_PM_MODE_START_FAST_ADJUST,
        SUB_PM_MODE_FAST_FILL,
        SUB_PM_MODE_FAST_DUMP,
        SUB_PM_MODE_FAST_COMPLETE,
        SUB_PM_MODE_FAST_SETTLE,
        SUB_PM_MODE_START_SLOW_ADJUST,
        SUB_PM_MODE_SLOW_FILL,
        SUB_PM_MODE_SLOW_DUMP,
        SUB_PM_MODE_SLOW_COMPLETE,
        SUB_PM_MODE_SLOW_SETTLE,
        SUB_PM_MODE_ACCELERATING,
        SUB_PM_MODE_AXLE_EQUAL_START,
        SUB_PM_MODE_AXLE_EQUAL_FRONT,
        SUB_PM_MODE_AXLE_EQUAL_FRONT_MINHEIGHT,
        SUB_PM_MODE_AXLE_EQUAL_REAR,
        SUB_PM_MODE_AXLE_EQUAL_REAR_MINHEIGHT,
        SUB_PM_MODE_AXLE_EQUAL_COMPLETE,
        SUB_PM_MODE_AIROUT_START,
        SUB_PM_MODE_AIROUT_SETTLE,
        SUB_PM_MODE_PRESET_MAINTAIN
    }

    public static class BlePacketHeader {
        public short checksum;
        public short[] data;
        public short length;
        public short messageId;
        public short pngLower;
        public short pngUpper;
        public short[] preamble;
        public short sourceId;
        public short[] startByte;

        public BlePacketHeader() {
            this.preamble = new short[2];
            this.startByte = new short[4];
            this.data = new short[8];
        }

        public BlePacketHeader(short[] sArr) {
            this.preamble = new short[2];
            this.startByte = new short[4];
            this.data = new short[8];
            this.messageId = sArr[1];
            this.pngLower = sArr[2];
            this.pngUpper = sArr[3];
            this.sourceId = sArr[4];
            this.length = sArr[5];
            int i = 6;
            int i2 = 0;
            while (i2 < 8) {
                this.data[i2] = sArr[i];
                i2++;
                i++;
            }
            this.checksum = sArr[i];
        }

        public short[] getData() {
            short s = this.length;
            short[] sArr = new short[s + 9 + 1];
            short[] sArr2 = this.startByte;
            int i = 0;
            sArr[0] = sArr2[0];
            sArr[1] = sArr2[1];
            sArr[2] = sArr2[2];
            sArr[3] = sArr2[3];
            sArr[4] = this.messageId;
            sArr[5] = this.pngLower;
            sArr[6] = this.pngUpper;
            sArr[7] = this.sourceId;
            sArr[8] = s;
            int i2 = 0;
            int i3 = 9;
            while (i2 < this.length) {
                sArr[i3] = this.data[i2];
                i2++;
                i3++;
            }
            for (int i4 = 4; i4 < this.length + 9; i4++) {
                i += sArr[i4];
            }
            sArr[i3] = (short) ((~i) & 255);
            return sArr;
        }
    }

    public static class LargeBlePacketHeader {
        public short checksum;
        public short length;
        public short messageId;
        public short pngLower;
        public short pngUpper;
        public short sourceId;
        public short[] preamble = new short[2];
        public short[] startByte = new short[4];
        public short[] data = new short[130];

        public LargeBlePacketHeader(short[] sArr) {
            this.messageId = sArr[1];
            this.pngLower = sArr[2];
            this.pngUpper = sArr[3];
            this.sourceId = sArr[4];
            this.length = sArr[5];
            int i = 6;
            int i2 = 0;
            while (i2 < 128) {
                this.data[i2] = sArr[i];
                i2++;
                i++;
            }
            this.checksum = sArr[i];
        }
    }
}
