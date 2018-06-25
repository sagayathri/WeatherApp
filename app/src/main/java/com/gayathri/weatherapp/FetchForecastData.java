package com.gayathri.weatherapp;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchForecastData {
    private static final String OPEN_WEATHER_MAP_API = "http://api.openweathermap.org/data/2.5/forecast?q=%s";

    public static JSONObject getJSON(Context context, String city){
        try{
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, city));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("x-api-key", context.getString(R.string.open_weather_map_api_key));
            BufferedReader reader = new BufferedReader( new InputStreamReader(connection.getInputStream()));
            StringBuffer json = new StringBuffer(2000);
            String temp ="";

            while ((temp = reader.readLine())!=null){
                json.append(temp).append("\n");
            }
            reader.close();

            JSONObject data = new JSONObject(json.toString());
            if(data.getInt("cod")!=200){
                return null;
            }
            else {
                Log.d("This", String.valueOf(data));
                return data;
            }
        }catch (Exception e){
            return null;
        }
    }
}
