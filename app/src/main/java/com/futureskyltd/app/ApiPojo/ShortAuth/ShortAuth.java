
package com.futureskyltd.app.ApiPojo.ShortAuth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ShortAuth {

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("user")
    @Expose
    private User user;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ShortAuth{" +
                "message='" + message + '\'' +
                ", token='" + token + '\'' +
                ", user=" + user +
                '}';
    }
}
