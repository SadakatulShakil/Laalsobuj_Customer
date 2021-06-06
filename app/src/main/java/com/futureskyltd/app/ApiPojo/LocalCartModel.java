package com.futureskyltd.app.ApiPojo;

import java.io.Serializable;

public class LocalCartModel implements Serializable {
    private String item_id;
    private String item_title;
    private String item_price;
    private String size;
    private String itemImage;
    private String shipping_time;
    private String qty;

    public LocalCartModel() {
    }

    public LocalCartModel(String item_id, String item_title, String item_price, String size, String itemImage, String shipping_time, String qty) {
        this.item_id = item_id;
        this.item_title = item_title;
        this.item_price = item_price;
        this.size = size;
        this.itemImage = itemImage;
        this.shipping_time = shipping_time;
        this.qty = qty;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getItem_title() {
        return item_title;
    }

    public void setItem_title(String item_title) {
        this.item_title = item_title;
    }

    public String getItem_price() {
        return item_price;
    }

    public void setItem_price(String item_price) {
        this.item_price = item_price;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public String getShipping_time() {
        return shipping_time;
    }

    public void setShipping_time(String shipping_time) {
        this.shipping_time = shipping_time;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    @Override
    public String toString() {
        return "LocalCartModel{" +
                "item_id='" + item_id + '\'' +
                ", item_title='" + item_title + '\'' +
                ", item_price='" + item_price + '\'' +
                ", size='" + size + '\'' +
                ", itemImage='" + itemImage + '\'' +
                ", shipping_time='" + shipping_time + '\'' +
                ", qty='" + qty + '\'' +
                '}';
    }
}
