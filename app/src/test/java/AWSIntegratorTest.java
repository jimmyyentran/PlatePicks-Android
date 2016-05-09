import android.provider.ContactsContract;

import com.google.gson.Gson;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.tinderui.util.AWSIntegratorAsyncTask;
import com.tinderui.util.AWSIntegratorInterface;
import com.tinderui.object.FoodRequest;


import com.platepicks.objects.FoodReturn;
import com.platepicks.util.AWSIntegratorAsyncTask;
import com.platepicks.util.AWSIntegratorInterface;
import com.platepicks.objects.FoodRequest;

//import context.arch.comm.DataObject; //added
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

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
//        initialize a list of type DataObject
//        List<DataObject> objList = new ArrayList<DataObject>();
        DataObject singleObject = new DataObject("value1", "value2", "value3");

        //Convert the object to a JSON string
        String json = new Gson().toJson(singleObject).toString();
        System.out.println(json);

        asyncTask.execute("hello-world", singleObject, activity);
//        asyncTask.execute("hello-world", "{\n  \"key1\" : \"value1\",\n  \"key2\" : \"value2\",\n  \"key3\" : \"value3\"\n}", activity);
//        asyncTask.execute("hello-world", "{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":\"value3\"}", activity);
//        System.out.println("{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":\"value3\"}");
        Robolectric.flushBackgroundThreadScheduler();
//        assertEquals(activity.returnResults(), "\"value1\"");
//        assertEquals("\"value1\"", "\"value1\"");
        String HelloWorldResult = activity.returnResults();
        System.out.println("helloOutput: " + HelloWorldResult);

        Type type = new TypeToken<List<DataObject>>(){}.getType();
        List<DataObject> inpList = new Gson().fromJson(HelloWorldResult, type);
        for(int i = 0; i < inpList.size(); i++){
            DataObject x = inpList.get(i);
            x.printDataObject();
        }
//        DataObject obj = new gson.fromJson(HelloWorldResult, type);
//        DataObject obj = new Gson().fromJson(HelloWorldResult, DataObject.class);
//        obj.printDataObject();

    }

//    @Test
//    public void FoodRequestTest(){
//        AWSIntegratorAsyncTask asyncTask = new AWSIntegratorAsyncTask();
//        TestActivity activity  = new TestActivity();
//
//        FoodRequest req = new FoodRequest("asian", 1, "33.7175, -117.8311", 2, 40000, "japanese", 1);
//        asyncTask.execute("yelpApi", req, activity);
//        Robolectric.flushBackgroundThreadScheduler();
//        System.out.print("foodRequest:" + activity.returnResults());
//
////        System.out.println("term: " + req.term);
////        System.out.println("business: " + req.food_per_business);
////        System.out.println("11: " + req.ll);
////        System.out.println("limit: " + req.limit);
////        System.out.println("radius_filter: " + req.radius_filter);
////        System.out.println("category_filter: " + req.category_filter);
////        System.out.println("sort: " + req.sort);
////        req.setlattitude(33.333);
////        System.out.println("setlattitude-11: " + req.ll);
////        req.setlongitude(44.444);
////        System.out.println("setlongitude-11: " + req.ll);
////        req.addCategory("Chinese");
////        System.out.println("category_filter: " + req.category_filter);
//
//    }


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

        public String returnResults() {
            return str;
        }
}






