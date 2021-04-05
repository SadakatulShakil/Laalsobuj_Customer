
package com.futureskyltd.app.ApiPojo.InfoApa;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InfoApaList {

    @SerializedName("apa")
    @Expose
    private List<Apa> apa = null;
    @SerializedName("message")
    @Expose
    private String message;

    public List<Apa> getApa() {
        return apa;
    }

    public void setApa(List<Apa> apa) {
        this.apa = apa;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "InfoApaList{" +
                "apa=" + apa +
                ", message='" + message + '\'' +
                '}';
    }
}
