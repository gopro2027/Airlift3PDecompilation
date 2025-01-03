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
public class CalibrationAccelerometer extends ALP3Activity {
    private static final String TAG = "CalibrationAccelerometer";
    private Button mCancelButton;
    private TextView mStatusTextView;
    private TextView mStepTextView;
    private TextView mTitleTextView;

    @Override // com.airliftcompany.alp3.custom.ALP3Activity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(C0380R.string.Accelerometer));
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(C0380R.layout.activity_calibration);
        this.mTitleTextView = (TextView) findViewById(C0380R.id.titleTextView);
        this.mStatusTextView = (TextView) findViewById(C0380R.id.statusTextView);
        this.mStepTextView = (TextView) findViewById(C0380R.id.stepTextView);
        Button button = (Button) findViewById(C0380R.id.cancelButton);
        this.mCancelButton = button;
        button.setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationAccelerometer.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (CalibrationAccelerometer.this.mCommService.alp3Device.canpng65300ECUStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_ACC.ordinal()) {
                    CalibrationAccelerometer.this.startCalibration();
                } else {
                    CalibrationAccelerometer.this.closeActivity();
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
        this.mCommService.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_ACC.ordinal();
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
        if (this.mCommService.alp3Device.canpng65300ECUStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_ACC.ordinal()) {
            return;
        }
        short s = this.mCommService.alp3Device.canpng65300ECUStatus.SubMode;
        if (s == 0 || s == 1 || s == 2) {
            this.mTitleTextView.setText(getString(C0380R.string.checking_mount));
            this.mStatusTextView.setText(getString(C0380R.string.wait_for_completion));
        } else if (s == 3) {
            this.mTitleTextView.setText(getString(C0380R.string.finding_base_line));
            this.mStatusTextView.setText(getString(C0380R.string.wait_for_completion));
        } else if (s == 4) {
            stopCalibration();
            this.mCommService.setCommServiceListener(null);
            mIgnoreOnPause = true;
            if (this.mCommService.alp3Device.calibrationSettings.wizardPressureIsAuto) {
                Intent intent = new Intent(this, (Class<?>) CalibrationPressure.class);
                finish();
                startActivity(intent);
            } else {
                Intent intent2 = new Intent(this, (Class<?>) CalibrationManualPressure.class);
                finish();
                startActivity(intent2);
            }
        } else if (s == 5) {
            stopCalibration();
            this.mTitleTextView.setText(getString(C0380R.string.Fault));
            this.mStatusTextView.setText(getString(C0380R.string.calibration_fault));
            Toast.makeText(this, getString(C0380R.string.calibration_fault) + " - " + getString(C0380R.string.sensor_not_present), 1).show();
        }
        if ((this.mCommService.alp3Device.canpng65304ECUIO.Output[1] > 0 || this.mCommService.alp3Device.canpng65304ECUIO.Output[0] > 0) && this.mCommService.alp3Device.canpng65300ECUStatus.SubMode < ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_SUCCESS.ordinal()) {
            this.mStatusTextView.setText(getString(C0380R.string.low_pressure_waiting_for_compressor));
        }
    }
}
