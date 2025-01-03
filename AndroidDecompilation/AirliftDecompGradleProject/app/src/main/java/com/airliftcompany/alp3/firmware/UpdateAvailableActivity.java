package com.airliftcompany.alp3.firmware;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import com.airliftcompany.alp3.C0380R;
import com.airliftcompany.alp3.comm.CommService;
import com.amazonaws.services.s3.util.Mimetypes;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class UpdateAvailableActivity extends Activity {
    private static final String TAG = "UpdateAvailableActivity";
    private JSONObject firmwareJSON;
    private CommService mCommService;
    private Boolean androidUpdate = false;
    private Boolean mandatoryUpdate = false;
    private final ServiceConnection mServiceConnection = new ServiceConnection() { // from class: com.airliftcompany.alp3.firmware.UpdateAvailableActivity.3
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            UpdateAvailableActivity.this.mCommService = ((CommService.LocalBinder) iBinder).getService();
            UpdateAvailableActivity.this.loadUI();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            UpdateAvailableActivity.this.mCommService = null;
        }
    };

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0380R.layout.activity_update_available);
        getWindow().addFlags(128);
        ((Button) findViewById(C0380R.id.cancelButton)).setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.firmware.UpdateAvailableActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                UpdateAvailableActivity.this.finish();
            }
        });
        ((Button) findViewById(C0380R.id.updateButton)).setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.firmware.UpdateAvailableActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (UpdateAvailableActivity.this.androidUpdate.booleanValue()) {
                    String packageName = UpdateAvailableActivity.this.getPackageName();
                    try {
                        UpdateAvailableActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + packageName)));
                        return;
                    } catch (ActivityNotFoundException unused) {
                        UpdateAvailableActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                        return;
                    }
                }
                Intent intent = new Intent(UpdateAvailableActivity.this.getApplicationContext(), (Class<?>) FirmwareUpdateActivity.class);
                intent.putExtra("firmwareJSON", UpdateAvailableActivity.this.firmwareJSON.toString());
                UpdateAvailableActivity.this.finish();
                UpdateAvailableActivity.this.startActivity(intent);
            }
        });
        bindService(new Intent(this, (Class<?>) CommService.class), this.mServiceConnection, 1);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this.mServiceConnection);
        this.mCommService = null;
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        if (this.mandatoryUpdate.booleanValue()) {
            return;
        }
        super.onBackPressed();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void loadUI() {
        this.mandatoryUpdate = false;
        TextView textView = (TextView) findViewById(C0380R.id.titleTextView);
        WebView webView = (WebView) findViewById(C0380R.id.releaseNotesWebView);
        Button button = (Button) findViewById(C0380R.id.cancelButton);
        TextView textView2 = (TextView) findViewById(C0380R.id.manifoldVersionTextView);
        TextView textView3 = (TextView) findViewById(C0380R.id.displayVersionTextView);
        webView.setBackgroundColor(0);
        try {
            JSONObject jSONObject = new JSONObject(getIntent().getStringExtra("firmwareJSON"));
            this.firmwareJSON = jSONObject;
            JSONObject jSONObject2 = jSONObject.getJSONObject(Constants.androidVersionDictKey);
            Log.i(TAG, jSONObject2.toString());
            try {
                String str = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
                String string = jSONObject2.getString(Constants.versionKey);
                String string2 = jSONObject2.getString(Constants.minVersionKey);
                String string3 = this.firmwareJSON.getString(Constants.minManifoldVersionKey);
                String string4 = this.firmwareJSON.getString(Constants.minDisplayVersionKey);
                String language = Locale.getDefault().getLanguage();
                String str2 = "";
                if (string.length() > 0 && Util.versionCompare(string, str).intValue() > 0) {
                    this.androidUpdate = true;
                    try {
                        JSONArray jSONArray = jSONObject2.getJSONArray(Constants.languagesKey);
                        for (int i = 0; i < jSONArray.length(); i++) {
                            JSONObject jSONObject3 = jSONArray.getJSONObject(i);
                            if (jSONObject3.getString(Constants.languageKey).equalsIgnoreCase(language)) {
                                str2 = jSONObject3.getString(Constants.releaseNotesKey);
                            }
                        }
                        if (str2.length() == 0) {
                            for (int i2 = 0; i2 < jSONArray.length(); i2++) {
                                JSONObject jSONObject4 = jSONArray.getJSONObject(i2);
                                if (jSONObject4.getString(Constants.languageKey).equalsIgnoreCase("en")) {
                                    str2 = jSONObject4.getString(Constants.releaseNotesKey);
                                }
                            }
                        }
                    } catch (JSONException unused) {
                    }
                    webView.loadData("<font face='HelveticaNeue-Light' size='2'>" + str2 + "</font>", Mimetypes.MIMETYPE_HTML, "UTF-8");
                    if (string2.length() > 0 && Util.versionCompare(string2, str).intValue() > 0) {
                        this.mandatoryUpdate = true;
                    }
                    if (this.mandatoryUpdate.booleanValue()) {
                        textView.setText(C0380R.string.mandatory_android_update);
                        button.setVisibility(4);
                    } else {
                        textView.setText(C0380R.string.android_update_available);
                    }
                } else {
                    try {
                        JSONArray jSONArray2 = this.firmwareJSON.getJSONArray(Constants.languagesKey);
                        for (int i3 = 0; i3 < jSONArray2.length(); i3++) {
                            JSONObject jSONObject5 = jSONArray2.getJSONObject(i3);
                            if (jSONObject5.getString(Constants.languageKey).equalsIgnoreCase(language)) {
                                str2 = jSONObject5.getString(Constants.releaseNotesKey);
                            }
                        }
                        if (str2.length() == 0) {
                            for (int i4 = 0; i4 < jSONArray2.length(); i4++) {
                                JSONObject jSONObject6 = jSONArray2.getJSONObject(i4);
                                if (jSONObject6.getString(Constants.languageKey).equalsIgnoreCase("en")) {
                                    str2 = jSONObject6.getString(Constants.releaseNotesKey);
                                }
                            }
                        }
                    } catch (JSONException unused2) {
                    }
                    webView.loadData("<font face='HelveticaNeue-Light' size='2'>" + str2 + "</font>", Mimetypes.MIMETYPE_HTML, "UTF-8");
                    if (this.mCommService.alp3Device.versionChecked) {
                        if (this.mCommService.alp3Device.ECUVersion.length() == 0 && this.mCommService.commStatus.BlueToothAccConnected) {
                            this.mandatoryUpdate = true;
                        } else if (string3.length() > 0 && this.mCommService.alp3Device.ECUVersion.length() > 0 && Util.versionCompare(string3, this.mCommService.alp3Device.ECUVersion).intValue() > 0) {
                            this.mandatoryUpdate = true;
                        } else if (this.mCommService.alp3Device.DisplayVersion.length() > 0 && !this.mCommService.alp3Device.DisplayVersion.equalsIgnoreCase("0.0.0") && string4.length() > 0 && Util.versionCompare(string4, this.mCommService.alp3Device.DisplayVersion).intValue() > 0) {
                            this.mandatoryUpdate = true;
                        }
                    }
                    if (this.mandatoryUpdate.booleanValue()) {
                        textView.setText(getString(C0380R.string.mandatory_update_available));
                        button.setVisibility(4);
                    } else {
                        textView.setText(getString(C0380R.string.firmware_update_available));
                    }
                }
                textView2.setText(this.firmwareJSON.getString(Constants.manifoldVersionKey));
                textView3.setText(this.firmwareJSON.getString(Constants.displayVersionKey));
            } catch (PackageManager.NameNotFoundException e) {
                handleException(e);
            }
        } catch (JSONException e2) {
            handleException(e2);
        }
    }

    private void handleException(Exception exc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(C0380R.string.Error));
        builder.setMessage(exc.getMessage());
        builder.setNeutralButton(getString(C0380R.string.OK), new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.firmware.UpdateAvailableActivity.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                UpdateAvailableActivity.this.finish();
            }
        });
        builder.create().show();
    }
}
