package com.juzi.duotulockscreen.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.juzi.duotulockscreen.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ImageLoaderManager {
    public static final String TAG = "ImageLoaderManager ";
    private static ImageLoaderManager sInstance;
    private ImageLoaderManager(){}

    private DisplayImageOptions display_img_options;

    public static final int IMAGE_LOAD_DURATION = 200;
    public static DecelerateInterpolator sInterpolator = new DecelerateInterpolator();

    /**
     * 单例
     */
    public static ImageLoaderManager getInstance() {
        if (sInstance == null) {
            synchronized (ImageLoader.class) {
                if (sInstance == null) {
                    sInstance = new ImageLoaderManager();
                }
            }
        }
        return sInstance;
    }

    public void loadLocalPic(String path, ImageView img) {
        ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(path), img);
    }

	/**
	 * 初始化Android-Universal-Image-Loader,只需要在应用启动时调用一次
	 */
	public static void initImageLoader(Context context) {
        //设置默认的desplayImageOptions
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.cacheOnDisk(true)
				.build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.writeDebugLogs() //是否打印加载图片过程的log，调试时使用
				.defaultDisplayImageOptions(defaultOptions)
				.threadPriority(Thread.NORM_PRIORITY)// 设置线程的优先级
				.denyCacheImageMultipleSizesInMemory()// 当同一个Uri获取不同大小的图片，缓存到内存时，只缓存一个。默认会缓存多个不同的大小的相同图片
				.diskCacheSize(200 * 1024 * 1024) //设置硬盘缓存大小，默认是大小无限制的
				.memoryCacheSizePercentage(25) //设置内存缓存大小位1/4，默认是应用分配内存的 1/8
				.threadPoolSize(5) // 线程池数量
                .tasksProcessingOrder(QueueProcessingType.FIFO)
				.build();

		ImageLoader.getInstance().init(config); // 全局初始化此配置
	}

    public void loadThumbnailFromWeb(String uri, NonViewAware viewAware,
                                     ImageLoadingListener loadingListener, ImageLoadingProgressListener progressListener) {
        loadThumbnailFromWeb(uri, viewAware, null, loadingListener, progressListener);
    }

	/**
	 * 使用方法前务必确保img的宽高已经被赋值，否则会使用屏幕宽高,使用imageAware，因为其中可以填充宽高
	 */
	public void loadThumbnailFromWeb(String uri, NonViewAware viewAware, DisplayImageOptions options,
                                    ImageLoadingListener loadingListener, ImageLoadingProgressListener progressListener) {
        if (display_img_options == null) {
            display_img_options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .build();
        }
        if (options == null) {
            options = display_img_options;
        }

        try {
            ImageLoader.getInstance().displayImage(uri, viewAware, options, loadingListener, progressListener);
        } catch(Exception e) {
            LogHelper.i(TAG, "loadThumbnailFromWeb something wrongm, uri = " + uri);
            e.printStackTrace();
        }
	}

    public static class ImgHolder {
        private NonViewAware imgAware;
        //用来设置图片开始加载和结束加载时监听器，复用这个监听器，不用每次都new
        public SimpleImageLoadingListener simpleImageLoadingListener;
        public ImageLoadingProgressListener imageLoadingProgressListener;
        public AlphaAnimation fadeImage;

        public ImgHolder(int imgWidth, int imgHeigh) {
            imgAware = new NonViewAware(null, new ImageSize(imgWidth,imgHeigh), ViewScaleType.CROP);
            fadeImage = new AlphaAnimation(0, 1);
            fadeImage.setDuration(IMAGE_LOAD_DURATION);
            fadeImage.setInterpolator(sInterpolator);
        }
    }

    public void asyncLoadImage(final ImageView img, String imageUrl, int width, int heigh) {
        asyncLoadImage(img, imageUrl, width, heigh, null, null, null);
    }

    public void asyncLoadImage(final ImageView img, String imageUrl, int width, int heigh
            , SimpleImageLoadingListener simpleImageLoadingListener
            , ImageLoadingProgressListener imageLoadingProgressListener
            , final ImageView.ScaleType scaleType) {
        if (imageUrl == null) {
            return;
        }

        String imgUrl = imageUrl.trim();
        ImgHolder holder;

        if (img.getTag(R.string.TAG_KEY_IMG_HOLDER) == null) {
            holder = new ImageLoaderManager.ImgHolder(width,heigh);
            if (simpleImageLoadingListener != null) {
                holder.simpleImageLoadingListener = simpleImageLoadingListener;
            } else {
                final AlphaAnimation fadeImage = holder.fadeImage;
                holder.simpleImageLoadingListener = new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (scaleType != null) {
                            img.setScaleType(scaleType);
                        } else {
                            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                        img.setImageBitmap(loadedImage);


                        Object tag = img.getTag(R.string.TAG_KEY_IS_FADEIN); //是否应该显示渐进动画，由各个img自己控制
                        boolean temp = !(tag != null && tag instanceof Boolean && !((Boolean) tag));
                        if (temp) {
                            img.clearAnimation();
                            img.startAnimation(fadeImage);
                        }
                    }
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        Object tag = img.getTag(R.string.TAG_KEY_IS_FADEIN); //是否应该显示渐进动画，由各个img自己控制
                        boolean temp = !(tag != null && tag instanceof Boolean && !((Boolean) tag));
                        if (temp) {
                            img.setScaleType(ImageView.ScaleType.CENTER);
                            img.setImageResource(R.drawable.ic_nopic);
                        }
                    }
                };
            }

            if (imageLoadingProgressListener != null) {
                holder.imageLoadingProgressListener = imageLoadingProgressListener;
            }

            img.setTag(R.string.TAG_KEY_IMG_HOLDER, holder);
        } else {
            holder = (ImgHolder) img.getTag(R.string.TAG_KEY_IMG_HOLDER);
        }
        loadThumbnailFromWeb(imgUrl, holder.imgAware, holder.simpleImageLoadingListener, holder.imageLoadingProgressListener);
    }
}
