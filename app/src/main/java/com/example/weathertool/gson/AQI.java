package com.example.weathertool.gson;

/**
 * Created by user on 2017/3/1.
 */

public class AQI {
    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
