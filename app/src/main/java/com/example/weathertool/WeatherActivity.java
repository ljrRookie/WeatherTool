package com.example.weathertool;

import android.content.SharedPreferences;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.weathertool.gson.Forecast;
import com.example.weathertool.gson.Weather;
import com.example.weathertool.util.HttpUtil;
import com.example.weathertool.util.Utility;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView mWeatherLayout;
    private LinearLayout mForecastLayout;
    private TextView mTitle, mUpdateTime, mDegre, mWeatherInfo, mAqi, mPm25, mComfort, mCarWash, mSport;
    private static final String TAG = "WeatherActivity";
    private ImageView mBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        initView();
        initData();

    }

    private void initView() {
       mBackground = (ImageView) findViewById(R.id.background);
        mWeatherLayout = (ScrollView) findViewById(R.id.weatherLayout);
        mTitle = (TextView) findViewById(R.id.title_city);
        mUpdateTime = (TextView) findViewById(R.id.title_time);
        mDegre = (TextView) findViewById(R.id.degre);
        mWeatherInfo = (TextView) findViewById(R.id.weather_info);
        mForecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        mAqi = (TextView) findViewById(R.id.aqi);
        mPm25 = (TextView) findViewById(R.id.pm25);
        mComfort = (TextView) findViewById(R.id.comfort);
        mCarWash = (TextView) findViewById(R.id.catWash);
        mSport = (TextView) findViewById(R.id.sport);

    }

    private void initData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherStr = preferences.getString("weather", null);
        if (weatherStr != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherStr);
            showWeatherInfo(weather);
        } else {
            //没有缓存时去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");
            Log.d(TAG, "===============initData: " + weatherId);
            mWeatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
      String bing_pic = preferences.getString("bing_pic", null);
        if (bing_pic != null) {
            Glide.with(this).load(bing_pic).into(mBackground);
        } else {
            loadBingPic();
        }
    }

    private void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=cf8956b5fe324e079b7777a647f70894";
        Log.d(TAG, "==================requestWeather: " + weatherId);

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                Log.d(TAG, "==================onResponse: " + responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            edit.putString("weather", responseText);
                            edit.apply();
                            showWeatherInfo(weather);
                            Toast.makeText(WeatherActivity.this, "成功获取天气信息", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
       loadBingPic();
    }

    /**
     * 更新Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather) {
        Toast.makeText(this, "布局显示", Toast.LENGTH_SHORT).show();
        String cityName = weather.basic.cityName;
        String UpdateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        mTitle.setText(cityName);
        mUpdateTime.setText(UpdateTime);
        mDegre.setText(degree);
        mWeatherInfo.setText(weatherInfo);
         mForecastLayout.removeAllViews();
        //动态添加，刷新布局
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, mForecastLayout, false);
            Log.d(TAG, "============showWeatherInfo: view");
            TextView date = (TextView) view.findViewById(R.id.date);
            TextView info = (TextView) view.findViewById(R.id.info);
            TextView max = (TextView) view.findViewById(R.id.max);
            TextView min = (TextView) view.findViewById(R.id.min);
            date.setText(forecast.date);
            Log.d(TAG, "-------------------showWeatherInfo: "+forecast.date);
            info.setText(forecast.more.info);
            Log.d(TAG, "-------------------showWeatherInfo: "+forecast.more.info);

            max.setText(forecast.temperature.max);
            min.setText(forecast.temperature.min);
            mForecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            mAqi.setText(weather.aqi.city.aqi);
            mPm25.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运行建议：" + weather.suggestion.sport.info;
        mComfort.setText(comfort);
        mCarWash.setText(carWash);
        mSport.setText(sport);
        mWeatherLayout.setVisibility(View.VISIBLE);
    }

   /**
     * 加载每日更新背景图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Log.d(TAG, "=========onResponse: "+responseText);
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                edit.putString("bing_pic", responseText);
                edit.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(responseText).into(mBackground);
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
