package com.juzi.duotulockscreen.view;

import android.content.Context;
import android.os.SystemClock;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * 可以支持图片放大缩小的viewpager，需要配合zoomimage一起实现
 */
public class ZoomImageViewPager extends ViewPager {

    private ZoomImageView mImgView = null;
	private int mCurrentPageEdgeScrollX;
	private int mPageWidth = 0;
	private float mLastX;
	private int mInitItem = 0;
	private final int mTouchSlop;
	private float mPerformDownX;
    private long mPerformDownTime;
    private boolean mPerformClik = false;


    public ZoomImageViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
//        mTouchSlop = DisplayUtil.dip2px(context, 5);
	}
	
	public void setZoomImageView(ZoomImageView view) {
		if (view == null) {
			throw new RuntimeException("setZoomImageView view == null!");
		}
		mImgView = view;
		int currentItem = getCurrentItem();
		mPageWidth = getWidth();
		if (mPageWidth != 0) {
		    mCurrentPageEdgeScrollX = (currentItem - mInitItem) * getWidth();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	    super.onSizeChanged(w, h, oldw, oldh);
        mPageWidth = getWidth();
        if (mPageWidth != 0) {
            mCurrentPageEdgeScrollX = (getCurrentItem() - mInitItem) * getWidth();
        }
	}

	public void setInitCurrentItem(int item) {
	    mInitItem = item;
	    super.setCurrentItem(item);
	}

    private boolean isFirstMove;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    try {
            //判断该viewpager是应该响应自己的左右滑动，还是应该判断是否要调用mImgView.hanleTouchEvent来处理
	    	if (mImgView == null || (getScrollX() != mCurrentPageEdgeScrollX && !mImgView.canHorizontalDrag())) {
		        return super.onTouchEvent(event);
		    }
			int actionMasked = event.getActionMasked();
            switch (actionMasked) {
                case MotionEvent.ACTION_DOWN:
                    //模拟一个onClick事件
                    mLastX = event.getX();
                    mPerformClik = true;
                    mPerformDownX = mLastX;
                    mPerformDownTime = SystemClock.uptimeMillis();
                    break;
                case MotionEvent.ACTION_MOVE:
                    //由viewpager滑动转向图片拖动的过程
                    float x = event.getX();
                    if (mImgView.canHorizontalDrag() && mImgView.getTouchMode() == ZoomImageView.NONE) {
                        float deltaX = x - mLastX;
                        if (isFirstMove) {
                            if (Math.abs(deltaX) < mTouchSlop) {
                                break;
                            } else {
                                isFirstMove = false;
                                mLastX = x;
                                break;
                            }
                        }

                        mLastX = x;
                        int scrollX = getScrollX();
                        //往右滑动手指，delta为正，scrollx逐渐减小，所以，scrollx减去delta，即目标位置
                        if ((mImgView.getIsOnLeftSide() && scrollX - deltaX > mCurrentPageEdgeScrollX) ||
                                //新位置大于边缘值，说明图片在左边缘，向左划，所以应该使图片响应拖动
                                (mImgView.getIsOnRightSide() && scrollX - deltaX < mCurrentPageEdgeScrollX)) {
                            //新位置小于边缘值，说明图片在右边缘，向右划，所以应该使图片响应拖动
                            scrollTo(mCurrentPageEdgeScrollX, 0);
                            event.setAction(MotionEvent.ACTION_DOWN);
                        }
                    }
                    if (mPerformClik && Math.abs(x - mPerformDownX) > mTouchSlop) {
                        mPerformClik = false;
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (getScrollX() == mCurrentPageEdgeScrollX) {
                        mImgView.setCanZoom(true);
                    } else {
                        mImgView.setCanZoom(false);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mPerformClik && mOnCustomClikListener != null && (SystemClock.uptimeMillis() - mPerformDownTime) < 300) { //
                        float xup = event.getX();
                        if (xup - mPerformDownX < mTouchSlop) {
                            mOnCustomClikListener.onCustomClick(mImgView);
                        }
                    }
                    break;
                default:
                    break;
            }

            int imgMode = mImgView.hanleTouchEvent(event, getScrollX() == mCurrentPageEdgeScrollX);
            if (imgMode != ZoomImageView.NONE) {
                isFirstMove = true;
                return true;
            }

//            if (actionMasked == MotionEvent.ACTION_DOWN) {
//                //模拟一个onClick事件
//				mLastX = event.getX();
//                mPerformClik = true;
//                mPerformDownX = mLastX;
//                mPerformDownTime = SystemClock.uptimeMillis();
//			} else if (actionMasked == MotionEvent.ACTION_MOVE) {
//                //由viewpager滑动转向图片拖动的过程
//                float x = event.getX();
//			    if (mImgView.canHorizontalDrag() && mImgView.getTouchMode() == ZoomImageView.NONE) {
//                    float deltaX = x - mLastX;
//                    if (isFirstMove) {
//                        if (Math.abs(deltaX) < mTouchSlop) {
//
//                        } else {
//                            isFirstMove = false;
//                        }
//                    }
//
//                    mLastX = x;
//                    int scrollX = getScrollX();
//                    //往右滑动手指，delta为正，scrollx逐渐减小，所以，scrollx减去delta，即目标位置
//                    if ((mImgView.getIsOnLeftSide() && scrollX - deltaX > mCurrentPageEdgeScrollX) ||
//                        //新位置大于边缘值，说明图片在左边缘，向左划，所以应该使图片响应拖动
//                        (mImgView.getIsOnRightSide() && scrollX - deltaX < mCurrentPageEdgeScrollX)) {
//                        //新位置小于边缘值，说明图片在右边缘，向右划，所以应该使图片响应拖动
//                        scrollTo(mCurrentPageEdgeScrollX, 0);
//                        event.setAction(MotionEvent.ACTION_DOWN);
//                    }
//			    }
//                if (x - mPerformDownX > mTouchSlop) {
//                    mPerformClik = false;
//                }
//			} else if (actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
//			    if (getScrollX() == mCurrentPageEdgeScrollX) {
//			        mImgView.setCanZoom(true);
//			    } else {
//			        mImgView.setCanZoom(false);
//			    }
//			} else if (actionMasked == MotionEvent.ACTION_UP) {
//                if (mPerformClik && mOnCustomClikListener != null && (SystemClock.uptimeMillis() - mPerformDownTime) < 150) { //
//                    float x = event.getX();
//                    if (x - mPerformDownX < mTouchSlop) {
//                        mOnCustomClikListener.onCustomClick(mImgView);
//                    }
//                }
//            }
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return super.onTouchEvent(event);
	}


    private onCustomClikListener mOnCustomClikListener;
    public interface onCustomClikListener {
        void onCustomClick(ZoomImageView zoomImageView);
    }

    public void setOnCustomClikListener(onCustomClikListener onCustomClikListener) {
        mOnCustomClikListener = onCustomClikListener;
    }

}
