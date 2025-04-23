package org.cucumber.utilities;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MongoDBDumpAndLoad {

    public static void main(String[] args) throws IOException, ParseException {




        JSONParser jsonParser = new JSONParser();
        FileReader partnerList = new FileReader(BrowserUtils.getDownloadPath()+"/partnerList.json");
        JSONArray partnerArray = (JSONArray) jsonParser.parse(partnerList);

        System.out.println("How many partners will be copied = " + partnerArray.size());
        for (int k = 0; k < partnerArray.size(); k++) {

            JSONObject partnerJsonObj = (JSONObject) partnerArray.get(k);
            System.out.println("********************************************************************************************************************");
            System.out.println("******************************* Copy Collection Started for Partner = "+partnerJsonObj.get("target")+" ******************************");
            System.out.println("********************************************************************************************************************");
            System.out.println("Partners copy from/to = " + partnerJsonObj);
            String sourceDatabaseName = partnerJsonObj.get("source").toString();
            String targetDatabaseName = partnerJsonObj.get("target").toString();

            // Output directory for the dump
            String outputDir = BrowserUtils.getDownloadPath();

            // Collections to dump
            List<String> collectionsToDump = Arrays.asList(
//                    "st_835_assigned",
//                    "clp_835_assigned",
//                    "svc_835_assigned",
//                    "cas_svc_835_assigned",
//                    "era_split_file_assigned",
//                    "ledger_1",
//                    "dfi_account_1",
//                    "claim_1"
//                    "claim_proc_1"
//                    "users"
            );

            MongoClientURI uri = new MongoClientURI(ConfigurationReader.get("mongoURI"));
            MongoClient mongoClient = new MongoClient(uri);


            MongoDatabase sourceDatabase = mongoClient.getDatabase(sourceDatabaseName);
            MongoDatabase targetDatabase = mongoClient.getDatabase(targetDatabaseName);

            // Copy each specified collection
            for (String collectionName : collectionsToDump) {
                System.out.println("Copying " + collectionName + " to " + targetDatabaseName + "...");

                MongoCollection<Document> sourceCollection = sourceDatabase.getCollection(collectionName);
                List<Document> documents = sourceCollection.find().into(new ArrayList<>());

                // Delete old data in the target collection
                MongoCollection<Document> targetCollection = targetDatabase.getCollection(collectionName);
                targetCollection.deleteMany(new Document());

                // Insert new data into the target collection
                targetCollection.insertMany(documents);

                // Save the documents to JSON files (optional)
//                    saveDocumentsToJson(outputDir, collectionName, documents);
            }

            System.out.println("Copying collection completed.");
        }

    }

//    private static String buildMongoURI(String host, int port, String username, String password, String databaseName) {
//        StringBuilder uriBuilder = new StringBuilder("mongodb://");
//        if (username != null && password != null) {
//            uriBuilder.append(username).append(":").append(password).append("@");
//        }
//        uriBuilder.append(host).append(":").append(port).append("/").append(databaseName);
//        return uriBuilder.toString();
//    }
//
//    private static void saveDocumentsToJson(String outputDir, String collectionName, List<Document> documents) throws IOException {
//        File outputFile = new File(outputDir, collectionName + ".json");
//        // Use your preferred method to save documents to a file, e.g., JSON serialization
//        // Here, we'll simply print the documents to the console for demonstration purposes
//        for (Document document : documents) {
//            System.out.println(document.toJson());
//        }
//    }
}

