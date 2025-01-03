package com.airliftcompany.alp3;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.airliftcompany.alp3.calibration.CalibrationQuestions;
import com.airliftcompany.alp3.comm.ALP3Device;
import com.airliftcompany.alp3.comm.ALP3Protocol;
import com.airliftcompany.alp3.comm.CommService;
import com.airliftcompany.alp3.comm.CommServiceListener;
import com.airliftcompany.alp3.custom.RepeatListener;
import com.airliftcompany.alp3.custom.ShapedButton;
import com.airliftcompany.alp3.firmware.ALP3Programmer;
import com.airliftcompany.alp3.firmware.FirmwareUpdateActivity;
import com.airliftcompany.alp3.firmware.FirmwareVersionDownloadUtility;
import com.airliftcompany.alp3.firmware.UpdateAvailableActivity;
import com.airliftcompany.alp3.settings.SettingsHome;
import com.airliftcompany.alp3.utils.ALP3Preferences;
import com.airliftcompany.alp3.utils.CognitoService;
import com.airliftcompany.alp3.utils.FaultInfo;
import com.airliftcompany.alp3.utils.Util;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.util.CognitoServiceConstants;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class MainActivity extends AppCompatActivity implements CommServiceListener, ALP3Programmer.ProgramFirmwareAsyncTaskListener {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int ARROW_TOP_MARGIN = 2;
    private static final int CANCEL_PRESET_TIME_MS = 5000;
    private static final int FAULT_TIMING_MS = 2000;
    private static final int PRESET_LONG_TOUCH_TIME_MS = 2000;
    static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "MainActivity";
    public static final int VALVE_INITIAL_DELAY_TIME_MS = 500;
    public static final int VALVE_REPEAT_DELAY_TIME_MS = 50;
    private static boolean mDisplayedCalibrationWarning = false;
    private static boolean mIgnoreOnPause = false;
    private static boolean mLongTouchFired = false;
    private static short mLongTouchPresetIndex = 0;
    private static boolean mSkipDeviceScanActivity = false;
    private FirmwareVersionDownloadUtility FirmwareVersionDownloadUtility;
    private ImageView faultImageView;
    private RelativeLayout faultLayout;
    private TextView faultTextView;
    private Timer faultTimer;
    private FaultTimerTask faultTimerTask;
    private ImageView lfArrow;
    private ShapedButton lfDownButton;
    private ImageView lfPresetArrow;
    private TextView lfPresetTextView;
    private TextView lfTextView;
    private ShapedButton lfUpButton;
    private ImageView lrArrow;
    private ShapedButton lrDownButton;
    private ImageView lrPresetArrow;
    private TextView lrPresetTextView;
    private TextView lrTextView;
    private ShapedButton lrUpButton;
    private ALP3Device mALP3Device;
    private ShapedButton mAllDownButton;
    private ShapedButton mAllUpButton;
    private CommService mCommService;
    private ProgressDialog mDialog;
    private RelativeLayout mDisplayLayout;
    private ArrayList<FaultInfo> mFaultArrayList;
    private int mFaultIndex;
    private ShapedButton mNormalRideButton;
    private ShapedButton mPresetDownButton;
    private ShapedButton mPresetUpButton;
    private TextView mainModeLabel;
    private ImageView rfArrow;
    private ShapedButton rfDownButton;
    private ImageView rfPresetArrow;
    private TextView rfPresetTextView;
    private TextView rfTextView;
    private ShapedButton rfUpButton;
    private ImageView rrArrow;
    private ShapedButton rrDownButton;
    private ImageView rrPresetArrow;
    private TextView rrPresetTextView;
    private TextView rrTextView;
    private ShapedButton rrUpButton;
    private TextView showModeTextView;
    private TextView tankTextView;
    boolean[] gFaultPressureDisplayYLevel = new boolean[4];
    boolean[] gFaultPressureDisplayRLevel = new boolean[4];
    boolean[] gFaultHeightDisplayYLevel = new boolean[4];
    boolean[] gFaultHeightDisplayRLevel = new boolean[4];
    boolean gFaultTankYLevel = false;
    boolean gFaultTankRLevel = false;
    private int mPresetPreviewIndex = -1;
    private Handler cancelPresetHandler = new Handler();
    private Handler longButtonTouchHandler = new Handler();
    private Runnable cancelPreviewRunnable = new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.19
        @Override // java.lang.Runnable
        public void run() {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.19.1
                @Override // java.lang.Runnable
                public void run() {
                    MainActivity.this.mPresetPreviewIndex = -1;
                    MainActivity.this.mALP3Device.adjustingPresets.amSavingPreset = false;
                    MainActivity.this.lfPresetTextView.setVisibility(4);
                    MainActivity.this.rfPresetTextView.setVisibility(4);
                    MainActivity.this.lrPresetTextView.setVisibility(4);
                    MainActivity.this.rrPresetTextView.setVisibility(4);
                    MainActivity.this.lfPresetArrow.setVisibility(4);
                    MainActivity.this.rfPresetArrow.setVisibility(4);
                    MainActivity.this.lrPresetArrow.setVisibility(4);
                    MainActivity.this.rrPresetArrow.setVisibility(4);
                    MainActivity.this.lfTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.white));
                    MainActivity.this.rfTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.white));
                    MainActivity.this.lrTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.white));
                    MainActivity.this.rrTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.white));
                    MainActivity.this.lfArrow.setImageResource(C0380R.drawable.left_arrow);
                    MainActivity.this.rfArrow.setImageResource(C0380R.drawable.right_arrow);
                    MainActivity.this.lrArrow.setImageResource(C0380R.drawable.left_arrow);
                    MainActivity.this.rrArrow.setImageResource(C0380R.drawable.right_arrow);
                }
            });
        }
    };
    private Runnable longButtonTouchRunnable = new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.20
        @Override // java.lang.Runnable
        public void run() {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.20.1
                @Override // java.lang.Runnable
                public void run() {
                    MainActivity.this.handlePresetLongButtonEvent(MainActivity.mLongTouchPresetIndex);
                    boolean unused = MainActivity.mLongTouchFired = true;
                }
            });
        }
    };
    private final ServiceConnection mServiceConnection = new ServiceConnection() { // from class: com.airliftcompany.alp3.MainActivity.21
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(MainActivity.TAG, "onServiceConnected");
            MainActivity.this.mCommService = ((CommService.LocalBinder) iBinder).getService();
            MainActivity.this.mCommService.setCommServiceListener(MainActivity.this);
            MainActivity mainActivity = MainActivity.this;
            mainActivity.mALP3Device = mainActivity.mCommService.alp3Device;
            if (Build.VERSION.SDK_INT >= 26) {
                MainActivity.this.startMyOwnForeground();
            }
            MainActivity.this.updateUI();
            MainActivity.this.faultTimerTask.run();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(MainActivity.TAG, "onServiceDisconnected");
            MainActivity.this.mCommService = null;
        }
    };
    ForgotPasswordHandler forgotPasswordHandler = new ForgotPasswordHandler() { // from class: com.airliftcompany.alp3.MainActivity.35
        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler
        public void onSuccess() {
            Log.e("tag", "onSuccess");
        }

        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler
        public void getResetCode(ForgotPasswordContinuation forgotPasswordContinuation) {
            Log.e("tag", "getResetCode");
        }

        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler
        public void onFailure(Exception exc) {
            Log.e("tag", exc.getLocalizedMessage());
        }
    };
    AuthenticationHandler authenticationHandler = new AuthenticationHandler() { // from class: com.airliftcompany.alp3.MainActivity.36
        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
        public void authenticationChallenge(ChallengeContinuation challengeContinuation) {
            if (challengeContinuation.getChallengeName().equals(CognitoServiceConstants.CHLG_TYPE_NEW_PASSWORD_REQUIRED)) {
                ((NewPasswordContinuation) challengeContinuation).setPassword("A;O5Dll7");
                challengeContinuation.continueTask();
            }
            Log.e("tag", "authenticationChallenge");
        }

        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice cognitoDevice) {
            Log.e("tag", "onSuccess");
        }

        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String str) {
            authenticationContinuation.setAuthenticationDetails(new AuthenticationDetails(str, "B;O5Dll7", (Map<String, String>) null));
            authenticationContinuation.continueTask();
        }

        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
            Log.e("tag", "getMFACode");
        }

        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
        public void onFailure(Exception exc) {
            Log.e("tag", exc.getLocalizedMessage());
        }
    };

    static /* synthetic */ int access$4308(MainActivity mainActivity) {
        int i = mainActivity.mFaultIndex;
        mainActivity.mFaultIndex = i + 1;
        return i;
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        loadView();
        this.mFaultArrayList = new ArrayList<>();
        if (getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            return;
        }
        Toast.makeText(this, "This application requires BLE support", 1).show();
        Toast.makeText(this, C0380R.string.About, 1).show();
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        if (this.mCommService != null) {
            unbindService(this.mServiceConnection);
            stopService(new Intent(this, (Class<?>) CommService.class));
            this.mCommService = null;
        }
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        loadView();
    }

    private void loadView() {
        setContentView(C0380R.layout.activity_main);
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.mDialog = progressDialog;
        progressDialog.getWindow().setFlags(8, 8);
        this.mainModeLabel = (TextView) findViewById(C0380R.id.mainModeLabel);
        this.mDisplayLayout = (RelativeLayout) findViewById(C0380R.id.displayLayout);
        this.lfArrow = (ImageView) findViewById(C0380R.id.lfArrow);
        this.rfArrow = (ImageView) findViewById(C0380R.id.rfArrow);
        this.lrArrow = (ImageView) findViewById(C0380R.id.lrArrow);
        this.rrArrow = (ImageView) findViewById(C0380R.id.rrArrow);
        this.lfPresetArrow = (ImageView) findViewById(C0380R.id.lfPresetArrow);
        this.rfPresetArrow = (ImageView) findViewById(C0380R.id.rfPresetArrow);
        this.lrPresetArrow = (ImageView) findViewById(C0380R.id.lrPresetArrow);
        this.rrPresetArrow = (ImageView) findViewById(C0380R.id.rrPresetArrow);
        this.tankTextView = (TextView) findViewById(C0380R.id.tankTextView);
        this.lfTextView = (TextView) findViewById(C0380R.id.leftFrontTextView);
        this.rfTextView = (TextView) findViewById(C0380R.id.rightFrontTextView);
        this.lrTextView = (TextView) findViewById(C0380R.id.leftRearTextView);
        this.rrTextView = (TextView) findViewById(C0380R.id.rightRearTextView);
        this.lfPresetTextView = (TextView) findViewById(C0380R.id.lfPresetTextView);
        this.rfPresetTextView = (TextView) findViewById(C0380R.id.rfPresetTextView);
        this.lrPresetTextView = (TextView) findViewById(C0380R.id.lrPresetTextView);
        this.rrPresetTextView = (TextView) findViewById(C0380R.id.rrPresetTextView);
        this.faultLayout = (RelativeLayout) findViewById(C0380R.id.faultLayout);
        this.faultTextView = (TextView) findViewById(C0380R.id.faultTextView);
        this.faultImageView = (ImageView) findViewById(C0380R.id.faultImageView);
        this.mAllUpButton = (ShapedButton) findViewById(C0380R.id.allUpButton);
        this.mPresetUpButton = (ShapedButton) findViewById(C0380R.id.presetUpButton);
        this.mNormalRideButton = (ShapedButton) findViewById(C0380R.id.normalRideButton);
        this.mPresetDownButton = (ShapedButton) findViewById(C0380R.id.presetDownButton);
        this.mAllDownButton = (ShapedButton) findViewById(C0380R.id.allDownButton);
        this.showModeTextView = (TextView) findViewById(C0380R.id.showModeTextView);
        ShapedButton shapedButton = (ShapedButton) findViewById(C0380R.id.leftFrontUpButton);
        this.lfUpButton = shapedButton;
        shapedButton.normalDrawable = getResources().getDrawable(C0380R.drawable.lfu);
        this.lfUpButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.lfu_pressed);
        this.lfUpButton.setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.MainActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Log.i(MainActivity.TAG, "onClick");
            }
        });
        this.lfUpButton.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.2
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MainActivity.this.handleValveButtonEvent(motionEvent, (short) 0, (short) 1);
                return true;
            }
        }));
        ShapedButton shapedButton2 = (ShapedButton) findViewById(C0380R.id.leftFrontDownButton);
        this.lfDownButton = shapedButton2;
        shapedButton2.normalDrawable = getResources().getDrawable(C0380R.drawable.lfd);
        this.lfDownButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.lfd_pressed);
        this.lfDownButton.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.3
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MainActivity.this.handleValveButtonEvent(motionEvent, (short) 0, (short) 2);
                return true;
            }
        }));
        ShapedButton shapedButton3 = (ShapedButton) findViewById(C0380R.id.rightFrontUpButton);
        this.rfUpButton = shapedButton3;
        shapedButton3.normalDrawable = getResources().getDrawable(C0380R.drawable.rfu);
        this.rfUpButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.rfu_pressed);
        this.rfUpButton.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.4
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MainActivity.this.handleValveButtonEvent(motionEvent, (short) 1, (short) 1);
                return true;
            }
        }));
        ShapedButton shapedButton4 = (ShapedButton) findViewById(C0380R.id.rightFrontDownButton);
        this.rfDownButton = shapedButton4;
        shapedButton4.normalDrawable = getResources().getDrawable(C0380R.drawable.rfd);
        this.rfDownButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.rfd_pressed);
        this.rfDownButton.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.5
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MainActivity.this.handleValveButtonEvent(motionEvent, (short) 1, (short) 2);
                return true;
            }
        }));
        ShapedButton shapedButton5 = (ShapedButton) findViewById(C0380R.id.leftRearUpButton);
        this.lrUpButton = shapedButton5;
        shapedButton5.normalDrawable = getResources().getDrawable(C0380R.drawable.lru);
        this.lrUpButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.lru_pressed);
        this.lrUpButton.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.6
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MainActivity.this.handleValveButtonEvent(motionEvent, (short) 2, (short) 1);
                return true;
            }
        }));
        ShapedButton shapedButton6 = (ShapedButton) findViewById(C0380R.id.leftRearDownButton);
        this.lrDownButton = shapedButton6;
        shapedButton6.normalDrawable = getResources().getDrawable(C0380R.drawable.lrd);
        this.lrDownButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.lrd_pressed);
        this.lrDownButton.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.7
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MainActivity.this.handleValveButtonEvent(motionEvent, (short) 2, (short) 2);
                return true;
            }
        }));
        ShapedButton shapedButton7 = (ShapedButton) findViewById(C0380R.id.rightRearUpButton);
        this.rrUpButton = shapedButton7;
        shapedButton7.normalDrawable = getResources().getDrawable(C0380R.drawable.rru);
        this.rrUpButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.rru_pressed);
        this.rrUpButton.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.8
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MainActivity.this.handleValveButtonEvent(motionEvent, (short) 3, (short) 1);
                return true;
            }
        }));
        ShapedButton shapedButton8 = (ShapedButton) findViewById(C0380R.id.rightRearDownButton);
        this.rrDownButton = shapedButton8;
        shapedButton8.normalDrawable = getResources().getDrawable(C0380R.drawable.rrd);
        this.rrDownButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.rrd_pressed);
        this.rrDownButton.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.9
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MainActivity.this.handleValveButtonEvent(motionEvent, (short) 3, (short) 2);
                return true;
            }
        }));
        this.mAllUpButton.normalDrawable = getResources().getDrawable(C0380R.drawable.all_up);
        this.mAllUpButton.normalBlinkDrawable = getResources().getDrawable(C0380R.drawable.all_up_pressed);
        this.mAllUpButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.all_up_pressed);
        this.mAllUpButton.altNormalDrawable = getResources().getDrawable(C0380R.drawable.all_up_selected);
        ShapedButton shapedButton9 = (ShapedButton) findViewById(C0380R.id.presetUpButton);
        this.mPresetUpButton = shapedButton9;
        shapedButton9.normalDrawable = getResources().getDrawable(C0380R.drawable.preset_01);
        this.mPresetUpButton.normalBlinkDrawable = getResources().getDrawable(C0380R.drawable.preset_01_pressed);
        this.mPresetUpButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.preset_01_pressed);
        this.mPresetUpButton.altNormalDrawable = getResources().getDrawable(C0380R.drawable.preset_01_selected);
        ShapedButton shapedButton10 = (ShapedButton) findViewById(C0380R.id.normalRideButton);
        this.mNormalRideButton = shapedButton10;
        shapedButton10.normalDrawable = getResources().getDrawable(C0380R.drawable.airlift);
        this.mNormalRideButton.normalBlinkDrawable = getResources().getDrawable(C0380R.drawable.airlift_pressed);
        this.mNormalRideButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.airlift_pressed);
        this.mNormalRideButton.altNormalDrawable = getResources().getDrawable(C0380R.drawable.airlift_selected);
        ShapedButton shapedButton11 = (ShapedButton) findViewById(C0380R.id.presetDownButton);
        this.mPresetDownButton = shapedButton11;
        shapedButton11.normalDrawable = getResources().getDrawable(C0380R.drawable.preset_02);
        this.mPresetDownButton.normalBlinkDrawable = getResources().getDrawable(C0380R.drawable.preset_02_pressed);
        this.mPresetDownButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.preset_02_pressed);
        this.mPresetDownButton.altNormalDrawable = getResources().getDrawable(C0380R.drawable.preset_02_selected);
        ShapedButton shapedButton12 = (ShapedButton) findViewById(C0380R.id.allDownButton);
        this.mAllDownButton = shapedButton12;
        shapedButton12.normalDrawable = getResources().getDrawable(C0380R.drawable.all_down);
        this.mAllDownButton.normalBlinkDrawable = getResources().getDrawable(C0380R.drawable.all_down_pressed);
        this.mAllDownButton.selectedDrawble = getResources().getDrawable(C0380R.drawable.all_down_pressed);
        this.mAllDownButton.altNormalDrawable = getResources().getDrawable(C0380R.drawable.all_down_selected);
        ((ImageButton) findViewById(C0380R.id.settingsButton)).setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.MainActivity.10
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                boolean unused = MainActivity.mIgnoreOnPause = true;
                MainActivity.this.startActivity(new Intent(MainActivity.this, (Class<?>) SettingsHome.class));
            }
        });
        if (this.mCommService != null) {
            setPresetButtonHandlers();
            updateUI();
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        this.faultTimer.cancel();
        this.faultTimer = null;
        if (mIgnoreOnPause) {
            mIgnoreOnPause = false;
            return;
        }
        ALP3Device aLP3Device = this.mALP3Device;
        if (aLP3Device != null && !aLP3Device.canpng65404ManualControl.valvesAreClosed()) {
            int[] iArr = this.mALP3Device.canpng65404ManualControl.Spring;
            int[] iArr2 = this.mALP3Device.canpng65404ManualControl.Spring;
            int[] iArr3 = this.mALP3Device.canpng65404ManualControl.Spring;
            this.mALP3Device.canpng65404ManualControl.Spring[3] = 0;
            iArr3[2] = 0;
            iArr2[1] = 0;
            iArr[0] = 0;
            this.mALP3Device.canpng65404ManualControl.Direction = 0;
            this.mCommService.forceSendManualMode();
            new Handler().postDelayed(new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.11
                @Override // java.lang.Runnable
                public void run() {
                    MainActivity.this.handleOnPause();
                }
            }, 500L);
            return;
        }
        handleOnPause();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleOnPause() {
        Log.i(TAG, "handleOnPause");
        CommService commService = this.mCommService;
        if (commService != null) {
            commService.disconnectBleSession();
        }
        ServiceConnection serviceConnection = this.mServiceConnection;
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
        try {
            stopService(new Intent(this, (Class<?>) CommService.class));
        } catch (NullPointerException e) {
            Log.i(TAG, e.getMessage());
        }
        this.mCommService = null;
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        getWindow().getDecorView().setSystemUiVisibility(Build.VERSION.SDK_INT > 18 ? 4868 : 772);
        CommService commService = this.mCommService;
        if (commService != null) {
            commService.setCommServiceListener(this);
            if (!this.mCommService.commStatus.BlueToothAccConnected) {
                reconnectIfPossible();
            }
            setPresetButtonHandlers();
        } else {
            Intent intent = new Intent(this, (Class<?>) CommService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            bindService(intent, this.mServiceConnection, 1);
        }
        this.faultTimer = new Timer();
        FaultTimerTask faultTimerTask = new FaultTimerTask();
        this.faultTimerTask = faultTimerTask;
        this.faultTimer.schedule(faultTimerTask, 2000L, 2000L);
        if (ALP3Preferences.preventSleep(getApplicationContext()).booleanValue()) {
            getWindow().addFlags(128);
        }
        CheckProductRegistration();
    }

    @Override // com.airliftcompany.alp3.firmware.ALP3Programmer.ProgramFirmwareAsyncTaskListener
    public void onTaskCompleted(Boolean bool) {
        Log.i(TAG, "onTaskCompleted");
        Toast.makeText(this, "Firmware update complete", 1).show();
    }

    @Override // com.airliftcompany.alp3.firmware.ALP3Programmer.ProgramFirmwareAsyncTaskListener
    public void onTaskProgressUpdate(Integer num, String str) {
        Log.i(TAG, "onTaskProgressUpdate");
        Toast.makeText(this, str + Float.toString(num.intValue()), 1).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setPresetButtonHandlers() {
        if (this.mALP3Device.displaySettings.allUpButtonsEnum() == ALP3Device.KeypadAllUpButtonsEnum.KEYPAD_ALL_UP || this.mALP3Device.displaySettings.allUpButtonsEnum() == ALP3Device.KeypadAllUpButtonsEnum.KEYPAD_FRONT_UP) {
            this.mAllUpButton.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.12
                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    MainActivity.this.handleValveButtonEvent(motionEvent, (short) 4, (short) 1);
                    return true;
                }
            }));
        } else {
            this.mAllUpButton.setOnTouchListener(new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.13
                private Rect rect;

                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getActionMasked()) {
                        case 0:
                        case 5:
                            this.rect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                            short unused = MainActivity.mLongTouchPresetIndex = (short) 3;
                            MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                            MainActivity.this.longButtonTouchHandler.postDelayed(MainActivity.this.longButtonTouchRunnable, 2000L);
                            boolean unused2 = MainActivity.mLongTouchFired = false;
                            return false;
                        case 1:
                        case 3:
                        case 4:
                        case 6:
                            MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                            if (!MainActivity.mLongTouchFired) {
                                MainActivity.this.handlePresetButtonEvent((short) 3);
                            }
                            boolean unused3 = MainActivity.mLongTouchFired = false;
                            return true;
                        case 2:
                            if (!this.rect.contains(view.getLeft() + ((int) motionEvent.getX()), view.getTop() + ((int) motionEvent.getY()))) {
                                MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                            }
                        default:
                            return true;
                    }
                }
            });
        }
        this.mPresetUpButton.setOnTouchListener(new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.14
            private Rect rect;

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case 0:
                    case 5:
                        this.rect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                        short unused = MainActivity.mLongTouchPresetIndex = (short) 1;
                        MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                        MainActivity.this.longButtonTouchHandler.postDelayed(MainActivity.this.longButtonTouchRunnable, 2000L);
                        boolean unused2 = MainActivity.mLongTouchFired = false;
                        return false;
                    case 1:
                    case 3:
                    case 4:
                    case 6:
                        MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                        if (!MainActivity.mLongTouchFired) {
                            MainActivity.this.handlePresetButtonEvent((short) 1);
                        }
                        boolean unused3 = MainActivity.mLongTouchFired = false;
                        return true;
                    case 2:
                        if (!this.rect.contains(view.getLeft() + ((int) motionEvent.getX()), view.getTop() + ((int) motionEvent.getY()))) {
                            MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                        }
                    default:
                        return true;
                }
            }
        });
        this.mNormalRideButton.setOnTouchListener(new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.15
            private Rect rect;

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case 0:
                    case 5:
                        this.rect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                        short unused = MainActivity.mLongTouchPresetIndex = (short) 0;
                        MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                        MainActivity.this.longButtonTouchHandler.postDelayed(MainActivity.this.longButtonTouchRunnable, 2000L);
                        boolean unused2 = MainActivity.mLongTouchFired = false;
                        return false;
                    case 1:
                    case 3:
                    case 4:
                    case 6:
                        MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                        if (!MainActivity.mLongTouchFired) {
                            MainActivity.this.handlePresetButtonEvent((short) 0);
                        }
                        boolean unused3 = MainActivity.mLongTouchFired = false;
                        return true;
                    case 2:
                        if (!this.rect.contains(view.getLeft() + ((int) motionEvent.getX()), view.getTop() + ((int) motionEvent.getY()))) {
                            MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                        }
                    default:
                        return true;
                }
            }
        });
        this.mPresetDownButton.setOnTouchListener(new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.16
            private Rect rect;

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case 0:
                    case 5:
                        this.rect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                        short unused = MainActivity.mLongTouchPresetIndex = (short) 2;
                        MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                        MainActivity.this.longButtonTouchHandler.postDelayed(MainActivity.this.longButtonTouchRunnable, 2000L);
                        boolean unused2 = MainActivity.mLongTouchFired = false;
                        return false;
                    case 1:
                    case 3:
                    case 4:
                    case 6:
                        MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                        if (!MainActivity.mLongTouchFired) {
                            MainActivity.this.handlePresetButtonEvent((short) 2);
                        }
                        boolean unused3 = MainActivity.mLongTouchFired = false;
                        return true;
                    case 2:
                        if (!this.rect.contains(view.getLeft() + ((int) motionEvent.getX()), view.getTop() + ((int) motionEvent.getY()))) {
                            MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                        }
                    default:
                        return true;
                }
            }
        });
        if (this.mALP3Device.displaySettings.allDownButtonsEnum() == ALP3Device.KeypadAllDownButtonsEnum.KEYPAD_ALL_DOWN || this.mALP3Device.displaySettings.allDownButtonsEnum() == ALP3Device.KeypadAllDownButtonsEnum.KEYPAD_FRONT_DOWN) {
            this.mAllDownButton.setOnTouchListener(new RepeatListener(500, 50, new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.17
                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    MainActivity.this.handleValveButtonEvent(motionEvent, (short) 5, (short) 2);
                    return true;
                }
            }));
        } else {
            this.mAllDownButton.setOnTouchListener(new View.OnTouchListener() { // from class: com.airliftcompany.alp3.MainActivity.18
                private Rect rect;

                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getActionMasked()) {
                        case 0:
                        case 5:
                            this.rect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                            short unused = MainActivity.mLongTouchPresetIndex = (short) 4;
                            MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                            MainActivity.this.longButtonTouchHandler.postDelayed(MainActivity.this.longButtonTouchRunnable, 2000L);
                            boolean unused2 = MainActivity.mLongTouchFired = false;
                            return false;
                        case 1:
                        case 3:
                        case 4:
                        case 6:
                            MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                            if (!MainActivity.mLongTouchFired) {
                                MainActivity.this.handlePresetButtonEvent((short) 4);
                            }
                            boolean unused3 = MainActivity.mLongTouchFired = false;
                            return true;
                        case 2:
                            if (!this.rect.contains(view.getLeft() + ((int) motionEvent.getX()), view.getTop() + ((int) motionEvent.getY()))) {
                                MainActivity.this.longButtonTouchHandler.removeCallbacks(MainActivity.this.longButtonTouchRunnable);
                            }
                        default:
                            return true;
                    }
                }
            });
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        Log.i(TAG, "onActivityResult");
        if (i == 1 && i2 == 0) {
            mSkipDeviceScanActivity = true;
        }
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onBleInitialized() {
        Log.i(TAG, "onBleInitialized");
        runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.22
            @Override // java.lang.Runnable
            public void run() {
                if (MainActivity.this.mCommService != null && !MainActivity.this.mCommService.bleService.isAdapterOn()) {
                    MainActivity.this.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
                    return;
                }
                if (Util.isChinese() && !ALP3Preferences.authorized(MainActivity.this.getApplicationContext()).booleanValue()) {
                    ALP3Preferences.setDeviceAddress("", MainActivity.this.getApplicationContext());
                    MainActivity.this.mCommService.bleService.mBluetoothDeviceAddress = null;
                }
                MainActivity.this.reconnectIfPossible();
            }
        });
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onBleInitializationFailed() {
        Log.i(TAG, "onBleInitializationFailed");
        Toast.makeText(this, "Error initializing Bluetooth LE module", 1).show();
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onAuthorizationFailed() {
        Log.i(TAG, "onAuthorizationFailed");
        runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.23
            @Override // java.lang.Runnable
            public void run() {
                if (MainActivity.this.mDialog != null && MainActivity.this.mDialog.isShowing()) {
                    MainActivity.this.mDialog.dismiss();
                }
                boolean unused = MainActivity.mIgnoreOnPause = true;
                MainActivity.this.startActivityForResult(new Intent(MainActivity.this, (Class<?>) DeviceScanActivity.class), 1);
            }
        });
    }

    /* renamed from: com.airliftcompany.alp3.MainActivity$24 */
    class RunnableC035824 implements Runnable {
        RunnableC035824() {
        }

        @Override // java.lang.Runnable
        public void run() {
            MainActivity.this.mDialog.dismiss();
            if (MainActivity.this.mCommService.commStatus.DeviceAuthorized != CommService.DeviceAuthorizedEnum.DeviceIsInBootloader) {
                Toast.makeText(MainActivity.this, "Error connecting to manifold", 1).show();
                boolean unused = MainActivity.mIgnoreOnPause = true;
                MainActivity.this.startActivityForResult(new Intent(MainActivity.this, (Class<?>) DeviceScanActivity.class), 1);
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(MainActivity.this.getString(C0380R.string.detected_unit_without_firmware_program_firmware_now_an_internet_connection_is_required));
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.MainActivity.24.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.i(MainActivity.TAG, "Start firmware download");
                    new FirmwareVersionDownloadUtility().checkForFirmwareUpdate(MainActivity.this, MainActivity.this.mCommService, new FirmwareVersionDownloadUtility.DownloadFirmwareListener() { // from class: com.airliftcompany.alp3.MainActivity.24.1.1
                        @Override // com.airliftcompany.alp3.firmware.FirmwareVersionDownloadUtility.DownloadFirmwareListener
                        public void onTaskProgressUpdate(Integer num) {
                        }

                        @Override // com.airliftcompany.alp3.firmware.FirmwareVersionDownloadUtility.DownloadFirmwareListener
                        public void onTaskCompleted(Boolean bool, Boolean bool2, JSONObject jSONObject, Exception exc) {
                            if (!bool.booleanValue()) {
                                Log.i(MainActivity.TAG, "Failed firmware download");
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                                builder2.setTitle(MainActivity.this.getString(C0380R.string.Error));
                                builder2.setMessage(MainActivity.this.getString(C0380R.string.error_fetching_firmware_filename_from_internet));
                                builder2.setNeutralButton("OK", new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.MainActivity.24.1.1.1
                                    @Override // android.content.DialogInterface.OnClickListener
                                    public void onClick(DialogInterface dialogInterface2, int i2) {
                                        dialogInterface2.dismiss();
                                    }
                                });
                                builder2.create().show();
                                return;
                            }
                            Log.i(MainActivity.TAG, "Firmware download success");
                            boolean unused2 = MainActivity.mIgnoreOnPause = true;
                            Intent intent = new Intent(MainActivity.this, (Class<?>) FirmwareUpdateActivity.class);
                            intent.putExtra("firmwareJSON", jSONObject.toString());
                            MainActivity.this.startActivity(intent);
                        }
                    });
                    if (MainActivity.this.mDialog == null || !MainActivity.this.mDialog.isShowing()) {
                        return;
                    }
                    MainActivity.this.mDialog.dismiss();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.MainActivity.24.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onConnectionFailed() {
        Log.i(TAG, "onConnectionFailed");
        runOnUiThread(new RunnableC035824());
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onAuthorized() {
        Log.i(TAG, "onAuthorized");
        runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.25
            @Override // java.lang.Runnable
            public void run() {
                if (MainActivity.this.mDialog == null || !MainActivity.this.mDialog.isShowing()) {
                    return;
                }
                MainActivity.this.mDialog.dismiss();
            }
        });
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onStatusUpdated() {
        runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.26
            @Override // java.lang.Runnable
            public void run() {
                MainActivity.this.updateUI();
            }
        });
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onCalibrationUpdated() {
        runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.27
            @Override // java.lang.Runnable
            public void run() {
                Log.i(MainActivity.TAG, "onCalibrationUpdated");
                if (MainActivity.this.mALP3Device.calibrationSettings.needsCalibration) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(MainActivity.this.getString(C0380R.string.system_not_calibrated));
                    builder.setMessage(MainActivity.this.getString(C0380R.string.use_this_wizard_to_calibrate_your_system_do_you_wish_to_continue));
                    builder.setPositiveButton(C0380R.string.OK, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.MainActivity.27.2
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            boolean unused = MainActivity.mIgnoreOnPause = true;
                            MainActivity.this.startActivity(new Intent(MainActivity.this, (Class<?>) CalibrationQuestions.class));
                        }
                    }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.MainActivity.27.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog create = builder.create();
                    create.getWindow().setFlags(8, 8);
                    create.show();
                }
            }
        });
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onSettingsUpdated() {
        Log.i(TAG, "onSettingsUpdated");
        runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.28
            @Override // java.lang.Runnable
            public void run() {
                MainActivity.this.setPresetButtonHandlers();
            }
        });
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onVersionUpdated() {
        Log.i(TAG, "onVersionUpdated");
        if (this.mALP3Device.versionMismatch) {
            runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.29
                @Override // java.lang.Runnable
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(MainActivity.this.getString(C0380R.string.Error));
                    builder.setMessage(MainActivity.this.getString(C0380R.string.unit_firmware_is_incompatible_with_this_version_of_the_app_try_updating_the_app_firmware_or_both));
                    builder.setPositiveButton(C0380R.string.OK, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.MainActivity.29.2
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            MainActivity.this.checkForFirmwareUpdate();
                        }
                    }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.MainActivity.29.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog create = builder.create();
                    create.getWindow().setFlags(8, 8);
                    create.show();
                }
            });
        } else {
            checkForFirmwareUpdate();
        }
    }

    @Override // com.airliftcompany.alp3.comm.CommServiceListener
    public void onSerialUpdated() {
        if (Util.isChinese()) {
            CognitoService.getInstance().checkDeviceAuthorization(this.mCommService.alp3Device.Serial, this.mCommService.alp3Device.MacAddress, this, new CognitoService.CallbackInterface() { // from class: com.airliftcompany.alp3.MainActivity.30
                @Override // com.airliftcompany.alp3.utils.CognitoService.CallbackInterface
                public void completeCallback(final CognitoService.AuthResponse authResponse) {
                    MainActivity.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.30.1
                        @Override // java.lang.Runnable
                        public void run() {
                            int i = C037237.f32x9eda8419[authResponse.ordinal()];
                            if (i == 1) {
                                ALP3Preferences.setAuthorized(true, MainActivity.this.getApplicationContext());
                                MainActivity.this.CheckProductRegistration();
                            } else {
                                if (i != 2) {
                                    return;
                                }
                                ALP3Preferences.setAuthorized(false, MainActivity.this.getApplicationContext());
                                ALP3Preferences.setDeviceAddress("", MainActivity.this.getApplicationContext());
                                Util.displayAlert(MainActivity.this.getString(C0380R.string.device_has_not_been_authorized), MainActivity.this);
                                MainActivity.this.mCommService.bleService.mBluetoothDeviceAddress = null;
                                MainActivity.this.mCommService.disconnectBleSession();
                            }
                        }
                    });
                }
            });
        } else {
            CheckProductRegistration();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startMyOwnForeground() {
        NotificationChannel notificationChannel = new NotificationChannel(BuildConfig.APPLICATION_ID, "BLE Service", 0);
        notificationChannel.setLightColor(-16776961);
        notificationChannel.setLockscreenVisibility(0);
        ((NotificationManager) getSystemService(TransferService.INTENT_KEY_NOTIFICATION)).createNotificationChannel(notificationChannel);
        this.mCommService.startForeground(2, new NotificationCompat.Builder(this, BuildConfig.APPLICATION_ID).setOngoing(true).setSmallIcon(C0380R.mipmap.ic_launcher).setContentTitle("App is running in background").setPriority(1).setCategory(NotificationCompat.CATEGORY_SERVICE).build());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void CheckProductRegistration() {
        CommService commService = this.mCommService;
        if (commService == null || commService.alp3Device == null) {
            return;
        }
        runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.31
            @Override // java.lang.Runnable
            public void run() {
                if (MainActivity.this.mCommService.alp3Device.Serial.length() > 0) {
                    HashSet hashSet = new HashSet(ALP3Preferences.registeredDevices(MainActivity.this.getApplicationContext()));
                    if (hashSet.contains(MainActivity.this.mCommService.alp3Device.Serial)) {
                        return;
                    }
                    boolean unused = MainActivity.mIgnoreOnPause = true;
                    MainActivity.this.startActivity(new Intent(MainActivity.this, (Class<?>) Registration.class));
                    hashSet.add(MainActivity.this.mCommService.alp3Device.Serial);
                    ALP3Preferences.setRegisteredDevices(hashSet, MainActivity.this.getApplicationContext());
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkForFirmwareUpdate() {
        new FirmwareVersionDownloadUtility().checkForFirmwareUpdate(this, this.mCommService, new FirmwareVersionDownloadUtility.DownloadFirmwareListener() { // from class: com.airliftcompany.alp3.MainActivity.32
            @Override // com.airliftcompany.alp3.firmware.FirmwareVersionDownloadUtility.DownloadFirmwareListener
            public void onTaskProgressUpdate(Integer num) {
            }

            @Override // com.airliftcompany.alp3.firmware.FirmwareVersionDownloadUtility.DownloadFirmwareListener
            public void onTaskCompleted(Boolean bool, Boolean bool2, JSONObject jSONObject, Exception exc) {
                if (bool.booleanValue() && bool2.booleanValue()) {
                    boolean unused = MainActivity.mIgnoreOnPause = true;
                    Intent intent = new Intent(MainActivity.this, (Class<?>) UpdateAvailableActivity.class);
                    intent.putExtra("firmwareJSON", jSONObject.toString());
                    MainActivity.this.startActivity(intent);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reconnectIfPossible() {
        this.mCommService.bleService.connectionRetryAttempts = 0;
        if (ALP3Preferences.deviceAddress(getApplicationContext()).length() == 0) {
            mIgnoreOnPause = true;
            if (mSkipDeviceScanActivity) {
                mSkipDeviceScanActivity = false;
                return;
            } else {
                startActivityForResult(new Intent(this, (Class<?>) DeviceScanActivity.class), 1);
                return;
            }
        }
        ((RelativeLayout) findViewById(C0380R.id.displayLayout)).setEnabled(false);
        ((RelativeLayout) findViewById(C0380R.id.buttonLayout)).setEnabled(false);
        this.mDialog.setMessage(getString(C0380R.string.reconnecting));
        this.mDialog.setCancelable(false);
        this.mDialog.show();
        Log.i(TAG, "reconnectIfPossible");
        this.mCommService.bleService.startScanningForPairedDevice(ALP3Preferences.deviceAddress(getApplicationContext()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleValveButtonEvent(MotionEvent motionEvent, short s, short s2) {
        if (this.mCommService == null) {
            return;
        }
        String str = TAG;
        Log.i(str, "handleValveButtonEvent");
        if (this.mALP3Device.adjustingPresets.amSavingPreset) {
            if ((motionEvent.getActionMasked() == 0 || motionEvent.getActionMasked() == 5 || motionEvent.getActionMasked() == 2) && s < 4) {
                if (s2 == 2) {
                    this.mALP3Device.adjustingPresets.decrementPresetValue(s);
                    return;
                } else {
                    this.mALP3Device.adjustingPresets.incrementPresetValue(s);
                    return;
                }
            }
            return;
        }
        Log.i(str, "Event: " + MotionEvent.actionToString(motionEvent.getActionMasked()));
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    this.mCommService.openValve(s, (short) (s2 + 2), false);
                    return;
                } else if (actionMasked != 5) {
                    if (actionMasked != 6) {
                        Log.w(str, "Unhandled Event: " + MotionEvent.actionToString(motionEvent.getActionMasked()));
                        return;
                    }
                }
            }
            this.mCommService.closeValve(s);
            return;
        }
        this.mCommService.openValve(s, s2, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:16:0x006a  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x00df  */
    /* JADX WARN: Removed duplicated region for block: B:27:0x010a  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void handlePresetButtonEvent(short r8) {
        /*
            Method dump skipped, instructions count: 317
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airliftcompany.alp3.MainActivity.handlePresetButtonEvent(short):void");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePresetLongButtonEvent(short s) {
        CommService commService = this.mCommService;
        if (commService == null || commService.canpng65400UIStatus.controlStateEnum() == ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_SHOW_MODE) {
            return;
        }
        if (this.mALP3Device.calibrationSettings.needsCalibration) {
            Toast.makeText(this, "Error - Calibration not completed", 1).show();
            return;
        }
        this.cancelPresetHandler.removeCallbacks(this.cancelPreviewRunnable);
        if (s == 3) {
            int i = C037237.f28xbea6593f[this.mALP3Device.displaySettings.allUpButtonsEnum().ordinal()];
            if (i == 1 || i == 2) {
                return;
            }
        } else if (s == 4) {
            int i2 = C037237.f27xa7143ad8[this.mALP3Device.displaySettings.allDownButtonsEnum().ordinal()];
            if (i2 == 1 || i2 == 2) {
                return;
            }
            if (i2 == 4) {
                if (this.mCommService.alp3Device.displaySettings.PresetMode == 0) {
                    this.cancelPresetHandler.postDelayed(this.cancelPreviewRunnable, 5000L);
                    this.mCommService.recallPreset((short) 4);
                    return;
                }
                return;
            }
        }
        ((Vibrator) getSystemService("vibrator")).vibrate(50L);
        if (this.mALP3Device.adjustingPresets.amSavingPreset) {
            this.mALP3Device.adjustingPresets.amSavingPreset = false;
            this.lfPresetArrow.setVisibility(4);
            this.rfPresetArrow.setVisibility(4);
            this.lrPresetArrow.setVisibility(4);
            this.rrPresetArrow.setVisibility(4);
            this.lfTextView.setTextColor(getResources().getColor(C0380R.color.white));
            this.rfTextView.setTextColor(getResources().getColor(C0380R.color.white));
            this.lrTextView.setTextColor(getResources().getColor(C0380R.color.white));
            this.rrTextView.setTextColor(getResources().getColor(C0380R.color.white));
            this.cancelPreviewRunnable.run();
            new SavePresetAsyncTask(this, s).execute(new Void[0]);
            return;
        }
        this.mALP3Device.adjustingPresets.presetAdjusting = s;
        this.mALP3Device.adjustingPresets.amSavingPreset = true;
        if (this.mALP3Device.canpng65300ECUStatus.StateIsPressure == 1) {
            this.mALP3Device.adjustingPresets.Pressure[0] = this.mALP3Device.canpng65301ECUSpringPressure.Pressure[0];
            this.mALP3Device.adjustingPresets.Pressure[1] = this.mALP3Device.canpng65301ECUSpringPressure.Pressure[1];
            this.mALP3Device.adjustingPresets.Pressure[2] = this.mALP3Device.canpng65301ECUSpringPressure.Pressure[2];
            this.mALP3Device.adjustingPresets.Pressure[3] = this.mALP3Device.canpng65301ECUSpringPressure.Pressure[3];
            return;
        }
        this.mALP3Device.adjustingPresets.Height[0] = this.mALP3Device.canpng65302ECUSpringHeight.Height[0];
        this.mALP3Device.adjustingPresets.Height[1] = this.mALP3Device.canpng65302ECUSpringHeight.Height[1];
        this.mALP3Device.adjustingPresets.Height[2] = this.mALP3Device.canpng65302ECUSpringHeight.Height[2];
        this.mALP3Device.adjustingPresets.Height[3] = this.mALP3Device.canpng65302ECUSpringHeight.Height[3];
    }

    private class SavePresetAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        private Context context;
        private short presetIndex;
        ProgressDialog progressDialog;

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onCancelled(Boolean bool) {
        }

        public SavePresetAsyncTask(Context context, short s) {
            this.context = context;
            this.presetIndex = s;
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            ((RelativeLayout) MainActivity.this.findViewById(C0380R.id.displayLayout)).setEnabled(false);
            ((RelativeLayout) MainActivity.this.findViewById(C0380R.id.buttonLayout)).setEnabled(false);
            ProgressDialog progressDialog = new ProgressDialog(this.context);
            this.progressDialog = progressDialog;
            progressDialog.setMessage(MainActivity.this.getString(C0380R.string.Saving));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setProgressStyle(1);
            this.progressDialog.getWindow().setFlags(8, 8);
            this.progressDialog.show();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Boolean doInBackground(Void... voidArr) {
            short s = this.presetIndex;
            short s2 = s != 0 ? s != 1 ? s != 2 ? s != 3 ? s != 4 ? (short) 0 : (short) 17 : (short) 13 : (short) 9 : (short) 5 : (short) 1;
            MainActivity.this.mCommService.com100msState = CommService.com100msEnum.COM100mS_PAUSE;
            if (MainActivity.this.mALP3Device.canpng65300ECUStatus.pressureMode()) {
                int i = 0;
                int i2 = 0;
                while (i < 4) {
                    short s3 = (short) (s2 + 1);
                    if (!MainActivity.this.mCommService.txController.txWriteVariable((short) 8, s2, MainActivity.this.mALP3Device.adjustingPresets.Pressure[i])) {
                        return false;
                    }
                    i2 += 20;
                    publishProgress(Integer.valueOf(i2));
                    i++;
                    s2 = s3;
                }
                if (!MainActivity.this.mCommService.txController.txStartFlashWrite()) {
                    return false;
                }
                publishProgress(Integer.valueOf(i2 + 20));
            } else {
                int i3 = 0;
                int i4 = 0;
                while (i3 < 4) {
                    short s4 = (short) (s2 + 1);
                    if (!MainActivity.this.mCommService.txController.txWriteVariable((short) 6, s2, MainActivity.this.mALP3Device.adjustingPresets.Height[i3])) {
                        return false;
                    }
                    i4 += 20;
                    publishProgress(Integer.valueOf(i4));
                    i3++;
                    s2 = s4;
                }
                if (!MainActivity.this.mCommService.txController.txStartFlashWrite()) {
                    return false;
                }
                publishProgress(Integer.valueOf(i4 + 20));
            }
            MainActivity.this.mALP3Device.presets.Synced = false;
            MainActivity.this.mCommService.com100msState = CommService.com100msEnum.COM100mS_STATUS;
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
            if (bool.booleanValue()) {
                Toast.makeText(MainActivity.this, C0380R.string.Success, 1).show();
            } else {
                Toast.makeText(MainActivity.this, C0380R.string.error_communicating_with_manifold, 1).show();
            }
            ((RelativeLayout) MainActivity.this.findViewById(C0380R.id.displayLayout)).setEnabled(true);
            ((RelativeLayout) MainActivity.this.findViewById(C0380R.id.buttonLayout)).setEnabled(true);
            ProgressDialog progressDialog = this.progressDialog;
            if (progressDialog != null && progressDialog.isShowing()) {
                this.progressDialog.setProgressStyle(0);
                this.progressDialog.dismiss();
            }
            this.progressDialog = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:107:0x086f  */
    /* JADX WARN: Removed duplicated region for block: B:112:0x09d8  */
    /* JADX WARN: Removed duplicated region for block: B:157:0x02d9  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void updateUI() {
        /*
            Method dump skipped, instructions count: 3698
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airliftcompany.alp3.MainActivity.updateUI():void");
    }

    /* renamed from: com.airliftcompany.alp3.MainActivity$37 */
    static /* synthetic */ class C037237 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$comm$ALP3Device$KeypadAllDownButtonsEnum */
        static final /* synthetic */ int[] f27xa7143ad8;

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$comm$ALP3Device$KeypadAllUpButtonsEnum */
        static final /* synthetic */ int[] f28xbea6593f;

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$comm$ALP3Protocol$ECU_PrimaryControlStateEnum */
        static final /* synthetic */ int[] f29xc2ea46ec;

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$comm$ALP3Protocol$HeightMode_NormalModeStateEnum */
        static final /* synthetic */ int[] f30xcdfc9a56;

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$comm$ALP3Protocol$PressureMode_NormalModeStateEnum */
        static final /* synthetic */ int[] f31xc26348b4;

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$utils$CognitoService$AuthResponse */
        static final /* synthetic */ int[] f32x9eda8419;

        static {
            int[] iArr = new int[ALP3Protocol.PressureMode_NormalModeStateEnum.values().length];
            f31xc26348b4 = iArr;
            try {
                iArr[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_START.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_ACCUMALATE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_START_FAST_ADJUST.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_FAST_FILL.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_FAST_DUMP.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_FAST_COMPLETE.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_FAST_SETTLE.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_START_SLOW_ADJUST.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_SLOW_FILL.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_SLOW_DUMP.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_SLOW_COMPLETE.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_SLOW_SETTLE.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_AXLE_EQUAL_START.ordinal()] = 13;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_AXLE_EQUAL_FRONT.ordinal()] = 14;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_AXLE_EQUAL_FRONT_MINHEIGHT.ordinal()] = 15;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_AXLE_EQUAL_REAR.ordinal()] = 16;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_AXLE_EQUAL_REAR_MINHEIGHT.ordinal()] = 17;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_AXLE_EQUAL_COMPLETE.ordinal()] = 18;
            } catch (NoSuchFieldError unused18) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_AIROUT_START.ordinal()] = 19;
            } catch (NoSuchFieldError unused19) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_AIROUT_SETTLE.ordinal()] = 20;
            } catch (NoSuchFieldError unused20) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_ACCELERATING.ordinal()] = 21;
            } catch (NoSuchFieldError unused21) {
            }
            try {
                f31xc26348b4[ALP3Protocol.PressureMode_NormalModeStateEnum.SUB_PM_MODE_PRESET_MAINTAIN.ordinal()] = 22;
            } catch (NoSuchFieldError unused22) {
            }
            int[] iArr2 = new int[ALP3Protocol.HeightMode_NormalModeStateEnum.values().length];
            f30xcdfc9a56 = iArr2;
            try {
                iArr2[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_ACCUMALATE.ordinal()] = 1;
            } catch (NoSuchFieldError unused23) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_PRESET_NOT_MAINTAIN.ordinal()] = 2;
            } catch (NoSuchFieldError unused24) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_START_FAST_ADJUST.ordinal()] = 3;
            } catch (NoSuchFieldError unused25) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_FAST_FILL.ordinal()] = 4;
            } catch (NoSuchFieldError unused26) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_FAST_DUMP.ordinal()] = 5;
            } catch (NoSuchFieldError unused27) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_FAST_COMPLETE.ordinal()] = 6;
            } catch (NoSuchFieldError unused28) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_FAST_SETTLE.ordinal()] = 7;
            } catch (NoSuchFieldError unused29) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_FAST_ITERATION_SHORT_WAIT.ordinal()] = 8;
            } catch (NoSuchFieldError unused30) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_FAST_ITERATION_LONG_WAIT.ordinal()] = 9;
            } catch (NoSuchFieldError unused31) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_START_SLOW_ADJUST.ordinal()] = 10;
            } catch (NoSuchFieldError unused32) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_SLOW_FILL.ordinal()] = 11;
            } catch (NoSuchFieldError unused33) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_SLOW_DUMP.ordinal()] = 12;
            } catch (NoSuchFieldError unused34) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_SLOW_COMPLETE.ordinal()] = 13;
            } catch (NoSuchFieldError unused35) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_SLOW_SETTLE.ordinal()] = 14;
            } catch (NoSuchFieldError unused36) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_AXLE_EQUAL_START.ordinal()] = 15;
            } catch (NoSuchFieldError unused37) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_AXLE_EQUAL_FRONT.ordinal()] = 16;
            } catch (NoSuchFieldError unused38) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_AXLE_EQUAL_FRONT_MINHEIGHT.ordinal()] = 17;
            } catch (NoSuchFieldError unused39) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_AXLE_EQUAL_REAR.ordinal()] = 18;
            } catch (NoSuchFieldError unused40) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_AXLE_EQUAL_REAR_MINHEIGHT.ordinal()] = 19;
            } catch (NoSuchFieldError unused41) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_AXLE_EQUAL_COMPLETE.ordinal()] = 20;
            } catch (NoSuchFieldError unused42) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HM_MODE_AIROUT_START.ordinal()] = 21;
            } catch (NoSuchFieldError unused43) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HM_MODE_AIROUT_SETTLE.ordinal()] = 22;
            } catch (NoSuchFieldError unused44) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_ACCELERATING.ordinal()] = 23;
            } catch (NoSuchFieldError unused45) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_MODE_CHECK_ATTAINED.ordinal()] = 24;
            } catch (NoSuchFieldError unused46) {
            }
            try {
                f30xcdfc9a56[ALP3Protocol.HeightMode_NormalModeStateEnum.SUB_HT_START.ordinal()] = 25;
            } catch (NoSuchFieldError unused47) {
            }
            int[] iArr3 = new int[ALP3Protocol.ECU_PrimaryControlStateEnum.values().length];
            f29xc2ea46ec = iArr3;
            try {
                iArr3[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_PRESET_HEIGHT.ordinal()] = 1;
            } catch (NoSuchFieldError unused48) {
            }
            try {
                f29xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_PRESET_PRESSURE.ordinal()] = 2;
            } catch (NoSuchFieldError unused49) {
            }
            try {
                f29xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_MANUAL_CONTROL.ordinal()] = 3;
            } catch (NoSuchFieldError unused50) {
            }
            try {
                f29xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_LIMITS.ordinal()] = 4;
            } catch (NoSuchFieldError unused51) {
            }
            try {
                f29xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_PRESSURE.ordinal()] = 5;
            } catch (NoSuchFieldError unused52) {
            }
            try {
                f29xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_HEIGHT.ordinal()] = 6;
            } catch (NoSuchFieldError unused53) {
            }
            try {
                f29xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_MANUFACTURING_PRESSURE_CAL.ordinal()] = 7;
            } catch (NoSuchFieldError unused54) {
            }
            try {
                f29xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_ACC.ordinal()] = 8;
            } catch (NoSuchFieldError unused55) {
            }
            try {
                f29xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_INIT.ordinal()] = 9;
            } catch (NoSuchFieldError unused56) {
            }
            try {
                f29xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_HEIGHT.ordinal()] = 10;
            } catch (NoSuchFieldError unused57) {
            }
            try {
                f29xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_CALIBRATE_MANUAL_PRESSURE.ordinal()] = 11;
            } catch (NoSuchFieldError unused58) {
            }
            try {
                f29xc2ea46ec[ALP3Protocol.ECU_PrimaryControlStateEnum.OP_MODE_SHOW_MODE.ordinal()] = 12;
            } catch (NoSuchFieldError unused59) {
            }
            int[] iArr4 = new int[ALP3Device.KeypadAllDownButtonsEnum.values().length];
            f27xa7143ad8 = iArr4;
            try {
                iArr4[ALP3Device.KeypadAllDownButtonsEnum.KEYPAD_ALL_DOWN.ordinal()] = 1;
            } catch (NoSuchFieldError unused60) {
            }
            try {
                f27xa7143ad8[ALP3Device.KeypadAllDownButtonsEnum.KEYPAD_FRONT_DOWN.ordinal()] = 2;
            } catch (NoSuchFieldError unused61) {
            }
            try {
                f27xa7143ad8[ALP3Device.KeypadAllDownButtonsEnum.KEYPAD_ALL_DOWN_IS_PRESET.ordinal()] = 3;
            } catch (NoSuchFieldError unused62) {
            }
            try {
                f27xa7143ad8[ALP3Device.KeypadAllDownButtonsEnum.KEYPAD_AIR_OUT.ordinal()] = 4;
            } catch (NoSuchFieldError unused63) {
            }
            int[] iArr5 = new int[ALP3Device.KeypadAllUpButtonsEnum.values().length];
            f28xbea6593f = iArr5;
            try {
                iArr5[ALP3Device.KeypadAllUpButtonsEnum.KEYPAD_ALL_UP.ordinal()] = 1;
            } catch (NoSuchFieldError unused64) {
            }
            try {
                f28xbea6593f[ALP3Device.KeypadAllUpButtonsEnum.KEYPAD_FRONT_UP.ordinal()] = 2;
            } catch (NoSuchFieldError unused65) {
            }
            try {
                f28xbea6593f[ALP3Device.KeypadAllUpButtonsEnum.KEYPAD_ALL_UP_IS_PRESET.ordinal()] = 3;
            } catch (NoSuchFieldError unused66) {
            }
            int[] iArr6 = new int[CognitoService.AuthResponse.values().length];
            f32x9eda8419 = iArr6;
            try {
                iArr6[CognitoService.AuthResponse.AuthSuccess.ordinal()] = 1;
            } catch (NoSuchFieldError unused67) {
            }
            try {
                f32x9eda8419[CognitoService.AuthResponse.AccessDenied.ordinal()] = 2;
            } catch (NoSuchFieldError unused68) {
            }
            try {
                f32x9eda8419[CognitoService.AuthResponse.UnknownError.ordinal()] = 3;
            } catch (NoSuchFieldError unused69) {
            }
        }
    }

    private void loadFaults() {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 4; i++) {
            this.gFaultPressureDisplayYLevel[i] = false;
            this.gFaultPressureDisplayRLevel[i] = false;
            this.gFaultHeightDisplayYLevel[i] = false;
            this.gFaultHeightDisplayRLevel[i] = false;
        }
        this.gFaultTankYLevel = false;
        this.gFaultTankRLevel = false;
        String str = null;
        String str2 = null;
        for (int i2 = 0; i2 < 4; i2++) {
            if (this.mALP3Device.canpng65280ECUFault.HeightSensorLimit[i2] > 0) {
                str2 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.height_sensor_limit);
                this.gFaultPressureDisplayYLevel[i2] = true;
            }
        }
        if (str2 != null) {
            FaultInfo faultInfo = new FaultInfo();
            faultInfo.message = str2;
            faultInfo.isWarning = true;
            arrayList.add(faultInfo);
        }
        String str3 = null;
        for (int i3 = 0; i3 < 4; i3++) {
            if (this.mALP3Device.canpng65280ECUFault.HeightSensorNotPresent[i3] > 0) {
                str3 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.height_sensor_not_present);
                this.gFaultHeightDisplayRLevel[i3] = true;
            }
        }
        if (str3 != null) {
            FaultInfo faultInfo2 = new FaultInfo();
            faultInfo2.message = str3;
            faultInfo2.isError = true;
            arrayList.add(faultInfo2);
        }
        String str4 = null;
        for (int i4 = 0; i4 < 4; i4++) {
            if (this.mALP3Device.canpng65280ECUFault.PressureSensor[i4] > 0) {
                str4 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.pressure_sensor);
                this.gFaultPressureDisplayRLevel[i4] = true;
            }
        }
        if (this.mALP3Device.canpng65280ECUFault.PressureSensor[4] > 0) {
            str4 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.pressure_sensor);
            this.gFaultTankRLevel = true;
        }
        if (str4 != null) {
            FaultInfo faultInfo3 = new FaultInfo();
            faultInfo3.message = str4;
            faultInfo3.isError = true;
            arrayList.add(faultInfo3);
        }
        if (this.mALP3Device.canpng65280ECUFault.FactoryCal > 0) {
            String str5 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.factory_calibration);
            FaultInfo faultInfo4 = new FaultInfo();
            faultInfo4.message = str5;
            faultInfo4.isWarning = true;
            arrayList.add(faultInfo4);
        }
        if (this.mALP3Device.canpng65280ECUFault.HeightLimitCal > 0) {
            String str6 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.height_limit_calibration);
            FaultInfo faultInfo5 = new FaultInfo();
            faultInfo5.message = str6;
            faultInfo5.isWarning = true;
            arrayList.add(faultInfo5);
        }
        if (this.mALP3Device.canpng65280ECUFault.PressureCal > 0) {
            String str7 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.pressure_calibration);
            FaultInfo faultInfo6 = new FaultInfo();
            faultInfo6.message = str7;
            faultInfo6.isWarning = true;
            arrayList.add(faultInfo6);
        }
        if (this.mALP3Device.canpng65280ECUFault.HeightCal > 0) {
            String str8 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.height_calibration);
            FaultInfo faultInfo7 = new FaultInfo();
            faultInfo7.message = str8;
            faultInfo7.isWarning = true;
            arrayList.add(faultInfo7);
        }
        if (this.mALP3Device.canpng65280ECUFault.AccelCal > 0) {
            String str9 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.mount_calibration);
            FaultInfo faultInfo8 = new FaultInfo();
            faultInfo8.message = str9;
            faultInfo8.isWarning = true;
            arrayList.add(faultInfo8);
        }
        if (this.mALP3Device.canpng65280ECUFault.CompressorFreeze > 0) {
            String str10 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.compressor_freeze);
            FaultInfo faultInfo9 = new FaultInfo();
            faultInfo9.message = str10;
            faultInfo9.isWarning = true;
            this.gFaultTankYLevel = true;
            arrayList.add(faultInfo9);
        }
        if (this.mALP3Device.canpng65280ECUFault.CompressorOverrun > 0) {
            String str11 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.compressor_overrun);
            FaultInfo faultInfo10 = new FaultInfo();
            faultInfo10.message = str11;
            faultInfo10.isError = true;
            this.gFaultTankRLevel = true;
            arrayList.add(faultInfo10);
        }
        if (this.mALP3Device.canpng65280ECUFault.TankPressureLow > 0) {
            String str12 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.tank_too_low);
            FaultInfo faultInfo11 = new FaultInfo();
            faultInfo11.message = str12;
            faultInfo11.isWarning = true;
            this.gFaultTankYLevel = true;
            arrayList.add(faultInfo11);
        }
        if (this.mALP3Device.canpng65280ECUFault.InvaildMount > 0) {
            String str13 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.invalid_mount);
            FaultInfo faultInfo12 = new FaultInfo();
            faultInfo12.message = str13;
            faultInfo12.isWarning = true;
            arrayList.add(faultInfo12);
        }
        if (this.mALP3Device.canpng65280ECUFault.HighVoltage > 0) {
            String str14 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.voltage_high);
            FaultInfo faultInfo13 = new FaultInfo();
            faultInfo13.message = str14;
            faultInfo13.isWarning = true;
            arrayList.add(faultInfo13);
        }
        if (this.mALP3Device.canpng65280ECUFault.LowVoltage > 0) {
            String str15 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.voltage_low);
            FaultInfo faultInfo14 = new FaultInfo();
            faultInfo14.message = str15;
            faultInfo14.isWarning = true;
            arrayList.add(faultInfo14);
        }
        if (this.mALP3Device.canpng65280ECUFault.ECUoverTemp > 0) {
            String str16 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.manifold_over_temperature);
            FaultInfo faultInfo15 = new FaultInfo();
            faultInfo15.message = str16;
            faultInfo15.isWarning = true;
            arrayList.add(faultInfo15);
        }
        String str17 = null;
        for (int i5 = 0; i5 < 5; i5++) {
            if (this.mALP3Device.canpng65280ECUFault.Valve[i5] > 0) {
                str17 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.Valve);
                this.gFaultPressureDisplayRLevel[i5] = true;
            }
        }
        if (str17 != null) {
            FaultInfo faultInfo16 = new FaultInfo();
            faultInfo16.message = str17;
            faultInfo16.isError = true;
            arrayList.add(faultInfo16);
        }
        String str18 = null;
        for (int i6 = 0; i6 < 4; i6++) {
            if (this.mALP3Device.canpng65280ECUFault.HeightSensorRange[i6] > 0) {
                str18 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.height_range);
                this.gFaultHeightDisplayYLevel[i6] = true;
            }
        }
        if (this.mALP3Device.canpng65280ECUFault.VersionFault > 0) {
            str18 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.incompatible_revision);
            FaultInfo faultInfo17 = new FaultInfo();
            faultInfo17.message = str18;
            faultInfo17.isWarning = true;
            arrayList.add(faultInfo17);
        }
        if (str18 != null) {
            FaultInfo faultInfo18 = new FaultInfo();
            faultInfo18.message = str18;
            faultInfo18.isWarning = true;
            arrayList.add(faultInfo18);
        }
        for (int i7 = 0; i7 < 4; i7++) {
            if (this.mALP3Device.canpng65280ECUFault.LeakDetect[i7] > 0) {
                str = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.leak_detected);
                this.gFaultPressureDisplayYLevel[i7] = true;
            }
        }
        if (str != null) {
            FaultInfo faultInfo19 = new FaultInfo();
            faultInfo19.message = str;
            faultInfo19.isWarning = true;
            arrayList.add(faultInfo19);
        }
        if (this.mALP3Device.canpng65280ECUFault.MinHeight > 0) {
            String str19 = getString(C0380R.string.Fault) + ": " + getString(C0380R.string.min_height_reached);
            FaultInfo faultInfo20 = new FaultInfo();
            faultInfo20.message = str19;
            faultInfo20.isWarning = true;
            arrayList.add(faultInfo20);
        }
        if (this.mFaultArrayList.size() == 0 && arrayList.size() != 0) {
            this.faultTimerTask.run();
        } else if (arrayList.size() == 0 && this.mFaultArrayList.size() != 0) {
            this.faultTimerTask.run();
        }
        this.mFaultArrayList = new ArrayList<>(arrayList);
    }

    private class FaultTimerTask extends TimerTask {
        private FaultTimerTask() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.MainActivity.FaultTimerTask.1
                @Override // java.lang.Runnable
                public void run() {
                    if (MainActivity.this.mCommService == null) {
                        return;
                    }
                    if (MainActivity.this.mALP3Device.adjustingPresets.amSavingPreset || MainActivity.this.mFaultArrayList.size() == 0) {
                        if (!MainActivity.this.mALP3Device.adjustingPresets.amSavingPreset) {
                            MainActivity.this.faultTextView.setText("");
                            MainActivity.this.faultLayout.setVisibility(4);
                            MainActivity.this.lfTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.white));
                            MainActivity.this.rfTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.white));
                            MainActivity.this.lrTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.white));
                            MainActivity.this.rrTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.white));
                            MainActivity.this.lfArrow.setImageResource(C0380R.drawable.left_arrow);
                            MainActivity.this.rfArrow.setImageResource(C0380R.drawable.right_arrow);
                            MainActivity.this.lrArrow.setImageResource(C0380R.drawable.left_arrow);
                            MainActivity.this.rrArrow.setImageResource(C0380R.drawable.right_arrow);
                        }
                        MainActivity.this.tankTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.white));
                        MainActivity.this.faultImageView.setImageResource(android.R.color.transparent);
                        return;
                    }
                    if (MainActivity.this.mFaultIndex >= MainActivity.this.mFaultArrayList.size()) {
                        MainActivity.this.mFaultIndex = 0;
                    }
                    MainActivity.this.faultLayout.setVisibility(0);
                    FaultInfo faultInfo = (FaultInfo) MainActivity.this.mFaultArrayList.get(MainActivity.this.mFaultIndex);
                    MainActivity.this.faultTextView.setText(faultInfo.message);
                    if (faultInfo.isError) {
                        MainActivity.this.faultImageView.setVisibility(0);
                        MainActivity.this.faultImageView.setImageResource(C0380R.drawable.red_warning);
                    } else if (faultInfo.isWarning) {
                        MainActivity.this.faultImageView.setVisibility(0);
                        MainActivity.this.faultImageView.setImageResource(C0380R.drawable.yellow_warning);
                    } else {
                        MainActivity.this.faultImageView.setVisibility(4);
                    }
                    if (MainActivity.this.gFaultTankRLevel) {
                        MainActivity.this.tankTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.red));
                    } else if (MainActivity.this.gFaultTankYLevel) {
                        MainActivity.this.tankTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.yellow));
                    } else {
                        MainActivity.this.tankTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.white));
                    }
                    if (MainActivity.this.gFaultPressureDisplayRLevel[0]) {
                        MainActivity.this.lfTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.red));
                    } else if (MainActivity.this.gFaultPressureDisplayYLevel[0]) {
                        MainActivity.this.lfTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.yellow));
                    } else {
                        MainActivity.this.lfTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.white));
                    }
                    if (MainActivity.this.gFaultPressureDisplayRLevel[1]) {
                        MainActivity.this.rfTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.red));
                    } else if (MainActivity.this.gFaultPressureDisplayYLevel[1]) {
                        MainActivity.this.rfTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.yellow));
                    } else {
                        MainActivity.this.rfTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.white));
                    }
                    if (MainActivity.this.gFaultPressureDisplayRLevel[2]) {
                        MainActivity.this.lrTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.red));
                    } else if (MainActivity.this.gFaultPressureDisplayYLevel[2]) {
                        MainActivity.this.lrTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.yellow));
                    } else {
                        MainActivity.this.lrTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.white));
                    }
                    if (MainActivity.this.gFaultPressureDisplayRLevel[3]) {
                        MainActivity.this.rrTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.red));
                    } else if (MainActivity.this.gFaultPressureDisplayYLevel[3]) {
                        MainActivity.this.rrTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.yellow));
                    } else {
                        MainActivity.this.rrTextView.setTextColor(MainActivity.this.getResources().getColor(C0380R.color.white));
                    }
                    if (MainActivity.this.gFaultHeightDisplayRLevel[0]) {
                        MainActivity.this.lfArrow.setImageResource(C0380R.drawable.red_arrow_l);
                    } else if (MainActivity.this.gFaultHeightDisplayYLevel[0]) {
                        MainActivity.this.lfArrow.setImageResource(C0380R.drawable.yellow_arrow_l);
                    } else {
                        MainActivity.this.lfArrow.setImageResource(C0380R.drawable.left_arrow);
                    }
                    if (MainActivity.this.gFaultHeightDisplayRLevel[1]) {
                        MainActivity.this.rfArrow.setImageResource(C0380R.drawable.red_arrow);
                    } else if (MainActivity.this.gFaultHeightDisplayYLevel[1]) {
                        MainActivity.this.rfArrow.setImageResource(C0380R.drawable.yellow_arrow);
                    } else {
                        MainActivity.this.rfArrow.setImageResource(C0380R.drawable.right_arrow);
                    }
                    if (MainActivity.this.gFaultHeightDisplayRLevel[2]) {
                        MainActivity.this.lrArrow.setImageResource(C0380R.drawable.red_arrow_l);
                    } else if (MainActivity.this.gFaultHeightDisplayYLevel[2]) {
                        MainActivity.this.lrArrow.setImageResource(C0380R.drawable.yellow_arrow_l);
                    } else {
                        MainActivity.this.lrArrow.setImageResource(C0380R.drawable.left_arrow);
                    }
                    if (MainActivity.this.gFaultHeightDisplayRLevel[2]) {
                        MainActivity.this.rrArrow.setImageResource(C0380R.drawable.red_arrow);
                    } else if (MainActivity.this.gFaultHeightDisplayYLevel[2]) {
                        MainActivity.this.rrArrow.setImageResource(C0380R.drawable.yellow_arrow);
                    } else {
                        MainActivity.this.rrArrow.setImageResource(C0380R.drawable.right_arrow);
                    }
                    MainActivity.access$4308(MainActivity.this);
                }
            });
        }
    }
}
