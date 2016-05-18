package com.juzi.duotulockscreen.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

public class FileUtil {

    public static final String TAG = "FileUtil";
    /**
     * 获取一个文件夹的大小，单位字节
     */
    public static long getFolderSize(File file) {
        if (file == null) {
            return 0;
        }
        long size = 0;
        try {
            if (file.isFile()) {
                return file.length();
            }
            File[] fileList = file.listFiles();
            if (fileList != null) {
                for (File f : fileList) {
                    size = size + getFolderSize(f);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getFolderSize exception");
            e.printStackTrace();
        }
        return size;
    }

    public static void deleteFile(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                deleteContents(file);
            } else {
                try {
                    if (!file.delete()) {
                        throw new IOException("failed to delete file: " + file);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "getFolderSize exception");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除指定目录下的所有内容
     */
    public static void deleteContents(File dir) {
        if (dir == null) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        try {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteContents(file);
                }
                if (!file.delete()) {
                    throw new IOException("failed to delete file: " + file);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getFolderSize exception");
            e.printStackTrace();
        }
    }

    /**
     * 格式化文件大小
     */
    public static String getFormatSize(double size) {
        double kb = size / 1024;
        if (kb < 1) {
            return size + "Byte";
        }
        DecimalFormat format = new DecimalFormat(".00");//必须保留两位小数，不够0补零
        double mb = kb / 1024;
        if (mb < 1) {
            return format.format(kb) + "KB";
        }

        double gb = mb / 1024;
        if (gb < 1) {
            return format.format(mb) + "MB";
        }

        double tb = gb / 1024;
        if (tb < 1) {
            return format.format(gb) + "GB";
        }
        return format.format(gb) + "TB";
    }

    public static String getPathUserImageCut(Context context) {
        String imageFolderName;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        sdCardExist = false; //截取的头像不像
        if (sdCardExist) {
            imageFolderName = Environment.getExternalStorageDirectory() + "/haokanScreen/user_cut/";
        } else {// 没有sd卡则存到/data/data/com.haokanhaokan.lockscreen/app_image
            imageFolderName = context.getDir("user_cut", Context.MODE_PRIVATE).getAbsolutePath() + "/";
        }
        try {
            new File(imageFolderName).mkdirs();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return imageFolderName;
    }

    public static String saveImgToInnerData(Context context, Bitmap destBitmap, String picName) {
        String path = context.getDir(Values.PATH_CLIP_DIR, Context.MODE_PRIVATE).getAbsolutePath() + "/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //deleteContents(dir);
        boolean success = savaBitmapToDir(context, dir, destBitmap, picName);
        if (success) {
            File f = new File(dir, picName);
            return f.getAbsolutePath();
        } else {
            ToastManager.showShort(context, "保存失败，请检查空间是否足够");
            return "";
        }
    }

//    /**
//     * 保存下载的图片到本地
//     */
//    public static boolean saveBitmapToExternalSd(Context context, Bitmap destBitmap, String picName) {
//        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            ToastManager.showShort(context, "sd卡不可用");
//            return false;
//        }
//        String path = Environment.getExternalStorageDirectory().toString()
//                + ConstantValues.PATH_DOWNLOAD_PIC;
//
//        File dir = new File(path);
//        return savaBitmapToDir(context, dir, destBitmap, picName);
//    }

    private static boolean savaBitmapToDir(Context context, File dir, Bitmap destBitmap, String picName) {
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File f = new File(dir, picName);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (fos == null) {
            return false;
        }

        destBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

        try {
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //发送扫描文件的广播,使系统读取到刚才存的图片
            if (f.exists()) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(f));
                context.sendBroadcast(intent);
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static String getImgNameByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return ".jpg";
        }
        int start = url.lastIndexOf("/") + 1;
        int end = url.lastIndexOf("@");
        if (end < start) {
            end = url.length();
        }
        return url.substring(start, end);
    }
}
