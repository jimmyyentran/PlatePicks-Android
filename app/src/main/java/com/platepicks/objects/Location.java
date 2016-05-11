package com.platepicks.objects;

/**
 * Created by alyza on 5/9/16.
 */
public class Location {
    public String city;
    public String display_address;
    public String name;
    public String postal_code;
    public String state;

    public Location(String v1, String v2, String v3, String v4, String v5){
        this.city= v1;
        this.display_address = v2;
        this.name = v3;
        this.postal_code = v4;
        this.state = v5;

    }
}
