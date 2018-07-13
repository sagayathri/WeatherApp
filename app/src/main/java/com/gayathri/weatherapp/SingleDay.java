package com.gayathri.weatherapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SingleDay {
    String day_time;
    String day_weather_description;
    String day_temp;
    Integer day_icon_field;

    public SingleDay() {
    }

    public String getDay_time() {
        return day_time;
    }

    public void setDay_time(String day_time) {
        this.day_time = day_time;
    }

    public String getDay_weather_description() {
        return day_weather_description;
    }

    public void setDay_weather_description(String day_weather_description) {
        this.day_weather_description = day_weather_description;
    }

    public String getDay_temp() {
        return day_temp;
    }

    public void setDay_temp(String day_temp) {
        this.day_temp = day_temp;
    }

    public Integer getDay_icon_field() {
        return day_icon_field;
    }

    public void setDay_icon_field(Integer day_icon_field) {
        this.day_icon_field = day_icon_field;
    }
}
