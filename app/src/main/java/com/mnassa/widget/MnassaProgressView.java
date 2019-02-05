package com.mnassa.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.mnassa.R;

public class MnassaProgressView extends View {

    public static final int STROKE_WIDTH_DEFAULT = 5;
    public static final int STROKE_WIDTH_SMALL = 4;
    public static final int MARGIN = 4;

    private int width;
    private int height;
    private float sweepAngleLeft;
    private float startAngleLeft;
    private float sweepAngleRight;
    private float startAngleRight;
    private RectF rectLeft, rectRight;
    private Paint paint, eraser;
    private boolean isLastFraction;
    private int margin = MARGIN;


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        reset();
    }

    public MnassaProgressView(Context context) {
        super(context);
        init(null, 0);
    }

    public MnassaProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MnassaProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        paint = new Paint();
        eraser = new Paint();
        paint.setColor(ContextCompat.getColor(getContext(), R.color.accent));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(convertToPx(STROKE_WIDTH_DEFAULT));
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        eraser.setColor(Color.TRANSPARENT);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        invalidateTextPaintAndMeasurements();
    }

    public void setProgressColor(@ColorRes int color) {
        paint.setColor(ContextCompat.getColor(getContext(), color));
    }

    public void setStrokeWidth(int width) {
        paint.setStrokeWidth(convertToPx(width));
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    private float convertToPx(int dp) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);

    }

    private void invalidateTextPaintAndMeasurements() {
        float offset = (float) (width / 4);
        float padding = convertToPx(margin);
        rectLeft = new RectF(-offset, offset / 2 + padding / 2, offset, height - padding / 2 - offset / 2);
        rectRight = new RectF(offset, offset / 2 + padding / 2, width - padding, height - padding / 2 - offset / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (sweepAngleLeft >= -90 && !isLastFraction)
            canvas.drawArc(rectLeft, startAngleLeft, sweepAngleLeft, false, paint);

        if (sweepAngleLeft <= -90 && sweepAngleRight < 320 && !isLastFraction) {
            drawRightCircle(canvas);
        } else if (sweepAngleLeft <= 0) {
            sweepAngleLeft -= 10;
        }
        if (sweepAngleLeft == -90 && sweepAngleRight >= 320 && !isLastFraction) {
            sweepAngleLeft = 90;
            startAngleLeft = 0;
        }
        if (sweepAngleLeft > startAngleLeft && !isLastFraction) {
            canvas.drawArc(rectRight, startAngleRight, sweepAngleRight, false, paint);
            canvas.drawArc(rectLeft, startAngleLeft, sweepAngleLeft, false, paint);
            sweepAngleLeft -= 10;
        }
        if (sweepAngleLeft == startAngleLeft) {
            isLastFraction = true;
            startAngleRight = 140;
            sweepAngleRight = -320;
        }
        if (isLastFraction) {
            sweepAngleRight += 10;
            canvas.drawArc(rectRight, startAngleRight, sweepAngleRight, false, paint);
        }
        if (sweepAngleRight == 0 && isLastFraction) {
            canvas.drawPaint(eraser);
            reset();

        }

        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void reset() {
        isLastFraction = false;
        sweepAngleLeft = 0;
        startAngleLeft = 90;
        sweepAngleRight = 0;
        startAngleRight = 180;
        invalidateTextPaintAndMeasurements();
        invalidate();
    }

    private void drawRightCircle(Canvas canvas) {
        canvas.drawArc(rectRight, startAngleRight, sweepAngleRight, false, paint);
        sweepAngleRight += 10;
    }

}
