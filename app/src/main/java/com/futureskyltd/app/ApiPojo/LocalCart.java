package com.futureskyltd.app.ApiPojo;

public class LocalCart {
    private String imageUrl;
    private String itemName;
    private String itemPrice;
    private String itemSize;
    private String itemShipping;

    public LocalCart() {
    }

    public LocalCart(String imageUrl, String itemName, String itemPrice, String itemSize, String itemShipping) {
        this.imageUrl = imageUrl;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemSize = itemSize;
        this.itemShipping = itemShipping;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemSize() {
        return itemSize;
    }

    public void setItemSize(String itemSize) {
        this.itemSize = itemSize;
    }

    public String getItemShipping() {
        return itemShipping;
    }

    public void setItemShipping(String itemShipping) {
        this.itemShipping = itemShipping;
    }

    @Override
    public String toString() {
        return "LocalCart{" +
                "imageUrl='" + imageUrl + '\'' +
                ", itemName='" + itemName + '\'' +
                ", itemPrice='" + itemPrice + '\'' +
                ", itemSize='" + itemSize + '\'' +
                ", itemShipping='" + itemShipping + '\'' +
                '}';
    }
}
