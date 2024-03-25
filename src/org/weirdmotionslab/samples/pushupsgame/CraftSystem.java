package org.weirdmotionslab.samples.pushupsgame;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftSystem {
    static int superItemsNumber = 5;
    static int bigItemsNumber = 10;
    static int locationsNumber = 10;
    static int materialsNumber = 6;
    static int smallItemsInBigItem = 3;
    static int materialsInSmallItem = 3;
    static String bigItemPrefix = "Item";
    static String smallItemPrefix = "Part";
    static String materialPrefix = "Material";
    static String locationPrefix = "Location";
    static int materialRequiredNumber = 5;
    static float bigItemDrop = 0.25f;
    static float smallItemDrop = 0.4f;
    static float materialDrop = 0.9f;
    static int materialGain = 3;

    static int winScore = 20;
    static float speedMultiplier = 1;
    static int lastBattleStatus = -1;
    static Location lastBattleLocation;
    static Location2 lastBattleSuperLocation;

    static HashMap<Item, Integer> inventory = new HashMap<>();
    static List<Item> allBigItems = new ArrayList<>();
    static List<Item> allSmallItems = new ArrayList<>();
    static List<Item> allMaterials = new ArrayList<>();
    static List<Item> allItems = new ArrayList<>();
    static List<Location> allLocations = new ArrayList<>();
    static List<Location2> allSuperLocations = new ArrayList<>();
    static List<Item> allSuperItems = new ArrayList<>();

    static String itemName = "Motorarmor";
    static String superItemName = "Elite Motorarmor";
    static List<String> superItemPrefixes = Arrays.asList("Quantum","Gravitational","Antimatter","Dark-Matter","Dirak-Sea");
    static List<String> itemPrefixes = Arrays.asList("Titanium","Steel","Aluminium","Ceramic","Fiber",
                                                    "Electro-organic","Liquid-metal","Metamaterial","Stealth","Silicon");
    static List<String> partName = Arrays.asList("Servomotors","Neurosensors","Processing Unit");
    static List<String> materialName = Arrays.asList("Copper","Semiconductors","Nanotubes","PCBs","Wavequides","Chemicals");
    static List<String> locationName = Arrays.asList("Military Avanpost","Sewers","Motorarmor Factory","Orbital Station",
                                                        "Research Lab","Battleship Crashsite","Corporation Skyscraper","Subway",
                                                        "Moonbase", "Arctic Training Ground");
    static List<String> superLocationName = Arrays.asList("Interplanetary Teleporter","Blackhole Probe Ship","Alien Nexus","Earth Core Base",
            "Temporal Anomaly");

    public static void addSuperItems(){
        Collections.shuffle(superItemPrefixes);
        for (int i=0; i<superItemsNumber; i++){
            Item superItem = new Item();
            superItem.type = 3;
            superItem.state = 0;
            superItem.name = superItemPrefixes.get(i)+" "+superItemName;
            allSuperItems.add(superItem);
        }
    }
    public static void addSuperLocations(){
        Collections.shuffle(superLocationName);
        Collections.shuffle(allSuperItems);
        Collections.shuffle(allBigItems);
        for (int i=0; i<superItemsNumber; i++){
            Collections.shuffle(allMaterials);
            Location2 superLocation = new Location2();
            superLocation.name = superLocationName.get(i);
            superLocation.drop.put(allSuperItems.get(i), 0.7f);
            superLocation.requiredItems.put(allBigItems.get(i * 2), 1);
            superLocation.requiredItems.put(allBigItems.get(i * 2 + 1), 1);
            superLocation.requiredMaterials.put(allMaterials.get(0), 6);
            superLocation.requiredMaterials.put(allMaterials.get(1), 6);
            superLocation.requiredMaterials.put(allMaterials.get(2), 6);
            allSuperLocations.add(superLocation);
        }
    }
    public static void renameItems() {
        Collections.shuffle(itemPrefixes);
        Collections.shuffle(partName);
        Collections.shuffle(materialName);
        for (int i=0; i<allBigItems.size(); i++) {
            Item bigItem = allBigItems.get(i);
            String bigItemName = itemPrefixes.get(i) + " " + itemName;
            bigItem.name = bigItemName;
            int j=0;
            for (Item smallItem: bigItem.requiredItems.keySet()) {
                smallItem.name = partName.get(j) + " [" + bigItemName + "]";
                j++;
            }
        }
        for (int i=0; i<allMaterials.size(); i++) {
            Item material = allMaterials.get(i);
            material.name = materialName.get(i);
        }
        for (int i=0; i<allLocations.size(); i++) {
            Location location = allLocations.get(i);
            location.name = locationName.get(i);
        }
    }

    public static void updateLocationDrop(Location location) {
        List<Item> dropItems = new ArrayList<>();
        List<Float> probabilities = new ArrayList<>();
        for (Map.Entry<Item,Float> dropEntry: location.drop.entrySet()){
            Item item = dropEntry.getKey();
            Float p = dropEntry.getValue();
            if (item.type == 0 || item.type == 1) {
                dropItems.add(item);
                if (inventory.containsKey(item)) {
                    probabilities.add(0f);
                } else {
                    probabilities.add(p);
                }
            }
        }
        List<Float> newProbabilities = updateProbabilities(probabilities, 0.75f);
        for (int i = 0; i < dropItems.size(); i++) {
            location.drop.put(dropItems.get(i), newProbabilities.get(i));
        }
    }

    public static String processBattleResult() {
        if (lastBattleStatus == -1) {
            return "No messages";
        }
        if (lastBattleStatus == 0) {
            return "Battle Lost :(";
        }
        lastBattleStatus = -1;
        if (lastBattleSuperLocation != null) {
            String message = "You WON!!!! >:)\nFused:\n";
            Item superItem = null;
            for (Item item: lastBattleSuperLocation.drop.keySet()){
                superItem = item;
            }
            float p = lastBattleSuperLocation.drop.get(superItem);
            if (Math.random() < p) {
                message += superItem.name + ": 1\n";
                superItem.state = 1;
                inventory.put(superItem, 1);
            }
            lastBattleSuperLocation = null;
            return message;
        }
        String message = "You WON!!!! >:)\nFound:\n";

        List<Item> dropItems = new ArrayList<>();
        List<Item> dropMaterials = new ArrayList<>();
        for (Item dropItem: lastBattleLocation.drop.keySet()){
            if (dropItem.type == 2) {
                dropMaterials.add(dropItem);
            }
            if (dropItem.type == 0 || dropItem.type == 1) {
                dropItems.add(dropItem);
            }
        }
        for (Item item: dropItems) {
            float probability = lastBattleLocation.drop.get(item);
            double rand = Math.random();
            if (rand < probability) {
                if (!inventory.containsKey(item)) {
                    message += item.name + ": " + 1 + " \n";
                    inventory.put(item,1);
                    updateLocationDrop(lastBattleLocation);
                }
            }
        }
        for (Item material: dropMaterials) {
            float probability = lastBattleLocation.drop.get(material);
            double rand = Math.random();
            if (rand < probability) {
                int amount = inventory.getOrDefault(material, 0);
                int gain = Util.rand(1,materialGain);
                amount += gain;
                if (inventory.containsKey(material)) {
                    inventory.put(material,amount);
                } else {
                    inventory.put(material,gain);
                }
                message += material.name + ": " + gain + " \n";
            }
        }
        return message;
    }

    public static void printLocation(Location location) {
        String str = location.name + " : ";
        for (Map.Entry<Item,Float> dropEntry: location.drop.entrySet()){
            Item item = dropEntry.getKey();
            Float p = dropEntry.getValue();
            str += item.name + " " + p + " ; ";
        }
        System.out.println(str);
    }

    public static void buildItem(Item item) {
        switch (item.type) {
            case 0:
                item.state = 1;
                break;
            case 1:
                for (Item requiredMaterial: item.requiredItems.keySet()) {
                    int materialAmount = inventory.get(requiredMaterial);
                    int requiredAmount = item.requiredItems.get(requiredMaterial);
                    materialAmount -= requiredAmount;
                    inventory.put(requiredMaterial, materialAmount);
                }
                item.state = 1;
                break;
        }
    }


    public static void initSystem() {
        for (int i=0; i<materialsNumber; i++){
            Item material = new Item();
            material.type = 2;
            material.state = 1;
            material.name = materialPrefix+i;
            allMaterials.add(material);
        }

        //create items
        for (int i=0; i<bigItemsNumber; i++){
            Item bigItem = new Item();
            bigItem.type = 0;
            bigItem.name = bigItemPrefix+i;
            for (int j=0; j < smallItemsInBigItem; j++){
                Item smallItem = new Item();
                smallItem.type = 1;
                smallItem.name = smallItemPrefix+j+"_of_Item"+i;
                List<Item> materials = Util.choose(allMaterials,materialsInSmallItem);
                for (Item material:materials){
                    smallItem.requiredItems.put(material, Util.rand(materialRequiredNumber - 2, materialRequiredNumber+1));
                }
                allSmallItems.add(smallItem);
                bigItem.requiredItems.put(smallItem,1);
            }
            allBigItems.add(bigItem);
        }

        for (int i=0; i<locationsNumber; i++) {
            Location location = new Location();
            location.name = locationPrefix+i;
            allLocations.add(location);
        }

        Collections.shuffle(allLocations);
        int n = 0;
        for (int i=0; i<locationsNumber; i++) {
            Location location = allLocations.get(i);
            Item bigItem;
            if (n <= allBigItems.size() - 1) {
                bigItem = allBigItems.get(n);
            } else {
                break;
            }
            location.drop.put(bigItem, bigItemDrop);
            n++;
            if (i == locationsNumber - 1){
                i = -1;
            }
        }

        Collections.shuffle(allLocations);
        n = 0;
        for (int i=0; i<locationsNumber; i++) {
            Location location = allLocations.get(i);
            Item smallItem;
            if (n <= allSmallItems.size() - 1) {
                smallItem = allSmallItems.get(n);
            } else {
                break;
            }
            location.drop.put(smallItem, smallItemDrop);
            n++;
            if (i == locationsNumber - 1){
                i = -1;
            }
        }

        Collections.shuffle(allLocations);
        for (int i=0; i<locationsNumber; i++) {
            Location location = allLocations.get(i);
            for (Item material:allMaterials) {
                location.drop.put(material, materialDrop);
            }
        }

        allItems.addAll(allBigItems);
        allItems.addAll(allSmallItems);
        allItems.addAll(allMaterials);
    }

    public static void save(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean("cv_pushups_started", true);

        String materials = Util.listItemSerialize(allMaterials);
        String smallItems = Util.listItemSerialize(allSmallItems);
        String bigItems = Util.listItemSerialize(allBigItems);
        String superItems = Util.listItemSerialize(allSuperItems);
        String locations = Util.listLocationSerialize(allLocations);
        String superLocations = Util.listSuperLocationSerialize(allSuperLocations);
        String inventoryString = Util.mapIntSerialize(inventory);

        editor.putString("cv_pushups_materials", materials);
        editor.putString("cv_pushups_smallItems", smallItems);
        editor.putString("cv_pushups_bigItems", bigItems);
        editor.putString("cv_pushups_locations", locations);
        editor.putString("cv_pushups_inventoryString", inventoryString);
        editor.putString("cv_pushups_superItems", superItems);
        editor.putString("cv_pushups_superLocations", superLocations);
        editor.apply();
    }

    public static void load(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        allMaterials = Util.listItemDeserialize(sharedPref.getString("cv_pushups_materials", ""), new ArrayList<>());
        allSmallItems = Util.listItemDeserialize(sharedPref.getString("cv_pushups_smallItems", ""), allMaterials);
        allBigItems = Util.listItemDeserialize(sharedPref.getString("cv_pushups_bigItems", ""), allSmallItems);
        allSuperItems = Util.listItemDeserialize(sharedPref.getString("cv_pushups_superItems", ""), new ArrayList<>());
        allItems.addAll(allBigItems);
        allItems.addAll(allSmallItems);
        allItems.addAll(allMaterials);
        allItems.addAll(allSuperItems);
        allLocations = Util.listLocationDeserialize(sharedPref.getString("cv_pushups_locations", ""), allItems);
        allSuperLocations = Util.listSuperLocationDeserialize(sharedPref.getString("cv_pushups_superLocations", ""), allItems);
        inventory = Util.inventoryDeserialize(sharedPref.getString("cv_pushups_inventoryString", ""), allItems);
    }

    public static void checkIfNewGame(Activity activity){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        boolean gameExists = sharedPref.getBoolean("cv_pushups_started", false);
        if (!gameExists){
            initSystem();
            renameItems();
            addSuperItems();
            addSuperLocations();
            save(activity);
        } else {
            load(activity);
        }
    }

    public static List<Float> updateProbabilities(List<Float> old, float pAny){
        List<Float> newP = new ArrayList<>(old);
        boolean allZero = true;
        for (Float p: old) {
            if (p != 0) {
                allZero = false;
            }
        }
        if (allZero) {
            return newP;
        }
        while (checkPList(newP, pAny)) {
            updatePList(newP);
        }
        return newP;
    }

    public static boolean checkPList(List<Float> probabilities, float pAny) {
        float currentPany = 1;
        for (Float p: probabilities){
            currentPany *= (1-p);
        }
        currentPany = 1 - currentPany;
        if (currentPany < pAny) {
            return true;
        }
        return false;
    }

    public static void updatePList(List<Float> probabilities) {
        float multiplier = 1.1f;
        for (int i = 0; i<probabilities.size(); i++){
            probabilities.set(i, probabilities.get(i) * multiplier);
        }
    }
}
