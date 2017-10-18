package io.github.keep2iron.hencoder.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import io.github.keep2iron.hencoder.R;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/10/16 10:48
 */
public class JiKeView extends View implements View.OnClickListener {
    private float mProgress;        //点击之后的手指进度
    private float mScale;           //点击之后的手指缩放
    private int mColor;             //选中的开始的颜色根据isClick判断只能是 COLOR_ORANGE和COLOR_GRAY的其中一种
    private boolean isClick;        //是否已经被点击

    private Paint mZanPaint;        //手指上方的的四个点的画笔
    private Paint mFingerPaint;     //手指画笔
    private Paint mNumberPaint;  //当前的数字的画笔

    //手指的绘制区域
    private int mFingerWidth = 55;
    private int mFingerHeight = 45;

    private int mCurrentNumber = 4309;
    private int mNextNumber;

    private int mNumberWidth;
    private int mNumberHeight;

    private int mChangeBitCount;    //进位或者退位产生的变化数
    private float mNumberProgress;

    public final static int COLOR_ORANGE = Color.parseColor("#e56a45");
    public final static int COLOR_GRAY = Color.parseColor("#cccdcb");
    private PaintFlagsDrawFilter mCanvasFilter;

    private int mFingerNumberPadding;

    public JiKeView(Context context) {
        this(context, null);
    }

    public JiKeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JiKeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = getResources().obtainAttributes(attrs, R.styleable.JiKeView);
        for (int i = 0; i < array.length(); i++) {
            switch (array.getIndex(i)) {
                case R.styleable.JiKeView_jv_number:
                    mCurrentNumber = array.getInteger(array.getIndex(i), 1);
                    break;
                case R.styleable.JiKeView_jv_is_click:
                    isClick = array.getBoolean(array.getIndex(i), false);
                    break;
            }
        }
        array.recycle();

        initView();
    }

    private void initView() {
        mScale = 1.0f;
        mColor = COLOR_GRAY;

        mFingerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFingerPaint.setStyle(Paint.Style.STROKE);
        mFingerPaint.setStrokeWidth(4);
        mFingerPaint.setColor(mColor);

        mZanPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mZanPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mZanPaint.setStrokeWidth(4);
        mZanPaint.setColor(COLOR_ORANGE);
        mZanPaint.setStrokeCap(Paint.Cap.ROUND);        //设置圆角

        mNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNumberPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mNumberPaint.setStrokeWidth(1);
        mNumberPaint.setColor(COLOR_GRAY);
        mNumberPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 19, getResources().getDisplayMetrics()));

        Rect drawNumberRect = new Rect();
        mNumberPaint.getTextBounds("0123456789", 0, "0123456789".length(), drawNumberRect);
        mNumberWidth = drawNumberRect.width() / "0123456789".length();
        mNumberHeight = drawNumberRect.height();

        mProgress = 1.0f;

        mCanvasFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        mFingerNumberPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

        setOnClickListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(mCanvasFilter);

        drawZanByPath(canvas);
        drawZanNumber(canvas);
    }

    /**
     * 画出赞的数字
     */
    private void drawZanNumber(Canvas canvas) {
        char[] currentArray = (mCurrentNumber + "").toCharArray();
        Paint.FontMetrics fontMetrics = mNumberPaint.getFontMetrics();
        int width = mNumberWidth;
        int height = mNumberHeight * 3;

        int startX = getWidth() / 2 + mFingerNumberPadding / 2;

        //因为在安卓中top 和 bottom都是相对于baseline的距离，bottom为正，因为在下方，top为负因为在上方
        int currBaseLine = (int) (getHeight() / 2 - (fontMetrics.bottom - fontMetrics.top) / 2 + (-fontMetrics.top));

        //如果是下一页，从中间往上面翻，如果上一页，从中间往下面翻
        int currChangeBitBaseLine = (int) (currBaseLine + (isClick ? -height * mNumberProgress : height * mNumberProgress));
        for (int i = 0; i < currentArray.length; i++) {
            //如果当前位是变化位,则进行透明度和平移动画
            if (i >= currentArray.length - mChangeBitCount) {
                //如果是下一页，透明度在增加,如果是上一页，透明度在减小
                mNumberPaint.setColor(
                        Color.argb((int) (255 * (1 - mNumberProgress)),
                                (COLOR_GRAY >> 16) & 0xFF,
                                (COLOR_GRAY >> 8) & 0xFF,
                                (COLOR_GRAY) & 0xFF));
                canvas.drawText(currentArray[i] + "", startX + width * i, currChangeBitBaseLine, mNumberPaint);
            } else {
                mNumberPaint.setColor(COLOR_GRAY);
                canvas.drawText(currentArray[i] + "", startX + width * i, currBaseLine, mNumberPaint);
            }
        }

        int nextNumber = mNextNumber;
//        Log.e("tag","mNumberProgress : " + mNumberProgress + " nextNumber : " + nextNumber + " currentNumber : " + mCurrentNumber);
        //如果是下一页，下一位从底部进行向中间移动,如果是上一页从上方往中间
        int nextBaseLine = (int) (currBaseLine + (isClick ? height * (1 - mNumberProgress) : -height * (1 - mNumberProgress)));
        char[] nextArray = (nextNumber + "").toCharArray();

        //如果由于退位产生的 改变位数大于了nextNUmber，则全部进行更换
        for (int i = nextArray.length - mChangeBitCount < 0 ?
                0 : nextArray.length - mChangeBitCount;
             i < nextArray.length; i++) {
            mNumberPaint.setColor(
                    Color.argb((int) (255 * mNumberProgress),
                            (COLOR_GRAY >> 16) & 0xFF,
                            (COLOR_GRAY >> 8) & 0xFF,
                            (COLOR_GRAY) & 0xFF));
            canvas.drawText(nextArray[i] + "", startX + width * i, nextBaseLine, mNumberPaint);
        }

        if (mNumberProgress >= 1.0f && mNextNumber != mCurrentNumber) {
            mCurrentNumber = mNextNumber;
        }
    }

    /**
     * 画赞的左边的部分
     */
    private void drawZanByPath(Canvas canvas) {
        int startX = (getWidth() / 2 - mFingerWidth) - mFingerNumberPadding / 2 + 10, startY = (getHeight() - mFingerHeight) / 2 + 25;

        //画手指
        int color = getCurrentColor(mColor, isClick ? COLOR_ORANGE : COLOR_GRAY, mProgress);
        mFingerPaint.setColor(color);
        if (mProgress >= 1.0f) {
            mColor = color;
        }

        canvas.save();
        canvas.scale(mScale + 0.7f, mScale + 0.7f, startX, startY);
        //不解释代码，下面的代码经过了一些计算得到的，不要问为什么这么写，因为就是慢慢计算得到的.
        Path finger = new Path();
        finger.moveTo(startX, startY);
        finger.rLineTo(10, -4);
        finger.rQuadTo(1.7f, -1.7f, 2, -2);
        finger.rLineTo(0, -12);
        finger.rQuadTo(4, -5, 10, 0);
        finger.rLineTo(0, 10);
        finger.rQuadTo(1.7f, 1.7f, 2, 2);
        finger.rLineTo(10, -1f);
        finger.rQuadTo(1.7f, 1.7f, 2, 2);
        finger.rLineTo(-2, 18);
        finger.rQuadTo(-1.4f, 1.4f, -2, 2);
        finger.rLineTo(-25f, 2f);
        finger.rLineTo(-4, -16);
        finger.rLineTo(4, 16);
        finger.rLineTo(-8, 0.8f);
        finger.rQuadTo(-1.4f, -1.4f, -2, -2);
        finger.rLineTo(-3, -12);
        finger.rQuadTo(1.4f, -1.4f, 2, -2);
        finger.close();

        canvas.drawPath(finger, mFingerPaint);

        float lineLength = 5.0f;
        if (!isClick) {         //如果没有被点击状态,进行透明度动画
            lineLength = 5.0f;
            int zanColor = Color.argb((int) (255 * (1.0f - mProgress)), Color.red(COLOR_ORANGE), Color.green(COLOR_ORANGE), Color.blue(COLOR_ORANGE));
            mZanPaint.setColor(zanColor);
        } else {                  //如果是点击状态则进行线段长度的增长动画
            mZanPaint.setColor(COLOR_ORANGE);
            lineLength *= mProgress;
        }
        canvas.rotate(-135, startX + 16, startY - 25 + 3);
        for (int i = 0; i < 4; i++) {
            //画手指上面的赞
            Path zan = new Path();
            //先移动到手指的上方的位置上的5像素处
            zan.moveTo(startX + 16, startY - 25 - 10);
            canvas.rotate(45, startX + 16, startY - 25);
            zan.rLineTo(0, -lineLength);
            canvas.drawPath(zan, mZanPaint);
        }

        canvas.restore();
    }

    @Override
    public void onClick(View v) {
        int duration = 120;
        isClick = !isClick;

        mChangeBitCount = 1;
        mNextNumber = mCurrentNumber;
        if (isClick) mNextNumber++;
        else mNextNumber--;

        int nextNumber = mNextNumber;

        //如果点击之后产生了进位，变化的位数是
        if (nextNumber % 10 == 0) {
            int temp = nextNumber;
            while (temp % 10 == 0) {
                temp /= 10;
                mChangeBitCount++;
            }
        } else if (mCurrentNumber % 10 == 0 && !isClick) {
            int temp = mCurrentNumber;
            while (temp % 10 == 0) {
                temp /= 10;
                mChangeBitCount++;
            }
        }


        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(1f, 1.15f, 1.0f)
                .setDuration(duration);
        scaleAnimator.setInterpolator(new AccelerateInterpolator(1.0f));
        scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mScale = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        scaleAnimator.start();

        ValueAnimator colorAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
                .setDuration(duration);
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgress = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        colorAnimator.start();

        ValueAnimator numberAnimator = ValueAnimator.ofFloat(0, 1.0f)
                .setDuration((long) (duration * 1.6));
        numberAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mNumberProgress = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        numberAnimator.start();
    }

    private int getCurrentColor(int startColor, int endColor, float percent) {
        int red = (int) (Color.red(startColor) + (Color.red(endColor) - Color.red(startColor)) * percent);
        int green = (int) (Color.green(startColor) + (Color.green(endColor) - Color.green(startColor)) * percent);
        int blue = (int) (Color.blue(startColor) + (Color.blue(endColor) - Color.blue(startColor)) * percent);

        return Color.rgb(red, green, blue);
    }
}