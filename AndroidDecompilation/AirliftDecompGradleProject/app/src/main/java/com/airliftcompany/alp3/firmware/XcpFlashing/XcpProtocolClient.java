package com.airliftcompany.alp3.firmware.XcpFlashing;

import android.util.Log;
import com.airliftcompany.alp3.firmware.XcpFlashing.Exceptions.XcpResponseException;
import com.airliftcompany.alp3.firmware.XcpFlashing.Exceptions.XcpTimeoutException;
import com.airliftcompany.alp3.firmware.XcpFlashing.Exceptions.XcpTransportException;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class XcpProtocolClient {
    private static final byte ERR = -2;

    /* renamed from: OK */
    private static final byte f60OK = -1;
    public ITransport transport;
    private int Timeout = 3000;
    private int ProgramClearTimeout = 10000;

    public void Connect() throws XcpTimeoutException, XcpResponseException {
        XcpCommands.ParseConnectResponse(SendReceive(XcpCommands.ConnectCommand(), this.Timeout));
    }

    public int GetSeed() throws XcpTimeoutException, XcpResponseException {
        return XcpCommands.ParseGetSeedResponse(SendReceive(XcpCommands.GetSeedCommand(), this.Timeout));
    }

    public void Unlock(int i) throws XcpTimeoutException {
        SendReceive(XcpCommands.UnlockCommand(i), this.Timeout);
    }

    public void ProgramPrepare(int i) throws XcpTimeoutException {
        SendReceive(XcpCommands.ProgramPrepareCommand(i), this.Timeout);
    }

    public void SetMta(int i) throws XcpTimeoutException {
        SendReceive(XcpCommands.SetMemoryTransferAddressCommand(i), this.Timeout);
    }

    public void Download(byte[] bArr, int i, int i2) throws XcpTimeoutException {
        SendReceive(XcpCommands.DownloadCommand(bArr, i, i2), this.Timeout);
    }

    public void ProgramStart() throws XcpResponseException, XcpTimeoutException {
        XcpCommands.ParseProgramStartResponse(SendReceive(XcpCommands.ProgramStartCommand(), this.Timeout));
    }

    public void ProgramClear(int i) throws XcpTimeoutException {
        SendReceive(XcpCommands.ProgramClearCommand(i), this.ProgramClearTimeout);
    }

    public void Program(byte[] bArr, int i, int i2, byte b) throws XcpTransportException, InterruptedException {
        this.transport.SendCommand(XcpCommands.ProgramCommand(bArr, i, i2, b), this.Timeout);
    }

    public void ProgramNext(byte[] bArr, int i, int i2) throws XcpTransportException, InterruptedException {
        this.transport.SendCommand(XcpCommands.ProgramNextCommand(bArr, i, i2), this.Timeout);
    }

    public long BuildChecksum(int i) throws XcpTimeoutException, XcpResponseException {
        return XcpCommands.ParseBuildChecksumResponse(SendReceive(XcpCommands.BuildChecksumCommand(i), this.Timeout));
    }

    public void ProgramReset() {
        try {
            SendReceive(XcpCommands.ProgramResetCommand(), this.Timeout);
            Log.e("XCP ERROR", "Got a program reset response");
        } catch (Exception unused) {
        }
    }

    public ArrayList<Byte> ReceiveResponse(int i) throws XcpResponseException, XcpTimeoutException {
        try {
            ArrayList<Byte> ReceiveResponse = this.transport.ReceiveResponse(i);
            if (ReceiveResponse == null) {
                throw new AssertionError();
            }
            if (ReceiveResponse.size() == 0) {
                throw new AssertionError();
            }
            byte byteValue = ReceiveResponse.get(0).byteValue();
            if (byteValue != -2) {
                if (byteValue == -1) {
                    return ReceiveResponse;
                }
                throw new XcpResponseException("Received invalid response code.");
            }
            Log.e("XCP ERROR RESPONSE " + ReceiveResponse.get(1), "An error occurred!");
            throw new XcpResponseException("Received XCP error response " + ReceiveResponse.get(1));
        } catch (XcpTimeoutException e) {
            throw new XcpTimeoutException("Response timed out.", e);
        } catch (XcpTransportException e2) {
            throw new XcpTimeoutException("Transport protocol error occurred.", e2);
        } catch (InterruptedException e3) {
            throw new XcpTimeoutException("Read was interrupted.", e3);
        }
    }

    private ArrayList<Byte> SendReceive(byte[] bArr, int i) throws XcpTimeoutException {
        int i2 = 0;
        while (true) {
            try {
                this.transport.SendCommand(bArr, i);
                return ReceiveResponse(i);
            } catch (XcpResponseException e) {
                if (i2 > 2) {
                    throw new XcpTimeoutException("Failed to receive data.", e);
                }
            } catch (XcpTransportException e2) {
                if (i2 > 2) {
                    throw new XcpTimeoutException("Could not send data.", e2);
                }
            } catch (InterruptedException e3) {
                throw new XcpTimeoutException("Read was interrupted.", e3);
            }
            i2++;
        }
    }
}
