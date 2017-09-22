package it.liceoarzignano.bold.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.support.v4.content.ContextCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import it.liceoarzignano.bold.R
import java.util.*

/**
 * Simple single android view component that can be used to showing a round progress bar.
 * It can be customized with size, stroke size, colors and text etc.
 * Progress change will be animated.
 * Created by Kristoffer, http://kmdev.se
 *
 *
 * Customized for it.liceoarzignano.bold by joey
 */
class CircularProgressBar : View {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0
    private var mSweepAngle = 0f
    private var mProgressColor: Int = 0
    private val mTextColor = ContextCompat.getColor(context, R.color.black)
    private var mValue: Double = 0.toDouble()

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, style: Int) : super(context, attrs, style)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        initMeasurements()
        drawOutlineArc(canvas)
        drawText(canvas)
    }

    private fun initMeasurements() {
        mViewWidth = width
        mViewHeight = height
    }

    private fun drawOutlineArc(canvas: Canvas) {
        val diameter = Math.min(mViewWidth, mViewHeight) - 48
        val outerOval = RectF(24f, 24f, diameter.toFloat(), diameter.toFloat())

        mPaint.color = ContextCompat.getColor(context, R.color.circular_progress_bar_bg)
        mPaint.strokeWidth = 32f
        mPaint.isAntiAlias = true
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.style = Paint.Style.STROKE
        canvas.drawArc(outerOval, 0f, 360f, false, mPaint)

        mPaint.color = mProgressColor
        canvas.drawArc(outerOval, -90f, mSweepAngle, false, mPaint)
    }

    private fun drawText(canvas: Canvas) {
        mPaint.textSize = Math.min(mViewWidth, mViewHeight) / 5f
        mPaint.textAlign = Paint.Align.CENTER
        mPaint.strokeWidth = 0f
        mPaint.color = mTextColor

        // Center text
        val posX = canvas.width / 2
        val posY = (canvas.height / 2 - (mPaint.descent() + mPaint.ascent()) / 2).toInt()

        canvas.drawText(String.format(Locale.ENGLISH, "%.2f", mValue), posX.toFloat(),
                posY.toFloat(), mPaint)

    }

    private fun calcSweepAngleFromProgress(progress: Int): Float = (36 * progress / 100).toFloat()

    fun setProgress(progress: Double) {
        mValue = progress

        // Animate only the first time
        if (mSweepAngle != 0f) {
            mSweepAngle = calcSweepAngleFromProgress(
                    if (progress < 1)
                        100
                    else
                        (progress * 100).toInt())
            return
        }

        mSweepAngle = 0f

        val animator = ValueAnimator.ofFloat(mSweepAngle, calcSweepAngleFromProgress(
                if (progress < 1)
                    100
                else
                    (progress * 100).toInt()))

        animator.interpolator = FastOutSlowInInterpolator()
        animator.duration = 1600
        animator.startDelay = 300
        animator.addUpdateListener { valueAnimator ->
            mSweepAngle = valueAnimator.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    fun setProgressColor(color: Int) {
        mProgressColor = color
        invalidate()
    }
}
