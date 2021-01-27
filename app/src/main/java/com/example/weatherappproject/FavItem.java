package com.example.weatherappproject;

public class FavItem {
    private String city_name;

    public FavItem(){
    }

    public FavItem(String city_name){
        this.city_name = city_name;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }
}
