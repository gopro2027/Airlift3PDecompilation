package com.airliftcompany.alp3.firmware.XcpFlashing;

import com.airliftcompany.alp3.firmware.XcpFlashing.Exceptions.XcpResponseException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class XcpCommands {
    private static final byte BUILD_CHECKSUM = -13;
    private static final byte CONNECT = -1;
    private static final byte DISCONNECT = -2;
    private static final byte DOWNLOAD = -16;
    private static final byte GET_SEED = -8;
    static int MaxCtoSize = 0;
    static int MaxDtoSize = 0;
    static int MaximumBlockSizeProgram = 0;
    static int MaximumCtoSizeProgram = 0;
    private static final byte PGM = 16;
    private static final byte PROGRAM = -48;
    private static final byte PROGRAM_CLEAR = -47;
    private static final byte PROGRAM_PREPARE = -52;
    private static final byte PROGRAM_RESET = -49;
    private static final byte PROGRAM_START = -46;
    private static final byte SET_MTA = -10;
    private static final byte UNLOCK = -9;
    private static ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    public static byte[] ProgramResetCommand() {
        return new byte[]{PROGRAM_RESET};
    }

    public static byte[] ProgramStartCommand() {
        return new byte[]{PROGRAM_START};
    }

    public static byte[] ConnectCommand() {
        return new byte[]{-1, 0};
    }

    public static void ParseConnectResponse(ArrayList<Byte> arrayList) throws XcpResponseException {
        if (arrayList.size() != 8) {
            throw new XcpResponseException("Response Length invalid");
        }
        MaxCtoSize = arrayList.get(3).byteValue() & 255;
        ByteBuffer allocate = ByteBuffer.allocate(2);
        allocate.order(byteOrder);
        allocate.put(arrayList.get(4).byteValue());
        allocate.put(arrayList.get(5).byteValue());
        allocate.flip();
        MaxDtoSize = allocate.getShort();
    }

    public static byte[] GetSeedCommand() {
        return new byte[]{GET_SEED, 0, 16};
    }

    public static int ParseGetSeedResponse(ArrayList<Byte> arrayList) throws XcpResponseException {
        if (arrayList.size() != 6) {
            throw new XcpResponseException("Response Length invalid");
        }
        ByteBuffer allocate = ByteBuffer.allocate(4);
        allocate.order(byteOrder);
        allocate.put(arrayList.get(2).byteValue());
        allocate.put(arrayList.get(3).byteValue());
        allocate.put(arrayList.get(4).byteValue());
        allocate.put(arrayList.get(5).byteValue());
        allocate.flip();
        return allocate.getInt();
    }

    public static byte[] UnlockCommand(int i) {
        ByteBuffer allocate = ByteBuffer.allocate(6);
        allocate.order(byteOrder);
        allocate.put(UNLOCK);
        allocate.put((byte) 4);
        allocate.putInt(2, i);
        return allocate.array();
    }

    public static byte[] ProgramPrepareCommand(int i) {
        ByteBuffer allocate = ByteBuffer.allocate(4);
        allocate.order(byteOrder);
        allocate.put(PROGRAM_PREPARE);
        allocate.putShort(2, (short) i);
        return allocate.array();
    }

    public static byte[] SetMemoryTransferAddressCommand(int i) {
        ByteBuffer allocate = ByteBuffer.allocate(8);
        allocate.order(byteOrder);
        allocate.put(SET_MTA);
        allocate.put(3, (byte) 0);
        allocate.putInt(4, i);
        return allocate.array();
    }

    public static byte[] DownloadCommand(byte[] bArr, int i, int i2) {
        if (i2 > MaxCtoSize - 2) {
            throw new AssertionError();
        }
        ByteBuffer allocate = ByteBuffer.allocate(i2 + 2);
        allocate.order(byteOrder);
        allocate.put(DOWNLOAD);
        allocate.put((byte) i2);
        allocate.put(bArr, i, i2);
        return allocate.array();
    }

    public static void ParseProgramStartResponse(ArrayList<Byte> arrayList) throws XcpResponseException {
        if (arrayList.size() != 7) {
            throw new XcpResponseException("Response Length invalid");
        }
        MaximumCtoSizeProgram = arrayList.get(3).byteValue() & 255;
        MaximumBlockSizeProgram = arrayList.get(4).byteValue() & 255;
    }

    public static byte[] ProgramClearCommand(int i) {
        ByteBuffer allocate = ByteBuffer.allocate(8);
        allocate.order(byteOrder);
        allocate.put(PROGRAM_CLEAR);
        allocate.put((byte) 0);
        allocate.putInt(4, i);
        return allocate.array();
    }

    public static byte[] ProgramCommand(byte[] bArr, int i, int i2, byte b) {
        ByteBuffer allocate = ByteBuffer.allocate(i2 + 2);
        allocate.order(byteOrder);
        allocate.put(PROGRAM);
        allocate.put(b);
        allocate.put(bArr, i, i2);
        return allocate.array();
    }

    public static byte[] ProgramNextCommand(byte[] bArr, int i, int i2) {
        ByteBuffer allocate = ByteBuffer.allocate(i2 + 2);
        allocate.order(byteOrder);
        allocate.put(PROGRAM);
        allocate.put((byte) i2);
        allocate.put(bArr, i, i2);
        return allocate.array();
    }

    public static byte[] BuildChecksumCommand(int i) {
        ByteBuffer allocate = ByteBuffer.allocate(8);
        allocate.order(byteOrder);
        allocate.put(BUILD_CHECKSUM);
        allocate.putInt(4, i);
        return allocate.array();
    }

    public static long ParseBuildChecksumResponse(ArrayList<Byte> arrayList) throws XcpResponseException {
        if (arrayList.size() != 8) {
            throw new XcpResponseException("Response Length invalid");
        }
        ByteBuffer allocate = ByteBuffer.allocate(4);
        allocate.order(byteOrder);
        allocate.put(arrayList.get(4).byteValue());
        allocate.put(arrayList.get(5).byteValue());
        allocate.put(arrayList.get(6).byteValue());
        allocate.put(arrayList.get(7).byteValue());
        allocate.flip();
        return allocate.getInt() & 4294967295L;
    }
}
