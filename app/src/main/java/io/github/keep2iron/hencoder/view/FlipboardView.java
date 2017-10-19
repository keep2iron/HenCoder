package io.github.keep2iron.hencoder.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;

import io.github.keep2iron.hencoder.R;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/10/19 10:58
 */
public class FlipboardView extends View {
    Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Bitmap mDrawBitmap;
    Matrix mMatrix;
    Camera mCamera;

    Rect mDrawRect = new Rect();
    float mScaleX;
    float mScaleY;

    AnimatorSet animatorSet;

    //图形的总体旋转的角度
    private float mRotate;
    private int setupValue;
    private int finalValue;

    public FlipboardView(Context context) {
        this(context, null);
    }

    public FlipboardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlipboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mDrawBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.flipboard);
        mMatrix = new Matrix();
        mCamera = new Camera();

        mRotate = 0;

        ValueAnimator setup1Animator = ValueAnimator.ofInt(0, 25);
        setup1Animator.setDuration(500);
        setup1Animator.setInterpolator(new LinearInterpolator());
        setup1Animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setupValue = (int) animation.getAnimatedValue();
                postInvalidate();
            }
        });

        ValueAnimator rotateAnimator = ValueAnimator.ofFloat(0, 270);
        rotateAnimator.setDuration(900);
        rotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRotate = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        rotateAnimator.setStartDelay(400);

        ValueAnimator finalAnimator = ValueAnimator.ofInt(0,25);
        finalAnimator.setDuration(500);
        finalAnimator.setInterpolator(new LinearInterpolator());
        finalAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                finalValue = (int)animation.getAnimatedValue();
                postInvalidate();
            }
        });
        finalAnimator.setStartDelay(500);

        animatorSet = new AnimatorSet();
        animatorSet.playSequentially(setup1Animator, rotateAnimator, finalAnimator);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mDrawRect.set(getWidth() / 2 - mDrawBitmap.getWidth() / 2,
                getHeight() / 2 - mDrawBitmap.getHeight() / 2,
                getWidth() / 2 + mDrawBitmap.getWidth() / 2,
                getHeight() / 2 + mDrawBitmap.getHeight() / 2);
        mScaleX = mDrawRect.width() * 1.f / mDrawBitmap.getWidth();
        mScaleY = mDrawRect.height() * 1.f / mDrawBitmap.getHeight();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animatorSet.setStartDelay(1500);
        animatorSet.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

//        cameraAnimator.cancel();
        animatorSet.cancel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //画上半部分,Canvas的执行顺序是一种栈形式，因此先canvas.xxx()后执行操作
        canvas.save();
        mCamera.save();
        canvas.translate(mDrawRect.centerX(), mDrawRect.centerY());
        canvas.rotate(-mRotate);
        mCamera.rotateY(-setupValue);
        mCamera.applyToCanvas(canvas);
        canvas.clipRect(0, mDrawRect.centerY(), mDrawRect.centerX(), -mDrawRect.centerY());
        canvas.rotate(mRotate);
        canvas.translate(-mDrawRect.centerX(), -mDrawRect.centerY());
        canvas.scale(1.5f, 1.5f, mDrawRect.centerX(), mDrawRect.centerY());
        mCamera.restore();
        canvas.drawBitmap(mDrawBitmap, mDrawRect.left, mDrawRect.top, mPaint);
        canvas.restore();

        //画下半部分
        canvas.save();
        mCamera.save();
        canvas.translate(mDrawRect.centerX(), mDrawRect.centerY());
        canvas.rotate(-mRotate);
        mCamera.rotateY(finalValue);
        mCamera.applyToCanvas(canvas);
        canvas.clipRect(-mDrawRect.centerX(), -mDrawRect.centerY(), 0, mDrawRect.centerY());
        canvas.rotate(mRotate);
        canvas.translate(-mDrawRect.centerX(), -mDrawRect.centerY());
        canvas.scale(1.5f, 1.5f, mDrawRect.centerX(), mDrawRect.centerY());
        mCamera.restore();
        canvas.drawBitmap(mDrawBitmap, mDrawRect.left, mDrawRect.top, mPaint);

        canvas.restore();
    }
}
