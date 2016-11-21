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
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;

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

    public CircularProgressBar(Context mContext) {
        this(mContext, null);
        mTextColor = ContextCompat.getColor(mContext, R.color.black);
    }

    public CircularProgressBar(Context mContext, AttributeSet mAttrs) {
        this(mContext, mAttrs, 0);
        mTextColor = ContextCompat.getColor(mContext, R.color.black);
    }

    public CircularProgressBar(Context mContext, AttributeSet mAttrs, int mDedStyleAttrs) {
        super(mContext, mAttrs, mDedStyleAttrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextColor = ContextCompat.getColor(mContext, R.color.black);
    }

    @Override
    protected void onDraw(Canvas mCanvas) {
        super.onDraw(mCanvas);
        initMeasurments();
        drawOutlineArc(mCanvas);
        drawText(mCanvas);
    }

    private void initMeasurments() {
        mViewWidth = getWidth();
        mViewHeight = getHeight();
    }

    private void drawOutlineArc(Canvas mCanvas) {
        final int mDiameter = Math.min(mViewWidth, mViewHeight) - 48;

        final RectF mOuterOval = new RectF(24, 24, mDiameter, mDiameter);

        mPaint.setColor(mProgressColor);
        mPaint.setStrokeWidth(24);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mCanvas.drawArc(mOuterOval, -90, mSweepAngle, false, mPaint);
    }

    private void drawText(Canvas mCanvas) {
        mPaint.setTextSize(Math.min(mViewWidth, mViewHeight) / 5f);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStrokeWidth(0);
        mPaint.setColor(mTextColor);

        // Center text
        int mXPos = (mCanvas.getWidth() / 2);
        int mYPos = (int) ((mCanvas.getHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2));

        mCanvas.drawText(String.format(Locale.ENGLISH, "%.2f", mValue), mXPos, mYPos, mPaint);

    }

    private float calcSweepAngleFromProgress(int mProgress) {
        return 36 * mProgress / 100;
    }

    /**
     * Set progress of the circular progress bar.
     * @param mProgress progress between 0 and 100.
     */
    public void setProgress(double mProgress) {
        mSweepAngle = 0f;
        mValue = mProgress;

        ValueAnimator mAnimator = ValueAnimator.ofFloat(mSweepAngle,
                calcSweepAngleFromProgress((int) mProgress * 100));
        //
        mAnimator.setInterpolator(new FastOutSlowInInterpolator());
        mAnimator.setDuration(1600);
        mAnimator.setStartDelay(300);
        mAnimator.addUpdateListener(valueAnimator -> {
            mSweepAngle = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        mAnimator.start();
    }

    /**
     * Set progress bar color
     * @param mColor color resource
     */
    public void setProgressColor(int mColor) {
        mProgressColor = mColor;
        invalidate();
    }
}
