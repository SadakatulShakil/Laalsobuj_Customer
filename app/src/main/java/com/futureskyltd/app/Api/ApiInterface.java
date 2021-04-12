package com.futureskyltd.app.Api;

import com.futureskyltd.app.ApiPojo.AuthUser.AuthUser;
import com.futureskyltd.app.ApiPojo.CustomerProfile.CustomerProfile;
import com.futureskyltd.app.ApiPojo.District.DistrictList;
import com.futureskyltd.app.ApiPojo.GeneralLogIn.GeneralLogIn;
import com.futureskyltd.app.ApiPojo.ImageUpload.ImageUpload;
import com.futureskyltd.app.ApiPojo.InfoApa.InfoApaList;
import com.futureskyltd.app.ApiPojo.ShortAuth.ShortAuth;
import com.futureskyltd.app.ApiPojo.SignUpComplete.SignUpComplete;
import com.futureskyltd.app.ApiPojo.Upazila.UpazilatList;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiInterface {

    ///////PHP || CakePhP use @Field  and @FromUrlEncoded for POST method//////
    ////// @Query is used for Get Method/////////
    @Headers("accept: application/json")
    @FormUrlEncoded
    @POST("saveFBdata")
    Call<ShortAuth> postBySaveFbData(
            @Field("id") String id,
            @Field("email") String email,
            @Field("accessToken") String accessToken
    );


    @Headers("accept: application/json, content-type: multipart/form-data")
    @GET("district-list")
    Call<DistrictList> getByDistrict();


    @Headers("accept: application/json, content-type: multipart/form-data")
    @FormUrlEncoded
    @POST("upazilas-selected-by-district")
    Call<UpazilatList> postByUpazila(
            @Field("district_id") int district_id
    );

    @Headers("accept: application/json, content-type: multipart/form-data")
    @FormUrlEncoded
    @POST("get-info-apa-selected-by-upazila")
    Call<InfoApaList> postByInfoApa(
            @Field("district") int district,
            @Field("upazila") int upazila
    );

    @Headers("accept: application/json, content-type: multipart/form-data")
    @FormUrlEncoded
    @POST("signupcomplete")
    Call<SignUpComplete> postByCompleteSignUp(
            @Field("first_name") String first_name,
            @Field("username") String username,
            @Field("email") String email,
            @Field("phone_no") String phone_no,
            @Field("password") String password,
            @Field("con_password") String con_password,
            @Field("storename") String storename,
            @Field("district") int district,
            @Field("upazila") int upazila,
            @Field("apa") int apa,
            @Field("address") String address,
            @Field("zip") String zip,
            @Field("facebook_id") String facebook_id
    );

    @Headers("accept: application/json, content-type: multipart/form-data")
    @GET("user")
    Call<AuthUser> getByAuthUser(
            @Header("Authorization") String token
    );

    @Headers("accept: application/json, content-type: application/json")
    @FormUrlEncoded
    @POST("login")
    Call<GeneralLogIn> postByGeneralLogIn(
            @Field("email") String email,
            @Field("password") String password
    );

    @Headers("accept: application/json, content-type: application/json")
    @FormUrlEncoded
    @POST("profile")
    Call<CustomerProfile> postByCustomerProfile(
            @Field("other_user_id") String other_user_id,
            @Field("logged_user_id") String logged_user_id
    );

    @Headers("accept: application/json, content-type: multipart/form-data")
    @Multipart
    @POST("imageuploadapi.php")
    Call<ImageUpload> postByUploadImage(
            @Part("images\"; filename=\"myProfile.jpg\" ") RequestBody images
    );
}
