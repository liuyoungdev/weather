package com.liuyoungdev.weather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * author： yang
 * date  ： 2020-05-18
 */
public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;

    }
}
