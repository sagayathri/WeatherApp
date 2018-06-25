package com.gayathri.weatherapp;


import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WeatherFragment extends Fragment {

    TextView cityfeild, update, details, currenttemp;
    ImageView weathericon;
    String finalDate, min_max, weather_description;
    ListView listView;
    Handler handler = new Handler();
    Integer icon_index;
    ArrayList<SingleRow> arrayList;

    public WeatherFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arrayList = new ArrayList<SingleRow>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        this.cityfeild = (TextView) view.findViewById(R.id.city_field);
        this.update = (TextView) view.findViewById(R.id.update_field);
        details = (TextView) view.findViewById(R.id.details_field);
        currenttemp = (TextView) view.findViewById(R.id.current_temp);
        weathericon = (ImageView) view.findViewById(R.id.weather_icon);
        listView = (ListView) view.findViewById(R.id.listView);

        return view;
    }

    public void updateWeatherData(final String city){
        new Thread(){
            @Override
            public void run(){
                final JSONObject json = RemoteFetch.getJSON(WeatherFragment.this.getActivity(), city);
                if(json==null){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.place_not_found), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    handler.post((new Runnable() {
                        @Override
                        public void run() {
                            renderWeather(json);
                        }
                    }));
                }
            }
        }.start();
    }

    public void updateForecast(final String city){
        new Thread(){
            @Override
            public void run(){
                final JSONObject json = FetchForecastData.getJSON(WeatherFragment.this.getActivity(), city);
                if(json==null){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.place_not_found), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    handler.post((new Runnable() {
                        @Override
                        public void run() {
                            renderForecast(json);
                        }
                    }));
                }
            }
        }.start();
    }

    private void renderForecast(JSONObject json) {
        try {
            JSONArray jsondetails = json.getJSONArray("list");
            for(int i=0; i<jsondetails.length();i++) {
                JSONObject list = jsondetails.getJSONObject(i);
                String dateString = list.getString("dt_txt");
                String[] splitdate = dateString.split(" ");
                String time = splitdate[1];
                String dateinfo = splitdate[0];
                JSONObject weatherJSON = list.getJSONArray("weather").getJSONObject(0);
                weather_description = weatherJSON.getString("main");
                Integer weather_id = weatherJSON.getInt("id");
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date = (Date) formatter.parse(dateinfo);
                SimpleDateFormat newFormat = new SimpleDateFormat("EE, dd/MM");
                finalDate = newFormat.format(date);
                JSONObject mainJSON = list.getJSONObject("main");
                String tempmin = mainJSON.getString("temp_min");
                String tempmax = mainJSON.getString("temp_max");
                Double double_min = Double.valueOf(tempmin)- 273.15;
                Double double_max = Double.valueOf(tempmax) - 273.15;
                String temp_min =String.valueOf(double_min.intValue());
                String temp_max =String.valueOf(double_max.intValue());
                min_max = (temp_min) + "°C / " + temp_max + "°C";
                switch (time) {
                    case "09:00:00":{
                        icon_index =getDayIcon(weather_id);
                        SingleRow singleRow = new SingleRow();
                        singleRow.setDate_feild(finalDate);
                        singleRow.setDescription(weather_description);
                        singleRow.setIcon_field(icon_index);
                        singleRow.setMinmax_field(min_max);
                        arrayList.add(singleRow);
                        listView.setAdapter(new myAdapter(getContext(), arrayList));
                        break;
                    }
                    default:
                        continue;
                }
                if(arrayList.size()>=5){
                    arrayList.clear();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void renderWeather(JSONObject json){
        try{
            this.cityfeild.setText(json.getString("name").toUpperCase(Locale.UK)
                    +", "+json.getJSONObject("sys").getString("country"));
            JSONObject jsondetails = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            JSONObject wind = json.getJSONObject("wind");

            this.details.setText(jsondetails.getString("description").toUpperCase(Locale.UK)+"\n"
                    + "Wind: "+wind.getString("speed")+"mph, "+ getWind(wind.getString("deg"))+"\n"
                    + "Humidity: "+main.getString("humidity")+"%"+"\n"
                    + "Min: "+main.getString("temp_min")+" °C"+"\t\t\t\t"
                    + "Max: "+main.getString("temp_max")+" °C");
            this.currenttemp.setText(String.format("%.2f", main.getDouble("temp"))+" °C");
            SimpleDateFormat dateFormat = new SimpleDateFormat("E dd MMMM, YYYY hh:mm a");
            String updateOn = dateFormat.format(new Date(json.getLong("dt")*1000L));
            this.update.setText("Last Update: "+updateOn);
            this.setWeatherIcon(jsondetails.getInt("id"), json.getJSONObject("sys").getLong("sunrise")*1000L,
                    json.getJSONObject("sys").getLong("sunset")*1000L);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getWind(String deg) {
        String direction = null;
        Integer degree = Integer.parseInt(deg);
        if(degree == 0 || degree == 360){
            direction = "North";
        }else if(degree > 0 || degree < 45){
            direction = "North-NorthEast";
        }else if(degree == 45){
            direction = "NorthEast";
        }else if(degree > 45 || degree < 90){
            direction = "East-NorthEast";
        }else if(degree == 90){
            direction = "East";
        }else if(degree > 90 || degree < 135){
            direction = "East-SouthEast";
        }else if(degree == 135){
            direction = "SouthEast";
        }else if(degree > 135 || degree < 180){
            direction = "South-SouthEast";
        }else if(degree == 180){
            direction = "South";
        }else if(degree > 180 || degree < 225){
            direction = "South-SouthWest";
        }else if(degree == 225){
            direction = "SouthWest";
        }else if(degree > 225 || degree < 270){
            direction = "West-SouthWest";
        }else if(degree == 270){
            direction = "West";
        }else if(degree > 270 || degree < 315){
            direction = "West-NorthWest";
        }else if(degree == 315){
            direction = "West";
        }else if(degree > 315 || degree < 360){
            direction = "North-NorthWest";
        }
        return direction;
    }

    private void setWeatherIcon(int actualid, long sunrise, long sunset) {
        long currentTime = (new Date()).getTime();
        if(currentTime>=sunrise && currentTime<=sunset){
            getDayIcon(actualid);
        } else if(currentTime<sunrise || currentTime>sunset){
            getNightIcon(actualid);
        }
    }

    private void getNightIcon(int actualid) {
        switch (actualid) {
            case 800: {
                icon_index = R.drawable.moon;
                weathericon.setImageResource(R.drawable.moon);
                break;
            }
            case 200:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_storm));
                break;
            case 201:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_storm));
                break;
            case 202:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_storm));
                break;
            case 210:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_storm));
                break;
            case 211:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_storm));
                break;
            case 212:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_storm));
                break;
            case 221:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_storm));
                break;
            case 230:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_storm));
                break;
            case 231:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_storm));
                break;
            case 232:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_storm));
                break;
            case 300:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                break;
            case 301:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                break;
            case 303:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                break;
            case 310:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                break;
            case 311:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                break;
            case 312:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                break;
            case 313:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                break;
            case 314:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                break;
            case 321:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                break;
            case 701:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_fog));
                break;
            case 711:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_fog));
                break;
            case 721:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_fog));
                break;
            case 731:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_fog));
                break;
            case 741:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_fog));
                break;
            case 751:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_fog));
                break;
            case 761:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_fog));
                break;
            case 762:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_fog));
                break;
            case 771:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_fog));
                break;
            case 781:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_fog));
                break;
            case 801:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_night));
                break;
            case 802:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_night));
                break;
            case 803:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_night));
                break;
            case 804:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_night));
                break;
            case 600:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_snow));
                break;
            case 601:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_snow));
                break;
            case 602:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_snow));
                break;
            case 611:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_snow));
                break;
            case 612:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_snow));
                break;
            case 615:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_snow));
                break;
            case 616:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_snow));
                break;
            case 620:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_snow));
                break;
            case 621:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_snow));
                break;
            case 622:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_snow));
                break;
            case 500:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_rain));
                break;
            case 501:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_rain));
                break;
            case 502:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_rain));
                break;
            case 503:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_rain));
                break;
            case 504:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_rain));
                break;
            case 511:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_rain));
                break;
            case 520:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_rain));
                break;
            case 521:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_rain));
                break;
            case 522:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_rain));
                break;
            case 531:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_rain));
                break;
        }
    }

    private Integer getDayIcon(int actualid) {
        switch (actualid){
            case 800:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                icon_index = R.drawable.sunny;
                break;
            case 200:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                icon_index = R.drawable.thunder_storm;
                break;
            case 201:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                icon_index = R.drawable.thunder_storm;
                break;
            case 202:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                icon_index = R.drawable.thunder_storm;
                break;
            case 210:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                icon_index = R.drawable.thunder_storm;
                break;
            case 211:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                icon_index = R.drawable.thunder_storm;
                break;
            case 212:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                icon_index = R.drawable.thunder_storm;
                break;
            case 221:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                icon_index = R.drawable.thunder_storm;
                break;
            case 230:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                icon_index = R.drawable.thunder_storm;
                break;
            case 231:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                icon_index = R.drawable.thunder_storm;
                break;
            case 232:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                icon_index = R.drawable.thunder_storm;
                break;
            case 300:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                icon_index = R.drawable.drizzle;
                break;
            case 301:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                icon_index = R.drawable.drizzle;
                break;
            case 303:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                icon_index = R.drawable.drizzle;
                break;
            case 310:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                icon_index = R.drawable.drizzle;
                break;
            case 311:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                icon_index = R.drawable.drizzle;
                break;
            case 312:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                icon_index = R.drawable.drizzle;
                break;
            case 313:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                icon_index = R.drawable.drizzle;
                break;
            case 314:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                icon_index = R.drawable.drizzle;
                break;
            case 321:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                icon_index = R.drawable.drizzle;
                break;
            case 701:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                icon_index = R.drawable.fog;
                break;
            case 711:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                icon_index = R.drawable.fog;
                break;
            case 721:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                icon_index = R.drawable.fog;
                break;
            case 731:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                icon_index = R.drawable.fog;
                break;
            case 741:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                icon_index = R.drawable.fog;
                break;
            case 751:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                icon_index = R.drawable.fog;
                break;
            case 761:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                icon_index = R.drawable.fog;
                break;
            case 762:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                icon_index = R.drawable.fog;
                break;
            case 771:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                icon_index = R.drawable.fog;
                break;
            case 781:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                icon_index = R.drawable.fog;
                break;
            case 801:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                icon_index = R.drawable.cloudy;
                break;
            case 802:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                icon_index = R.drawable.cloudy;
                break;
            case 803:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                icon_index = R.drawable.cloudy;
                break;
            case 804:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                icon_index = R.drawable.cloudy;
                break;
            case 600:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                icon_index = R.drawable.snow;
                break;
            case 601:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                icon_index = R.drawable.snow;
                break;
            case 602:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                icon_index = R.drawable.snow;
                break;
            case 611:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                icon_index = R.drawable.snow;
                break;
            case 612:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                icon_index = R.drawable.snow;
                break;
            case 615:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                icon_index = R.drawable.snow;
                break;
            case 616:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                icon_index = R.drawable.snow;
                break;
            case 620:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                icon_index = R.drawable.snow;
                break;
            case 621:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                icon_index = R.drawable.snow;
                break;
            case 622:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                icon_index = R.drawable.snow;
                break;
            case 500:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                icon_index = R.drawable.rainy;
                break;
            case 501:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                icon_index = R.drawable.rainy;
                break;
            case 502:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                icon_index = R.drawable.rainy;
                break;
            case 503:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                icon_index = R.drawable.rainy;
                break;
            case 504:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                icon_index = R.drawable.rainy;
                break;
            case 511:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                icon_index = R.drawable.rainy;
                break;
            case 520:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                icon_index = R.drawable.rainy;
                break;
            case 521:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                icon_index = R.drawable.rainy;
                break;
            case 522:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                icon_index = R.drawable.rainy;
                break;
            case 531:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                icon_index = R.drawable.rainy;
                break;
            default:
                break;
        }
        return icon_index;
    }

    public void Changecity(String string){
        this.updateWeatherData(string);
        this.updateForecast(string);
    }
}
