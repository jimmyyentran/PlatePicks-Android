import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
//import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
//import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
//import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
//import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
//import com.platepicks.dynamoDB.nosql.FoodDO;

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
import com.platepicks.dynamoDB.nosql.RestaurantDO;
import com.platepicks.dynamoDB.nosql.SampleDataGenerator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import context.arch.comm.DataObject; //added

@RunWith(RobolectricTestRunner.class)
@Config(manifest="src/main/AndroidManifest.xml", sdk = 18) //sdk level may change
public class getRestaurantTest {

    private final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

    //Set up persistent activity
    @Before
    public void setUp() {
    }

    //    @Test
    public void DynamodbInsertToFoodTest() {
        // Start all test input as 'test' so it can be easily removed from table later
       // insertSampleData("testRestaurant", "testAddress");
    }

   // @Test
    public void DynamodbUpdateFoodTest(){
        //updateSampleData("testRestaurant", "testAddress");
    }

//   @Test
    public void DynamodbGetFoodTest(){
        getSampleData("tortas-sinaloa-corona");
    }
    @Test
    public void DynamodbGetFoodFromFoodTest(){
        getSampleDataFromFood("nAckXM1Z7qg6qQUBDZ8mXg");
    }


//    public void insertSampleData(String restId, Set<String> address) throws AmazonClientException {
//        System.out.println("Inserting Sample data");
//        final RestaurantDO firstItem = new RestaurantDO();
//
//        firstItem.setRestaurantId(restId);
//        firstItem.setAddress(address);
//        AmazonClientException lastException = null;
//
//        try {
//            mapper.save(firstItem);
//        } catch (final AmazonClientException ex) {
//            System.out.println("Failed saving item batch: " + ex.getMessage());
//            lastException = ex;
//        }
//
//        if (lastException != null) {
//            // Re-throw the last exception encountered to alert the user.
//            throw lastException;
//        }
//
//        System.out.println("Insert successful");
//    }

//    public void updateSampleData(String restId, Set<String> address) throws AmazonClientException {
//        System.out.println("Updating Sample data");
//        final RestaurantDO firstItem = new RestaurantDO();
//
//        firstItem.setRestaurantId(restId);
//        firstItem.setAddress(address);
//        AmazonClientException lastException = null;
//
//        try {
//            mapper.save(firstItem);
//        } catch (final AmazonClientException ex) {
//            System.out.println("Failed saving item batch: " + ex.getMessage());
//            lastException = ex;
//        }
//
//        if (lastException != null) {
//            // Re-throw the last exception encountered to alert the user.
//            throw lastException;
//        }
//
//        System.out.println("Update successful");
//    }

    public void getSampleData(String restaurantId){
        RestaurantDO itemToGet;

        itemToGet = mapper.load(RestaurantDO.class, restaurantId);
        System.out.println("Got State: " + itemToGet.getState());
        System.out.println("Got Postal Code: " + itemToGet.getPostalCode());
    }
    public void getSampleDataFromFood(String foodId) {
        FoodDO foodToGet;
        foodToGet = mapper.load(FoodDO.class, foodId);
        System.out.println("Got restaurant Id: " + foodToGet.getRestaurantId());
    }



        //itemToGet = mapper.load(FoodDO.class, "gAdqUpIbkuaYFyzfIAyjSg");
//        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
//        eav.put(":_id", new AttributeValue().withS("gAdqUpIbkuaYFyzfIAyjSg"));
//        DynamoDBQueryExpression<FoodDO> queryExpression = new DynamoDBQueryExpression<FoodDO>()
//                .withKeyConditionExpression("foodId = :_id")
//                .withExpressionAttributeValues(eav);
//        QuerySpec spec = new QuerySpec()

//                .withValueMap(new ValueMap()
//                        .withString(":_id", "gAdqUpIbkuaYFyzfIAyjSg"));

        //itemToGet = mapper.query(FoodDO.class, "id = :gAdqUpIbkuaYFyzfIAyjSg");
//        List<FoodDO> itemsToGetList = mapper.query(FoodDO.class, queryExpression);
//        DynamoDBQueryExpression<FoodDO> queryExpression = new DynamoDBQueryExpression<FoodDO>()
//                .withKeyConditionExpression("id = :_id")
//                .withValueMap(new ValueMap()
//                    .withString(":_id", "gAdqUpIbkuaYFyzfIAyjSg");


        //System.out.println("Got food name: " + itemToGet.getName());

    }

