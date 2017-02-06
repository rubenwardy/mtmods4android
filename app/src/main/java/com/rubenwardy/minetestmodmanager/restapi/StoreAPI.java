package com.rubenwardy.minetestmodmanager.restapi;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface StoreAPI {
    @GET("v2/list")
    Call<List<RestMod>> getModList();

    @FormUrlEncoded
    @POST("v1/report")
    Call<ResponseBody> sendReport(@Field("modname") String modname,
                                  @Field("msg")     String msg,
                                  @Field("reason")  String reason,
                                  @Field("author")  String author,
                                  @Field("list")    String list,
                                  @Field("link")    String link);

    @FormUrlEncoded
    @POST("v1/on-download")
    Call<ResponseBody> sendDownloadReport(@Field("modname") String modname,
                                          @Field("link")    String link,
                                          @Field("size")    int size,
                                          @Field("status")  int status,
                                          @Field("author")  String author,
                                          @Field("error")   String error);

    class RestMod {
        public String author;
        public String type;
        public String basename;
        public String title;
        public String description;
        public String forum_url;
        public String download_link;
        public int    download_size;
        public String repo_host;
        public String repo;
        public String commit_hash;
        public String repo_author;
        public String repo_name;
        public int    score;
    }
}
