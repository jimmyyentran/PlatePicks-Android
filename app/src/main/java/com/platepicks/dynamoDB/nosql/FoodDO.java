package com.platepicks.dynamoDB.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "foodtinder-mobilehub-761050320-food")

public class FoodDO {
    private String _foodId;
    private String _restaurantId;
    private double _dislike;
    private double _like;

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
    public String getBusinessId() {
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

}
