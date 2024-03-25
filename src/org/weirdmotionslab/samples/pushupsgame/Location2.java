package org.weirdmotionslab.samples.pushupsgame;

import java.util.HashMap;
import java.util.Map;

public class Location2 {
    public String name = "";
    public HashMap<Item, Float> drop = new HashMap<>();
    public HashMap<Item, Integer> requiredItems = new HashMap<>();
    public HashMap<Item, Integer> requiredMaterials = new HashMap<>();
    public String serialize(){
        return name+":"+Util.mapFloatSerialize(drop)+":"+Util.mapIntSerialize(requiredItems)+":"+Util.mapIntSerialize(requiredMaterials);
    }
}
