package com.platepicks.objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

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
    private FoodReceive original;       // Daniel: reference for Liked/Disliked database request
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
        // true if 1
        return clicked == 1;
    }
    public void setClicked(int n) {
        this.clicked = n;
    }
    public boolean isDownloaded() {return downloaded;}
    public void setDownloaded(boolean downloaded) {this.downloaded = downloaded;}
    public FoodReceive getOriginal() {return original;}
    public void setOriginal(FoodReceive original) {this.original = original;}

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

    public String getFileString() {
        int isClicked = this.isClicked() ? 1 : 0;

        return this.getFoodId() + "::" +
                this.getFoodName() + "::" +
                this.getRestaurantName() + "::" +
                this.getRestaurantAddress() + "::" +
                this.getImageUrl() + "::" +
                String.valueOf(isClicked);
    }

    static public ListItemClass createFrom(String fileString) {
        String[] members = fileString.split("::");
        ListItemClass newItem = new ListItemClass();

        if (members.length == 6) {
            newItem.setFoodId(members[0]);
            newItem.setFoodName(members[1]);
            newItem.setRestaurantName(members[2]);
            newItem.setRestaurantAddress(members[3]);
            newItem.setImageUrl(members[4]);
            newItem.setClicked(Integer.decode(members[5]));
        } else {
            try {
                throw new Exception("Error reading file");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return newItem;
    }

    public String getTinyImageUrl() {
        String url = imageUrl.substring(0, imageUrl.length() - 5) + "258s.jpg";
        Log.d("ListItemClass", url);
        return url;
    }
}
