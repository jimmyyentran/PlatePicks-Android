import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.platepicks.objects.FoodReceive;
import com.platepicks.objects.FoodRequest;
import com.platepicks.util.AWSIntegratorAsyncTask;
import com.platepicks.util.AWSIntegratorInterface;
import com.platepicks.util.ConvertToObject;

import java.util.List; //added
import java.util.ArrayList; //added
import java.lang.Object; //added
//import context.arch.comm.DataObject; //added
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest="src/main/AndroidManifest.xml", sdk = 18) //sdk level may change
public class AWSIntegratorTest {

    private TestActivity activity;

    //Set up persistent activity
    @Before
    public void setUp(){
//        activity  = new TestActivity();
    }

    @Test
    public void HelloWorldTest(){
        AWSIntegratorAsyncTask asyncTask = new AWSIntegratorAsyncTask();
        TestActivity activity  = new TestActivity();
        DataObject singleObject = new DataObject("value1", "value2", "value3");

        //Convert the object to a JSON string
//        String json = new Gson().toJson(singleObject).toString();
//        System.out.println(json);

        asyncTask.execute("hello-world", singleObject, activity);
        Robolectric.flushBackgroundThreadScheduler();
        String HelloWorldResult = activity.returnResults();
//        System.out.println("helloOutput: " + HelloWorldResult);

        Type type = new TypeToken<List<DataObject>>(){}.getType();
        List<DataObject> inpList = new Gson().fromJson(HelloWorldResult, type);
        for(int i = 0; i < inpList.size(); i++){
            DataObject x = inpList.get(i);
            x.printDataObject();
        }
    }

    @Test
    public void FoodRequestTest(){
        String category = "tradamerican,newamerican," +
                "chinese," +
                "mexican," +
                "japanese," +
                "italian," +
                "vietnamese," +
                "thai," +
                "indpak," +
                "mediterranean," +
                "korean";

        String category2 = "french," +
                "pizza," +
                "seafood," +
                "dimsum," +
                "asianfusion," +
                "sandwiches," +
                "burgers," +
                "hotdogs," +
                "coffee," +
                "breakfast_brunch";

        AWSIntegratorAsyncTask asyncTask = new AWSIntegratorAsyncTask();
        TestActivity activity  = new TestActivity();
        FoodRequest req = new FoodRequest("asian", 1, "33.7175, -117.8311", 2, 40000, category + "," + category2, 1, 0);
        asyncTask.execute("yelpApi", req, activity);
        Robolectric.flushBackgroundThreadScheduler();
        List<FoodReceive> foodReceives = ConvertToObject.toFoodReceiveList(activity.returnResults());
        System.out.println("Id: " + foodReceives.get(0).getLocation().getRestaurantId());
        System.out.println("Name: " + foodReceives.get(0).getLocation().getRestaurantName());
        System.out.println("Category 1: " + foodReceives.get(0).getLocation().getCategory().get(0));
        System.out.println("Longitude: " + foodReceives.get(0).getLocation().getLongitude());
        assertThat(foodReceives.isEmpty(), is(false));
    }


//    @Test
//    public void HelloWorldTest(){
//        AWSIntegratorAsyncTask asyncTask = new AWSIntegratorAsyncTask();
//        TestActivity activity  = new TestActivity();
//        asyncTask.execute("hello-world", "{\n  \"key1\" : \"value1\",\n  \"key2\" : \"value2\",\n  \"key3\" : \"value3\"\n}", activity);
//        Robolectric.flushBackgroundThreadScheduler();
//        assertEquals(activity.returnResults(), "\"value1\"");
//    }



//    @Test
//    public void YelpAPITest(){
//        AWSIntegratorAsyncTask asyncTask = new AWSIntegratorAsyncTask();
//        TestActivity activity =  new TestActivity();
//    //    System.out.println("{\n  \"coordinates\": { \n    \"longitude\": 23.123,\n    \"latitude\": 33.444\n  },\n  \"radius_filter\": 10000\n}");
//        asyncTask.execute("hello-world-python", "{\n  \"coordinates\": { \n    \"longitude\": 23.123,\n    \"latitude\": 33.444\n  },\n  \"radius_filter\": 10000\n}", activity);
//        Robolectric.flushBackgroundThreadScheduler();
//        assertEquals(activity.returnResults(), "\"value1\"");
//    }


//    @Test
//    public void TestPythonTest(){
//        AWSIntegratorAsyncTask asyncTask = new AWSIntegratorAsyncTask();
//        TestActivity activity =  new TestActivity();
////        System.out.println("{\n  \"coordinates\": { \n    \"longitude\": 23.123,\n    \"latitude\": 33.444\n  },\n  \"radius_filter\": 10000\n}");
//        asyncTask.execute("hello-world-python", "{\n  \"coordinates\": { \n    \"longitude\": 23.123,\n    \"latitude\": 33.444\n  },\n  \"radius_filter\": 10000\n}", activity);
//        Robolectric.flushBackgroundThreadScheduler();
//        assertEquals(activity.returnResults(), "\"value1\"");
//    }

}

//class TestActivity implements AWSIntegratorInterface {
//    Type type = new TypeToken<List<DataObject>>(){}.getType();

//    @Override
//    public void doSomethingWithResults(String ob) {
//        List<DataObject> inpList = new Gson().fromJson(ob, type);
//        for(int i=0; i<inpList.size(); i++){
//            DataObject x = inpList.get(i);
//        }
//    }

//    public String returnResults() {
//        return str;
//    }
//}
    class TestActivity implements AWSIntegratorInterface {
        String str;
        @Override
        public void doSomethingWithResults(String ob) {
            str = ob;
        }

    @Override
    public void doSomethingOnAWSError() {

    }

    public String returnResults() {
            return str;
        }
}






