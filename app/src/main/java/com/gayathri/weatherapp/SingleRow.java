package com.gayathri.weatherapp;

public class SingleRow{
    String date_feild;
    String description;
    String minmax_field;
    Integer icon_field;

    SingleRow(){ }

    public String getDate_feild() {
        return date_feild;
    }

    public void setDate_feild(String date_feild) {
        this.date_feild = date_feild;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMinmax_field() {
        return minmax_field;
    }

    public void setMinmax_field(String minmax_field) {
        this.minmax_field = minmax_field;
    }

    public Integer getIcon_field() {
        return icon_field;
    }

    public void setIcon_field(Integer icon_field) {
        this.icon_field = icon_field;
    }
}