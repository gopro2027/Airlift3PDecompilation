package com.airliftcompany.alp3.custom;

import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/* loaded from: classes.dex */
public class RepeatListener implements View.OnTouchListener {
    private static final String TAG = "RepeatListener";
    private Handler handler = new Handler();
    private Runnable handlerRunnable = new Runnable() { // from class: com.airliftcompany.alp3.custom.RepeatListener.1
        @Override // java.lang.Runnable
        public void run() {
            ImageView imageView = (ImageView) RepeatListener.this.view;
            if (imageView.getColorFilter() != null || imageView.isSelected()) {
                Log.i(RepeatListener.TAG, "RepeatListener runnable fired");
                RepeatListener.this.motionEvent.setAction(2);
                RepeatListener.this.handler.postDelayed(this, RepeatListener.this.normalInterval);
                RepeatListener.this.touchListener.onTouch(RepeatListener.this.view, RepeatListener.this.motionEvent);
                return;
            }
            Log.i(RepeatListener.TAG, "RepeatListener canceled");
            RepeatListener.this.motionEvent.setAction(1);
            RepeatListener.this.touchListener.onTouch(RepeatListener.this.view, RepeatListener.this.motionEvent);
            RepeatListener.this.handler.removeCallbacks(RepeatListener.this.handlerRunnable);
            RepeatListener.this.view.setPressed(false);
            RepeatListener.this.view = null;
            RepeatListener.this.motionEvent = null;
        }
    };
    private int initialInterval;
    private MotionEvent motionEvent;
    private final int normalInterval;
    private final View.OnTouchListener touchListener;
    private View view;

    public RepeatListener(int i, int i2, View.OnTouchListener onTouchListener) {
        if (onTouchListener == null) {
            throw new IllegalArgumentException("null runnable");
        }
        if (i < 0 || i2 < 0) {
            throw new IllegalArgumentException("negative interval");
        }
        Log.i(TAG, "RepeatListener initialized");
        this.initialInterval = i;
        this.normalInterval = i2;
        this.touchListener = onTouchListener;
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.i(TAG, "onTouch Event: " + MotionEvent.actionToString(motionEvent.getActionMasked()));
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1 && actionMasked != 3) {
                if (actionMasked != 5) {
                    if (actionMasked != 6) {
                        return false;
                    }
                }
            }
            this.touchListener.onTouch(view, motionEvent);
            this.handler.removeCallbacks(this.handlerRunnable);
            view.setPressed(false);
            this.view = null;
            this.motionEvent = null;
            return true;
        }
        this.handler.removeCallbacks(this.handlerRunnable);
        this.handler.postDelayed(this.handlerRunnable, this.initialInterval);
        this.view = view;
        this.motionEvent = motionEvent;
        view.setPressed(true);
        this.touchListener.onTouch(view, motionEvent);
        return true;
    }
}
