package com.airliftcompany.alp3.firmware;

import android.content.Context;
import android.util.Log;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/* loaded from: classes.dex */
public class FirmwareDownloadUtility {
    private static final String TAG = "FirmwareDownloadUtility";
    Context context;
    File file;
    private DownloadFirmwareListener listener;
    private List<TransferObserver> observers;
    private TransferUtility transferUtility;

    public interface DownloadFirmwareListener {
        void onTaskCompleted(Boolean bool, byte[] bArr, Exception exc);

        void onTaskProgressUpdate(Integer num);
    }

    protected void finalize() throws Throwable {
        Log.i(TAG, "Downloaded JSON: ");
        super.finalize();
    }

    public void downloadFirmwareFile(Context context, String str, DownloadFirmwareListener downloadFirmwareListener) {
        TransferUtility transferUtility = Util.getTransferUtility(context);
        this.transferUtility = transferUtility;
        this.observers = transferUtility.getTransfersWithType(TransferType.DOWNLOAD);
        this.listener = downloadFirmwareListener;
        this.context = context;
        try {
            this.file = File.createTempFile(str, "bin", context.getCacheDir());
            TransferObserver download = this.transferUtility.download(Constants.S3BucketName(), str, this.file);
            this.observers.add(download);
            download.setTransferListener(new DownloadListener());
        } catch (IOException e) {
            downloadFirmwareListener.onTaskCompleted(false, null, e);
        }
    }

    private class DownloadListener implements TransferListener {
        private DownloadListener() {
        }

        @Override // com.amazonaws.mobileconnectors.p004s3.transferutility.TransferListener
        public void onError(int i, Exception exc) {
            FirmwareDownloadUtility.this.listener.onTaskCompleted(false, null, exc);
            FirmwareDownloadUtility.this.transferUtility.cancel(i);
        }

        @Override // com.amazonaws.mobileconnectors.p004s3.transferutility.TransferListener
        public void onProgressChanged(int i, long j, long j2) {
            FirmwareDownloadUtility.this.listener.onTaskProgressUpdate(Integer.valueOf((int) ((j / j2) * 100.0f)));
        }

        @Override // com.amazonaws.mobileconnectors.p004s3.transferutility.TransferListener
        public void onStateChanged(int i, TransferState transferState) {
            if (transferState == TransferState.COMPLETED) {
                byte[] bArr = new byte[(int) FirmwareDownloadUtility.this.file.length()];
                try {
                    if (new FileInputStream(FirmwareDownloadUtility.this.file).read(bArr) == FirmwareDownloadUtility.this.file.length()) {
                        FirmwareDownloadUtility.this.listener.onTaskCompleted(true, bArr, null);
                        return;
                    }
                    throw new IOException("Error reading firmware file");
                } catch (FileNotFoundException e) {
                    FirmwareDownloadUtility.this.listener.onTaskCompleted(false, null, e);
                } catch (IOException e2) {
                    FirmwareDownloadUtility.this.listener.onTaskCompleted(false, null, e2);
                }
            }
        }
    }
}
