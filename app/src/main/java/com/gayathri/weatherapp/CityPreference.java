package com.gayathri.weatherapp;

import android.app.Activity;
import android.content.SharedPreferences;

public class CityPreference {

    SharedPreferences preferences;

    public CityPreference(Activity activity){
        preferences = activity.getPreferences(0);
    }
    String getCity(){
        return preferences.getString("city", "London, UK");
    }
    void setCity(String city){
        preferences.edit().putString("city", city).commit();
    }
}
