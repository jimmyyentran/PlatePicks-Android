import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.platepicks.dynamoDB.nosql.FoodDO;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by alyza on 5/15/16.
 */


import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.platepicks.dynamoDB.nosql.FoodDO;
import com.platepicks.dynamoDB.nosql.ListDO;
import com.platepicks.dynamoDB.nosql.SampleDataGenerator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashSet;

//import context.arch.comm.DataObject; //added

@RunWith(RobolectricTestRunner.class)
@Config(manifest="src/main/AndroidManifest.xml", sdk = 18) //sdk level may change
public class getRestaurantTest {

    private final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();;

    //Set up persistent activity
    @Before
    public void setUp() {
    }

    //    @Test
    public void DynamodbInsertToFoodTest() {
        // Start all test input as 'test' so it can be easily removed from table later
        insertSampleData("testId", "testRestaurant");
    }

    @Test
    public void DynamodbUpdateFoodTest(){
        updateSampleData("testId", "testUpdatedRestaurant");
    }

    @Test
    public void DynamodbGetFoodTest(){
        getSampleData("testId");
    }

    public void insertSampleData(String foodId, String restaurantId) throws AmazonClientException {
        System.out.println("Inserting Sample data");
        final FoodDO firstItem = new FoodDO();

        firstItem.setFoodId(foodId);
        firstItem.setRestaurantId(restaurantId);
        AmazonClientException lastException = null;

        try {
            mapper.save(firstItem);
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

    public void updateSampleData(String foodId, String restaurantId) throws AmazonClientException {
        System.out.println("Updating Sample data");
        final FoodDO firstItem = new FoodDO();

        firstItem.setFoodId(foodId);
        firstItem.setRestaurantId(restaurantId);
        AmazonClientException lastException = null;

        try {
            mapper.save(firstItem);
        } catch (final AmazonClientException ex) {
            System.out.println("Failed saving item batch: " + ex.getMessage());
            lastException = ex;
        }

        if (lastException != null) {
            // Re-throw the last exception encountered to alert the user.
            throw lastException;
        }

        System.out.println("Update successful");
    }

    public void getSampleData(String foodId){
        RestaurantDO itemToGet = new RestaurantDO();
        itemToGet.setFoodId(foodId);

        itemToGet = mapper.load(FoodDO.class, foodId);
        System.out.println("Got: " + itemToGet.getRestaurantId());
    }
}
