
package com.futureskyltd.app.ApiPojo.SignUpComplete;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Email implements Serializable {

    @SerializedName("_empty")
    @Expose
    private String empty;

    public String getEmpty() {
        return empty;
    }

    public void setEmpty(String empty) {
        this.empty = empty;
    }

    @Override
    public String toString() {
        return "Email{" +
                "empty='" + empty + '\'' +
                '}';
    }
}
