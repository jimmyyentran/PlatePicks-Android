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

@DynamoDBTable(tableName = "foodtinder-mobilehub-761050320-restaurant")

public class RestaurantDO {
    private String _restaurantId;
    private List<String> _address;
    private List<String> _categories;
    private String _city;
    private double _latitude;
    private double _longitude;
    private String _postalCode;
    private String _restaurantName;
    private String _state;

    @DynamoDBHashKey(attributeName = "restaurantId")
    @DynamoDBAttribute(attributeName = "restaurantId")
    public String getRestaurantId() {
        return _restaurantId;
    }

    public void setRestaurantId(final String _restaurantId) {
        this._restaurantId = _restaurantId;
    }
    @DynamoDBAttribute(attributeName = "address")
    public List<String> getAddress() {
        return _address;
    }

    public void setAddress(final List<String> _address) {
        this._address = _address;
    }
    @DynamoDBAttribute(attributeName = "categories")
    public List<String> getCategories() {
        return _categories;
    }

    public void setCategories(final List<String> _categories) {
        this._categories = _categories;
    }
    @DynamoDBAttribute(attributeName = "city")
    public String getCity() {
        return _city;
    }

    public void setCity(final String _city) {
        this._city = _city;
    }
    @DynamoDBAttribute(attributeName = "latitude")
    public double getLatitude() {
        return _latitude;
    }

    public void setLatitude(final double _latitude) {
        this._latitude = _latitude;
    }
    @DynamoDBAttribute(attributeName = "longitude")
    public double getLongitude() {
        return _longitude;
    }

    public void setLongitude(final double _longitude) {
        this._longitude = _longitude;
    }
    @DynamoDBAttribute(attributeName = "postal_code")
    public String getPostalCode() {
        return _postalCode;
    }

    public void setPostalCode(final String _postalCode) {
        this._postalCode = _postalCode;
    }
    @DynamoDBAttribute(attributeName = "restaurant_name")
    public String getRestaurantName() {
        return _restaurantName;
    }

    public void setRestaurantName(final String _restaurantName) {
        this._restaurantName = _restaurantName;
    }
    @DynamoDBAttribute(attributeName = "state")
    public String getState() {
        return _state;
    }

    public void setState(final String _state) {
        this._state = _state;
    }

}
