package com.platepicks.objects;

import android.util.Log;

import java.util.List;

/**
 * Created by alyza on 5/9/16.
 */
public class Location {
    private String city;
    //    private List<String> display_address;
    private String restaurantId;
    private double longitude;
    private double latitude;
    private List<String> category;
    private String restaurant_name;
    private int postal_code;
    private String state;
    private List<String> address;

    public Location(String city, String name, int postal_code, String state, List<String> address,
                    String restaurantId, double longitude, double latitude, List<String> category){
        this.city= city;
        this.restaurant_name = name;
        this.restaurantId = restaurantId;
        this.postal_code = postal_code;
        this.state = state;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.category = category;
    }

    public Location(){

    }

    //mutator methods
    public void setCity(String city) {
        this.city = city;
    }
    public void setName(String name) {
        this.restaurant_name = name;
    }
    public void setPostal_code(int postal_code) {
        this.postal_code = postal_code;
    }
    public void setState(String state) {
        this.state = state;
    }
    public void setAddress(List<String> address) {
        this.address = address;
    }
    //    public String getDisplay_address() { return display_address; }
    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }


    //accessor methods
    public String getCity() {
        return city;
    }
    public String getName() {
        return restaurant_name;
    }
    public int getPostal_code() {
        return postal_code;
    }
    public String getState() {
        return state;
    }
    public List<String> getAddress() {
        return address;
    }
    public double getLongitude() {
        return longitude;
    }
    public List<String> getCategory() {
        return category;
    }
    public String getRestaurantName() {
        return restaurant_name;
    }
    public String getRestaurantId() {
        return restaurantId;
    }
    public double getLatitude() {
        return latitude;
    }

    //    public void setDisplay_address(String display_address) { this.display_address = display_address; }

    public void printLocation() {
        String fa = "";
        for (String adr : address) fa += adr + ",";

        Log.d("Location", restaurant_name + ", " + city + "," + state + "," + postal_code + ": " + fa);
    }

    public String getAddressString() {
        String fa = "";
        for (String adr : address) fa += adr + ", ";

        return fa + city + " " + state + ", " + postal_code;
    }
}
