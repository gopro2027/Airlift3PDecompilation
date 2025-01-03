package com.airliftcompany.alp3.calibration;

import android.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NavUtils;
import com.airliftcompany.alp3.C0380R;
import com.airliftcompany.alp3.comm.ALP3Protocol;
import com.airliftcompany.alp3.custom.ALP3Activity;

/* loaded from: classes.dex */
public class CalibrationPressure extends ALP3Activity {
    private static final String TAG = "CalibrationPressure";
    private Button mCancelButton;
    private TextView mStatusTextView;
    private TextView mStepTextView;
    private TextView mTitleTextView;

    @Override // com.airliftcompany.alp3.custom.ALP3Activity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(C0380R.string.Pressure));
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(C0380R.layout.activity_calibration);
        this.mTitleTextView = (TextView) findViewById(C0380R.id.titleTextView);
        this.mStatusTextView = (TextView) findViewById(C0380R.id.statusTextView);
        this.mStepTextView = (TextView) findViewById(C0380R.id.stepTextView);
        Button button = (Button) findViewById(C0380R.id.cancelButton);
        this.mCancelButton = button;
        button.setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationPressure.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (CalibrationPressure.this.mCommService.alp3Device.canpng65300ECUStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_PRESSURE.ordinal()) {
                    CalibrationPressure.this.startCalibration();
                } else {
                    CalibrationPressure.this.closeActivity();
                }
            }
        });
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            closeActivity();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        closeActivity();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startCalibration() {
        this.mCommService.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_PRESSURE.ordinal();
        this.mCommService.canpng65400UIStatus.SubControlMode = (short) 1;
        this.mCancelButton.setText(getString(C0380R.string.Stop));
    }

    private void stopCalibration() {
        this.mCommService.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_MANUAL_CONTROL.ordinal();
        this.mCommService.canpng65400UIStatus.SubControlMode = (short) 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void closeActivity() {
        this.mCommService.setCommServiceListener(null);
        stopCalibration();
        mIgnoreOnPause = true;
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override // com.airliftcompany.alp3.custom.ALP3Activity
    public void commServiceConnected() {
        super.commServiceConnected();
        this.mCommService.setCommServiceListener(this);
        this.mCommService.alp3Device.calibrationSettings.calibrationStep++;
        this.mStepTextView.setText(getString(C0380R.string.Step) + " " + this.mCommService.alp3Device.calibrationSettings.calibrationStep + " " + getString(C0380R.string.of) + " " + this.mCommService.alp3Device.calibrationSettings.calibrationCount);
        startCalibration();
    }

    @Override // com.airliftcompany.alp3.custom.ALP3Activity
    public void updateUI() {
        super.updateUI();
        if (this.mCommService.alp3Device.canpng65300ECUStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_PRESSURE.ordinal()) {
            return;
        }
        switch (C04163.f53x23dc09d[this.mCommService.alp3Device.canpng65300ECUStatus.calPressureControlStateEnum().ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                this.mTitleTextView.setText(getString(C0380R.string.calibrating_front));
                this.mStatusTextView.setText(getString(C0380R.string.wait_for_completion));
                break;
            case 7:
            case 8:
            case 9:
            case 10:
                this.mTitleTextView.setText(getString(C0380R.string.calibrating_rear));
                this.mStatusTextView.setText(getString(C0380R.string.wait_for_completion));
                break;
            case 11:
                this.mTitleTextView.setText(getString(C0380R.string.Complete));
                this.mStatusTextView.setText(getString(C0380R.string.calibration_successful));
                stopCalibration();
                this.mCommService.setCommServiceListener(null);
                if (this.mCommService.alp3Device.calibrationSettings.PressureOnly == 1) {
                    this.mCommService.alp3Device.calibrationSettings.needsCalibration = false;
                    new AlertDialog.Builder(this).setTitle(getString(C0380R.string.Complete)).setMessage(getString(C0380R.string.system_is_ready_to_use)).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationPressure.2
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            CalibrationPressure.this.finish();
                        }
                    }).setIcon(R.drawable.ic_dialog_info).show();
                    break;
                } else {
                    mIgnoreOnPause = true;
                    if (this.mCommService.alp3Device.calibrationSettings.wizardHeightIsAuto) {
                        Intent intent = new Intent(this, (Class<?>) CalibrationAutoSensorLimits.class);
                        finish();
                        startActivity(intent);
                        break;
                    } else {
                        Intent intent2 = new Intent(this, (Class<?>) CalibrationManualSensorLimits.class);
                        finish();
                        startActivity(intent2);
                        break;
                    }
                }
            case 12:
                stopCalibration();
                this.mTitleTextView.setText(getString(C0380R.string.Fault));
                this.mStatusTextView.setText(getString(C0380R.string.calibration_fault));
                Toast.makeText(this, getString(C0380R.string.calibration_fault) + " - " + getString(C0380R.string.sensor_not_present), 1).show();
                break;
        }
        if ((this.mCommService.alp3Device.canpng65304ECUIO.Output[1] > 0 || this.mCommService.alp3Device.canpng65304ECUIO.Output[0] > 0) && this.mCommService.alp3Device.canpng65300ECUStatus.SubMode < ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_SUCCESS.ordinal()) {
            this.mStatusTextView.setText(getString(C0380R.string.low_pressure_waiting_for_compressor));
        }
    }

    /* renamed from: com.airliftcompany.alp3.calibration.CalibrationPressure$3 */
    static /* synthetic */ class C04163 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$comm$ALP3Protocol$ECU_CalPressureControlStateEnum */
        static final /* synthetic */ int[] f53x23dc09d;

        static {
            int[] iArr = new int[ALP3Protocol.ECU_CalPressureControlStateEnum.values().length];
            f53x23dc09d = iArr;
            try {
                iArr[ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_LIMIT_WAIT_GO.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f53x23dc09d[ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_START.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f53x23dc09d[ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_ZERO_ALL_FRONT.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f53x23dc09d[ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_STEP_FILL_FRONT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                f53x23dc09d[ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_ZERO_DUMP_FRONT.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                f53x23dc09d[ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_STEP_DUMP_FRONT.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                f53x23dc09d[ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_ZERO_ALL_REAR.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                f53x23dc09d[ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_STEP_FILL_REAR.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                f53x23dc09d[ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_ZERO_DUMP_REAR.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                f53x23dc09d[ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_STEP_DUMP_REAR.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                f53x23dc09d[ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_SUCCESS.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                f53x23dc09d[ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_FAULT.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
        }
    }
}
