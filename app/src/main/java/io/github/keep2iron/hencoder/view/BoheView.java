package io.github.keep2iron.hencoder.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/10/17 14:37
 */
public class BoheView extends View {
    private static int UNIT_KG_PIX;                 //最小单位kg对应在屏幕上的像素值
    private final static int MAX_KG_SIZE = 200;     //最大的KG的数

    private Scroller mScroller;                     //滑动的一个帮助类
    private ViewConfiguration mViewConfiguration;   //一个配置常量类
    private VelocityTracker mVelocityTracker;       //速度跟踪器,用于计算手指滑动的初始速度

    public final static int COLOR_DEEP_GRAY = Color.parseColor("#363836");
    public final static int COLOR_GRAY = Color.parseColor("#bcbcbc");
    public final static int COLOR_GREEN = Color.parseColor("#4abb74");

    private Paint mLinePaint;
    private Paint mUnitTextPaint;
    private Rect mTextRect = new Rect();
    private Paint mShowTextPaint;

    PointF mLastPoint = new PointF();

    public BoheView(Context context) {
        this(context, null);
    }

    public BoheView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BoheView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int measureWidth = 0;
//        int computeSize = MAX_KG_SIZE * 10 * UNIT_KG_PIX;
//
//        switch (widthMode) {
//            case MeasureSpec.EXACTLY:
//                measureWidth = Math.max(MeasureSpec.getSize(widthMeasureSpec), computeSize);
//                break;
//            case MeasureSpec.AT_MOST:
//            case MeasureSpec.UNSPECIFIED:
//                measureWidth = computeSize;
//                break;
//        }
//        measureWidth = measureWidth + getPaddingLeft() + getPaddingRight();
//
//        setMeasuredDimension(measureWidth, MeasureSpec.getSize(heightMeasureSpec));
    }

    private void init() {
        mScroller = new Scroller(getContext());
        mViewConfiguration = ViewConfiguration.get(getContext());

        UNIT_KG_PIX = getResources().getDisplayMetrics().widthPixels / 40;

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(1);
        mLinePaint.setColor(COLOR_GRAY);

        mUnitTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnitTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mUnitTextPaint.setStrokeWidth(1);
        mUnitTextPaint.setColor(COLOR_DEEP_GRAY);
        mUnitTextPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics()));

        mShowTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShowTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mShowTextPaint.setStrokeWidth(3);
        mShowTextPaint.setColor(COLOR_GREEN);
        mShowTextPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, getResources().getDisplayMetrics()));
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);

                mLastPoint.x = event.getX();
                mLastPoint.y = event.getY();

                //如果是在滑动过程中打断滑动动画
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                //必要的一步回收速度跟踪器
                if (mVelocityTracker != null)
                    mVelocityTracker.clear();
                mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (mLastPoint.x - event.getX());
                int computeX = getScrollX() + dx;

                Log.e("tag", "scrollX : " + getScrollX());

                if (computeX < 0) {
                    dx = -getScrollX();
                }
                scrollBy(dx, 0);

                mLastPoint.x = event.getX();
                mLastPoint.y = event.getY();

                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);

                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(600, mViewConfiguration.getScaledMaximumFlingVelocity());
                float xVelocity = mVelocityTracker.getXVelocity();      //通过刚刚函数计算600ms的一个瞬时速度
                if (Math.abs(xVelocity) < mViewConfiguration.getScaledMinimumFlingVelocity()) {
                    xVelocity = 0;
                }
                mScroller.startScroll(getScrollX(), getScrollY(), -(int) (xVelocity / 3), 0, 600);
                invalidate();
                mVelocityTracker.recycle();
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if (x < 0) {
                x = 0;
            } else if (x > MAX_KG_SIZE * 10 * UNIT_KG_PIX) {
                x = MAX_KG_SIZE * 10 * UNIT_KG_PIX;
            }

            scrollTo(x, y);
            postInvalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawSize(canvas);
        drawWeightText(canvas);
    }

    /**
     * 画出地中的文本
     */
    private void drawWeightText(Canvas canvas) {
        //水平轴的线
        mLinePaint.setColor(COLOR_GRAY);
        mLinePaint.setStrokeWidth(1);
        canvas.drawLine(0, getHeight() / 2, MAX_KG_SIZE * 10 * UNIT_KG_PIX, getHeight() / 2, mLinePaint);

        int centerX = getScrollX() + getWidth() / 2;
        int dp60 = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics()));
        mLinePaint.setStrokeWidth(5);
        mLinePaint.setColor(COLOR_GREEN);
        canvas.drawRect(centerX - 6, getHeight() / 2, centerX + 6, getHeight() / 2 + dp60, mLinePaint);

        float kg = (getScrollX() + getWidth() / 2) / UNIT_KG_PIX / 10.f;
        String text = kg + "";
        mShowTextPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, getResources().getDisplayMetrics()));
        mShowTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
        Paint.FontMetrics fontMetrics = mShowTextPaint.getFontMetrics();
        int baseLine = (int) (getHeight() / 2 - (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.top - mTextRect.height() - 30);
        canvas.drawText(text, 0, text.length(), centerX - mTextRect.width() / 2, baseLine, mShowTextPaint);

        int top = (int) fontMetrics.top;
        int sp18 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics());
        mShowTextPaint.setTextSize(sp18);
        fontMetrics = mShowTextPaint.getFontMetrics();
        int unitBaseLine = (int) (baseLine - ((fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.top) + sp18 / 2);
        canvas.drawText("kg", 0, "kg".length(), centerX + mTextRect.width() / 2 + 50, unitBaseLine, mShowTextPaint);
    }

    /**
     * 画出尺寸的文本信息
     */
    private void drawSize(Canvas canvas) {
        Paint.FontMetrics fontMetrics = mUnitTextPaint.getFontMetrics();
        int lineHeight;

        for (int i = -20; i < MAX_KG_SIZE * 10; i++) {
            if (i % 10 == 0) {        //画KG线
                mLinePaint.setColor(COLOR_GRAY);
                mLinePaint.setStrokeWidth(3);
                lineHeight = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()));
                if (i >= 0) {
                    String text = String.valueOf(i / 10);
                    mUnitTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
                    int baseLineY = (int) (getHeight() / 2 + lineHeight + (fontMetrics.bottom - fontMetrics.top) / 2 + (-fontMetrics.top));

                    canvas.drawText(text, 0, text.length(),
                            i * UNIT_KG_PIX - mTextRect.width() / 2, baseLineY, mUnitTextPaint);
                }
            } else {                  //画普通的线
                mLinePaint.setColor(COLOR_GRAY);
                mLinePaint.setStrokeWidth(1);
                lineHeight = (int) (+TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()));
            }
            canvas.drawLine(i * UNIT_KG_PIX, getHeight() / 2,
                    i * UNIT_KG_PIX, getHeight() / 2 + lineHeight, mLinePaint);
        }
    }
}