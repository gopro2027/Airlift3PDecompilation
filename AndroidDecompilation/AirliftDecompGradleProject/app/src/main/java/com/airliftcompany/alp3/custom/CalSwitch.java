package com.airliftcompany.alp3.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Switch;
import java.lang.reflect.Field;

/* loaded from: classes.dex */
public class CalSwitch extends Switch {
    public CalSwitch(Context context) {
        super(context);
    }

    public CalSwitch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public CalSwitch(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public CalSwitch(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    @Override // android.view.View
    public void requestLayout() {
        try {
            Field declaredField = Switch.class.getDeclaredField("mOnLayout");
            declaredField.setAccessible(true);
            declaredField.set(this, null);
            Field declaredField2 = Switch.class.getDeclaredField("mOffLayout");
            declaredField2.setAccessible(true);
            declaredField2.set(this, null);
        } catch (Exception e) {
            Log.e("LayoutSwitch", e.getMessage(), e);
        }
        super.requestLayout();
    }
}
