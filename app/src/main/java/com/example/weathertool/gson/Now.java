package com.example.weathertool.gson;

import com.google.gson.annotations.SerializedName;
/**
 * Created by user on 2017/3/1.
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
