package com.platepicks.dynamoDB;

import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.platepicks.dynamoDB.nosql.CommentDO;
import com.platepicks.objects.FoodReceive;
import com.platepicks.objects.Location;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by alyza on 5/17/16.
 */
public class TableComment {

    private final static String LOG_TAG = TableComment.class.getSimpleName();


    public static void insertComment(String userId, String foodId, String content) throws AmazonClientException {
        System.out.println("Inserting comment");

        long time = System.currentTimeMillis();
        String s = String.valueOf(time/1000);
//        System.out.println("time: " + time);

        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        final CommentDO firstItem = new CommentDO();

        firstItem.setUserId(userId);
        firstItem.setFoodId(foodId);
        firstItem.setContent(content);
        firstItem.setTime(time);
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
    }

    /**
     * print data members of CommentDO object
     * @param comment The object to be printed
     */
    public static void printComment(CommentDO comment){
        String user = comment.getUserId();
        String food = comment.getFoodId();
        double rating = comment.getRating();
        String content = comment.getContent();
        long time = comment.getTime();
        String s_time = String.valueOf(time/1000);
        String r_time = String.valueOf(rating);

        System.out.println("user id: " + user);
        System.out.println("food id: " + food);
        System.out.println("rating: " + r_time);
        System.out.println("content: " + content);
        System.out.println("time: " + getLocalTime(time));
        System.out.println();
    }

    /**
     * prints each element of list
     * @param list The list of CommentDO objects to be printed
     */
    public static void printCommentList(List<CommentDO> list){
        for(CommentDO comment : list){
            printComment(comment);
        }

    }

    public static List<CommentDO> getCommentsFromFoodID(String foodId) {
        Log.d(LOG_TAG, "Getting the comments from foodId");

        PaginatedScanList<CommentDO> results;
        Iterator<CommentDO> resultsIterator;

        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

        // These hold attribute actual names
        final Map<String, String> filterExpressionAttributeNames = new HashMap<>();
        filterExpressionAttributeNames.put("#key", "foodId");

        // These hold attributes values
        final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
        filterExpressionAttributeValues.put(":value",
                new AttributeValue().withS(foodId));

        // Setup Scan
        final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("#key = :value")
                .withExpressionAttributeNames(filterExpressionAttributeNames)
                .withExpressionAttributeValues(filterExpressionAttributeValues);

        // Execute Scan
        results = mapper.scan(CommentDO.class, scanExpression);

        return results;
    }

    public static List<CommentDO> getCommentsFromUserID(String userId) {
        Log.d(LOG_TAG, "Getting the comments from userId");

        PaginatedScanList<CommentDO> results;
        Iterator<CommentDO> resultsIterator;

        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

        // These hold attribute actual names
        final Map<String, String> filterExpressionAttributeNames = new HashMap<>();
        filterExpressionAttributeNames.put("#key", "userId");

        // These hold attributes values
        final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
        filterExpressionAttributeValues.put(":value",
                new AttributeValue().withS(userId));

        // Setup Scan
        final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("#key = :value")
                .withExpressionAttributeNames(filterExpressionAttributeNames)
                .withExpressionAttributeValues(filterExpressionAttributeValues);

        // Execute Scan
        results = mapper.scan(CommentDO.class, scanExpression);
        return results;
    }

    public static String getLocalTime(long epochTime){
        Date date = new Date(epochTime);
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        String formatted = format.format(date);
        return formatted;
    }

    public static Date getDateObject(long epochTime){
        Date date = new Date(epochTime);
        return date;
    }
}
