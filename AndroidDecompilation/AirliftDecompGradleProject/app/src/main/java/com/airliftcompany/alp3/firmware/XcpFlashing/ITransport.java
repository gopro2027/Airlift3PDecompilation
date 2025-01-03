package com.airliftcompany.alp3.firmware.XcpFlashing;

import com.airliftcompany.alp3.firmware.XcpFlashing.Exceptions.XcpTimeoutException;
import com.airliftcompany.alp3.firmware.XcpFlashing.Exceptions.XcpTransportException;
import java.util.ArrayList;

/* loaded from: classes.dex */
public interface ITransport {
    ArrayList<Byte> ReceiveResponse(int i) throws XcpTimeoutException, InterruptedException, XcpTransportException;

    void SendCommand(byte[] bArr, int i) throws InterruptedException, XcpTransportException;
}
