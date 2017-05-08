package com.company;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.util.FileManager;

import java.io.*;
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

    private static OntModel loadOntology(String ontFilepath){
        //OntDocumentManager mgr = new OntDocumentManager();
        OntModel ontMod = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
        InputStream in = FileManager.get().open(ontFilepath);
        ontMod.read(in, null, "TURTLE");
        return ontMod;
    }

    public static void main(String[] args) {
        Pair<ArrayList<String>, Integer> cLineArgs = readCommandLineArgs(args);
        String ontologyFile = getOntologyArg(args);
        if(cLineArgs.cdr() == -1) // return failure if invalid command line args
            return;
        HashMap<String, LeagueItem> allItemHash = ItemReader.readInItems(System.getProperty("user.dir"));

        String modelFilepath = System.getProperty("user.dir") + "/" + ontologyFile; // "/OE_11_LeagueOfLegends-Ind_V2.ttl";
        OntModel model = loadOntology(modelFilepath); // load on ontology model
        //FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
        //Model model = FileManager.get().loadModel(modelFilepath, null, "TURTLE");
        //ItemReader.addItemStatsToModel(allItemHash, model);

        int goldAvailable = cLineArgs.cdr(); // 3600;
        int inventorySpace = 20; // currently static..
        ArrayList<String> currentItems = cLineArgs.car();
        if(!currentItems.contains("NoneItem"))
            currentItems.add("NoneItem");
        //currentItems.add("DoransBlade");
        //currentItems.add("WardingTotem");
        //currentItems.add("Pickaxe");
        //currentItems.add("HealthPotion");
        //currentItems.add("HealthPotion2");
        HashMap<String, Integer> ownedItems = BuildItem.getOwnedItemsHash(currentItems, model);
        //ownedItems.put("NoneItem", 0);
        //ownedItems.put("DoransBlade", 450);
        //ownedItems.put("WardingTotem", 0);
        //ownedItems.put("Pickaxe", 875);
        //ownedItems.put("HealthPotion", 50);
        //ownedItems.put("HealthPotion2", 50);

        HashMap<String, Integer> itemSuggestionList = BuildItem.getItemSuggestions(model, currentItems, ownedItems, goldAvailable, inventorySpace, 0);
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
