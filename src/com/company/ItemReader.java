package com.company;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by anders on 5/6/17.
 */
public class ItemReader {

    public static HashMap<String, LeagueItem> readInItems(String dirpath){
        String itemJsonPath = dirpath + "/item.json";
        JSONParser parser = new JSONParser();
        HashMap<String, LeagueItem> allItemHash = new HashMap<>();
        Set<String> allStatTypes = new HashSet<>();
        try{
            JSONObject mainObj = (JSONObject)parser.parse(new FileReader(itemJsonPath));
            JSONObject allData = (JSONObject)mainObj.get("data");
            Set<Map.Entry<String, JSONObject>> entrySet = allData.entrySet();
            int x = 0;
            for(Map.Entry<String, JSONObject> currEnt : entrySet){ // go over all items
                JSONObject currItemData = currEnt.getValue();
                String currItemName = ((String)currItemData.get("name")).replaceAll("[^a-zA-Z ]", "");
                currItemName = currItemName.replaceAll("\\s", "");
                if(currItemName.length() > 0) { // ignore the none item
                    JSONObject statObj = (JSONObject) currItemData.get("stats");
                    JSONObject goldObj = (JSONObject) currItemData.get("gold");
                    int currItemGoldCost = (int)(long) goldObj.get("total");
                    //System.out.println("on elt: " + currItemName + ", costs: " + currItemGoldCost);
                    Set<Map.Entry<String, Double>> statsList = statObj.entrySet();
                    HashMap<String, Double> currItemStats = new HashMap<>();
                    for(Map.Entry<String, Double> currStatEnt : statsList){
                        currItemStats.put(currStatEnt.getKey(), currStatEnt.getValue());
                        if(!allStatTypes.contains(currStatEnt.getKey()))
                            allStatTypes.add(currStatEnt.getKey());
                    }
                    LeagueItem newItem = new LeagueItem(currItemName, currItemGoldCost, currItemStats);
                    allItemHash.put(currItemName, newItem);
                    x++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for(String currStatName : allStatTypes){
            System.out.println("stat: " + currStatName);
        }
        System.out.println("len of allStatTypes: " + allStatTypes.size());
        return allItemHash;
    }
}
