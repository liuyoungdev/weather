package com.liuyoungdev.weather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * author： yang
 * date  ： 2020-05-18
 */
public class Weather {
    public Basic basic;
    public String status;
    public Now now;
    public Api api;
    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> daily_forecast;
}
