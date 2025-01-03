package com.airliftcompany.alp3.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.core.app.NavUtils;
import com.airliftcompany.alp3.C0380R;
import com.airliftcompany.alp3.comm.ALP3Protocol;
import com.airliftcompany.alp3.comm.CommService;
import com.airliftcompany.alp3.custom.ALP3Activity;
import com.airliftcompany.alp3.custom.RepeatListener;
import com.airliftcompany.alp3.custom.SensorGraphView;
import com.airliftcompany.alp3.custom.ShapedButton;

/* loaded from: classes.dex */
public class SettingsSensorTool extends ALP3Activity {
    private static final int AD_RANGE_INVERTED_MIN = 187;
    private static final int AD_RANGE_MAX = 950;
    private static final int AD_RANGE_NORMAL_MIN = 77;
    private static final int HEIGHT_HIGH_LIMIT_FAULT_ADLEVEL = 823;
    private static final int HEIGHT_LOW_LIMIT_FAULT_ADLEVEL = 108;
    private static final String TAG = "SettingsSensorTool";
    private SensorGraphView lfSensorGraphView;
    private SensorGraphView lrSensorGraphView;
    private SensorGraphView rfSensorGraphView;
    private SensorGraphView rrSensorGraphView;
    private long toolHeightTemp;
    private long[] rangeLo = new long[4];
    private long[] rangeHi = new long[4];
    private long[] rangeTotal = new long[4];

    private class SetADStreamingAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        private Context context;
        private boolean enableAD;
        ProgressDialog progressDialog;

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onCancelled(Boolean bool) {
        }

        public SetADStreamingAsyncTask(Context context, boolean z) {
            this.context = context;
            this.enableAD = z;
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            ProgressDialog progressDialog = new ProgressDialog(this.context);
            this.progressDialog = progressDialog;
            progressDialog.setMessage(SettingsSensorTool.this.getString(C0380R.string.please_wait));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setProgressStyle(0);
            this.progressDialog.getWindow().setFlags(8, 8);
            this.progressDialog.show();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Boolean doInBackground(Void... voidArr) {
            SettingsSensorTool.this.mCommService.com100msState = CommService.com100msEnum.COM100mS_PAUSE;
            if (!SettingsSensorTool.this.mCommService.txController.txWriteRecord((short) 144, this.enableAD ? (short) 1 : (short) 0)) {
                return false;
            }
            publishProgress(99);
            SettingsSensorTool.this.mCommService.com100msState = CommService.com100msEnum.COM100mS_STATUS;
            return true;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Integer... numArr) {
            this.progressDialog.setProgress(numArr[0].intValue());
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Boolean bool) {
            ProgressDialog progressDialog = this.progressDialog;
            if (progressDialog != null && progressDialog.isShowing()) {
                this.progressDialog.setProgressStyle(0);
                this.progressDialog.dismiss();
            }
            this.progressDialog = null;
            if (!bool.booleanValue()) {
                Toast.makeText(this.context, C0380R.string.error_communicating_with_manifold, 1).show();
            }
            if (this.enableAD) {
                return;
            }
            SettingsSensorTool.this.mCommService.setCommServiceListener(null);
            ALP3Activity.mIgnoreOnPause = true;
            NavUtils.navigateUpFromSameTask(SettingsSensorTool.this);
        }
    }

    @Override // com.airliftcompany.alp3.custom.ALP3Activity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(C0380R.string.sensor_tool));
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(C0380R.layout.activity_sensor_tool);
        SensorGraphView sensorGraphView = (SensorGraphView) findViewById(C0380R.id.lfSensorGraphView);
        this.lfSensorGraphView = sensorGraphView;
        sensorGraphView.setGraphName("FL");
        SensorGraphView sensorGraphView2 = (SensorGraphView) findViewById(C0380R.id.rfSensorGraphView);
        this.rfSensorGraphView = sensorGraphView2;
        sensorGraphView2.setGraphName("FR");
        SensorGraphView sensorGraphView3 = (SensorGraphView) findViewById(C0380R.id.lrSensorGraphView);
        this.lrSensorGraphView = sensorGraphView3;
        sensorGraphView3.setGraphName("RL");
        SensorGraphView sensorGraphView4 = (SensorGraphView) findViewById(C0380R.id.rrSensorGraphView);
        this.rrSensorGraphView = sensorGraphView4;
        sensorGraphView4.setGraphName("RR");
        ShapedButton shapedButton = (ShapedButton) findViewById(C0380R.id.leftFrontUpButton);
        shapedButton.normalDrawable = getResources().getDrawable(C0380R.drawable.lfu);
        shapedButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.lfu_pressed);
        shapedButton.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.settings.SettingsSensorTool.1
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SettingsSensorTool.this.handleValveButtonEvent(motionEvent, (short) 0, (short) 1);
                return true;
            }
        }));
        ShapedButton shapedButton2 = (ShapedButton) findViewById(C0380R.id.leftFrontDownButton);
        shapedButton2.normalDrawable = getResources().getDrawable(C0380R.drawable.lfd);
        shapedButton2.selectedDrawble = getResources().getDrawable(C0380R.drawable.lfd_pressed);
        shapedButton2.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.settings.SettingsSensorTool.2
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SettingsSensorTool.this.handleValveButtonEvent(motionEvent, (short) 0, (short) 2);
                return true;
            }
        }));
        ShapedButton shapedButton3 = (ShapedButton) findViewById(C0380R.id.rightFrontUpButton);
        shapedButton3.normalDrawable = getResources().getDrawable(C0380R.drawable.rfu);
        shapedButton3.selectedDrawble = getResources().getDrawable(C0380R.drawable.rfu_pressed);
        shapedButton3.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.settings.SettingsSensorTool.3
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SettingsSensorTool.this.handleValveButtonEvent(motionEvent, (short) 1, (short) 1);
                return true;
            }
        }));
        ShapedButton shapedButton4 = (ShapedButton) findViewById(C0380R.id.rightFrontDownButton);
        shapedButton4.normalDrawable = getResources().getDrawable(C0380R.drawable.rfd);
        shapedButton4.selectedDrawble = getResources().getDrawable(C0380R.drawable.rfd_pressed);
        shapedButton4.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.settings.SettingsSensorTool.4
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SettingsSensorTool.this.handleValveButtonEvent(motionEvent, (short) 1, (short) 2);
                return true;
            }
        }));
        ShapedButton shapedButton5 = (ShapedButton) findViewById(C0380R.id.leftRearUpButton);
        shapedButton5.normalDrawable = getResources().getDrawable(C0380R.drawable.lru);
        shapedButton5.selectedDrawble = getResources().getDrawable(C0380R.drawable.lru_pressed);
        shapedButton5.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.settings.SettingsSensorTool.5
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SettingsSensorTool.this.handleValveButtonEvent(motionEvent, (short) 2, (short) 1);
                return true;
            }
        }));
        ShapedButton shapedButton6 = (ShapedButton) findViewById(C0380R.id.leftRearDownButton);
        shapedButton6.normalDrawable = getResources().getDrawable(C0380R.drawable.lrd);
        shapedButton6.selectedDrawble = getResources().getDrawable(C0380R.drawable.lrd_pressed);
        shapedButton6.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.settings.SettingsSensorTool.6
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SettingsSensorTool.this.handleValveButtonEvent(motionEvent, (short) 2, (short) 2);
                return true;
            }
        }));
        ShapedButton shapedButton7 = (ShapedButton) findViewById(C0380R.id.rightRearUpButton);
        shapedButton7.normalDrawable = getResources().getDrawable(C0380R.drawable.rru);
        shapedButton7.selectedDrawble = getResources().getDrawable(C0380R.drawable.rru_pressed);
        shapedButton7.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.settings.SettingsSensorTool.7
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SettingsSensorTool.this.handleValveButtonEvent(motionEvent, (short) 3, (short) 1);
                return true;
            }
        }));
        ShapedButton shapedButton8 = (ShapedButton) findViewById(C0380R.id.rightRearDownButton);
        shapedButton8.normalDrawable = getResources().getDrawable(C0380R.drawable.rrd);
        shapedButton8.selectedDrawble = getResources().getDrawable(C0380R.drawable.rrd_pressed);
        shapedButton8.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.settings.SettingsSensorTool.8
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SettingsSensorTool.this.handleValveButtonEvent(motionEvent, (short) 3, (short) 2);
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
        shapedButton12.setEnabled(true);
        shapedButton12.normalDrawable = getResources().getDrawable(C0380R.drawable.preset_02);
        shapedButton12.normalBlinkDrawable = getResources().getDrawable(C0380R.drawable.preset_02_pressed);
        shapedButton12.selectedDrawble = getResources().getDrawable(C0380R.drawable.preset_02_pressed);
        shapedButton12.altNormalDrawable = getResources().getDrawable(C0380R.drawable.preset_02_selected);
        shapedButton12.setOnTouchListener(new View.OnTouchListener() { // from class: com.airliftcompany.alp3.settings.SettingsSensorTool.9
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked != 0) {
                    if (actionMasked != 1 && actionMasked != 3 && actionMasked != 4) {
                        if (actionMasked != 5) {
                            if (actionMasked != 6) {
                                return true;
                            }
                        }
                    }
                    for (int i = 0; i < 4; i++) {
                        SettingsSensorTool.this.rangeTotal[i] = 0;
                        SettingsSensorTool.this.rangeHi[i] = 0;
                        SettingsSensorTool.this.rangeLo[i] = 1024;
                    }
                    return true;
                }
                return false;
            }
        });
        ShapedButton shapedButton13 = (ShapedButton) findViewById(C0380R.id.allDownButton);
        shapedButton13.setEnabled(false);
        shapedButton13.normalDrawable = getResources().getDrawable(C0380R.drawable.all_down);
        shapedButton13.normalBlinkDrawable = getResources().getDrawable(C0380R.drawable.all_down_pressed);
        shapedButton13.selectedDrawble = getResources().getDrawable(C0380R.drawable.all_down_pressed);
        shapedButton13.altNormalDrawable = getResources().getDrawable(C0380R.drawable.all_down_selected);
    }

    @Override // com.airliftcompany.alp3.custom.ALP3Activity
    public void commServiceConnected() {
        super.commServiceConnected();
        for (int i = 0; i < 4; i++) {
            this.mCommService.alp3Device.canpng65308ECUSpringHeightAD.Height[i] = 4095;
            this.rangeTotal[i] = 0;
            this.rangeHi[i] = 0;
            this.rangeLo[i] = 1024;
        }
        this.mCommService.setCommServiceListener(this);
        if (this.mCommService.canpng65400UIStatus.ControlMode > ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_MANUAL_CONTROL.ordinal()) {
            this.mCommService.canpng65400UIStatus.ControlMode = (short) ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_MANUAL_CONTROL.ordinal();
            this.mCommService.canpng65400UIStatus.SubControlMode = (short) 0;
        }
        new SetADStreamingAsyncTask(this, true).execute(new Void[0]);
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

    private void closeActivity() {
        new SetADStreamingAsyncTask(this, false).execute(new Void[0]);
    }

    @Override // com.airliftcompany.alp3.custom.ALP3Activity
    public void updateUI() {
        Float valueOf;
        super.updateUI();
        long[] jArr = new long[4];
        if (this.mCommService.alp3Device.displaySettings.Units > 0) {
            float f = (float) ((this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[0] / 10) * 0.0689475729d);
            int i = (int) f;
            this.lfSensorGraphView.setPressure(String.format("%d.%02d bar", Integer.valueOf(i), Integer.valueOf((int) ((f - i) * 100.0f))));
            float f2 = (float) ((this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[1] / 10) * 0.0689475729d);
            int i2 = (int) f2;
            this.rfSensorGraphView.setPressure(String.format("%d.%02d bar", Integer.valueOf(i2), Integer.valueOf((int) ((f2 - i2) * 100.0f))));
            float f3 = (float) ((this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[2] / 10) * 0.0689475729d);
            int i3 = (int) f3;
            this.lrSensorGraphView.setPressure(String.format("%d.%02d bar", Integer.valueOf(i3), Integer.valueOf((int) ((f3 - i3) * 100.0f))));
            float f4 = (float) ((this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[3] / 10) * 0.0689475729d);
            int i4 = (int) f4;
            this.rrSensorGraphView.setPressure(String.format("%d.%02d bar", Integer.valueOf(i4), Integer.valueOf((int) ((f4 - i4) * 100.0f))));
        } else {
            this.lfSensorGraphView.setPressure(String.format("%d", Long.valueOf(this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[0] / 10)));
            this.rfSensorGraphView.setPressure(String.format("%d", Long.valueOf(this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[1] / 10)));
            this.lrSensorGraphView.setPressure(String.format("%d", Long.valueOf(this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[2] / 10)));
            this.rrSensorGraphView.setPressure(String.format("%d", Long.valueOf(this.mCommService.alp3Device.canpng65301ECUSpringPressure.PressureFiltered[3] / 10)));
        }
        if (this.mCommService.alp3Device.canpng65308ECUSpringHeightAD.Height[0] == 4095) {
            return;
        }
        for (int i5 = 0; i5 < 4; i5++) {
            jArr[i5] = this.mCommService.alp3Device.canpng65308ECUSpringHeightAD.Height[i5];
            if (this.mCommService.alp3Device.displaySettings.SensorIsInverted[i5] == 1) {
                if (jArr[i5] < 187) {
                    jArr[i5] = 187;
                }
            } else if (jArr[i5] < 77) {
                jArr[i5] = 77;
            }
            if (jArr[i5] > 950) {
                jArr[i5] = 950;
            }
            long j = jArr[i5];
            long[] jArr2 = this.rangeHi;
            if (j > jArr2[i5]) {
                jArr2[i5] = jArr[i5];
            }
            long j2 = jArr[i5];
            long[] jArr3 = this.rangeLo;
            if (j2 < jArr3[i5]) {
                jArr3[i5] = jArr[i5];
            }
            this.rangeTotal[i5] = jArr2[i5] - jArr3[i5];
        }
        SensorGraphView[] sensorGraphViewArr = {this.lfSensorGraphView, this.rfSensorGraphView, this.lrSensorGraphView, this.rrSensorGraphView};
        int i6 = 0;
        for (int i7 = 0; i7 < 4; i7++) {
            SensorGraphView sensorGraphView = sensorGraphViewArr[i7];
            Float.valueOf(0.0f);
            if (this.mCommService.alp3Device.displaySettings.SensorIsInverted[i6] == 1) {
                valueOf = Float.valueOf((jArr[i6] - 187.0f) / 763.0f);
            } else {
                valueOf = Float.valueOf((jArr[i6] - 77.0f) / 873.0f);
            }
            sensorGraphView.setPercentValue(valueOf);
            if (this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorLimit[i6] > 0 || this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorNotPresent[i6] > 0) {
                sensorGraphView.setBarColor(Color.rgb(175, 0, 0));
            } else {
                sensorGraphView.setBarColor(Color.rgb(0, 175, 0));
            }
            if (this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorNotPresent[i6] > 0) {
                sensorGraphView.setStatus(getString(C0380R.string.not_found));
            } else if (this.mCommService.alp3Device.canpng65280ECUFault.HeightSensorLimit[i6] > 0) {
                sensorGraphView.setStatus(getString(C0380R.string.Limit));
            } else if (this.rangeTotal[i6] < 250) {
                sensorGraphView.setStatus(getString(C0380R.string.range_low));
            } else {
                sensorGraphView.setStatus(getString(C0380R.string.OK));
            }
            i6++;
        }
        if (this.mCommService.alp3Device.canpng65304ECUIO.Output[1] > 0 || this.mCommService.alp3Device.canpng65304ECUIO.Output[0] > 0) {
            short s = this.mCommService.alp3Device.canpng65300ECUStatus.SubMode;
            ALP3Protocol.ECU_CalPressureControlStateEnum.OP_MODE_PCAL_SUCCESS.ordinal();
        }
    }
}
