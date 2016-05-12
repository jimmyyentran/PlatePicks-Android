package com.platepicks.objects;

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
    private Integer offset;

    // https://www.yelp.com/developers/documentation/v2/search_api
    // Daniel's notes:
    // 1. Leave query blank to search "everything" (given other constraints like radius)
    // 2. Sort: 0 = best matched, 1 = distance, 2 = highest rated
    public FoodRequest(String query, Integer food_per_business,
                       String location, Integer number_of_businesses,
                       Integer radius_filter, String category_filter,
                       Integer sort_option){
        this.term = query;
        this.food_per_business = food_per_business;
        this.ll = location;
        this.limit = number_of_businesses;
        this.radius_filter = radius_filter;
        this.category_filter = category_filter;
        this.sort = sort_option;
        String delims = "[,]";
        String[] tokens = location.split(delims);
    }

    public void addCategory(String category){
        if(category_filter.isEmpty()){
            category_filter = category;
        }
        else{
            category_filter += "," + category;
        }

    }

    public void setlongitude(Double l){
        String delims = "[,]";
        String[] tokens = ll.split(delims);
        Double latitude = Double.parseDouble(tokens[1]);
        String lon = String.valueOf(l);
        String lat = String.valueOf(latitude);
        ll = lon + ", " + lat;
    }

    public void setlattitude(Double l){
        String delims = "[,]";
        String[] tokens = ll.split(delims);
        Double longitude = Double.parseDouble(tokens[0]);
        String lat = String.valueOf(l);
        String lon = String.valueOf(longitude);
        ll = lon + ", " + lat;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
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
