package com.juzi.duotulockscreen.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.util.CommonUtil;
import com.juzi.duotulockscreen.util.MyActivityTransanimHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

public class LockScreenBigImgActivity extends BaseActivity implements View.OnClickListener {
    public static final String IMAGE_PATH = "image_path";
    public static final String IMAGE_POSITION = "image_position";
    private ImageView mIvItemContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockscreen_bigimg);
        assignViews();
        MyActivityTransanimHelper.getInstance().setTransitionImageView(this, mIvItemContent, "image");
        loadData();
    }

    private void loadData() {
        String imgPath = getIntent().getStringExtra(IMAGE_PATH);
        if (!TextUtils.isEmpty(imgPath)) {
            ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(imgPath), mIvItemContent);
        }
    }

    private void assignViews() {
        mIvItemContent = (ImageView) findViewById(R.id.iv_item_content);
        mIvItemContent.setOnClickListener(this);
        ImageView ivBootomDelete = (ImageView) findViewById(R.id.iv_bootom_delete);
        ivBootomDelete.setOnClickListener(this);
    }

    @Override
    protected boolean getIsFullScreen() {
        return true;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        MyActivityTransanimHelper.getInstance().exit(this, mIvItemContent,"image", 0);
    }

    @Override
    public void onClick(View v) {
        if (CommonUtil.isQuickClick()) {
            return;
        }
        int id = v.getId();
        switch (id) {
            case R.id.iv_item_content:
                setResult(RESULT_CANCELED);
                onBackPressed();
                break;
            case R.id.iv_bootom_delete:
                MainActivity.sDeleteImgPos = getIntent(). getIntExtra(LockScreenBigImgActivity.IMAGE_POSITION, -1);
                setResult(RESULT_OK, getIntent());
                onBackPressed();
                break;
            default:
                break;
        }
    }
}
