package org.weirdmotionslab.samples.pushupsgame;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util{
    static List<Integer> nums = new ArrayList<>();
    public static int rand(int min, int max) {
        return min + (int)(Math.random() * (max - min));
    }

    public static int nextRandomNumber(){
        int nextRand = 0;
        do {
            nextRand = rand(1000,9999);
        } while (nums.contains(nextRand));
        nums.add(nextRand);
        return nextRand;
    }
    public static String nextRandomName(){
        return String.valueOf(nextRandomNumber());
    }

    public static List<Item> choose(List<Item> list, int num) {
        Collections.shuffle(list);
        return list.subList(0,num);
    }

    public static String mapIntSerialize(HashMap<Item,Integer> map) {
        String result = "";
        int i = 0;
        for (Map.Entry<Item,Integer> entry: map.entrySet()) {
            Item item = entry.getKey();
            int amount = entry.getValue();
            result+=item.name+"="+amount;
            if (i < map.entrySet().size() - 1) {
                result+=",";
            }
            i++;
        }
        return result;
    }

    public static String mapFloatSerialize(HashMap<Item,Float> map) {
        String result = "";
        int i = 0;
        for (Map.Entry<Item,Float> entry: map.entrySet()) {
            Item item = entry.getKey();
            float amount = entry.getValue();
            result+=item.name+"="+amount;
            if (i < map.entrySet().size() - 1) {
                result+=",";
            }
            i++;
        }
        return result;
    }

    public static String listItemSerialize(List<Item> list) {
        String result = "";
        int i = 0;
        for (Item item: list) {
            result+=item.serialize();
            if (i < list.size() - 1) {
                result+=";";
            }
            i++;
        }
        return result;
    }

    public static String listLocationSerialize(List<Location> list) {
        String result = "";
        int i = 0;
        for (Location loc: list) {
            result+=loc.serialize();
            if (i < list.size() - 1) {
                result+=";";
            }
            i++;
        }
        return result;
    }

    public static String listSuperLocationSerialize(List<Location2> list) {
        String result = "";
        int i = 0;
        for (Location2 loc: list) {
            result+=loc.serialize();
            if (i < list.size() - 1) {
                result+=";";
            }
            i++;
        }
        return result;
    }


    public static HashMap<Item, Integer> inventoryDeserialize(String str, List<Item> ref) {
        HashMap<Item, Integer> map = new HashMap<>();
        if (str.isEmpty()) {
            return map;
        }
        String[] mapEntries = str.split(",");
        for (int j=0; j< mapEntries.length; j++) {
            String[] entry = mapEntries[j].split("=");
            String invItemName = entry[0];
            int invItemAmount = Integer.parseInt(entry[1]);
            for (Item invItem: ref) {
                if (invItem.name.equals(invItemName)) {
                    map.put(invItem, invItemAmount);
                }
            }
        }
        return map;
    }

    public static List<Location> listLocationDeserialize(String str, List<Item> ref) {
        List<Location> result = new ArrayList<>();
        String[] locations = str.split(";");
        for (int i=0; i<locations.length; i++) {
            String[] params = locations[i].split(":");
            Location loc = new Location();
            loc.name = params[0];
            HashMap<Item, Float> map = new HashMap<>();
            String mapString = params[1];
            String[] mapEntries = mapString.split(",");
            for (int j=0; j< mapEntries.length; j++) {
                String[] entry = mapEntries[j].split("=");
                String dropItemName = entry[0];
                float dropItemAmount = Float.parseFloat(entry[1]);
                for (Item dropItem: ref) {
                    if (dropItem.name.equals(dropItemName)) {
                        map.put(dropItem, dropItemAmount);
                    }
                }
            }
            loc.drop = map;
            result.add(loc);
        }
        return result;
    }

    public static List<Location2> listSuperLocationDeserialize(String str, List<Item> ref) {
        List<Location2> result = new ArrayList<>();
        if (str.isEmpty()) {
            return result;
        }
        String[] locations = str.split(";");
        for (int i=0; i<locations.length; i++) {
            String[] params = locations[i].split(":");
            Location2 loc = new Location2();
            loc.name = params[0];

            HashMap<Item, Float> map = new HashMap<>();
            String mapString = params[1];
            String[] mapEntries = mapString.split(",");
            for (int j=0; j< mapEntries.length; j++) {
                String[] entry = mapEntries[j].split("=");
                String dropItemName = entry[0];
                float dropItemAmount = Float.parseFloat(entry[1]);
                for (Item dropItem: ref) {
                    if (dropItem.name.equals(dropItemName)) {
                        map.put(dropItem, dropItemAmount);
                    }
                }
            }
            loc.drop = map;

            HashMap<Item, Integer> map2 = new HashMap<>();
            String mapString2 = params[2];
            String[] mapEntries2 = mapString2.split(",");
            for (int j=0; j< mapEntries2.length; j++) {
                String[] entry = mapEntries2[j].split("=");
                String dropItemName = entry[0];
                int dropItemAmount = Integer.parseInt(entry[1]);
                for (Item dropItem: ref) {
                    if (dropItem.name.equals(dropItemName)) {
                        map2.put(dropItem, dropItemAmount);
                    }
                }
            }
            loc.requiredItems = map2;

            HashMap<Item, Integer> map3 = new HashMap<>();
            String mapString3 = params[3];
            String[] mapEntries3 = mapString3.split(",");
            for (int j=0; j< mapEntries3.length; j++) {
                String[] entry = mapEntries3[j].split("=");
                String dropItemName = entry[0];
                int dropItemAmount = Integer.parseInt(entry[1]);
                for (Item dropItem: ref) {
                    if (dropItem.name.equals(dropItemName)) {
                        map3.put(dropItem, dropItemAmount);
                    }
                }
            }
            loc.requiredMaterials = map3;

            result.add(loc);
        }
        return result;
    }

    public static List<Item> listItemDeserialize(String str, List<Item> ref) {
        List<Item> result = new ArrayList<>();
        if (str.isEmpty()) {
            return result;
        }
        String[] items = str.split(";");
        for (int i=0; i<items.length; i++) {
            String[] params = items[i].split(":");
            if (params.length == 3) {
                int type = Integer.parseInt(params[1]);
                if (type == 2) {
                    Item material = new Item();
                    material.name = params[0];
                    material.type = 2;
                    material.state = 1;
                    result.add(material);
                } else if (type == 3) {
                    Item superItem = new Item();
                    superItem.name = params[0];
                    superItem.type = 3;
                    superItem.state = Integer.parseInt(params[2]);
                    result.add(superItem);
                }
            } else {             //Big or small item
                Item item = new Item();
                item.name = params[0];
                item.type = Integer.parseInt(params[1]);
                item.state = Integer.parseInt(params[2]);
                HashMap<Item, Integer> map = new HashMap<>();
                String mapString = params[3];
                String[] mapEntries = mapString.split(",");
                for (int j=0; j< mapEntries.length; j++) {
                    String[] entry = mapEntries[j].split("=");
                    String reqItemName = entry[0];
                    int reqItemAmount = Integer.parseInt(entry[1]);
                    for (Item reqItem: ref) {
                        if (reqItem.name.equals(reqItemName)) {
                            map.put(reqItem, reqItemAmount);
                        }
                    }
                }
                item.requiredItems = map;
                result.add(item);
            }
        }
        return result;
    }
}