package com.airliftcompany.alp3.firmware;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.airliftcompany.alp3.C0380R;
import com.airliftcompany.alp3.comm.CommService;
import com.airliftcompany.alp3.firmware.ALP3Programmer;
import com.airliftcompany.alp3.firmware.FirmwareDownloadUtility;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class FirmwareUpdateActivity extends Activity {
    private static final String TAG = "FirmwareUpdateActivity";
    private JSONObject firmwareJSON;
    private CommService mCommService;
    private Integer mSelectedItem = 0;
    private final ServiceConnection mServiceConnection = new ServiceConnection() { // from class: com.airliftcompany.alp3.firmware.FirmwareUpdateActivity.4
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            FirmwareUpdateActivity.this.mCommService = ((CommService.LocalBinder) iBinder).getService();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            FirmwareUpdateActivity.this.mCommService = null;
        }
    };
    private ProgressBar progressBar;
    private TextView statusTextView;

    @Override // android.app.Activity
    public void onBackPressed() {
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0380R.layout.activity_firmware_update);
        getWindow().addFlags(128);
        bindService(new Intent(this, (Class<?>) CommService.class), this.mServiceConnection, 1);
        ProgressBar progressBar = (ProgressBar) findViewById(C0380R.id.progressBar);
        this.progressBar = progressBar;
        progressBar.setProgress(0);
        this.statusTextView = (TextView) findViewById(C0380R.id.statusTextView);
        try {
            this.firmwareJSON = new JSONObject(getIntent().getStringExtra("firmwareJSON"));
        } catch (JSONException e) {
            e.printStackTrace();
            handleException(e);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(new CharSequence[]{"No Display", "Keypad - Normal", "Keypad - Right", "Keypad - Left"}, -1, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.firmware.FirmwareUpdateActivity.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                FirmwareUpdateActivity.this.mSelectedItem = Integer.valueOf(i);
            }
        }).setTitle(getString(C0380R.string.choose_display_configuration)).setPositiveButton(C0380R.string.OK, new DialogInterfaceOnClickListenerC04512()).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.firmware.FirmwareUpdateActivity.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                FirmwareUpdateActivity.this.finish();
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    /* renamed from: com.airliftcompany.alp3.firmware.FirmwareUpdateActivity$2 */
    class DialogInterfaceOnClickListenerC04512 implements DialogInterface.OnClickListener {
        DialogInterfaceOnClickListenerC04512() {
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            try {
                int intValue = FirmwareUpdateActivity.this.mSelectedItem.intValue();
                String string = intValue != 0 ? intValue != 1 ? intValue != 2 ? intValue != 3 ? "" : FirmwareUpdateActivity.this.firmwareJSON.getString(Constants.displayLeftFilenameKey) : FirmwareUpdateActivity.this.firmwareJSON.getString(Constants.displayRightFilenameKey) : FirmwareUpdateActivity.this.firmwareJSON.getString(Constants.displayNormalFilenameKey) : FirmwareUpdateActivity.this.firmwareJSON.getString(Constants.manifoldFilenameKey);
                FirmwareUpdateActivity.this.statusTextView.setText(FirmwareUpdateActivity.this.getString(C0380R.string.downloading_firmware_from_internet));
                new FirmwareDownloadUtility().downloadFirmwareFile(FirmwareUpdateActivity.this, string, new AnonymousClass1());
            } catch (JSONException e) {
                e.printStackTrace();
                FirmwareUpdateActivity.this.handleException(e);
            }
        }

        /* renamed from: com.airliftcompany.alp3.firmware.FirmwareUpdateActivity$2$1, reason: invalid class name */
        class AnonymousClass1 implements FirmwareDownloadUtility.DownloadFirmwareListener {
            AnonymousClass1() {
            }

            @Override // com.airliftcompany.alp3.firmware.FirmwareDownloadUtility.DownloadFirmwareListener
            public void onTaskCompleted(Boolean bool, byte[] bArr, Exception exc) {
                if (bool.booleanValue()) {
                    try {
                        FirmwareUpdateActivity.this.statusTextView.setText(FirmwareUpdateActivity.this.getString(C0380R.string.updating_firmware));
                        new ALP3Programmer.ProgramFirmwareAsyncTask(bArr, FirmwareUpdateActivity.this, FirmwareUpdateActivity.this.mCommService, new C16941()).execute(new Void[0]);
                        return;
                    } catch (Exception e) {
                        Log.d(FirmwareUpdateActivity.TAG, e.toString());
                        FirmwareUpdateActivity.this.handleException(e);
                        return;
                    }
                }
                if (exc != null) {
                    FirmwareUpdateActivity.this.handleException(exc);
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(FirmwareUpdateActivity.this);
                builder.setTitle(FirmwareUpdateActivity.this.getString(C0380R.string.Error));
                builder.setMessage(FirmwareUpdateActivity.this.getString(C0380R.string.error_downloading_firmware_from_internet));
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.firmware.FirmwareUpdateActivity.2.1.2
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirmwareUpdateActivity.this.finish();
                    }
                });
                builder.create().show();
            }

            /* renamed from: com.airliftcompany.alp3.firmware.FirmwareUpdateActivity$2$1$1, reason: invalid class name and collision with other inner class name */
            class C16941 implements ALP3Programmer.ProgramFirmwareAsyncTaskListener {
                C16941() {
                }

                @Override // com.airliftcompany.alp3.firmware.ALP3Programmer.ProgramFirmwareAsyncTaskListener
                public void onTaskCompleted(final Boolean bool) {
                    FirmwareUpdateActivity.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.firmware.FirmwareUpdateActivity.2.1.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FirmwareUpdateActivity.this);
                            if (bool.booleanValue()) {
                                builder.setTitle(FirmwareUpdateActivity.this.getString(C0380R.string.Success));
                            } else {
                                builder.setTitle(FirmwareUpdateActivity.this.getString(C0380R.string.Error));
                                builder.setMessage(FirmwareUpdateActivity.this.getString(C0380R.string.error_downloading_firmware_from_internet));
                            }
                            builder.setNeutralButton(FirmwareUpdateActivity.this.getString(C0380R.string.OK), new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.firmware.FirmwareUpdateActivity.2.1.1.1.1
                                @Override // android.content.DialogInterface.OnClickListener
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FirmwareUpdateActivity.this.finish();
                                }
                            });
                            builder.create().show();
                        }
                    });
                }

                @Override // com.airliftcompany.alp3.firmware.ALP3Programmer.ProgramFirmwareAsyncTaskListener
                public void onTaskProgressUpdate(final Integer num, final String str) {
                    FirmwareUpdateActivity.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.firmware.FirmwareUpdateActivity.2.1.1.2
                        @Override // java.lang.Runnable
                        public void run() {
                            FirmwareUpdateActivity.this.progressBar.setProgress(num.intValue());
                            FirmwareUpdateActivity.this.statusTextView.setText(str);
                        }
                    });
                }
            }

            @Override // com.airliftcompany.alp3.firmware.FirmwareDownloadUtility.DownloadFirmwareListener
            public void onTaskProgressUpdate(final Integer num) {
                FirmwareUpdateActivity.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.firmware.FirmwareUpdateActivity.2.1.3
                    @Override // java.lang.Runnable
                    public void run() {
                        FirmwareUpdateActivity.this.progressBar.setProgress(num.intValue());
                    }
                });
            }
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this.mServiceConnection);
        this.mCommService = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleException(Exception exc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(C0380R.string.Error));
        builder.setMessage(exc.getMessage());
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.firmware.FirmwareUpdateActivity.5
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                FirmwareUpdateActivity.this.finish();
            }
        });
        builder.create().show();
    }
}
