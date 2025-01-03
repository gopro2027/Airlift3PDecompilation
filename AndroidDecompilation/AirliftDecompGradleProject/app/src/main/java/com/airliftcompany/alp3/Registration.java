package com.airliftcompany.alp3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.app.NavUtils;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/* loaded from: classes.dex */
public class Registration extends Activity {
    private static final int FILECHOOSER_RESULTCODE = 1;
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final String TAG = "Registration";
    private String mCameraPhotoPath;
    private Uri mCapturedImageURI = null;
    private ProgressDialog mDialog;
    private ValueCallback<Uri[]> mFilePathCallback;
    private ValueCallback<Uri> mUploadMessage;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(C0380R.layout.activity_registration);
        if (Build.VERSION.SDK_INT >= 23) {
            int checkSelfPermission = checkSelfPermission("android.permission.CAMERA");
            ArrayList arrayList = new ArrayList();
            if (checkSelfPermission != 0) {
                arrayList.add("android.permission.CAMERA");
            }
            if (!arrayList.isEmpty()) {
                requestPermissions((String[]) arrayList.toArray(new String[arrayList.size()]), 114);
            }
        }
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.mDialog = progressDialog;
        progressDialog.getWindow().setFlags(8, 8);
        WebView webView = (WebView) findViewById(C0380R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setCacheMode(2);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setUserAgentString("Android Mozilla/5.0 AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().supportZoom();
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() { // from class: com.airliftcompany.alp3.Registration.1
            @Override // android.webkit.WebChromeClient
            public void onPermissionRequest(final PermissionRequest permissionRequest) {
                Log.d(Registration.TAG, "onPermissionRequest");
                Registration.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.Registration.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        Log.d(Registration.TAG, permissionRequest.getOrigin().toString());
                        PermissionRequest permissionRequest2 = permissionRequest;
                        permissionRequest2.grant(permissionRequest2.getResources());
                    }
                });
            }

            /* JADX WARN: Removed duplicated region for block: B:13:0x0091  */
            /* JADX WARN: Removed duplicated region for block: B:20:0x004b  */
            /* JADX WARN: Removed duplicated region for block: B:9:0x008c  */
            @Override // android.webkit.WebChromeClient
            /*
                Code decompiled incorrectly, please refer to instructions dump.
                To view partially-correct code enable 'Show inconsistent code' option in preferences
            */
            public boolean onShowFileChooser(android.webkit.WebView r4, android.webkit.ValueCallback<android.net.Uri[]> r5, android.webkit.WebChromeClient.FileChooserParams r6) {
                /*
                    r3 = this;
                    com.airliftcompany.alp3.Registration r4 = com.airliftcompany.alp3.Registration.this
                    android.webkit.ValueCallback r4 = com.airliftcompany.alp3.Registration.access$100(r4)
                    r6 = 0
                    if (r4 == 0) goto L12
                    com.airliftcompany.alp3.Registration r4 = com.airliftcompany.alp3.Registration.this
                    android.webkit.ValueCallback r4 = com.airliftcompany.alp3.Registration.access$100(r4)
                    r4.onReceiveValue(r6)
                L12:
                    com.airliftcompany.alp3.Registration r4 = com.airliftcompany.alp3.Registration.this
                    com.airliftcompany.alp3.Registration.access$102(r4, r5)
                    android.content.Intent r4 = new android.content.Intent
                    java.lang.String r5 = "android.media.action.IMAGE_CAPTURE"
                    r4.<init>(r5)
                    com.airliftcompany.alp3.Registration r5 = com.airliftcompany.alp3.Registration.this
                    android.content.pm.PackageManager r5 = r5.getPackageManager()
                    android.content.ComponentName r5 = r4.resolveActivity(r5)
                    if (r5 == 0) goto L76
                    com.airliftcompany.alp3.Registration r5 = com.airliftcompany.alp3.Registration.this     // Catch: java.io.IOException -> L3e
                    java.io.File r5 = com.airliftcompany.alp3.Registration.access$200(r5)     // Catch: java.io.IOException -> L3e
                    java.lang.String r0 = "PhotoPath"
                    com.airliftcompany.alp3.Registration r1 = com.airliftcompany.alp3.Registration.this     // Catch: java.io.IOException -> L3c
                    java.lang.String r1 = com.airliftcompany.alp3.Registration.access$300(r1)     // Catch: java.io.IOException -> L3c
                    r4.putExtra(r0, r1)     // Catch: java.io.IOException -> L3c
                    goto L49
                L3c:
                    r0 = move-exception
                    goto L40
                L3e:
                    r0 = move-exception
                    r5 = r6
                L40:
                    java.lang.String r1 = com.airliftcompany.alp3.Registration.access$000()
                    java.lang.String r2 = "Unable to create Image File"
                    android.util.Log.e(r1, r2, r0)
                L49:
                    if (r5 == 0) goto L77
                    com.airliftcompany.alp3.Registration r6 = com.airliftcompany.alp3.Registration.this
                    java.lang.StringBuilder r0 = new java.lang.StringBuilder
                    r0.<init>()
                    java.lang.String r1 = "file:"
                    r0.append(r1)
                    java.lang.String r1 = r5.getAbsolutePath()
                    r0.append(r1)
                    java.lang.String r0 = r0.toString()
                    com.airliftcompany.alp3.Registration.access$302(r6, r0)
                    com.airliftcompany.alp3.Registration r6 = com.airliftcompany.alp3.Registration.this
                    android.content.Context r6 = r6.getApplicationContext()
                    java.lang.String r0 = "com.airliftcompany.alp3.fileprovider"
                    android.net.Uri r5 = androidx.core.content.FileProvider.getUriForFile(r6, r0, r5)
                    java.lang.String r6 = "output"
                    r4.putExtra(r6, r5)
                L76:
                    r6 = r4
                L77:
                    android.content.Intent r4 = new android.content.Intent
                    java.lang.String r5 = "android.intent.action.GET_CONTENT"
                    r4.<init>(r5)
                    java.lang.String r5 = "android.intent.category.OPENABLE"
                    r4.addCategory(r5)
                    java.lang.String r5 = "image/*"
                    r4.setType(r5)
                    r5 = 0
                    r0 = 1
                    if (r6 == 0) goto L91
                    android.content.Intent[] r1 = new android.content.Intent[r0]
                    r1[r5] = r6
                    goto L93
                L91:
                    android.content.Intent[] r1 = new android.content.Intent[r5]
                L93:
                    android.content.Intent r5 = new android.content.Intent
                    java.lang.String r6 = "android.intent.action.CHOOSER"
                    r5.<init>(r6)
                    java.lang.String r6 = "android.intent.extra.INTENT"
                    r5.putExtra(r6, r4)
                    java.lang.String r4 = "android.intent.extra.TITLE"
                    java.lang.String r6 = "Image Chooser"
                    r5.putExtra(r4, r6)
                    java.lang.String r4 = "android.intent.extra.INITIAL_INTENTS"
                    r5.putExtra(r4, r1)
                    com.airliftcompany.alp3.Registration r4 = com.airliftcompany.alp3.Registration.this
                    r4.startActivityForResult(r5, r0)
                    return r0
                */
                throw new UnsupportedOperationException("Method not decompiled: com.airliftcompany.alp3.Registration.C03811.onShowFileChooser(android.webkit.WebView, android.webkit.ValueCallback, android.webkit.WebChromeClient$FileChooserParams):boolean");
            }

            public void openFileChooser(ValueCallback<Uri> valueCallback, String str) {
                Registration.this.mUploadMessage = valueCallback;
                File file = new File(Registration.this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "AndroidExampleFolder");
                if (!file.exists()) {
                    file.mkdirs();
                }
                Registration.this.mCapturedImageURI = Uri.fromFile(new File(file + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra("output", Registration.this.mCapturedImageURI);
                Intent intent2 = new Intent("android.intent.action.GET_CONTENT");
                intent2.addCategory("android.intent.category.OPENABLE");
                intent2.setType("image/*");
                Intent createChooser = Intent.createChooser(intent2, "Image Chooser");
                createChooser.putExtra("android.intent.extra.INITIAL_INTENTS", new Parcelable[]{intent});
                Registration.this.startActivityForResult(createChooser, 1);
            }

            public void openFileChooser(ValueCallback<Uri> valueCallback) {
                openFileChooser(valueCallback, "");
            }

            public void openFileChooser(ValueCallback<Uri> valueCallback, String str, String str2) {
                openFileChooser(valueCallback, str);
            }

            @Override // android.webkit.WebChromeClient
            public boolean onJsAlert(WebView webView2, String str, String str2, JsResult jsResult) {
                Log.d("LogTag", str2);
                jsResult.confirm();
                return true;
            }
        });
        webView.loadUrl("https://www.airliftperformance.com/support/warranty-registration/");
        ((Button) findViewById(C0380R.id.done_button)).setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.Registration.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Registration.this.finish();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public File createImageFile() throws IOException {
        return File.createTempFile("JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_", ".jpg", getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES));
    }

    @Override // android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        ValueCallback<Uri> valueCallback;
        Uri data;
        Uri[] uriArr;
        if (Build.VERSION.SDK_INT >= 21) {
            if (i != 1 || this.mFilePathCallback == null) {
                super.onActivityResult(i, i2, intent);
                return;
            }
            if (i2 == -1) {
                if (intent == null) {
                    String str = this.mCameraPhotoPath;
                    if (str != null) {
                        uriArr = new Uri[]{Uri.parse(str)};
                    }
                    uriArr = null;
                } else {
                    String dataString = intent.getDataString();
                    if (dataString != null) {
                        uriArr = new Uri[]{Uri.parse(dataString)};
                    } else {
                        String str2 = this.mCameraPhotoPath;
                        if (str2 != null) {
                            uriArr = new Uri[]{Uri.parse(str2)};
                        }
                        uriArr = null;
                    }
                }
                this.mFilePathCallback.onReceiveValue(uriArr);
                this.mFilePathCallback = null;
                return;
            }
            return;
        }
        if (Build.VERSION.SDK_INT <= 19) {
            if (i != 1 || (valueCallback = this.mUploadMessage) == null) {
                super.onActivityResult(i, i2, intent);
                return;
            }
            if (i != 1 || valueCallback == null) {
                return;
            }
            if (i2 == -1) {
                try {
                    data = intent == null ? this.mCapturedImageURI : intent.getData();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "activity :" + e, 1).show();
                }
                this.mUploadMessage.onReceiveValue(data);
                this.mUploadMessage = null;
            }
            data = null;
            this.mUploadMessage.onReceiveValue(data);
            this.mUploadMessage = null;
        }
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        ProgressDialog progressDialog = this.mDialog;
        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }
        this.mDialog.dismiss();
    }
}
