/**
 * Created by alyza on 5/15/16.
 */

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.platepicks.objects.Location;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static com.platepicks.dynamoDB.TableRestaurant.insertRestaurant;

//import context.arch.comm.DataObject; //added

@RunWith(RobolectricTestRunner.class)
@Config(manifest="src/main/AndroidManifest.xml", sdk = 18) //sdk level may change
public class TableRestaurantTest {

    private final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();;

    @Test
    public void insertRestaurantTest(){
        List<String> addressList = new ArrayList<String>();
        addressList.add("addressTest");
        List<String> categoryList = new ArrayList<String>();
        categoryList.add("categoryTest");
        insertRestaurant(new Location( "city", "restaurantname", 99999, "CA", addressList,
                "restaurantId", 88.88, 99.99, categoryList));
    }
}






