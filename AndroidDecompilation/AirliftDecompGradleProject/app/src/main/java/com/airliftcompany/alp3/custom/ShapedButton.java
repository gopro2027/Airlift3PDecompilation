package com.airliftcompany.alp3.custom;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageButton;
import java.util.Timer;
import java.util.TimerTask;

/* loaded from: classes.dex */
public class ShapedButton extends ImageButton {
    private static final int BLINK_TIMING_MS = 250;
    private static final String TAG = "ShapedButton";
    private TimerTask BlinkTimerTask;
    public Drawable altNormalDrawable;
    private byte blinkOn;
    private boolean blinkRestore;
    private Timer blinkTimer;
    private boolean buttonIsDown;
    public boolean isBlinking;
    private Handler mTimerHandler;
    public Drawable normalBlinkDrawable;
    public Drawable normalDrawable;
    private Rect rect;
    public Drawable selectedDrawble;
    private boolean useAltNormalImage;

    public ShapedButton(Context context) {
        super(context);
        this.mTimerHandler = new Handler();
        this.BlinkTimerTask = new TimerTask() { // from class: com.airliftcompany.alp3.custom.ShapedButton.1
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                if (ShapedButton.this.isBlinking) {
                    ShapedButton shapedButton = ShapedButton.this;
                    shapedButton.blinkOn = (byte) (shapedButton.blinkOn ^ 1);
                    ShapedButton.this.mTimerHandler.post(new Runnable() { // from class: com.airliftcompany.alp3.custom.ShapedButton.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            if (ShapedButton.this.blinkOn == 1) {
                                ShapedButton.this.setImageDrawable(ShapedButton.this.normalBlinkDrawable);
                            } else {
                                ShapedButton.this.setImageDrawable(ShapedButton.this.normalDrawable);
                            }
                        }
                    });
                }
            }
        };
        setup();
    }

    public ShapedButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTimerHandler = new Handler();
        this.BlinkTimerTask = new TimerTask() { // from class: com.airliftcompany.alp3.custom.ShapedButton.1
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                if (ShapedButton.this.isBlinking) {
                    ShapedButton shapedButton = ShapedButton.this;
                    shapedButton.blinkOn = (byte) (shapedButton.blinkOn ^ 1);
                    ShapedButton.this.mTimerHandler.post(new Runnable() { // from class: com.airliftcompany.alp3.custom.ShapedButton.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            if (ShapedButton.this.blinkOn == 1) {
                                ShapedButton.this.setImageDrawable(ShapedButton.this.normalBlinkDrawable);
                            } else {
                                ShapedButton.this.setImageDrawable(ShapedButton.this.normalDrawable);
                            }
                        }
                    });
                }
            }
        };
        setup();
    }

    public ShapedButton(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mTimerHandler = new Handler();
        this.BlinkTimerTask = new TimerTask() { // from class: com.airliftcompany.alp3.custom.ShapedButton.1
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                if (ShapedButton.this.isBlinking) {
                    ShapedButton shapedButton = ShapedButton.this;
                    shapedButton.blinkOn = (byte) (shapedButton.blinkOn ^ 1);
                    ShapedButton.this.mTimerHandler.post(new Runnable() { // from class: com.airliftcompany.alp3.custom.ShapedButton.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            if (ShapedButton.this.blinkOn == 1) {
                                ShapedButton.this.setImageDrawable(ShapedButton.this.normalBlinkDrawable);
                            } else {
                                ShapedButton.this.setImageDrawable(ShapedButton.this.normalDrawable);
                            }
                        }
                    });
                }
            }
        };
        setup();
    }

    private void setup() {
        Timer timer = new Timer();
        this.blinkTimer = timer;
        timer.schedule(this.BlinkTimerTask, 250L, 250L);
    }

    public void setBlinking(boolean z) {
        if (isSelected()) {
            return;
        }
        if (this.isBlinking != z) {
            this.isBlinking = z;
        }
        if (isSelected()) {
            setSelected(false);
        }
    }

    public void setUseAltNormalImage(boolean z) {
        if (this.useAltNormalImage == z) {
            return;
        }
        this.useAltNormalImage = z;
        setSelected(isSelected());
    }

    @Override // android.widget.ImageView, android.view.View
    public void setSelected(boolean z) {
        if (z) {
            setImageDrawable(this.selectedDrawble);
            this.blinkRestore = this.isBlinking;
            this.isBlinking = false;
        } else {
            this.isBlinking = this.blinkRestore;
            if (this.useAltNormalImage) {
                setImageDrawable(this.altNormalDrawable);
            } else {
                setImageDrawable(this.normalDrawable);
            }
        }
        super.setSelected(z);
    }

    /* JADX WARN: Code restructure failed: missing block: B:27:0x00fa, code lost:
    
        if (r2 != 6) goto L57;
     */
    @Override // android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean dispatchTouchEvent(android.view.MotionEvent r14) {
        /*
            Method dump skipped, instructions count: 323
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airliftcompany.alp3.custom.ShapedButton.dispatchTouchEvent(android.view.MotionEvent):boolean");
    }
}
