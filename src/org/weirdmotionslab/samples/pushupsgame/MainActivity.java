package org.weirdmotionslab.samples.pushupsgame;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.TableRow.LayoutParams;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.LinearLayout;
import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import java.util.Comparator;
import java.util.Iterator;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Display display = getWindowManager().getDefaultDisplay();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CraftSystem.checkIfNewGame(this);
    }

    private void addButtons(){
        TableLayout settings = (TableLayout) findViewById(R.id.settings);
        settings.removeAllViews();
        TableRow currentRow = new TableRow(this);
        TextView speedText = new TextView(this);
        speedText.setText("Battle speed:");
        speedText.setId(View.generateViewId());
        currentRow.addView(speedText);
        settings.addView(currentRow);
        currentRow = new TableRow(this);

        Button speedButton1 = new Button(this);
        String stext = "Normal Speed";
        speedButton1.setText(stext);
        speedButton1.setId(View.generateViewId());
        speedButton1.setOnClickListener(v -> {
            CraftSystem.speedMultiplier = 1;
        });
        currentRow.addView(speedButton1);
        settings.addView(currentRow);
        currentRow = new TableRow(this);
        Button speedButton2 = new Button(this);
        stext = "Low Speed";
        speedButton2.setText(stext);
        speedButton2.setId(View.generateViewId());
        speedButton2.setOnClickListener(v -> {
            CraftSystem.speedMultiplier = 1.6f;
        });
        currentRow.addView(speedButton2);
        settings.addView(currentRow);
        currentRow = new TableRow(this);

        TextView difficultyText = new TextView(this);
        difficultyText.setText("Points to score:");
        difficultyText.setId(View.generateViewId());
        currentRow.addView(difficultyText);
        settings.addView(currentRow);

        currentRow = new TableRow(this);
        Button dButton1 = new Button(this);
        stext = "15";
        dButton1.setText(stext);
        dButton1.setId(View.generateViewId());
        dButton1.setOnClickListener(v -> {
            CraftSystem.winScore = 15;
        });
        currentRow.addView(dButton1);
        settings.addView(currentRow);
        currentRow = new TableRow(this);

        Button dButton2 = new Button(this);
        stext = "20";
        dButton2.setText(stext);
        dButton2.setId(View.generateViewId());
        dButton2.setOnClickListener(v -> {
            CraftSystem.winScore = 20;
        });
        currentRow.addView(dButton2);
        settings.addView(currentRow);
        currentRow = new TableRow(this);

        Button dButton3 = new Button(this);
        stext = "25";
        dButton3.setText(stext);
        dButton3.setId(View.generateViewId());
        dButton3.setOnClickListener(v -> {
            CraftSystem.winScore = 25;
        });
        currentRow.addView(dButton3);
        settings.addView(currentRow);
        currentRow = new TableRow(this);

        TableLayout locationButtons = (TableLayout) findViewById(R.id.locations);
        locationButtons.removeAllViews();
        currentRow = new TableRow(this);

        for (int i = 0; i < CraftSystem.allLocations.size(); i++) {
            Button b = new Button(this);
            Location location = CraftSystem.allLocations.get(i);
            String text = location.name + "\nDrop: \n";
            ArrayList<Item> dropItems = new ArrayList<Item>(location.drop.keySet());
            for (Item dropItem: dropItems) {
                if (dropItem.type == 0) {
                    text += "=="+dropItem.name+"==" + "\n";
                }
                if (dropItem.type == 1) {
                    text += dropItem.name + "\n";
                }
            }
            b.setText(text);
            b.setId(View.generateViewId());

            b.setOnClickListener(v -> {
                CraftSystem.lastBattleLocation = location;
                Intent intent = new Intent(MainActivity.this, BattleActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            });

            currentRow.addView(b);
            if ((i+1) % 1 == 0 || i == CraftSystem.allLocations.size() - 1 ) {
                locationButtons.addView(currentRow);
                currentRow = new TableRow(this);
            }
        }

    }

    public void updateUI(){
        String message = CraftSystem.processBattleResult();
        TextView log = (TextView) findViewById(R.id.message);
        log.setText(message);

        LinearLayout inventoryLayout = (LinearLayout) findViewById(R.id.inventoryLayout);
        inventoryLayout.removeAllViews();
        HashMap<Item, Integer> inventory = CraftSystem.inventory;
        ArrayList<Item> sortedItems = new ArrayList<Item>(inventory.keySet());
        Collections.sort(sortedItems, new ItemComparator());
        for (Item item: sortedItems) {
            if (item.type == 3) {
                continue;
            }
            TextView itemText = new TextView(this);
            Button b = new Button(this);
            String itemMessage = "";
            boolean canBeBuilt = true;
            if (item.type == 0 || item.type == 1) {
                if (item.state == 0) {
                    itemMessage = item.name + ": \n";
                    for (Item requiredItem : item.requiredItems.keySet()) {
                        int amount = 0;
                        if (inventory.containsKey(requiredItem)) {
                            if (requiredItem.state == 1) {
                                amount = inventory.get(requiredItem);
                            }
                        }
                        int requiredAmount = item.requiredItems.get(requiredItem);
                        if (amount < requiredAmount) {
                            canBeBuilt = false;
                        }
                        itemMessage += "[ " + requiredItem.name + " " + amount + "/" + requiredAmount + " ] \n";
                    }
                }
                if (item.state == 1) {
                    if (item.type == 1) {
                        boolean bigItemAlreadyBuilt = false;
                        for (Item bigItem: CraftSystem.allBigItems) {
                            if (bigItem.requiredItems.keySet().contains(item)) {
                                if (bigItem.state == 1) {
                                    bigItemAlreadyBuilt = true;
                                }
                                break;
                            }
                        }
                        if (!bigItemAlreadyBuilt) {
                            canBeBuilt = false;
                            itemMessage = item.name + ": 1";
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            } else {
                canBeBuilt = false;
                int amount = inventory.get(item);
                itemMessage = item.name + ": " + amount;
            }
            if (canBeBuilt) {
                b.setText(itemMessage);
                b.setId(View.generateViewId());
                b.setOnClickListener(v -> {
                    CraftSystem.buildItem(item);
                    CraftSystem.save(this);
                    updateUI();
                });
                inventoryLayout.addView(b);
            } else {
                itemText.setText(itemMessage);
                itemText.setId(View.generateViewId());
                inventoryLayout.addView(itemText);
            }
        }
        LinearLayout bigItemsLayout = (LinearLayout) findViewById(R.id.bigItemsLayout);
        bigItemsLayout.removeAllViews();
        for (Item bigItem: CraftSystem.allBigItems) {
            TextView itemText = new TextView(this);
            itemText.setText(bigItem.name);
            itemText.setId(View.generateViewId());
            if (bigItem.state == 0) {
                itemText.setTextColor(Color.rgb(128, 128, 128));
            } else {
                itemText.setTextColor(Color.rgb(100, 255, 100));
            }
            bigItemsLayout.addView(itemText);
        }
        LinearLayout superItemsLayout = (LinearLayout) findViewById(R.id.superItemsLayout);
        superItemsLayout.removeAllViews();
        for (Location2 superLocation: CraftSystem.allSuperLocations) {
            Item superItem = null;
            for (Item item: superLocation.drop.keySet()){
                superItem = item;
            }
            if (inventory.containsKey(superItem)) {
                continue;
            }
            Button b = new Button(this);
            TextView t = new TextView(this);
            String buttonMessage = "[" + superLocation.name + "]" + "\n";
            buttonMessage += "==" + superItem.name + "==\n";
            buttonMessage += "Motorarmors Required:\n";
            boolean readyToFuse = true;
            for (Item bigItem: superLocation.requiredItems.keySet()) {
                if (inventory.containsKey(bigItem) && bigItem.state == 1) {
                    buttonMessage += "    " + bigItem.name + ": [1/1]\n";
                } else {
                    buttonMessage += "    " + bigItem.name + ": [0/1]\n";
                    readyToFuse = false;
                }
            }
            buttonMessage += "Materials Required:\n";
            for (Item material: superLocation.requiredMaterials.keySet()) {
                int required = superLocation.requiredMaterials.get(material);
                if (inventory.containsKey(material)) {
                    int amount = inventory.get(material);
                    if (amount < required) {
                        readyToFuse = false;
                    }
                    buttonMessage += "    " + material.name + ": [" + amount + "/" + required + "]\n";
                } else {
                    buttonMessage += "    " + material.name + ": [0/" + required + "]\n";
                    readyToFuse = false;
                }
            }
            if (readyToFuse) {
                b.setText(buttonMessage);
                b.setId(View.generateViewId());
                b.setOnClickListener(v -> {
                    for (Item material: superLocation.requiredMaterials.keySet()) {
                        inventory.put(material, inventory.get(material) - superLocation.requiredMaterials.get(material));
                    }
                    CraftSystem.lastBattleSuperLocation = superLocation;
                    Intent intent = new Intent(MainActivity.this, BattleActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                });
                superItemsLayout.addView(b);
            } else {
                t.setText(buttonMessage);
                t.setId(View.generateViewId());
                superItemsLayout.addView(t);
            }
        }

        LinearLayout superItemsSumLayout = (LinearLayout) findViewById(R.id.superItemsSumLayout);
        superItemsSumLayout.removeAllViews();

        for (Item superItem: CraftSystem.allSuperItems) {
            TextView itemText = new TextView(this);
            itemText.setText(superItem.name);
            itemText.setId(View.generateViewId());
            if (superItem.state == 0) {
                itemText.setTextColor(Color.rgb(128, 128, 128));
            } else {
                itemText.setTextColor(Color.rgb(255, 215, 0));
            }
            superItemsSumLayout.addView(itemText);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        addButtons();
        updateUI();
        CraftSystem.save(this);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class ItemComparator implements Comparator<Item> {

        public int compare(Item i1, Item i2)
        {
            if (i1.type > i2.type) {
                return 1;
            }
            if (i1.type < i2.type) {
                return -1;
            }
            if (i1.type == i2.type) {
                if (i1.state > i2.state) {
                    return 1;
                }
                if (i1.state < i2.state) {
                    return -1;
                }
                if (i1.state == i2.state) {
                    return 0;
                }
                return 0;
            }
            return 0;
        }
    }
}