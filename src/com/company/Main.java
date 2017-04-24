package com.company;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.util.FileManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Main {


    /*
    An example fully formed item query using the 'nextItemInBuild' property
    String itemQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX lol: <http://tw.rpi.edu/web/Courses/Ontologies/2017/LeagueOfLegends/LeagueOfLegends/>\n" +
                "PREFIX lol-ind: <http://tw.rpi.edu/web/Courses/Ontologies/2017/LeagueOfLegends/LeagueOfLegends-Ind/>\n" +
                "SELECT ?nextItem ?nextitemcost\n" +
                "WHERE {?item a lol:Item. ?item lol:nextItemInBuild ?nextItem.\n" +
                "\t?item lol:hasGoldCost ?cost.\n" +
                "\t?nextItem lol:hasGoldCost ?nextitemcost.\n" +
                "\tFILTER (?item = lol-ind:DoransBlade || ?item = lol-ind:WardingTotem).\n" +
                "\tFILTER(?nextitemcost <= 1100)\n" +
                "} group by ?item ?nextItem ?cost ?nextitemcost ?totalcost limit 5";
     */



    private static String getRootItemsStr(ArrayList<String> currentItems){
        StringBuilder s = new StringBuilder();
        s.append("\tFILTER (");
        for(int x = 0; x < currentItems.size(); x++){
            String currItem = "?item = lol-ind:" + currentItems.get(x);
            if(x != currentItems.size()-1) {
                s.append(currItem);
                s.append(" || ");
            } else
                s.append(currItem);
        }
        s.append(").\n");
        return s.toString();
    }

    private static String getPrecludeItemsString(ArrayList<String> currentItems){
        StringBuilder s = new StringBuilder();
        s.append("\tFILTER (");
        for(int x = 0; x < currentItems.size(); x++){
            String currItem = "?nextItem != lol-ind:" + currentItems.get(x);
            if(x != currentItems.size()-1) {
                s.append(currItem);
                s.append(" && ");
            } else
                s.append(currItem);
        }
        s.append(").\n");
        return s.toString();
    }

    private static String getNextItemQuery(ArrayList<String> currentItems, int goldAvailable, int inventorySpace){
        StringBuilder s = new StringBuilder();
        String itemQueryPrefix = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX lol: <http://tw.rpi.edu/web/Courses/Ontologies/2017/LeagueOfLegends/LeagueOfLegends/>\n" +
                "PREFIX lol-ind: <http://tw.rpi.edu/web/Courses/Ontologies/2017/LeagueOfLegends/LeagueOfLegends-Ind/>\n" +
                "SELECT ?nextItem ?nextitemcost\n" +
                "WHERE {?item a lol:Item. ?item lol:nextItemInBuild ?nextItem.\n" +
                "\t?item lol:hasGoldCost ?cost.\n" +
                "\t?nextItem lol:hasGoldCost ?nextitemcost.\n";
        s.append(itemQueryPrefix);
        String rootItemsStr = getRootItemsStr(currentItems);
        s.append(rootItemsStr);
        String precludeItemsStr = getPrecludeItemsString(currentItems);
        s.append(precludeItemsStr);
        s.append("\tFILTER(?nextitemcost <= ");
        s.append(goldAvailable);
        s.append(")\n");
        s.append("} group by ?item ?nextItem ?cost ?nextitemcost ?totalcost limit ");
        s.append(inventorySpace);
        return s.toString();
    }

    private static String getItemBuildsIntoQuery(ArrayList<String> currentItems, int goldAvailable, int inventorySpace){
        StringBuilder s = new StringBuilder();
        String itemQueryPrefix = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX lol: <http://tw.rpi.edu/web/Courses/Ontologies/2017/LeagueOfLegends/LeagueOfLegends/>\n" +
                "PREFIX lol-ind: <http://tw.rpi.edu/web/Courses/Ontologies/2017/LeagueOfLegends/LeagueOfLegends-Ind/>\n" +
                "SELECT ?item ?nextItem ?nextitemcost\n" +
                "WHERE {?item a lol:Item. ?item lol:buildsInto ?nextItem.\n" +
                "\t?item lol:hasGoldCost ?cost.\n" +
                "\t?nextItem lol:hasGoldCost ?nextitemcost.\n";
        s.append(itemQueryPrefix);
        String rootItemsStr = getRootItemsStr(currentItems);
        s.append(rootItemsStr);
        String precludeItemsStr = getPrecludeItemsString(currentItems);
        //s.append(precludeItemsStr);
        //s.append("\tFILTER(?nextitemcost <= ");
        //s.append(goldAvailable);
        //s.append(")\n");
        s.append("} group by ?item ?nextItem ?cost ?nextitemcost ?totalcost limit ");
        s.append(inventorySpace);
        return s.toString();
    }

    private static HashMap<String, Integer> runItemQuery(String itemQuery, Model model, HashMap<String, String> itemBuildsIntoRels, boolean nextItemP){
        Query query = QueryFactory.create(itemQuery);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        HashMap<String, Integer> items = new HashMap<>();
        try {
            ResultSet results = qexec.execSelect();
            while ( results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                String nextItem = soln.getResource("nextItem").getLocalName();
                int nextItemCost = soln.getLiteral("nextitemcost").getInt();
                if(!items.containsKey(nextItem)) {
                    items.put(nextItem, nextItemCost);
                    if(nextItemP) {
                        String currItem = soln.getResource("item").getLocalName();
                        if (!itemBuildsIntoRels.containsKey(currItem)) {
                            itemBuildsIntoRels.put(currItem, nextItem);
                        }
                        //System.out.println("item: " + currItem + " builds into " + nextItem + ", nextItemCost: " + nextItemCost);
                    }
                }
            }
        } finally {
            qexec.close();
        }
        return items;
    }

    private static HashMap<String, Integer> getItemSuggestions(Model model, ArrayList<String> currentItems, HashMap<String, Integer> ownedItems, int goldAvailable, int inventorySpace){
        HashMap<String, String> itemBuildsIntoRels = new HashMap<>();
        HashMap<String, Integer> itemSuggestionList = new HashMap<>();
        int runningGoldTot = goldAvailable;
        String nextItemQuery = getNextItemQuery(currentItems, goldAvailable, inventorySpace);
        String itemBuildsIntoQuery = getItemBuildsIntoQuery(currentItems, goldAvailable, inventorySpace);
        HashMap<String, Integer> nextItems = runItemQuery(nextItemQuery, model, itemBuildsIntoRels, false);
        HashMap<String, Integer> buildsIntoItems = runItemQuery(itemBuildsIntoQuery, model, itemBuildsIntoRels, true);

        for(Map.Entry<String, Integer> ent : buildsIntoItems.entrySet()){
            int valOwnedCompItems = 0;
            if(itemBuildsIntoRels.containsValue(ent.getKey())){ // if an item is incomplete
                for(Map.Entry<String, String> subEnt : itemBuildsIntoRels.entrySet()){
                    if(subEnt.getValue().equals(ent.getKey())){ // if we own an item that's part of the build path
                        int compItemCost = ownedItems.get(subEnt.getKey());
                        valOwnedCompItems+=compItemCost; // add sum of what we have already paid for
                    }
                }
            }
            int tempVal = (ent.getValue()-valOwnedCompItems);
            if(runningGoldTot >= tempVal) {
                ent.setValue(tempVal);
                itemSuggestionList.put(ent.getKey(), ent.getValue());
                runningGoldTot-=tempVal;
            }
        }
        boolean buyableCompItemsP = false;
        for(Map.Entry<String, Integer> ent : nextItems.entrySet()){
            if(!ownedItems.containsKey(ent.getKey()) && runningGoldTot >= ent.getValue()) {
                itemSuggestionList.put(ent.getKey(), ent.getValue());
                buyableCompItemsP = true;
            }
        }
        if(buyableCompItemsP) { // if we have enough gold to look at larger component items, recursively search for all possible options
            HashMap<String, Integer> newItemSuggestions = new HashMap<>();
            for(Map.Entry<String, Integer> ent : itemSuggestionList.entrySet()){ // see what new items appear if we buy each possible item
                int tempGoldAvailable = runningGoldTot - ent.getValue();
                ArrayList<String> tempCurrItems = new ArrayList<>(currentItems);
                tempCurrItems.add(ent.getKey());
                HashMap<String, Integer> tempOwnedItems = new HashMap<>(ownedItems);
                tempOwnedItems.put(ent.getKey(), ent.getValue());
                HashMap<String, Integer> tempItemSuggestions = getItemSuggestions(model, tempCurrItems, tempOwnedItems, tempGoldAvailable, inventorySpace);
                for(Map.Entry<String, Integer> ent2 : tempItemSuggestions.entrySet()){
                    if(!newItemSuggestions.containsKey(ent2.getKey())){
                        newItemSuggestions.put(ent2.getKey(), ent2.getValue()+ent.getValue());
                    }
                }
            }
            itemSuggestionList.putAll(newItemSuggestions);
        }
        return itemSuggestionList;

    }

    public static void main(String[] args) {
        String modelFilepath = System.getProperty("user.dir") + "/OE_11_LeagueOfLegends-Ind_V2.ttl";
        System.out.println("Model Filepath = " + modelFilepath);
        FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
        Model model = FileManager.get().loadModel(modelFilepath, null, "TURTLE");

        // Example auto generate query
        int goldAvailable = 3600;
        int inventorySpace = 20;
        ArrayList<String> currentItems = new ArrayList<>();
        currentItems.add("NoneItem");
        currentItems.add("DoransBlade");
        currentItems.add("WardingTotem");
        //currentItems.add("Pickaxe");
        currentItems.add("HealthPotion");
        currentItems.add("HealthPotion2");
        HashMap<String, Integer> ownedItems = new HashMap<>();
        ownedItems.put("NoneItem", 0);
        ownedItems.put("DoransBlade", 450);
        ownedItems.put("WardingTotem", 0);
        //ownedItems.put("Pickaxe", 875);
        ownedItems.put("HealthPotion", 50);
        ownedItems.put("HealthPotion2", 50);

        HashMap<String, Integer> itemSuggestionList = getItemSuggestions(model, currentItems, ownedItems, goldAvailable, inventorySpace);

        for(Map.Entry<String, Integer> ent : itemSuggestionList.entrySet()){ // print results
            System.out.println("Can buy: " + ent.getKey() + " for " + ent.getValue() + " gold");
        }

        //System.out.println(itemBuildsIntoQuery);

        /*
        String testQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "PREFIX lol: <http://tw.rpi.edu/web/Courses/Ontologies/2017/LeagueOfLegends/LeagueOfLegends/>" +
                "SELECT ?team ?x ?role ?roleType ?roleLikelihood ?damageType where{ " +
                    "?team a lol:Team." +
                    "?team lol:hasChampion ?x." +
                    "?x lol:hasRole ?role." +
                    "?role rdf:type ?roleType." +
                    "?role lol:hasDamageTypeString ?damageType." +
                    "?role lol:hasLikelihood ?roleLikelihood" +
                "}";
        Query query = QueryFactory.create(testQuery);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        HashSet<String> teams = new HashSet<>();
        HashMap<String, ArrayList<String>> roleMappings = new HashMap<>();
        try {
            ResultSet results = qexec.execSelect();
            while ( results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                String currTeamName = soln.getResource("team").getLocalName();
                if(!teams.contains(currTeamName)){
                    teams.add(currTeamName);
                    System.out.println(currTeamName); // returns TeamOne
                }
                String currRole = soln.getResource("roleType").getLocalName();
                if(!currRole.equals("NamedIndividual")) {
                    String currChampName = soln.getResource("x").getLocalName();
                    System.out.print("\tchampion: " + currChampName);
                    System.out.print("\trole: " + currRole);
                    Literal currRoleLikelihoodLit = soln.getLiteral("roleLikelihood");
                    int currRoleLikelihood = Integer.parseInt(currRoleLikelihoodLit.getString());
                    System.out.println("\trole likelihood: " + currRoleLikelihood);

                }

            }
        } finally {
            qexec.close();
        }
        */
    }
}
