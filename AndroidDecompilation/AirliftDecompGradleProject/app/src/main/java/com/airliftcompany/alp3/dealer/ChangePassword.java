package com.airliftcompany.alp3.dealer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.core.app.NavUtils;
import com.airliftcompany.alp3.C0380R;
import com.airliftcompany.alp3.utils.CognitoService;
import com.airliftcompany.alp3.utils.Util;

/* loaded from: classes.dex */
public class ChangePassword extends Activity {
    private ProgressDialog mDialog;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().setTitle(C0380R.string.change_password);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(C0380R.layout.activity_change_password);
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.mDialog = progressDialog;
        progressDialog.getWindow().setFlags(8, 8);
        EditText editText = (EditText) findViewById(C0380R.id.oldPasswordEditText);
        EditText editText2 = (EditText) findViewById(C0380R.id.passwordEditText);
        EditText editText3 = (EditText) findViewById(C0380R.id.verifyPasswordEditText);
        Button button = (Button) findViewById(C0380R.id.changePasswordButton);
        button.setOnClickListener(new ViewOnClickListenerC04391(editText2, editText3, button, editText));
    }

    /* renamed from: com.airliftcompany.alp3.dealer.ChangePassword$1 */
    class ViewOnClickListenerC04391 implements View.OnClickListener {
        final /* synthetic */ Button val$changePasswordButton;
        final /* synthetic */ EditText val$oldPasswordEditText;
        final /* synthetic */ EditText val$passwordEditText;
        final /* synthetic */ EditText val$verifyPasswordEditText;

        ViewOnClickListenerC04391(EditText editText, EditText editText2, Button button, EditText editText3) {
            this.val$passwordEditText = editText;
            this.val$verifyPasswordEditText = editText2;
            this.val$changePasswordButton = button;
            this.val$oldPasswordEditText = editText3;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (!this.val$passwordEditText.getText().toString().equals(this.val$verifyPasswordEditText.getText().toString())) {
                Util.displayAlert(ChangePassword.this.getString(C0380R.string.passwords_do_not_match), ChangePassword.this);
                return;
            }
            this.val$changePasswordButton.setEnabled(false);
            ChangePassword.this.mDialog.setMessage(ChangePassword.this.getString(C0380R.string.please_wait));
            ChangePassword.this.mDialog.setCancelable(true);
            ChangePassword.this.mDialog.show();
            CognitoService.getInstance().changePassword(this.val$oldPasswordEditText.getText().toString(), this.val$passwordEditText.getText().toString(), ChangePassword.this, new CognitoService.CallbackInterface() { // from class: com.airliftcompany.alp3.dealer.ChangePassword.1.1
                @Override // com.airliftcompany.alp3.utils.CognitoService.CallbackInterface
                public void completeCallback(final CognitoService.AuthResponse authResponse) {
                    ChangePassword.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.dealer.ChangePassword.1.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            int i = C04402.f57x9eda8419[authResponse.ordinal()];
                            if (i == 1) {
                                Toast.makeText(ChangePassword.this, ChangePassword.this.getString(C0380R.string.success), 0).show();
                                NavUtils.navigateUpFromSameTask(ChangePassword.this);
                            } else if (i == 2) {
                                Util.displayAlert(ChangePassword.this.getString(C0380R.string.invalid_password), ChangePassword.this);
                            } else if (i == 3) {
                                Util.displayAlert(ChangePassword.this.getString(C0380R.string.your_old_password_is_incorrect), ChangePassword.this);
                            } else if (i == 4) {
                                Util.displayAlert(ChangePassword.this.getString(C0380R.string.too_many_failed_login_attempts), ChangePassword.this);
                            } else {
                                Util.displayAlert(ChangePassword.this.getString(C0380R.string.an_unknown_error_occured), ChangePassword.this);
                            }
                            if (ChangePassword.this.mDialog != null && ChangePassword.this.mDialog.isShowing()) {
                                ChangePassword.this.mDialog.dismiss();
                            }
                            ViewOnClickListenerC04391.this.val$changePasswordButton.setEnabled(true);
                        }
                    });
                }
            });
        }
    }

    /* renamed from: com.airliftcompany.alp3.dealer.ChangePassword$2 */
    static /* synthetic */ class C04402 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$utils$CognitoService$AuthResponse */
        static final /* synthetic */ int[] f57x9eda8419;

        static {
            int[] iArr = new int[CognitoService.AuthResponse.values().length];
            f57x9eda8419 = iArr;
            try {
                iArr[CognitoService.AuthResponse.AuthSuccess.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f57x9eda8419[CognitoService.AuthResponse.InvalidPassword.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f57x9eda8419[CognitoService.AuthResponse.IncorrectPassword.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f57x9eda8419[CognitoService.AuthResponse.TooManyFailedAttempts.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                f57x9eda8419[CognitoService.AuthResponse.UnknownError.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
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
