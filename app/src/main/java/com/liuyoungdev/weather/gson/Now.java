package com.liuyoungdev.weather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * author： yang
 * date  ： 2020-05-18
 */
public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {

        @SerializedName("txt")
        public String info;

    }
}
