package com.liuyoungdev.weather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.liuyoungdev.weather.gson.Forecast;
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
    private TextView weather_title;
    private TextView weather_temperature;
    private TextView weather_txt;
    private TextView suggestion01;
    private TextView suggestion02;
    private TextView suggestion03;
    public SwipeRefreshLayout refresh;
    private ScrollView weatherLayout;
    public DrawerLayout drawerLayout;
    private Button buttonHome;

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
        drawerLayout = findViewById(R.id.drawer_layout);
        buttonHome = findViewById(R.id.button_home);
        layoutForecast = findViewById(R.id.layout_forecast);
        weather_title = findViewById(R.id.weather_title);
        weather_temperature = findViewById(R.id.weather_temperature);
        weather_txt = findViewById(R.id.weather_txt);
        suggestion01 = findViewById(R.id.suggestion01);
        suggestion02 = findViewById(R.id.suggestion02);
        suggestion03 = findViewById(R.id.suggestion03);
        weatherLayout = findViewById(R.id.weatherLayout);
        refresh = findViewById(R.id.refresh);
        refresh.setColorSchemeResources(R.color.colorPrimary);
        initListener();
    }

    private void initListener() {
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryServer(mWeatherId);
            }
        });
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });
    }

    private void getIntentData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String result = sharedPreferences.getString("weather", null);
        if (result != null) {
            Weather weather = Utilty.handleResponseWeather(result);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            mWeatherId = getIntent().getStringExtra("weatherid");
            weatherLayout.setVisibility(View.INVISIBLE);
            queryServer(mWeatherId);
        }

    }

    public void queryServer(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "数据加载失败", Toast.LENGTH_SHORT).show();
                        refresh.setRefreshing(false);
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
                        refresh.setRefreshing(false);
                    }
                });


            }
        });
        queryPic();
    }

    private void showWeatherInfo(Weather weather) {
        weather_title.setText(weather.basic.cityName);
        weather_temperature.setText(weather.now.temperature);
        weather_txt.setText(weather.now.more.info);
        layoutForecast.removeAllViews();
        for (Forecast forecast : weather.daily_forecast) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, layoutForecast, false);
            TextView item_date = view.findViewById(R.id.item_date);
            TextView item_weather = view.findViewById(R.id.item_weather);
            TextView item_tmp_max = view.findViewById(R.id.item_tmp_max);
            TextView item_tmp_min = view.findViewById(R.id.item_tmp_min);
            item_date.setText(forecast.date);
            item_weather.setText(forecast.more.info);
            item_tmp_max.setText(forecast.temperature.max);
            item_tmp_min.setText(forecast.temperature.min);
            layoutForecast.addView(view);

        }

        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        suggestion01.setText(comfort);
        suggestion02.setText(carWash);
        suggestion03.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
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
