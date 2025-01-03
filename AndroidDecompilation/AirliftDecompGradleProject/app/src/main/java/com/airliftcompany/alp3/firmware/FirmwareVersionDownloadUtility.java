package com.airliftcompany.alp3.firmware;

import android.content.Context;
import android.content.pm.PackageManager;
import com.airliftcompany.alp3.comm.CommService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class FirmwareVersionDownloadUtility {
    private static final String TAG = "FirmwareVersionDownloadUtility";
    CommService commService;
    Context context;
    File file;
    private DownloadFirmwareListener listener;
    private List<TransferObserver> observers;
    private TransferUtility transferUtility;

    public interface DownloadFirmwareListener {
        void onTaskCompleted(Boolean bool, Boolean bool2, JSONObject jSONObject, Exception exc);

        void onTaskProgressUpdate(Integer num);
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void checkForFirmwareUpdate(Context context, CommService commService, DownloadFirmwareListener downloadFirmwareListener) {
        TransferUtility transferUtility = Util.getTransferUtility(context);
        this.transferUtility = transferUtility;
        this.observers = transferUtility.getTransfersWithType(TransferType.DOWNLOAD);
        this.listener = downloadFirmwareListener;
        this.context = context;
        this.commService = commService;
        try {
            this.file = File.createTempFile(Constants.versionKey, "json", context.getCacheDir());
            TransferObserver download = this.transferUtility.download(Constants.S3BucketName(), Constants.VERSION_FILENAME, this.file);
            this.observers.add(download);
            download.setTransferListener(new DownloadListener());
        } catch (IOException e) {
            downloadFirmwareListener.onTaskCompleted(false, false, null, e);
        }
    }

    private class DownloadListener implements TransferListener {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        private DownloadListener() {
        }

        @Override // com.amazonaws.mobileconnectors.p004s3.transferutility.TransferListener
        public void onError(int i, Exception exc) {
            FirmwareVersionDownloadUtility.this.listener.onTaskCompleted(false, false, null, exc);
            FirmwareVersionDownloadUtility.this.transferUtility.cancel(i);
        }

        @Override // com.amazonaws.mobileconnectors.p004s3.transferutility.TransferListener
        public void onProgressChanged(int i, long j, long j2) {
            FirmwareVersionDownloadUtility.this.listener.onTaskProgressUpdate(Integer.valueOf((int) ((j / j2) * 100.0f)));
        }

        @Override // com.amazonaws.mobileconnectors.p004s3.transferutility.TransferListener
        public void onStateChanged(int i, TransferState transferState) {
            BufferedReader bufferedReader;
            if (transferState != TransferState.COMPLETED) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            BufferedReader bufferedReader2 = null;
            try {
                try {
                    bufferedReader = new BufferedReader(new FileReader(FirmwareVersionDownloadUtility.this.file));
                    while (true) {
                        try {
                            String readLine = bufferedReader.readLine();
                            if (readLine == null) {
                                try {
                                    break;
                                } catch (Exception e) {
                                    FirmwareVersionDownloadUtility.this.listener.onTaskCompleted(false, false, null, e);
                                }
                            } else {
                                sb.append(readLine);
                                sb.append('\n');
                            }
                        } catch (IOException e2) {
                            e = e2;
                            FirmwareVersionDownloadUtility.this.listener.onTaskCompleted(false, false, null, e);
                            try {
                                bufferedReader.close();
                                return;
                            } catch (Exception e3) {
                                FirmwareVersionDownloadUtility.this.listener.onTaskCompleted(false, false, null, e3);
                                return;
                            }
                        }
                    }
                    bufferedReader.close();
                    try {
                        boolean z = false;
                        JSONObject jSONObject = new JSONObject(sb.toString());
                        if (Integer.valueOf(jSONObject.getInt(Constants.documentVersionKey)) != Constants.FIRMWARE_DOCUMENT_VERSION) {
                            throw new JSONException("Unable to check for firmware update. Try updating app version first.");
                        }
                        try {
                            if (Util.versionCompare(jSONObject.getJSONObject(Constants.androidVersionDictKey).getString(Constants.versionKey), FirmwareVersionDownloadUtility.this.context.getPackageManager().getPackageInfo(FirmwareVersionDownloadUtility.this.context.getPackageName(), 0).versionName).intValue() > 0) {
                                z = true;
                            }
                            String string = jSONObject.getString(Constants.manifoldVersionKey);
                            if (FirmwareVersionDownloadUtility.this.commService.alp3Device.ECUVersion.length() == 0 && FirmwareVersionDownloadUtility.this.commService.commStatus.BlueToothAccConnected) {
                                z = true;
                            } else if (FirmwareVersionDownloadUtility.this.commService.alp3Device.ECUVersion.length() > 0 && string.length() > 0 && Util.versionCompare(string, FirmwareVersionDownloadUtility.this.commService.alp3Device.ECUVersion).intValue() > 0) {
                                z = true;
                            }
                            String string2 = jSONObject.getString(Constants.displayVersionKey);
                            if (FirmwareVersionDownloadUtility.this.commService.alp3Device.DisplayVersion.length() > 0 && !FirmwareVersionDownloadUtility.this.commService.alp3Device.DisplayVersion.equalsIgnoreCase("0.0.0") && string2.length() > 0 && Util.versionCompare(string2, FirmwareVersionDownloadUtility.this.commService.alp3Device.DisplayVersion).intValue() > 0) {
                                z = true;
                            }
                            FirmwareVersionDownloadUtility.this.listener.onTaskCompleted(true, z, jSONObject, null);
                        } catch (PackageManager.NameNotFoundException e4) {
                            FirmwareVersionDownloadUtility.this.listener.onTaskCompleted(false, false, null, e4);
                        }
                    } catch (JSONException e5) {
                        FirmwareVersionDownloadUtility.this.listener.onTaskCompleted(false, false, null, e5);
                    }
                } catch (IOException e6) {
                    e = e6;
                    bufferedReader = null;
                } catch (Throwable th) {
                    th = th;
                    try {
                        bufferedReader2.close();
                    } catch (Exception e7) {
                        FirmwareVersionDownloadUtility.this.listener.onTaskCompleted(false, false, null, e7);
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                bufferedReader2.close();
                throw th;
            }
        }
    }
}
