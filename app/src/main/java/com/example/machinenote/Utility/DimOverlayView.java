package com.example.machinenote.Utility;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class DimOverlayView extends View {

    private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private RectF holeRect = null;
    private float holeRadius = 0f;

    public DimOverlayView(Context context) {
        super(context);
        init();
    }

    public DimOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 25% black dim like you had
        dimPaint.setColor(0xBF000000);

        // This paint will "punch" a transparent hole
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        // Needed for CLEAR to work reliably
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    /** Call this every step to move the cutout */
    public void setHole(RectF rect, float radiusPx) {
        this.holeRect = rect;
        this.holeRadius = radiusPx;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw dim over whole screen
        canvas.drawRect(0, 0, getWidth(), getHeight(), dimPaint);

        // Cut out hole
        if (holeRect != null) {
            canvas.drawRoundRect(holeRect, holeRadius, holeRadius, clearPaint);
        }
    }
}
