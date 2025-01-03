package com.airliftcompany.alp3.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.airliftcompany.alp3.C0380R;
import com.airliftcompany.alp3.DeviceScanActivity;
import com.airliftcompany.alp3.Registration;
import com.airliftcompany.alp3.calibration.CalibrationQuestions;
import com.airliftcompany.alp3.custom.ALP3ListActivity;
import com.airliftcompany.alp3.dealer.DealerLogin;
import com.airliftcompany.alp3.utils.ALP3Preferences;
import com.airliftcompany.alp3.utils.Util;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class SettingsHome extends ALP3ListActivity {
    private static final String TAG = "SettingsHome";
    private Handler handler;
    private GestureDetector mDetector;
    private Runnable mLongPressed;

    public enum SettingsRowEnum {
        DisplayRow,
        OperationRow,
        SetupRow,
        CalibrationRow,
        DiscoverRow,
        AboutRow,
        RegistrationRow
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0380R.layout.activity_settings_home);
        if (Util.isChinese()) {
            this.handler = new Handler(getMainLooper());
            this.mLongPressed = new Runnable() { // from class: com.airliftcompany.alp3.settings.SettingsHome.1
                @Override // java.lang.Runnable
                public void run() {
                    ALP3ListActivity.mIgnoreOnPause = true;
                    SettingsHome.this.startActivity(new Intent(SettingsHome.this, (Class<?>) DealerLogin.class));
                }
            };
            ((RelativeLayout) findViewById(C0380R.id.my_view)).setOnTouchListener(new View.OnTouchListener() { // from class: com.airliftcompany.alp3.settings.SettingsHome.2
                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int action = motionEvent.getAction() & 255;
                    if (action == 0 || action == 5) {
                        if (motionEvent.getPointerCount() == 2) {
                            Log.d("TAG", "onDown: ");
                            SettingsHome.this.handler.postDelayed(SettingsHome.this.mLongPressed, 3000L);
                        }
                    } else if (motionEvent.getAction() == 1) {
                        Log.d("TAG", "onUp: ");
                        SettingsHome.this.handler.removeCallbacks(SettingsHome.this.mLongPressed);
                    }
                    return true;
                }
            });
        }
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(C0380R.menu.done_menu, menu);
        menu.findItem(C0380R.id.done_button).setVisible(true);
        return true;
    }

    @Override // android.app.ListActivity
    protected void onListItemClick(ListView listView, View view, int i, long j) {
        super.onListItemClick(listView, view, i, j);
        switch (C04986.f66x86e7699a[SettingsRowEnum.values()[i].ordinal()]) {
            case 1:
                startActivity(new Intent(this, (Class<?>) SettingsDisplay.class));
                break;
            case 2:
                startActivity(new Intent(this, (Class<?>) SettingsOperation.class));
                break;
            case 3:
                startActivity(new Intent(this, (Class<?>) SettingsSetup.class));
                break;
            case 4:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                if (this.mCommService.alp3Device.calibrationSettings.needsCalibration) {
                    builder.setTitle(getString(C0380R.string.system_not_calibrated));
                    builder.setMessage(getString(C0380R.string.use_this_wizard_to_calibrate_your_system_do_you_wish_to_continue));
                } else {
                    builder.setTitle(getString(C0380R.string.system_is_calibrated));
                    builder.setMessage(getString(C0380R.string.do_you_wish_to_re_calibrate_your_system));
                }
                builder.setPositiveButton(C0380R.string.OK, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsHome.4
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        dialogInterface.dismiss();
                        SettingsHome.this.startActivity(new Intent(SettingsHome.this, (Class<?>) CalibrationQuestions.class));
                    }
                }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsHome.3
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
                break;
            case 5:
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.getWindow().setFlags(8, 8);
                progressDialog.setMessage(getString(C0380R.string.please_wait));
                progressDialog.setCancelable(false);
                progressDialog.show();
                ALP3Preferences.setDeviceAddress("", getApplicationContext());
                ALP3Preferences.setAuthorized(false, getApplicationContext());
                this.mCommService.bleService.mBluetoothDeviceAddress = null;
                this.mCommService.disconnectBleSession();
                new Handler().postDelayed(new Runnable() { // from class: com.airliftcompany.alp3.settings.SettingsHome.5
                    @Override // java.lang.Runnable
                    public void run() {
                        SettingsHome.this.startActivity(new Intent(SettingsHome.this, (Class<?>) DeviceScanActivity.class));
                        ProgressDialog progressDialog2 = progressDialog;
                        if (progressDialog2 == null || !progressDialog2.isShowing()) {
                            return;
                        }
                        progressDialog.dismiss();
                    }
                }, 3000L);
                break;
            case 6:
                startActivity(new Intent(this, (Class<?>) SettingsAbout.class));
                break;
            case 7:
                startActivity(new Intent(this, (Class<?>) Registration.class));
                break;
        }
        mIgnoreOnPause = true;
    }

    /* renamed from: com.airliftcompany.alp3.settings.SettingsHome$6 */
    static /* synthetic */ class C04986 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$settings$SettingsHome$SettingsRowEnum */
        static final /* synthetic */ int[] f66x86e7699a;

        static {
            int[] iArr = new int[SettingsRowEnum.values().length];
            f66x86e7699a = iArr;
            try {
                iArr[SettingsRowEnum.DisplayRow.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f66x86e7699a[SettingsRowEnum.OperationRow.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f66x86e7699a[SettingsRowEnum.SetupRow.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f66x86e7699a[SettingsRowEnum.CalibrationRow.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                f66x86e7699a[SettingsRowEnum.DiscoverRow.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                f66x86e7699a[SettingsRowEnum.AboutRow.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                f66x86e7699a[SettingsRowEnum.RegistrationRow.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
        }
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == C0380R.id.done_button) {
            mIgnoreOnPause = true;
            finish();
        }
        return true;
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity
    public void commServiceConnected() {
        super.commServiceConnected();
        this.mCommService.setCommServiceListener(this);
        ArrayList arrayList = new ArrayList();
        arrayList.add(getString(C0380R.string.Display));
        arrayList.add(getString(C0380R.string.Operation));
        arrayList.add(getString(C0380R.string.Setup));
        arrayList.add(getString(C0380R.string.Calibration));
        arrayList.add(getString(C0380R.string.discover_manifolds));
        arrayList.add(getString(C0380R.string.About));
        arrayList.add(getString(C0380R.string.product_registration));
        setListAdapter(new ArrayAdapter(this, C0380R.layout.listitem_settings, C0380R.id.title_text_view, arrayList));
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity
    public void updateUI() {
        super.updateUI();
    }
}
