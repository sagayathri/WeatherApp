package com.gayathri.weatherapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.chrono.MinguoChronology;

public class MainActivity extends AppCompatActivity {

    String data;
    private static final String filename  ="Data.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            this.getSupportFragmentManager().beginTransaction().add(R.id.container, new WeatherFragment()).commit();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("firstStart", true);

        if(isFirstRun){
            Toast.makeText(this, "Welcome to MyWeatherApp!", Toast.LENGTH_LONG).show();
            showInputDialog(this);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstStart", false);
            editor.apply();
        }else{
            readAssetFile();
            Toast.makeText(this, "Hi there, How are you today!", Toast.LENGTH_LONG).show();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.change_city){
            showInputDialog(this);
        }else if(id == R.id.refresh ){
            TextView city_name =(TextView) findViewById(R.id.city_field);
            changeCity(city_name.getText().toString());
            Toast.makeText(getApplicationContext(), "Data Updated!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void showInputDialog(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter a City");
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeCity(input.getText().toString());
            }
        });
        builder.show();
    }

    public void changeCity(String string) {
        if(!string.equals("")) {
            WeatherFragment wf = (WeatherFragment) this.getSupportFragmentManager().findFragmentById(R.id.container);
            wf.Changecity(string);
            (new CityPreference(this)).setCity(string);
        }else
            showInputDialog(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        data =  (new CityPreference(this)).getCity();
        writeAssetFile(data);
    }

    public void writeAssetFile(String data){
        FileOutputStream fos =null;
        try {
            fos=openFileOutput(filename, MODE_PRIVATE);
            fos.write(data.getBytes());
            getFilesDir();
            if(fos!=null){
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readAssetFile(){
        try {
            FileInputStream fis = null;
            fis = openFileInput(filename);
            InputStreamReader inputStream = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(inputStream);
            StringBuilder stringBuilder = new StringBuilder();
            String text;
            while ((text = bufferedReader.readLine()) != null) {
                stringBuilder.append(text);
            }
            changeCity(stringBuilder.toString());
            if(fis!=null){
                fis.close();
            }
        }catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
