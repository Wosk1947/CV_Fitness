package org.weirdmotionslab.samples.pushupsgame;

import java.util.HashMap;
import java.util.Map;

public class Item {
    public String name = "";
    public int type = 0;
    public int state = 0; //0 - blueprint, 1 - finished item
    public HashMap<Item,Integer> requiredItems = new HashMap<>();

    public String serialize(){
        return name+":"+type+":"+state+":"+Util.mapIntSerialize(requiredItems);
    }

    public void deserialize(String str){

    }
}
