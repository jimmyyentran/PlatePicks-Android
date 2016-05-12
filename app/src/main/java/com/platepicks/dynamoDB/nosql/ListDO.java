package com.platepicks.dynamoDB.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "foodtinder-mobilehub-761050320-list")

public class ListDO {
    private String _userId;
    private String _foodId;
    private double _creationDate;
    private byte[] _type;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBIndexHashKey(attributeName = "userId", globalSecondaryIndexName = "DateSorted")
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
    @DynamoDBIndexRangeKey(attributeName = "creationDate", globalSecondaryIndexName = "DateSorted")
    public double getCreationDate() {
        return _creationDate;
    }

    public void setCreationDate(final double _creationDate) {
        this._creationDate = _creationDate;
    }
    @DynamoDBAttribute(attributeName = "type")
    public byte[] getType() {
        return _type;
    }

    public void setType(final byte[] _type) {
        this._type = _type;
    }

}
