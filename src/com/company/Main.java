package com.company;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.util.FileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
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


    /**
     * Called by getNextItemQuery and getItemBuildsIntoQuery
     * @param currentItems, the current items in the users inventory
     * @return a formatted SPARQL query line containing all items
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

    /**
     * Called by getNextItemQuery
     * @param currentItems, the current items in the users inventory
     * @return a formatted SPARQL query line containing all items
     */
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

    /**
     * Called by getItemSuggestions
     * @param currentItems, array list of the users current items
     * @param goldAvailable, the users current gold
     * @param inventorySpace, the number of open slots in the users inventory
     * @return a SPARQL query string to get all 'nextItemInBuild' relationships
     */
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

    /**
     * Called by getItemSuggestions
     * @param currentItems, array list of the users current items
     * @param goldAvailable, the users current gold
     * @param inventorySpace, the number of open slots in the users inventory
     * @return a SPARQL query string to get all 'buildsInto' relationships
     */
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
        s.append("} group by ?item ?nextItem ?cost ?nextitemcost ?totalcost limit ");
        s.append(inventorySpace);
        return s.toString();
    }

    /**
     * Called by getItemSuggestions, runs a SPARQL query through the model
     * @param itemQuery, the query string to be run
     * @param model, the model object representing the ontology
     * @param itemBuildsIntoRels, structure keeping track of which items build into which
     * @param nextItemP, true when we are running a 'next item' query, false otherwise
     * @return a mapping of affordable items to their gold costs
     */
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
                    }
                }
            }
        } finally {
            qexec.close();
        }
        return items;
    }

    /**
     * Called by getItemSuggestions, writes a SPARQL query to file
     * @param theQuery, the query string to write to file
     * @param destFilename, the file name the query will be saved under
     */
    private static void writeQueryToFile(String theQuery, String destFilename){
        String outputDirpath = System.getProperty("user.dir");
        File outputF = new File(outputDirpath + "/generatedQueries");
        if(!outputF.exists()){ // ensure directory for output queries exists
            outputF.mkdir();
        }
        try {
            PrintWriter writer = new PrintWriter(outputDirpath + "/generatedQueries/" + destFilename, "UTF-8");
            writer.println(theQuery);
            writer.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Called by main and itself (recursive)
     * @param model, the model object representing the ontology
     * @param currentItems, array list of the users current items
     * @param ownedItems, a mapping of all items to their respective costs
     * @param goldAvailable, the users current gold
     * @param inventorySpace, the number of open slots in the users inventory
     * @return A hash map of suggested items to buy and their respective costs
     */
    private static HashMap<String, Integer> getItemSuggestions(Model model, ArrayList<String> currentItems, HashMap<String, Integer> ownedItems, int goldAvailable, int inventorySpace, int depth){
        HashMap<String, String> itemBuildsIntoRels = new HashMap<>();
        HashMap<String, Integer> itemSuggestionList = new HashMap<>();
        int runningGoldTot = goldAvailable;
        String nextItemQuery = getNextItemQuery(currentItems, goldAvailable, inventorySpace);
        String itemBuildsIntoQuery = getItemBuildsIntoQuery(currentItems, goldAvailable, inventorySpace);
        writeQueryToFile(nextItemQuery, "nextItemQuery" + depth + ".txt");
        writeQueryToFile(itemBuildsIntoQuery, "buildsIntoQuery" + depth + ".txt");
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
                HashMap<String, Integer> tempItemSuggestions = getItemSuggestions(model, tempCurrItems, tempOwnedItems, tempGoldAvailable, inventorySpace, depth+1);
                for(Map.Entry<String, Integer> ent2 : tempItemSuggestions.entrySet()){
                    if(!newItemSuggestions.containsKey(ent2.getKey()))
                        newItemSuggestions.put(ent2.getKey(), ent2.getValue()+ent.getValue());
                }
            }
            itemSuggestionList.putAll(newItemSuggestions);
        }
        return itemSuggestionList;
    }

    private static Pair<ArrayList<String>, Integer> readCommandLineArgs(String[] args){
        int currGold = -1;
        boolean itemsP = false;
        ArrayList<String> currItems = new ArrayList<>();
        for(int x = 0; x < args.length; x++){
            if(itemsP)
                currItems.add(args[x]);
            if(args[x].equals("--gold") && x+1 < args.length)
                currGold = Integer.parseInt(args[x+1]);
            else if (args[x].equals("--items"))
                itemsP = true;
        }
        if(currGold == -1)
            System.out.println("error, invalid command line args");
        return new Pair<>(currItems, currGold);
    }

    private static String getOntologyArg(String[] args){
        String ontologyFile = "";
        for(int x = 0; x < args.length; x++){
            if(args[x].equals("--ontology") && x+1 < args.length)
                ontologyFile = args[x+1];
        }
        return ontologyFile;
    }

    /**
     * Called by main, get the gold costs of all current items from the ontology
     * @param currentItems, array list of the users current items
     * @param model, the model object representing the ontology
     * @return a mapping of all items to their respective costs
     */
    private static HashMap<String, Integer> getOwnedItemsHash(ArrayList<String> currentItems, Model model){
        HashMap<String, Integer> ownedItems = new HashMap<>();
        StringBuilder s = new StringBuilder();
        String itemQueryPrefix = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX lol: <http://tw.rpi.edu/web/Courses/Ontologies/2017/LeagueOfLegends/LeagueOfLegends/>\n" +
                "PREFIX lol-ind: <http://tw.rpi.edu/web/Courses/Ontologies/2017/LeagueOfLegends/LeagueOfLegends-Ind/>\n" +
                "SELECT ?item ?cost\n" +
                "WHERE {?item a lol:Item. \n" +
                "\t?item lol:hasGoldCost ?cost.";
        s.append(itemQueryPrefix);
        String rootItemsStr = getRootItemsStr(currentItems);
        s.append(rootItemsStr);
        s.append("} group by ?item ?cost limit 20");
        String itemQuery = s.toString();
        Query query = QueryFactory.create(itemQuery);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                String currItem = soln.getResource("item").getLocalName();
                int currItemCost = soln.getLiteral("cost").getInt();
                if(!ownedItems.containsKey(currItem))
                    ownedItems.put(currItem, currItemCost);
            }
        } finally {
            qexec.close();
        }
        return ownedItems;
    }

    public static void main(String[] args) {

        Pair<ArrayList<String>, Integer> cLineArgs = readCommandLineArgs(args);
        String ontologyFile = getOntologyArg(args);
        if(cLineArgs.cdr() == -1) // return failure if invalid command line args
            return;
        String modelFilepath = System.getProperty("user.dir") + "/" + ontologyFile; // "/OE_11_LeagueOfLegends-Ind_V2.ttl";
        System.out.println("Model Filepath = " + modelFilepath);
        FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
        Model model = FileManager.get().loadModel(modelFilepath, null, "TURTLE");

        int goldAvailable = cLineArgs.cdr(); // 3600;
        int inventorySpace = 20; // currently static..
        ArrayList<String> currentItems = cLineArgs.car(); //new ArrayList<>();
        if(!currentItems.contains("NoneItem"))
            currentItems.add("NoneItem");
        //currentItems.add("DoransBlade");
        //currentItems.add("WardingTotem");
        //currentItems.add("Pickaxe");
        //currentItems.add("HealthPotion");
        //currentItems.add("HealthPotion2");
        HashMap<String, Integer> ownedItems = getOwnedItemsHash(currentItems, model); //new HashMap<>();
        //ownedItems.put("NoneItem", 0);
        //ownedItems.put("DoransBlade", 450);
        //ownedItems.put("WardingTotem", 0);
        //ownedItems.put("Pickaxe", 875);
        //ownedItems.put("HealthPotion", 50);
        //ownedItems.put("HealthPotion2", 50);

        HashMap<String, Integer> itemSuggestionList = getItemSuggestions(model, currentItems, ownedItems, goldAvailable, inventorySpace, 0);

        for(Map.Entry<String, Integer> ent : itemSuggestionList.entrySet()){ // print results
            System.out.println("Can buy: " + ent.getKey() + " for " + ent.getValue() + " gold");
        }

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
