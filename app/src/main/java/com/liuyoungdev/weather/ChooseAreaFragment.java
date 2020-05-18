package com.liuyoungdev.weather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.liuyoungdev.weather.db.City;
import com.liuyoungdev.weather.db.County;
import com.liuyoungdev.weather.db.Province;
import com.liuyoungdev.weather.utils.HttpUtil;
import com.liuyoungdev.weather.utils.Utilty;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * author： yang
 * date  ： 2020-05-13
 */
public class ChooseAreaFragment extends Fragment {
    private Button button_bcak;
    private TextView titile_text;
    private ListView listView;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private ArrayAdapter<String> arrayAdapter;
    private final int LEVEL_PROVINCE = 0;
    private final int LEVEL_CITY = 1;
    private final int LEVEL_COUNTY = 2;
    private int currentLevel;
    private Province province;
    private ProgressDialog progressDialog;
    private List<City> cityList;
    private City city;
    private List<County> countyList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_area, container, false);
        button_bcak = view.findViewById(R.id.button_back);
        titile_text = view.findViewById(R.id.titile_text);
        listView = view.findViewById(R.id.listview);
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(arrayAdapter);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    province = provinceList.get(position);
                    queryCities(province);
                } else if (currentLevel == LEVEL_CITY) {
                    city = cityList.get(position);
                    queryCounties(city);
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weatherid", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }

                }
            }
        });
        button_bcak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                } else if (currentLevel == LEVEL_COUNTY) {
                    queryCities(province);
                }
            }
        });
        queryProvinces();
    }

    private void queryCounties(City city) {
        button_bcak.setVisibility(View.VISIBLE);
        titile_text.setText(city.getCityName());
        countyList = LitePal.where("cityId = ?", String.valueOf(city.getCityCode())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int proviceCode = province.getProviceCode();
            int cityCode = city.getCityCode();
            String url = "http://guolin.tech/api/china/" + proviceCode + "/" + cityCode;
            queryServerData(url, "county");
        }
    }

    private void queryCities(Province province) {
        button_bcak.setVisibility(View.VISIBLE);
        titile_text.setText(province.getProviceName());
        cityList = LitePal.where("proviceId = ?", String.valueOf(province.getProviceCode())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int proviceCode = province.getProviceCode();
            String url = "http://guolin.tech/api/china/" + proviceCode;
            queryServerData(url, "city");
        }
    }

    private void queryProvinces() {
        button_bcak.setVisibility(View.GONE);
        titile_text.setText("中国");
        //先查看数据库中有没有数据
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProviceName());

            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String url = "http://guolin.tech/api/china";
            queryServerData(url, "province");
        }

    }

    private void queryServerData(String url, final String type) {
        showLoadingDialog();
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissLoadingDialog();
                        Toast.makeText(getActivity(), "数据加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String resultString = response.body().string();
                Log.d("返回身份数据", resultString);
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utilty.handleResponseProvince(resultString);
                    Log.d("data1", result + "");
                } else if ("city".equals(type)) {
                    result = Utilty.handleResponseCity(resultString, province.getProviceCode());
                    Log.d("data2", result + "");
                } else if ("county".equals(type)) {
                    result = Utilty.handleResponseCounty(resultString, city.getCityCode());
                    Log.d("data3", result + "");
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities(province);
                            } else if ("county".equals(type)) {
                                queryCounties(city);
                            }

                        }
                    });
                }

            }
        });
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void showLoadingDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载中");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
}
