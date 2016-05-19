package com.juzi.duotulockscreen.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.util.CommonUtil;
import com.juzi.duotulockscreen.util.ImageLoaderManager;

public class LockScreenBigImgActivity extends BaseActivity implements View.OnClickListener {
    public static final String IMAGE_PATH = "image_path";
    public static final String IMAGE_POSITION = "image_position";
    private ImageView mIvItemContent;
    public static Drawable sPreviewDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockscreen_bigimg);
        assignViews();

        loadData();
    }

    private void loadData() {
        String imgPath = getIntent().getStringExtra(IMAGE_PATH);
        if (!TextUtils.isEmpty(imgPath)) {
            ImageLoaderManager.getInstance().loadLocalPic(imgPath, mIvItemContent);
        }
    }

    private void assignViews() {
        mIvItemContent = (ImageView) findViewById(R.id.iv_item_content);
        if (sPreviewDrawable != null) {
            mIvItemContent.setImageDrawable(sPreviewDrawable);
            sPreviewDrawable = null;
        }
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
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_retain, R.anim.activity_fade_out);
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
                setResult(RESULT_OK, getIntent());
                onBackPressed();
                break;
            default:
                break;
        }
    }
}
