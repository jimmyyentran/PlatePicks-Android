package com.platepicks.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.platepicks.objects.FoodReceive;
import com.platepicks.objects.FoodRequest;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by jimmytran on 5/11/16.
 */
public class ConvertToObject {

    public static List<FoodReceive> toFoodReceiveList(String json){
        Type foodReceiveCollection = new TypeToken<Collection<FoodReceive>>(){}.getType();
        Collection<FoodReceive> receivedCollection = new Gson().fromJson(json, foodReceiveCollection);
        FoodReceive[] foodReceiveArray = receivedCollection.toArray(new FoodReceive[receivedCollection.size()]);
        return Arrays.asList(foodReceiveArray);
    }
}
