import com.tinderui.util.AWSIntegratorAsyncTask;
import com.tinderui.util.AWSIntegratorInterface;

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
        asyncTask.execute("hello-world", "{\n  \"key1\" : \"value1\",\n  \"key2\" : \"value2\",\n  \"key3\" : \"value3\"\n}", activity);
        Robolectric.flushBackgroundThreadScheduler();
        assertEquals(activity.returnResults(), "\"value1\"");
    }

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
