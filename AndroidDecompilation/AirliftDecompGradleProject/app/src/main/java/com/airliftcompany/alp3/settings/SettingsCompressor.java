package com.airliftcompany.alp3.settings;

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
public class SettingsCompressor extends ALP3ListActivity {
    private static final int MIN_TANK_PRESSURE = 150;
    private static final String TAG = "SettingsCompressor";
    DisplayListAdapter mDisplayListAdapter;
    private int mSelectedItem;
    private int maxTankPressure = -1;
    private int tankConfig = -1;
    private int dutyCycle = -1;

    public enum DisplayRowEnum {
        CompressorOnOffRow,
        CompressorEnableDisableRow,
        CompressorDualSingleRow,
        CompressorMaxPressureRow,
        CompressorDutyCycleRow
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0380R.layout.activity_settings_home);
        DisplayListAdapter displayListAdapter = new DisplayListAdapter();
        this.mDisplayListAdapter = displayListAdapter;
        setListAdapter(displayListAdapter);
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(C0380R.string.Compressor));
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
            progressDialog.setMessage(SettingsCompressor.this.getString(C0380R.string.updating));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setProgressStyle(1);
            this.progressDialog.getWindow().setFlags(8, 8);
            this.progressDialog.show();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Boolean doInBackground(Void... voidArr) {
            SettingsCompressor.this.mCommService.com100msState = CommService.com100msEnum.COM100mS_PAUSE;
            if (SettingsCompressor.this.mCommService.txController.txGetRecord(3, 2)) {
                SettingsCompressor settingsCompressor = SettingsCompressor.this;
                settingsCompressor.maxTankPressure = (int) settingsCompressor.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                publishProgress(20);
                if (SettingsCompressor.this.mCommService.txController.txGetRecord(3, 6)) {
                    SettingsCompressor settingsCompressor2 = SettingsCompressor.this;
                    settingsCompressor2.tankConfig = (int) settingsCompressor2.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                    publishProgress(40);
                    if (SettingsCompressor.this.mCommService.txController.txGetRecord(3, 3)) {
                        float f = (int) SettingsCompressor.this.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                        if (SettingsCompressor.this.mCommService.txController.txGetRecord(3, 4)) {
                            float f2 = (int) SettingsCompressor.this.mCommService.alp3Device.canpng65350ReplyRecord.Variable;
                            int i = (int) ((f2 / (f + f2)) * 100.0f);
                            if (i < 40) {
                                SettingsCompressor.this.dutyCycle = 0;
                            } else if (i < 55) {
                                SettingsCompressor.this.dutyCycle = 1;
                            } else if (i < 70) {
                                SettingsCompressor.this.dutyCycle = 2;
                            } else if (i < 80) {
                                SettingsCompressor.this.dutyCycle = 3;
                            } else if (i < 110) {
                                SettingsCompressor.this.dutyCycle = 4;
                            }
                            publishProgress(60);
                            SettingsCompressor.this.mCommService.com100msState = CommService.com100msEnum.COM100mS_STATUS;
                            return true;
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
            SettingsCompressor.this.mDisplayListAdapter.notifyDataSetChanged();
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
            this.mInflator = SettingsCompressor.this.getLayoutInflater();
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
                view = this.mInflator.inflate(C0380R.layout.listitem_settings, (ViewGroup) SettingsCompressor.this.getListView(), false);
                viewHolder = new ViewHolder();
                viewHolder.titleTextView = (TextView) view.findViewById(C0380R.id.title_text_view);
                viewHolder.detailTextView = (TextView) view.findViewById(C0380R.id.detail_text_view);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.titleTextView.setText(getItem(i));
            if (SettingsCompressor.this.mCommService == null) {
                viewHolder.detailTextView.setText(C0380R.string.updating);
            } else {
                int i2 = C046815.f62x720115f1[DisplayRowEnum.values()[i].ordinal()];
                if (i2 != 1) {
                    if (i2 != 2) {
                        if (i2 != 3) {
                            if (i2 != 4) {
                                if (i2 == 5) {
                                    int i3 = SettingsCompressor.this.dutyCycle;
                                    if (i3 == 0) {
                                        viewHolder.detailTextView.setText("33%");
                                    } else if (i3 == 1) {
                                        viewHolder.detailTextView.setText("50%");
                                    } else if (i3 == 2) {
                                        viewHolder.detailTextView.setText("66%");
                                    } else if (i3 == 3) {
                                        viewHolder.detailTextView.setText("75%");
                                    } else if (i3 == 4) {
                                        viewHolder.detailTextView.setText("100%");
                                    } else {
                                        viewHolder.detailTextView.setText(C0380R.string.updating);
                                    }
                                }
                            } else if (SettingsCompressor.this.maxTankPressure >= 0) {
                                viewHolder.detailTextView.setText(String.format("%d", Integer.valueOf(SettingsCompressor.this.maxTankPressure / 10)));
                            } else {
                                viewHolder.detailTextView.setText(C0380R.string.updating);
                            }
                        } else if (SettingsCompressor.this.tankConfig >= 0) {
                            if (SettingsCompressor.this.tankConfig == 0) {
                                viewHolder.detailTextView.setText(C0380R.string.Single);
                            } else {
                                viewHolder.detailTextView.setText(C0380R.string.Dual);
                            }
                        } else {
                            viewHolder.detailTextView.setText(C0380R.string.updating);
                        }
                    } else if (SettingsCompressor.this.mCommService.canpng65400UIStatus.CompressorDisabled == 1) {
                        viewHolder.detailTextView.setText(C0380R.string.Disabled);
                    } else {
                        viewHolder.detailTextView.setText(C0380R.string.Enabled);
                    }
                } else if (SettingsCompressor.this.mCommService.canpng65400UIStatus.CompressorForceOn == 1) {
                    viewHolder.detailTextView.setText(C0380R.string.On);
                } else {
                    viewHolder.detailTextView.setText(C0380R.string.Off);
                }
            }
            return view;
        }
    }

    /* renamed from: com.airliftcompany.alp3.settings.SettingsCompressor$15 */
    static /* synthetic */ class C046815 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$settings$SettingsCompressor$DisplayRowEnum */
        static final /* synthetic */ int[] f62x720115f1;

        static {
            int[] iArr = new int[DisplayRowEnum.values().length];
            f62x720115f1 = iArr;
            try {
                iArr[DisplayRowEnum.CompressorOnOffRow.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f62x720115f1[DisplayRowEnum.CompressorEnableDisableRow.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f62x720115f1[DisplayRowEnum.CompressorDualSingleRow.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f62x720115f1[DisplayRowEnum.CompressorMaxPressureRow.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                f62x720115f1[DisplayRowEnum.CompressorDutyCycleRow.ordinal()] = 5;
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
        int i2 = C046815.f62x720115f1[DisplayRowEnum.values()[i].ordinal()];
        if (i2 == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View inflate = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
            TextView textView = (TextView) inflate.findViewById(C0380R.id.dialogTitleTextView);
            TextView textView2 = (TextView) inflate.findViewById(C0380R.id.dialogSubTitleTextView);
            textView.setText(getString(C0380R.string.Compressor) + " " + getString(C0380R.string.on_off));
            textView2.setText(C0380R.string.controls_the_compressor_manually);
            builder.setCustomTitle(inflate);
            this.mSelectedItem = -1;
            builder.setSingleChoiceItems(new CharSequence[]{getString(C0380R.string.Off), getString(C0380R.string.On)}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    if (i3 == 0) {
                        SettingsCompressor.this.mCommService.canpng65400UIStatus.CompressorForceOn = (short) 2;
                    } else {
                        SettingsCompressor.this.mCommService.canpng65400UIStatus.CompressorForceOn = (short) 1;
                    }
                }
            }).setPositiveButton(C0380R.string.OK, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    dialogInterface.dismiss();
                    SettingsCompressor.this.mCommService.canpng65400UIStatus.CompressorForceOn = (short) 0;
                    SettingsCompressor.this.mDisplayListAdapter.notifyDataSetChanged();
                }
            });
            builder.create().show();
            return;
        }
        if (i2 == 2) {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
            View inflate2 = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
            TextView textView3 = (TextView) inflate2.findViewById(C0380R.id.dialogTitleTextView);
            TextView textView4 = (TextView) inflate2.findViewById(C0380R.id.dialogSubTitleTextView);
            textView3.setText(getString(C0380R.string.Compressor) + " " + getString(C0380R.string.enable_disable));
            textView4.setText(C0380R.string.controls_the_compressor_manually);
            builder2.setCustomTitle(inflate2);
            this.mSelectedItem = -1;
            builder2.setSingleChoiceItems(new CharSequence[]{getString(C0380R.string.Enable), getString(C0380R.string.Disable)}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.5
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    SettingsCompressor.this.mSelectedItem = i3;
                }
            }).setPositiveButton(C0380R.string.Save, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.4
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    dialogInterface.dismiss();
                    SettingsCompressor.this.mCommService.canpng65400UIStatus.CompressorDisabled = (short) SettingsCompressor.this.mSelectedItem;
                    SettingsCompressor.this.mDisplayListAdapter.notifyDataSetChanged();
                }
            }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.3
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    dialogInterface.dismiss();
                }
            });
            builder2.create().show();
            return;
        }
        if (i2 == 3) {
            AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
            View inflate3 = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
            TextView textView5 = (TextView) inflate3.findViewById(C0380R.id.dialogTitleTextView);
            TextView textView6 = (TextView) inflate3.findViewById(C0380R.id.dialogSubTitleTextView);
            textView5.setText(getString(C0380R.string.Compressor) + " " + getString(C0380R.string.dual_single));
            textView6.setText(C0380R.string.select_if_a_dual_or_single_compressor_is_installed);
            builder3.setCustomTitle(inflate3);
            this.mSelectedItem = this.tankConfig;
            builder3.setSingleChoiceItems(new CharSequence[]{getString(C0380R.string.Single), getString(C0380R.string.Dual)}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.8
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    SettingsCompressor.this.mSelectedItem = i3;
                }
            }).setPositiveButton(C0380R.string.Save, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.7
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    dialogInterface.dismiss();
                    new TXController.WriteVariableAsyncTask(SettingsCompressor.this.mCommService.txController, (short) 3, (short) 6, SettingsCompressor.this.mSelectedItem, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.7.1
                        @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                        public void onTaskProgressUpdate(Integer num) {
                        }

                        @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                        public void onTaskCompleted(Boolean bool) {
                            if (bool.booleanValue()) {
                                SettingsCompressor.this.tankConfig = SettingsCompressor.this.mSelectedItem;
                                SettingsCompressor.this.mDisplayListAdapter.notifyDataSetChanged();
                                Toast.makeText(SettingsCompressor.this, C0380R.string.Success, 0).show();
                                return;
                            }
                            Toast.makeText(SettingsCompressor.this, C0380R.string.error_communicating_with_manifold, 1).show();
                        }
                    }).execute(new Void[0]);
                }
            }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.6
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    dialogInterface.dismiss();
                }
            });
            builder3.create().show();
            return;
        }
        if (i2 != 4) {
            if (i2 != 5) {
                return;
            }
            AlertDialog.Builder builder4 = new AlertDialog.Builder(this);
            this.mSelectedItem = this.dutyCycle;
            View inflate4 = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
            TextView textView7 = (TextView) inflate4.findViewById(C0380R.id.dialogTitleTextView);
            TextView textView8 = (TextView) inflate4.findViewById(C0380R.id.dialogSubTitleTextView);
            textView7.setText(C0380R.string.duty_cycle);
            textView8.setText(C0380R.string.set_the_compressors_duty_cycle);
            builder4.setCustomTitle(inflate4);
            builder4.setSingleChoiceItems(new CharSequence[]{"33%", "50%", "66%", "75%", "100%"}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.14
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    SettingsCompressor.this.mSelectedItem = i3;
                }
            }).setPositiveButton(C0380R.string.Save, new DialogInterfaceOnClickListenerC046613()).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.12
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    dialogInterface.dismiss();
                }
            });
            builder4.create().show();
            return;
        }
        AlertDialog.Builder builder5 = new AlertDialog.Builder(this);
        if (this.maxTankPressure > 0) {
            this.mSelectedItem = (r12 / 10) - 150;
        } else {
            this.mSelectedItem = 0;
        }
        View inflate5 = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
        TextView textView9 = (TextView) inflate5.findViewById(C0380R.id.dialogTitleTextView);
        TextView textView10 = (TextView) inflate5.findViewById(C0380R.id.dialogSubTitleTextView);
        textView9.setText(C0380R.string.max_pressure);
        textView10.setText(C0380R.string.sets_the_maximum_compressor_pressure);
        builder5.setCustomTitle(inflate5);
        RelativeLayout relativeLayout = new RelativeLayout(this);
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMaxValue(50);
        numberPicker.setMinValue(0);
        numberPicker.setFormatter(new NumberPicker.Formatter() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.9
            @Override // android.widget.NumberPicker.Formatter
            public String format(int i3) {
                return String.format("%d", Integer.valueOf(i3 + SettingsCompressor.MIN_TANK_PRESSURE));
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
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams2.addRule(14);
        relativeLayout.setLayoutParams(layoutParams);
        relativeLayout.addView(numberPicker, layoutParams2);
        builder5.setView(relativeLayout).setPositiveButton(C0380R.string.Save, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.11
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i3) {
                dialogInterface.dismiss();
                SettingsCompressor.this.mSelectedItem = (numberPicker.getValue() + SettingsCompressor.MIN_TANK_PRESSURE) * 10;
                new TXController.WriteVariableAsyncTask(SettingsCompressor.this.mCommService.txController, (short) 3, (short) 2, SettingsCompressor.this.mSelectedItem, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.11.1
                    @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                    public void onTaskProgressUpdate(Integer num) {
                    }

                    @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                    public void onTaskCompleted(Boolean bool) {
                        if (bool.booleanValue()) {
                            SettingsCompressor.this.maxTankPressure = SettingsCompressor.this.mSelectedItem;
                            SettingsCompressor.this.mDisplayListAdapter.notifyDataSetChanged();
                            Toast.makeText(SettingsCompressor.this, C0380R.string.Success, 0).show();
                            return;
                        }
                        Toast.makeText(SettingsCompressor.this, C0380R.string.error_communicating_with_manifold, 1).show();
                    }
                }).execute(new Void[0]);
            }
        }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.10
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i3) {
                dialogInterface.dismiss();
            }
        });
        builder5.create().show();
    }

    /* renamed from: com.airliftcompany.alp3.settings.SettingsCompressor$13 */
    class DialogInterfaceOnClickListenerC046613 implements DialogInterface.OnClickListener {
        DialogInterfaceOnClickListenerC046613() {
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsCompressor.this);
            builder.setTitle(SettingsCompressor.this.getString(C0380R.string.Caution)).setPositiveButton(C0380R.string.OK, new AnonymousClass2()).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.13.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface2, int i2) {
                    dialogInterface2.dismiss();
                }
            });
            builder.setMessage(SettingsCompressor.this.getString(C0380R.string.compressor_damage_possible_refer_to_compressor_mfg_spec));
            builder.create().show();
        }

        /* renamed from: com.airliftcompany.alp3.settings.SettingsCompressor$13$2, reason: invalid class name */
        class AnonymousClass2 implements DialogInterface.OnClickListener {
            AnonymousClass2() {
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                int i2 = SettingsCompressor.this.mSelectedItem;
                int i3 = 900000;
                int i4 = 600000;
                if (i2 == 0) {
                    i3 = 600000;
                    i4 = 1200000;
                } else if (i2 == 1) {
                    i4 = 900000;
                } else if (i2 == 2) {
                    i3 = 1200000;
                } else if (i2 != 3) {
                    i3 = i2 != 4 ? 0 : 1800000;
                    i4 = 0;
                } else {
                    i3 = 1380000;
                    i4 = 420000;
                }
                new TXController.WriteVariableAsyncTask(SettingsCompressor.this.mCommService.txController, (short) 3, (short) 3, i4, new AnonymousClass1(i3)).execute(new Void[0]);
            }

            /* renamed from: com.airliftcompany.alp3.settings.SettingsCompressor$13$2$1, reason: invalid class name */
            class AnonymousClass1 implements TXController.AsyncTaskListener {
                final /* synthetic */ int val$val2final;

                @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                public void onTaskProgressUpdate(Integer num) {
                }

                AnonymousClass1(int i) {
                    this.val$val2final = i;
                }

                @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                public void onTaskCompleted(Boolean bool) {
                    if (bool.booleanValue()) {
                        SettingsCompressor.this.dutyCycle = SettingsCompressor.this.mSelectedItem;
                        new TXController.WriteVariableAsyncTask(SettingsCompressor.this.mCommService.txController, (short) 3, (short) 4, this.val$val2final, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.13.2.1.1
                            @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                            public void onTaskProgressUpdate(Integer num) {
                            }

                            @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                            public void onTaskCompleted(Boolean bool2) {
                                if (bool2.booleanValue()) {
                                    new TXController.WriteVariableAsyncTask(SettingsCompressor.this.mCommService.txController, (short) 18, (short) 15, 1L, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsCompressor.13.2.1.1.1
                                        @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                                        public void onTaskProgressUpdate(Integer num) {
                                        }

                                        @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                                        public void onTaskCompleted(Boolean bool3) {
                                            if (bool3.booleanValue()) {
                                                SettingsCompressor.this.mDisplayListAdapter.notifyDataSetChanged();
                                                Toast.makeText(SettingsCompressor.this, C0380R.string.Success, 0).show();
                                            } else {
                                                Toast.makeText(SettingsCompressor.this, C0380R.string.error_communicating_with_manifold, 1).show();
                                            }
                                        }
                                    }).execute(new Void[0]);
                                } else {
                                    Toast.makeText(SettingsCompressor.this, C0380R.string.error_communicating_with_manifold, 1).show();
                                }
                            }
                        }).execute(new Void[0]);
                        return;
                    }
                    Toast.makeText(SettingsCompressor.this, C0380R.string.error_communicating_with_manifold, 1).show();
                }
            }
        }
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity
    public void commServiceConnected() {
        super.commServiceConnected();
        this.mCommService.setCommServiceListener(this);
        new DownloadVariablesAsyncTask(this).execute(new Void[0]);
        this.mDisplayListAdapter.addItem(getString(C0380R.string.Compressor));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.Compressor));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.dual_single));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.max_pressure));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.duty_cycle));
        this.mDisplayListAdapter.notifyDataSetChanged();
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity
    public void updateUI() {
        super.updateUI();
    }
}
