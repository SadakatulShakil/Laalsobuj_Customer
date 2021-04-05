
package com.futureskyltd.app.ApiPojo.SignUpComplete;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Error implements Serializable {

    @SerializedName("email")
    @Expose
    private Email email;
    @SerializedName("phone_no")
    @Expose
    private PhoneNo phoneNo;

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public PhoneNo getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(PhoneNo phoneNo) {
        this.phoneNo = phoneNo;
    }

    @Override
    public String toString() {
        return "Error{" +
                "email=" + email +
                ", phoneNo=" + phoneNo +
                '}';
    }
}
