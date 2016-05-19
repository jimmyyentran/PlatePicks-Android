import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.platepicks.dynamoDB.nosql.FoodDO;
import com.platepicks.dynamoDB.nosql.RestaurantDO;
import com.platepicks.objects.FoodReceive;
import com.platepicks.objects.Location;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.platepicks.dynamoDB.TableFood.dislikeFood;
import static com.platepicks.dynamoDB.TableFood.getFood;
import static com.platepicks.dynamoDB.TableFood.insertFood;
import static com.platepicks.dynamoDB.TableFood.likeFood;

/**
 * Created by alyza on 5/16/16.
 */


@RunWith(RobolectricTestRunner.class)
@Config(manifest="src/main/AndroidManifest.xml", sdk = 18) //sdk level may change
public class TableFoodTest {

    private final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
    //    @Test
    public void DynamodbInsertToFoodTest() {
        // Start all test input as 'test' so it can be easily removed from table later
        // insertSampleData("testRestaurant", "testAddress");
    }

    // @Test
    public void DynamodbUpdateFoodTest(){
        //updateSampleData("testRestaurant", "testAddress");
    }

    @Test
    public void insertFoodTest(){
        //Make sample food item
        FoodReceive item = new FoodReceive();
        Location loc = new Location();
        loc.setRestaurantId("TestRestaurantID");
        item.setLocation(loc);
        item.setName("TestFood");
        item.setFood_id("TestFoodId");

        insertFood(item);
    }

    //   @Test
    public void DynamodbGetFoodTest(){
        getSampleData("tortas-sinaloa-corona");
    }
    @Test
    public void DynamodbGetFoodFromFoodTest(){
        getSampleDataFromFood("nAckXM1Z7qg6qQUBDZ8mXg");
    }

    @Test
    public void likeFoodWithValidFoodIDTest(){
        FoodReceive retreived = getFood("_icxSHw7vkV7gbd-Oi2XWQ");
        likeFood(retreived);
    }

    @Test
    public void LikeFoodWithInvalidFoodIDTest(){
        FoodReceive retreived = getFood("_icxSHw7vkV7gbd-Oi2XWQ");
        retreived.setFood_id("testingFoodLike");
        retreived.getLocation().setRestaurantId("testRestaurantLike");
        likeFood(retreived);
    }

    @Test
    public void DisLikeFoodWithInvalidFoodIDTest(){
        FoodReceive retrieved = getFood("_icxSHw7vkV7gbd-Oi2XWQ");
        retrieved.setFood_id("testingFoodBad");
        retrieved.getLocation().setRestaurantId("tesRestaurantBad");
        dislikeFood(retrieved);
    }

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
    public void getFoodName(String foodId) {
        FoodReceive foodItemReceived = getFood(foodId);
        String foodName = foodItemReceived.getName();
        System.out.println("food name: " + foodName);
        System.out.println("URL: " + foodItemReceived.getUrl() );

        Location loc = foodItemReceived.getLocation();
        System.out.println("restaurant name: " + loc.getName());
        System.out.println("restaurant postal code: " + loc.getPostal_code());
        System.out.println("restaurant state: " + loc.getState());
    }
}