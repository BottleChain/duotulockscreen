package com.juzi.duotulockscreen.bean;

public class DemoImgBean {
    private String img_url;

    //用来实现乱序流的属性
    private int width; //图片本身的真实宽高
    private int height;

    //通过计算得出的图片距离父容易的左上，和图片用来展示的宽高
    private int marginLeft;
    private int marginTop;
    private int itemWidth;
    private int itemHeigh;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(int marginLeft) {
        this.marginLeft = marginLeft;
    }

    public int getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }

    public int getItemWidth() {
        return itemWidth;
    }

    public void setItemWidth(int itemWidth) {
        this.itemWidth = itemWidth;
    }

    public int getItemHeigh() {
        return itemHeigh;
    }

    public void setItemHeigh(int itemHeigh) {
        this.itemHeigh = itemHeigh;
    }

    public String getImg_url_cuted() {
        return img_url_cuted;
    }

    public void setImg_url_cuted(String img_url_cuted) {
        this.img_url_cuted = img_url_cuted;
    }

    private String img_url_cuted;

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
}
