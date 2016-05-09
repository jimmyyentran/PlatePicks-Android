package com.platepicks;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by elizamae on 4/29/16.
 */
public class ListItemClass implements Parcelable {
    private String foodName;
    private String restaurantName;
    private String restaurantAddress;
    private int clicked = 0;

    public String getFoodName() {
        return foodName;
    }
    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }
    public String getRestaurantName() {
        return restaurantName;
    }
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    public String getRestaurantAddress()
    {
        return restaurantAddress;
    }
    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }
    public boolean isClicked() {
        if (clicked == 1)
            return true;
        else return false;
    }
    public void setClicked(int n) {
        this.clicked = n;
    }

    public static final Parcelable.Creator<ListItemClass> CREATOR = new Parcelable.Creator<ListItemClass>() {
        public ListItemClass createFromParcel(Parcel source) {
            ListItemClass mItem = new ListItemClass();
            mItem.foodName = source.readString();
            mItem.restaurantName = source.readString();
            mItem.restaurantAddress = source.readString();
            mItem.clicked = source.readInt();
            return mItem;
        }
        public ListItemClass[] newArray(int size) {
            return new ListItemClass[size];
        }
    };

    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(foodName);
        parcel.writeString(restaurantName);
        parcel.writeString(restaurantAddress);
        parcel.writeInt(clicked);
    }
}
