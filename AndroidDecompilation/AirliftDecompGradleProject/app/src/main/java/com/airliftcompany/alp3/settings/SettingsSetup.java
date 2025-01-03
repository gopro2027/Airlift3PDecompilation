package com.airliftcompany.alp3.settings;

import android.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NavUtils;
import com.airliftcompany.alp3.C0380R;
import com.airliftcompany.alp3.comm.ALP3Device;
import com.airliftcompany.alp3.comm.CommService;
import com.airliftcompany.alp3.comm.TXController;
import com.airliftcompany.alp3.custom.ALP3ListActivity;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class SettingsSetup extends ALP3ListActivity {
    private static final String TAG = "SettingsSetup";
    DisplayListAdapter mDisplayListAdapter;
    private ProgressDialog mProgressDialog;
    private int mSelectedItem;
    private int minBatteryVoltage = -1;
    private int presetMode = -1;

    public enum DisplayRowEnum {
        SetupSensorTool,
        SetupCompressorRow,
        SetupMinBatteryVoltageRow,
        SetupPresetModeRow,
        SetupFactoryResetRow
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0380R.layout.activity_settings_home);
        DisplayListAdapter displayListAdapter = new DisplayListAdapter();
        this.mDisplayListAdapter = displayListAdapter;
        setListAdapter(displayListAdapter);
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(C0380R.string.Setup));
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
        ProgressDialog progressDialog;

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onCancelled(Boolean bool) {
        }

        public DownloadVariablesAsyncTask(Context context) {
            this.context = context;
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            ProgressDialog progressDialog = new ProgressDialog(this.context);
            this.progressDialog = progressDialog;
            progressDialog.setMessage(SettingsSetup.this.getString(C0380R.string.updating));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setProgressStyle(1);
            this.progressDialog.getWindow().setFlags(8, 8);
            this.progressDialog.show();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Boolean doInBackground(Void... voidArr) {
            SettingsSetup.this.mCommService.com100msState = CommService.com100msEnum.COM100mS_PAUSE;
            if (SettingsSetup.this.mCommService.txController.txGetRecord(9, 5)) {
                SettingsSetup settingsSetup = SettingsSetup.this;
                settingsSetup.minBatteryVoltage = (int) settingsSetup.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                if (SettingsSetup.this.mCommService.txController.txGetRecord(12, 12)) {
                    SettingsSetup settingsSetup2 = SettingsSetup.this;
                    settingsSetup2.presetMode = (int) settingsSetup2.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                    publishProgress(100);
                    SettingsSetup.this.mCommService.com100msState = CommService.com100msEnum.COM100mS_STATUS;
                    return true;
                }
            }
            return false;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Integer... numArr) {
            this.progressDialog.setProgress(numArr[0].intValue());
            SettingsSetup.this.mDisplayListAdapter.notifyDataSetChanged();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Boolean bool) {
            if (!bool.booleanValue()) {
                Toast.makeText(this.context, C0380R.string.error_communicating_with_manifold, 1).show();
            }
            ProgressDialog progressDialog = this.progressDialog;
            if (progressDialog != null && progressDialog.isShowing()) {
                this.progressDialog.setProgressStyle(0);
                this.progressDialog.dismiss();
            }
            this.progressDialog = null;
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
            this.mInflator = SettingsSetup.this.getLayoutInflater();
        }

        public void addItem(String str) {
            this.listValues.add(str);
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
                view = this.mInflator.inflate(C0380R.layout.listitem_settings, (ViewGroup) SettingsSetup.this.getListView(), false);
                viewHolder = new ViewHolder();
                viewHolder.titleTextView = (TextView) view.findViewById(C0380R.id.title_text_view);
                viewHolder.detailTextView = (TextView) view.findViewById(C0380R.id.detail_text_view);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.titleTextView.setText(getItem(i));
            if (SettingsSetup.this.mCommService == null) {
                viewHolder.detailTextView.setText(C0380R.string.updating);
            } else {
                int i2 = C05359.f68xf44c327[DisplayRowEnum.values()[i].ordinal()];
                if (i2 == 1 || i2 == 2 || i2 == 3) {
                    viewHolder.detailTextView.setText("");
                } else if (i2 != 4) {
                    if (i2 == 5) {
                        if (SettingsSetup.this.presetMode != 0) {
                            if (SettingsSetup.this.presetMode == 1) {
                                viewHolder.detailTextView.setText("Single");
                            } else {
                                viewHolder.detailTextView.setText(C0380R.string.updating);
                            }
                        } else {
                            viewHolder.detailTextView.setText("Double");
                        }
                    }
                } else if (SettingsSetup.this.minBatteryVoltage >= 0) {
                    viewHolder.detailTextView.setText(String.format("%d ", Integer.valueOf(SettingsSetup.this.minBatteryVoltage / 10)) + SettingsSetup.this.getString(C0380R.string.volts));
                } else {
                    viewHolder.detailTextView.setText(C0380R.string.updating);
                }
            }
            return view;
        }
    }

    /* renamed from: com.airliftcompany.alp3.settings.SettingsSetup$9 */
    static /* synthetic */ class C05359 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$settings$SettingsSetup$DisplayRowEnum */
        static final /* synthetic */ int[] f68xf44c327;

        static {
            int[] iArr = new int[DisplayRowEnum.values().length];
            f68xf44c327 = iArr;
            try {
                iArr[DisplayRowEnum.SetupSensorTool.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f68xf44c327[DisplayRowEnum.SetupCompressorRow.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f68xf44c327[DisplayRowEnum.SetupFactoryResetRow.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f68xf44c327[DisplayRowEnum.SetupMinBatteryVoltageRow.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                f68xf44c327[DisplayRowEnum.SetupPresetModeRow.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
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
        int i2 = C05359.f68xf44c327[DisplayRowEnum.values()[i].ordinal()];
        if (i2 == 1) {
            mIgnoreOnPause = true;
            startActivity(new Intent(this, (Class<?>) SettingsSensorTool.class));
            return;
        }
        if (i2 == 2) {
            mIgnoreOnPause = true;
            startActivity(new Intent(this, (Class<?>) SettingsCompressor.class));
            return;
        }
        if (i2 == 3) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View inflate = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
            TextView textView = (TextView) inflate.findViewById(C0380R.id.dialogTitleTextView);
            TextView textView2 = (TextView) inflate.findViewById(C0380R.id.dialogSubTitleTextView);
            textView.setText(C0380R.string.factory_reset);
            textView2.setText(C0380R.string.resets_all_settings_and_calibration_to_factory_default);
            builder.setCustomTitle(inflate);
            builder.setPositiveButton(C0380R.string.OK, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsSetup.8
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    SettingsSetup.this.mProgressDialog = new ProgressDialog(SettingsSetup.this);
                    SettingsSetup.this.mProgressDialog.setMessage(SettingsSetup.this.getString(C0380R.string.Resetting));
                    SettingsSetup.this.mProgressDialog.setCancelable(false);
                    SettingsSetup.this.mProgressDialog.setProgressStyle(1);
                    SettingsSetup.this.mProgressDialog.getWindow().setFlags(8, 8);
                    SettingsSetup.this.mProgressDialog.show();
                    dialogInterface.dismiss();
                    new TXController.FactoryResetAsyncTask(SettingsSetup.this.mCommService.txController, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsSetup.8.1
                        @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                        public void onTaskCompleted(Boolean bool) {
                            if (SettingsSetup.this.mProgressDialog != null && SettingsSetup.this.mProgressDialog.isShowing()) {
                                SettingsSetup.this.mProgressDialog.dismiss();
                            }
                            if (bool.booleanValue()) {
                                SettingsSetup.this.mDisplayListAdapter.notifyDataSetChanged();
                                new AlertDialog.Builder(SettingsSetup.this).setMessage(SettingsSetup.this.getString(C0380R.string.turn_ignition_off_then_back_on_to_complete_reset)).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsSetup.8.1.1
                                    @Override // android.content.DialogInterface.OnClickListener
                                    public void onClick(DialogInterface dialogInterface2, int i4) {
                                        dialogInterface2.cancel();
                                    }
                                }).setIcon(R.drawable.ic_dialog_info).show();
                            } else {
                                Toast.makeText(SettingsSetup.this, C0380R.string.error_communicating_with_manifold, 1).show();
                            }
                        }

                        @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                        public void onTaskProgressUpdate(Integer num) {
                            SettingsSetup.this.mProgressDialog.setProgress(num.intValue());
                        }
                    }).execute(new Void[0]);
                }
            }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsSetup.7
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    dialogInterface.dismiss();
                }
            });
            builder.create().show();
            return;
        }
        if (i2 != 4) {
            if (i2 != 5) {
                return;
            }
            AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
            View inflate2 = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
            TextView textView3 = (TextView) inflate2.findViewById(C0380R.id.dialogTitleTextView);
            TextView textView4 = (TextView) inflate2.findViewById(C0380R.id.dialogSubTitleTextView);
            textView3.setText(C0380R.string.preset_mode);
            textView4.setText(C0380R.string.preset_mode_help);
            builder2.setCustomTitle(inflate2);
            int i3 = this.presetMode;
            if (i3 > 0) {
                this.mSelectedItem = i3;
            } else {
                this.mSelectedItem = 0;
            }
            builder2.setSingleChoiceItems(new CharSequence[]{getString(C0380R.string.double_tap), getString(C0380R.string.single_tap)}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsSetup.6
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i4) {
                    SettingsSetup.this.mSelectedItem = i4;
                }
            }).setPositiveButton(C0380R.string.Save, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsSetup.5
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i4) {
                    dialogInterface.dismiss();
                    final TXController.WriteVariableAsyncTask writeVariableAsyncTask = new TXController.WriteVariableAsyncTask(SettingsSetup.this.mCommService.txController, (short) 12, (short) 12, SettingsSetup.this.mSelectedItem, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsSetup.5.1
                        @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                        public void onTaskProgressUpdate(Integer num) {
                        }

                        @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                        public void onTaskCompleted(Boolean bool) {
                            if (bool.booleanValue()) {
                                SettingsSetup settingsSetup = SettingsSetup.this;
                                ALP3Device.DisplaySettings displaySettings = SettingsSetup.this.mCommService.alp3Device.displaySettings;
                                int i5 = SettingsSetup.this.mSelectedItem;
                                displaySettings.PresetMode = i5;
                                settingsSetup.presetMode = i5;
                                SettingsSetup.this.mDisplayListAdapter.notifyDataSetChanged();
                                Toast.makeText(SettingsSetup.this, C0380R.string.Success, 0).show();
                                return;
                            }
                            Toast.makeText(SettingsSetup.this, C0380R.string.error_communicating_with_manifold, 1).show();
                        }
                    });
                    if (SettingsSetup.this.mSelectedItem == 1) {
                        AlertDialog.Builder builder3 = new AlertDialog.Builder(SettingsSetup.this);
                        builder3.setTitle(SettingsSetup.this.getString(C0380R.string.Caution)).setPositiveButton(C0380R.string.OK, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsSetup.5.3
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialogInterface2, int i5) {
                                dialogInterface2.dismiss();
                                writeVariableAsyncTask.execute(new Void[0]);
                            }
                        }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsSetup.5.2
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialogInterface2, int i5) {
                                dialogInterface2.dismiss();
                            }
                        });
                        builder3.setMessage(SettingsSetup.this.getString(C0380R.string.preset_caution));
                        builder3.create().show();
                        return;
                    }
                    writeVariableAsyncTask.execute(new Void[0]);
                }
            }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsSetup.4
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i4) {
                    dialogInterface.dismiss();
                }
            });
            builder2.create().show();
            return;
        }
        AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
        if (this.minBatteryVoltage > 0) {
            this.mSelectedItem = (r10 / 10) - 10;
        } else {
            this.mSelectedItem = 0;
        }
        View inflate3 = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
        TextView textView5 = (TextView) inflate3.findViewById(C0380R.id.dialogTitleTextView);
        TextView textView6 = (TextView) inflate3.findViewById(C0380R.id.dialogSubTitleTextView);
        textView5.setText(C0380R.string.min_battery_voltage);
        textView6.setText(C0380R.string.minimum_voltage_for_compressor_operation);
        builder3.setCustomTitle(inflate3);
        RelativeLayout relativeLayout = new RelativeLayout(this);
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMaxValue(5);
        numberPicker.setMinValue(0);
        numberPicker.setFormatter(new NumberPicker.Formatter() { // from class: com.airliftcompany.alp3.settings.SettingsSetup.1
            @Override // android.widget.NumberPicker.Formatter
            public String format(int i4) {
                return String.format("%d ", Integer.valueOf(i4 + 10)) + SettingsSetup.this.getString(C0380R.string.volts);
            }
        });
        try {
            Method declaredMethod = numberPicker.getClass().getDeclaredMethod("changeValueByOne", Boolean.TYPE);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(numberPicker, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
        }
        numberPicker.setValue(this.mSelectedItem);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams2.addRule(14);
        relativeLayout.setLayoutParams(layoutParams);
        relativeLayout.addView(numberPicker, layoutParams2);
        builder3.setView(relativeLayout).setPositiveButton(C0380R.string.Save, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsSetup.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i4) {
                dialogInterface.dismiss();
                SettingsSetup.this.mSelectedItem = (numberPicker.getValue() + 10) * 10;
                new TXController.WriteVariableAsyncTask(SettingsSetup.this.mCommService.txController, (short) 9, (short) 5, SettingsSetup.this.mSelectedItem, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsSetup.3.1
                    @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                    public void onTaskProgressUpdate(Integer num) {
                    }

                    @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                    public void onTaskCompleted(Boolean bool) {
                        if (bool.booleanValue()) {
                            SettingsSetup.this.minBatteryVoltage = SettingsSetup.this.mSelectedItem;
                            SettingsSetup.this.mDisplayListAdapter.notifyDataSetChanged();
                            Toast.makeText(SettingsSetup.this, C0380R.string.Success, 0).show();
                            return;
                        }
                        Toast.makeText(SettingsSetup.this, C0380R.string.error_communicating_with_manifold, 1).show();
                    }
                }).execute(new Void[0]);
            }
        }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsSetup.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i4) {
                dialogInterface.dismiss();
            }
        });
        builder3.create().show();
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity
    public void commServiceConnected() {
        super.commServiceConnected();
        this.mCommService.setCommServiceListener(this);
        new DownloadVariablesAsyncTask(this).execute(new Void[0]);
        this.mDisplayListAdapter.addItem(getString(C0380R.string.sensor_tool));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.Compressor));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.min_battery_voltage));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.preset_mode));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.factory_reset));
        this.mDisplayListAdapter.notifyDataSetChanged();
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity
    public void updateUI() {
        super.updateUI();
    }
}
