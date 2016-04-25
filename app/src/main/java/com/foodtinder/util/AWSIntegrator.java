package com.foodtinder.util;

/**
 * Created by jimmytran on 4/25/16.
 */
public class AWSIntegrator {

    /**
     * This static class can be called anywhere. Currently it's return type is void but we would
     * want it to return a json object
     * @param functionName name of the function to call on AWS
     * @param requestPayload json to be passed into the called function, it's ok
     *                       to take in a string for now, but in the future it will take in a json
     *                       object.
     * @return return json object
     */
    public static void call(String functionName, String requestPayload){

    }
}
