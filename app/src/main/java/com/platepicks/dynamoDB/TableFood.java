package com.platepicks.dynamoDB;

import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.platepicks.dynamoDB.nosql.FoodDO;
import com.platepicks.dynamoDB.nosql.RestaurantDO;
import com.platepicks.objects.FoodReceive;
import com.platepicks.objects.Location;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jimmytran on 5/15/16.
 */
public final class TableFood {
    private static final String LOG_TAG = TableFood.class.getSimpleName();


    /*
    * Query the dabase and return a FoodReceive
    * - requires two calls:
    *   1. Call to food table to get restaurantId
    *   2. Call to restaurant table for the rest
    *
    * - package all info into a FoodReceive
    *  public String food_id;
    *  public Location location;
    *  public String name;
    *  public URL url; http://s3-media3.fl.yelpcdn.com/bphoto/gEEf7KrPffnywZglGxNnjQ/o.jpg
    *
    *  -RestaurantDO
    *   private String _restaurantId;
    * private List<String> _address;
    * private List<String> _categories;
    * private String _city;
    * private double _latitude;
    * private double _longitude;
    * private int _postalCode;
    * private String _restaurantName;
    * private String _state;
    * */

    public static FoodReceive getFood (String foodId){
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        FoodDO foodToGet;
        foodToGet = mapper.load(FoodDO.class, foodId);
        String restId = foodToGet.getRestaurantId();
        String foodName = foodToGet.getName();
        //System.out.println("Got restaurant Id: " + foodToGet.getRestaurantId());
        Location loc = getRestaurantInfo(restId);
        URL myURL = null;

        try{
            String strURL = "http://s3-media3.fl.yelpcdn.com/bphoto/" + foodId + "/o.jpg";
            myURL = new URL(strURL);
        }catch (MalformedURLException e){
            System.out.println("Bad URL");
        }
//        return new FoodReceive("1", null , "3", myURL);
        return new FoodReceive(foodId, loc, foodName, myURL);
    }

    public static Location getRestaurantInfo (String restId){
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        RestaurantDO restaurantToGet;
        restaurantToGet = mapper.load(RestaurantDO.class, restId);

        //get information to construct Location object
        String city = restaurantToGet.getCity();
        String restaurantName = restaurantToGet.getRestaurantName();
        String postalCode = String.valueOf(restaurantToGet.getPostalCode());
        String state = restaurantToGet.getState();
        List<String> address = restaurantToGet.getAddress();

        return new Location(city, restaurantName, postalCode, state, address);


    }

    /*
    * Append the 'like' attribute in table by 1
    * pull current value first, increase by 1, then push
    * Log if successful
    * */

    public static void likeFood (String foodId){
//        Log.d(LOG_TAG, "Like food update successful: " + foodId);

        System.out.println("Inserting like!");
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

        FoodDO foodToGet;
        foodToGet = mapper.load(FoodDO.class, foodId);
        double likes = foodToGet.getLike();
        likes += 1.0;
        foodToGet.setLike(likes);

        AmazonClientException lastException = null;

        try {
            mapper.save(foodToGet);
        } catch (final AmazonClientException ex) {
            System.out.println("Failed saving item batch: " + ex.getMessage());
            lastException = ex;
        }

        if (lastException != null) {
//            // Re-throw the last exception encountered to alert the user.
            throw lastException;
        }

        System.out.println("Insert like successful");

    }

    /*
    * Append the 'dislike' attribute in table by 1
    *
    * Log if successful
    * */
    public static void dislikeFood (String foodId){
//        Log.d(LOG_TAG, "Dislike food update successful: " + foodId);

        System.out.println("Inserting dislike!");
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

        FoodDO foodToGet;
        foodToGet = mapper.load(FoodDO.class, foodId);

        double dislikes = foodToGet.getDislike();
        dislikes += 1.0;
        foodToGet.setDislike(dislikes);

        AmazonClientException lastException = null;

        try {
            mapper.save(foodToGet);
        } catch (final AmazonClientException ex) {
            System.out.println("Failed saving item batch: " + ex.getMessage());
            lastException = ex;
        }

        if (lastException != null) {
//            // Re-throw the last exception encountered to alert the user.
            throw lastException;
        }

        System.out.println("Insert dislike successful");

    }


}
