package com.airliftcompany.alp3.custom;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.airliftcompany.alp3.MainActivity;
import com.airliftcompany.alp3.comm.CommService;
import com.airliftcompany.alp3.comm.CommServiceListener;
import com.airliftcompany.alp3.utils.ALP3Preferences;

/* loaded from: classes.dex */
public class ALP3Activity extends Activity implements CommServiceListener {
    private static final String TAG = "ALP3Activity";
    public static boolean mIgnoreOnPause = false;
    public CommService mCommService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() { // from class: com.airliftcompany.alp3.custom.ALP3Activity.2
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ALP3Activity.this.mCommService = ((CommService.LocalBinder) iBinder).getService();
            ALP3Activity.this.commServiceConnected();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            ALP3Activity.this.mCommService = null;
        }
    };

    public void commServiceConnected() {
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onSerialUpdated() {
    }

    public void updateUI() {
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        bindService(new Intent(this, (Class<?>) CommService.class), this.mServiceConnection, 1);
        if (ALP3Preferences.preventSleep(getApplicationContext()).booleanValue()) {
            getWindow().addFlags(128);
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        if (this.mCommService != null) {
            unbindService(this.mServiceConnection);
            this.mCommService = null;
        }
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        if (this.mCommService.applicationCommOff) {
            return;
        }
        if (mIgnoreOnPause) {
            mIgnoreOnPause = false;
            return;
        }
        if (!this.mCommService.alp3Device.canpng65404ManualControl.valvesAreClosed()) {
            int[] iArr = this.mCommService.alp3Device.canpng65404ManualControl.Spring;
            int[] iArr2 = this.mCommService.alp3Device.canpng65404ManualControl.Spring;
            int[] iArr3 = this.mCommService.alp3Device.canpng65404ManualControl.Spring;
            this.mCommService.alp3Device.canpng65404ManualControl.Spring[3] = 0;
            iArr3[2] = 0;
            iArr2[1] = 0;
            iArr[0] = 0;
            this.mCommService.alp3Device.canpng65404ManualControl.Direction = 0;
            this.mCommService.forceSendManualMode();
            new Handler().postDelayed(new Runnable() { // from class: com.airliftcompany.alp3.custom.ALP3Activity.1
                @Override // java.lang.Runnable
                public void run() {
                    ALP3Activity.this.handleOnPause();
                }
            }, 500L);
            return;
        }
        handleOnPause();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleOnPause() {
        this.mCommService.setCommServiceListener(null);
        this.mCommService.disconnectBleSession();
        unbindService(this.mServiceConnection);
        stopService(new Intent(this, (Class<?>) CommService.class));
        this.mCommService = null;
        Intent intent = new Intent(this, (Class<?>) MainActivity.class);
        intent.addFlags(67108864);
        startActivity(intent);
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        mIgnoreOnPause = false;
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onBleInitialized() {
        Log.i(TAG, "onBleInitialized");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onBleInitializationFailed() {
        Log.i(TAG, "onBleInitializationFailed");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onAuthorizationFailed() {
        Log.i(TAG, "onAuthorizationFailed");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onConnectionFailed() {
        Log.i(TAG, "onConnectionFailed");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onAuthorized() {
        Log.i(TAG, "onAuthorized");
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onStatusUpdated() {
        runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.custom.ALP3Activity.3
            @Override // java.lang.Runnable
            public void run() {
                ALP3Activity.this.updateUI();
            }
        });
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
}
