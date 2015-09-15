package com.vizo.news.api;

import com.vizo.news.api.domain.ArticleSourcesResponse;
import com.vizo.news.api.domain.CategoriesWithGlancesResponse;
import com.vizo.news.api.domain.GeneralResponse;
import com.vizo.news.api.domain.GetGlancedItemsResponse;
import com.vizo.news.api.domain.LikeUserGlanceResponse;
import com.vizo.news.api.domain.PostLoginResponse;
import com.vizo.news.api.domain.VizoUGlancesResponse;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public interface APIService {

    /**
     * Get all categories with glances belonging to them
     */
    @GET("/categories?with=glances")
    void getCategoriesWithGlances(Callback<CategoriesWithGlancesResponse> callback);

    /**
     * Get the list of article sources for the glance
     *
     * @param glanceId The ID of the glance
     * @param callback The callback should handle the response
     */
    @GET("/glances/sources")
    void getArticleSources(@Query("id") String glanceId, Callback<ArticleSourcesResponse> callback);

    /**
     * Post user login request
     *
     * @param emailAddress user email address
     * @param password     user password
     * @param callback     when response is returned, callback is called
     */
    @FormUrlEncoded
    @POST("/mobileusers/login")
    void postLoginRequest(@Field("email") String emailAddress, @Field("passwd") String password,
                          Callback<PostLoginResponse> callback);

    /**
     * Post create user request
     *
     * @param email       user email
     * @param facebook_id facebook ID
     * @param twitter_id  twitter ID
     * @param password    user password (optional if social media)
     * @param firstName   user first name
     * @param lastName    user last name
     * @param avatarImage base64 encoded string or url of user image
     * @param callback    the callback should be called when the response is returned
     */
    @FormUrlEncoded
    @POST("/mobileusers/create")
    void postCreateUserRequest(@Field("email") String email,
                               @Field("facebook_id") String facebook_id,
                               @Field("twitter_id") String twitter_id,
                               @Field("passwd") String password,
                               @Field("first_name") String firstName,
                               @Field("last_name") String lastName,
                               @Field("avatar_image") String avatarImage,
                               Callback<PostLoginResponse> callback);

    /**
     * Register Google Cloud Messaging token to Vizo
     *
     * @param deviceToken    GCM registration token
     * @param platform       Platform, we will use the constant "Android"
     * @param accessToken    Access token
     * @param deviceTimeZone Device's timezone
     * @return De-serialized response object
     */
    @GET("/mobileusers/register_device_token")
    GeneralResponse registerDeviceToken(@Query("device_token") String deviceToken,
                                        @Query("platform") String platform,
                                        @Query("access_token") String accessToken,
                                        @Query("device_timezone") String deviceTimeZone);

    /**
     * Post request to save preference settings
     *
     * @param key   Key
     * @param value Value
     * @return Response from the server
     */
    @FormUrlEncoded
    @POST("/mobileusers/setpreference")
    GeneralResponse postUserProfile(@Field("key") String key, @Field("value") String value);

    /**
     * Post glanced item to the server
     *
     * @param glanceId The id of glanced item
     * @param favorite Flag value, 1 - favorite, 0 - not favorite
     * @return Response from the server
     */
    @GET("/glances/setglanced")
    GeneralResponse postGlancedItem(@Query("glance_id") String glanceId, @Query("stared") Integer favorite);

    /**
     * Get all glanced items of the user
     *
     * @param callback Callback which handles response from server
     */
    @GET("/glances/getglanced")
    void getGlancedItems(Callback<GetGlancedItemsResponse> callback);

    /**
     * Get all glanced items of the user (synchronous request)
     */
    @GET("/glances/getglanced")
    GetGlancedItemsResponse getGlancedItems();

    /**
     * Remove glanced item from the server
     *
     * @return Response from the server
     */
    @GET("/glances/removeglanced")
    GeneralResponse removeGlancedItems(@Query("glance_id") String glanceId);

    /**
     * Get all user glances
     *
     * @param limit    Limit count of glances
     * @param offset   Offset
     * @param callback The callback which is called after complete request
     */
    @GET("/glances")
    void getVizoUGlances(@Query("limit") Integer limit,
                         @Query("offset") Integer offset,
                         @Query("global") Integer global,
                         Callback<VizoUGlancesResponse> callback);

    /**
     * Publish user glance
     *
     * @param glanceText   Glance description in String type
     * @param imageFile    Base64 code for glance image object
     * @param categoryId   Category ID in String type
     * @param posterEmail  Poster's email address in String type
     * @param posterName   Poster's name
     * @param posterAvatar Poster's facebook profile picture
     * @param callback     The callback which is called after request is done
     */
    @FormUrlEncoded
    @POST("/glances/create")
    void publishUserGlance(@Field("glance_text") String glanceText,
                           @Field("image_file") String imageFile,
                           @Field("category_id") String categoryId,
                           @Field("poster_email") String posterEmail,
                           @Field("poster_name") String posterName,
                           @Field("poster_avatar") String posterAvatar,
                           @Field("global_post") Integer isGlobal,
                           Callback<Response> callback);

    /**
     * Delete published user glance
     *
     * @param glanceId Glance ID to be deleted
     * @param callback The callback which is called after reuqest is done
     */
    @GET("/glances/delete")
    void deleteUserGlance(@Query("id") String glanceId,
                          Callback<Response> callback);

    /**
     * Like or dislike the user glance
     *
     * @param glanceId The glance ID in string type
     * @param type     1 if like, -1 if unlike
     * @param callback The callback which is called after the request is done
     */
    @GET("/glances/updateliked")
    void likeUserGlance(@Query("id") String glanceId,
                        @Query("type") Integer type,
                        Callback<LikeUserGlanceResponse> callback);

}