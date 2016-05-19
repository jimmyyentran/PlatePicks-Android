package com.platepicks;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.TableLayout;

/**
 * Created by elizamae on 4/29/16.
 */
public class ListItemClass implements Parcelable {

    // members
    private String foodId;
    private String foodName;
    private String restaurantName;
    private String restaurantAddress;
    private String imageUrl;
    private TableLayout pageComments;
    private int clicked = 0;
    private boolean downloaded = false; // Daniel: don't need to save this in parcelable

    // member functions
    public String getFoodId() {return foodId;}
    public void setFoodId(String foodId) {this.foodId = foodId;}
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
    public String getImageUrl() {return imageUrl;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}

    public boolean isClicked() {
        if (clicked == 1) // true
            return true;
        else return false;
    }
    public void setClicked(int n) {
        this.clicked = n;
    }
    public boolean isDownloaded() {return downloaded;}
    public void setDownloaded(boolean downloaded) {this.downloaded = downloaded;}

    public static final Parcelable.Creator<ListItemClass> CREATOR = new Parcelable.Creator<ListItemClass>() {
        public ListItemClass createFromParcel(Parcel source) {
            ListItemClass mItem = new ListItemClass();
            mItem.setFoodId(source.readString());
            mItem.setFoodName(source.readString());
            mItem.setRestaurantName(source.readString());
            mItem.setRestaurantAddress(source.readString());
            mItem.setImageUrl(source.readString());
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
        parcel.writeString(this.getFoodId());
        parcel.writeString(this.getFoodName());
        parcel.writeString(this.getRestaurantName());
        parcel.writeString(this.getRestaurantAddress());
        parcel.writeString(this.getImageUrl());
        parcel.writeInt(clicked);

    }
}
