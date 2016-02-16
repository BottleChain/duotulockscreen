package com.juzi.duotulockscreen.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "table_lockscreen_imgs")
public class LockScreenImgBean {
    @DatabaseField(generatedId = true)
    private long _id;

    @DatabaseField
    private String img_url;

    @DatabaseField
    private String img_title;

    @DatabaseField
    private String img_desc;

    @DatabaseField
    private int img_index;

    @DatabaseField
    private int isDeflaut; //用户没有添加锁屏图片是，用默认的图片，此值为1，其他的都是0

    public int getIsDeflaut() {
        return isDeflaut;
    }

    public void setIsDeflaut(int isDeflaut) {
        this.isDeflaut = isDeflaut;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getImg_title() {
        return img_title;
    }

    public void setImg_title(String img_title) {
        this.img_title = img_title;
    }

    public String getImg_desc() {
        return img_desc;
    }

    public void setImg_desc(String img_desc) {
        this.img_desc = img_desc;
    }

    public int getImg_index() {
        return img_index;
    }

    public void setImg_index(int img_index) {
        this.img_index = img_index;
    }
}
