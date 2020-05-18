package com.liuyoungdev.weather.utils;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * author： yang
 * date  ： 2020-05-14
 */
public class HttpUtil {
    public static void sendOkHttpRequest(String url, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);

    }
}
