
package com.futureskyltd.app.ApiPojo.AuthUser;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthUser {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("username_url")
    @Expose
    private String usernameUrl;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("user_level")
    @Expose
    private String userLevel;
    @SerializedName("user_status")
    @Expose
    private String userStatus;
    @SerializedName("profile_image")
    @Expose
    private String profileImage;
    @SerializedName("follow_count")
    @Expose
    private String followCount;
    @SerializedName("login_type")
    @Expose
    private String loginType;
    @SerializedName("facebook_id")
    @Expose
    private String facebookId;
    @SerializedName("activation")
    @Expose
    private String activation;
    @SerializedName("phone_no")
    @Expose
    private String phoneNo;
    @SerializedName("district")
    @Expose
    private String district;
    @SerializedName("upazila")
    @Expose
    private String upazila;
    @SerializedName("zip")
    @Expose
    private String zip;
    @SerializedName("apa")
    @Expose
    private String apa;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsernameUrl() {
        return usernameUrl;
    }

    public void setUsernameUrl(String usernameUrl) {
        this.usernameUrl = usernameUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFollowCount() {
        return followCount;
    }

    public void setFollowCount(String followCount) {
        this.followCount = followCount;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getActivation() {
        return activation;
    }

    public void setActivation(String activation) {
        this.activation = activation;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getUpazila() {
        return upazila;
    }

    public void setUpazila(String upazila) {
        this.upazila = upazila;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getApa() {
        return apa;
    }

    public void setApa(String apa) {
        this.apa = apa;
    }

    @Override
    public String toString() {
        return "AuthUser{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", usernameUrl='" + usernameUrl + '\'' +
                ", firstName='" + firstName + '\'' +
                ", city='" + city + '\'' +
                ", userLevel='" + userLevel + '\'' +
                ", userStatus='" + userStatus + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", followCount='" + followCount + '\'' +
                ", loginType='" + loginType + '\'' +
                ", facebookId='" + facebookId + '\'' +
                ", activation='" + activation + '\'' +
                ", phoneNo='" + phoneNo + '\'' +
                ", district='" + district + '\'' +
                ", upazila='" + upazila + '\'' +
                ", zip='" + zip + '\'' +
                ", apa='" + apa + '\'' +
                '}';
    }
}
