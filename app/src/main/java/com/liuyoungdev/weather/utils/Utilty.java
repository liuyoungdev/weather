package com.liuyoungdev.weather.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.liuyoungdev.weather.db.City;
import com.liuyoungdev.weather.db.County;
import com.liuyoungdev.weather.db.Province;
import com.liuyoungdev.weather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * author： yang
 * date  ： 2020-05-14
 */
public class Utilty {
    public static boolean handleResponseProvince(String resultString) {
        if (!TextUtils.isEmpty(resultString)) {

            try {
                JSONArray allProvince = new JSONArray(resultString);
                for (int i = 0; i < allProvince.length(); i++) {
                    JSONObject provinceObject = allProvince.getJSONObject(i);
                    Province province = new Province();
                    province.setProviceCode(provinceObject.getInt("id"));
                    province.setProviceName(provinceObject.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;

    }

    public static boolean handleResponseCity(String resultString, int proviceCode) {
        if (!TextUtils.isEmpty(resultString)) {

            try {
                JSONArray allCities = new JSONArray(resultString);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setProviceId(proviceCode);
                    city.setCityCode(cityObject.getInt("id"));
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleResponseCounty(String resultString, int cityCode) {
        if (!TextUtils.isEmpty(resultString)) {

            try {
                JSONArray allCounties = new JSONArray(resultString);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setCityId(cityCode);
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleResponseWeather(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray heWeather = jsonObject.getJSONArray("HeWeather");
            String weatherContent = heWeather.get(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
