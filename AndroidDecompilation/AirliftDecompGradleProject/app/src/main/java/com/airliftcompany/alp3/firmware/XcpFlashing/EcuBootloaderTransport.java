package com.airliftcompany.alp3.firmware.XcpFlashing;

import com.airliftcompany.alp3.comm.CommService;
import com.airliftcompany.alp3.firmware.XcpFlashing.Exceptions.XcpTimeoutException;
import com.airliftcompany.alp3.firmware.XcpFlashing.Exceptions.XcpTransportException;
import com.google.common.base.Ascii;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public class EcuBootloaderTransport implements ITransport {
    private static final int SerialPacketChecksumLength = 1;
    private static final int SerialPacketLengthSize = 2;
    private CommService communicationService;
    private boolean isSynced;
    private static final byte[] SyncDataToSend = {Ascii.DC2, 119, 52, 119};
    private static final byte[] SyncDataToReceive = {-104, 51, 118, 51};

    public EcuBootloaderTransport(CommService commService) {
        if (commService == null) {
            throw new IllegalArgumentException("Bootloader service argument was null");
        }
        this.communicationService = commService;
    }

    @Override // com.airliftcompany.alp3.firmware.XcpFlashing.ITransport
    public void SendCommand(byte[] bArr, int i) throws InterruptedException, XcpTransportException {
        ByteBuffer allocate = ByteBuffer.allocate(bArr.length + 3);
        allocate.order(ByteOrder.LITTLE_ENDIAN);
        allocate.putShort((short) bArr.length);
        allocate.put(bArr);
        allocate.put(GetChecksum(allocate.array(), 0, allocate.array().length));
        try {
            if (!this.isSynced) {
                Synchronize(i);
            }
            this.communicationService.bleService.writeValue(allocate.array(), i);
        } catch (XcpTimeoutException e) {
            throw new XcpTransportException("Failed to send command", e);
        }
    }

    public byte GetChecksum(byte[] bArr, int i, int i2) {
        byte b = 0;
        for (int i3 = i; i3 < i2 + i; i3++) {
            b = (byte) (b + bArr[i3]);
        }
        return b;
    }

    private void Synchronize(int i) throws InterruptedException, XcpTransportException, XcpTimeoutException {
        this.communicationService.bleService.writeValue(SyncDataToSend, i);
        long currentTimeMillis = System.currentTimeMillis();
        while (true) {
            int i2 = 0;
            while (true) {
                long j = i;
                if (System.currentTimeMillis() - currentTimeMillis < j) {
                    byte[] bArr = SyncDataToReceive;
                    if (i2 == bArr.length) {
                        this.isSynced = true;
                        return;
                    }
                    Byte poll = this.communicationService.RawDataBuffer.poll(j, TimeUnit.MILLISECONDS);
                    if (poll != null && poll.byteValue() == bArr[i2]) {
                        i2++;
                    } else if (poll != null && poll.byteValue() == bArr[0]) {
                        i2 = 1;
                    }
                } else {
                    this.isSynced = false;
                    throw new XcpTransportException("Could not synchronize display.");
                }
            }
        }
    }

    @Override // com.airliftcompany.alp3.firmware.XcpFlashing.ITransport
    public ArrayList<Byte> ReceiveResponse(int i) throws XcpTimeoutException, InterruptedException, XcpTransportException {
        long currentTimeMillis = System.currentTimeMillis();
        ByteBuffer allocate = ByteBuffer.allocate(2);
        allocate.order(ByteOrder.LITTLE_ENDIAN);
        while (allocate.position() < allocate.capacity()) {
            long j = i;
            if (System.currentTimeMillis() - currentTimeMillis > j) {
                throw new XcpTimeoutException("Bootloader failed to receive length.");
            }
            Byte poll = this.communicationService.RawDataBuffer.poll(j, TimeUnit.MILLISECONDS);
            if (poll != null) {
                allocate.put(poll.byteValue());
            }
        }
        allocate.flip();
        short s = allocate.getShort();
        ArrayList<Byte> arrayList = new ArrayList<>();
        while (arrayList.size() < s) {
            long j2 = i;
            if (System.currentTimeMillis() - currentTimeMillis > j2) {
                throw new XcpTimeoutException("Bootloader failed to receive data.");
            }
            Byte poll2 = this.communicationService.RawDataBuffer.poll(j2, TimeUnit.MILLISECONDS);
            if (poll2 != null) {
                arrayList.add(poll2);
            }
        }
        Byte poll3 = this.communicationService.RawDataBuffer.poll(i, TimeUnit.MILLISECONDS);
        if (poll3 == null) {
            throw new XcpTimeoutException("Bootloader failed to receive checksum.");
        }
        ByteBuffer allocate2 = ByteBuffer.allocate(s + 2);
        allocate2.put(allocate.array());
        for (int i2 = 0; i2 < arrayList.size(); i2++) {
            allocate2.put(arrayList.get(i2).byteValue());
        }
        if (poll3.byteValue() == GetChecksum(allocate2.array(), 0, allocate2.capacity())) {
            return arrayList;
        }
        throw new XcpTransportException("Bootloader packet checksum failed.");
    }
}
