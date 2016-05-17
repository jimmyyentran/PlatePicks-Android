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

import static com.platepicks.dynamoDB.TableFood.getFood;

/**
 * Created by alyza on 5/16/16.
 */


@RunWith(RobolectricTestRunner.class)
@Config(manifest="src/main/AndroidManifest.xml", sdk = 18) //sdk level may change
public class TableFoodTest {

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

    @Test
    public void TableFoodTest(){
        getFoodName("_icxSHw7vkV7gbd-Oi2XWQ");
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

        Location loc = foodItemReceived.getLocation();
        System.out.println("restaurant name: " + loc.getName());
        System.out.println("restaurant postal code: " + loc.getPostal_code());
        System.out.println("restaurant state: " + loc.getState());
    }


}