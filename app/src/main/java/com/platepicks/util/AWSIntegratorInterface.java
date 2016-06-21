package com.platepicks.util;

/**
 * Created by jimmytran on 4/28/16.
 */
public interface AWSIntegratorInterface {
    void doSomethingWithResults(String ob);
    void doSomethingOnAWSError();
//    public String returnResults(); //used for testing
}
