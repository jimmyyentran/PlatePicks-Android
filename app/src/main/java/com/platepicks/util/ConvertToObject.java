package com.platepicks.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.platepicks.objects.FoodReceive;
import com.platepicks.objects.FoodRequest;
import com.platepicks.ListItemClass;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jimmytran on 5/11/16.
 */
public class ConvertToObject {

    public static List<FoodReceive> toFoodReceiveList(String json){
        Log.d("ConvertToObject", " ");
        Type foodReceiveCollection = new TypeToken<Collection<FoodReceive>>(){}.getType();
        Collection<FoodReceive> receivedCollection = new Gson().fromJson(json, foodReceiveCollection);
        FoodReceive[] foodReceiveArray = receivedCollection.toArray(new FoodReceive[receivedCollection.size()]);
        return Arrays.asList(foodReceiveArray);
    }

    public static List<ListItemClass> toListItemClassList(List<FoodReceive> foodReceiveList) {
        ArrayList<ListItemClass> convertedObjects = new ArrayList<>();

        for (FoodReceive fr : foodReceiveList) {
            ListItemClass item = new ListItemClass();

            item.setFoodId(fr.getFood_id());
            item.setFoodName(fr.getName());
            item.setRestaurantName(fr.getLocation().getName());
            item.setRestaurantAddress(fr.getLocation().getAddressString());
            item.setImageUrl(fr.getUrl().toString());

            convertedObjects.add(item);
        }

        return convertedObjects;
    }
}
