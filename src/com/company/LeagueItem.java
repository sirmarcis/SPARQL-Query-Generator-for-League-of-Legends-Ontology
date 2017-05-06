package com.company;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anders on 5/6/17.
 */
public class LeagueItem {
    private String name;
    private int goldCost;
    private HashMap<String, Double> itemStats;

    public LeagueItem(String theName, int theGoldCost, HashMap<String, Double> theItemStats){
        name = theName;
        goldCost = theGoldCost;
        convertStatNames(theItemStats);
    }

    private void convertStatNames(HashMap<String, Double> theItemStats){
        // TODO: account for stats instead of just lumping them with others, done here
        itemStats = new HashMap<>();
        for(Map.Entry<String, Double> currEnt : theItemStats.entrySet()){
            String convertedStatName;
            switch(currEnt.getKey()){
                case "FlatSpellBlockMod":   convertedStatName = "MagicResistance";
                                            break;
                case "FlatHPRegenMod":      convertedStatName = "HealthRegeneration";
                                            break;
                case "PercentMovementSpeedMod": convertedStatName = "MovementSpeed";
                                            break;
                case "FlatCritChanceMod":   convertedStatName = "CriticalStrikeChance";
                                            break;
                case "FlatPhysicalDamageMod": convertedStatName = "AttackDamage";
                                            break;
                case "PercentLifeStealMod": convertedStatName = "LifeSteal";
                                            break;
                case "PercentAttackSpeedMod": convertedStatName = "AttackSpeed";
                                            break;
                case "FlatMPRegenMod": convertedStatName = "ManaRegeneration";
                                            break;
                case "FlatMagicDamageMod": convertedStatName = "AbilityPower";
                                            break;
                case "FlatMovementSpeedMod": convertedStatName = "MovementSpeed";
                                            break;
                case "FlatArmorMod": convertedStatName = "Armor";
                                            break;
                case "FlatMPPoolMod": convertedStatName = "Mana";
                                            break;
                case "FlatHPPoolMod": convertedStatName = "Health";
                                            break;
                default: convertedStatName = "";
                    break;
            }
            itemStats.put(convertedStatName, currEnt.getValue());
        }
    }

    public String getName(){
        return name;
    }

    public int getGoldCost(){
        return goldCost;
    }

    public HashMap<String, Double> getItemStats() {
        return itemStats;
    }
}
