package com.patrickiv.demo.loaderdemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * A simple spinner loader for demoing purposes
 *
 * Created by Patrick Ivarsson on 7/10/17.
 */
public class SpinnerLoaderView extends View {

    private static final String TAG = SpinnerLoaderView.class.getSimpleName();

    private static final int DEGREES_IN_CIRCLE = 360;
    private static final int ARC_MAX_DEGREES = 180;
    private static final int ARC_START_OFFSET_DEGREES = -90;

    private static final long ANIMATION_DURATION = 1250L;

    private final DecelerateInterpolator mDecelerateInterpolator = new DecelerateInterpolator();
    private final RectF mArcBounds = new RectF();

    private final Paint mPaint = new Paint();

    private ValueAnimator mAnimator;
    private float mArcStart;
    private float mArcSize;

    /** @see View#View(Context) */
    public SpinnerLoaderView(Context context) {
        super(context);
        init();
    }

    /** @see View#View(Context, AttributeSet) */
    public SpinnerLoaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /** @see View#View(Context, AttributeSet, int) */
    public SpinnerLoaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        // Sets the color of the progress bar
        mPaint.setColor(Color.LTGRAY);
        // Only the outline of a shape will be drawn
        mPaint.setStyle(Paint.Style.STROKE);
        // The start and end of the progress bar will have rounded edges
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        // To get smoother shapes
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow: Setting up animator...");
        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        // We want to use a LinearInterpolator as a base and in places where
        // a certain interpolation is desired, it's calculated based on the
        // linear progress.
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final float progress = (float) valueAnimator.getAnimatedValue();
                mArcStart = calculateArcStart(progress);
                mArcSize = calculateArcSize(progress);
                invalidate();
            }
        });
        mAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow: Tearing down animator...");
        mAnimator.cancel();
        mAnimator.removeAllUpdateListeners();
        mAnimator = null;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        final int smallestSide = Math.min(width, height);
        mPaint.setStrokeWidth(smallestSide * 0.05f);

        // When drawing a stroked line at an X,Y position it's always the
        // the center of the brush that's positioned there. This means that
        // if the position is set the one of view edges, half the stroke width
        // will be drawn outside of the view. To counter this, add half the
        // stroke width as a padding.
        final float paintPadding = mPaint.getStrokeWidth() / 2f;

        // Also apply any padding set in the XML/using setPadding(...)
        mArcBounds.set(
                paintPadding + getPaddingLeft(),
                paintPadding + getPaddingTop(),
                width - paintPadding - getPaddingRight(),
                height - paintPadding - getPaddingBottom()
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawArc(mArcBounds, mArcStart, mArcSize, false, mPaint);
    }

    private float calculateArcSize(float animationProgress) {
        // One animationProgress iteration ranges moves from 0 -> 0.5 -> 1.
        // By running it through the Math.sin(...) it's transformed from:
        //      0 -> 0.5 -> 1
        // to:
        //      0 -> 1.0 -> 0
        // Using the transformed progress we'll have a progress that iterates
        // once from 0 -> 1 and then reverse it's progress back to 0.
        final double adjustedProgress = Math.sin(animationProgress * Math.PI);
        // ARC_MAX_DEGREES is the maximum size the arc will have in degrees.
        // ARC_MAX_DEGREES = 180 means that at it's largest it will cover
        // half the circle.
        return (float) adjustedProgress * -ARC_MAX_DEGREES;
    }

    private float calculateArcStart(float animationProgress) {
        final float deceleratedProgress = mDecelerateInterpolator.getInterpolation(animationProgress);

        // ARC_START_OFFSET_DEGREES is used to offset the starting position,
        // -90 makes the top of the view the starting point.
        return ARC_START_OFFSET_DEGREES + deceleratedProgress * DEGREES_IN_CIRCLE;
    }

}

