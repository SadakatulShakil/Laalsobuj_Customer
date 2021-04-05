package com.futureskyltd.app.Api;

import com.futureskyltd.app.ApiPojo.AuthUser.AuthUser;
import com.futureskyltd.app.ApiPojo.District.DistrictList;
import com.futureskyltd.app.ApiPojo.InfoApa.InfoApaList;
import com.futureskyltd.app.ApiPojo.ShortAuth.ShortAuth;
import com.futureskyltd.app.ApiPojo.SignUpComplete.SignUpComplete;
import com.futureskyltd.app.ApiPojo.Upazila.UpazilatList;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

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
   /* @Headers("accept: application/json")
    @FormUrlEncoded
    @POST("api/saveFBdata")
    Call<ShortAuth> postBySaveFbData(
            @Field("id") String id,
            @Field("email") String email,
            @Field("accessToken") String accessToken
    );*/

    @Headers("accept: application/json, content-type: multipart/form-data")
    @GET("district-list")
    Call<DistrictList> getByDistrict();
   /*@Headers("accept: application/json, content-type: multipart/form-data")
   @GET("api/district-list")
   Call<DistrictList> getByDistrict();*/

    @Headers("accept: application/json, content-type: multipart/form-data")
    @FormUrlEncoded
    @POST("upazilas-selected-by-district")
    Call<UpazilatList> postByUpazila(
            @Field("district_id") int district_id
    );
    /*@Headers("accept: application/json, content-type: multipart/form-data")
    @FormUrlEncoded
    @POST("api/upazilas-selected-by-district")
    Call<UpazilatList> postByUpazila(
            @Field("district_id") int district_id
    );*/

    @Headers("accept: application/json, content-type: multipart/form-data")
    @FormUrlEncoded
    @POST("get-info-apa-selected-by-upazila")
    Call<InfoApaList> postByInfoApa(
            @Field("district") int district,
            @Field("upazila") int upazila
    );
    /*@Headers("accept: application/json, content-type: multipart/form-data")
    @FormUrlEncoded
    @POST("api/get-info-apa-selected-by-upazila")
    Call<InfoApaList> postByInfoApa(
            @Field("district") int district,
            @Field("upazila") int upazila
    );*/

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
    /*@Headers("accept: application/json, content-type: multipart/form-data")
    @FormUrlEncoded
    @POST("api/signupcomplete")
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
    );*/
    @Headers("accept: application/json, content-type: multipart/form-data")
    @GET("user")
    Call<AuthUser> getByAuthUser(
            @Header("Authorization") String token
    );
}
