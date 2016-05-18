package com.platepicks.dynamoDB;

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
        String postalCode = String.valueOf(restaurantToGet.getPostalCode());
        String state = restaurantToGet.getState();
        List<String> address = restaurantToGet.getAddress();

        return new Location(city, restaurantName, postalCode, state, address);


    }
}
