package fr.emmanuel.loisance.androidstarter.service;

import fr.emmanuel.loisance.androidstarter.classe.User;
import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface APIService {

    @GET("users/{email}/{password}")
    Call<User> getUserFromDefault(@Path("email") String email, @Path("password") String password);

    @GET("users/{idGoogle}")
    Call<User> getUserFromGoogle(@Path("idGoogle") String idGoogle);

    @FormUrlEncoded
    @POST("users/create/google")
    Call<User> createUserWithGoogle(@Field("idGoogle") String idGoogle, @Field("firstname") String firstname, @Field("lastname") String lastname, @Field("email") String email);

    @FormUrlEncoded
    @POST("users/create/default")
    Call<User> createUserWithDefault(@Field("firstname") String firstname, @Field("lastname") String lastname, @Field("email") String email, @Field("password") String password);

}