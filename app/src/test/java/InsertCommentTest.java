/**
 * Created by alyza on 5/15/16.
 */
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.platepicks.dynamoDB.nosql.FoodDO;
import com.platepicks.dynamoDB.nosql.CommentDO;
import com.platepicks.dynamoDB.nosql.ListDO;
import com.platepicks.dynamoDB.nosql.SampleDataGenerator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;

//import context.arch.comm.DataObject; //added

@RunWith(RobolectricTestRunner.class)
@Config(manifest="src/main/AndroidManifest.xml", sdk = 18) //sdk level may change
public class InsertCommentTest {

    private final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();;

    //Set up persistent activity
    @Before
    public void setUp() {
    }

    @Test
    public void DynamodbInsertToFoodTest() {
        // Start all test input as 'test' so it can be easily removed from table later
        insertSampleData("testUserId", "testFoodID");
    }

    public void insertSampleData(String userId, String foodId) throws AmazonClientException {
        System.out.println("Inserting Sample data");
        final CommentDO firstItem = new CommentDO();

        firstItem.setUserId(userId);
        firstItem.setFoodId(foodId);
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
}






