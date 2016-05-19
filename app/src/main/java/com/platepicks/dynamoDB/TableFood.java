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

import static com.platepicks.dynamoDB.TableRestaurant.getRestaurantInfo;
import static com.platepicks.dynamoDB.TableRestaurant.insertRestaurant;

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

    /**
     * Increase 'disLike' attribute by 1. If food does not exist, add it to database
     * @param food The object to be increased
     */
    public static void dislikeFood (FoodReceive food){
        Log.d(LOG_TAG, "Inserting like!");
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        AmazonClientException lastException = null;

        FoodDO foodToGet = null;
        try {
            foodToGet = mapper.load(FoodDO.class, food.getFood_id());
            if(foodToGet == null){
                Log.d(LOG_TAG, "The foodId does not exist, attempting to add to database");
                System.out.println("The foodId does not exist, attempting to add to database");
                try {
                    // Food doesn't exist, so upload then exit
                    insertFood(food);
                    insertRestaurant(food.getLocation());
                    Log.d(LOG_TAG, "Upload to database successful");
                    return;
                } catch (final AmazonClientException ex2){
                    lastException = ex2;
                }
            }
        } catch (final AmazonClientException ex) {
            Log.d(LOG_TAG, "The foodId does not exist, attempting to add to database");
            System.out.println("The foodId does not exist, attempting to add to database");
            try {
                // Food doesn't exist, so upload then exit
                insertFood(food);
                insertRestaurant(food.getLocation());
                Log.d(LOG_TAG, "Upload to database successful");
                return;
            } catch (final AmazonClientException ex2){
                lastException = ex2;
            }
            //TODO
        }

        double likes = foodToGet.getLike();
        likes += 1.0;
        foodToGet.setLike(likes);

        try {
            mapper.save(foodToGet);
        } catch (final AmazonClientException ex) {
            Log.d(LOG_TAG,"Failed saving item batch: " + ex.getMessage());
            lastException = ex;
        }

        if (lastException != null) {
//            // Re-throw the last exception encountered to alert the user.
            throw lastException;
        }

        Log.d(LOG_TAG,"Insert like successful");
    }

    /**
     * Increase 'like' attribute by 1. If food does not exist, add it to database
     * @param food The object to be increased
     */
    public static void likeFood (FoodReceive food){
        Log.d(LOG_TAG, "Inserting like!");
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        AmazonClientException lastException = null;

        FoodDO foodToGet = null;
        try {
            foodToGet = mapper.load(FoodDO.class, food.getFood_id());
            if(foodToGet == null){
                Log.d(LOG_TAG, "The foodId does not exist, attempting to add to database");
                System.out.println("The foodId does not exist, attempting to add to database");
                try {
                    // Food doesn't exist, so upload then exit
                    insertFood(food);
                    insertRestaurant(food.getLocation());
                    Log.d(LOG_TAG, "Upload to database successful");
                    return;
                } catch (final AmazonClientException ex2){
                    lastException = ex2;
                }
            }
        } catch (final AmazonClientException ex) {
            Log.d(LOG_TAG, "The foodId does not exist, attempting to add to database");
            System.out.println("The foodId does not exist, attempting to add to database");
            try {
                // Food doesn't exist, so upload then exit
                insertFood(food);
                insertRestaurant(food.getLocation());
                Log.d(LOG_TAG, "Upload to database successful");
                return;
            } catch (final AmazonClientException ex2){
                lastException = ex2;
            }
            //TODO
        }

        double likes = foodToGet.getDislike();
        likes += 1.0;
        foodToGet.setDislike(likes);

        try {
            mapper.save(foodToGet);
        } catch (final AmazonClientException ex) {
            Log.d(LOG_TAG,"Failed saving item batch: " + ex.getMessage());
            lastException = ex;
        }

        if (lastException != null) {
//            // Re-throw the last exception encountered to alert the user.
            throw lastException;
        }

        Log.d(LOG_TAG,"Insert like successful");
    }

    /**
     * Inserting the food into the database, dislike and like are set to 0
     * @param food
     */
    public static void insertFood(FoodReceive food){
        Log.d(LOG_TAG, "Inserting: " + food.getName());
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        final FoodDO firstItem = new FoodDO();

        firstItem.setFoodId(food.getFood_id());
        firstItem.setRestaurantId(food.getLocation().getRestaurantId());
        firstItem.setName(food.getName());

        AmazonClientException lastException = null;

        try {
            mapper.save(firstItem);
        } catch (final AmazonClientException ex) {
            Log.d(LOG_TAG,"Failed saving item batch: " + ex.getMessage());
            lastException = ex;
        }

        if (lastException != null) {
            // Re-throw the last exception encountered to alert the user.
            throw lastException;
        }

        Log.d(LOG_TAG, "Insert successful");
    }

    /*
    * Append the 'like' attribute in table by 1
    * pull current value first, increase by 1, then push
    * Log if successful
    * */
//    public static void likeFood (String foodId){
//        Log.d(LOG_TAG, "Inserting like!");
//        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
//
//        FoodDO foodToGet;
//        foodToGet = mapper.load(FoodDO.class, foodId);
//        double likes = foodToGet.getLike();
//        likes += 1.0;
//        foodToGet.setLike(likes);
//
//        AmazonClientException lastException = null;
//
//        try {
//            mapper.save(foodToGet);
//        } catch (final AmazonClientException ex) {
//            Log.d(LOG_TAG,"Failed saving item batch: " + ex.getMessage());
//            lastException = ex;
//        }
//
//        if (lastException != null) {
////            // Re-throw the last exception encountered to alert the user.
//            throw lastException;
//        }
//
//        Log.d(LOG_TAG,"Insert like successful");
//
//    }

    /*
    * Append the 'dislike' attribute in table by 1
    *
    * Log if successful
    * */
//    public static void dislikeFood (FoodReceive food){
//        Log.d(LOG_TAG, "Inserting dislike!");
//        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
//        AmazonClientException lastException = null;
//
//        FoodDO foodToGet = null;
//        try {
//            foodToGet = mapper.load(FoodDO.class, food.getFood_id());
//        } catch (final AmazonClientException ex) {
//            Log.d(LOG_TAG, "The foodId does not exist, attempting to add to database");
//            try {
//                // Food doesn't exist, so upload then exit
//                insertFood(food);
//                insertRestaurant(food.getLocation());
//                Log.d(LOG_TAG,"Upload to database successful");
//                return;
//            } catch (final AmazonClientException ex2){
//                lastException = ex2;
//            }
//            //TODO
//        }
//
//        double dislikes = foodToGet.getDislike();
//        dislikes += 1.0;
//        foodToGet.setDislike(dislikes);
//
//        try {
//            mapper.save(foodToGet);
//        } catch (final AmazonClientException ex) {
//            Log.d(LOG_TAG, "Failed saving item batch: " + ex.getMessage());
//            lastException = ex;
//        }
//
//        if (lastException != null) {
////            // Re-throw the last exception encountered to alert the user.
//            throw lastException;
//        }
//
//        System.out.println("Insert dislike successful");
//    }

}
