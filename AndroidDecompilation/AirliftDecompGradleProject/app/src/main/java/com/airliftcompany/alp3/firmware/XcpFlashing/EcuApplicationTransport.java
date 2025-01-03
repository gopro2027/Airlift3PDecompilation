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
public class EcuApplicationTransport implements ITransport {
    private static final byte[] ApplicationSyncWord = {-86, 85, -86, 86};
    private static final int EcuAppPacketChecksumLength = 1;
    private static final int EcuAppPacketHeaderLength = 5;
    private static final int EcuAppPacketIdentifierIndex = 1;
    private static final short EcuCtoIdentifier = 256;
    private static final int EcuDtoIdentifier = 257;
    private CommService communicationService;

    public EcuApplicationTransport(CommService commService) {
        if (commService == null) {
            throw new IllegalArgumentException("Application service argument was null");
        }
        this.communicationService = commService;
    }

    @Override // com.airliftcompany.alp3.firmware.XcpFlashing.ITransport
    public void SendCommand(byte[] bArr, int i) throws XcpTransportException {
        byte[] bArr2 = ApplicationSyncWord;
        ByteBuffer allocate = ByteBuffer.allocate(bArr2.length + 5 + bArr.length + 1);
        allocate.order(ByteOrder.LITTLE_ENDIAN);
        allocate.put(bArr2);
        allocate.put(Ascii.CAN);
        allocate.putShort(EcuCtoIdentifier);
        allocate.put((byte) 2);
        allocate.put((byte) bArr.length);
        allocate.put(bArr);
        allocate.put(GetChecksum(allocate.array(), bArr2.length, bArr.length + 5));
        try {
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
        return (byte) (~b);
    }

    private void Synchronize(int i) throws InterruptedException, XcpTransportException {
        long currentTimeMillis = System.currentTimeMillis();
        while (true) {
            int i2 = 0;
            while (true) {
                long j = i;
                if (System.currentTimeMillis() - currentTimeMillis < j) {
                    byte[] bArr = ApplicationSyncWord;
                    if (i2 == bArr.length) {
                        return;
                    }
                    Byte poll = this.communicationService.RawDataBuffer.poll(j, TimeUnit.MILLISECONDS);
                    if (poll != null && poll.byteValue() == bArr[i2]) {
                        i2++;
                    } else if (poll != null && poll.byteValue() == bArr[0]) {
                        i2 = 1;
                    }
                } else {
                    throw new XcpTransportException("Could not synchronize application.");
                }
            }
        }
    }

    @Override // com.airliftcompany.alp3.firmware.XcpFlashing.ITransport
    public ArrayList<Byte> ReceiveResponse(int i) throws InterruptedException, XcpTimeoutException, XcpTransportException {
        short s;
        ArrayList<Byte> arrayList;
        long currentTimeMillis = System.currentTimeMillis();
        do {
            long j = i;
            if (System.currentTimeMillis() - currentTimeMillis < j) {
                Synchronize(i);
                ArrayList arrayList2 = new ArrayList();
                while (arrayList2.size() < 5) {
                    if (System.currentTimeMillis() - currentTimeMillis > j) {
                        throw new XcpTimeoutException("Failed to receive application header.");
                    }
                    Byte poll = this.communicationService.RawDataBuffer.poll(j, TimeUnit.MILLISECONDS);
                    if (poll != null) {
                        arrayList2.add(poll);
                    }
                }
                byte byteValue = ((Byte) arrayList2.get(4)).byteValue();
                ByteBuffer allocate = ByteBuffer.allocate(2);
                allocate.order(ByteOrder.LITTLE_ENDIAN);
                allocate.put(((Byte) arrayList2.get(1)).byteValue());
                allocate.put(((Byte) arrayList2.get(2)).byteValue());
                allocate.flip();
                s = allocate.getShort();
                arrayList = new ArrayList<>();
                while (arrayList.size() < byteValue) {
                    if (System.currentTimeMillis() - currentTimeMillis > j) {
                        throw new XcpTimeoutException("Failed to receive application data.");
                    }
                    Byte poll2 = this.communicationService.RawDataBuffer.poll(j, TimeUnit.MILLISECONDS);
                    if (poll2 != null) {
                        arrayList.add(poll2);
                    }
                }
            } else {
                throw new XcpTransportException("Failed to receive application response.");
            }
        } while (s != 257);
        return arrayList;
    }
}
