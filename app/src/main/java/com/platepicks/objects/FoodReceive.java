package com.platepicks.objects;

import java.net.URL;

/**
 * Created by alyza on 5/9/16.
 */
public class FoodReceive {

    public String food_id;
    public Location location;
    public String name;
    public URL url;

    public FoodReceive(String v1, Location v2, String v3, URL v4){
        this.food_id= v1;
        this.location = v2;
        this.name = v3;
        this.url = v4;

    }

    public String getFood_id() {
        return food_id;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public void print() {
        System.out.println(food_id);
        System.out.println(name);
        System.out.println(url.toString());
        System.out.println(location.getName());
        System.out.println(location.getCity());
    }
}
