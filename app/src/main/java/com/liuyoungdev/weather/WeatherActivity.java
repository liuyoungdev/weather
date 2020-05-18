package com.liuyoungdev.weather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.liuyoungdev.weather.gson.Weather;
import com.liuyoungdev.weather.utils.HttpUtil;
import com.liuyoungdev.weather.utils.Utilty;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

/**
 * author： yang
 * date  ： 2020-05-15
 */
public class WeatherActivity extends AppCompatActivity {

    private ImageView beijing_pic;
    private LinearLayout layoutForecast;
    private String mWeatherId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        initView();
        getIntentData();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String pic = sharedPreferences.getString("beijing_pic", null);
        if (pic == null) {
            queryPic();
        } else {
            Glide.with(this).load(pic).into(beijing_pic);
        }
    }


    private void initView() {
        beijing_pic = findViewById(R.id.beijing_pic);
        layoutForecast = findViewById(R.id.layout_forecast);
    }

    private void getIntentData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherId = sharedPreferences.getString("weatherid", null);
        if (weatherId == null) {
            String weatherid = getIntent().getStringExtra("weatherid");
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString("weatherid", weatherid);
            edit.apply();
            queryServer(weatherid);
        } else {
            queryServer(weatherId);
        }
    }

    private void queryServer(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "数据加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String result = response.body().string();
                Log.d("查看天气数据", result);
                final Weather weather = Utilty.handleResponseWeather(result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && weather.status.equals("ok")) {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                            SharedPreferences.Editor edit = sharedPreferences.edit();
                            edit.putString("weather", result);
                            edit.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        }
                    }
                });


            }
        });
    }

    private void showWeatherInfo(Weather weather) {

    }

    private void queryPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(beijing_pic);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
}
