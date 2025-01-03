package com.airliftcompany.alp3.calibration;

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
public class CalibrationAutoSensorLimits extends ALP3Activity {
    private static final String TAG = "CalibrationAutoSensorLimits";
    private Button mCancelButton;
    private TextView mStatusTextView;
    private TextView mStepTextView;
    private TextView mTitleTextView;

    @Override // com.airliftcompany.alp3.custom.ALP3Activity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(C0380R.string.auto_sensor_limits));
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(C0380R.layout.activity_calibration);
        this.mTitleTextView = (TextView) findViewById(C0380R.id.titleTextView);
        this.mStatusTextView = (TextView) findViewById(C0380R.id.statusTextView);
        this.mStepTextView = (TextView) findViewById(C0380R.id.stepTextView);
        Button button = (Button) findViewById(C0380R.id.cancelButton);
        this.mCancelButton = button;
        button.setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationAutoSensorLimits.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (CalibrationAutoSensorLimits.this.mCommService.alp3Device.canpng65300ECUStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_LIMITS.ordinal()) {
                    CalibrationAutoSensorLimits.this.startCalibration();
                } else {
                    CalibrationAutoSensorLimits.this.closeActivity();
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
        stopCalibration();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startCalibration() {
        this.mCommService.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_LIMITS.ordinal();
        this.mCommService.canpng65400UIStatus.SubControlMode = (short) 1;
        this.mCancelButton.setText(getText(C0380R.string.Stop));
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
        if (this.mCommService.alp3Device.canpng65300ECUStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_LIMITS.ordinal()) {
            return;
        }
        switch (C03852.f50xd68330b7[this.mCommService.alp3Device.canpng65300ECUStatus.calLimitsStateEnum().ordinal()]) {
            case 1:
            case 2:
                this.mTitleTextView.setText(getString(C0380R.string.finding_lower_sensor_limit));
                this.mStatusTextView.setText(getString(C0380R.string.wait_for_completion));
                break;
            case 3:
            case 4:
                this.mTitleTextView.setText(getString(C0380R.string.finding_upper_sensor_limit));
                this.mStatusTextView.setText(getString(C0380R.string.wait_for_completion));
                break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                this.mTitleTextView.setText(getString(C0380R.string.checking_wiring));
                this.mStatusTextView.setText(getString(C0380R.string.wait_for_completion));
                break;
            case 11:
                stopCalibration();
                this.mCommService.setCommServiceListener(null);
                mIgnoreOnPause = true;
                Intent intent = new Intent(this, (Class<?>) CalibrationHeight.class);
                finish();
                startActivity(intent);
                break;
            case 12:
                stopCalibration();
                this.mTitleTextView.setText(getString(C0380R.string.sensor_not_present));
                this.mStatusTextView.setText(getString(C0380R.string.calibration_fault));
                Toast.makeText(this, getString(C0380R.string.calibration_fault) + " - " + getString(C0380R.string.sensor_not_present), 1).show();
                break;
            case 13:
                stopCalibration();
                this.mTitleTextView.setText(getString(C0380R.string.sensor_hit_limit));
                this.mStatusTextView.setText(getString(C0380R.string.calibration_fault));
                Toast.makeText(this, getString(C0380R.string.calibration_fault) + " - " + getString(C0380R.string.sensor_hit_limit), 1).show();
                break;
            case 14:
                stopCalibration();
                this.mTitleTextView.setText(getString(C0380R.string.wire_error));
                this.mStatusTextView.setText(getString(C0380R.string.calibration_fault));
                Toast.makeText(this, getString(C0380R.string.calibration_fault) + " - " + getString(C0380R.string.wire_error), 1).show();
                break;
            case 15:
                stopCalibration();
                this.mTitleTextView.setText(getString(C0380R.string.range_error));
                this.mStatusTextView.setText(getString(C0380R.string.calibration_fault));
                Toast.makeText(this, getString(C0380R.string.calibration_fault) + " - " + getString(C0380R.string.range_error), 1).show();
                break;
            case 16:
                stopCalibration();
                this.mTitleTextView.setText(getString(C0380R.string.time_out_error));
                this.mStatusTextView.setText(getString(C0380R.string.calibration_fault));
                Toast.makeText(this, getString(C0380R.string.calibration_fault) + " - " + getString(C0380R.string.time_out_error), 1).show();
                break;
            default:
                stopCalibration();
                this.mTitleTextView.setText(getString(C0380R.string.Error));
                this.mStatusTextView.setText(getString(C0380R.string.op_mode_not_handled));
                Toast.makeText(this, getString(C0380R.string.calibration_fault) + " - " + getString(C0380R.string.op_mode_not_handled), 1).show();
                break;
        }
        if ((this.mCommService.alp3Device.canpng65304ECUIO.Output[1] > 0 || this.mCommService.alp3Device.canpng65304ECUIO.Output[0] > 0) && this.mCommService.alp3Device.canpng65300ECUStatus.SubMode < ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_SUCCESS.ordinal()) {
            this.mStatusTextView.setText(getString(C0380R.string.low_pressure_waiting_for_compressor));
        }
    }

    /* renamed from: com.airliftcompany.alp3.calibration.CalibrationAutoSensorLimits$2 */
    static /* synthetic */ class C03852 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$comm$ALP3Protocol$ECU_CalLimitsStateEnum */
        static final /* synthetic */ int[] f50xd68330b7;

        static {
            int[] iArr = new int[ALP3Protocol.ECU_CalLimitsStateEnum.values().length];
            f50xd68330b7 = iArr;
            try {
                iArr[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_START.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_LOWER.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_MOVE_UP.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_CALCULATE.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_CHECK_CORNER_PRESSURIZE.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_CHECK_LFCORNER.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_CHECK_RFCORNER.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_CHECK_LRCORNER.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_CHECK_RRCORNER.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_SAVE.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_FINISHED.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_NOT_PRESENT_ERROR.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_LIMIT_ERROR.ordinal()] = 13;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_WIRE_ERROR.ordinal()] = 14;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_RANGE_ERROR.ordinal()] = 15;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                f50xd68330b7[ALP3Protocol.ECU_CalLimitsStateEnum.OP_MODE_CAL_LIMIT_TIMEOUT_ERROR.ordinal()] = 16;
            } catch (NoSuchFieldError unused16) {
            }
        }
    }
}
