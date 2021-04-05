
package com.futureskyltd.app.ApiPojo.District;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class District implements Serializable {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("district")
    @Expose
    private String district;
    @SerializedName("created")
    @Expose
    private String created;
    @SerializedName("modified")
    @Expose
    private Object modified;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public Object getModified() {
        return modified;
    }

    public void setModified(Object modified) {
        this.modified = modified;
    }

    @Override
    public String toString() {
        return "District{" +
                "id=" + id +
                ", district='" + district + '\'' +
                ", created='" + created + '\'' +
                ", modified=" + modified +
                '}';
    }
}
