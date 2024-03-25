package org.weirdmotionslab.samples.pushupsgame;

import java.util.HashMap;
import java.util.Map;

public class Location {
    public String name = "";
    public HashMap<Item, Float> drop = new HashMap<>();

    public String serialize(){
        return name+":"+Util.mapFloatSerialize(drop);
    }
}
