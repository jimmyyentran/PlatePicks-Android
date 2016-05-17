package com.platepicks.objects;

import java.util.List;

/**
 * Created by alyza on 5/9/16.
 */
public class Location {
    private String city;
//    private List<String> display_address;
    private String name;
    private String postal_code;
    private String state;
    private List<String> address;

    public Location(String city, String name, String postal_code, String state, List<String> address){
        this.city= city;
        this.name = name;
        this.postal_code = postal_code;
        this.state = state;
        this.address = address;

    }


    //mutator methods
    public void setCity(String city) {
        this.city = city;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }
    public void setState(String state) {
        this.state = state;
    }
    public void setAddress(List<String> address) {
        this.address = address;
    }
    //    public String getDisplay_address() { return display_address; }

    //accessor methods
    public String getCity() {
        return city;
    }
    public String getName() {
        return name;
    }
    public String getPostal_code() {
        return postal_code;
    }
    public String getState() {
        return state;
    }
    public List<String> getAddress() {
        return address;
    }
    //    public void setDisplay_address(String display_address) { this.display_address = display_address; }


}
