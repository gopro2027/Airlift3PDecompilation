package com.airliftcompany.alp3.dealer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.app.NavUtils;
import com.airliftcompany.alp3.C0380R;
import com.airliftcompany.alp3.utils.CognitoService;
import com.airliftcompany.alp3.utils.Util;
import com.amazonaws.mobileconnectors.cognitoauth.util.ClientConstants;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.google.gson.Gson;
import java.util.Date;
import java.util.HashMap;

/* loaded from: classes.dex */
public class EditDevice extends Activity {
    private boolean authorized;
    private Date authorizedDate;
    private ProgressDialog mDialog;
    private String macAddress;
    private String serial;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().setTitle(C0380R.string.device_settings);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(C0380R.layout.activity_edit_device);
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.mDialog = progressDialog;
        progressDialog.getWindow().setFlags(8, 8);
        this.macAddress = getIntent().getExtras().getString("macAddress", "");
        this.serial = getIntent().getExtras().getString("serial", "");
        this.authorized = getIntent().getExtras().getBoolean("authorized", false);
        this.authorizedDate = Util.dateForServerString(getIntent().getExtras().getString("authorizedDateString", ""));
        ((TextView) findViewById(C0380R.id.serialTextView)).setText(this.serial);
        TextView textView = (TextView) findViewById(C0380R.id.authorizedTextView);
        textView.setText(getString(this.authorized ? C0380R.string.Yes : C0380R.string.No));
        Button button = (Button) findViewById(C0380R.id.authorizedButton);
        button.setText(getString(this.authorized ? C0380R.string.set_unauthorized : C0380R.string.set_authorized));
        button.setOnClickListener(new ViewOnClickListenerC04441(button, textView));
    }

    /* renamed from: com.airliftcompany.alp3.dealer.EditDevice$1 */
    class ViewOnClickListenerC04441 implements View.OnClickListener {
        final /* synthetic */ TextView val$authorizedTextView;
        final /* synthetic */ Button val$setAuthorizedButton;

        ViewOnClickListenerC04441(Button button, TextView textView) {
            this.val$setAuthorizedButton = button;
            this.val$authorizedTextView = textView;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.val$setAuthorizedButton.setEnabled(false);
            EditDevice.this.mDialog.setMessage(EditDevice.this.getString(C0380R.string.please_wait));
            EditDevice.this.mDialog.setCancelable(true);
            EditDevice.this.mDialog.show();
            AsyncTask.execute(new Runnable() { // from class: com.airliftcompany.alp3.dealer.EditDevice.1.1
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        Gson gson = new Gson();
                        HashMap hashMap = new HashMap();
                        hashMap.put("Serial", new AttributeValue().withS(EditDevice.this.serial));
                        HashMap hashMap2 = new HashMap();
                        hashMap2.put(":a", new AttributeValue().withBOOL(Boolean.valueOf(!EditDevice.this.authorized)));
                        hashMap2.put(":d", new AttributeValue().withS(Util.serverStringForDate(EditDevice.this.authorizedDate != null ? EditDevice.this.authorizedDate : new Date())));
                        hashMap2.put(":m", new AttributeValue().withS(Util.serverStringForDate(new Date())));
                        hashMap2.put(":u", new AttributeValue().withS(CognitoService.getInstance().currentUsername()));
                        HashMap hashMap3 = new HashMap();
                        hashMap3.put("#a", "Authorized");
                        hashMap3.put("#d", "AuthorizedDate");
                        hashMap3.put("#m", "ModifiedDate");
                        hashMap3.put("#u", "Authorizer");
                        final UpdateItemResult updateItem = CognitoService.getInstance().getDynamoDBClient(EditDevice.this).updateItem(new UpdateItemRequest().withTableName("Devices").withKey(hashMap).withExpressionAttributeValues(hashMap2).withUpdateExpression("set #a = :a, #d = :d, #m = :m, #u = :u").withExpressionAttributeNames(hashMap3).withReturnValues(ReturnValue.ALL_NEW));
                        Log.e("updateItemResult :", gson.toJson(updateItem));
                        EditDevice.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.dealer.EditDevice.1.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                EditDevice editDevice;
                                int i;
                                EditDevice editDevice2;
                                int i2;
                                UpdateItemResult updateItemResult = updateItem;
                                if (updateItemResult != null) {
                                    AttributeValue attributeValue = updateItemResult.getAttributes().get("Authorized");
                                    EditDevice.this.authorized = attributeValue.getBOOL().booleanValue();
                                    TextView textView = ViewOnClickListenerC04441.this.val$authorizedTextView;
                                    if (EditDevice.this.authorized) {
                                        editDevice = EditDevice.this;
                                        i = C0380R.string.Yes;
                                    } else {
                                        editDevice = EditDevice.this;
                                        i = C0380R.string.No;
                                    }
                                    textView.setText(editDevice.getString(i));
                                    Button button = ViewOnClickListenerC04441.this.val$setAuthorizedButton;
                                    if (EditDevice.this.authorized) {
                                        editDevice2 = EditDevice.this;
                                        i2 = C0380R.string.set_unauthorized;
                                    } else {
                                        editDevice2 = EditDevice.this;
                                        i2 = C0380R.string.set_authorized;
                                    }
                                    button.setText(editDevice2.getString(i2));
                                    Util.displayAlert(EditDevice.this.getString(C0380R.string.success), EditDevice.this);
                                } else {
                                    Util.displayAlert(EditDevice.this.getString(C0380R.string.an_unknown_error_occured), EditDevice.this);
                                }
                                if (EditDevice.this.mDialog != null && EditDevice.this.mDialog.isShowing()) {
                                    EditDevice.this.mDialog.dismiss();
                                }
                                ViewOnClickListenerC04441.this.val$setAuthorizedButton.setEnabled(true);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(ClientConstants.DOMAIN_QUERY_PARAM_ERROR, e.getLocalizedMessage());
                        EditDevice.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.dealer.EditDevice.1.1.2
                            @Override // java.lang.Runnable
                            public void run() {
                                Util.displayAlert(EditDevice.this.getString(C0380R.string.an_unknown_error_occured), EditDevice.this);
                                ViewOnClickListenerC04441.this.val$setAuthorizedButton.setEnabled(true);
                                if (EditDevice.this.mDialog == null || !EditDevice.this.mDialog.isShowing()) {
                                    return;
                                }
                                EditDevice.this.mDialog.dismiss();
                            }
                        });
                    }
                }
            });
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
