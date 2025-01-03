package com.airliftcompany.alp3.firmware;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import com.airliftcompany.alp3.comm.CommService;
import com.airliftcompany.alp3.firmware.FirmwareFiles.FirmwarePackage;
import com.airliftcompany.alp3.firmware.FirmwareFiles.PackageException;
import com.airliftcompany.alp3.firmware.FirmwareFiles.PackageParser;
import com.airliftcompany.alp3.firmware.XcpFlashing.EcuBootloaderTransport;
import com.airliftcompany.alp3.firmware.XcpFlashing.Exceptions.XcpFlashException;
import com.airliftcompany.alp3.firmware.XcpFlashing.XcpFlashSequence;
import com.airliftcompany.alp3.firmware.XcpFlashing.XcpProtocolClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParserException;

/* loaded from: classes.dex */
public class ALP3Programmer {
    private static final String TAG = "ALP3Programmer";

    public interface ProgramFirmwareAsyncTaskListener {
        void onTaskCompleted(Boolean bool);

        void onTaskProgressUpdate(Integer num, String str);
    }

    public static boolean CheckBootloader(CommService commService) {
        commService.applicationCommOff = true;
        SystemClock.sleep(100L);
        EcuBootloaderTransport ecuBootloaderTransport = new EcuBootloaderTransport(commService);
        XcpProtocolClient xcpProtocolClient = new XcpProtocolClient();
        xcpProtocolClient.transport = ecuBootloaderTransport;
        try {
            try {
                xcpProtocolClient.Connect();
                commService.applicationCommOff = false;
                Log.i(TAG, "ECU Bootloader detected");
                return true;
            } catch (Exception unused) {
                Log.i(TAG, "ECU Bootloader not detected");
                commService.applicationCommOff = false;
                return false;
            }
        } catch (Throwable th) {
            commService.applicationCommOff = false;
            throw th;
        }
    }

    public static class ProgramFirmwareAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        private Context context;
        private byte[] data;
        private FirmwarePackage flashPackage;
        private ProgramFirmwareAsyncTaskListener listener;
        private CommService mCommService;
        private String status;

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
        }

        public ProgramFirmwareAsyncTask(byte[] bArr, Context context, CommService commService, ProgramFirmwareAsyncTaskListener programFirmwareAsyncTaskListener) {
            this.data = bArr;
            this.mCommService = commService;
            this.listener = programFirmwareAsyncTaskListener;
            this.context = context;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // android.os.AsyncTask
        public Boolean doInBackground(Void... voidArr) {
            boolean z = true;
            boolean z2 = false;
            z2 = false;
            try {
                try {
                    InputStream open = this.context.getAssets().open("Interlock.bin");
                    int available = open.available();
                    byte[] bArr = new byte[available];
                    open.read(bArr, 0, available);
                    this.flashPackage = PackageParser.Parse(new ByteArrayInputStream(this.data));
                    Log.i("PARSER", "Success");
                    this.flashPackage.Validate();
                    XcpFlashSequence xcpFlashSequence = new XcpFlashSequence(this.listener, this.mCommService, bArr, this.flashPackage);
                    Log.i(ALP3Programmer.TAG, "Starting firmware update");
                    this.mCommService.com100msState = CommService.com100msEnum.COM100mS_PAUSE;
                    this.mCommService.applicationCommOff = true;
                    xcpFlashSequence.Flash();
                    this.mCommService.alp3Device.versionMismatch = false;
                    this.mCommService.applicationCommOff = false;
                    ProgramFirmwareAsyncTaskListener programFirmwareAsyncTaskListener = this.listener;
                    programFirmwareAsyncTaskListener.onTaskCompleted(true);
                    z2 = programFirmwareAsyncTaskListener;
                } catch (PackageException | XcpFlashException | IOException | XmlPullParserException e) {
                    Log.i(ALP3Programmer.TAG, "Firmware update failed");
                    e.printStackTrace();
                    this.listener.onTaskProgressUpdate(100, e.getMessage());
                    this.mCommService.alp3Device.versionMismatch = false;
                    this.mCommService.applicationCommOff = false;
                    this.listener.onTaskCompleted(false);
                    z = false;
                }
                return Boolean.valueOf(z);
            } catch (Throwable th) {
                this.mCommService.alp3Device.versionMismatch = z2;
                this.mCommService.applicationCommOff = z2;
                this.listener.onTaskCompleted(Boolean.valueOf(z2));
                throw th;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Integer... numArr) {
            this.listener.onTaskProgressUpdate(numArr[0], this.status);
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
}
