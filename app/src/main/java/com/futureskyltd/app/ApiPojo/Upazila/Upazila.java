
package com.futureskyltd.app.ApiPojo.Upazila;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Upazila {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("upazila")
    @Expose
    private String upazila;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUpazila() {
        return upazila;
    }

    public void setUpazila(String upazila) {
        this.upazila = upazila;
    }

    @Override
    public String toString() {
        return "Upazila{" +
                "id=" + id +
                ", upazila='" + upazila + '\'' +
                '}';
    }
}
