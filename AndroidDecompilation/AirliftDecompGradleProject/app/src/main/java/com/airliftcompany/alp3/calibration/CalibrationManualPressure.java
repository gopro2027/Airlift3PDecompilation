package com.airliftcompany.alp3.calibration;

import android.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.app.NavUtils;
import com.airliftcompany.alp3.C0380R;
import com.airliftcompany.alp3.comm.ALP3Protocol;
import com.airliftcompany.alp3.custom.ALP3Activity;
import com.airliftcompany.alp3.custom.RepeatListener;
import com.airliftcompany.alp3.custom.ShapedButton;

/* loaded from: classes.dex */
public class CalibrationManualPressure extends ALP3Activity {
    private static final String TAG = CalibrationManualSensorLimits.class.getSimpleName();
    private Button mCancelButton;
    private TextView mStatusTextView;
    private TextView mStepTextView;
    private TextView mTitleTextView;
    private boolean sendNotification;

    @Override // com.airliftcompany.alp3.custom.ALP3Activity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(C0380R.string.manual_pressure_limits));
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(C0380R.layout.activity_cal_manual_sensors);
        this.mTitleTextView = (TextView) findViewById(C0380R.id.titleTextView);
        this.mStatusTextView = (TextView) findViewById(C0380R.id.statusTextView);
        this.mStepTextView = (TextView) findViewById(C0380R.id.stepTextView);
        Button button = (Button) findViewById(C0380R.id.cancelButton);
        this.mCancelButton = button;
        button.setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationManualPressure.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                CalibrationManualPressure.this.continueCalibration();
            }
        });
        ShapedButton shapedButton = (ShapedButton) findViewById(C0380R.id.leftFrontUpButton);
        shapedButton.normalDrawable = getResources().getDrawable(C0380R.drawable.lfu);
        shapedButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.lfu_pressed);
        shapedButton.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationManualPressure.2
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                CalibrationManualPressure.this.handleValveButtonEvent(motionEvent, (short) 0, (short) 1);
                return true;
            }
        }));
        ShapedButton shapedButton2 = (ShapedButton) findViewById(C0380R.id.leftFrontDownButton);
        shapedButton2.normalDrawable = getResources().getDrawable(C0380R.drawable.lfd);
        shapedButton2.selectedDrawble = getResources().getDrawable(C0380R.drawable.lfd_pressed);
        shapedButton2.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationManualPressure.3
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                CalibrationManualPressure.this.handleValveButtonEvent(motionEvent, (short) 0, (short) 2);
                return true;
            }
        }));
        ShapedButton shapedButton3 = (ShapedButton) findViewById(C0380R.id.rightFrontUpButton);
        shapedButton3.normalDrawable = getResources().getDrawable(C0380R.drawable.rfu);
        shapedButton3.selectedDrawble = getResources().getDrawable(C0380R.drawable.rfu_pressed);
        shapedButton3.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationManualPressure.4
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                CalibrationManualPressure.this.handleValveButtonEvent(motionEvent, (short) 1, (short) 1);
                return true;
            }
        }));
        ShapedButton shapedButton4 = (ShapedButton) findViewById(C0380R.id.rightFrontDownButton);
        shapedButton4.normalDrawable = getResources().getDrawable(C0380R.drawable.rfd);
        shapedButton4.selectedDrawble = getResources().getDrawable(C0380R.drawable.rfd_pressed);
        shapedButton4.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationManualPressure.5
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                CalibrationManualPressure.this.handleValveButtonEvent(motionEvent, (short) 1, (short) 2);
                return true;
            }
        }));
        ShapedButton shapedButton5 = (ShapedButton) findViewById(C0380R.id.leftRearUpButton);
        shapedButton5.normalDrawable = getResources().getDrawable(C0380R.drawable.lru);
        shapedButton5.selectedDrawble = getResources().getDrawable(C0380R.drawable.lru_pressed);
        shapedButton5.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationManualPressure.6
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                CalibrationManualPressure.this.handleValveButtonEvent(motionEvent, (short) 2, (short) 1);
                return true;
            }
        }));
        ShapedButton shapedButton6 = (ShapedButton) findViewById(C0380R.id.leftRearDownButton);
        shapedButton6.normalDrawable = getResources().getDrawable(C0380R.drawable.lrd);
        shapedButton6.selectedDrawble = getResources().getDrawable(C0380R.drawable.lrd_pressed);
        shapedButton6.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationManualPressure.7
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                CalibrationManualPressure.this.handleValveButtonEvent(motionEvent, (short) 2, (short) 2);
                return true;
            }
        }));
        ShapedButton shapedButton7 = (ShapedButton) findViewById(C0380R.id.rightRearUpButton);
        shapedButton7.normalDrawable = getResources().getDrawable(C0380R.drawable.rru);
        shapedButton7.selectedDrawble = getResources().getDrawable(C0380R.drawable.rru_pressed);
        shapedButton7.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationManualPressure.8
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                CalibrationManualPressure.this.handleValveButtonEvent(motionEvent, (short) 3, (short) 1);
                return true;
            }
        }));
        ShapedButton shapedButton8 = (ShapedButton) findViewById(C0380R.id.rightRearDownButton);
        shapedButton8.normalDrawable = getResources().getDrawable(C0380R.drawable.rrd);
        shapedButton8.selectedDrawble = getResources().getDrawable(C0380R.drawable.rrd_pressed);
        shapedButton8.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationManualPressure.9
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                CalibrationManualPressure.this.handleValveButtonEvent(motionEvent, (short) 3, (short) 2);
                return true;
            }
        }));
        ShapedButton shapedButton9 = (ShapedButton) findViewById(C0380R.id.allUpButton);
        shapedButton9.setEnabled(false);
        shapedButton9.normalDrawable = getResources().getDrawable(C0380R.drawable.all_up);
        shapedButton9.normalBlinkDrawable = getResources().getDrawable(C0380R.drawable.all_up_pressed);
        shapedButton9.selectedDrawble = getResources().getDrawable(C0380R.drawable.all_up_pressed);
        shapedButton9.altNormalDrawable = getResources().getDrawable(C0380R.drawable.all_up_selected);
        ShapedButton shapedButton10 = (ShapedButton) findViewById(C0380R.id.presetUpButton);
        shapedButton10.setEnabled(false);
        shapedButton10.normalDrawable = getResources().getDrawable(C0380R.drawable.preset_01);
        shapedButton10.normalBlinkDrawable = getResources().getDrawable(C0380R.drawable.preset_01_pressed);
        shapedButton10.selectedDrawble = getResources().getDrawable(C0380R.drawable.preset_01_pressed);
        shapedButton10.altNormalDrawable = getResources().getDrawable(C0380R.drawable.preset_01_selected);
        ShapedButton shapedButton11 = (ShapedButton) findViewById(C0380R.id.normalRideButton);
        shapedButton11.setEnabled(false);
        shapedButton11.normalDrawable = getResources().getDrawable(C0380R.drawable.airlift);
        shapedButton11.normalBlinkDrawable = getResources().getDrawable(C0380R.drawable.airlift_pressed);
        shapedButton11.selectedDrawble = getResources().getDrawable(C0380R.drawable.airlift_pressed);
        shapedButton11.altNormalDrawable = getResources().getDrawable(C0380R.drawable.airlift_selected);
        ShapedButton shapedButton12 = (ShapedButton) findViewById(C0380R.id.presetDownButton);
        shapedButton12.setEnabled(false);
        shapedButton12.normalDrawable = getResources().getDrawable(C0380R.drawable.preset_02);
        shapedButton12.normalBlinkDrawable = getResources().getDrawable(C0380R.drawable.preset_02_pressed);
        shapedButton12.selectedDrawble = getResources().getDrawable(C0380R.drawable.preset_02_pressed);
        shapedButton12.altNormalDrawable = getResources().getDrawable(C0380R.drawable.preset_02_selected);
        ShapedButton shapedButton13 = (ShapedButton) findViewById(C0380R.id.allDownButton);
        shapedButton13.setEnabled(false);
        shapedButton13.normalDrawable = getResources().getDrawable(C0380R.drawable.all_down);
        shapedButton13.normalBlinkDrawable = getResources().getDrawable(C0380R.drawable.all_down_pressed);
        shapedButton13.selectedDrawble = getResources().getDrawable(C0380R.drawable.all_down_pressed);
        shapedButton13.altNormalDrawable = getResources().getDrawable(C0380R.drawable.all_down_selected);
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
    public void handleValveButtonEvent(MotionEvent motionEvent, short s, short s2) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    this.mCommService.openValve(s, (short) (s2 + 2), false);
                    return;
                } else if (actionMasked != 5) {
                    if (actionMasked != 6) {
                        return;
                    }
                }
            }
            this.mCommService.closeValve(s);
            return;
        }
        this.mCommService.openValve(s, s2, true);
    }

    private void startCalibration() {
        this.mCommService.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_PRESSURE.ordinal();
        this.mCommService.canpng65400UIStatus.SubControlMode = (short) 1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void continueCalibration() {
        this.mCommService.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_PRESSURE.ordinal();
        if (this.mCommService.alp3Device.canpng65300ECUStatus.SubMode == ALP3Protocol.ECU_ManualCalModeStateEnum.SUB_MANUALCAL_GO_TOP.ordinal()) {
            this.mCommService.canpng65400UIStatus.SubControlMode = (short) ALP3Protocol.ECU_ManualCalModeStateEnum.SUB_MANUALCAL_SAVE_TOP.ordinal();
            Log.i(TAG, "upper mode set");
            this.sendNotification = true;
            new Handler().postDelayed(new Runnable() { // from class: com.airliftcompany.alp3.calibration.CalibrationManualPressure.10
                @Override // java.lang.Runnable
                public void run() {
                    if (CalibrationManualPressure.this.mCommService != null && CalibrationManualPressure.this.mCommService.canpng65400UIStatus.ControlMode == ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_PRESSURE.ordinal() && CalibrationManualPressure.this.mCommService.canpng65400UIStatus.SubControlMode == ALP3Protocol.ECU_ManualCalModeStateEnum.SUB_MANUALCAL_SAVE_TOP.ordinal()) {
                        CalibrationManualPressure.this.mCommService.canpng65400UIStatus.SubControlMode = (short) 255;
                        Log.i(CalibrationManualPressure.TAG, "upper mode cleared");
                    }
                }
            }, 750L);
            return;
        }
        if (this.mCommService.alp3Device.canpng65300ECUStatus.SubMode == ALP3Protocol.ECU_ManualCalModeStateEnum.SUB_MANUALCAL_GO_BOTTOM.ordinal()) {
            this.mCommService.canpng65400UIStatus.SubControlMode = (short) ALP3Protocol.ECU_ManualCalModeStateEnum.SUB_MANUALCAL_SAVE_BOTTOM.ordinal();
            Log.i(TAG, "lower mode set");
            new Handler().postDelayed(new Runnable() { // from class: com.airliftcompany.alp3.calibration.CalibrationManualPressure.11
                @Override // java.lang.Runnable
                public void run() {
                    if (CalibrationManualPressure.this.mCommService != null && CalibrationManualPressure.this.mCommService.canpng65400UIStatus.ControlMode == ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_PRESSURE.ordinal() && CalibrationManualPressure.this.mCommService.canpng65400UIStatus.SubControlMode == ALP3Protocol.ECU_ManualCalModeStateEnum.SUB_MANUALCAL_SAVE_BOTTOM.ordinal()) {
                        CalibrationManualPressure.this.mCommService.canpng65400UIStatus.SubControlMode = (short) 255;
                        Log.i(CalibrationManualPressure.TAG, "lower mode cleared");
                    }
                }
            }, 750L);
        }
    }

    private void stopCalibration() {
        this.mCommService.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_MANUAL_CONTROL.ordinal();
        this.mCommService.canpng65400UIStatus.SubControlMode = (short) 0;
    }

    private void closeActivity() {
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
        String str;
        super.updateUI();
        if (this.mCommService.alp3Device.canpng65300ECUStatus.ControlMode != ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_PRESSURE.ordinal()) {
            return;
        }
        if (this.mCommService.alp3Device.displaySettings.Units > 0) {
            float f = (float) ((this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[0] / 10) * 0.0689475729d);
            int i = (int) f;
            String str2 = "(" + String.format("%d.%02d,", Integer.valueOf(i), Integer.valueOf((int) ((f - i) * 100.0f)));
            float f2 = (float) ((this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[1] / 10) * 0.0689475729d);
            int i2 = (int) f2;
            String str3 = str2 + String.format("%d.%02d,", Integer.valueOf(i2), Integer.valueOf((int) ((f2 - i2) * 100.0f)));
            float f3 = (float) ((this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[2] / 10) * 0.0689475729d);
            int i3 = (int) f3;
            float f4 = (float) ((this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[3] / 10) * 0.0689475729d);
            int i4 = (int) f4;
            str = (str3 + String.format("%d.%02d,", Integer.valueOf(i3), Integer.valueOf((int) ((f3 - i3) * 100.0f)))) + String.format("%d.%02d)", Integer.valueOf(i4), Integer.valueOf((int) ((f4 - i4) * 100.0f)));
        } else {
            str = ((("(" + String.format("%d,", Long.valueOf(this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[0] / 10))) + String.format("%d,", Long.valueOf(this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[1] / 10))) + String.format("%d,", Long.valueOf(this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[2] / 10))) + String.format("%d)", Long.valueOf(this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[3] / 10));
        }
        int i5 = C039213.f51x67b34e72[this.mCommService.alp3Device.canpng65300ECUStatus.manualCalLimitsStateEnum().ordinal()];
        if (i5 == 1) {
            this.mCancelButton.setVisibility(0);
            this.mCancelButton.setText(getString(C0380R.string.Continue));
            String str4 = getString(C0380R.string.set_max_pressure) + " ";
            this.mTitleTextView.setText(str4 + str);
            this.mStatusTextView.setText(getString(C0380R.string.use_up_down_manual_keys_to_set_maximum_pressure_sensor_limit));
        } else if (i5 == 2) {
            this.mCancelButton.setVisibility(4);
            this.mTitleTextView.setText(getString(C0380R.string.saved_upper));
        } else if (i5 == 3) {
            this.mCancelButton.setVisibility(0);
            if (this.sendNotification) {
                this.sendNotification = false;
                new AlertDialog.Builder(this).setMessage(getString(C0380R.string.set_lower_limit)).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationManualPressure.12
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i6) {
                        dialogInterface.cancel();
                    }
                }).setIcon(R.drawable.ic_dialog_info).show();
            }
            this.mCancelButton.setText(getString(C0380R.string.Continue));
            String str5 = getString(C0380R.string.set_min_pressure) + " ";
            this.mTitleTextView.setText(str5 + str);
            this.mStatusTextView.setText(getString(C0380R.string.use_up_down_manual_keys_to_set_minimum_pressure_limit));
        } else if (i5 == 4) {
            this.mCancelButton.setVisibility(4);
            this.mTitleTextView.setText(getString(C0380R.string.saved_lower));
        } else if (i5 == 5) {
            stopCalibration();
            this.mCommService.setCommServiceListener(null);
            mIgnoreOnPause = true;
            Intent intent = new Intent(this, (Class<?>) CalibrationPressure.class);
            finish();
            startActivity(intent);
        }
        if ((this.mCommService.alp3Device.canpng65304ECUIO.Output[1] > 0 || this.mCommService.alp3Device.canpng65304ECUIO.Output[0] > 0) && this.mCommService.alp3Device.canpng65300ECUStatus.SubMode < ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_SUCCESS.ordinal()) {
            this.mStatusTextView.setText(getString(C0380R.string.low_pressure_waiting_for_compressor));
        }
    }

    /* renamed from: com.airliftcompany.alp3.calibration.CalibrationManualPressure$13 */
    static /* synthetic */ class C039213 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$comm$ALP3Protocol$ECU_ManualCalModeStateEnum */
        static final /* synthetic */ int[] f51x67b34e72;

        static {
            int[] iArr = new int[ALP3Protocol.ECU_ManualCalModeStateEnum.values().length];
            f51x67b34e72 = iArr;
            try {
                iArr[ALP3Protocol.ECU_ManualCalModeStateEnum.SUB_MANUALCAL_GO_TOP.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f51x67b34e72[ALP3Protocol.ECU_ManualCalModeStateEnum.SUB_MANUALCAL_SAVE_TOP.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f51x67b34e72[ALP3Protocol.ECU_ManualCalModeStateEnum.SUB_MANUALCAL_GO_BOTTOM.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f51x67b34e72[ALP3Protocol.ECU_ManualCalModeStateEnum.SUB_MANUALCAL_SAVE_BOTTOM.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                f51x67b34e72[ALP3Protocol.ECU_ManualCalModeStateEnum.SUB_MANUALCAL_FINISHED.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }
}