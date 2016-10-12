package it.liceoarzignano.bold.ui;

/**
 * Simple single android view component that can be used to showing a round progress bar.
 * It can be customized with size, stroke size, colors and text etc.
 * Progress change will be animated.
 * Created by Kristoffer, http://kmdev.se
 *
 * Customized for it.liceoarzignano.bold by joey
 */

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.Locale;

import it.liceoarzignano.bold.R;

public class CircularProgressBar extends View {

    // Width
    private int mViewWidth;
    // Height
    private int mViewHeight;
    // How long to sweep from mStartAngle
    private float mSweepAngle = 0;
    // Outline color
    private int mProgressColor;
    // Progress text color
    private int mTextColor;
    // Allocate paint outside onDraw to avoid unnecessary object creation
    private final Paint mPaint;
    // Average value
    private double mValue;

    public CircularProgressBar(Context context) {
        this(context, null);
        mTextColor = ContextCompat.getColor(context, R.color.black);
    }

    public CircularProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mTextColor = ContextCompat.getColor(context, R.color.black);
    }

    public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextColor = ContextCompat.getColor(context, R.color.black);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initMeasurments();
        drawOutlineArc(canvas);
        drawText(canvas);
    }

    private void initMeasurments() {
        mViewWidth = getWidth();
        mViewHeight = getHeight();
    }

    private void drawOutlineArc(Canvas canvas) {
        final int diameter = Math.min(mViewWidth, mViewHeight) - 48;

        final RectF outerOval = new RectF(24, 24, diameter, diameter);

        mPaint.setColor(mProgressColor);
        mPaint.setStrokeWidth(24);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(outerOval, -90, mSweepAngle, false, mPaint);
    }

    private void drawText(Canvas canvas) {
        mPaint.setTextSize(Math.min(mViewWidth, mViewHeight) / 5f);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStrokeWidth(0);
        mPaint.setColor(mTextColor);

        // Center text
        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2)) ;

        canvas.drawText(String.format(Locale.ENGLISH, "%.2f", mValue), xPos, yPos, mPaint);

    }

    private float calcSweepAngleFromProgress(int progress) {
        return 36 * progress / 100;
    }

    /**
     * Set progress of the circular progress bar.
     * @param progress progress between 0 and 100.
     */
    public void setProgress(double progress) {
        mValue = progress;
        ValueAnimator animator = ValueAnimator.ofFloat(mSweepAngle,
                calcSweepAngleFromProgress((int) progress * 100));
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(2000);
        animator.setStartDelay(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mSweepAngle = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    /**
     * Set progress bar color
     * @param color color resource
     */
    public void setProgressColor(int color) {
        mProgressColor = color;
        invalidate();
    }
}
