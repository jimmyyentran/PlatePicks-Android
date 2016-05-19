package com.platepicks.dynamoDB;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.platepicks.dynamoDB.nosql.RestaurantDO;
import com.platepicks.objects.Location;

import java.util.List;

/**
 * Created by jimmytran on 5/15/16.
 */
public class TableRestaurant {

    /**
     * Get the restaurant and return return Location object
     * @param restId
     * @return
     */
    public static Location getRestaurantInfo (String restId){
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        RestaurantDO restaurantToGet;
        restaurantToGet = mapper.load(RestaurantDO.class, restId);

        //get information to construct Location object
        String city = restaurantToGet.getCity();
        String restaurantName = restaurantToGet.getRestaurantName();
        int postalCode = restaurantToGet.getPostalCode();
        String state = restaurantToGet.getState();
        List<String> address = restaurantToGet.getAddress();
        String restaurantId = restaurantToGet.getRestaurantId();
        double longitude = restaurantToGet.getLongitude();
        double latitude = restaurantToGet.getLatitude();
        List<String> category = restaurantToGet.getCategories();

        return new Location(city, restaurantName, postalCode, state, address, restaurantId, longitude,
                latitude, category);
    }

    public static void insertRestaurant (Location loc){
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();;

        RestaurantDO restaurantToBeInserted = new RestaurantDO();
        restaurantToBeInserted.setCity(loc.getCity());
        restaurantToBeInserted.setRestaurantName(loc.getRestaurantName());
        restaurantToBeInserted.setPostalCode(loc.getPostal_code());
        restaurantToBeInserted.setAddress(loc.getAddress());
        restaurantToBeInserted.setRestaurantId(loc.getRestaurantId());
        restaurantToBeInserted.setLongitude(loc.getLongitude());
        restaurantToBeInserted.setLatitude(loc.getLatitude());
        restaurantToBeInserted.setCategories(loc.getCategory());

        AmazonClientException lastException = null;

        try {
            mapper.save(restaurantToBeInserted);
        } catch (final AmazonClientException ex) {
            System.out.println("Failed saving item batch: " + ex.getMessage());
            lastException = ex;
        }

        if (lastException != null) {
            // Re-throw the last exception encountered to alert the user.
            throw lastException;
        }

        System.out.println("Insert successful");
    }
}
