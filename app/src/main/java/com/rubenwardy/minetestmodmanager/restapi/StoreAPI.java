package com.rubenwardy.minetestmodmanager.restapi;

import android.support.annotation.Nullable;
import android.util.Log;

import com.rubenwardy.minetestmodmanager.models.Mod;
import com.rubenwardy.minetestmodmanager.models.ModList;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
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

    class MissingModReport {
        public List<String> mods;
        public String required_by;

        public MissingModReport(List<String> mods, String required_by) {
            this.mods = mods;
            this.required_by = required_by;
        }
    }

    @POST("v2/on-missing-dep")
    Call<ResponseBody> sendMissingDependsReport(@Body MissingModReport info);

    class RestMod {
        public String author;
        public String type;
        public String basename;
        public String title;
        public String description;
        public String forum_url;
        public String download_link;

        @Nullable
        Mod toMod(final String modstore_url) {
            String modname = this.basename;
            String title = this.title;
            String link = this.download_link;

            if (modname == null || title == null || null == link) {
                return null;
            }

            String author = this.author;
            String type_s = this.type;

            String desc = "";
            if (this.description != null) {
                desc = this.description;
            }

            String forum = null;
            if (this.forum_url != null) {
                forum = this.forum_url;
            }

            Mod.ModType type = Mod.ModType.EMT_MOD;
            if (type_s != null) {
                if (type_s.equals("1")) {
                    type = Mod.ModType.EMT_MOD;
                } else if (type_s.equals("2")) {
                    type = Mod.ModType.EMT_MODPACK;
                }
            }

            Mod mod = new Mod(type, modstore_url, modname, title, desc);
            mod.link = link;
            mod.author = author;
            mod.forum_url = forum;
            mod.size = 0;
            return mod;
        }

        public static void addAllToList(ModList list, List<RestMod> mods, String modstore_url) {
            for (StoreAPI.RestMod rmod : mods) {
                Mod mod = rmod.toMod(modstore_url);
                if (mod == null) {
                    Log.e("RestMod", "Invalid object in list");
                } else {
                    list.add(mod);
                }
            }
        }
    }
}
