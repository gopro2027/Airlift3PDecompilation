package com.airliftcompany.alp3.dealer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.core.app.NavUtils;
import com.airliftcompany.alp3.C0380R;
import com.airliftcompany.alp3.utils.CognitoService;
import com.airliftcompany.alp3.utils.Util;

/* loaded from: classes.dex */
public class DealerLogin extends Activity {
    private ProgressDialog mDialog;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().setTitle(C0380R.string.dealer_login);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(C0380R.layout.activity_dealer_login);
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.mDialog = progressDialog;
        progressDialog.getWindow().setFlags(8, 8);
        EditText editText = (EditText) findViewById(C0380R.id.emailAddressEditText);
        EditText editText2 = (EditText) findViewById(C0380R.id.passwordEditText);
        Button button = (Button) findViewById(C0380R.id.loginButton);
        button.setOnClickListener(new ViewOnClickListenerC04411(button, editText, editText2));
        ((Button) findViewById(C0380R.id.forgotPasswordButton)).setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.dealer.DealerLogin.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DealerLogin.this.startActivity(new Intent(DealerLogin.this, (Class<?>) ResetPassword.class));
            }
        });
    }

    /* renamed from: com.airliftcompany.alp3.dealer.DealerLogin$1 */
    class ViewOnClickListenerC04411 implements View.OnClickListener {
        final /* synthetic */ Button val$buttonLogin;
        final /* synthetic */ EditText val$emailAddressEditText;
        final /* synthetic */ EditText val$passwordEditText;

        ViewOnClickListenerC04411(Button button, EditText editText, EditText editText2) {
            this.val$buttonLogin = button;
            this.val$emailAddressEditText = editText;
            this.val$passwordEditText = editText2;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.val$buttonLogin.setEnabled(false);
            DealerLogin.this.mDialog.setMessage(DealerLogin.this.getString(C0380R.string.please_wait));
            DealerLogin.this.mDialog.setCancelable(true);
            DealerLogin.this.mDialog.show();
            CognitoService.getInstance().login(this.val$emailAddressEditText.getText().toString(), this.val$passwordEditText.getText().toString(), DealerLogin.this, new AnonymousClass1());
        }

        /* renamed from: com.airliftcompany.alp3.dealer.DealerLogin$1$1, reason: invalid class name */
        class AnonymousClass1 implements CognitoService.CallbackInterface {
            AnonymousClass1() {
            }

            @Override // com.airliftcompany.alp3.utils.CognitoService.CallbackInterface
            public void completeCallback(final CognitoService.AuthResponse authResponse) {
                DealerLogin.this.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.dealer.DealerLogin.1.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        switch (C04433.f58x9eda8419[authResponse.ordinal()]) {
                            case 1:
                                DealerLogin.this.startActivity(new Intent(DealerLogin.this, (Class<?>) FindDevice.class));
                                break;
                            case 2:
                                Util.displayAlert(DealerLogin.this.getString(C0380R.string.your_password_is_incorrect), DealerLogin.this);
                                break;
                            case 3:
                                AlertDialog.Builder builder = new AlertDialog.Builder(DealerLogin.this);
                                builder.setMessage(DealerLogin.this.getString(C0380R.string.you_must_change_your_password));
                                builder.setPositiveButton(DealerLogin.this.getString(C0380R.string.OK), new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.dealer.DealerLogin.1.1.1.1
                                    @Override // android.content.DialogInterface.OnClickListener
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        DealerLogin.this.startActivity(new Intent(DealerLogin.this, (Class<?>) ChangePassword.class));
                                    }
                                });
                                builder.show();
                                break;
                            case 4:
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(DealerLogin.this);
                                builder2.setMessage(DealerLogin.this.getString(C0380R.string.you_must_reset_your_password));
                                builder2.setPositiveButton(DealerLogin.this.getString(C0380R.string.OK), new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.dealer.DealerLogin.1.1.1.2
                                    @Override // android.content.DialogInterface.OnClickListener
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        DealerLogin.this.startActivity(new Intent(DealerLogin.this, (Class<?>) ResetPassword.class));
                                    }
                                });
                                builder2.show();
                                break;
                            case 5:
                                Util.displayAlert(DealerLogin.this.getString(C0380R.string.too_many_failed_login_attempts), DealerLogin.this);
                                break;
                            case 6:
                                Util.displayAlert(DealerLogin.this.getString(C0380R.string.your_email_address_is_not_found), DealerLogin.this);
                                break;
                            default:
                                Util.displayAlert(DealerLogin.this.getString(C0380R.string.an_unknown_error_occured), DealerLogin.this);
                                break;
                        }
                        if (DealerLogin.this.mDialog != null && DealerLogin.this.mDialog.isShowing()) {
                            DealerLogin.this.mDialog.dismiss();
                        }
                        ViewOnClickListenerC04411.this.val$buttonLogin.setEnabled(true);
                    }
                });
            }
        }
    }

    /* renamed from: com.airliftcompany.alp3.dealer.DealerLogin$3 */
    static /* synthetic */ class C04433 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$utils$CognitoService$AuthResponse */
        static final /* synthetic */ int[] f58x9eda8419;

        static {
            int[] iArr = new int[CognitoService.AuthResponse.values().length];
            f58x9eda8419 = iArr;
            try {
                iArr[CognitoService.AuthResponse.AuthSuccess.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f58x9eda8419[CognitoService.AuthResponse.IncorrectPassword.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f58x9eda8419[CognitoService.AuthResponse.ForceChangePassword.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f58x9eda8419[CognitoService.AuthResponse.ResetPasswordRequired.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                f58x9eda8419[CognitoService.AuthResponse.TooManyFailedAttempts.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                f58x9eda8419[CognitoService.AuthResponse.UserNotFound.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                f58x9eda8419[CognitoService.AuthResponse.UnknownError.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
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
