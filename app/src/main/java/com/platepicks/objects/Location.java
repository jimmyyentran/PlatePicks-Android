package com.platepicks.objects;

import android.util.Log;

import java.util.List;

/**
 * Created by alyza on 5/9/16.
 */
public class Location {
    private String city;
    private List<String> display_address;
    private String name;
    private String postal_code;
    private String state;
    private List<String> address;

    public Location(String v1, String v2, String v3, String v4, String v5){
        this.city= v1;
//        this.display_address = v2;
        this.name = v3;
        this.postal_code = v4;
        this.state = v5;

    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

//    public String getDisplay_address() {
//        return display_address;
//    }
//
//    public void setDisplay_address(String display_address) {
//        this.display_address = display_address;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<String> getAddress() {
        return address;
    }

    public void setAddress(List<String> address) {
        this.address = address;
    }

    public void printLocation() {
        String fa = "";
        for (String adr : address) fa += adr + ",";

        Log.d("Location", name + ", " + city + "," + state + "," + postal_code + ": " + fa);
    }

    public String getAddressString() {
        String fa = "";
        for (String adr : address) fa += adr + ", ";

        return fa + city + " " + state + ", " + postal_code;
    }
}
