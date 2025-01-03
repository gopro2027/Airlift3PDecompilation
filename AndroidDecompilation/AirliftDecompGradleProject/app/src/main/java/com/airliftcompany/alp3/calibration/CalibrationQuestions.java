package com.airliftcompany.alp3.calibration;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NavUtils;
import com.airliftcompany.alp3.C0380R;
import com.airliftcompany.alp3.comm.CommService;
import com.airliftcompany.alp3.custom.ALP3ListActivity;
import com.airliftcompany.alp3.custom.CalSwitch;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class CalibrationQuestions extends ALP3ListActivity {
    private static final String TAG = "CalibrationQuestions";
    DisplayListAdapter mDisplayListAdapter;
    private ArrayList<QuestionsRowEnum> mQuestionArrayList;

    public enum QuestionsRowEnum {
        LevelRow,
        FrontRow,
        ObstructionRow,
        MountRow,
        CompressorRow,
        AutoPressureRow,
        HeightRow,
        AutoRow
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0380R.layout.activity_cal_questions);
        this.mQuestionArrayList = new ArrayList<>();
        DisplayListAdapter displayListAdapter = new DisplayListAdapter();
        this.mDisplayListAdapter = displayListAdapter;
        setListAdapter(displayListAdapter);
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(C0380R.string.calibration));
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ((Button) findViewById(C0380R.id.continueButton)).setOnClickListener(new View.OnClickListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationQuestions.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (CalibrationQuestions.this.mQuestionArrayList.contains(QuestionsRowEnum.LevelRow)) {
                    if (CalibrationQuestions.this.mQuestionArrayList.contains(QuestionsRowEnum.FrontRow)) {
                        if (CalibrationQuestions.this.mQuestionArrayList.contains(QuestionsRowEnum.ObstructionRow)) {
                            if (!CalibrationQuestions.this.mQuestionArrayList.contains(QuestionsRowEnum.MountRow)) {
                                Toast.makeText(CalibrationQuestions.this, "Manifold must first be mounted to continue", 1).show();
                                return;
                            } else {
                                CalibrationQuestions calibrationQuestions = CalibrationQuestions.this;
                                calibrationQuestions.new SaveVariablesAsyncTask(calibrationQuestions).execute(new Void[0]);
                                return;
                            }
                        }
                        Toast.makeText(CalibrationQuestions.this, "Vehicle must be free of obstructions to continue", 1).show();
                        return;
                    }
                    Toast.makeText(CalibrationQuestions.this, "Front wheels must be straight to continue", 1).show();
                    return;
                }
                Toast.makeText(CalibrationQuestions.this, "Vehicle must be on level surface to continue", 1).show();
            }
        });
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            mIgnoreOnPause = true;
            this.mCommService.setCommServiceListener(null);
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private class SaveVariablesAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        private Context context;
        ProgressDialog progressDialog;

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onCancelled(Boolean bool) {
        }

        public SaveVariablesAsyncTask(Context context) {
            this.context = context;
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            ProgressDialog progressDialog = new ProgressDialog(this.context);
            this.progressDialog = progressDialog;
            progressDialog.setMessage(CalibrationQuestions.this.getString(C0380R.string.Saving));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setProgressStyle(1);
            this.progressDialog.getWindow().setFlags(8, 8);
            this.progressDialog.show();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Boolean doInBackground(Void... voidArr) {
            CalibrationQuestions.this.mCommService.com100msState = CommService.com100msEnum.COM100mS_PAUSE;
            CalibrationQuestions.this.mCommService.alp3Device.calibrationSettings.PressureOnly = 0;
            if (!CalibrationQuestions.this.mQuestionArrayList.contains(QuestionsRowEnum.HeightRow)) {
                CalibrationQuestions.this.mCommService.alp3Device.calibrationSettings.PressureOnly = 1;
            }
            if (CalibrationQuestions.this.mCommService.txController.txWriteVariable((short) 9, (short) 1, (short) CalibrationQuestions.this.mCommService.alp3Device.calibrationSettings.PressureOnly)) {
                CalibrationQuestions.this.mCommService.alp3Device.calibrationSettings.wizardHeightIsAuto = CalibrationQuestions.this.mQuestionArrayList.contains(QuestionsRowEnum.AutoRow);
                CalibrationQuestions.this.mCommService.alp3Device.calibrationSettings.wizardPressureIsAuto = CalibrationQuestions.this.mQuestionArrayList.contains(QuestionsRowEnum.AutoPressureRow);
                CalibrationQuestions.this.mCommService.alp3Device.calibrationSettings.calibrationCount = 0;
                if (CalibrationQuestions.this.mCommService.alp3Device.calibrationSettings.PressureOnly == 1) {
                    CalibrationQuestions.this.mCommService.alp3Device.calibrationSettings.calibrationCount = 3;
                } else {
                    CalibrationQuestions.this.mCommService.alp3Device.calibrationSettings.calibrationCount = 5;
                }
                if (!CalibrationQuestions.this.mCommService.alp3Device.calibrationSettings.wizardPressureIsAuto) {
                    CalibrationQuestions.this.mCommService.alp3Device.calibrationSettings.calibrationCount++;
                }
                CalibrationQuestions.this.mCommService.alp3Device.calibrationSettings.calibrationStep = 0;
                publishProgress(40);
                if (!CalibrationQuestions.this.mCommService.txController.txWriteVariable((short) 3, (short) 6, CalibrationQuestions.this.mQuestionArrayList.contains(QuestionsRowEnum.CompressorRow) ? 1L : 0L)) {
                    return false;
                }
                publishProgress(80);
                CalibrationQuestions.this.mCommService.com100msState = CommService.com100msEnum.COM100mS_STATUS;
                return true;
            }
            return false;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Integer... numArr) {
            this.progressDialog.setProgress(numArr[0].intValue());
            CalibrationQuestions.this.mDisplayListAdapter.notifyDataSetChanged();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Boolean bool) {
            ProgressDialog progressDialog = this.progressDialog;
            if (progressDialog != null && progressDialog.isShowing()) {
                this.progressDialog.setProgressStyle(0);
                this.progressDialog.dismiss();
            }
            this.progressDialog = null;
            if (!bool.booleanValue()) {
                Toast.makeText(this.context, C0380R.string.error_communicating_with_manifold, 1).show();
                return;
            }
            ALP3ListActivity.mIgnoreOnPause = true;
            Intent intent = new Intent(CalibrationQuestions.this, (Class<?>) CalibrationAccelerometer.class);
            CalibrationQuestions.this.finish();
            CalibrationQuestions.this.startActivity(intent);
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
            this.mInflator = CalibrationQuestions.this.getLayoutInflater();
        }

        public void addItem(String str) {
            this.listValues.add(str);
        }

        public void removeItem(String str) {
            this.listValues.remove(str);
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
            QuestionsRowEnum questionsRowEnum = QuestionsRowEnum.values()[i];
            if (view == null) {
                view = this.mInflator.inflate(C0380R.layout.listitem_cal_question, (ViewGroup) CalibrationQuestions.this.getListView(), false);
                viewHolder = new ViewHolder();
                viewHolder.titleTextView = (TextView) view.findViewById(C0380R.id.titleTextView);
                viewHolder.calSwitch = (CalSwitch) view.findViewById(C0380R.id.calSwitch);
                viewHolder.calSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.airliftcompany.alp3.calibration.CalibrationQuestions.DisplayListAdapter.1
                    @Override // android.widget.CompoundButton.OnCheckedChangeListener
                    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                        QuestionsRowEnum questionsRowEnum2 = (QuestionsRowEnum) compoundButton.getTag();
                        if (z) {
                            if (!CalibrationQuestions.this.mQuestionArrayList.contains(questionsRowEnum2)) {
                                CalibrationQuestions.this.mQuestionArrayList.add(questionsRowEnum2);
                            }
                            if (questionsRowEnum2 != QuestionsRowEnum.HeightRow || DisplayListAdapter.this.listValues.contains(CalibrationQuestions.this.getString(C0380R.string.calibrate_height_manual_or_auto))) {
                                return;
                            }
                            CalibrationQuestions.this.mDisplayListAdapter.addItem(CalibrationQuestions.this.getString(C0380R.string.calibrate_height_manual_or_auto));
                            CalibrationQuestions.this.mDisplayListAdapter.notifyDataSetChanged();
                            CalibrationQuestions.this.getListView().post(new Runnable() { // from class: com.airliftcompany.alp3.calibration.CalibrationQuestions.DisplayListAdapter.1.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    CalibrationQuestions.this.getListView().setSelection(CalibrationQuestions.this.mDisplayListAdapter.getCount() - 1);
                                }
                            });
                            return;
                        }
                        CalibrationQuestions.this.mQuestionArrayList.remove(questionsRowEnum2);
                        if (questionsRowEnum2 == QuestionsRowEnum.HeightRow && DisplayListAdapter.this.listValues.contains(CalibrationQuestions.this.getString(C0380R.string.calibrate_height_manual_or_auto))) {
                            CalibrationQuestions.this.mDisplayListAdapter.removeItem(DisplayListAdapter.this.getItem(QuestionsRowEnum.AutoRow.ordinal()));
                            CalibrationQuestions.this.mDisplayListAdapter.notifyDataSetChanged();
                        }
                    }
                });
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.titleTextView.setText(getItem(i));
            viewHolder.row = questionsRowEnum;
            viewHolder.calSwitch.setTag(questionsRowEnum);
            if (CalibrationQuestions.this.mQuestionArrayList.contains(questionsRowEnum)) {
                viewHolder.calSwitch.setChecked(true);
            } else {
                viewHolder.calSwitch.setChecked(false);
            }
            if (questionsRowEnum == QuestionsRowEnum.CompressorRow) {
                viewHolder.calSwitch.setTextOff("One");
                viewHolder.calSwitch.setTextOn("Two");
            } else if (questionsRowEnum == QuestionsRowEnum.AutoPressureRow) {
                viewHolder.calSwitch.setTextOff("Manual");
                viewHolder.calSwitch.setTextOn("Auto");
            } else if (questionsRowEnum == QuestionsRowEnum.AutoRow) {
                viewHolder.calSwitch.setTextOff("Manual");
                viewHolder.calSwitch.setTextOn("Auto");
            } else {
                viewHolder.calSwitch.setTextOff("No");
                viewHolder.calSwitch.setTextOn("Yes");
            }
            viewHolder.calSwitch.requestLayout();
            return view;
        }
    }

    static class ViewHolder {
        CalSwitch calSwitch;
        QuestionsRowEnum row;
        TextView titleTextView;

        ViewHolder() {
        }
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity
    public void commServiceConnected() {
        super.commServiceConnected();
        this.mCommService.setCommServiceListener(this);
        this.mDisplayListAdapter.addItem(getString(C0380R.string.is_the_vehicle_on_level_surface));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.are_the_front_wheels_straight));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.is_the_vehicle_and_all_wheels_free_from_obstructions));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.is_your_manifold_mounted));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.do_you_have_one_or_two_compressors));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.calibrate_pressure_manual_or_auto));
        this.mDisplayListAdapter.addItem(getString(C0380R.string.do_you_have_height_sensors));
        this.mDisplayListAdapter.notifyDataSetChanged();
    }

    @Override // com.airliftcompany.alp3.custom.ALP3ListActivity
    public void updateUI() {
        super.updateUI();
    }
}
