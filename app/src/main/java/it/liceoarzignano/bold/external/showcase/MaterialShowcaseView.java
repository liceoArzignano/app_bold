/**
 * Copyright 2015 Dean Wild
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.liceoarzignano.bold.external.showcase;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.external.showcase.shape.CircleShape;
import it.liceoarzignano.bold.external.showcase.shape.Shape;
import it.liceoarzignano.bold.external.showcase.target.Target;
import it.liceoarzignano.bold.external.showcase.target.ViewTarget;

public class MaterialShowcaseView extends FrameLayout
        implements View.OnTouchListener, View.OnClickListener{

    private int mOldHeight;
    private int mOldWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mEraser;
    private static Target mTarget;
    private Shape mShape;
    private int mXPosition;
    private int mYPosition;
    private boolean mWasNotDismissed = true;
    private final int mShapePadding = ShowcaseConfig.DEFAULT_SHAPE_PADDING;

    private View mContentBox;
    private TextView mContentTextView;
    private TextView mDismissButton;
    private int mGravity;
    private int mContentBottomMargin;
    private int mContentTopMargin;
    private boolean mShouldNotRender = true;
    private int mMaskColour;
    private AnimationFactory mAnimationFactory;
    private final long mFadeDurationInMillis = ShowcaseConfig.DEFAULT_FADE_TIME;
    private Handler mHandler;
    private boolean mSingleUse = false;
    private PrefsManager mPrefsManager;
    private List<IShowcaseListener> mListeners;
    private UpdateOnGlobalLayout mLayoutListener;

    public MaterialShowcaseView(Context context) {
        super(context);
        init();
    }
    public MaterialShowcaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaterialShowcaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);

        // create our animation factory
        mAnimationFactory = new AnimationFactory();

        mListeners = new ArrayList<>();

        // make sure we add a global layout listener so we can adapt to changes
        mLayoutListener = new UpdateOnGlobalLayout();
        getViewTreeObserver().addOnGlobalLayoutListener(mLayoutListener);

        // consume touch events
        setOnTouchListener(this);

        //noinspection deprecation
        mMaskColour = getResources().getColor(R.color.showcase_color);
        setVisibility(INVISIBLE);

        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.showcase_content, this, true);
        mContentBox = contentView.findViewById(R.id.content_box);
        mContentTextView = (TextView) contentView.findViewById(R.id.tv_content);
        mDismissButton = (TextView) contentView.findViewById(R.id.tv_dismiss);
        mDismissButton.setOnClickListener(this);
        mDismissButton.setText(getContext().getString(R.string.intro_gotit));
    }


    /**
     * Interesting drawing stuff.
     * We draw a block of semi transparent colour to fill the whole screen then we draw of transparency
     * to create a circular "viewport" through to the underlying content
     *
     * @param canvas: the canvas we'll draw things in
     */
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // don't bother drawing if we're not ready
        if (mShouldNotRender) return;

        // get current dimensions
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        // don't bother drawing if there is nothing to draw on
        if(width <= 0 || height <= 0) return;

        // build a new canvas if needed i.e first pass or new dimensions
        if (mBitmap == null || mCanvas == null || mOldHeight != height || mOldWidth != width) {

            if (mBitmap != null) mBitmap.recycle();

            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            mCanvas = new Canvas(mBitmap);
        }

        // save our 'old' dimensions
        mOldWidth = width;
        mOldHeight = height;

        // clear canvas
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // draw solid background
        mCanvas.drawColor(mMaskColour);

        // Prepare eraser Paint if needed
        if (mEraser == null) {
            mEraser = new Paint();
            mEraser.setColor(0xFFFFFFFF);
            mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mEraser.setFlags(Paint.ANTI_ALIAS_FLAG);
        }

        // draw (erase) shape
        mShape.draw(mCanvas, mEraser, mXPosition, mYPosition, mShapePadding);

        // Draw the bitmap on our views  canvas.
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        /**
         * If we're being detached from the window without the mWasNotDismissed flag then we weren't purposefully dismissed
         * Probably due to an orientation change or user backed out of activity.
         * Ensure we reset the flag so the showcase display again.
         */
        if (mWasNotDismissed && mSingleUse && mPrefsManager != null) {
            mPrefsManager.resetShowcase();
        }


        notifyOnDismissed();

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(mTarget.getBounds().contains((int)event.getX(), (int)event.getY())) {
            hide();
            return false;
        }
        return true;
    }


    private void notifyOnDisplayed() {

        if(mListeners != null){
            for (IShowcaseListener listener : mListeners) {
                listener.onShowcaseDisplayed(this);
            }
        }
    }

    private void notifyOnDismissed() {
        if (mListeners != null) {
            for (IShowcaseListener listener : mListeners) {
                listener.onShowcaseDismissed(this);
            }

            mListeners.clear();
            mListeners = null;
        }
    }

    /**
     * Dismiss button clicked
     *
     * @param v: view
     */
    @Override
    public void onClick(View v) {
        hide();
    }

    /**
     * Tells us about the "Target" which is the view we want to anchor to.
     * We figure out where it is on screen and (optionally) how big it is.
     * We also figure out whether to place our content and dismiss button above or below it.
     *
     * @param target: target
     */
    private void setTarget(Target target) {
        mTarget = target;

        // update dismiss button state
        mDismissButton.setVisibility(VISIBLE);

        if (mTarget != null) {

            /**
             * If we're on lollipop then make sure we don't draw over the nav bar
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int mBottomMargin = getSoftButtonsBarSizePort((Activity) getContext());
                FrameLayout.LayoutParams contentLP = (LayoutParams) getLayoutParams();

                if (contentLP != null && contentLP.bottomMargin != mBottomMargin)
                    contentLP.bottomMargin = mBottomMargin;
            }

            // apply the target position
            Point targetPoint = mTarget.getPoint();
            Rect targetBounds = mTarget.getBounds();
            setPosition(targetPoint);

            // now figure out whether to put content above or below it
            int height = getMeasuredHeight();
            int midPoint = height / 2;
            int yPos = targetPoint.y;

            int radius = Math.max(targetBounds.height(), targetBounds.width()) / 2;
            if (mShape != null) {
                mShape.updateTarget(mTarget);
                radius = mShape.getHeight() / 2;
            }

            if (yPos > midPoint) {
                // target is in lower half of screen, we'll sit above it
                mContentTopMargin = 0;
                mContentBottomMargin = (height - yPos) + radius + mShapePadding;
                mGravity = Gravity.BOTTOM;
            } else {
                // target is in upper half of screen, we'll sit below it
                mContentTopMargin = yPos + radius + mShapePadding;
                mContentBottomMargin = 0;
                mGravity = Gravity.TOP;
            }
        }

        applyLayoutParams();
    }

    private void applyLayoutParams() {

        if (mContentBox != null && mContentBox.getLayoutParams() != null) {
            FrameLayout.LayoutParams contentLP = (LayoutParams) mContentBox.getLayoutParams();

            boolean layoutParamsChanged = false;

            if (contentLP.bottomMargin != mContentBottomMargin) {
                contentLP.bottomMargin = mContentBottomMargin;
                layoutParamsChanged = true;
            }

            if (contentLP.topMargin != mContentTopMargin) {
                contentLP.topMargin = mContentTopMargin;
                layoutParamsChanged = true;
            }

            if (contentLP.gravity != mGravity) {
                contentLP.gravity = mGravity;
                layoutParamsChanged = true;
            }

            /**
             * Only apply the layout params if we've actually changed them, otherwise we'll get stuck in a layout loop
             */
            if (layoutParamsChanged)
                mContentBox.setLayoutParams(contentLP);
        }
    }

    /**
     * SETTERS
     */

    private void setPosition(Point point) {
        setPosition(point.x, point.y);
    }

    private void setPosition(int x, int y) {
        mXPosition = x;
        mYPosition = y;
    }

    private void setContentText(CharSequence contentText) {
        if (mContentTextView != null) {
            mContentTextView.setText(contentText);
        }
    }

    private void setShouldRender() {
        mShouldNotRender = false;
    }


    private void setShape(Shape mShape) {
        this.mShape = mShape;
    }

    /**
     * REDRAW LISTENER - this ensures we redraw after activity finishes laying out
     */
    private class UpdateOnGlobalLayout implements ViewTreeObserver.OnGlobalLayoutListener {

        @Override
        public void onGlobalLayout() {
            setTarget(mTarget);
        }
    }


    /**
     * BUILDER CLASS
     * Gives us a builder utility class with a fluent API for easily configuring showcase views
     */
    public static class Builder {
        static MaterialShowcaseView showcaseView;

        private final Activity activity;

        public Builder(Activity activity) {
            this.activity = activity;

            showcaseView = new MaterialShowcaseView(activity);
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        public Builder setTarget(View target) {
            showcaseView.setTarget(new ViewTarget(target));
            return this;
        }

        /**
         * Set the descriptive text shown on the ShowcaseView.
         */
        public Builder setContentText(CharSequence text) {
            showcaseView.setContentText(text);
            return this;
        }

        public Builder singleUse(String showcaseID) {
            showcaseView.singleUse(showcaseID);
            return this;
        }

        public MaterialShowcaseView build() {
            if (showcaseView.mShape == null) {
                //noinspection AccessStaticViaInstance
                showcaseView.setShape(new CircleShape(showcaseView.mTarget));
            }

            return showcaseView;
        }

        public void show() {
            build().show(activity);
        }

    }

    private void singleUse(String showcaseID) {
        mSingleUse = true;
        mPrefsManager = new PrefsManager(getContext(), showcaseID);
    }

    private void removeFromWindow() {
        if (getParent() != null && getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).removeView(this);
        }

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        mEraser = null;
        mAnimationFactory = null;
        mCanvas = null;
        mHandler = null;

        //noinspection deprecation
        getViewTreeObserver().removeGlobalOnLayoutListener(mLayoutListener);
        mLayoutListener = null;

        if (mPrefsManager != null)
            mPrefsManager.close();

        mPrefsManager = null;


    }


    /**
     * Reveal the showcase view
     *
     * @param activity : activity
     */
    private void show(Activity activity) {

        /**
         * if we're in single use mode and have already shot our bolt then do nothing
         */
        if (mSingleUse) {
            if (mPrefsManager.hasFired()) {
                return;
            } else {
                mPrefsManager.setFired();
            }
        }

        ((ViewGroup) activity.getWindow().getDecorView()).addView(this);

        setShouldRender();

        mHandler = new Handler();
        long mDelayInMillis = ShowcaseConfig.DEFAULT_DELAY;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fadeIn();
            }
        }, mDelayInMillis);

        mDismissButton.setVisibility(VISIBLE);
    }


    /**
     * This flag is used to indicate to onDetachedFromWindow that the
     * showcase view was dismissed purposefully (by the user or programmatically)
     */
    private void hide() {
        mWasNotDismissed = false;
         fadeOut();
    }

    private void fadeIn() {
        setVisibility(INVISIBLE);

        mAnimationFactory.fadeInView(this, mFadeDurationInMillis,
                new IAnimationFactory.AnimationStartListener() {
                    @Override
                    public void onAnimationStart() {
                        setVisibility(View.VISIBLE);
                        notifyOnDisplayed();
                    }
                }
        );
    }

    private void fadeOut() {

        mAnimationFactory.fadeOutView(this, mFadeDurationInMillis, new IAnimationFactory.AnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                setVisibility(INVISIBLE);
                removeFromWindow();
            }
        });
    }

    private static int getSoftButtonsBarSizePort(Activity activity) {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }
}
