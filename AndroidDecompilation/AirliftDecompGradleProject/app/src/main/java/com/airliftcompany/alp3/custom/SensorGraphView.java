package com.airliftcompany.alp3.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.internal.view.SupportMenu;
import androidx.core.view.ViewCompat;
import com.airliftcompany.alp3.C0380R;

/* loaded from: classes.dex */
public class SensorGraphView extends View {
    private static final String TAG = "SensorGraphView";
    String mGraphName;
    Paint mGraphPaint;
    Paint mIndicatorPaint;
    Float mPercentValue;
    String mPressure;
    String mStatus;
    Paint mTextPaint;

    public SensorGraphView(Context context) {
        super(context);
        init(context);
    }

    public SensorGraphView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public SensorGraphView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    private void init(Context context) {
        this.mPercentValue = Float.valueOf(0.0f);
        this.mGraphName = "";
        this.mStatus = "";
        this.mPressure = "";
        Paint paint = new Paint(1);
        this.mTextPaint = paint;
        paint.setColor(ViewCompat.MEASURED_STATE_MASK);
        this.mTextPaint.setTextSize(getResources().getDimensionPixelSize(C0380R.dimen.graphFontSize));
        Paint paint2 = new Paint(1);
        this.mIndicatorPaint = paint2;
        paint2.setColor(SupportMenu.CATEGORY_MASK);
        this.mIndicatorPaint.setStrokeWidth(getResources().getDimensionPixelSize(C0380R.dimen.graphIndicatorStrokeWidth));
        Paint paint3 = new Paint(1);
        this.mGraphPaint = paint3;
        paint3.setColor(ViewCompat.MEASURED_STATE_MASK);
        this.mGraphPaint.setStrokeWidth(getResources().getDimensionPixelSize(C0380R.dimen.graphStrokeWidth));
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float dimensionPixelSize = getResources().getDimensionPixelSize(C0380R.dimen.graphTextTopMarginOffset);
        float dimensionPixelSize2 = getResources().getDimensionPixelSize(C0380R.dimen.graphTextBottomMarginOffset);
        canvas.drawText(this.mGraphName, 0.0f, (getHeight() / 2.0f) + (getResources().getDimensionPixelSize(C0380R.dimen.graphFontSize) / 2.0f), this.mTextPaint);
        canvas.drawText(this.mStatus, getResources().getDimensionPixelSize(C0380R.dimen.graphLeftMargin) + 10.0f, getResources().getDimensionPixelSize(C0380R.dimen.graphHorizontalLineTopMargin) + dimensionPixelSize + 10.0f, this.mTextPaint);
        canvas.drawText(this.mPressure, (getWidth() - getResources().getDimensionPixelSize(C0380R.dimen.graphRightMargin)) - getResources().getDimensionPixelSize(C0380R.dimen.graphRightTextMargin), getResources().getDimensionPixelSize(C0380R.dimen.graphHorizontalLineTopMargin) + dimensionPixelSize + 10.0f, this.mTextPaint);
        canvas.drawLine(getResources().getDimensionPixelSize(C0380R.dimen.graphLeftMargin), 0.0f, getResources().getDimensionPixelSize(C0380R.dimen.graphLeftMargin), getHeight() - dimensionPixelSize2, this.mGraphPaint);
        canvas.drawLine(getWidth() - getResources().getDimensionPixelSize(C0380R.dimen.graphRightMargin), 0.0f, getWidth() - getResources().getDimensionPixelSize(C0380R.dimen.graphRightMargin), getHeight() - dimensionPixelSize2, this.mGraphPaint);
        canvas.drawLine(getResources().getDimensionPixelSize(C0380R.dimen.graphLeftMargin), getResources().getDimensionPixelSize(C0380R.dimen.graphHorizontalLineTopMargin), getWidth() - getResources().getDimensionPixelSize(C0380R.dimen.graphRightMargin), getResources().getDimensionPixelSize(C0380R.dimen.graphHorizontalLineTopMargin), this.mGraphPaint);
        Float valueOf = Float.valueOf((((getWidth() - getResources().getDimensionPixelSize(C0380R.dimen.graphRightMargin)) - getResources().getDimensionPixelSize(C0380R.dimen.graphLeftMargin)) * this.mPercentValue.floatValue()) + getResources().getDimensionPixelSize(C0380R.dimen.graphLeftMargin));
        canvas.drawLine(valueOf.floatValue(), getResources().getDimensionPixelSize(C0380R.dimen.graphIndicatorLineTopMargin), valueOf.floatValue(), getResources().getDimensionPixelSize(C0380R.dimen.graphHorizontalLineTopMargin), this.mIndicatorPaint);
    }

    public void setPercentValue(Float f) {
        if (f.floatValue() > 1.0f) {
            f = Float.valueOf(1.0f);
        }
        if (f.floatValue() < 0.0f) {
            f = Float.valueOf(0.0f);
        }
        this.mPercentValue = f;
        invalidate();
    }

    public void setGraphName(String str) {
        this.mGraphName = str;
        invalidate();
        requestLayout();
    }

    public void setStatus(String str) {
        this.mStatus = str;
        invalidate();
    }

    public void setPressure(String str) {
        this.mPressure = str;
        invalidate();
    }

    public void setBarColor(int i) {
        Paint paint = new Paint(1);
        this.mIndicatorPaint = paint;
        paint.setColor(i);
        this.mIndicatorPaint.setStrokeWidth(getResources().getDimensionPixelSize(C0380R.dimen.graphIndicatorStrokeWidth));
        invalidate();
    }
}
