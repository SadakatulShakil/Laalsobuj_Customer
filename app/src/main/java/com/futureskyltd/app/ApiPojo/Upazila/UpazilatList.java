
package com.futureskyltd.app.ApiPojo.Upazila;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpazilatList {

    @SerializedName("upazila")
    @Expose
    private List<Upazila> upazila = null;
    @SerializedName("message")
    @Expose
    private String message;

    public List<Upazila> getUpazila() {
        return upazila;
    }

    public void setUpazila(List<Upazila> upazila) {
        this.upazila = upazila;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "UpazilatList{" +
                "upazila=" + upazila +
                ", message='" + message + '\'' +
                '}';
    }
}
