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

import static com.platepicks.dynamoDB.TableComment.getCommentsFromFoodID;
import static com.platepicks.dynamoDB.TableComment.getCommentsFromUserID;
import static com.platepicks.dynamoDB.TableComment.insertComment;
import static com.platepicks.dynamoDB.TableComment.printCommentList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;

@RunWith(RobolectricTestRunner.class)
@Config(manifest="src/main/AndroidManifest.xml", sdk = 18) //sdk level may change
public class TableCommentTest {

    private final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();;

    //Set up persistent activity
    @Before
    public void setUp() {
    }

    //@Test
    public void DynamodbInsertToFoodTest() {
        // Start all test input as 'test' so it can be easily removed from table later
        insertComment("testUserId1", "testFoodID1", "testCommentContent1");
    }

    @Test
    public void getCommentsFromFoodIDTest(){
        printCommentList(getCommentsFromFoodID("6kNuxODLj-y9XNRACvtzqg"));
//        printCommentList(getCommentsFromUserID("BurritoLover"));
        printCommentList(getCommentsFromUserID("testUserId1"));

    }


}






