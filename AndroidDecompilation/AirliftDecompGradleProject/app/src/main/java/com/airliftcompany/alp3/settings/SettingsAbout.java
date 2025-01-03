package com.airliftcompany.alp3.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NavUtils;
import com.airliftcompany.alp3.C0380R;
import com.airliftcompany.alp3.comm.CommService;
import com.airliftcompany.alp3.custom.ALP3ListActivity;
import com.airliftcompany.alp3.firmware.FirmwareVersionDownloadUtility;
import com.airliftcompany.alp3.firmware.UpdateAvailableActivity;
import java.util.ArrayList;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class SettingsAbout extends ALP3ListActivity {
    private static final String TAG = "SettingsAbout";
    private String applicationVersion;
    private String displayVersion;
    private String ecuVersion;
    private Boolean mAreAllItemsEnabled = true;
    DisplayListAdapter mDisplayListAdapter;
    ProgressDialog mProgressDialog;

    public enum DisplayListEnum {
        AboutAppVersionRow,
        AboutManifoldVersionRow,
        AboutDisplayVersionRow,
        AboutCheckVersionRow
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0380R.layout.activity_settings_home);
        this.ecuVersion = getString(C0380R.string.updating);
        this.displayVersion = getString(C0380R.string.updating);
        this.applicationVersion = getString(C0380R.string.updating);
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            this.applicationVersion = packageInfo.versionName + " (" + packageInfo.versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        DisplayListAdapter displayListAdapter = new DisplayListAdapter();
        this.mDisplayListAdapter = displayListAdapter;
        setListAdapter(displayListAdapter);
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(C0380R.string.About));
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            mIgnoreOnPause = true;
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private class DownloadVariablesAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        private Context context;

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onCancelled(Boolean bool) {
        }

        public DownloadVariablesAsyncTask(Context context) {
            this.context = context;
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            SettingsAbout.this.mProgressDialog = new ProgressDialog(this.context);
            SettingsAbout.this.mProgressDialog.setMessage(SettingsAbout.this.getString(C0380R.string.updating));
            SettingsAbout.this.mProgressDialog.setCancelable(false);
            SettingsAbout.this.mProgressDialog.setProgressStyle(1);
            SettingsAbout.this.mProgressDialog.getWindow().setFlags(8, 8);
            SettingsAbout.this.mProgressDialog.show();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Boolean doInBackground(Void... voidArr) {
            SettingsAbout.this.mCommService.com100msState = CommService.com100msEnum.COM100mS_PAUSE;
            if (SettingsAbout.this.mCommService.txController.txGetVariable(1)) {
                SettingsAbout.this.ecuVersion = String.format("%d.%d.%d", Long.valueOf((SettingsAbout.this.mCommService.alp3Device.canpng65351ReplyVariable.Variable & (-16777216)) >> 24), Long.valueOf((SettingsAbout.this.mCommService.alp3Device.canpng65351ReplyVariable.Variable & 16711680) >> 16), Long.valueOf((SettingsAbout.this.mCommService.alp3Device.canpng65351ReplyVariable.Variable & 65280) >> 8));
                publishProgress(50);
                if (SettingsAbout.this.mCommService.txController.txGetVariable(3)) {
                    SettingsAbout.this.displayVersion = String.format("%d.%d.%d", Long.valueOf(((-16777216) & SettingsAbout.this.mCommService.alp3Device.canpng65351ReplyVariable.Variable) >> 24), Long.valueOf((SettingsAbout.this.mCommService.alp3Device.canpng65351ReplyVariable.Variable & 16711680) >> 16), Long.valueOf((SettingsAbout.this.mCommService.alp3Device.canpng65351ReplyVariable.Variable & 65280) >> 8));
                    publishProgress(100);
                    SettingsAbout.this.mCommService.com100msState = CommService.com100msEnum.COM100mS_STATUS;
                    return true;
                }
            }
            return false;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Integer... numArr) {
            SettingsAbout.this.mProgressDialog.setProgress(numArr[0].intValue());
            SettingsAbout.this.mDisplayListAdapter.notifyDataSetChanged();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Boolean bool) {
            if (!bool.booleanValue()) {
                Toast.makeText(this.context, C0380R.string.error_communicating_with_manifold, 1).show();
            }
            if (SettingsAbout.this.mProgressDialog != null && SettingsAbout.this.mProgressDialog.isShowing()) {
                SettingsAbout.this.mProgressDialog.setProgressStyle(0);
                SettingsAbout.this.mProgressDialog.dismiss();
            }
            SettingsAbout.this.mProgressDialog = null;
        }
    }

    private class DisplayListAdapter extends BaseAdapter {
        private final ArrayList<String> listValues = new ArrayList<>();
        private final LayoutInflater mInflator;

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        public DisplayListAdapter() {
            this.mInflator = SettingsAbout.this.getLayoutInflater();
        }

        public void removeAllItems() {
            this.listValues.clear();
        }

        public void addItem(String str) {
            this.listValues.add(str);
        }

        @Override // android.widget.BaseAdapter, android.widget.ListAdapter
        public boolean areAllItemsEnabled() {
            return SettingsAbout.this.mAreAllItemsEnabled.booleanValue();
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.listValues.size();
        }

        @Override // android.widget.Adapter
        public String getItem(int i) {
            return this.listValues.get(i);
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = this.mInflator.inflate(C0380R.layout.listitem_settings, (ViewGroup) SettingsAbout.this.getListView(), false);
                viewHolder = new ViewHolder();
                viewHolder.titleTextView = (TextView) view.findViewById(C0380R.id.title_text_view);
                viewHolder.detailTextView = (TextView) view.findViewById(C0380R.id.detail_text_view);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.titleTextView.setText(getItem(i));
            if (SettingsAbout.this.mCommService == null) {
                viewHolder.detailTextView.setText(C0380R.string.updating);
            } else {
                int i2 = C04612.f61xcd042c23[DisplayListEnum.values()[i].ordinal()];
                if (i2 == 1) {
                    viewHolder.detailTextView.setText(SettingsAbout.this.applicationVersion);
                } else if (i2 == 2) {
                    viewHolder.detailTextView.setText(SettingsAbout.this.ecuVersion);
                } else if (i2 == 3) {
                    viewHolder.detailTextView.setText(SettingsAbout.this.displayVersion);
                }
            }
            return view;
        }
    }

    /* renamed from: com.airliftcompany.alp3.settings.SettingsAbout$2 */
    static /* synthetic */ class C04612 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$settings$SettingsAbout$DisplayListEnum */
        static final /* synthetic */ int[] f61xcd042c23;

        static {
            int[] iArr = new int[DisplayListEnum.values().length];
            f61xcd042c23 = iArr;
            try {
                iArr[DisplayListEnum.AboutAppVersionRow.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f61xcd042c23[DisplayListEnum.AboutManifoldVersionRow.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f61xcd042c23[DisplayListEnum.AboutDisplayVersionRow.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f61xcd042c23[DisplayListEnum.AboutCheckVersionRow.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    static class ViewHolder {
        TextView detailTextView;
        TextView titleTextView;

        ViewHolder() {
        }
    }

    @Override // android.app.ListActivity
    protected void onListItemClick(ListView listView, View view, int i, long j) {
        super.onListItemClick(listView, view, i, j);
        if (C04612.f61xcd042c23[DisplayListEnum.values()[i].ordinal()] != 4) {
            return;
        }
        this.mAreAllItemsEnabled = false;
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.mProgressDialog = progressDialog;
        progressDialog.setMessage(getString(C0380R.string.check_for_firmware_update));
        this.mProgressDialog.setCancelable(true);
        this.mProgressDialog.setProgressStyle(0);
        this.mProgressDialog.getWindow().setFlags(8, 8);
        this.mProgressDialog.show();
        new FirmwareVersionDownloadUtility().checkForFirmwareUpdate(this, this.mCommService, new FirmwareVersionDownloadUtility.DownloadFirmwareListener() { // from class: com.airliftcompany.alp3.settings.SettingsAbout.1
            @Override // com.airliftcompany.alp3.firmware.FirmwareVersionDownloadUtility.DownloadFirmwareListener
            public void onTaskProgressUpdate(Integer num) {
            }

            @Override // com.airliftcompany.alp3.firmware.FirmwareVersionDownloadUtility.DownloadFirmwareListener
            public void onTaskCompleted(Boolean bool, Boolean bool2, final JSONObject jSONObject, Exception exc) {
                SettingsAbout.this.mAreAllItemsEnabled = true;
                if (SettingsAbout.this.mProgressDialog != null && SettingsAbout.this.mProgressDialog.isShowing()) {
                    SettingsAbout.this.mProgressDialog.setProgressStyle(0);
                    SettingsAbout.this.mProgressDialog.dismiss();
                }
                SettingsAbout.this.mProgressDialog = null;
                if (!bool.booleanValue()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsAbout.this);
                    builder.setTitle(SettingsAbout.this.getString(C0380R.string.Error));
                    builder.setMessage(SettingsAbout.this.getString(C0380R.string.error_downloading_firmware_from_internet));
                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsAbout.1.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i2) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.create().show();
                    return;
                }
                if (bool2.booleanValue()) {
                    ALP3ListActivity.mIgnoreOnPause = true;
                    Intent intent = new Intent(SettingsAbout.this, (Class<?>) UpdateAvailableActivity.class);
                    intent.putExtra("firmwareJSON", jSONObject.toString());
                    SettingsAbout.this.startActivity(intent);
                    return;
                }
                AlertDialog.Builder builder2 = new AlertDialog.Builder(SettingsAbout.this);
                builder2.setMessage(SettingsAbout.this.getString(C0380R.string.no_update_available_update_firmware_anyway));
                builder2.setPositiveButton("Yes", new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsAbout.1.2
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        ALP3ListActivity.mIgnoreOnPause = true;
                        Intent intent2 = new Intent(SettingsAbout.this, (Class<?>) UpdateAvailableActivity.class);
                        intent2.putExtra("firmwareJSON", jSONObject.toString());
                        SettingsAbout.this.startActivity(intent2);
                        dialogInterface.dismiss();
                    }
                });
                builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsAbout.1.3
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        dialogInterface.dismiss();
                    }
                });
                builder2.show();
            }
        });
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity
    public void commServiceConnected() {
        super.commServiceConnected();
        this.mCommService.setCommServiceListener(this);
        if (this.mCommService.commStatus.BlueToothAccConnected) {
            new DownloadVariablesAsyncTask(this).execute(new Void[0]);
        }
        this.mDisplayListAdapter.removeAllItems();
        this.mDisplayListAdapter.addItem(getString(C0380R.string.android_app));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.manifold_version));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.display_version));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.check_for_firmware_update));
        this.mDisplayListAdapter.notifyDataSetChanged();
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity
    public void updateUI() {
        super.updateUI();
    }
}
