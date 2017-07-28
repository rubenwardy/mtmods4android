package com.rubenwardy.minetestmodmanager.restapi;

import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StoreAPIBuilder {
    public static final String API_BASE_URL = "https://minetest-mods.rubenwardy.com/";

    private static Retrofit.Builder createBaseBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        return new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()));
    }

    public static StoreAPI createService() {
        return createBaseBuilder().build().create(StoreAPI.class);
    }
}
