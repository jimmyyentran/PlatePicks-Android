package com.tinderui.object;

/**
 * Created by alyza on 5/2/16.
 */
public class FoodRequest {
    public String term;
    public Integer food_per_business;
    public String ll;
    public Integer limit;
    public Integer radius_filter;
    public String category_filter;
    public Integer sort;
    public Double longitude;
    public Double lattitude;

    public FoodRequest(String v1, Integer v2, String v3, Integer v4, Integer v5, String v6, Integer v7){
        this.term = v1;
        this.food_per_business = v2;
        this.ll = v3;
        this.limit = v4;
        this.radius_filter = v5;
        this.category_filter = v6;
        this.sort = v7;
        String delims = "[,]";
        String[] tokens = v3.split(delims);
        this.longitude = Double.parseDouble(tokens[0]);
        this.lattitude = Double.parseDouble(tokens[1]);
    }

    public void addCategory(String category){
        if(category_filter.isEmpty()){
            category_filter = category;
        }
        else{
            category_filter += "' " + category;
        }

    }

    public void setlongitude(Double l){
        longitude = l;
        String lon = String.valueOf(l);
        String lat = String.valueOf(lattitude);
        ll = lon + ", " + lat;
    }

    public void setlattitude(Double l){
        lattitude = l;
        String lat = String.valueOf(l);
        String lon = String.valueOf(longitude);
        ll = lon + ", " + lat;
    }

//    public String toString(){
//        return "key = " +key+ ", value= " +value;
//    }
}
