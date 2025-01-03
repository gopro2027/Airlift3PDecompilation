package com.airliftcompany.alp3.dealer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.core.app.NavUtils;
import com.airliftcompany.alp3.C0380R;
import com.airliftcompany.alp3.utils.CognitoService;
import com.airliftcompany.alp3.utils.Util;
import com.amazonaws.mobileconnectors.cognitoauth.util.ClientConstants;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import java.util.Map;

/* loaded from: classes.dex */
public class FindDevice extends Activity {
    private ProgressDialog mDialog;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().setTitle("Find Device");
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(C0380R.layout.activity_find_device);
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.mDialog = progressDialog;
        progressDialog.getWindow().setFlags(8, 8);
        EditText editText = (EditText) findViewById(C0380R.id.serialEditText);
        Button button = (Button) findViewById(C0380R.id.searchButton);
        button.setOnClickListener(new ViewOnClickListenerC04451(button, editText));
    }

    /* renamed from: com.airliftcompany.alp3.dealer.FindDevice$1 */
    class ViewOnClickListenerC04451 implements View.OnClickListener {
        final /* synthetic */ Button val$searchButton;
        final /* synthetic */ EditText val$serialEditText;

        ViewOnClickListenerC04451(Button button, EditText editText) {
            this.val$searchButton = button;
            this.val$serialEditText = editText;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.val$searchButton.setEnabled(false);
            FindDevice.this.mDialog.setMessage(FindDevice.this.getString(C0380R.string.please_wait));
            FindDevice.this.mDialog.setCancelable(true);
            FindDevice.this.mDialog.show();
            AsyncTask.execute(new Runnable() { // from class: com.airliftcompany.alp3.dealer.FindDevice.1.1
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        final QueryResult query = CognitoService.getInstance().getDynamoDBClient(FindDevice.this).query(new QueryRequest().withTableName("Devices").withConsistentRead(Boolean.TRUE).withKeyConditions(ImmutableMap.m67of("Serial", new Condition().withComparisonOperator(ComparisonOperator.EQ).withAttributeValueList(new AttributeValue(ViewOnClickListenerC04451.this.val$serialEditText.getText().toString())))));
                        Log.e("updateItemResult :", new Gson().toJson(query));
                        final Map<String, AttributeValue> map = query.getItems().get(0);
                        FindDevice.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.dealer.FindDevice.1.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                if (query != null) {
                                    AttributeValue attributeValue = (AttributeValue) map.get("Serial");
                                    String s = attributeValue != null ? attributeValue.getS() : "";
                                    AttributeValue attributeValue2 = (AttributeValue) map.get("MacAddress");
                                    String s2 = attributeValue2 != null ? attributeValue2.getS() : "";
                                    AttributeValue attributeValue3 = (AttributeValue) map.get("Authorized");
                                    boolean booleanValue = attributeValue3 != null ? attributeValue3.getBOOL().booleanValue() : false;
                                    AttributeValue attributeValue4 = (AttributeValue) map.get("AuthorizedDate");
                                    String s3 = attributeValue4 != null ? attributeValue4.getS() : "";
                                    Intent intent = new Intent(FindDevice.this, (Class<?>) EditDevice.class);
                                    intent.putExtra("macAddress", s2);
                                    intent.putExtra("serial", s);
                                    intent.putExtra("authorized", booleanValue);
                                    intent.putExtra("authorizedDateString", s3);
                                    FindDevice.this.startActivity(intent);
                                } else {
                                    Util.displayAlert(FindDevice.this.getString(C0380R.string.device_not_found), FindDevice.this);
                                }
                                ViewOnClickListenerC04451.this.val$searchButton.setEnabled(true);
                                if (FindDevice.this.mDialog == null || !FindDevice.this.mDialog.isShowing()) {
                                    return;
                                }
                                FindDevice.this.mDialog.dismiss();
                            }
                        });
                    } catch (Exception e) {
                        Log.e(ClientConstants.DOMAIN_QUERY_PARAM_ERROR, e.getLocalizedMessage());
                        FindDevice.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.dealer.FindDevice.1.1.2
                            @Override // java.lang.Runnable
                            public void run() {
                                Util.displayAlert(FindDevice.this.getString(C0380R.string.device_not_found), FindDevice.this);
                                ViewOnClickListenerC04451.this.val$searchButton.setEnabled(true);
                                if (FindDevice.this.mDialog == null || !FindDevice.this.mDialog.isShowing()) {
                                    return;
                                }
                                FindDevice.this.mDialog.dismiss();
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
