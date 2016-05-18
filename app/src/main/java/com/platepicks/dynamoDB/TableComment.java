package com.platepicks.dynamoDB;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.platepicks.dynamoDB.nosql.CommentDO;
import com.platepicks.objects.FoodReceive;
import com.platepicks.objects.Location;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alyza on 5/17/16.
 */
public class TableComment extends AsyncTask<String, Void, Void> {

    public static void insertComment(String userId, String foodId, String content) throws AmazonClientException {
        System.out.println("Inserting comment");
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        final CommentDO firstItem = new CommentDO();

        firstItem.setUserId(userId);
        firstItem.setFoodId(foodId);
        firstItem.setContent(content);
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

        System.out.println("Insert comment successful");

//        CommentDO CommentToGet;
//        CommentToGet = mapper.load(CommentDO.class, userId);
//        System.out.println("Got comment: " + CommentToGet.getContent());

    }

    @Override
    protected Void doInBackground(String... params) {
        insertComment(params[0], params[1], params[2]);
        return null;
    }
}
