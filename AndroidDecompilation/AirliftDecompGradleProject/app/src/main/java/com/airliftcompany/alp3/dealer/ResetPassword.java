package com.airliftcompany.alp3.dealer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NavUtils;
import com.airliftcompany.alp3.C0380R;
import com.airliftcompany.alp3.utils.CognitoService;
import com.airliftcompany.alp3.utils.Util;

/* loaded from: classes.dex */
public class ResetPassword extends Activity {
    private ProgressDialog mDialog;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().setTitle(C0380R.string.reset_password);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(C0380R.layout.activity_reset_password);
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.mDialog = progressDialog;
        progressDialog.getWindow().setFlags(8, 8);
        EditText editText = (EditText) findViewById(C0380R.id.emailAddressEditText);
        Button button = (Button) findViewById(C0380R.id.resetCodeButton);
        TextView textView = (TextView) findViewById(C0380R.id.infoText);
        EditText editText2 = (EditText) findViewById(C0380R.id.resetCodeEditText);
        EditText editText3 = (EditText) findViewById(C0380R.id.passwordEditText);
        EditText editText4 = (EditText) findViewById(C0380R.id.verifyPasswordEditText);
        Button button2 = (Button) findViewById(C0380R.id.setPasswordButton);
        button.setOnClickListener(new ViewOnClickListenerC04461(button, editText, textView, editText2, editText3, editText4, button2));
        button2.setOnClickListener(new ViewOnClickListenerC04472(editText3, editText4, button2, editText2));
        textView.setVisibility(4);
        editText2.setVisibility(4);
        editText3.setVisibility(4);
        editText4.setVisibility(4);
        button2.setVisibility(4);
    }

    /* renamed from: com.airliftcompany.alp3.dealer.ResetPassword$1 */
    class ViewOnClickListenerC04461 implements View.OnClickListener {
        final /* synthetic */ EditText val$emailAddressEditText;
        final /* synthetic */ TextView val$infoText;
        final /* synthetic */ EditText val$passwordEditText;
        final /* synthetic */ Button val$resetCodeButton;
        final /* synthetic */ EditText val$resetCodeEditText;
        final /* synthetic */ Button val$setPasswordButton;
        final /* synthetic */ EditText val$verifyPasswordEditText;

        ViewOnClickListenerC04461(Button button, EditText editText, TextView textView, EditText editText2, EditText editText3, EditText editText4, Button button2) {
            this.val$resetCodeButton = button;
            this.val$emailAddressEditText = editText;
            this.val$infoText = textView;
            this.val$resetCodeEditText = editText2;
            this.val$passwordEditText = editText3;
            this.val$verifyPasswordEditText = editText4;
            this.val$setPasswordButton = button2;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.val$resetCodeButton.setEnabled(false);
            ResetPassword.this.mDialog.setMessage(ResetPassword.this.getString(C0380R.string.please_wait));
            ResetPassword.this.mDialog.setCancelable(true);
            ResetPassword.this.mDialog.show();
            CognitoService.getInstance().resetPassword(this.val$emailAddressEditText.getText().toString(), ResetPassword.this, new CognitoService.CallbackInterface() { // from class: com.airliftcompany.alp3.dealer.ResetPassword.1.1
                @Override // com.airliftcompany.alp3.utils.CognitoService.CallbackInterface
                public void completeCallback(final CognitoService.AuthResponse authResponse) {
                    ResetPassword.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.dealer.ResetPassword.1.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            int i = C04483.f59x9eda8419[authResponse.ordinal()];
                            if (i == 1) {
                                ViewOnClickListenerC04461.this.val$infoText.setVisibility(0);
                                ViewOnClickListenerC04461.this.val$resetCodeEditText.setVisibility(0);
                                ViewOnClickListenerC04461.this.val$passwordEditText.setVisibility(0);
                                ViewOnClickListenerC04461.this.val$verifyPasswordEditText.setVisibility(0);
                                ViewOnClickListenerC04461.this.val$setPasswordButton.setVisibility(0);
                                Toast.makeText(ResetPassword.this, ResetPassword.this.getString(C0380R.string.success), 0).show();
                            } else if (i == 2) {
                                Util.displayAlert(ResetPassword.this.getString(C0380R.string.your_email_address_is_not_found), ResetPassword.this);
                            } else if (i == 3) {
                                Util.displayAlert(ResetPassword.this.getString(C0380R.string.too_many_failed_login_attempts), ResetPassword.this);
                            } else if (i == 4) {
                                Util.displayAlert(ResetPassword.this.getString(C0380R.string.error_sending_password_reset_code), ResetPassword.this);
                            } else {
                                Util.displayAlert(ResetPassword.this.getString(C0380R.string.an_unknown_error_occured), ResetPassword.this);
                            }
                            if (ResetPassword.this.mDialog != null && ResetPassword.this.mDialog.isShowing()) {
                                ResetPassword.this.mDialog.dismiss();
                            }
                            ViewOnClickListenerC04461.this.val$resetCodeButton.setEnabled(true);
                        }
                    });
                }
            });
        }
    }

    /* renamed from: com.airliftcompany.alp3.dealer.ResetPassword$3 */
    static /* synthetic */ class C04483 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$utils$CognitoService$AuthResponse */
        static final /* synthetic */ int[] f59x9eda8419;

        static {
            int[] iArr = new int[CognitoService.AuthResponse.values().length];
            f59x9eda8419 = iArr;
            try {
                iArr[CognitoService.AuthResponse.AuthSuccess.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f59x9eda8419[CognitoService.AuthResponse.UserNotFound.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f59x9eda8419[CognitoService.AuthResponse.TooManyFailedAttempts.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f59x9eda8419[CognitoService.AuthResponse.CodeDeliveryFailure.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                f59x9eda8419[CognitoService.AuthResponse.UnknownError.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                f59x9eda8419[CognitoService.AuthResponse.IncorrectPassword.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                f59x9eda8419[CognitoService.AuthResponse.ExpiredCode.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                f59x9eda8419[CognitoService.AuthResponse.IncorrectCode.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
        }
    }

    /* renamed from: com.airliftcompany.alp3.dealer.ResetPassword$2 */
    class ViewOnClickListenerC04472 implements View.OnClickListener {
        final /* synthetic */ EditText val$passwordEditText;
        final /* synthetic */ EditText val$resetCodeEditText;
        final /* synthetic */ Button val$setPasswordButton;
        final /* synthetic */ EditText val$verifyPasswordEditText;

        ViewOnClickListenerC04472(EditText editText, EditText editText2, Button button, EditText editText3) {
            this.val$passwordEditText = editText;
            this.val$verifyPasswordEditText = editText2;
            this.val$setPasswordButton = button;
            this.val$resetCodeEditText = editText3;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (!this.val$passwordEditText.getText().toString().equals(this.val$verifyPasswordEditText.getText().toString())) {
                Util.displayAlert(ResetPassword.this.getString(C0380R.string.passwords_do_not_match), ResetPassword.this);
                return;
            }
            this.val$setPasswordButton.setEnabled(false);
            ResetPassword.this.mDialog.setMessage(ResetPassword.this.getString(C0380R.string.please_wait));
            ResetPassword.this.mDialog.setCancelable(true);
            ResetPassword.this.mDialog.show();
            CognitoService.getInstance().confirmForgottenPassword(this.val$resetCodeEditText.getText().toString(), this.val$passwordEditText.getText().toString(), ResetPassword.this, new CognitoService.CallbackInterface() { // from class: com.airliftcompany.alp3.dealer.ResetPassword.2.1
                @Override // com.airliftcompany.alp3.utils.CognitoService.CallbackInterface
                public void completeCallback(final CognitoService.AuthResponse authResponse) {
                    ResetPassword.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.dealer.ResetPassword.2.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            int i = C04483.f59x9eda8419[authResponse.ordinal()];
                            if (i == 1) {
                                Toast.makeText(ResetPassword.this, ResetPassword.this.getString(C0380R.string.success), 0).show();
                                NavUtils.navigateUpFromSameTask(ResetPassword.this);
                            } else if (i == 3) {
                                Util.displayAlert(ResetPassword.this.getString(C0380R.string.too_many_failed_login_attempts), ResetPassword.this);
                            } else if (i == 6) {
                                Util.displayAlert(ResetPassword.this.getString(C0380R.string.invalid_password), ResetPassword.this);
                            } else if (i == 7) {
                                Util.displayAlert(ResetPassword.this.getString(C0380R.string.reset_code_has_expired), ResetPassword.this);
                            } else if (i == 8) {
                                Util.displayAlert(ResetPassword.this.getString(C0380R.string.password_reset_code_is_incorrect), ResetPassword.this);
                            } else {
                                Util.displayAlert(ResetPassword.this.getString(C0380R.string.an_unknown_error_occured), ResetPassword.this);
                            }
                            Toast.makeText(ResetPassword.this, "Success!", 0).show();
                            if (ResetPassword.this.mDialog != null && ResetPassword.this.mDialog.isShowing()) {
                                ResetPassword.this.mDialog.dismiss();
                            }
                            ViewOnClickListenerC04472.this.val$setPasswordButton.setEnabled(true);
                        }
                    });
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
