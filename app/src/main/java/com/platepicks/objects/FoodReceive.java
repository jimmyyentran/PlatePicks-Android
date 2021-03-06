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

    public FoodReceive(){}
    /* mutator methods */
    public void setFood_id(String food_id) {
        this.food_id = food_id;
    }
    public void setLocation(Location location) {
        this.location = location;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setUrl(URL url) {
        this.url = url;
    }

    /* accessor methods */
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

    //    public static void main(){
//        FoodRequest req = new FoodRequest("asian", 3, "33.7175, -117.8311", 4, 40000, "Japanese", 1);
//        System.out.println("term: " + req.term);
//        System.out.println("business: " + req.food_per_business);
//        System.out.println("11: " + req.ll);
//        System.out.println("limit: " + req.limit);
//        System.out.println("radius_filter: " + req.radius_filter);
//        System.out.println("category_filter: " + req.category_filter);
//        System.out.println("sort: " + req.sort);
//
//    }

//    public String toString(){
//        return "key = " +key+ ", value= " +value;
//    }
}
