package com.mysampleapp.demo.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "foodtinder-mobilehub-761050320-food")

public class FoodDO {
    private String _foodId;
    private String _restaurantId;
    private double _dislike;
    private double _like;
    private String _name;
    private String _rating;

    @DynamoDBHashKey(attributeName = "foodId")
    @DynamoDBAttribute(attributeName = "foodId")
    public String getFoodId() {
        return _foodId;
    }

    public void setFoodId(final String _foodId) {
        this._foodId = _foodId;
    }
    @DynamoDBRangeKey(attributeName = "restaurantId")
    @DynamoDBAttribute(attributeName = "restaurantId")
    public String getRestaurantId() {
        return _restaurantId;
    }

    public void setRestaurantId(final String _restaurantId) {
        this._restaurantId = _restaurantId;
    }
    @DynamoDBAttribute(attributeName = "dislike")
    public double getDislike() {
        return _dislike;
    }

    public void setDislike(final double _dislike) {
        this._dislike = _dislike;
    }
    @DynamoDBAttribute(attributeName = "like")
    public double getLike() {
        return _like;
    }

    public void setLike(final double _like) {
        this._like = _like;
    }
    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return _name;
    }

    public void setName(final String _name) {
        this._name = _name;
    }
    @DynamoDBAttribute(attributeName = "rating")
    public String getRating() {
        return _rating;
    }

    public void setRating(final String _rating) {
        this._rating = _rating;
    }

}
