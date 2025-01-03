package com.airliftcompany.alp3.settings;

import android.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.airliftcompany.alp3.comm.CommService;
import com.airliftcompany.alp3.comm.TXController;
import com.airliftcompany.alp3.custom.ALP3ListActivity;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class SettingsOperation extends ALP3ListActivity {
    private static final String TAG = "SettingsOperation";
    DisplayListAdapter mDisplayListAdapter;
    private int mSelectedItem;
    private int riseOnStart = -1;
    private int preSetMaintain = -1;
    private int minDriveHeight = -1;
    private int controlMode = -1;
    private int equalize = -1;

    public enum DisplayRowEnum {
        OperationRiseOnStartRow,
        OperationPreSetMaintainRow,
        OperationMinDrivingHeightRow,
        OperationPressureHeightRow,
        OperationAxleEqualizationRow,
        OperationShowModeRow
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0380R.layout.activity_settings_home);
        getListView().setBackgroundColor(getResources().getColor(R.color.transparent));
        DisplayListAdapter displayListAdapter = new DisplayListAdapter();
        this.mDisplayListAdapter = displayListAdapter;
        setListAdapter(displayListAdapter);
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(C0380R.string.Operation));
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
            progressDialog.setMessage(SettingsOperation.this.getString(C0380R.string.updating));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setProgressStyle(1);
            this.progressDialog.getWindow().setFlags(8, 8);
            this.progressDialog.show();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Boolean doInBackground(Void... voidArr) {
            SettingsOperation.this.mCommService.com100msState = CommService.com100msEnum.COM100mS_PAUSE;
            if (SettingsOperation.this.mCommService.txController.txGetRecord(9, 3)) {
                SettingsOperation settingsOperation = SettingsOperation.this;
                settingsOperation.riseOnStart = (int) settingsOperation.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                publishProgress(20);
                if (SettingsOperation.this.mCommService.txController.txGetRecord(9, 2)) {
                    SettingsOperation settingsOperation2 = SettingsOperation.this;
                    settingsOperation2.preSetMaintain = (int) settingsOperation2.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                    publishProgress(40);
                    if (SettingsOperation.this.mCommService.txController.txGetRecord(9, 4)) {
                        SettingsOperation settingsOperation3 = SettingsOperation.this;
                        settingsOperation3.minDriveHeight = (int) settingsOperation3.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                        publishProgress(60);
                        if (SettingsOperation.this.mCommService.txController.txGetRecord(9, 1)) {
                            SettingsOperation settingsOperation4 = SettingsOperation.this;
                            settingsOperation4.controlMode = (int) settingsOperation4.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                            publishProgress(80);
                            if (SettingsOperation.this.mCommService.txController.txGetRecord(9, 6)) {
                                SettingsOperation settingsOperation5 = SettingsOperation.this;
                                settingsOperation5.equalize = (int) settingsOperation5.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                                publishProgress(100);
                                SettingsOperation.this.mCommService.com100msState = CommService.com100msEnum.COM100mS_STATUS;
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Integer... numArr) {
            this.progressDialog.setProgress(numArr[0].intValue());
            SettingsOperation.this.mDisplayListAdapter.notifyDataSetChanged();
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
            this.mInflator = SettingsOperation.this.getLayoutInflater();
        }

        public void removeAllItems() {
            this.listValues.clear();
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

        /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = this.mInflator.inflate(C0380R.layout.listitem_settings, (ViewGroup) SettingsOperation.this.getListView(), false);
                viewHolder = new ViewHolder();
                viewHolder.titleTextView = (TextView) view.findViewById(C0380R.id.title_text_view);
                viewHolder.detailTextView = (TextView) view.findViewById(C0380R.id.detail_text_view);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.titleTextView.setText(getItem(i));
            if (SettingsOperation.this.mCommService != null) {
                switch (C050919.f67xc8f7cb7d[DisplayRowEnum.values()[i].ordinal()]) {
                    case 1:
                        if (SettingsOperation.this.riseOnStart >= 0) {
                            if (SettingsOperation.this.riseOnStart == 0) {
                                viewHolder.detailTextView.setText(C0380R.string.Off);
                                break;
                            } else {
                                viewHolder.detailTextView.setText(C0380R.string.On);
                                break;
                            }
                        } else {
                            viewHolder.detailTextView.setText(C0380R.string.updating);
                            break;
                        }
                    case 2:
                        if (SettingsOperation.this.preSetMaintain >= 0) {
                            if (SettingsOperation.this.preSetMaintain == 0) {
                                viewHolder.detailTextView.setText(C0380R.string.Off);
                                break;
                            } else {
                                viewHolder.detailTextView.setText(C0380R.string.On);
                                break;
                            }
                        } else {
                            viewHolder.detailTextView.setText(C0380R.string.updating);
                            break;
                        }
                    case 3:
                        if (SettingsOperation.this.minDriveHeight >= 0) {
                            if (SettingsOperation.this.controlMode >= 0) {
                                if (SettingsOperation.this.controlMode == 0) {
                                    viewHolder.detailTextView.setText(String.format("%d %%", Integer.valueOf(SettingsOperation.this.minDriveHeight / 10)));
                                    break;
                                } else {
                                    viewHolder.detailTextView.setText(String.format("%d PSI", Integer.valueOf(SettingsOperation.this.minDriveHeight / 10)));
                                    break;
                                }
                            } else {
                                viewHolder.detailTextView.setText(C0380R.string.updating);
                                break;
                            }
                        } else {
                            viewHolder.detailTextView.setText(C0380R.string.updating);
                            break;
                        }
                    case 4:
                        if (SettingsOperation.this.controlMode >= 0) {
                            if (SettingsOperation.this.controlMode == 0) {
                                viewHolder.detailTextView.setText(C0380R.string.Height);
                                break;
                            } else {
                                viewHolder.detailTextView.setText(C0380R.string.Pressure);
                                break;
                            }
                        } else {
                            viewHolder.detailTextView.setText(C0380R.string.updating);
                            break;
                        }
                    case 5:
                        if (SettingsOperation.this.controlMode >= 0) {
                            if (SettingsOperation.this.controlMode == 0) {
                                viewHolder.titleTextView.setText(C0380R.string.anti_cross_load);
                            } else {
                                viewHolder.titleTextView.setText(C0380R.string.axle_equalization);
                            }
                        } else {
                            viewHolder.detailTextView.setText(C0380R.string.updating);
                        }
                        if (SettingsOperation.this.equalize >= 0) {
                            if (SettingsOperation.this.equalize == 0) {
                                viewHolder.detailTextView.setText(C0380R.string.Off);
                                break;
                            } else {
                                viewHolder.detailTextView.setText(C0380R.string.On);
                                break;
                            }
                        } else {
                            viewHolder.detailTextView.setText(C0380R.string.updating);
                            break;
                        }
                    case 6:
                        if (SettingsOperation.this.mCommService.alp3Device.displaySettings.ShowMode) {
                            viewHolder.detailTextView.setText(C0380R.string.On);
                            break;
                        } else {
                            viewHolder.detailTextView.setText(C0380R.string.Off);
                            break;
                        }
                }
            } else {
                viewHolder.detailTextView.setText(C0380R.string.updating);
            }
            return view;
        }
    }

    /* renamed from: com.airliftcompany.alp3.settings.SettingsOperation$19 */
    static /* synthetic */ class C050919 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$settings$SettingsOperation$DisplayRowEnum */
        static final /* synthetic */ int[] f67xc8f7cb7d;

        static {
            int[] iArr = new int[DisplayRowEnum.values().length];
            f67xc8f7cb7d = iArr;
            try {
                iArr[DisplayRowEnum.OperationRiseOnStartRow.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f67xc8f7cb7d[DisplayRowEnum.OperationPreSetMaintainRow.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f67xc8f7cb7d[DisplayRowEnum.OperationMinDrivingHeightRow.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f67xc8f7cb7d[DisplayRowEnum.OperationPressureHeightRow.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                f67xc8f7cb7d[DisplayRowEnum.OperationAxleEqualizationRow.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                f67xc8f7cb7d[DisplayRowEnum.OperationShowModeRow.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
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
        switch (C050919.f67xc8f7cb7d[DisplayRowEnum.values()[i].ordinal()]) {
            case 1:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View inflate = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
                TextView textView = (TextView) inflate.findViewById(C0380R.id.dialogTitleTextView);
                TextView textView2 = (TextView) inflate.findViewById(C0380R.id.dialogSubTitleTextView);
                textView.setText(C0380R.string.Rise_On_Start);
                textView2.setText(C0380R.string.rise_on_start_brings_vehicle_to_ride_height_at_key_on);
                builder.setCustomTitle(inflate);
                this.mSelectedItem = this.riseOnStart;
                builder.setSingleChoiceItems(new CharSequence[]{getString(C0380R.string.Off), getString(C0380R.string.On)}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.3
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        SettingsOperation.this.mSelectedItem = i2;
                    }
                }).setPositiveButton(C0380R.string.Save, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.2
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        dialogInterface.dismiss();
                        new TXController.WriteVariableAsyncTask(SettingsOperation.this.mCommService.txController, (short) 9, (short) 3, SettingsOperation.this.mSelectedItem, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.2.1
                            @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                            public void onTaskProgressUpdate(Integer num) {
                            }

                            @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                            public void onTaskCompleted(Boolean bool) {
                                if (bool.booleanValue()) {
                                    SettingsOperation.this.riseOnStart = SettingsOperation.this.mSelectedItem;
                                    SettingsOperation.this.mDisplayListAdapter.notifyDataSetChanged();
                                    Toast.makeText(SettingsOperation.this, C0380R.string.Success, 0).show();
                                    return;
                                }
                                Toast.makeText(SettingsOperation.this, C0380R.string.error_communicating_with_manifold, 1).show();
                            }
                        }).execute(new Void[0]);
                    }
                }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.1
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
                break;
            case 2:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                View inflate2 = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
                TextView textView3 = (TextView) inflate2.findViewById(C0380R.id.dialogTitleTextView);
                TextView textView4 = (TextView) inflate2.findViewById(C0380R.id.dialogSubTitleTextView);
                textView3.setText(C0380R.string.pre_set_maintain);
                textView4.setText(C0380R.string.when_enabled_the_pre_set_maintain_will_maintain_height);
                builder2.setCustomTitle(inflate2);
                this.mSelectedItem = this.preSetMaintain;
                builder2.setSingleChoiceItems(new CharSequence[]{getString(C0380R.string.Off), getString(C0380R.string.On)}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.6
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        SettingsOperation.this.mSelectedItem = i2;
                    }
                }).setPositiveButton(C0380R.string.Save, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.5
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        dialogInterface.dismiss();
                        new TXController.WriteVariableAsyncTask(SettingsOperation.this.mCommService.txController, (short) 9, (short) 2, SettingsOperation.this.mSelectedItem, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.5.1
                            @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                            public void onTaskProgressUpdate(Integer num) {
                            }

                            @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                            public void onTaskCompleted(Boolean bool) {
                                if (bool.booleanValue()) {
                                    SettingsOperation.this.preSetMaintain = SettingsOperation.this.mSelectedItem;
                                    SettingsOperation.this.mDisplayListAdapter.notifyDataSetChanged();
                                    Toast.makeText(SettingsOperation.this, C0380R.string.Success, 0).show();
                                    return;
                                }
                                Toast.makeText(SettingsOperation.this, C0380R.string.error_communicating_with_manifold, 1).show();
                            }
                        }).execute(new Void[0]);
                    }
                }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.4
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        dialogInterface.dismiss();
                    }
                });
                builder2.create().show();
                break;
            case 3:
                AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
                int i2 = this.minDriveHeight;
                if (i2 > 0) {
                    this.mSelectedItem = i2 / 10;
                } else {
                    this.mSelectedItem = 0;
                }
                View inflate3 = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
                TextView textView5 = (TextView) inflate3.findViewById(C0380R.id.dialogTitleTextView);
                TextView textView6 = (TextView) inflate3.findViewById(C0380R.id.dialogSubTitleTextView);
                textView5.setText(C0380R.string.min_driving_height);
                textView6.setText(C0380R.string.sets_the_minimum_vehicle_ride_height);
                builder3.setCustomTitle(inflate3);
                RelativeLayout relativeLayout = new RelativeLayout(this);
                NumberPicker numberPicker = new NumberPicker(this);
                numberPicker.setMaxValue(100);
                numberPicker.setMinValue(0);
                numberPicker.setFormatter(new NumberPicker.Formatter() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.7
                    @Override // android.widget.NumberPicker.Formatter
                    public String format(int i3) {
                        return SettingsOperation.this.mCommService.alp3Device.canpng65300ECUStatus.pressureMode() ? String.format("%d PSI", Integer.valueOf(i3)) : String.format("%d %%", Integer.valueOf(i3));
                    }
                });
                numberPicker.setValue(this.mSelectedItem);
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
                ViewGroup.LayoutParams layoutParams = new RelativeLayout.LayoutParams(50, 50);
                RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
                layoutParams2.addRule(14);
                relativeLayout.setLayoutParams(layoutParams);
                relativeLayout.addView(numberPicker, layoutParams2);
                builder3.setView(relativeLayout).setPositiveButton(C0380R.string.Save, new DialogInterfaceOnClickListenerC05179(numberPicker)).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.8
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i3) {
                        dialogInterface.dismiss();
                    }
                });
                builder3.create().show();
                break;
            case 4:
                AlertDialog.Builder builder4 = new AlertDialog.Builder(this);
                View inflate4 = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
                TextView textView7 = (TextView) inflate4.findViewById(C0380R.id.dialogTitleTextView);
                TextView textView8 = (TextView) inflate4.findViewById(C0380R.id.dialogSubTitleTextView);
                textView7.setText(C0380R.string.pressure_height_mode);
                textView8.setText(C0380R.string.system_to_operate_in_pressure_or_height_control_mode);
                builder4.setCustomTitle(inflate4);
                this.mSelectedItem = this.controlMode;
                builder4.setSingleChoiceItems(new CharSequence[]{getString(C0380R.string.Height), getString(C0380R.string.Pressure)}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.12
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i3) {
                        SettingsOperation.this.mSelectedItem = i3;
                    }
                }).setPositiveButton(C0380R.string.Save, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.11
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i3) {
                        dialogInterface.dismiss();
                        new TXController.WriteVariableAsyncTask(SettingsOperation.this.mCommService.txController, (short) 9, (short) 1, SettingsOperation.this.mSelectedItem, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.11.1
                            @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                            public void onTaskProgressUpdate(Integer num) {
                            }

                            @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                            public void onTaskCompleted(Boolean bool) {
                                if (bool.booleanValue()) {
                                    SettingsOperation.this.controlMode = SettingsOperation.this.mSelectedItem;
                                    SettingsOperation.this.mDisplayListAdapter.notifyDataSetChanged();
                                    Toast.makeText(SettingsOperation.this, C0380R.string.Success, 0).show();
                                    return;
                                }
                                Toast.makeText(SettingsOperation.this, C0380R.string.error_communicating_with_manifold, 1).show();
                            }
                        }).execute(new Void[0]);
                    }
                }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.10
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i3) {
                        dialogInterface.dismiss();
                    }
                });
                builder4.create().show();
                break;
            case 5:
                AlertDialog.Builder builder5 = new AlertDialog.Builder(this);
                this.mSelectedItem = this.equalize;
                View inflate5 = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
                TextView textView9 = (TextView) inflate5.findViewById(C0380R.id.dialogTitleTextView);
                TextView textView10 = (TextView) inflate5.findViewById(C0380R.id.dialogSubTitleTextView);
                if (this.mCommService.alp3Device.canpng65300ECUStatus.pressureMode()) {
                    textView9.setText(C0380R.string.axle_equalization);
                    textView10.setText(C0380R.string.equalize_the_pressure_across_axles);
                } else {
                    textView9.setText(C0380R.string.anti_cross_load);
                    textView10.setText(C0380R.string.equalize_the_axles_to_prevent_cross_load);
                }
                builder5.setCustomTitle(inflate5);
                builder5.setSingleChoiceItems(new CharSequence[]{getString(C0380R.string.Off), getString(C0380R.string.On)}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.15
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i3) {
                        SettingsOperation.this.mSelectedItem = i3;
                    }
                }).setPositiveButton(C0380R.string.Save, new DialogInterfaceOnClickListenerC050414()).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.13
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i3) {
                        dialogInterface.dismiss();
                    }
                });
                builder5.create().show();
                break;
            case 6:
                AlertDialog.Builder builder6 = new AlertDialog.Builder(this);
                View inflate6 = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
                TextView textView11 = (TextView) inflate6.findViewById(C0380R.id.dialogTitleTextView);
                TextView textView12 = (TextView) inflate6.findViewById(C0380R.id.dialogSubTitleTextView);
                textView11.setText(C0380R.string.show_mode);
                textView12.setText(C0380R.string.enables_show_mode);
                builder6.setCustomTitle(inflate6);
                this.mSelectedItem = this.mCommService.alp3Device.displaySettings.ShowMode ? 1 : 0;
                builder6.setSingleChoiceItems(new CharSequence[]{getString(C0380R.string.Off), getString(C0380R.string.On)}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.18
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i3) {
                        SettingsOperation.this.mSelectedItem = i3;
                    }
                }).setPositiveButton(C0380R.string.Save, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.17
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i3) {
                        dialogInterface.dismiss();
                        new TXController.WriteVariableAsyncTask(SettingsOperation.this.mCommService.txController, (short) 9, (short) 7, SettingsOperation.this.mSelectedItem, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.17.1
                            @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                            public void onTaskProgressUpdate(Integer num) {
                            }

                            @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                            public void onTaskCompleted(Boolean bool) {
                                if (bool.booleanValue()) {
                                    SettingsOperation.this.mCommService.alp3Device.displaySettings.ShowMode = SettingsOperation.this.mSelectedItem > 0;
                                    SettingsOperation.this.mDisplayListAdapter.notifyDataSetChanged();
                                    Toast.makeText(SettingsOperation.this, C0380R.string.Success, 0).show();
                                    return;
                                }
                                Toast.makeText(SettingsOperation.this, C0380R.string.error_communicating_with_manifold, 1).show();
                            }
                        }).execute(new Void[0]);
                    }
                }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.16
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i3) {
                        dialogInterface.dismiss();
                    }
                });
                builder6.create().show();
                break;
        }
    }

    /* renamed from: com.airliftcompany.alp3.settings.SettingsOperation$9 */
    class DialogInterfaceOnClickListenerC05179 implements DialogInterface.OnClickListener {
        final /* synthetic */ NumberPicker val$numberPicker;

        DialogInterfaceOnClickListenerC05179(NumberPicker numberPicker) {
            this.val$numberPicker = numberPicker;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            SettingsOperation.this.mSelectedItem = this.val$numberPicker.getValue() * 10;
            final TXController.WriteVariableAsyncTask writeVariableAsyncTask = new TXController.WriteVariableAsyncTask(SettingsOperation.this.mCommService.txController, (short) 9, (short) 4, SettingsOperation.this.mSelectedItem, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.9.1
                @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                public void onTaskProgressUpdate(Integer num) {
                }

                @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                public void onTaskCompleted(Boolean bool) {
                    if (bool.booleanValue()) {
                        SettingsOperation.this.minDriveHeight = SettingsOperation.this.mSelectedItem;
                        new TXController.WriteVariableAsyncTask(SettingsOperation.this.mCommService.txController, (short) 18, (short) 13, 1L, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.9.1.1
                            @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                            public void onTaskProgressUpdate(Integer num) {
                            }

                            @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                            public void onTaskCompleted(Boolean bool2) {
                                if (bool2.booleanValue()) {
                                    SettingsOperation.this.mDisplayListAdapter.notifyDataSetChanged();
                                    Toast.makeText(SettingsOperation.this, C0380R.string.Success, 0).show();
                                } else {
                                    Toast.makeText(SettingsOperation.this, C0380R.string.error_communicating_with_manifold, 1).show();
                                }
                            }
                        }).execute(new Void[0]);
                        return;
                    }
                    Toast.makeText(SettingsOperation.this, C0380R.string.error_communicating_with_manifold, 1).show();
                }
            });
            if (SettingsOperation.this.mSelectedItem < 250) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsOperation.this);
                builder.setTitle(SettingsOperation.this.getString(C0380R.string.Caution)).setPositiveButton(C0380R.string.OK, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.9.3
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface2, int i2) {
                        dialogInterface2.dismiss();
                        writeVariableAsyncTask.execute(new Void[0]);
                    }
                }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.9.2
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface2, int i2) {
                        dialogInterface2.dismiss();
                    }
                });
                builder.setMessage(SettingsOperation.this.getString(C0380R.string.min_driving_height_below_25_may_cause_vehicle_damage));
                builder.create().show();
                return;
            }
            writeVariableAsyncTask.execute(new Void[0]);
        }
    }

    /* renamed from: com.airliftcompany.alp3.settings.SettingsOperation$14 */
    class DialogInterfaceOnClickListenerC050414 implements DialogInterface.OnClickListener {
        DialogInterfaceOnClickListenerC050414() {
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            final TXController.WriteVariableAsyncTask writeVariableAsyncTask = new TXController.WriteVariableAsyncTask(SettingsOperation.this.mCommService.txController, (short) 9, (short) 6, SettingsOperation.this.mSelectedItem, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.14.1
                @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                public void onTaskProgressUpdate(Integer num) {
                }

                @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                public void onTaskCompleted(Boolean bool) {
                    if (bool.booleanValue()) {
                        SettingsOperation.this.equalize = SettingsOperation.this.mSelectedItem;
                        new TXController.WriteVariableAsyncTask(SettingsOperation.this.mCommService.txController, (short) 18, (short) 13, 1L, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.14.1.1
                            @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                            public void onTaskProgressUpdate(Integer num) {
                            }

                            @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                            public void onTaskCompleted(Boolean bool2) {
                                if (bool2.booleanValue()) {
                                    SettingsOperation.this.mDisplayListAdapter.notifyDataSetChanged();
                                    Toast.makeText(SettingsOperation.this, C0380R.string.Success, 0).show();
                                } else {
                                    Toast.makeText(SettingsOperation.this, C0380R.string.error_communicating_with_manifold, 1).show();
                                }
                            }
                        }).execute(new Void[0]);
                        return;
                    }
                    Toast.makeText(SettingsOperation.this, C0380R.string.error_communicating_with_manifold, 1).show();
                }
            });
            if (SettingsOperation.this.mSelectedItem == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsOperation.this);
                builder.setTitle(SettingsOperation.this.getString(C0380R.string.Caution)).setPositiveButton(C0380R.string.OK, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.14.3
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface2, int i2) {
                        dialogInterface2.dismiss();
                        writeVariableAsyncTask.execute(new Void[0]);
                    }
                }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsOperation.14.2
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface2, int i2) {
                        dialogInterface2.dismiss();
                    }
                });
                if (SettingsOperation.this.mCommService.alp3Device.canpng65300ECUStatus.pressureMode()) {
                    builder.setMessage(SettingsOperation.this.getString(C0380R.string.disabling_axle_equalization_may_change_vehicle_handling));
                } else {
                    builder.setMessage(SettingsOperation.this.getString(C0380R.string.disabling_anti_cross_load_may_change_vehicle_handling));
                }
                builder.create().show();
                return;
            }
            writeVariableAsyncTask.execute(new Void[0]);
        }
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity
    public void commServiceConnected() {
        super.commServiceConnected();
        this.mCommService.setCommServiceListener(this);
        if (this.mCommService.commStatus.BlueToothAccConnected) {
            new DownloadVariablesAsyncTask(this).execute(new Void[0]);
        }
        this.mDisplayListAdapter.removeAllItems();
        this.mDisplayListAdapter.addItem(getString(C0380R.string.Rise_On_Start));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.pre_set_maintain));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.min_driving_height));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.pressure_height_mode));
        if (this.mCommService.alp3Device.canpng65300ECUStatus.pressureMode()) {
            this.mDisplayListAdapter.addItem(getString(C0380R.string.axle_equalization));
        } else {
            this.mDisplayListAdapter.addItem(getString(C0380R.string.anti_cross_load));
        }
        this.mDisplayListAdapter.addItem(getString(C0380R.string.show_mode));
        this.mDisplayListAdapter.notifyDataSetChanged();
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity
    public void updateUI() {
        super.updateUI();
    }
}
