package io.github.keep2iron.hencoder.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by keep-iron on 17-10-17.
 */
public class XiaoMiView extends View {

    private RectF mCircleRect = new RectF();
    private int mRadius;

    private int mLineWidth;
    private int mLineNumber = 18;

    //连接过程中的线条
    private Paint mLinkingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<LinkingLine> mLinkingPath = new ArrayList<>();

    //火花
    private Paint mFirePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Fire> mFires = new ArrayList<>();

    //完成状态的圆环
    private Paint mFinishCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private State mCurrentState;

    //旋转角度
    private int mRotate;

    //缩放动画
    private float mScale;
    private float mTranslateSize;

    //文本
    private int mStep = 2274;                  //步数
    private float mRunDistance = 1.5f;         //行走的距离
    private int mCalorie = 30000;               //消耗的卡路里
    private Paint mTextPaint;
    private Rect mTextRect = new Rect();

    //圆形线条的paint
    private Paint mCircleLinePaint;

    enum State {
        LINKING,
        FINISH,
    }

    public XiaoMiView(Context context) {
        this(context, null);
    }

    public XiaoMiView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XiaoMiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        mCurrentState = State.LINKING;

        mLineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());

        mRadius = getWidth() / 2 - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        mCircleRect = new RectF(getWidth() / 2 - mRadius, getHeight() / 2 - mRadius,
                getWidth() / 2 + mRadius, getHeight() / 2 + mRadius);

        Random random = new Random(System.currentTimeMillis());

        float unitLineWidth = mLineWidth * 1.0f / mLineNumber;

        for (int i = 0; i < mLineNumber; i++) {
            LinkingLine line = new LinkingLine();

            int r = random.nextInt(mLineWidth / 3);
            Path path = new Path();
//            path.addArc(new RectF(mCircleRect.left + r , mCircleRect.top, mCircleRect.right - r, mCircleRect.bottom), 0, 330);
            path.addArc(new RectF(mCircleRect.left + r - unitLineWidth * i,
                    mCircleRect.top - unitLineWidth * i,
                    mCircleRect.right - r + unitLineWidth * i,
                    mCircleRect.bottom + unitLineWidth * i), 15, 330);

            line.path = path;
            if (i >= mLineNumber - 2) {
                line.startAlpha = random.nextInt(25) + 230;
            } else {
                line.startAlpha = random.nextInt(30) + 128;
            }

            mLinkingPath.add(line);
        }

        for (int i = 0; i < 40; i++) {
            Fire fire = new Fire(i);
            mFires.add(fire);
        }

        mLinkingPaint.setColor(Color.WHITE);
        mLinkingPaint.setStyle(Paint.Style.STROKE);
        mLinkingPaint.setStrokeWidth(2.2f);

        mFirePaint.setColor(Color.WHITE);
        mFirePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        BlurMaskFilter blurMaskFilter = new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL);
        mFirePaint.setMaskFilter(blurMaskFilter);

        SweepGradient sweepGradient = new SweepGradient(mCircleRect.centerX(), mCircleRect.centerY(),
                new int[]{Color.WHITE, Color.argb(128, 255, 255, 255), Color.WHITE}, new float[]{0, 0.5f, 1.0f});
        mFinishCirclePaint.setShader(sweepGradient);
        mFinishCirclePaint.setStyle(Paint.Style.STROKE);
        mFinishCirclePaint.setStrokeWidth(mLineWidth);
        mFinishCirclePaint.setShadowLayer(mLineWidth, 10, 10, Color.argb(50, 255, 255, 255));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mCircleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCircleLinePaint.setColor(Color.WHITE);
        mCircleLinePaint.setStrokeWidth(5.f);
        mCircleLinePaint.setStyle(Paint.Style.STROKE);

        setLinkingState();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setFinishState();
            }
        }, 3000);
    }

    public void setLinkingState() {
        mCurrentState = State.LINKING;
        ValueAnimator rotateAnimator = ValueAnimator.ofInt(0, 360);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.setRepeatMode(ValueAnimator.RESTART);
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimator.setDuration(2000);
        rotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRotate = (int) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        rotateAnimator.start();
    }

    /**
     * 设置取消状态
     */
    public void setFinishState() {
        mCurrentState = State.FINISH;
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(1.f, 1.15f, 1.07f);
        scaleAnimator.setInterpolator(new AccelerateInterpolator());
        scaleAnimator.setDuration(400);
        scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mScale = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        scaleAnimator.start();

        ValueAnimator translateAnimator = ValueAnimator.ofFloat(0,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -20, getResources().getDisplayMetrics()),
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()));
        translateAnimator.setDuration(700);
        translateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mTranslateSize = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        translateAnimator.start();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            init();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawText(canvas);
        switch (mCurrentState) {
            case LINKING:
                drawLinkingCircle(canvas);
                drawFire(canvas);
                break;
            case FINISH:
                drawFinishCircle(canvas);
                break;
        }
    }

    private void drawText(Canvas canvas) {
        canvas.save();
        canvas.translate(0, -mTranslateSize);

        String text = "" + mStep;
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 50, getResources().getDisplayMetrics()));
        mTextPaint.setColor(Color.WHITE);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
        int baseLine = (int) (mCircleRect.centerY() - (fontMetrics.bottom - fontMetrics.top) / 2 + (-fontMetrics.top)) - mLineWidth;
        canvas.drawText(text, mCircleRect.centerX() - mTextRect.width() / 2, baseLine, mTextPaint);

        text = mRunDistance + "公里 | " + mCalorie + "千卡";
        float sp15 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics());
        baseLine = (int) (baseLine + sp15 + 30);
        mTextPaint.setColor(Color.argb(157, 255, 255, 255));
        mTextPaint.setTextSize(sp15);
        mTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
        canvas.drawText(text, mCircleRect.centerX() - mTextRect.width() / 2, baseLine, mTextPaint);

        canvas.restore();
    }

    private void drawFinishCircle(Canvas canvas) {
        canvas.save();
        mFinishCirclePaint.setStyle(Paint.Style.STROKE);
        canvas.scale(mScale, mScale, mCircleRect.centerX(), mCircleRect.centerY());
        canvas.translate(0, -mTranslateSize);
        canvas.rotate(mRotate, mCircleRect.centerX(), mCircleRect.centerY());
        canvas.drawCircle(mCircleRect.centerX(), mCircleRect.centerY(), mRadius, mFinishCirclePaint);
        canvas.restore();

        //如果缩放动画结束之后开始画圆
        if (mScale >= 1.07f) {
            canvas.save();
            canvas.translate(0, -mTranslateSize);
            canvas.scale(0.9f, 0.9f, mCircleRect.centerX(), mCircleRect.centerY());
            canvas.rotate(-90, mCircleRect.centerX(), mCircleRect.centerY());
            mCircleLinePaint.setPathEffect(null);
            canvas.drawArc(mCircleRect, 0, 270, false, mCircleLinePaint);
            PathEffect pathEffect = new DashPathEffect(new float[]{10, 10}, 0);
            mCircleLinePaint.setPathEffect(pathEffect);
            canvas.drawArc(mCircleRect, 270, 360, false, mCircleLinePaint);

            mFinishCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawCircle(mCircleRect.centerX(), mCircleRect.top, 4, mFinishCirclePaint);
            canvas.restore();
        }
    }

    public void drawFire(Canvas canvas) {
        canvas.save();
        canvas.rotate(mRotate, mCircleRect.centerX(), mCircleRect.centerY());
        for (int i = 0; i < mFires.size(); i++) {
            Fire fire = mFires.get(i);
            int color = Color.argb(fire.alpha, 255, 255, 255);
            fire.move();
            mFirePaint.setColor(color);
            canvas.drawCircle(fire.getCurX(), fire.getCurY(), fire.radius, mFirePaint);
        }
        canvas.restore();
    }

    /**
     * 画连接时的圆形动画
     */
    private void drawLinkingCircle(Canvas canvas) {
        for (int i = 0; i < mLinkingPath.size(); i++) {
            canvas.save();
            canvas.rotate(mRotate + i, mCircleRect.centerX(), mCircleRect.centerY());

            LinkingLine line = mLinkingPath.get(i);
            SweepGradient sweepGradient = new SweepGradient(mCircleRect.centerX(), mCircleRect.centerY(),
                    Color.argb(0, 255, 255, 255), Color.argb(line.startAlpha, 255, 255, 255));
            mLinkingPaint.setShader(sweepGradient);
            mLinkingPaint.setMaskFilter(new BlurMaskFilter(mRadius, BlurMaskFilter.Blur.NORMAL));

            canvas.drawPath(line.path, mLinkingPaint);
            canvas.restore();
        }
    }

    class LinkingLine {
        Path path;
        int startAlpha;
    }

    class Fire {
        private int dx;
        private int dy;

        private float vx;
        private float vy;

        private float radius;
        private int randomAlpha;
        private int alpha;

        private float x;
        private float y;

        private Random random;
        private int index;

        int MAX_DY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());

        public Fire(int i) {
            this.alpha = 255;
            random = new Random(System.currentTimeMillis() + i * 1000);
            this.index = i;

            reset();
        }

        public void move() {
            dx += vx;
            dy += vy;

            alpha = (int) ((1 - Math.abs(dy) * 1.0f / MAX_DY) * randomAlpha);
            this.radius -= 0.1f;

            if (Math.abs(dy) > MAX_DY)
                reset();
        }

        private void reset() {
            dx = 0;
            dy = 0;
            radius = 30;
            alpha = 255;
            this.radius = random.nextInt(10) + 10;
            randomAlpha = random.nextInt(128) + 127;

            y = mCircleRect.centerY() + index;
            x = mCircleRect.right + mLineWidth *1.0f / index * index + random.nextInt(mLineWidth / 2) - mLineWidth / 2;

            boolean b = random.nextBoolean();
            vx = (random.nextFloat() * 1.5f) * (b ? 1 : -1);
            vy = (random.nextFloat() * 7 + 3) * -1;
        }

        public float getCurX() {
            return x + dx;
        }

        public float getCurY() {
            return y + dy;
        }
    }
}