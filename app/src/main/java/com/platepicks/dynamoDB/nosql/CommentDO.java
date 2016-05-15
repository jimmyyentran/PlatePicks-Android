package com.platepicks.dynamoDB.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "foodtinder-mobilehub-761050320-comment")

public class CommentDO {
    private String _userId;
    private String _foodId;
    private String _content;
    private double _rating;
    private String _subject;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }
    @DynamoDBRangeKey(attributeName = "foodId")
    @DynamoDBAttribute(attributeName = "foodId")
    public String getFoodId() {
        return _foodId;
    }

    public void setFoodId(final String _foodId) {
        this._foodId = _foodId;
    }
    @DynamoDBAttribute(attributeName = "content")
    public String getContent() {
        return _content;
    }

    public void setContent(final String _content) {
        this._content = _content;
    }
    @DynamoDBAttribute(attributeName = "rating")
    public double getRating() {
        return _rating;
    }

    public void setRating(final double _rating) {
        this._rating = _rating;
    }
    @DynamoDBAttribute(attributeName = "subject")
    public String getSubject() {
        return _subject;
    }

    public void setSubject(final String _subject) {
        this._subject = _subject;
    }

}
