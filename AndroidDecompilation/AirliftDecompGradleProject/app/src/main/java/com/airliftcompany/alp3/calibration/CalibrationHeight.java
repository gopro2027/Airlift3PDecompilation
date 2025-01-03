package com.airliftcompany.alp3.calibration;

import android.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
public class CalibrationHeight extends ALP3Activity {
    private static final String TAG = "CalibrationHeight";
    private Button mCancelButton;
    private TextView mStatusTextView;
    private TextView mStepTextView;
    private TextView mTitleTextView;

    @Override // com.airliftcompany.alp3.custom.ALP3Activity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(C0380R.string.Height));
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(C0380R.layout.activity_calibration);
        this.mTitleTextView = (TextView) findViewById(C0380R.id.titleTextView);
        this.mStatusTextView = (TextView) findViewById(C0380R.id.statusTextView);
        this.mStepTextView = (TextView) findViewById(C0380R.id.stepTextView);
        Button button = (Button) findViewById(C0380R.id.cancelButton);
        this.mCancelButton = button;
        button.setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationHeight.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (CalibrationHeight.this.mCommService.alp3Device.canpng65300ECUStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_HEIGHT.ordinal()) {
                    CalibrationHeight.this.startCalibration();
                } else {
                    CalibrationHeight.this.closeActivity();
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
        this.mCommService.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_HEIGHT.ordinal();
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
        if (this.mCommService.alp3Device.canpng65300ECUStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_HEIGHT.ordinal()) {
            return;
        }
        switch (this.mCommService.alp3Device.canpng65300ECUStatus.SubMode) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                this.mTitleTextView.setText(getString(C0380R.string.movement_calibration));
                this.mStatusTextView.setText(getString(C0380R.string.wait_for_completion));
                break;
            case 8:
                this.mTitleTextView.setText(getString(C0380R.string.Complete));
                this.mStatusTextView.setText(getString(C0380R.string.calibration_successful));
                stopCalibration();
                this.mCommService.setCommServiceListener(null);
                this.mCommService.alp3Device.calibrationSettings.needsCalibration = false;
                new AlertDialog.Builder(this).setTitle(getString(C0380R.string.Complete)).setMessage(getString(C0380R.string.system_is_ready_to_use)).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationHeight.2
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        CalibrationHeight.this.finish();
                    }
                }).setIcon(R.drawable.ic_dialog_info).show();
                break;
            case 9:
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
}
