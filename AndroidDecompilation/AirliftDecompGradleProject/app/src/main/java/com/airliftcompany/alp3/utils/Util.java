package com.airliftcompany.alp3.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.airliftcompany.alp3.C0380R;
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/* loaded from: classes.dex */
public class Util {
    public static boolean isChinese() {
        return false;
    }

    public static boolean internetConnection(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public static void handleException(final Exception exc, final Context context) {
        ThreadUtils.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.utils.Util.1
            @Override // java.lang.Runnable
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(exc.getLocalizedMessage());
                builder.setPositiveButton(context.getString(C0380R.string.OK), new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.utils.Util.1.1
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.show();
            }
        });
    }

    public static void displayAlert(final String str, final Context context) {
        ThreadUtils.runOnUiThread(new Runnable() { // from class: com.airliftcompany.alp3.utils.Util.2
            @Override // java.lang.Runnable
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(str);
                builder.setPositiveButton(context.getString(C0380R.string.OK), new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.utils.Util.2.1
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.show();
            }
        });
    }

    public static Date dateForServerString(String str) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(str);
        } catch (Exception unused) {
            return null;
        }
    }

    public static String serverStringForDate(Date date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(new Date());
        } catch (Exception unused) {
            return null;
        }
    }
}
