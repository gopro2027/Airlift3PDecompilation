package com.airliftcompany.alp3.firmware.XcpFlashing;

import android.os.SystemClock;
import com.airliftcompany.alp3.comm.CommService;
import com.airliftcompany.alp3.firmware.ALP3Programmer;
import com.airliftcompany.alp3.firmware.FirmwareFiles.Controller;
import com.airliftcompany.alp3.firmware.FirmwareFiles.FirmwarePackage;
import com.airliftcompany.alp3.firmware.FirmwareFiles.Image;
import com.airliftcompany.alp3.firmware.FirmwareFiles.Segment;
import com.airliftcompany.alp3.firmware.XcpFlashing.Exceptions.XcpFlashException;
import com.airliftcompany.alp3.firmware.XcpFlashing.Exceptions.XcpResponseException;
import com.airliftcompany.alp3.firmware.XcpFlashing.Exceptions.XcpTimeoutException;
import com.airliftcompany.alp3.firmware.XcpFlashing.Exceptions.XcpTransportException;

/* loaded from: classes.dex */
public class XcpFlashSequence {
    private static final int InterlockAddress = 1073741824;
    private Controller DisplayController;
    private Controller EcuController;
    private byte[] InterlockBytes;
    private ALP3Programmer.ProgramFirmwareAsyncTaskListener Listener;
    private int TotalProgramSize;
    private XcpProtocolClient client;
    private CommService commService;
    private String Status = "";
    private int Progress = 0;
    private int CurrentProgramProgress = 0;

    public XcpFlashSequence(ALP3Programmer.ProgramFirmwareAsyncTaskListener programFirmwareAsyncTaskListener, CommService commService, byte[] bArr, FirmwarePackage firmwarePackage) {
        this.TotalProgramSize = 0;
        if (programFirmwareAsyncTaskListener == null || commService == null || bArr == null || firmwarePackage == null) {
            throw new IllegalArgumentException("A null argument was passed into the XcpFlashSequence");
        }
        this.client = new XcpProtocolClient();
        this.commService = commService;
        this.InterlockBytes = bArr;
        this.DisplayController = firmwarePackage.GetDisplayController();
        this.EcuController = firmwarePackage.GetEcuController();
        this.Listener = programFirmwareAsyncTaskListener;
        if (this.DisplayController != null) {
            for (int i = 0; i < this.DisplayController.Images.size(); i++) {
                Image image = this.DisplayController.Images.get(i);
                for (int i2 = 0; i2 < image.Segments.size(); i2++) {
                    this.TotalProgramSize = (int) (this.TotalProgramSize + image.Segments.get(i2).Size);
                }
            }
        }
        if (this.EcuController != null) {
            for (int i3 = 0; i3 < this.EcuController.Images.size(); i3++) {
                Image image2 = this.EcuController.Images.get(i3);
                for (int i4 = 0; i4 < image2.Segments.size(); i4++) {
                    this.TotalProgramSize = (int) (this.TotalProgramSize + image2.Segments.get(i4).Size);
                }
            }
        }
    }

    public void Flash() throws XcpFlashException {
        this.CurrentProgramProgress = 0;
        this.Progress = 0;
        UpdateStatus("Preparing Update");
        this.client.transport = new EcuApplicationTransport(this.commService);
        PrepareController();
        SystemClock.sleep(25000L);
        if (this.DisplayController != null) {
            UpdateStatus("Updating Display");
            ProgramDisplay();
        }
        if (this.EcuController != null) {
            UpdateStatus("Updating ECU");
            ProgramEcu();
        }
        this.TotalProgramSize = 25000;
        this.CurrentProgramProgress = 0;
        this.Progress = 0;
        UpdateStatus("Confirming successful update");
        for (int i = 0; i < 20000; i += 100) {
            SystemClock.sleep(100L);
            BlockCompleted(100);
        }
        UpdateStatus("Update Complete");
    }

    private void ProgramDisplay() throws XcpFlashException {
        this.client.transport = new DisplayTransport(this.commService);
        if (!PrepareController() && !PrepareController()) {
            throw new XcpFlashException("Failed to prepare display");
        }
        for (int i = 0; i < this.DisplayController.Images.size(); i++) {
            ProgramImage(this.DisplayController.Images.get(i));
        }
        this.client.ProgramReset();
    }

    private void ProgramEcu() throws XcpFlashException {
        this.client.transport = new EcuBootloaderTransport(this.commService);
        if (!PrepareController()) {
            throw new XcpFlashException("Failed to prepare ecu.");
        }
        for (int i = 0; i < this.EcuController.Images.size(); i++) {
            ProgramImage(this.EcuController.Images.get(i));
        }
        this.client.ProgramReset();
    }

    private void ProgramImage(Image image) throws XcpFlashException {
        try {
            this.client.SetMta((int) image.Address);
            this.client.ProgramClear((int) image.Size);
            for (int i = 0; i < image.Segments.size(); i++) {
                ProgramSegment(image.Segments.get(i));
            }
            this.client.Program(new byte[0], 0, 0, (byte) 0);
            this.client.ReceiveResponse(5000);
            this.client.SetMta((int) image.Address);
            if (image.Checksum != this.client.BuildChecksum((int) image.Size)) {
                throw new XcpFlashException("Invalid checksum.");
            }
        } catch (XcpResponseException | XcpTimeoutException | XcpTransportException | InterruptedException e) {
            e.printStackTrace();
            throw new XcpFlashException("Failed to program image.", e);
        }
    }

    private boolean PrepareController() {
        try {
            this.client.Connect();
            this.client.Unlock((int) ((this.client.GetSeed() * 1103515245) + 2217337987L));
            this.client.SetMta(1073741824);
            int length = this.InterlockBytes.length;
            this.client.ProgramPrepare((short) length);
            int i = 0;
            while (i < length) {
                int min = Math.min(length - i, XcpCommands.MaxCtoSize - 2);
                this.client.Download(this.InterlockBytes, i, min);
                i += min;
            }
            this.client.ProgramStart();
            return true;
        } catch (XcpResponseException | XcpTimeoutException e) {
            new XcpFlashException("Could not prepare controller.", e).printStackTrace();
            return false;
        }
    }

    private void ProgramSegment(Segment segment) throws XcpFlashException {
        try {
            this.client.SetMta((int) segment.Address);
            int i = 0;
            while (i < segment.Size) {
                int min = Math.min(((int) segment.Size) - i, Math.min(XcpCommands.MaximumBlockSizeProgram * (XcpCommands.MaximumCtoSizeProgram - 2), 255));
                ProgramBlock(segment.Data, i, min);
                i += min;
                BlockCompleted(min);
            }
        } catch (XcpTimeoutException e) {
            e.printStackTrace();
            throw new XcpFlashException("Program segment could not set mta.", e);
        }
    }

    private void ProgramBlock(byte[] bArr, int i, int i2) throws XcpFlashException {
        int i3 = 0;
        while (i3 < i2) {
            try {
                int min = Math.min(i2 - i3, XcpCommands.MaximumCtoSizeProgram - 2);
                if (i3 == 0) {
                    this.client.Program(bArr, i + i3, min, (byte) i2);
                } else {
                    this.client.ProgramNext(bArr, (byte) (i + i3), (byte) min);
                }
                i3 += min;
            } catch (XcpResponseException | XcpTimeoutException | XcpTransportException | InterruptedException e) {
                e.printStackTrace();
                throw new XcpFlashException("Program block failed.", e);
            }
        }
        this.client.ReceiveResponse(5000);
    }

    private void UpdateStatus(String str) {
        this.Status = str;
        this.Listener.onTaskProgressUpdate(Integer.valueOf(this.Progress), this.Status);
    }

    private void BlockCompleted(int i) {
        int i2 = this.CurrentProgramProgress + i;
        this.CurrentProgramProgress = i2;
        UpdateProgress((i2 * 100) / this.TotalProgramSize);
    }

    private void UpdateProgress(int i) {
        this.Progress = i;
        this.Listener.onTaskProgressUpdate(Integer.valueOf(i), this.Status);
    }
}
