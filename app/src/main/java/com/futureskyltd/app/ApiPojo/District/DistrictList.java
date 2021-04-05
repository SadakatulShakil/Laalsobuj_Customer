
package com.futureskyltd.app.ApiPojo.District;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class DistrictList implements Serializable {

    @SerializedName("districts")
    @Expose
    private List<District> districts = null;
    @SerializedName("message")
    @Expose
    private String message;

    public List<District> getDistricts() {
        return districts;
    }

    public void setDistricts(List<District> districts) {
        this.districts = districts;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "DistrictList{" +
                "districts=" + districts +
                ", message='" + message + '\'' +
                '}';
    }
}
