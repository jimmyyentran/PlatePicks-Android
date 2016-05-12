package com.tinderui;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by elizamae on 4/29/16.
 */
public class ListItemClass implements Parcelable {

    // members
    private String foodName;
    private String restaurantName;
    private String restaurantAddress;
    private int clicked = 0;

    // member functions
    public String getFoodName() {return foodName;}
    public void setFoodName(String foodName) {this.foodName = foodName;}
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
    public void setRestaurantAddress(String restaurantAddress) {this.restaurantAddress = restaurantAddress;}
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
            mItem.setFoodName(source.readString());
            mItem.setRestaurantName(source.readString());
            mItem.setRestaurantAddress(source.readString());
            mItem.setClicked(source.readInt());
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
        parcel.writeString(this.getFoodName());
        parcel.writeString(this.getRestaurantAddress());
        parcel.writeString(this.getRestaurantAddress());
        parcel.writeInt(clicked);

    }
}
