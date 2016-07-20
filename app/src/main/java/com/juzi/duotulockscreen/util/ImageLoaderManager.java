package com.juzi.duotulockscreen.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

public class ImageLoaderManager {
    public static final String TAG = "ImageLoaderManager ";
    private static ImageLoaderManager sInstance;
    private ImageLoaderManager(){}
    public static final int IMAGE_FADEIN_DURATION = 200;
    private DisplayImageOptions mDisplay_options_circle; //用来显示圆角图片的options
    private DisplayImageOptions mDisplay_options_fadein; //用来渐隐进入效果显示图片的options

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
//				.writeDebugLogs() //是否打印加载图片过程的log，调试时使用
                .defaultDisplayImageOptions(defaultOptions)
                .threadPriority(Thread.NORM_PRIORITY)// 设置线程的优先级
//				.denyCacheImageMultipleSizesInMemory()// 当同一个Uri获取不同大小的图片，缓存到内存时，只缓存一个。默认会缓存多个不同的大小的相同图片
                .diskCacheSize(200 * 1024 * 1024) //设置硬盘缓存大小，默认是大小无限制的
//				.memoryCacheSizePercentage(25) //设置内存缓存大小位1/4，默认是应用分配内存的 1/8
                .threadPoolSize(4) // 线程池数量
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();

        ImageLoader.getInstance().init(config); // 全局初始化此配置
    }

    /**
     * 加载圆角图片，需要传入特殊的opitions
     * @param img
     * @param imageUrl
     * @param cornerRadiusPixels 圆角半径
     * @param marginPixels 图片bitmap到imageview控件边缘的宽度
     */
    public void asyncLoadCircleImage(final ImageView img, String imageUrl, int cornerRadiusPixels, int marginPixels
            , final int width, final int heigh) {
        if (TextUtils.isEmpty(imageUrl)) {
            return;
        }
        if (mDisplay_options_circle == null) {
            mDisplay_options_circle = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .displayer(new RoundedBitmapDisplayer(cornerRadiusPixels, marginPixels))
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .build();
        }
        ImageLoader.getInstance().displayImage(imageUrl, new ImageViewAware(img){
            @Override
            public int getWidth() {
                return width;
            }

            @Override
            public int getHeight() {
                return heigh;
            }
        }, mDisplay_options_circle);
    }

    /**
     * 加载图片的封装，原装ImageLoader没有可以传宽高的的方法，并且如果要fadein需要新的options
     * 如果使用其不传宽高的的api，前面的图片会获取到宽高为0，然后框架会使用反射获取maxWidth和maxHeight，
     * 多了反射逻辑，而且是使用默认的最大宽高，即屏幕宽高，在加载小图片时会浪费资源，所以自己穿宽高会好一些
     * gridview加载图片是，positon=0的位置会加载许多次，用没有宽高的api更显浪费
     * @param imageUrl
     * @param imageView
     * @param width
     * @param heigh
     * @param fadeIn 是否显示渐入动画，只有网络图片才有动画
     */
    public void displayImage(String imageUrl, ImageView imageView, final int width, final int heigh, boolean fadeIn) {
        if (mDisplay_options_fadein == null) {
            mDisplay_options_fadein = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .displayer(new FadeInBitmapDisplayer(IMAGE_FADEIN_DURATION, true, false, false))
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .build();
        }
        ImageLoader.getInstance().displayImage(imageUrl, new ImageViewAware(imageView) {
            @Override
            public int getWidth() {
                return width;
            }

            @Override
            public int getHeight() {
                return heigh;
            }
        }, fadeIn ? mDisplay_options_fadein : null);
    }
}
