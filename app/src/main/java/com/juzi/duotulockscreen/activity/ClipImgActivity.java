package com.juzi.duotulockscreen.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.util.FileUtil;
import com.juzi.duotulockscreen.util.ToastManager;
import com.juzi.duotulockscreen.view.ClipConveredView;
import com.juzi.duotulockscreen.view.ZoomImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class ClipImgActivity extends BaseActivity implements View.OnClickListener {
    private Handler mHandler = new Handler();
    private ZoomImageView mClipImageSrcImage;
    private TextView mTvCancel;
    private TextView mTvConfirm;
    private ClipConveredView mClipImageConverView;
    public static final String KEY_INTENT_CLIPIMG_SRC_PATH = "img_path";
    public static final String KEY_INTENT_CLIPIMG_DONE_PATH = "cliped_path";
    public static final String KEY_INTENT_CLIPIMG_PREFIX = "prefix";
    private String mCutimgName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clipimg);
        assignview();
    }

    @Override
    protected boolean getIsFullScreen() {
        return true;
    }

    private void assignview() {
        final Dialog mLoadingProgress = new Dialog(this, R.style.loading_progress);
        mLoadingProgress.setContentView(R.layout.loading_progressbar_bai);
        mLoadingProgress.setCancelable(false);
        mLoadingProgress.show();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoadingProgress.dismiss();
            }
        }, 400);

        mClipImageSrcImage = (ZoomImageView) findViewById(R.id.clip_image_src_image);
        mTvCancel = (TextView) findViewById(R.id.tv_cancel);
        mTvConfirm = (TextView) findViewById(R.id.tv_confirm);
        mClipImageConverView = (ClipConveredView) findViewById(R.id.clip_image_conver_view);

        mTvCancel.setOnClickListener(this);
        mTvConfirm.setOnClickListener(this);
        String imgPrefix = getIntent().getStringExtra(KEY_INTENT_CLIPIMG_PREFIX);
        final String path = getIntent().getStringExtra(KEY_INTENT_CLIPIMG_SRC_PATH);
        if (TextUtils.isEmpty(path)) {
            ToastManager.showShort(this, "图片路径为空");
            return;
        } else {
            mCutimgName = imgPrefix + path.substring(path.lastIndexOf("/") + 1);
        }
        Log.d("wangzixu", "assignview path = " + path);
        mClipImageConverView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect clipRect = mClipImageConverView.getClipRect();
                mClipImageSrcImage.setScaleType(ImageView.ScaleType.MATRIX);
                mClipImageSrcImage.setClipRect(new RectF(clipRect));
                mClipImageSrcImage.setEdgeRect(mClipImageSrcImage.getClipRect());
                mClipImageSrcImage.setMaxMinScale(4.0f, .01f);
                ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(path), mClipImageSrcImage,
                        new DisplayImageOptions.Builder()
                                .cacheInMemory(false)
                                .cacheOnDisk(false)
                                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                                .build(),
                        new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        Log.d("wangzixu", "onLoadingStarted");
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        Log.d("wangzixu", "onLoadingFailed");
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        Log.d("wangzixu", "onLoadingComplete loadedImage = " + loadedImage);
                        mClipImageSrcImage.setImageBitmap(loadedImage);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        Log.d("wangzixu", "onLoadingCancelled");
                    }
                });
                mClipImageConverView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel: // 取消
                setResult(RESULT_CANCELED);
                onBackPressed();
                break;
            case R.id.tv_confirm: //剪裁
                final Dialog mLoadingProgress = new Dialog(this, R.style.loading_progress);
                mLoadingProgress.setContentView(R.layout.loading_progressbar_clip);
                mLoadingProgress.setCancelable(false);
                mLoadingProgress.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String path = clipImage();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mLoadingProgress.dismiss();
                                if (TextUtils.isEmpty(path)) {
                                    setResult(RESULT_CANCELED);
                                } else {
                                    Intent data = new Intent();
                                    data.putExtra(KEY_INTENT_CLIPIMG_DONE_PATH, path);
                                    setResult(RESULT_OK, data);
                                }
                                onBackPressed();
                            }
                        });
                    }
                }).start();
                break;
            default:
                break;
        }
    }

    private String clipImage() {
        String path = null;
        Matrix matrix = mClipImageSrcImage.getmMatrix();
        float[] f = new float[9];
        matrix.getValues(f);
        RectF clipRect = mClipImageSrcImage.getClipRect();
        float scale = f[Matrix.MSCALE_X];
        float transX = clipRect.left - f[Matrix.MTRANS_X];
        float transY = clipRect.top - f[Matrix.MTRANS_Y];
        float offsetX = Math.max(0, transX / scale);
        float offsetY = Math.max(0, transY / scale);
        float clipWidth = clipRect.width() / scale;
        float clipHight = clipRect.height() / scale;
        Log.d("wangzixu", "clipImage x, y = " + offsetX + ", " + offsetY);

        Bitmap destBitmap = getClipBitmap((int) offsetX, (int) offsetY, (int) clipWidth, (int) clipHight);
        if (destBitmap != null) {
            path = FileUtil.saveImgToInnerData(this, destBitmap, mCutimgName);
        }
        return path;
    }

    private Bitmap getClipBitmap(int offsetX, int offsetY, int clipWidth, int clipHight) {
        Bitmap source = mClipImageSrcImage.getOriginalBmp();
        if (source == null) {
            return null;
        }

        Bitmap destBitmap = null;
        try {
            Canvas canvas = new Canvas();
            Bitmap.Config newConfig = Bitmap.Config.ARGB_8888;
            final Bitmap.Config config = source.getConfig();
            if (config != null) {
                switch (config) {
                    case RGB_565:
                        newConfig = Bitmap.Config.RGB_565;
                        break;
                    case ALPHA_8:
                        newConfig = Bitmap.Config.ALPHA_8;
                        break;
                    // noinspection deprecation
                    case ARGB_4444:
                    case ARGB_8888:
                    default:
                        newConfig = Bitmap.Config.ARGB_8888;
                        break;
                }
            }
            destBitmap = Bitmap.createBitmap(mClipImageConverView.getScreenWidth(), mClipImageConverView.getScreenHeight(), newConfig);
            Rect srcR = new Rect(offsetX, offsetY, offsetX + clipWidth, offsetY + clipHight);
            RectF dstR = new RectF(0, 0, mClipImageConverView.getScreenWidth(), mClipImageConverView.getScreenHeight());

            destBitmap.setDensity(source.getDensity());
            destBitmap.setHasAlpha(source.hasAlpha());
            // destBitmap.setPremultiplied(source.isPremultiplied()); // api
            // 19

            canvas.setBitmap(destBitmap);
            canvas.drawBitmap(source, srcR, dstR, null);
            canvas.setBitmap(null);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return destBitmap;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_in_left2right, R.anim.activity_out_left2right);
    }
}
