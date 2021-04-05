
package com.futureskyltd.app.ApiPojo.SignUpComplete;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PhoneNo {

    @SerializedName("numeric")
    @Expose
    private String numeric;

    public String getNumeric() {
        return numeric;
    }

    public void setNumeric(String numeric) {
        this.numeric = numeric;
    }

    @Override
    public String toString() {
        return "PhoneNo{" +
                "numeric='" + numeric + '\'' +
                '}';
    }
}
