package com.airliftcompany.alp3.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.airliftcompany.alp3.comm.ALP3Device;
import com.airliftcompany.alp3.comm.TXController;
import com.airliftcompany.alp3.custom.ALP3ListActivity;
import com.airliftcompany.alp3.utils.ALP3Preferences;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class SettingsDisplay extends ALP3ListActivity {
    private static final String TAG = "SettingsDisplay";
    DisplayListAdapter mDisplayListAdapter;
    private int mSelectedItem;

    public enum DisplayRowEnum {
        DisplayPreventSleepRow,
        DisplayAllUpRow,
        DisplayAllDownRow,
        DisplayUnitsRow,
        DisplayBleFilteringRow
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0380R.layout.activity_settings_home);
        DisplayListAdapter displayListAdapter = new DisplayListAdapter();
        this.mDisplayListAdapter = displayListAdapter;
        setListAdapter(displayListAdapter);
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(C0380R.string.Display));
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

    private class DisplayListAdapter extends BaseAdapter {
        private final ArrayList<String> listValues = new ArrayList<>();
        private final LayoutInflater mInflator;

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        public DisplayListAdapter() {
            this.mInflator = SettingsDisplay.this.getLayoutInflater();
        }

        public void addItem(String str) {
            this.listValues.add(str);
        }

        public void clear() {
            this.listValues.clear();
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
                view = this.mInflator.inflate(C0380R.layout.listitem_settings, (ViewGroup) SettingsDisplay.this.getListView(), false);
                viewHolder = new ViewHolder();
                viewHolder.titleTextView = (TextView) view.findViewById(C0380R.id.title_text_view);
                viewHolder.detailTextView = (TextView) view.findViewById(C0380R.id.detail_text_view);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.titleTextView.setText(getItem(i));
            if (SettingsDisplay.this.mCommService == null) {
                viewHolder.detailTextView.setText(C0380R.string.updating);
            } else {
                int i2 = C048416.f65x42d0c482[DisplayRowEnum.values()[i].ordinal()];
                if (i2 != 1) {
                    if (i2 == 2) {
                        int i3 = C048416.f64xbea6593f[SettingsDisplay.this.mCommService.alp3Device.displaySettings.allUpButtonsEnum().ordinal()];
                        if (i3 == 1) {
                            viewHolder.detailTextView.setText(C0380R.string.All_Up);
                        } else if (i3 == 2) {
                            viewHolder.detailTextView.setText(C0380R.string.Front_Up);
                        } else if (i3 == 3) {
                            viewHolder.detailTextView.setText(C0380R.string.Preset);
                        }
                    } else if (i2 == 3) {
                        int i4 = C048416.f63xa7143ad8[SettingsDisplay.this.mCommService.alp3Device.displaySettings.allDownButtonsEnum().ordinal()];
                        if (i4 == 1) {
                            viewHolder.detailTextView.setText(C0380R.string.All_Down);
                        } else if (i4 == 2) {
                            viewHolder.detailTextView.setText(C0380R.string.Front_Down);
                        } else if (i4 == 3) {
                            viewHolder.detailTextView.setText(C0380R.string.Preset);
                        } else if (i4 == 4) {
                            viewHolder.detailTextView.setText(C0380R.string.Air_Out);
                        }
                    } else if (i2 != 4) {
                        if (i2 == 5) {
                            if (ALP3Preferences.bleFiltering(SettingsDisplay.this.getApplicationContext()).booleanValue()) {
                                viewHolder.detailTextView.setText(C0380R.string.On);
                            } else {
                                viewHolder.detailTextView.setText(C0380R.string.Off);
                            }
                        }
                    } else if (SettingsDisplay.this.mCommService.alp3Device.displaySettings.Units > 0) {
                        viewHolder.detailTextView.setText(C0380R.string.BAR);
                    } else {
                        viewHolder.detailTextView.setText(C0380R.string.PSI);
                    }
                } else if (ALP3Preferences.preventSleep(SettingsDisplay.this.getApplicationContext()).booleanValue()) {
                    viewHolder.detailTextView.setText(C0380R.string.On);
                } else {
                    viewHolder.detailTextView.setText(C0380R.string.Off);
                }
            }
            return view;
        }
    }

    /* renamed from: com.airliftcompany.alp3.settings.SettingsDisplay$16 */
    static /* synthetic */ class C048416 {

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$comm$ALP3Device$KeypadAllDownButtonsEnum */
        static final /* synthetic */ int[] f63xa7143ad8;

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$comm$ALP3Device$KeypadAllUpButtonsEnum */
        static final /* synthetic */ int[] f64xbea6593f;

        /* renamed from: $SwitchMap$com$airliftcompany$alp3$settings$SettingsDisplay$DisplayRowEnum */
        static final /* synthetic */ int[] f65x42d0c482;

        static {
            int[] iArr = new int[DisplayRowEnum.values().length];
            f65x42d0c482 = iArr;
            try {
                iArr[DisplayRowEnum.DisplayPreventSleepRow.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f65x42d0c482[DisplayRowEnum.DisplayAllUpRow.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f65x42d0c482[DisplayRowEnum.DisplayAllDownRow.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f65x42d0c482[DisplayRowEnum.DisplayUnitsRow.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                f65x42d0c482[DisplayRowEnum.DisplayBleFilteringRow.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            int[] iArr2 = new int[ALP3Device.KeypadAllDownButtonsEnum.values().length];
            f63xa7143ad8 = iArr2;
            try {
                iArr2[ALP3Device.KeypadAllDownButtonsEnum.KEYPAD_ALL_DOWN.ordinal()] = 1;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                f63xa7143ad8[ALP3Device.KeypadAllDownButtonsEnum.KEYPAD_FRONT_DOWN.ordinal()] = 2;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                f63xa7143ad8[ALP3Device.KeypadAllDownButtonsEnum.KEYPAD_ALL_DOWN_IS_PRESET.ordinal()] = 3;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                f63xa7143ad8[ALP3Device.KeypadAllDownButtonsEnum.KEYPAD_AIR_OUT.ordinal()] = 4;
            } catch (NoSuchFieldError unused9) {
            }
            int[] iArr3 = new int[ALP3Device.KeypadAllUpButtonsEnum.values().length];
            f64xbea6593f = iArr3;
            try {
                iArr3[ALP3Device.KeypadAllUpButtonsEnum.KEYPAD_ALL_UP.ordinal()] = 1;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                f64xbea6593f[ALP3Device.KeypadAllUpButtonsEnum.KEYPAD_FRONT_UP.ordinal()] = 2;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                f64xbea6593f[ALP3Device.KeypadAllUpButtonsEnum.KEYPAD_ALL_UP_IS_PRESET.ordinal()] = 3;
            } catch (NoSuchFieldError unused12) {
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
        int i2 = C048416.f65x42d0c482[DisplayRowEnum.values()[i].ordinal()];
        if (i2 == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View inflate = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
            TextView textView = (TextView) inflate.findViewById(C0380R.id.dialogTitleTextView);
            TextView textView2 = (TextView) inflate.findViewById(C0380R.id.dialogSubTitleTextView);
            textView.setText(C0380R.string.display_sleep);
            textView2.setText(C0380R.string.prevent_display_sleeping);
            builder.setCustomTitle(inflate);
            this.mSelectedItem = ALP3Preferences.preventSleep(getApplicationContext()).booleanValue() ? 1 : 0;
            builder.setSingleChoiceItems(new CharSequence[]{getString(C0380R.string.Off), getString(C0380R.string.On)}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.3
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    SettingsDisplay.this.mSelectedItem = i3;
                }
            }).setPositiveButton(C0380R.string.Save, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    if (SettingsDisplay.this.mSelectedItem != 1) {
                        if (SettingsDisplay.this.mSelectedItem == 0) {
                            ALP3Preferences.setPreventSleep(false, SettingsDisplay.this.getApplicationContext());
                        }
                    } else {
                        ALP3Preferences.setPreventSleep(true, SettingsDisplay.this.getApplicationContext());
                    }
                    SettingsDisplay.this.mDisplayListAdapter.notifyDataSetChanged();
                    dialogInterface.dismiss();
                }
            }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    dialogInterface.dismiss();
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
            textView3.setText(C0380R.string.all_up_button);
            textView4.setText(C0380R.string.select_function_for_the_all_up_button);
            builder2.setCustomTitle(inflate2);
            this.mSelectedItem = this.mCommService.alp3Device.displaySettings.AllUp;
            builder2.setSingleChoiceItems(new CharSequence[]{getString(C0380R.string.All_Up), getString(C0380R.string.Front_Up), getString(C0380R.string.Preset)}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.6
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    SettingsDisplay.this.mSelectedItem = i3;
                }
            }).setPositiveButton(C0380R.string.Save, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.5
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    dialogInterface.dismiss();
                    new TXController.WriteVariableAsyncTask(SettingsDisplay.this.mCommService.txController, (short) 12, (short) 8, SettingsDisplay.this.mSelectedItem, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.5.1
                        @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                        public void onTaskProgressUpdate(Integer num) {
                        }

                        @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                        public void onTaskCompleted(Boolean bool) {
                            if (bool.booleanValue()) {
                                SettingsDisplay.this.mCommService.alp3Device.displaySettings.AllUp = SettingsDisplay.this.mSelectedItem;
                                SettingsDisplay.this.mDisplayListAdapter.notifyDataSetChanged();
                                Toast.makeText(SettingsDisplay.this, C0380R.string.Success, 0).show();
                                return;
                            }
                            Toast.makeText(SettingsDisplay.this, C0380R.string.error_communicating_with_manifold, 1).show();
                        }
                    }).execute(new Void[0]);
                }
            }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.4
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
            textView5.setText(C0380R.string.all_down_button);
            textView6.setText(C0380R.string.select_function_for_the_all_down_button);
            builder3.setCustomTitle(inflate3);
            this.mSelectedItem = this.mCommService.alp3Device.displaySettings.AllDown;
            builder3.setSingleChoiceItems(new CharSequence[]{getString(C0380R.string.All_Down), getString(C0380R.string.Front_Down), getString(C0380R.string.Preset), getString(C0380R.string.Air_Out)}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.9
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    SettingsDisplay.this.mSelectedItem = i3;
                }
            }).setPositiveButton(C0380R.string.Save, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.8
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    dialogInterface.dismiss();
                    new TXController.WriteVariableAsyncTask(SettingsDisplay.this.mCommService.txController, (short) 12, (short) 9, SettingsDisplay.this.mSelectedItem, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.8.1
                        @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                        public void onTaskProgressUpdate(Integer num) {
                        }

                        @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                        public void onTaskCompleted(Boolean bool) {
                            if (bool.booleanValue()) {
                                SettingsDisplay.this.mCommService.alp3Device.displaySettings.AllDown = SettingsDisplay.this.mSelectedItem;
                                SettingsDisplay.this.mDisplayListAdapter.notifyDataSetChanged();
                                Toast.makeText(SettingsDisplay.this, C0380R.string.Success, 0).show();
                                return;
                            }
                            Toast.makeText(SettingsDisplay.this, C0380R.string.error_communicating_with_manifold, 1).show();
                        }
                    }).execute(new Void[0]);
                }
            }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.7
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    dialogInterface.dismiss();
                }
            });
            builder3.create().show();
            return;
        }
        if (i2 == 4) {
            AlertDialog.Builder builder4 = new AlertDialog.Builder(this);
            View inflate4 = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
            TextView textView7 = (TextView) inflate4.findViewById(C0380R.id.dialogTitleTextView);
            TextView textView8 = (TextView) inflate4.findViewById(C0380R.id.dialogSubTitleTextView);
            textView7.setText(C0380R.string.Units);
            textView8.setText(C0380R.string.system_display_in_psi_or_bar);
            builder4.setCustomTitle(inflate4);
            this.mSelectedItem = this.mCommService.alp3Device.displaySettings.Units;
            builder4.setSingleChoiceItems(new CharSequence[]{getString(C0380R.string.PSI), getString(C0380R.string.BAR)}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.12
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    SettingsDisplay.this.mSelectedItem = i3;
                }
            }).setPositiveButton(C0380R.string.Save, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.11
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    dialogInterface.dismiss();
                    new TXController.WriteVariableAsyncTask(SettingsDisplay.this.mCommService.txController, (short) 12, (short) 7, SettingsDisplay.this.mSelectedItem, new TXController.AsyncTaskListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.11.1
                        @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                        public void onTaskProgressUpdate(Integer num) {
                        }

                        @Override // com.airliftcompany.alp3.comm.TXController.AsyncTaskListener
                        public void onTaskCompleted(Boolean bool) {
                            if (bool.booleanValue()) {
                                SettingsDisplay.this.mCommService.alp3Device.displaySettings.Units = SettingsDisplay.this.mSelectedItem;
                                SettingsDisplay.this.mDisplayListAdapter.notifyDataSetChanged();
                                Toast.makeText(SettingsDisplay.this, C0380R.string.Success, 0).show();
                                return;
                            }
                            Toast.makeText(SettingsDisplay.this, C0380R.string.error_communicating_with_manifold, 1).show();
                        }
                    }).execute(new Void[0]);
                }
            }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.10
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    dialogInterface.dismiss();
                }
            });
            builder4.create().show();
            return;
        }
        if (i2 != 5) {
            return;
        }
        AlertDialog.Builder builder5 = new AlertDialog.Builder(this);
        View inflate5 = getLayoutInflater().inflate(C0380R.layout.dialog_custom_title, (ViewGroup) null);
        TextView textView9 = (TextView) inflate5.findViewById(C0380R.id.dialogTitleTextView);
        TextView textView10 = (TextView) inflate5.findViewById(C0380R.id.dialogSubTitleTextView);
        textView9.setText("BLE Device Filtering");
        textView10.setText("Enable BLE device filtering");
        builder5.setCustomTitle(inflate5);
        this.mSelectedItem = ALP3Preferences.bleFiltering(getApplicationContext()).booleanValue() ? 1 : 0;
        builder5.setSingleChoiceItems(new CharSequence[]{getString(C0380R.string.Off), getString(C0380R.string.On)}, this.mSelectedItem, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.15
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i3) {
                SettingsDisplay.this.mSelectedItem = i3;
            }
        }).setPositiveButton(C0380R.string.Save, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.14
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i3) {
                if (SettingsDisplay.this.mSelectedItem != 1) {
                    if (SettingsDisplay.this.mSelectedItem == 0) {
                        ALP3Preferences.setBleFiltering(false, SettingsDisplay.this.getApplicationContext());
                    }
                } else {
                    ALP3Preferences.setBleFiltering(true, SettingsDisplay.this.getApplicationContext());
                }
                SettingsDisplay.this.mDisplayListAdapter.notifyDataSetChanged();
                dialogInterface.dismiss();
            }
        }).setNegativeButton(C0380R.string.Cancel, new DialogInterface.OnClickListener() { // from class: com.airliftcompany.alp3.settings.SettingsDisplay.13
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i3) {
                dialogInterface.dismiss();
            }
        });
        builder5.create().show();
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity
    public void commServiceConnected() {
        super.commServiceConnected();
        this.mCommService.setCommServiceListener(this);
        this.mDisplayListAdapter.addItem(getString(C0380R.string.prevent_display_sleeping));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.all_up_button));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.all_down_button));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.Units));
        this.mDisplayListAdapter.addItem("BLE Device Filtering");
        this.mDisplayListAdapter.notifyDataSetChanged();
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity
    public void updateUI() {
        super.updateUI();
    }
}
