package com.juzi.duotulockscreen.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.adapter.MyLockScreensGalleryAdapter;
import com.juzi.duotulockscreen.bean.LockScreenImgBean;
import com.juzi.duotulockscreen.service.LockScreenService;
import com.juzi.duotulockscreen.util.LogHelper;

import java.util.ArrayList;

public class ManinActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private ImageButton mIvTopBarLeft;
    private TextView mTvTopBarRight;
    private GridView mGridView;
    private ArrayList<LockScreenImgBean> mData  = new ArrayList<LockScreenImgBean>();
    private MyLockScreensGalleryAdapter mAdapter;

    @Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCustomActionBar();
        setContentView(R.layout.activity_main);
        Intent lock = new Intent(this, LockScreenService.class);
        startService(lock);
        assignViews();
    }

    private void assignViews() {
        mAdapter = new MyLockScreensGalleryAdapter(this, mData);
        mGridView = (GridView) findViewById(R.id.gv_gridview);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);

//        mRlMyfavoriteEditBottom = (RelativeLayout) findViewById(R.id.rl_edit_bottom_bar);
//        ImageView ivMyfavoriteBootomDelete = (ImageView) findViewById(R.id.iv_bootom_delete);
//
//        btMyfavoriteAdd.setOnClickListener(this);
//        ivMyfavoriteBootomDelete.setOnClickListener(this);
//
//        mBottomOutAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
//                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1.0f);
//        mBottomInAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
//                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0f);
//        mBottomInAnim.setDuration(120);
//        mBottomOutAnim.setDuration(120);
    }

    //自定义actionbar
    private void initCustomActionBar() {
        ActionBar actionBar = getActionBar();
        LogHelper.d(TAG, "ActivitonBAr = " + actionBar);
        if (actionBar == null) {
            return;
        }
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.home_page_top_bar);

        mIvTopBarLeft = (ImageButton) getActionBar().getCustomView().findViewById(R.id.iv_top_bar_left);
        mIvTopBarLeft.setOnClickListener(this);
        mTvTopBarRight = (TextView) getActionBar().getCustomView().findViewById(R.id.tv_top_bar_right);
        mTvTopBarRight.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
