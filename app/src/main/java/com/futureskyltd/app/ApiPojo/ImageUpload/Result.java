
package com.futureskyltd.app.ApiPojo.ImageUpload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Result implements Serializable {

    @SerializedName("typePost")
    @Expose
    private String typePost;
    @SerializedName("typeGet")
    @Expose
    private String typeGet;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("image")
    @Expose
    private String image;

    public String getTypePost() {
        return typePost;
    }

    public void setTypePost(String typePost) {
        this.typePost = typePost;
    }

    public String getTypeGet() {
        return typeGet;
    }

    public void setTypeGet(String typeGet) {
        this.typeGet = typeGet;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Result{" +
                "typePost='" + typePost + '\'' +
                ", typeGet='" + typeGet + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
