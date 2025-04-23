package org.cucumber.utilities;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
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

public class MongoDBDumpAndLoadwithBatch {

    public static void main(String[] args) throws IOException, ParseException {


        JSONParser jsonParser = new JSONParser();
        FileReader partnerList = new FileReader(BrowserUtils.getDownloadPath() + "/partnerList.json");
        JSONArray partnerArray = (JSONArray) jsonParser.parse(partnerList);

        System.out.println("How many partners will be copied = " + partnerArray.size());
        for (int k = 0; k < partnerArray.size(); k++) {

            JSONObject partnerJsonObj = (JSONObject) partnerArray.get(k);
            System.out.println("********************************************************************************************************************");
            System.out.println("******************************* Copy Collection Started for Partner = " + partnerJsonObj.get("target") + " ******************************");
            System.out.println("********************************************************************************************************************");
            System.out.println("Partners copy from/to = " + partnerJsonObj);
            String sourceDatabaseName = partnerJsonObj.get("source").toString();
            String targetDatabaseName = partnerJsonObj.get("target").toString();

            // Output directory for the dump
//            String outputDir = BrowserUtils.getDownloadPath();

            // Collections to dump
            List<String> collectionsToDump = Arrays.asList(
//                    "st_835_assigned",
//                    "clp_835_assigned",
//                    "svc_835_assigned",
//                    "cas_svc_835_assigned",
                    "era_split_file_assigned"
//                    "ledger_1",
//                    "dfi_account_1",
//                    "claim_1",
//                    "claim_proc_1",
//                    "hsbc_820_835_entry",
//                    "patient_plan_1",
//                    "insurance_plan_1",
//                    "insurance_subscriber_1",
//                    "enrollment_1",
//                    "patient_plan_companion_1",
//                    "patient_payer_mapping_1"
            );

            MongoClientURI uri = new MongoClientURI(ConfigurationReader.get("mongoURI"));
            MongoClient mongoClient = new MongoClient(uri);


            MongoDatabase sourceDatabase = mongoClient.getDatabase(sourceDatabaseName);
            MongoDatabase targetDatabase = mongoClient.getDatabase(targetDatabaseName);

            // Copy each specified collection
            for (String collectionName : collectionsToDump) {
                System.out.println("Copying " + collectionName + " to " + targetDatabaseName + "...");

                MongoCollection<Document> sourceCollection = sourceDatabase.getCollection(collectionName);
                MongoCollection<Document> targetCollection = targetDatabase.getCollection(collectionName);

                // Delete old data in the target collection
                targetCollection.deleteMany(new Document());


                // Fetch documents from the source collection
                FindIterable<Document> findIterable = sourceCollection.find();
                MongoCursor<Document> cursor = findIterable.iterator();

                // Delete old data in the target collection
                targetCollection.deleteMany(new Document());

                // Process documents in batches
                List<Document> batch = new ArrayList<>();
                int batchSize = 1000;

                while (cursor.hasNext()) {
                    batch.add(cursor.next());

                    if (batch.size() == batchSize) {
                        // Send the batch to the target collection
                        targetCollection.insertMany(batch);

                        // Clear the batch and free up memory
                        batch.clear();
                    }
                }

                // Process any remaining documents
                if (!batch.isEmpty()) {
                    targetCollection.insertMany(batch);
                    batch.clear();
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
}
