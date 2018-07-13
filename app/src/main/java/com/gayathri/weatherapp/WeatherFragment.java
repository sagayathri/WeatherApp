package com.gayathri.weatherapp;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WeatherFragment extends Fragment {
    TextView cityfeild, update, details, currenttemp;
    ImageView weathericon;
    String finalDate, min_max, newDate;
    ListView listView;
    Handler handler;
    Integer icon_index, day_icon_index;
    ArrayList<SingleRow> arrayList;
    ArrayList<SingleDay> day_arrayList;
    SingleRow singleRow;
    SingleDay singleDay;

    public WeatherFragment() {
        handler = new Handler();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arrayList = new ArrayList<SingleRow>();
        day_arrayList = new ArrayList<SingleDay>();
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

    private void renderForecast(final JSONObject json) {
        try {
            if(!arrayList.isEmpty())
                arrayList.clear();
            final JSONArray jsondetails = json.getJSONArray("list");
            for (int i = 0; i < jsondetails.length(); i++) {
                final JSONObject list = jsondetails.getJSONObject(i);
                final String dateString = list.getString("dt_txt");
                String[] splitdate = dateString.split(" ");
                String time = splitdate[1];
                final String dateinfo = splitdate[0];
                JSONObject weatherJSON = list.getJSONArray("weather").getJSONObject(0);
                String weather_description = weatherJSON.getString("description");
                String upperString = weather_description.substring(0,1).toUpperCase() + weather_description.substring(1);
                Integer weather_id = weatherJSON.getInt("id");
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                final Date date = (Date) formatter.parse(dateinfo);
                SimpleDateFormat newFormat = new SimpleDateFormat("EE, dd/MM");
                finalDate = newFormat.format(date);
                JSONObject mainJSON = list.getJSONObject("main");
                String tempmin = mainJSON.getString("temp_min");
                String tempmax = mainJSON.getString("temp_max");
                Double double_min = Double.valueOf(tempmin) - 273.15;
                Double double_max = Double.valueOf(tempmax) - 273.15;
                String temp_min = String.valueOf(double_min.intValue());
                String temp_max = String.valueOf(double_max.intValue());
                min_max = (temp_min) + "°C / " + temp_max + "°C";
                switch (time) {
                    case "21:00:00": {
                        icon_index = getDayIcon(weather_id);
                        singleRow = new SingleRow();
                        singleRow.setDate_feild(finalDate);
                        singleRow.setDescription(upperString);
                        singleRow.setIcon_field(icon_index);
                        singleRow.setMinmax_field(min_max);
                        if (arrayList.size() < 5) {
                            arrayList.add(singleRow);
                        }
                        break;
                    }
                    default:
                        continue;
                }
                listView.setAdapter(new myAdapter(getContext(), arrayList));
            }
        }catch (JSONException e) {
                e.printStackTrace();
        } catch (ParseException e) {
                e.printStackTrace();
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    final JSONArray jsondetails = json.getJSONArray("list");
                    for (int i = 0; i < jsondetails.length(); i++) {
                        JSONObject list = jsondetails.getJSONObject(i);
                        String epochdate = list.getString("dt");
                        Long epodate = Long.parseLong(epochdate);
                        Date epoDate = new Date(epodate*1000);
                        SimpleDateFormat newFormat2 = new SimpleDateFormat("EEE, dd-MMM-yyyy");
                        final String dateString = list.getString("dt_txt");
                        String[] splitdate = dateString.split(" ");
                        String time = splitdate[1];
                        String subtime = time.substring(0, 5);
                        final String dateinfo = splitdate[0];
                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        final Date date = (Date) formatter.parse(dateinfo);
                        SimpleDateFormat newFormat = new SimpleDateFormat("EE, dd/MM");
                        finalDate = newFormat.format(date);
                        String getDate = String.valueOf(finalDate);
                        String itemlist = arrayList.get(position).date_feild;
                        JSONObject mainJSON = list.getJSONObject("main");
                        String temp = mainJSON.getString("temp");
                        JSONObject weatherJSON = list.getJSONArray("weather").getJSONObject(0);
                        String day_weather_description = weatherJSON.getString("description");
                        String upperString = day_weather_description.substring(0,1).toUpperCase() + day_weather_description.substring(1);
                        Integer day_weather_id = weatherJSON.getInt("id");
                        Double tempinC = Double.valueOf(temp) - 273.15;
                        String tempnew = (new DecimalFormat("##").format(tempinC))+"°C";
                        if(itemlist.equals(getDate)) {
                            newDate = newFormat2.format(epoDate);
                            Integer dayiconindex = getDayForecastIcon(day_weather_id);
                            singleDay = new SingleDay();
                            singleDay.setDay_time(subtime);
                            singleDay.setDay_weather_description(upperString);
                            singleDay.setDay_temp(tempnew);
                            singleDay.setDay_icon_field(dayiconindex);
                            day_arrayList.add(singleDay);
                        }
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(newDate);
                    ListView day_listView = new ListView(getContext());
                    day_listView.setAdapter(new dayAdapter(getContext(), day_arrayList));
                    builder.setView(day_listView);
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            day_arrayList.clear();
                        }
                    });
                    builder.show();
                    day_arrayList.clear();
                }catch (JSONException e1) {
                    e1.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Integer getDayForecastIcon(int weather_id) {
        switch (weather_id) {
            case 800:
                day_icon_index = R.drawable.sunny_small;
                break;
            case 200:
                day_icon_index = R.drawable.thunderstrom_rain_small;
                break;
            case 201:
                day_icon_index = R.drawable.thunderstrom_rain_small;
                break;
            case 202:
                day_icon_index = R.drawable.thunderstrom_rain_small;
                break;
            case 210:
                day_icon_index = R.drawable.thunder_storm_small;
                break;
            case 211:
                day_icon_index = R.drawable.thunder_storm_small;
                break;
            case 212:
                day_icon_index = R.drawable.thunder_storm_small;
                break;
            case 221:
                day_icon_index = R.drawable.thunder_storm_small;
                break;
            case 230:
                day_icon_index = R.drawable.thunder_storm_small;
                break;
            case 231:
                day_icon_index = R.drawable.thunderstrom_rain_small;
                break;
            case 232:
                day_icon_index = R.drawable.thunderstrom_rain_small;
                break;
            case 300:
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 301:
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 303:
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 310:
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 311:
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 312:
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 313:
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 314:
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 321:
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 701:
                day_icon_index = R.drawable.fog_small;
                break;
            case 711:
                day_icon_index = R.drawable.fog_small;
                break;
            case 721:
                day_icon_index = R.drawable.fog_small;
                break;
            case 731:
                day_icon_index = R.drawable.fog_small;
                break;
            case 741:
                day_icon_index = R.drawable.fog_small;
                break;
            case 751:
                day_icon_index = R.drawable.fog_small;
                break;
            case 761:
                day_icon_index = R.drawable.fog_small;
                break;
            case 762:
                day_icon_index = R.drawable.fog_small;
                break;
            case 771:
                day_icon_index = R.drawable.fog_small;
                break;
            case 781:
                day_icon_index = R.drawable.fog_small;
                break;
            case 801:
                day_icon_index = R.drawable.cloudy_small;
                break;
            case 802:
                day_icon_index = R.drawable.scattered_clouds_small;
                break;
            case 803:
                day_icon_index = R.drawable.scattered_clouds_small;
                break;
            case 804:
                day_icon_index = R.drawable.overcast_clouds_small;
                break;
            case 600:
                day_icon_index = R.drawable.light_snow_small;
                break;
            case 601:
                day_icon_index = R.drawable.snow_small;
                break;
            case 602:
                day_icon_index = R.drawable.heavy_snow_small;
                break;
            case 611:
                day_icon_index = R.drawable.sleet_small;
                break;
            case 612:
                day_icon_index = R.drawable.sleet_small;
                break;
            case 615:
                day_icon_index = R.drawable.snow_rain_small;
                break;
            case 616:
                day_icon_index = R.drawable.snow_rain_small;
                break;
            case 620:
                day_icon_index = R.drawable.snow_rain_small;
                break;
            case 621:
                day_icon_index = R.drawable.heavy_snow_small;
                break;
            case 622:
                day_icon_index = R.drawable.heavy_snow_small;
                break;
            case 500:
                day_icon_index = R.drawable.light_rain_small;
                break;
            case 501:
                day_icon_index = R.drawable.light_rain_small;
                break;
            case 502:
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 503:
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 504:
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 511:
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 520:
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 521:
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 522:
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 531:
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            default:
                break;
        }
        return day_icon_index;
    }

    private void renderWeather(JSONObject json){
        try{
            this.cityfeild.setText(json.getString("name").toUpperCase(Locale.UK)
                    +", "+json.getJSONObject("sys").getString("country"));
            JSONObject jsondetails = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            JSONObject wind = json.getJSONObject("wind");

            this.details.setText(jsondetails.getString("description").toUpperCase(Locale.UK)+"\n"
                    + "Wind: "+wind.getString("speed")+"mph, " + getWind(wind.getString("deg"))+"\n"
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
        int degree = Integer.parseInt(deg);
        if(degree == 0 || degree == 360){
            direction = "North";
        }else if(degree > 0 && degree < 45){
            direction = "North-NorthEast";
        }else if(degree == 45){
            direction = "NorthEast";
        }else if(degree > 45 && degree < 90){
            direction = "East-NorthEast";
        }else if(degree == 90){
            direction = "East";
        }else if(degree > 90 && degree < 135){
            direction = "East-SouthEast";
        }else if(degree == 135){
            direction = "SouthEast";
        }else if(degree > 135 && degree < 180){
            direction = "South-SouthEast";
        }else if(degree == 180){
            direction = "South";
        }else if(degree > 180 && degree < 225){
            direction = "South-SouthWest";
        }else if(degree == 225){
            direction = "SouthWest";
        }else if(degree > 225 && degree < 270){
            direction = "West-SouthWest";
        }else if(degree == 270){
            direction = "West";
        }else if(degree > 270 && degree < 315){
            direction = "West-NorthWest";
        }else if(degree == 315){
            direction = "West";
        }else if(degree > 315 && degree < 360){
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

    private Integer getNightIcon(int actualid) {
        switch (actualid) {
            case 200:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunderstrom_rain));
                }
                icon_index = R.drawable.thunderstrom_rain;
                day_icon_index = R.drawable.thunderstrom_rain;
                break;
            case 201:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunderstrom_rain));
                }
                icon_index = R.drawable.thunderstrom_rain;
                day_icon_index = R.drawable.thunderstrom_rain;
                break;
            case 202:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunderstrom_rain));
                }
                icon_index = R.drawable.thunderstrom_rain;
                day_icon_index = R.drawable.thunderstrom_rain;
                break;
            case 210:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                }
                icon_index = R.drawable.thunder_storm;
                day_icon_index = R.drawable.thunder_storm_small;
                break;
            case 211:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                }
                icon_index = R.drawable.thunder_storm;
                day_icon_index = R.drawable.thunder_storm_small;
                break;
            case 212:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                }
                icon_index = R.drawable.thunder_storm;
                day_icon_index = R.drawable.thunder_storm_small;
                break;
            case 221:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                }
                icon_index = R.drawable.thunder_storm;
                day_icon_index = R.drawable.thunder_storm_small;
                break;
            case 230:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunderstrom_rain));
                }
                icon_index = R.drawable.thunderstrom_rain;
                day_icon_index = R.drawable.thunderstrom_rain;
                break;
            case 231:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunderstrom_rain));
                }
                icon_index = R.drawable.thunderstrom_rain;
                day_icon_index = R.drawable.thunderstrom_rain;
                break;
            case 232:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunderstrom_rain));
                }
                icon_index = R.drawable.thunderstrom_rain;
                day_icon_index = R.drawable.thunderstrom_rain;
                break;
            case 300:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                }
                icon_index = R.drawable.night_drizzle;
                day_icon_index = R.drawable.night_drizzle_small;
                break;
            case 301:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                }
                icon_index = R.drawable.night_drizzle;
                day_icon_index = R.drawable.night_drizzle_small;
                break;
            case 303:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                }
                icon_index = R.drawable.night_drizzle;
                day_icon_index = R.drawable.night_drizzle_small;
                break;
            case 310:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                }
                icon_index = R.drawable.night_drizzle;
                day_icon_index = R.drawable.night_drizzle_small;
                break;
            case 311:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                }
                icon_index = R.drawable.night_drizzle;
                day_icon_index = R.drawable.night_drizzle_small;
                break;
            case 312:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                }
                icon_index = R.drawable.night_drizzle;
                day_icon_index = R.drawable.night_drizzle_small;
                break;
            case 313:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                }
                icon_index = R.drawable.night_drizzle;
                day_icon_index = R.drawable.night_drizzle_small;
                break;
            case 314:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                }
                icon_index = R.drawable.night_drizzle;
                day_icon_index = R.drawable.night_drizzle_small;
                break;
            case 321:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_drizzle));
                }
                icon_index = R.drawable.night_drizzle;
                day_icon_index = R.drawable.night_drizzle_small;
                break;
            case 500:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_rain));
                }
                icon_index = R.drawable.night_rain;
                day_icon_index = R.drawable.night_rain_small;
                break;
            case 501:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_rain));
                }
                icon_index = R.drawable.night_rain;
                day_icon_index = R.drawable.night_rain_small;
                break;
            case 502:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 503:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 504:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 511:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 520:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 521:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 522:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 531:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 600:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                }
                icon_index = R.drawable.snow;
                day_icon_index = R.drawable.snow_small;
                break;
            case 601:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                }
                icon_index = R.drawable.snow;
                day_icon_index = R.drawable.snow_small;
                break;
            case 602:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_snow));
                }
                icon_index = R.drawable.heavy_snow;
                day_icon_index = R.drawable.heavy_snow_small;
                break;
            case 611:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.sleet));
                }
                icon_index = R.drawable.sleet;
                day_icon_index = R.drawable.sleet_small;
                break;
            case 612:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.sleet));
                }
                icon_index = R.drawable.sleet;
                day_icon_index = R.drawable.sleet_small;
                break;
            case 615:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow_rain));
                }
                icon_index = R.drawable.snow_rain;
                day_icon_index = R.drawable.snow_rain_small;
                break;
            case 616:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow_rain));
                }
                icon_index = R.drawable.snow_rain;
                day_icon_index = R.drawable.snow_rain_small;
                break;
            case 620:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow_rain));
                }
                icon_index = R.drawable.snow_rain;
                day_icon_index = R.drawable.snow_rain_small;
                break;
            case 621:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow_rain));
                }
                icon_index = R.drawable.snow_rain;
                day_icon_index = R.drawable.snow_rain_small;
                break;
            case 622:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow_rain));
                }
                icon_index = R.drawable.snow_rain;
                day_icon_index = R.drawable.snow_rain_small;
                break;
            case 701:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }
                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 711:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }
                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 721: if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }
                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 731:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }
                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 741:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 751:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }
                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 761:
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 762:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }
                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 771:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }
                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 781:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }
                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 800: {
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageResource(R.drawable.moon);
                }
                icon_index = R.drawable.moon;
                day_icon_index = R.drawable.moon_small;
            }
            break;
            case 801:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.night_clouds));
                }
                icon_index = R.drawable.night_clouds;
                day_icon_index = R.drawable.night_clouds;
                break;
            case 802:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.scattered_clouds));
                }
                icon_index = R.drawable.scattered_clouds;
                day_icon_index = R.drawable.scattered_clouds_small;
                break;
            case 803:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.scattered_clouds));
                }
                icon_index = R.drawable.scattered_clouds;
                day_icon_index = R.drawable.scattered_clouds_small;
                break;
            case 804:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.overcast_clouds));
                }
                icon_index = R.drawable.overcast_clouds;
                day_icon_index = R.drawable.overcast_clouds_small;
                break;
            default:
                break;
        }
        return icon_index;
    }

    private Integer getDayIcon(int actualid) {
        switch (actualid){
            case 200:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunderstrom_rain));
                }
                icon_index = R.drawable.thunderstrom_rain;
                day_icon_index = R.drawable.thunderstrom_rain;
                break;
            case 201:
                if (weathericon.getDrawable() == null) {
                weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunderstrom_rain));
                }
                icon_index = R.drawable.thunderstrom_rain;
                day_icon_index = R.drawable.thunderstrom_rain;
                break;
            case 202:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunderstrom_rain));
                }
                icon_index = R.drawable.thunderstrom_rain;
                day_icon_index = R.drawable.thunderstrom_rain;
                break;
            case 210:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                }
                icon_index = R.drawable.thunder_storm;
                day_icon_index = R.drawable.thunder_storm_small;
                break;
            case 211:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                }
                icon_index = R.drawable.thunder_storm;
                day_icon_index = R.drawable.thunder_storm_small;
                break;
            case 212:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                }
                icon_index = R.drawable.thunder_storm;
                day_icon_index = R.drawable.thunder_storm_small;
                break;
            case 221:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_storm));
                }
                icon_index = R.drawable.thunder_storm;
                day_icon_index = R.drawable.thunder_storm_small;
                break;
            case 230:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunderstrom_rain));
                }
                icon_index = R.drawable.thunderstrom_rain;
                day_icon_index = R.drawable.thunderstrom_rain;
                break;
            case 231:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunderstrom_rain));
                }
                icon_index = R.drawable.thunderstrom_rain;
                day_icon_index = R.drawable.thunderstrom_rain;
                break;
            case 232:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.thunderstrom_rain));
                }
                icon_index = R.drawable.thunderstrom_rain;
                day_icon_index = R.drawable.thunderstrom_rain;
                break;
            case 300:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                }
                icon_index = R.drawable.drizzle;
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 301:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                }
                icon_index = R.drawable.drizzle;
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 303:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                }
                icon_index = R.drawable.drizzle;
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 310:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                }
                icon_index = R.drawable.drizzle;
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 311:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                }
                icon_index = R.drawable.drizzle;
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 312:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                }
                icon_index = R.drawable.drizzle;
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 313:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                }
                icon_index = R.drawable.drizzle;
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 314:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                }
                icon_index = R.drawable.drizzle;
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 321:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.drizzle));
                }
                icon_index = R.drawable.drizzle;
                day_icon_index = R.drawable.drizzle_small;
                break;
            case 500:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.light_rain));
                }
                icon_index = R.drawable.light_rain;
                day_icon_index = R.drawable.light_rain_small;
                break;
            case 501:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.light_rain));
                }
                icon_index = R.drawable.light_rain;
                day_icon_index = R.drawable.light_rain_small;
                break;
            case 502:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 503:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 504:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 511:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 520:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 521:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 522:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 531:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_rain));
                }
                icon_index = R.drawable.heavy_rain;
                day_icon_index = R.drawable.heavy_rain_small;
                break;
            case 600:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.light_snow));
                }
                icon_index = R.drawable.light_snow;
                day_icon_index = R.drawable.light_snow_small;
                break;
            case 601:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                }
                icon_index = R.drawable.snow;
                day_icon_index = R.drawable.snow_small;
                break;
            case 602:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.heavy_snow));
                }
                icon_index = R.drawable.heavy_snow;
                day_icon_index = R.drawable.heavy_snow_small;
                break;
            case 611:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.sleet));
                }
                icon_index = R.drawable.sleet;
                day_icon_index = R.drawable.sleet_small;
                break;
            case 612:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.sleet));
                }
                icon_index = R.drawable.sleet;
                day_icon_index = R.drawable.sleet_small;
                break;
            case 615:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow_rain));
                }
                icon_index = R.drawable.snow_rain;
                day_icon_index = R.drawable.snow_rain_small;
                break;
            case 616:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow_rain));
                }
                icon_index = R.drawable.snow_rain;
                day_icon_index = R.drawable.snow_rain_small;
                break;
            case 620:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow_rain));
                }
                icon_index = R.drawable.snow_rain;
                day_icon_index = R.drawable.snow_rain_small;
                break;
            case 621:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow_rain));
                }
                icon_index = R.drawable.snow_rain;
                day_icon_index = R.drawable.snow_rain_small;
                break;
            case 622:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.snow_rain));
                }
                icon_index = R.drawable.snow_rain;
                day_icon_index = R.drawable.snow_rain_small;
                break;
            case 701:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 711:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 721:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 731:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 741:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 751:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 761:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 762:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 771:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 781:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                }
                icon_index = R.drawable.fog;
                day_icon_index = R.drawable.fog_small;
                break;
            case 800:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageResource(R.drawable.sunny);
                }
                icon_index = R.drawable.sunny;
                day_icon_index = R.drawable.sunny_small;
            break;
            case 801:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                }
                icon_index = R.drawable.cloudy;
                day_icon_index = R.drawable.cloudy_small;
                break;
            case 802:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.scattered_clouds));
                }
                icon_index = R.drawable.scattered_clouds;
                day_icon_index = R.drawable.scattered_clouds_small;
                break;
            case 803:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.scattered_clouds));
                }
                icon_index = R.drawable.scattered_clouds;
                day_icon_index = R.drawable.scattered_clouds_small;
                break;
            case 804:
                if (weathericon.getDrawable() == null) {
                    weathericon.setImageDrawable(getResources().getDrawable(R.drawable.overcast_clouds));
                }
                icon_index = R.drawable.overcast_clouds;
                day_icon_index = R.drawable.overcast_clouds_small;
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
