package com.juzi.duotulockscreen.View;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.juzi.duotulockscreen.util.DisplayUtil;

public class LockView extends ImageView {

	/**
	 * 半透明区域的高度
	 */
	private int mRadius;

    /**
     * 解锁时解锁效果跟手的系数，系数越大解锁反馈越明显
     */
    private final float UNLOCK_RADIO = 1.6f;

	/**
	 * 上划解锁时需要动态画的半透明区域
	 */
	private Rect mDestBottomRect = new Rect();

	/**
	 * 上划解锁时的可见区域
	 */
	private Rect mDestTopRect = new Rect();
    /**
     * 原图的下半部分
     */
	private Rect mSrcBottomRect = new Rect();
    // 原图bitmap中要被画的上半部分
    private Rect mSrcTopRect = new Rect();

    // 原图biamap的宽高
    private int mBitmapH, mBitmapW;

	/**
	 * 画半透明区域时的canvas Y 轴偏移量
	 */
	private int mBlurRectTransY;

	/**
	 * 屏幕宽高
	 */
	private int mWidth, mHeight;

    /**
     * 手指落点
     */
	private int mDownY;

    /**
     * 手指滑动的最高点，如果抬手的点比最高点小于一定值，就认为用户并不想解锁
     */
    private int mMinY;
    /**
     * 如果抬手的点比最高点小于一定值，就认为用户并不想解锁，差值
     */
    private int mLockDelta;

    /**
	 * 上划时画半透区域的画布
	 */
	private Canvas mCanvas;

	/**
	 * 上划时画半透区域的paint
	 */
	private static Paint sBlurPaint;
    private Context context;

	/**
	 * 这个imagview的bitmap
	 */
	private Bitmap mImageBitmap;

    /**
     * 要用来画半透明区域bitmap，是自己创建的bitmap，所以尽量小，尽量能公用
     */
    private static Bitmap sBlurBitmap;

	/**
	 * 是否在触摸屏幕
	 */
	boolean isTouch = false;

	public LockView(Context context) {
		super(context);
		this.context = context;
        init();
	}

	public LockView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	public void init() {
        Point realScreenPoint = DisplayUtil.getRealScreenPoint(context);
        mWidth = realScreenPoint.x;
		mHeight = realScreenPoint.y;
//        mWidth = context.getResources().getDisplayMetrics().widthPixels;
//		mHeight = context.getResources().getDisplayMetrics().heightPixels;
        mRadius = DisplayUtil.dip2px(context, 200);
        mLockDelta = DisplayUtil.dip2px(context, 80);

		mDestBottomRect.set(0, 0, mWidth, mRadius);

        if (sBlurPaint == null) {
            sBlurPaint = new Paint();
            //线程渐变，用来画遮罩
            LinearGradient linearGradient = new LinearGradient(0, 0, 0, mRadius, 0xFFFFFFFF, 0x00FFFFFF, TileMode.MIRROR);
            //遮罩
            PorterDuffXfermode XFermode = new PorterDuffXfermode(Mode.DST_ATOP);
            sBlurPaint.setShader(linearGradient);
            sBlurPaint.setXfermode(XFermode);
        }
        if (sBlurBitmap == null) {
            sBlurBitmap = Bitmap.createBitmap(mWidth, mRadius, Config.ARGB_8888);
        }
		mCanvas = new Canvas(sBlurBitmap);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		this.mImageBitmap = bm;
		mBitmapH = mImageBitmap.getHeight();
		mBitmapW = mImageBitmap.getWidth();
	}


	@Override
	public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        int action = event.getAction();
        int y = (int) event.getY();
        if (y < mMinY) {
            mMinY = y;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mMinY = mDownY = y;
                if (mImageBitmap == null) {
                    buildDrawingCache();
                    // setDrawingCacheEnabled(true);
                    if (getDrawingCache() != null) {
                        mImageBitmap = Bitmap.createBitmap(getDrawingCache());
                        mBitmapH = mImageBitmap.getHeight();
                        mBitmapW = mImageBitmap.getWidth();
                    }
                    destroyDrawingCache();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaY = mDownY - y;
                if (deltaY > 20 || isTouch) {
                    isTouch = true;
//                    int foreGroundbottom = mHeight - mRadius / 2 - deltaY;
                    int foreGroundbottom = (int) Math.min(mHeight, mHeight - UNLOCK_RADIO * deltaY);
                    calcDisplayRect(foreGroundbottom);
                } else {
                    isTouch = false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                if (mDownY - y > mLockDelta
                        && y - mMinY < mLockDelta) {
                    // unLock anim
                    startAnim(mDestTopRect.bottom, 0);
                } else {
                    // lock anim
                    startAnim(mDestTopRect.bottom, mHeight);
                }
                break;

            default:
                break;
        }
        invalidate();
        return isTouch || super.dispatchTouchEvent(event);
        //return true;
    }

	/**
	 * 此效果分两部分，上面是原图清晰的部分，下面是带遮罩效果的由不透明到全透明的过渡区域，foreGroundbottom
	 * 这个数值代表此两部分分界线
	 */
	private void calcDisplayRect(int foreGroundbottom) {
		// 要把原图画到目标屏幕上，分成两部分，上面是不透明部分，下面是透明渐变部分
		// 所以我们要知道4个方框：原图的上半部分方框，原图的半透明部分方框，目标屏幕的上半部分，目标屏幕的下半部分。

		// 目标屏幕要画的两个区域大小基于我们手指上划的距离，传进来的foreGroundbottom代表目标屏幕上面区域的底边
		// 所以可以根据这个值很容易计算出目标屏幕的两个方框，如下：
		// 1，目标屏幕的上边部分
		mDestTopRect.set(0, 0, mWidth, foreGroundbottom);
		// 2，目标屏幕的下半部分
		// 其实就是我们根据透明半径创建的mBlurRect大小， mDestBottomRect.set(0, 0, mWidth, mRadius)，
		// 我们在画时动态的向下移动canvas即可，移动的距离即：
		mBlurRectTransY = foreGroundbottom;
        if (mOnUnLockListener != null) {
            mOnUnLockListener.onUnLocking(mBlurRectTransY * 1.0f / mHeight);
        }

		// 3，原图的上半部分，原图的宽高为mBitmapH, mBitmapW,左上都是0，右边是宽，我们只需要计算下边就可以，即：
		float bottom = mBitmapH * ((float) mBlurRectTransY / mHeight); // ----根据目标屏幕上半部分的比例，来计算原图大小。
		mSrcTopRect.set(0, 0, mBitmapW, (int) bottom);

		// 4,原图的下半部分，我们需要根据目标屏幕中下半部分方框高所占的比例，来计算出原图中半透明区域的高度：
		float radiusInBitmap = mBitmapH * ((float) mRadius / mHeight);
		mSrcBottomRect.set(0, (int) bottom, mBitmapW, (int) (bottom + radiusInBitmap));
	}

	private void startAnim(int start, final int end) {
//        if (start == end) {//开头和结尾相同，不用做动画了
//            calcDisplayRect(end);
//            isTouch = false;
//            if (mOnUnLockListener != null) {
//                if (end == 0) {
//                    mOnUnLockListener.onUnLockSuccess();
//                } else {
//                    mOnUnLockListener.onUnLockFailed();
//                }
//            }
//            return;
//        }

		ValueAnimator anim = ValueAnimator.ofInt(start, end);
		int d = (Math.min(300, mDestTopRect.bottom >> 2) > 0) ? Math.min(300, mDestTopRect.bottom >> 2) : 0;
        anim.setDuration(d);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int foreGroundbottom = (Integer) animation.getAnimatedValue();
                calcDisplayRect(foreGroundbottom);
                invalidate();
            }
        });
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				isTouch = false;
                if (mOnUnLockListener != null) {
                    if (end == 0) {
                        mOnUnLockListener.onUnLockSuccess();
                    } else {
                        mOnUnLockListener.onUnLockFailed();
                    }
                }
			}
		});
		anim.start();
	}

	@Override
	protected void onDraw(@NonNull Canvas canvas) {
		if (isTouch) {
			drawForeGround(canvas);
		} else {
			super.onDraw(canvas);
		}
	}

	private void drawForeGround(Canvas canvas) {
		canvas.drawBitmap(mImageBitmap, mSrcTopRect, mDestTopRect, null);

		mCanvas.drawBitmap(mImageBitmap, mSrcBottomRect, mDestBottomRect, null);
		mCanvas.drawRect(mDestBottomRect, sBlurPaint);

		canvas.save();
		canvas.translate(0, mBlurRectTransY);
		canvas.drawBitmap(sBlurBitmap, 0, 0, null);
		canvas.restore();
	}

    public interface onUnLockListener {
        void onUnLockSuccess();
        void onUnLockFailed();
        void onUnLocking(float f);
    }

    private onUnLockListener mOnUnLockListener;

    public void setOnUnLockListener(onUnLockListener onUnLockListener) {
        mOnUnLockListener = onUnLockListener;
    }
}
