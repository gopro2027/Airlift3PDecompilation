package com.airliftcompany.alp3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.airliftcompany.alp3.comm.CommService;
import com.airliftcompany.alp3.comm.CommServiceListener;
import com.airliftcompany.alp3.utils.ALP3Preferences;
import com.airliftcompany.alp3.utils.CognitoService;
import com.airliftcompany.alp3.utils.Util;

/* loaded from: classes.dex */
public class DevicePairActivity extends Activity implements CommServiceListener {
    private static final String TAG = "DevicePairActivity";
    private String deviceAddress;
    private CommService mCommService;
    private ProgressDialog mDialog;
    private Handler mHandler;
    private final ServiceConnection mServiceConnection = new ServiceConnection() { // from class: com.airliftcompany.alp3.DevicePairActivity.3
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            DevicePairActivity.this.mCommService = ((CommService.LocalBinder) iBinder).getService();
            DevicePairActivity.this.mCommService.setCommServiceListener(DevicePairActivity.this);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            DevicePairActivity.this.mCommService = null;
        }
    };

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onBleInitializationFailed() {
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onBleInitialized() {
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(C0380R.string.device_pairing));
        }
        setContentView(C0380R.layout.activity_pair_device);
        this.deviceAddress = getIntent().getExtras().getString("deviceAddress");
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.mDialog = progressDialog;
        progressDialog.getWindow().setFlags(8, 8);
        ((Button) findViewById(C0380R.id.cancelButton)).setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.DevicePairActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DevicePairActivity.this.mCommService.setCommServiceListener(null);
                if (DevicePairActivity.this.mDialog != null && DevicePairActivity.this.mDialog.isShowing()) {
                    DevicePairActivity.this.mDialog.dismiss();
                }
                DevicePairActivity.this.finish();
            }
        });
        final Button button = (Button) findViewById(C0380R.id.pairButton);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.DevicePairActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                button.setEnabled(false);
                DevicePairActivity.this.mDialog.setMessage(DevicePairActivity.this.getString(C0380R.string.Pairing));
                DevicePairActivity.this.mDialog.setCancelable(true);
                DevicePairActivity.this.mDialog.show();
                DevicePairActivity.this.mCommService.bleService.connectionRetryAttempts = 0;
                DevicePairActivity.this.mCommService.bleService.amConnecting = false;
                DevicePairActivity.this.mCommService.bleService.connect(DevicePairActivity.this.deviceAddress);
            }
        });
        bindService(new Intent(this, (Class<?>) CommService.class), this.mServiceConnection, 1);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        ProgressDialog progressDialog = this.mDialog;
        if (progressDialog != null && progressDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        unbindService(this.mServiceConnection);
        this.mCommService = null;
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onAuthorizationFailed() {
        Log.i(TAG, "onAuthorizationFailed");
        runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.DevicePairActivity.4
            @Override // java.lang.Runnable
            public void run() {
                if (DevicePairActivity.this.mDialog != null && DevicePairActivity.this.mDialog.isShowing()) {
                    DevicePairActivity.this.mDialog.dismiss();
                }
                ((Button) DevicePairActivity.this.findViewById(C0380R.id.pairButton)).setEnabled(true);
                Toast.makeText(DevicePairActivity.this, "Error - manifold not in pairing mode", 1).show();
            }
        });
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onConnectionFailed() {
        Log.i(TAG, "onConnectionFailed");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onAuthorized() {
        Log.i(TAG, "onAuthorized");
        runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.DevicePairActivity.5
            @Override // java.lang.Runnable
            public void run() {
                if (Util.isChinese()) {
                    return;
                }
                DevicePairActivity.this.mCommService.setCommServiceListener(null);
                ALP3Preferences.setDeviceAddress(DevicePairActivity.this.deviceAddress, DevicePairActivity.this.getApplicationContext());
                Toast.makeText(DevicePairActivity.this, "Connected", 0).show();
                if (DevicePairActivity.this.mDialog != null && DevicePairActivity.this.mDialog.isShowing()) {
                    DevicePairActivity.this.mDialog.dismiss();
                }
                DevicePairActivity.this.finish();
            }
        });
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onStatusUpdated() {
        Log.i(TAG, "onStatusUpdated");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onCalibrationUpdated() {
        Log.i(TAG, "onCalibrationUpdated");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onSettingsUpdated() {
        Log.i(TAG, "onSettingsUpdated");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onVersionUpdated() {
        Log.i(TAG, "onVersionUpdated");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onSerialUpdated() {
        CognitoService.getInstance().checkDeviceAuthorization(this.mCommService.alp3Device.Serial, this.mCommService.alp3Device.MacAddress, this, new CognitoService.CallbackInterface() { // from class: com.airliftcompany.alp3.DevicePairActivity.6
            @Override // com.airliftcompany.alp3.utils.CognitoService.CallbackInterface
            public void completeCallback(final CognitoService.AuthResponse authResponse) {
                DevicePairActivity.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.DevicePairActivity.6.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (DevicePairActivity.this.mDialog != null && DevicePairActivity.this.mDialog.isShowing()) {
                            DevicePairActivity.this.mDialog.dismiss();
                        }
                        int i = C03297.f25x9eda8419[authResponse.ordinal()];
                        if (i == 1) {
                            ALP3Preferences.setAuthorized(true, DevicePairActivity.this.getApplicationContext());
                            DevicePairActivity.this.mCommService.setCommServiceListener(null);
                            ALP3Preferences.setDeviceAddress(DevicePairActivity.this.deviceAddress, DevicePairActivity.this.getApplicationContext());
                            Toast.makeText(DevicePairActivity.this, "Connected", 0).show();
                            DevicePairActivity.this.finish();
                            return;
                        }
                        if (i == 2) {
                            ALP3Preferences.setAuthorized(false, DevicePairActivity.this.getApplicationContext());
                            Util.displayAlert(DevicePairActivity.this.getString(C0380R.string.device_has_not_been_authorized), DevicePairActivity.this);
                            DevicePairActivity.this.mCommService.bleService.mBluetoothDeviceAddress = null;
                            DevicePairActivity.this.mCommService.disconnectBleSession();
                            return;
                        }
                        ALP3Preferences.setAuthorized(false, DevicePairActivity.this.getApplicationContext());
                        Util.displayAlert(DevicePairActivity.this.getString(C0380R.string.error_checking_device_authorization), DevicePairActivity.this);
                        DevicePairActivity.this.mCommService.bleService.mBluetoothDeviceAddress = null;
                        DevicePairActivity.this.mCommService.disconnectBleSession();
                    }
                });
            }
        });
    }

    /* renamed from: com.airliftcompany.alp3.DevicePairActivity$7 */
    static /* synthetic */ class C03297 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$utils$CognitoService$AuthResponse */
        static final /* synthetic */ int[] f25x9eda8419;

        static {
            int[] iArr = new int[CognitoService.AuthResponse.values().length];
            f25x9eda8419 = iArr;
            try {
                iArr[CognitoService.AuthResponse.AuthSuccess.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f25x9eda8419[CognitoService.AuthResponse.AccessDenied.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f25x9eda8419[CognitoService.AuthResponse.UnknownError.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }
}
