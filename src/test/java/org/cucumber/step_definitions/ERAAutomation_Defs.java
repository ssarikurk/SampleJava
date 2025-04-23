package org.cucumber.step_definitions;

import org.cucumber.utilities.*;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.*;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class ERAAutomation_Defs {

    String partner = "";
    String practiceId = "";
    String mysqlIp = "";
    String mysqlDbName = "";
    String s3BucketName = "retrace-synthetic-bank-processing";
    String practiceName = "";
    String integrationId = "";
    String s3TestFolderName = "eraAutomationTest/";
    String collectionName = "eraCycleTestCases";
    String serverIp = "10.200.55.91";
    String server820UploadPath = "/tmp/process820";


    JsonPath eraForSweep = null;
    List<Object> eraWritebackRecords = null;
    String eraSplitFileAssignedId = "";
    String dfiAccountId = "";
    //    String ledgerId = "";
    String eraCycleCountStr = "";


    List<Map<String, Object>> testCaseList = new ArrayList<>();
    List<String> fileNameList837 = new ArrayList<>();
    List<String> downloaded820NameList = new ArrayList<>();
    String testCasesDB = "";
    String tin = "";
    String npi = "";
    private BasicDBObject parse;
    private BasicDBObject query;

    @Given("set master data for Autonomus ERA Cycle Tests2")
    public void setMasterDataForAutonomusERACycleTests2() {
        testCasesDB = "qa-clinic3Era"; // we are keeping all data in this database
        partner = "qa-clinic3";
        practiceId = "LtB29WCY4jh1WL9u";
        practiceName = "ERA CYCLE";
        mysqlIp = "10.200.11.31";
        s3BucketName = "retrace-synthetic-bank-processing";
        mysqlDbName = "era_cycle";
        integrationId = "48ba61f4-d1d5-4f8d-bd5b-2ca1e8672e53";
        s3TestFolderName = "eraAutomationTestECAT/";
        collectionName = "eraCycleTestCases";
        dfiAccountId = "q09iwToxSvUevirtualEraCycle";
        serverIp = "10.200.55.91";
        server820UploadPath = "/tmp/process820ecat";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> dfiAccountColl = MongoDBUtils.connectMongodb(mongoClient, partner, "dfi_account_1");
        Bson dfiFilter = eq("dfi_account_id", dfiAccountId);
        Document dfiDoc = dfiAccountColl.find(dfiFilter).first();
        tin = dfiDoc.getString("tin");
        npi = dfiDoc.getString("npi");
        mongoClient.close();
    }


    @Given("set master data for ERA Cycle Tests")
    public void setMasterDataForERACycleTests() {
        testCasesDB = "qa-test";
        partner = "qa-cls5";
        practiceId = "iCyaFQjSsFmt1RfN";
        practiceName = "CLS2";
        mysqlIp = "10.200.11.31";
        s3BucketName = "retrace-synthetic-bank-processing";
        mysqlDbName = "clc_staging";
        integrationId = "d76b0913-0268-42d3-9a44-f87be448d590";
        s3TestFolderName = "eraAutomationTest/";
        collectionName = "eraCycleTestCases";
//        collectionName = "claimEraMatchInfo";
//        collectionName = "claimEraMatchInfoTest";
        dfiAccountId = "54f0a5e3-754e-4661-8f68-f81d5ff92e99";
        serverIp = "10.200.55.91";
        server820UploadPath = "/tmp/process820";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> dfiAccountColl = MongoDBUtils.connectMongodb(mongoClient, partner, "dfi_account_1");
        Bson dfiFilter = eq("dfi_account_id", dfiAccountId);
        Document dfiDoc = dfiAccountColl.find(dfiFilter).first();
        tin = dfiDoc.getString("tin");
        npi = dfiDoc.getString("npi");

        MongoCollection<Document> cycleCountColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, "cycleCount");
        Document filterDoc = Document.parse("{countName:'eraCycle'}");

        Document cycleCountDoc = cycleCountColl.find(filterDoc).first();
//        System.out.println("cycleCountDoc.toJson() = " + cycleCountDoc.toJson());
        eraCycleCountStr = cycleCountDoc.get("eraCycleCount").toString();

        mongoClient.close();
    }

    @Given("read test case list")
    public void read_test_case_list() throws IOException {
//        String projectPath = System.getProperty("user.dir")+"\\src\\test\\resources\\Downloads\\eraAutomation";

        String projectPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" +
                File.separator + "resources" + File.separator + "Downloads" + File.separator + "eraAutomation";

//        System.out.println("projectPath = " + projectPath);
        testCaseList = ExcelUtil.readCSVtoListofMapWithPath(projectPath + File.separator + "eraAutomationTestClaimList.csv");
//        for (Map<String, Object> stringObjectMap : testCaseList) {
//            System.out.println("stringObjectMap = " + stringObjectMap);
//            System.out.println("stringObjectMap.get(\"testCaseName8\") = " + stringObjectMap.get("testCaseName"));
//        }
    }


    @Given("read test case list from google")
    public void readTestCaseListFromGoogle() throws GeneralSecurityException, IOException {

        String spreadsheetId = "1v4b21ceKj5dnxkPo_uDbHx4bf9GSgGLomyG4alsM8Hc";
        String range = "eraAutomationTestClaimList!A1:AL200";
        String credentialPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" +
                File.separator + "resources" + File.separator + "Downloads" + File.separator + "eraAutomation" +
                File.separator + "cred.json";

//        System.out.println("CREDENTIALS_FILE_PATH = " + CREDENTIALS_FILE_PATH);
        testCaseList = ExcelUtil.readGoogleSheetToListOfMap(spreadsheetId, range, credentialPath);
//        testCaseList.forEach(System.out::println);
//        for (Map<String, Object> testCase : testCaseList) {
//            System.out.println("testCase = " + testCase);
//            System.out.println("testCase.get(\"settings.era.primary_with_secondary_claim\") = " + testCase.get("settings.era.primary_with_secondary_claim"));
//        }

    }

    @Given("set Mongo and MySQL to beginning")
    public void setMongoAndMySQLToBeginning() {

        String[] queries = {
                "DELETE FROM claimpayment;",
                "DELETE FROM etrans835;",
                "DELETE FROM etrans835attach;",
                "DELETE FROM etrans;",
                "DELETE FROM etransmessagetext;",
                "DELETE FROM insbluebook;",
                "DELETE FROM adjustment;",
                "DELETE FROM retrace_change_log;",
                "UPDATE claimproc SET STATUS = 0, DedApplied = 0, InsPayAmt = 0, WriteOff = 0, AllowedOverride = 0, PaymentRow = 0, ClaimAdjReasonCodes = '', Remarks = '', ClaimPaymentNum = 0;",
                "UPDATE claim SET ClaimStatus = 'S', WriteOff = 0, InsPayAmt = 0 WHERE ClaimType = 'P';",
                "UPDATE claim SET ClaimStatus = 'H', WriteOff = 0, InsPayAmt = 0 WHERE ClaimType = 'S';"
        };

        sqlUtils.createConnection(mysqlIp, mysqlDbName, "root", "retrace123");
        sqlUtils.batchQueryExecuter(queries);


        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        try {
            // Connect to the MongoDB database
            MongoDatabase database = mongoClient.getDatabase(partner);

            String practiceFilter = "{practice_id: '" + practiceId + "'}";
            // Execute MongoDB update queries
//            MongoDBUtils.executeUpdateQuery(database, "claim_proc_1", practiceFilter,
//                    "{$set: {claim_payment_id: null, status: 'not-received', payment_row: 0, supp_received_date: null, allowed_override: null}}");
//
//            MongoDBUtils.executeUpdateQuery(database, "claim_1", "{claim_type: {$in: ['primary']}, practice_id: '"+practiceId+"' }",
//                    "{$set: {status: 'sent'}}");
//
//            MongoDBUtils.executeUpdateQuery(database, "claim_1", "{claim_type: {$in: ['secondary']}, practice_id: '"+practiceId+"'}",
//                    "{$set: {status: 'hold-until-pri-received'}}");

//            MongoDBUtils.executeUpdateQuery(database, "era_split_file_assigned", "{}",
//                    "{$set: {status: 'created', rejection_reasons: [], 'auto_post.approval': null, 'auto_post.approvalProvider': null, " +
//                            "'auto_post.user': null, 'auto_post.timestamp': null, 'auto_post.userProvider': null, 'auto_post.timestampProvider': null, auto_post_warning: []}," +
//                            "$unset: {etrans_message_text_id: ''}}");

            MongoDBUtils.executeDeleteQuery(database, "claim_payment_1", practiceFilter);
            MongoDBUtils.executeDeleteQuery(database, "opendental_writeback_1", "{}");
            MongoDBUtils.executeDeleteQuery(database, "opendental_writeback_archive_1", "{}");
            MongoDBUtils.executeDeleteQuery(database, "etrans835_1", practiceFilter);
//            MongoDBUtils.executeDeleteQuery(database, "ledger_1", "{category: {$in: ['disbursement', 'fees']}}");
//
//            MongoDBUtils.executeUpdateQuery(database, "ledger_1", "{category: 'inspay'}",
//                    "{$set: {status: 'reconciled', disburseTraceNumber: [], end_to_end_id: null, disburseDate: null}}");
//
//            MongoDBUtils.executeUpdateQuery(database, "ledger_1", "{}",
//                    "{$unset: {disburseDate: ''}}");

            System.out.println("Queries executed successfully.");
        } catch (Exception e) {
            logger.error("MongoRestore Error --> " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            System.out.println("Error resetting MongoDb");
        } finally {
            mongoClient.close();
        }

        ///////////////////////////////////////////////////////
        MongoClient mongoClient1 = MongoDBUtils.getMongoClient();

        Bson filter = eq("practice_id", practiceId);
        MongoCollection<Document> claim1Col = MongoDBUtils.connectMongodb(mongoClient1, partner, "claim_1");
        MongoCollection<Document> eraFile1Col = MongoDBUtils.connectMongodb(mongoClient1, partner, "era_file_1");
        MongoCollection<Document> treatmentPlan1Col = MongoDBUtils.connectMongodb(mongoClient1, partner, "treatment_plan_1");
        MongoCollection<Document> provider1Col = MongoDBUtils.connectMongodb(mongoClient1, partner, "provider_1");
        MongoCollection<Document> toothInitial1Col = MongoDBUtils.connectMongodb(mongoClient1, partner, "tooth_initial_1");

        MongoCollection<Document> task1Col = MongoDBUtils.connectMongodb(mongoClient1, partner, "task_1");
        MongoCollection<Document> ledger1Col = MongoDBUtils.connectMongodb(mongoClient1, partner, "ledger_1");
        MongoCollection<Document> eraSplitFileAssignedCol = MongoDBUtils.connectMongodb(mongoClient1, partner, "era_split_file_assigned");
        MongoCollection<Document> eraSplitClaimProc1Col = MongoDBUtils.connectMongodb(mongoClient1, partner, "era_split_claim_proc_1");
        MongoCollection<Document> writebackCol = MongoDBUtils.connectMongodb(mongoClient1, partner, "opendental_writeback_1");
        MongoCollection<Document> claimHistoryCol = MongoDBUtils.connectMongodb(mongoClient1, partner, "claim_history_1");
        MongoCollection<Document> hsbc820835Entry = MongoDBUtils.connectMongodb(mongoClient1, partner, "hsbc_820_835_entry");

        MongoCollection<Document> st835AssignedCol = MongoDBUtils.connectMongodb(mongoClient1, partner, "st_835_assigned");
        MongoCollection<Document> clp835AssignedCol = MongoDBUtils.connectMongodb(mongoClient1, partner, "clp_835_assigned");
        MongoCollection<Document> svc835AssignedCol = MongoDBUtils.connectMongodb(mongoClient1, partner, "svc_835_assigned");
        MongoCollection<Document> casSvc835AssignedCol = MongoDBUtils.connectMongodb(mongoClient1, partner, "cas_svc_835_assigned");

        MongoCollection<Document> file835Col = MongoDBUtils.connectMongodb(mongoClient1, "operations", "file_835");
        MongoCollection<Document> st835Col = MongoDBUtils.connectMongodb(mongoClient1, "operations", "st_835");
        MongoCollection<Document> clp835Col = MongoDBUtils.connectMongodb(mongoClient1, "operations", "clp_835");
        MongoCollection<Document> svc835Col = MongoDBUtils.connectMongodb(mongoClient1, "operations", "svc_835");
        MongoCollection<Document> casSvc835Col = MongoDBUtils.connectMongodb(mongoClient1, "operations", "cas_svc_835");
        MongoCollection<Document> hsbc820835S3ProcessedCol = MongoDBUtils.connectMongodb(mongoClient1, "operations", "hsbc_820_835_s3_processed");

        BasicDBObject hsbc820835S3Filter = BasicDBObject.parse("{ file_name: RegExp('Test_Case') }");

        Document practiceFilter = new Document("practice_id", practiceId);
        try {
            hsbc820835S3ProcessedCol.deleteMany(hsbc820835S3Filter);
            BrowserUtils.waitFor(0.5);

            hsbc820835Entry.deleteMany(new Document());
            BrowserUtils.waitFor(0.5);

            st835AssignedCol.deleteMany(new Document());
            BrowserUtils.waitFor(0.5);
            clp835AssignedCol.deleteMany(new Document());
            BrowserUtils.waitFor(0.5);
            svc835AssignedCol.deleteMany(new Document());
            BrowserUtils.waitFor(0.5);
            casSvc835AssignedCol.deleteMany(new Document());
            BrowserUtils.waitFor(0.5);

            file835Col.deleteMany(new Document());
            BrowserUtils.waitFor(0.5);
            st835Col.deleteMany(practiceFilter);
            BrowserUtils.waitFor(0.5);
            clp835Col.deleteMany(practiceFilter);
            BrowserUtils.waitFor(0.5);
            svc835Col.deleteMany(practiceFilter);
            BrowserUtils.waitFor(0.5);
            casSvc835Col.deleteMany(practiceFilter);
            BrowserUtils.waitFor(0.5);

            writebackCol.drop();
            BrowserUtils.waitFor(0.5);
            claim1Col.deleteMany(filter);
            BrowserUtils.waitFor(0.5);
//        eraFile1Col.deleteMany(filter);
            eraFile1Col.drop();
            BrowserUtils.waitFor(0.5);
            treatmentPlan1Col.deleteMany(filter);
            BrowserUtils.waitFor(0.5);
            provider1Col.deleteMany(filter);
            BrowserUtils.waitFor(0.5);
            toothInitial1Col.deleteMany(filter);
            BrowserUtils.waitFor(0.5);
//        task1Col.deleteMany(filter);
            task1Col.drop();
            BrowserUtils.waitFor(0.5);
            eraSplitClaimProc1Col.deleteMany(filter);
            BrowserUtils.waitFor(0.5);
            eraSplitFileAssignedCol.deleteMany(new Document());
            BrowserUtils.waitFor(0.5);
            ledger1Col.deleteMany(new Document());
            BrowserUtils.waitFor(0.5);
            claimHistoryCol.deleteMany(new Document());
            BrowserUtils.waitFor(0.5);

            String projectPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" +
                    File.separator + "resources" + File.separator + "Downloads" + File.separator + "eraAutomation";

            String filePath = projectPath + File.separator + partner + ".ledger_1.json";
            System.out.println("filePath = " + filePath);
            Document document = JSONUtils.JsonToBsonConverter(filePath);
            System.out.println("document.toJson() = " + document.toJson());
//            JsonPath jsonPath = JsonPath.from(document.toJson());
//            jsonPath.prettyPrint();
            ledger1Col.insertOne(document);


        } catch (Exception e) {
            logger.error("MongoRestore Error --> " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        } finally {
            mongoClient1.close();
        }

        //++++++++++++++++++++++++++++++++++++++//

        MongoClient mongoClient2 = MongoDBUtils.getMongoClient();
        Bson filter2 = eq("practice_id", "headquarters");

        MongoCollection<Document> task1ColHq = MongoDBUtils.connectMongodb(mongoClient2, partner, "task_1");
        MongoCollection<Document> eraFile1ColHq = MongoDBUtils.connectMongodb(mongoClient2, partner, "era_file_1");
        MongoCollection<Document> eraSplitClaimProc1ColHq = MongoDBUtils.connectMongodb(mongoClient2, partner, "era_split_claim_proc_1");

        try {
//        task1Col.deleteMany(filter);
            task1ColHq.drop();
            BrowserUtils.waitFor(1);
//        eraFile1Col.deleteMany(filter);
            eraFile1ColHq.drop();
            BrowserUtils.waitFor(1);
            eraSplitClaimProc1ColHq.deleteMany(filter2);
            BrowserUtils.waitFor(1);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("MongoRestore Error --> " + Arrays.toString(e.getStackTrace()));
        } finally {
            mongoClient2.close();
        }
    }

    @Given("restore Mysql")
    public void restoreMysql() {

        System.out.println(textColorUtils.ANSI_YELLOW_BACKGROUND + textColorUtils.ANSI_RED + "------------------Restore MySQL Started------------------" + textColorUtils.ANSI_RESET);
        String accessKey = ConfigurationReader.get("accessKeyStaging");
        String secretKey = ConfigurationReader.get("secretKeyStaging");
        String instanceId = "i-05fea3b9a69d91990";

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        AWSSimpleSystemsManagement ssmClient = AWSSimpleSystemsManagementClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();

        // Execute the command on the EC2 instance
        BrowserUtils.waitFor(5);
        executeCommandOnInstance(ssmClient, instanceId);
    }

    private static void executeCommandOnInstance(AWSSimpleSystemsManagement ssmClient, String instanceId) {
        // Replace with your command to execute the .bat file
        String command = "cmd.exe /c start /wait C:\\Users\\Administrator\\Desktop\\mysqlRestoreStaging.bat";


        SendCommandRequest sendCommandRequest = new SendCommandRequest()
                .withInstanceIds(instanceId)
                .withDocumentName("AWS-RunPowerShellScript")
                .withParameters(
                        new HashMap<String, List<String>>() {{
                            put("commands", Arrays.asList(command));
                        }}
                );

        SendCommandResult sendCommandResult = ssmClient.sendCommand(sendCommandRequest);
        String commandId = sendCommandResult.getCommand().getCommandId();

        // Monitor the command execution
        GetCommandInvocationRequest getCommandInvocationRequest = new GetCommandInvocationRequest()
                .withCommandId(commandId)
                .withInstanceId(instanceId);


        int maxRetries = 50;  // Set the maximum number of retries
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                GetCommandInvocationResult getCommandInvocationResult = ssmClient.getCommandInvocation(getCommandInvocationRequest);

                if (!getCommandInvocationResult.getStatus().equals("InProgress")) {
                    System.out.println("Command output: " + getCommandInvocationResult.getStandardOutputContent());
                    System.out.println("Command error: " + getCommandInvocationResult.getStandardErrorContent());
                    System.out.println("Command status: " + getCommandInvocationResult.getStatus());
                    BrowserUtils.waitFor(60);
                    break;
                }

                Thread.sleep(1000);
                retryCount++;
            } catch (InvocationDoesNotExistException e) {
                System.out.println("Invocation does not exist yet, retrying...");
                try {
                    Thread.sleep(2000); // Wait before retrying
                    retryCount++;
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }

            if (retryCount >= maxRetries) {
                BrowserUtils.waitFor(120);
                System.out.println("Reached maximum retry limit. Command status could not be confirmed.");
            }

        }
    }

    @Given("start full sync")
    public void startFullSync() {

        BrowserUtils.waitFor(20);
        System.out.println(apiUtils.ANSI_YELLOW_BACKGROUND + apiUtils.ANSI_RED + "------------------Opendental Fullsync Started------------------" + apiUtils.ANSI_RESET);
        apiUtils.startFullSync(partner, integrationId);
        BrowserUtils.waitFor(20);

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> integrationColl = MongoDBUtils.connectMongodb(mongoClient, partner, "integration_1");
        BasicDBObject query = BasicDBObject.parse("{integration_id:'" + integrationId + "'}");
//        System.out.println("query = " + query);
        Document integrationDoc = integrationColl.find(query).first();

        JsonPath jsonPath = JsonPath.from(integrationDoc.toJson());
        String status = jsonPath.getString("statuses.current_status");
        System.out.println("status = " + status);
        for (int i = 0; i < 200; i++) {
            if (!status.equals("change-syncing")) {
                BrowserUtils.waitFor(5);
                integrationColl = MongoDBUtils.connectMongodb(mongoClient, partner, "integration_1");
                integrationDoc = integrationColl.find(query).first();
                jsonPath = JsonPath.from(integrationDoc.toJson());
                status = jsonPath.getString("statuses.current_status");
                System.out.println("status = " + status);
            } else {
                mongoClient.close();
                break;
            }
        }
        BrowserUtils.waitFor(30);
    }

    @Given("delete old s3 bucket records")
    public void deleteOldS3BucketRecords() {
        s3Utils.deleteFilesFromS3(s3TestFolderName, s3BucketName);
        BrowserUtils.waitFor(5);
        s3Utils.deleteFilesFromS3("synthetic-chi/era_outbound/2025/", "retrace-synthetic-processing-cmteasxriap");
        BrowserUtils.waitFor(5);
        s3Utils.deleteFilesFromS3("synthetic-chi/inbound/", "retrace-synthetic-processing-cmteasxriap");
        BrowserUtils.waitFor(5);
        s3Utils.deleteFilesFromS3("synthetic-chi/outbound/2025/", "retrace-synthetic-processing-cmteasxriap");
        BrowserUtils.waitFor(5);
        s3Utils.deleteFilesFromS3("synthetic-chi/status_outbound/2025/", "retrace-synthetic-processing-cmteasxriap");
        BrowserUtils.waitFor(5);
    }

    @Given("submit all claims key= {string} value= {string}")
    public void submitAllClaimsKeyValue(String key, String value) {

        System.out.println(apiUtils.ANSI_YELLOW_BACKGROUND + apiUtils.ANSI_RED + "------------------Claim Submission Started------------------" + apiUtils.ANSI_RESET);
        String claimIDs = "";
        List<String> claimIdListCombined = new ArrayList<>();
        List<String> claimIdList = new ArrayList<>();
        List<String> claimIdListtoSubmit = new ArrayList<>();
        List<String> claimIdListZeroPaid = new ArrayList<>();
        List<String> claimIdListGeneratePartial = new ArrayList<>();

        boolean expectedValue = Boolean.parseBoolean(value);
        boolean testStepValue = false;
        boolean combineClaimsFromExcel = false;
        boolean isZeroPaidFromExcel = false;
        boolean generatePartialClaimFromExcel = false;
        for (Map<String, Object> testCase : testCaseList) {
//            System.out.println("testCase = " + testCase);
            testStepValue = Boolean.parseBoolean(testCase.get(key).toString());
            isZeroPaidFromExcel = Boolean.parseBoolean(testCase.get("zeroPaid").toString());
            generatePartialClaimFromExcel = Boolean.parseBoolean(testCase.get("generatePartialClaim").toString());
            combineClaimsFromExcel = Boolean.parseBoolean(testCase.get("combineClaims").toString());
//            System.out.println("testStepValue = " + testStepValue);
            if (!testStepValue && !isZeroPaidFromExcel && !generatePartialClaimFromExcel) {
                String claim = testCase.get("claimIdentifier").toString();

                if (claim.contains("~")) {

                    // Split the string by '~' and convert it to a list
                    List<String> resultList = Arrays.stream(claim.split("~"))
                            .collect(Collectors.toList());
//                    System.out.println(resultList);
                    for (String s : resultList) {
                        claim = s.substring(s.indexOf("/") + 1);
//            System.out.println("claim = " + claim);
                        claimIdList.add(claim);
                    }
                } else {
                    claim = claim.substring(claim.indexOf("/") + 1);
//            System.out.println("claim = " + claim);
                    claimIdList.add(claim);
                }
            } else if (testStepValue && !isZeroPaidFromExcel && !generatePartialClaimFromExcel) {
                String claim = testCase.get("claimIdentifier").toString();
                if (claim.contains("~")) {

                    // Split the string by '~' and convert it to a list
                    List<String> resultList = Arrays.stream(claim.split("~"))
                            .collect(Collectors.toList());
//                    System.out.println(resultList);
                    for (String s : resultList) {
                        claim = s.substring(s.indexOf("/") + 1);
//            System.out.println("claim = " + claim);
                        claimIdListCombined.add(claim);
                    }
                } else {
                    claim = claim.substring(claim.indexOf("/") + 1);
//            System.out.println("claim = " + claim);
                    claimIdListCombined.add(claim);
                }
            } else if (isZeroPaidFromExcel && !generatePartialClaimFromExcel) {
                String claim = testCase.get("claimIdentifier").toString();

                if (claim.contains("~")) {

                    // Split the string by '~' and convert it to a list
                    List<String> resultList = Arrays.stream(claim.split("~"))
                            .collect(Collectors.toList());
//                    System.out.println(resultList);
                    for (String s : resultList) {
                        claim = s.substring(s.indexOf("/") + 1);
//            System.out.println("claim = " + claim);
                        claimIdListZeroPaid.add(claim);
                    }
                } else {
                    claim = claim.substring(claim.indexOf("/") + 1);
//            System.out.println("claim = " + claim);
                    claimIdListZeroPaid.add(claim);
                }
            } else if (testStepValue && combineClaimsFromExcel && !isZeroPaidFromExcel) {
                String claim = testCase.get("claimIdentifier").toString();

                if (claim.contains("~")) {
                    // Split the string by '~' and convert it to a list
                    List<String> resultList = Arrays.stream(claim.split("~"))
                            .collect(Collectors.toList());
//                    System.out.println(resultList);
                    for (String s : resultList) {
                        claim = s.substring(s.indexOf("/") + 1);
//            System.out.println("claim = " + claim);
                        claimIdListGeneratePartial.add(claim);
                    }
                } else {
                    claim = claim.substring(claim.indexOf("/") + 1);
//            System.out.println("claim = " + claim);
                    claimIdListGeneratePartial.add(claim);
                }
            }


        }
//        System.out.println("claimIdList = " + claimIdList);
//        System.out.println("claimIdListCombined = " + claimIdListCombined);
//        System.out.println("claimIdListZeroPaid = " + claimIdListZeroPaid);

        boolean isKeyZeroPaid = key.equals("zeroPaid");
        boolean isKeyCombineClaims = key.equals("combineClaims");
        boolean isKeyGeneratePartial = key.equals("generatePartialClaim");
        if (!expectedValue && isKeyCombineClaims) {
            System.out.println("claimIdList = " + claimIdList);
            claimIdListtoSubmit = claimIdList;
            claimIDs = claimIdList.toString().replace("[", "").replace("]", "").replace(", ", ",");
        } else if (expectedValue && isKeyCombineClaims) {
            System.out.println("claimIdListCombined = " + claimIdListCombined);
            claimIdListtoSubmit = claimIdListCombined;
            claimIDs = claimIdListCombined.toString().replace("[", "").replace("]", "").replace(", ", ",");
        } else if (expectedValue && isKeyZeroPaid) {
            System.out.println("claimIdListZeroPaid = " + claimIdListZeroPaid);
            claimIdListtoSubmit = claimIdListZeroPaid;
            claimIDs = claimIdListZeroPaid.toString().replace("[", "").replace("]", "").replace(", ", ",");
        } else if (expectedValue && isKeyGeneratePartial) {
            System.out.println("claimIdListGeneratePartial = " + claimIdListGeneratePartial);
            claimIdListtoSubmit = claimIdListGeneratePartial;
            claimIDs = claimIdListGeneratePartial.toString().replace("[", "").replace("]", "").replace(", ", ",");
        }

//        claimIDs =  claimIdList.toString().replace("[", "").replace("]","").replace(", ",",");
//        System.out.println("claimIDs = " + claimIDs);


        System.out.println("claimIdListtoSubmit Size = " + claimIdListtoSubmit.size());
//        apiUtils.submitMultipleClaimsEraCycle(partner,practiceId,claimIDs,collectionName);
        apiUtils.submitClaimsEraCycle(partner, practiceId, claimIdListtoSubmit, collectionName);

    }

    @Given("submit all claims")
    public void submitAllClaims() {


        String claimIDs = "";
        List<String> claimIdList = new ArrayList<>();

        for (Map<String, Object> testCase : testCaseList) {
            boolean combineClaims = Boolean.parseBoolean(testCase.get("combineClaims").toString());
//            System.out.println("combineClaims = " + combineClaims);
            if (!combineClaims) {
                String claim = testCase.get("claimIdentifier").toString();
                if (claim.contains("~")) {

                    // Split the string by '~' and convert it to a list
                    List<String> resultList = Arrays.stream(claim.split("~"))
                            .collect(Collectors.toList());
//                    System.out.println(resultList);
                    for (String s : resultList) {
                        claim = s.substring(s.indexOf("/") + 1);
//            System.out.println("claim = " + claim);
                        claimIdList.add(claim);
                    }
                } else {
                    claim = claim.substring(claim.indexOf("/") + 1);
//            System.out.println("claim = " + claim);
                    claimIdList.add(claim);
                }
            }
//            else{
//                String claim = testCase.get("claimIdentifier").toString();
//                System.out.println("claim is partial = " + claim);
//            }

        }
        System.out.println("claimIdList = " + claimIdList);
        claimIDs = claimIdList.toString().replace("[", "").replace("]", "").replace(", ", ",");
        System.out.println("claimIDs = " + claimIDs);

//        claimIDs = "535,536";

        apiUtils.submitMultipleClaimsEraCycle(partner, practiceId, claimIDs, collectionName);

    }

    @Given("run synthetic clearinghouse script")
    public void runSyntheticClearinghouseScript() {

        String ip = serverIp;
        String script = "node ./background/synthetic-clearinghouse.js --cat P0 --sts 1 --interface-id hsbcdev " +
                "--interchange-name synthetic --interchange-env dev";

        sshUtils.runScriptsLogToFile(script, ip, "syntheticClearingHouse", testCasesDB);
//        BrowserUtils.waitFor(20);

    }

    @Given("run synthetic clearinghouse script for partial claims")
    public void runSyntheticClearinghouseScriptForPartialClaims() {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> partialEraColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
        BasicDBObject query = BasicDBObject.parse("{combineClaims:true, isPartialClaim:false}");

        FindIterable<Document> iterDoc = partialEraColl.find(query);
        MongoCursor<Document> resultList = iterDoc.iterator();

        String ip = serverIp;
        Gson gson = new Gson();
        while (resultList.hasNext()) {
            String name837s = "";
            Document myDoc = resultList.next();
            Map<String, Object> mapInfo = gson.fromJson(myDoc.toJson(), Map.class);
            List<String> fileNames = (List<String>) mapInfo.get("fileName837");
//            System.out.println("fileNames = " + fileNames);
            for (String fileName : fileNames) {
                name837s = name837s + fileName + " ";
            }
            System.out.println("name837s = " + name837s);

            if (fileNames.size() > 1) {

                String script = "node ./background/synthetic-clearinghouse.js --combine-claims --all-matched" +
                        " --file " + name837s +
                        " --cat F0 --sts 1";

                System.out.println("script = " + script);
                sshUtils.runScriptsLogToFile(script, ip, "syntheticClearingHouseForCombinedClaims", testCasesDB);
//                sshUtils.runScripts(script,ip);

//                int name835Index = fileNames.size();
//                String file835Name = fileNames.get(name835Index-1);
//                System.out.println("file837Name = " + file835Name);
//
//                Document filter = new Document("fileName837", file835Name);
//
//                file835Name = file835Name.replace(".837",".835");
//                System.out.println("file835Name = " + file835Name);
//
//                Document update = new Document()
//                        .append("$set", new Document("fileName835", file835Name));
//                partialEraColl.updateOne(filter,update);
//                MongoDBUtils.executeUpdateQuery(partialEraColl,filter,update);
            }
        }
        mongoClient.close();
//        BrowserUtils.waitFor(20);
    }


    @Given("run synthetic clearinghouse script for ZeroPaid claims")
    public void runSyntheticClearinghouseScriptForZeroPaidClaims() {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> partialEraColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
        BasicDBObject query = BasicDBObject.parse("{isZeroPaid:true, isPartialClaim:false}");

        FindIterable<Document> iterDoc = partialEraColl.find(query);
        MongoCursor<Document> resultList = iterDoc.iterator();


        String ip = serverIp;
        Gson gson = new Gson();

        while (resultList.hasNext()) {
            String name837s = "";
            Document myDoc = resultList.next();
            Map<String, Object> mapInfo = gson.fromJson(myDoc.toJson(), Map.class);
            List<String> fileNames = (List<String>) mapInfo.get("fileName837");
//            System.out.println("fileNames = " + fileNames);
            for (String fileName : fileNames) {
                name837s = name837s + fileName + " ";
            }
//            System.out.println("name837s = " + name837s);

            if (fileNames.size() > 0) {
                // node ./background/synthetic-clearinghouse.js --cat F2 --sts 1 --interface-id hsbcdev --interchange-name synthetic --interchange-env dev
                String script = "node ./background/synthetic-clearinghouse.js" +
                        " --file " + name837s +
                        " --interface-id hsbcdev --interchange-name synthetic --interchange-env dev" +
                        " --cat F2 --sts 1";

                System.out.println("script = " + script);
                sshUtils.runScriptsLogToFile(script, ip, "syntheticClearingHouseForZeropaid", testCasesDB);
            }

//
//            int name835Index = fileNames.size();
//            String file835Name = fileNames.get(name835Index-1);
//            System.out.println("file837Name = " + file835Name);
//
//            Document filter = new Document("fileName837", file835Name);
//
//            file835Name = file835Name.replace(".837",".835");
//            System.out.println("file835Name = " + file835Name);
//
//            Document update = new Document()
//                    .append("$set", new Document("fileName835", file835Name));
////            partialEraColl.updateOne(filter,update);
//            MongoDBUtils.executeUpdateQuery(partialEraColl,filter,update);

        }
        mongoClient.close();
//        BrowserUtils.waitFor(20);
    }

    @Given("run synthetic clearinghouse script for generatePartial claims")
    public void runSyntheticClearinghouseScriptForGeneratePartialClaims() {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> partialEraColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
        BasicDBObject query = BasicDBObject.parse("{combineClaims:true, isPartialClaim:true}");

        FindIterable<Document> iterDoc = partialEraColl.find(query);
        MongoCursor<Document> resultList = iterDoc.iterator();

        String ip = serverIp;
        Gson gson = new Gson();
        while (resultList.hasNext()) {
            String name837s = "";
            Document myDoc = resultList.next();
            Map<String, Object> mapInfo = gson.fromJson(myDoc.toJson(), Map.class);
            List<String> fileNames = (List<String>) mapInfo.get("fileName837");
//            System.out.println("fileNames = " + fileNames);
            for (String fileName : fileNames) {
                name837s = name837s + fileName + " ";
            }
            System.out.println("name837s = " + name837s);

            if (fileNames.size() > 1) {

                String script = "node ./background/synthetic-clearinghouse.js --combine-claims" +
                        " --file " + name837s +
                        " --cat F0 --sts 1";

                System.out.println("script = " + script);
                sshUtils.runScriptsLogToFile(script, ip, "syntheticClearingHouseForGeneratePartial", testCasesDB);
            }
        }
        mongoClient.close();
//        BrowserUtils.waitFor(20);
    }

    @Given("get 837 file list from sftp")
    public void get837FileListFromSftp() {
        String filePath = "/synthetic-chi/inbound";

//        List<String> newFileNameList837 = new ArrayList<>();
        List<String> newFileNameList837 = sshUtils.sftpGetFileName837(filePath);
        fileNameList837.addAll(newFileNameList837);

        for (String s : newFileNameList837) {
            System.out.println("837 file name = " + s);
        }
        System.out.println("Total " + newFileNameList837.size() + " - 837 file/s exists in sftp inbound folder");
    }

    @Given("download 820 files")
    public void download820Files() {
        String filePath = "/hsbc-chi/pymt_outbound";
        String downloadPath = BrowserUtils.getDownloadPath();

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> partialEraColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
        BasicDBObject query = BasicDBObject.parse("{combineClaims:false, fileName835:{$ne:null}}");

        FindIterable<Document> iterDoc = partialEraColl.find(query);
        MongoCursor<Document> resultList = iterDoc.iterator();
        List<String> fileNameList835 = new ArrayList<>();

        while (resultList.hasNext()) {
            Document myDoc = resultList.next();
            String file835Name = myDoc.getString("fileName835");
            System.out.println("file835Name from resultList= " + file835Name);
            fileNameList835.add(file835Name);
        }

        Assert.assertTrue("There is NO 835 file generated for this Cycle", fileNameList835.size() > 0);

        for (String fileName835 : fileNameList835) {
            System.out.println("fileName835 to replace 820= " + fileName835);
            String fileName820 = "CDT04FU8.RETRACE.EDI_" + fileName835.replace(".835", ".820");
            System.out.println("fileName820 = " + fileName820);
//            System.out.println("fileName820 = " + fileName820);
            String filter = "{fileName835:'" + fileName835 + "'}";
            String update = "{$set:{fileName820:'" + fileName820 + "'}}";
            MongoDBUtils.executeUpdateQuery(partialEraColl, filter, update);

            sshUtils.download820FromSftp(filePath, fileName820, downloadPath, collectionName);
            downloaded820NameList.add(fileName820);
        }

        mongoClient.close();
        System.out.println("downloaded820NameList = " + downloaded820NameList);
    }


    @Given("download 820 files for zeroPaid claims")
    public void download820FilesForZeroPaidClaims() {
        String filePath = "/hsbc-chi/pymt_outbound";
        String downloadPath = BrowserUtils.getDownloadPath();

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> partialEraColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
        BasicDBObject query = BasicDBObject.parse("{isZeroPaid:true, fileName835:{$ne:null}}");

        FindIterable<Document> iterDoc = partialEraColl.find(query);
        MongoCursor<Document> resultList = iterDoc.iterator();
        List<String> fileNameList835 = new ArrayList<>();

        while (resultList.hasNext()) {
            Document myDoc = resultList.next();
            String file835Name = myDoc.getString("fileName835");
            System.out.println("file835Name from resultList= " + file835Name);
            fileNameList835.add(file835Name);
        }


        for (String fileName835 : fileNameList835) {
            System.out.println("fileName835 to replace 820= " + fileName835);
            String fileName820 = "CDT04FU8.RETRACE.EDI_" + fileName835.replace(".835", ".820");
            System.out.println("fileName820 = " + fileName820);

            String filter = "{fileName835:'" + fileName835 + "'}";
            String update = "{$set:{fileName820:'" + fileName820 + "'}}";
            MongoDBUtils.executeUpdateQuery(partialEraColl, filter, update);

            sshUtils.download820FromSftp(filePath, fileName820, downloadPath, collectionName);
            downloaded820NameList.add(fileName820);
        }
        mongoClient.close();
        System.out.println("downloaded820NameList = " + downloaded820NameList);
    }

    @Given("download 820 files for partial claims")
    public void download820FilesForPartialClaims() {
        String filePath = "/hsbc-chi/pymt_outbound";
        String downloadPath = BrowserUtils.getDownloadPath();

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> partialEraColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
        BasicDBObject query = BasicDBObject.parse("{isPartialClaim:true, fileName835:{$ne:null}}");

        FindIterable<Document> iterDoc = partialEraColl.find(query);
        MongoCursor<Document> resultList = iterDoc.iterator();
        List<String> fileNameList835 = new ArrayList<>();

        while (resultList.hasNext()) {
            Document myDoc = resultList.next();
            String file835Name = myDoc.getString("fileName835");
            System.out.println("file835Name from resultList= " + file835Name);
            fileNameList835.add(file835Name);
        }


        for (String fileName835 : fileNameList835) {
            System.out.println("fileName835 to replace 820= " + fileName835);
            String fileName820 = "CDT04FU8.RETRACE.EDI_" + fileName835.replace(".835", ".820");
            System.out.println("fileName820 = " + fileName820);

            String filter = "{fileName835:'" + fileName835 + "'}";
            String update = "{$set:{fileName820:'" + fileName820 + "'}}";
            MongoDBUtils.executeUpdateQuery(partialEraColl, filter, update);

            sshUtils.download820FromSftp(filePath, fileName820, downloadPath, collectionName);
            downloaded820NameList.add(fileName820);
        }
        mongoClient.close();
        System.out.println("downloaded820NameList = " + downloaded820NameList);
    }

    @Given("download 820 files for combined claims")
    public void download820FilesForCombinedClaims() {
        String filePath = "/hsbc-chi/pymt_outbound";
        String downloadPath = BrowserUtils.getDownloadPath();

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> partialEraColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
        BasicDBObject query = BasicDBObject.parse("{combineClaims:true, fileName835:{$ne:null}}");

        FindIterable<Document> iterDoc = partialEraColl.find(query);
        MongoCursor<Document> resultList = iterDoc.iterator();
        List<String> fileNameList835 = new ArrayList<>();

        while (resultList.hasNext()) {
            Document myDoc = resultList.next();
            String file835Name = myDoc.getString("fileName835");
            System.out.println("file835Name from resultList= " + file835Name);
            fileNameList835.add(file835Name);
        }


        for (String fileName835 : fileNameList835) {
            System.out.println("fileName835 to replace 820= " + fileName835);
            String fileName820 = "CDT04FU8.RETRACE.EDI_" + fileName835.replace(".835", ".820");
            System.out.println("fileName820 = " + fileName820);
//            System.out.println("fileName820 = " + fileName820);
            String filter = "{fileName835:'" + fileName835 + "'}";
            String update = "{$set:{fileName820:'" + fileName820 + "'}}";
            MongoDBUtils.executeUpdateQuery(partialEraColl, filter, update);

            sshUtils.download820FromSftp(filePath, fileName820, downloadPath, collectionName);
            downloaded820NameList.add(fileName820);
        }
        mongoClient.close();
        System.out.println("downloaded820NameList = " + downloaded820NameList);
    }

    @Given("upload 820 files to S3 and server")
    public void upload820FilesToS3AndServer() {
        String downloadPath = BrowserUtils.getDownloadPath();
        String ip = serverIp;

        sshUtils.uploadFileToServer(ip, downloaded820NameList, server820UploadPath, testCaseName);

        for (String file820Name : downloaded820NameList) {
            s3Utils.upload820ToS3(file820Name, s3TestFolderName, s3BucketName, collectionName);
//            String fullPath = downloadPath+File.separator+file820Name;
//            System.out.println("fullPath for delete files = " + fullPath);
//            try {
//                Path filePath = Paths.get(fullPath);
//                Files.delete(filePath);
//                System.out.println(fullPath+" --> Deleted");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }


    @Given("import test case info to mongodb")
    public void importTestCaseInfoToMongodb() {
        String databaseName = testCasesDB;
        System.out.println("collectionName = " + collectionName);
        MongoCollection<Document> collection = null;
        try (MongoClient mongoClient = MongoDBUtils.getMongoClient()) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            collection = database.getCollection(collectionName);
//            BasicDBObject query = BasicDBObject.parse("{_id:{$exists:true}, practice_id:'"+practice+"'}");
            // Delete the collection if it exists
            if (collection != null) {
                collection.deleteMany(new Document());
                System.out.println("All documents deleted from collection '" + collectionName + "'.");
            }
            // Create the collection
            database.createCollection(collectionName);
            System.out.println("Collection '" + collectionName + "' created successfully.");

            for (Map<String, Object> data : testCaseList) {
                Document document = new Document(data);

//                Set<String> keySet =  data.keySet();
//                System.out.println("keySet = " + keySet);
//                System.out.println("data.keySet() = " + data.keySet());
//                for (String s : keySet) {
//                    System.out.println(s);
//                }
//                System.out.println("document.toJson() = " + document.toJson());
                String testCaseName = (String) document.get("testCaseName");
                String claimIdentifier = (String) document.get("claimIdentifier");
//                System.out.println("claimIdentifier = " + claimIdentifier);
                String testId = (String) document.get("testId");
                String automationSettings = (String) document.get("automationSettings");
                String purpose = (String) document.get("purpose");
                String expectedResult = (String) document.get("expectedResult");
                String patient = claimIdentifier.substring(0, claimIdentifier.indexOf("/"));
//                String claim = claimIdentifier.substring(claimIdentifier.indexOf("/")+1);
//                String approve_auto_post_ortho_claims_automatically = document.getString("settings.era.approve_auto_post_ortho_claims_automatically");
//                String auto_post_plbs = document.getString("settings.era.auto_post_plbs");
//                String	approve_preauth_auto_post_automatically	=	document.getString("settings.era.approve_preauth_auto_post_automatically");
//                String	settings.era.approve_preauth_auto_post_automatically	=	document.getString(	"	settings.era.approve_preauth_auto_post_automatically	"	);

                String approve_auto_post_0_pay_claims_automatically = document.getString("settings.era.approve_auto_post_0_pay_claims_automatically");
                String approve_auto_post_automatically = document.getString("settings.era.approve_auto_post_automatically");
                String approve_auto_post_ortho_claims_automatically = document.getString("settings.era.approve_auto_post_ortho_claims_automatically");
                String approve_auto_post_secondary_claim_automatically = document.getString("settings.era.approve_auto_post_secondary_claim_automatically");
                String approve_partial_payment_era_writeback_automatically = document.getString("settings.era.approve_partial_payment_era_writeback_automatically");
                String approve_preauth_auto_post_automatically = document.getString("settings.era.approve_preauth_auto_post_automatically");
                String global_denial_location_preference = document.getString("global_denial_location_preference");
                String global_denial_behavior_general = document.getString("global_denial_behavior_general");
                String global_denial_behavior_custom_qualification = document.getString("global_denial_behavior_custom_qualification");
                String global_denial_behavior_custom_task = document.getString("global_denial_behavior_custom_task");
                String practice_denial_behavior_general = document.getString("practice_denial_behavior_general");
                String practice_denial_behavior_custom_qualification = document.getString("practice_denial_behavior_custom_qualification");
                String practice_denial_behavior_custom_task = document.getString("practice_denial_behavior_custom_task");
                String auto_post_ortho_claims = document.getString("settings.era.auto_post_ortho_claims");
                String auto_post_plbs = document.getString("settings.era.auto_post_plbs");
                String auto_post_secondary_claim = document.getString("settings.era.auto_post_secondary_claim");
                String internal_auto_approve = document.getString("settings.internal_auto_approve");
                String negative_patient_responsibility = document.getString("settings.negative_patient_responsibility");
                String collect_fees_on_potential_auto_post = document.getString("settings.era.collect_fees_on_potential_auto_post");
                String enable_auto_post = document.getString("settings.era.enable_auto_post");
                String era_writeback_file_path = document.getString("settings.era.era_writeback_file_path");
                String allow_partial_clps = document.getString("settings.allow_partial_clps");
                String set_claim_to_received_for_partial_clps = document.getString("settings.set_claim_to_received_for_partial_clps");
                String multi_practice_split_era_auto_post = document.getString("settings.era.multi_practice_split_era_auto_post");
                String partial_payment_era_writeback = document.getString("settings.era.partial_payment_era_writeback");
                String preauth_auto_post = document.getString("settings.era.preauth_auto_post");
                String primary_with_secondary_claim = document.getString("settings.era.primary_with_secondary_claim");
                String plb_adjustment = document.getString("settings.plb_adjustment");
                String combineClaims = document.getString("combineClaims");
                String isZeroPaid = document.getString("zeroPaid");
                String isPartialClaim = document.getString("generatePartialClaim");
                String isTestCaseReady = document.getString("isTestCaseReady");


                Document doc2 = new Document("testCaseName", testCaseName)
                        .append("eraCycleCount", eraCycleCountStr)
                        .append("isTestCaseHealthy", null)
                        .append("isTestCaseReady", Boolean.parseBoolean(isTestCaseReady))
                        .append("testHealthErrorList", Collections.emptyList())
                        .append("claimIdentifier", claimIdentifier)
                        .append("combineClaims", Boolean.parseBoolean(combineClaims))
                        .append("isZeroPaid", Boolean.parseBoolean(isZeroPaid))
                        .append("isPartialClaim", Boolean.parseBoolean(isPartialClaim))
                        .append("testStatus", null)
                        .append("errorLog", null)
                        .append("fileName837", Collections.emptyList())
                        .append("file837Content", Collections.emptyList())
                        .append("checkNumber", null)
                        .append("fileName820", null)
                        .append("fileName835", null)
                        .append("file835Content", null)
                        .append("claimSubmitSuccess", "notSubmittedYet")
                        .append("download820Success", "notDownloadedYet")
                        .append("upload820ToS3Success", "notUploadedYet")
                        .append("isAutoPostQualified", null)
                        .append("isPotentialAutoPostQualified", null)
                        .append("ledgerId", null)
                        .append("eraSplitFileId", null)
                        .append("matchingBpr835820", null)
                        .append("tinMismatch", null)
                        .append("createdAt", new Date())
                        .append("updatedAt", null)
                        .append("purpose", purpose)
                        .append("expectedResult", expectedResult)
                        .append("testId", testId)
                        .append("automationSettings", automationSettings)
                        .append("partner", partner)
                        .append("practiceId", practiceId)
                        .append("practiceName", practiceName)
                        .append("s3Bucket", s3BucketName)
                        .append("s3TestFolderName", s3TestFolderName)
                        .append("mysqlIp", mysqlIp)
                        .append("mysqlDbName", mysqlDbName)
                        .append("integrationId", integrationId)
                        .append("patient", patient)
                        .append("s3TestFolderName", s3TestFolderName)
                        .append("collectionName", collectionName)
                        .append("dfiAccountId", dfiAccountId)
                        .append("serverIp", serverIp)
                        .append("server820UploadPath", server820UploadPath)
                        .append("settings", new Document("approve_auto_post_ortho_claims_automatically", Boolean.parseBoolean(approve_auto_post_ortho_claims_automatically))
                                .append("approve_auto_post_0_pay_claims_automatically", Boolean.parseBoolean(approve_auto_post_0_pay_claims_automatically))
                                .append("approve_auto_post_automatically", Boolean.parseBoolean(approve_auto_post_automatically))
                                .append("approve_auto_post_secondary_claim_automatically", Boolean.parseBoolean(approve_auto_post_secondary_claim_automatically))
                                .append("approve_partial_payment_era_writeback_automatically", Boolean.parseBoolean(approve_partial_payment_era_writeback_automatically))
                                .append("approve_preauth_auto_post_automatically", Boolean.parseBoolean(approve_preauth_auto_post_automatically))
                                .append("auto_post_ortho_claims", Boolean.parseBoolean(auto_post_ortho_claims))
                                .append("auto_post_plbs", Boolean.parseBoolean(auto_post_plbs))
                                .append("auto_post_secondary_claim", Boolean.parseBoolean(auto_post_secondary_claim))
                                .append("internal_auto_approve", Boolean.parseBoolean(internal_auto_approve))
                                .append("collect_fees_on_potential_auto_post", Boolean.parseBoolean(collect_fees_on_potential_auto_post))
                                .append("enable_auto_post", Boolean.parseBoolean(enable_auto_post))
                                .append("era_writeback_file_path", Boolean.parseBoolean(era_writeback_file_path))
                                .append("allow_partial_clps", Boolean.parseBoolean(allow_partial_clps))
                                .append("set_claim_to_received_for_partial_clps", Boolean.parseBoolean(set_claim_to_received_for_partial_clps))
                                .append("multi_practice_split_era_auto_post", Boolean.parseBoolean(multi_practice_split_era_auto_post))
                                .append("partial_payment_era_writeback", Boolean.parseBoolean(partial_payment_era_writeback))
                                .append("preauth_auto_post", Boolean.parseBoolean(preauth_auto_post))
                                .append("primary_with_secondary_claim", primary_with_secondary_claim)
                                .append("negative_patient_responsibility", negative_patient_responsibility)
                                .append("plb_adjustment", Boolean.parseBoolean(plb_adjustment))
                        )
                        .append("denial_partner_settings", new Document("denial_location_preference", global_denial_location_preference)
                                .append("denial_behavior_general", global_denial_behavior_general)
                                .append("denial_behavior_custom_qualification", global_denial_behavior_custom_qualification)
                                .append("denial_behavior_custom_task", global_denial_behavior_custom_task)
                        )
                        .append("denial_practice_settings", new Document("denial_behavior_general", practice_denial_behavior_general)
                                .append("denial_behavior_custom_qualification", practice_denial_behavior_custom_qualification)
                                .append("denial_behavior_custom_task", practice_denial_behavior_custom_task)
                );
//                System.out.println("doc2.toJson() = " + doc2.toJson());
                collection.insertOne(doc2);

            }
            System.out.println("All Documents Inserted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }

    }

    @Given("find 837 name with matching claim")
    public void find837NameWithMatchingClaim() {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        for (String fileName : fileNameList837) {
            System.out.println("fileName = " + fileName);
            String filePath = "/synthetic-chi/inbound";
            String fileContent837 = "File not Written";
            try {
                fileContent837 = sshUtils.readFile(filePath, fileName);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(fileName + " read error --> " + Arrays.toString(e.getStackTrace()));
            }
            System.out.println("fileContent837 = " + fileContent837);
            if (fileContent837.length() < 18) {
                String errorMessage = "837 file content not looks well, there is problem with " + fileName +
                        "\nThis file dismissed and continued with next one!!!";
                System.out.println("errorMessage = " + errorMessage);
                logger.error(fileName + " --> " + errorMessage);
                break;
            }
            int clmIndex = fileContent837.indexOf("~CLM*") + 5;
            String clmStr = fileContent837.substring(clmIndex);
//            System.out.println("clmStr = " + clmStr);
            clmStr = clmStr.substring(0, clmStr.indexOf("~"));
//            System.out.println("clmStr 2= " + clmStr);
            String claimIdentifier837 = clmStr.substring(0, clmStr.indexOf("+"));
//            System.out.println("claimIdentifier837 = " + claimIdentifier837);
//            String filter = "{claimIdentifier:'"+claimIdentifier837+"'}";
            String filterStr = "{claimIdentifier:{$regex:'" + claimIdentifier837 + "'}}";
            Document filter = Document.parse(filterStr);
            BasicDBObject query = BasicDBObject.parse(filterStr);
//            System.out.println("query = " + query);
            MongoCollection<Document> eraTestColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
            Document document = eraTestColl.find(query).first();

            if (document != null) {
                System.out.println(fileName + " matched to claim = " + claimIdentifier837);
//                System.out.println("documenJson() = " + document.toJson());
                Document update = new Document()
                        .append("$addToSet", new Document("fileName837", fileName)
                                .append("file837Content", fileContent837));
//                String update = "{$addToset:{fileName837:'"+fileName+"', file837Content:'"+fileContent837+"'}}";
                MongoDBUtils.executeUpdateQuery(eraTestColl, filter, update);
//                System.out.println("fileName = " + fileName);
//                System.out.println("fileContent837 = " + fileContent837);
            }
            System.out.println("---------------------------------------");
        }
        mongoClient.close();
    }

    Document testDoc = new Document();
    String claimIdentifier = "";
    String testCaseName = "";
    List<String> fileName837List = new ArrayList<>();
    List<String> file837ContentList = new ArrayList<>();
    String fileName835 = "";
    String automationSettings = "";
    String checkNumber = "";
    String reference = "";
    String testId = "";

    @Given("get {string} data for {string} from Mongodb")
    public void getDataForFromMongodb(String key, String value) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        BasicDBObject query = BasicDBObject.parse("{" + key + ":'" + value + "'}");
//        System.out.println("query = " + query);
        MongoCollection<Document> eraTestColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
        testDoc = eraTestColl.find(query).first();
//        System.out.println("testDoc.toJson() = " + testDoc.toJson());
        JsonPath testDocJson = JsonPath.from(testDoc.toJson());

//        String errorStr = testDocJson.get("testHealthErrorList").toString();
//        Boolean isTestCaseHealthy = testDoc.getBoolean("isTestCaseHealthy");
        Boolean isTestCaseHealthy = testDoc.get("isTestCaseHealthy") != null
                ? testDoc.getBoolean("isTestCaseHealthy")
                : null;

        boolean isRestoreInprogress = eraCycleUtils.getRestoreStatus();
//        System.out.println("isRestoreInprogress = " + isRestoreInprogress);
        while (isRestoreInprogress) {
            BrowserUtils.waitFor(20);
            isRestoreInprogress = eraCycleUtils.getRestoreStatus();
        }

        logUtils.assertNotNull(isTestCaseHealthy, textColorUtils.ANSI_YELLOW_BACKGROUND + textColorUtils.ANSI_RED +
                "!!!!!Test Case NOT healty Test STOPPED!!!!!" + textColorUtils.ANSI_RESET);
        logUtils.assertTrue(isTestCaseHealthy, textColorUtils.ANSI_YELLOW_BACKGROUND + textColorUtils.ANSI_RED +
                "!!!!!Test Case NOT healty Test STOPPED!!!!!" + textColorUtils.ANSI_RESET);

//        try {
//            Assert.assertTrue(textColorUtils.ANSI_YELLOW_BACKGROUND + textColorUtils.ANSI_RED +
//                    "!!!!!Test Case NOT healty Test STOPPED!!!!!" + textColorUtils.ANSI_RESET, isTestCaseHealthy);
//        } catch (AssertionError e) {
//            System.out.println("errorStr = " + errorStr);
//            logger.error("!!!!!Test Case NOT healty Test STOPPED!!!!! Error --> " + errorStr + Arrays.toString(e.getStackTrace()));
//            throw e;
//        }
//        System.out.println("testDoc.toJson() = " + testDoc.toJson());

        claimIdentifier = testDoc.getString("claimIdentifier");
        testCaseName = testDoc.getString("testCaseName").replace(" ", "_");
        fileName837List = (List<String>) testDoc.get("fileName837");
        file837ContentList = (List<String>) testDoc.get("file837Content");
//        automationSettings = testDoc.getString("automationSettings");
        checkNumber = testDoc.getString("checkNumber");
        reference = checkNumber;
        fileName835 = testDoc.getString("fileName835");
        testId = testDoc.getString("testId");


        System.out.println("isTestCaseHealthy = " + isTestCaseHealthy);
        System.out.println("fileName835 = " + fileName835);
        System.out.println("checkNumber = " + checkNumber);
        System.out.println("claimIdentifier = " + claimIdentifier);
        System.out.println("testCaseName = " + testCaseName);
        System.out.println("fileName837List = " + fileName837List);
        System.out.println("file837Content = " + file837ContentList);
//        System.out.println("automationSettings = " + automationSettings);
        System.out.println("testId = " + testId);
    }

    @And("run check clearinghouseftp script")
    public void runCheckClearinghouseftpScript() {
//        fileName837 = "2024071009059684149.837";
//        String fileName835 = fileName837.replace(".837",".835");
        String ip = serverIp;
        String script = "node ./background/checkClearinghouseFtp.js --clearinghouse synthetic --env dev --transaction 835 " +
                "--partner-id " + partner + " --file " + fileName835;
        System.out.println("script = " + script);
        for (int i = 0; i < 200; i++) {
            if (!MongoDBUtils.getSingletonSettings()) {
                System.out.println("----started for singleton script run checkclearingHouseFtp----");
                if (!MongoDBUtils.getSingletonSettings()) { // check last time and then start
                    MongoDBUtils.pushAndKeepSettings(partner, practiceId, logger, testCaseName, script, testCasesDB);
                    MongoDBUtils.deleteSingletonRecord("checkClearinghouseFtp");
                    int exitStatus = sshUtils.runScriptsLogToFile(script, ip, testCaseName + "_checkClearingHouseFtp", testCasesDB);
                    if (exitStatus != 0) {
                        String filePath_CheckClearingHouse = "src/test/resources/Logs/" + testCaseName + "_checkClearingHouseFtp.log";
                        System.out.println("filePath_CheckClearingHouse = " + filePath_CheckClearingHouse);
                        String checkClearingHouseScriptLog = fileUtils.readLogContent(filePath_CheckClearingHouse);
                        System.out.println("checkClearingHouseScriptLog = " + checkClearingHouseScriptLog);
                        String singletonError = "Error: Another process is running, job is not executed";
                        if (checkClearingHouseScriptLog.contains(singletonError)) {
                            System.out.println("singletonError = " + singletonError);
                            System.out.println("------- Script run started from beginning ------- = ");
                            continue;
                        } else {
                            logUtils.assertEquals("checkClearingHouseFtp run NOT Success!!! \n " +
                                    "Error is not Singleton Error \n " + script, 0, exitStatus);
//                            break;
                        }
                    } else {
                        logUtils.assertEquals("checkClearingHouseFtp run NOT Success!!! \n" + script, 0, exitStatus);
//                        break;
                    }
                }
                BrowserUtils.waitFor(0.5);
                MongoDBUtils.setSingletonSettings(false, "notKept", "none", testCasesDB);
                BrowserUtils.waitFor(1);
                eraCycleUtils.updateTestCaseDoc(testId, "testStatus", "checkClearinghouseFtp script run", testCasesDB);
                break;
            } else {
                System.out.println("----waiting for singleton script run checkclearingHouseFtp----");
                BrowserUtils.waitFor(3);
            }
        }
        try {
            boolean settingsMatched = eraCycleUtils.isAutomationSettingsMatching(partner, practiceId, testCasesDB, testCaseName);
            System.out.println("settingsMatched before checkClearinghouseFtp = " + settingsMatched);
        } catch (AssertionError e) {
            boolean settingsMatchedCatchBlok = eraCycleUtils.isAutomationSettingsMatching(partner, practiceId, testCasesDB, testCaseName);
            System.out.println("settingsMatchedCatchBlok = " + settingsMatchedCatchBlok);
            throw e;  // Rethrow the exception so the test still fails
        }
    }


    @And("run check clearinghouseftp script for {string}")
    public void runCheckClearinghouseftpScriptFor(String testCaseName) {
//        fileName837 = "2024071009059684149.837";
//        String fileName835 = fileName837.replace(".837",".835");
        String fileName835 = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("fileName835");
        String ip = serverIp;
        String script = "node ./background/checkClearinghouseFtp.js --clearinghouse synthetic --env dev --transaction 835 " +
                "--partner-id " + partner + " --file " + fileName835;
        System.out.println("script = " + script);
        testCaseName = testCaseName.replace(" ", "_");
        for (int i = 0; i < 200; i++) {
            if (!MongoDBUtils.getSingletonSettings()) {
                System.out.println("----started for singleton script run checkclearingHouseFtp----");
                if (!MongoDBUtils.getSingletonSettings()) { // check last time and then start --race condition
                    MongoDBUtils.pushAndKeepSettings(partner, practiceId, logger, testCaseName, script, testCasesDB);
                    MongoDBUtils.deleteSingletonRecord("checkClearinghouseFtp");
                    int exitStatus = sshUtils.runScriptsLogToFile(script, ip, testCaseName + "_checkClearingHouseFtp", testCasesDB);
                    if (exitStatus != 0) {
                        String filePath_CheckClearingHouse = "src/test/resources/Logs/" + testCaseName + "_checkClearingHouseFtp.log";
                        System.out.println("filePath_CheckClearingHouse = " + filePath_CheckClearingHouse);
                        String checkClearingHouseScriptLog = fileUtils.readLogContent(filePath_CheckClearingHouse);
                        System.out.println("checkClearingHouseScriptLog = " + checkClearingHouseScriptLog);
                        String singletonError = "Error: Another process is running, job is not executed";
                        if (checkClearingHouseScriptLog.contains(singletonError)) {
                            System.out.println("singletonError = " + singletonError);
                            System.out.println("------- Script run started from beginning ------- = ");
                            continue;
                        } else {
                            logUtils.assertEquals("checkClearingHouseFtp run NOT Success!!! \n " +
                                    "Error is not Singleton Error \n " + script, 0, exitStatus);
//                            break;
                        }
                    } else {
                        logUtils.assertEquals("checkClearingHouseFtp run NOT Success!!! \n" + script, 0, exitStatus);
//                        break;
                    }
                }
                BrowserUtils.waitFor(0.5);
                MongoDBUtils.setSingletonSettings(false, "notKept", "none", testCasesDB);
                BrowserUtils.waitFor(1);
                break;
            } else {
                System.out.println("----waiting for singleton script run checkclearingHouseFtp----");
                BrowserUtils.waitFor(3);
            }
        }
    }


    @And("run process 820 script")
    public void runProcess820Script() {
//        fileName837 = "2024071009059684149.837";
//        System.out.println("testCaseName = " + testCaseName);
        String fileName820 = "";
        if (fileName835.contains("Test_Case_")) {
            fileName835 = fileName835.replace(testCaseName + "_", "");
            fileName820 = testCaseName + "_CDT04FU8.RETRACE.EDI_" + fileName835.replace(".835", ".820");
        } else {
            fileName820 = "CDT04FU8.RETRACE.EDI_" + fileName835.replace(".835", ".820");
        }
        System.out.println("fileName820 = " + fileName820);
        String ip = serverIp;
//        String script = "node background/process-hsbc.js --s3-bucket retrace-synthetic-bank-processing " +
//                "--prefix " +s3TestFolderName+ " --file "+fileName820;
        String filePathServer820 = server820UploadPath + "/" + fileName820;
        String script = "node background/process-hsbc.js --file " + filePathServer820;
        System.out.println("script = " + script);

        for (int i = 0; i < 200; i++) {
            if (!MongoDBUtils.getSingletonSettings()) {
                System.out.println("----started- for singleton script run process820----");
                if (!MongoDBUtils.getSingletonSettings()) {
                    MongoDBUtils.pushAndKeepSettings(partner, practiceId, logger, testCaseName, script, testCasesDB);
                    int exitStatus = sshUtils.runScriptsLogToFile(script, ip, testCaseName + "_process820", testCasesDB);
                    BrowserUtils.waitFor(0.5);
                    MongoDBUtils.setSingletonSettings(false, "notKept", "none", testCasesDB);
                    BrowserUtils.waitFor(1);
                    logUtils.assertEquals("process820 run NOT Success!!! \n" + script, 0, exitStatus);
                }
                eraCycleUtils.updateTestCaseDoc(testId, "testStatus", "process820 script run", testCasesDB);
                break;
            } else {
                System.out.println("----waiting for singleton script run process820----");
                BrowserUtils.waitFor(3);
            }
        }
        try {
            boolean settingsMatched = eraCycleUtils.isAutomationSettingsMatching(partner, practiceId, testCasesDB, testCaseName);
            System.out.println("settingsMatched before background/process-hsbc script run " + settingsMatched);
            logUtils.assertTrue(settingsMatched, "settings NOT Matched before background/process-hsbc script run ");
        } catch (AssertionError e) {
            boolean settingsMatchedCatchBlok = eraCycleUtils.isAutomationSettingsMatching(partner, practiceId, testCasesDB, testCaseName);
            System.out.println("settingsMatchedCatchBlok = " + settingsMatchedCatchBlok);
            throw e;  // Rethrow the exception so the test still fails
        }
    }

//    @And("run process 820 script for {string}")
//    public void runProcess820Script(String testCaseName) {
////        fileName837 = "2024071009059684149.837";
//        String fileName835 = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("fileName835");
//        String fileName820 = "CDT04FU8.RETRACE.EDI_" + fileName835.replace(".835", ".820");
//        System.out.println("fileName820 = " + fileName820);
////        fileName820 ="CDT04FU8.RETRACE.EDI_2024071009059684149.820";
//        String ip = serverIp;
////        String script = "node background/process-hsbc.js --s3-bucket retrace-synthetic-bank-processing " +
////                "--prefix " +s3TestFolderName+ " --file "+fileName820;
//        String filePathServer820 = server820UploadPath + "/" + fileName820;
//        String script = "node background/process-hsbc.js --file " + filePathServer820;
//        System.out.println("script = " + script);
//
//        testCaseName = testCaseName.replace(" ", "_");
//        for (int i = 0; i < 200; i++) {
//            if (!MongoDBUtils.getSingletonSettings()) {
//                System.out.println("----started- for singleton script run process820----");
//                if (!MongoDBUtils.getSingletonSettings()) {
//                    MongoDBUtils.pushAndKeepSettings(partner, practiceId, logger, testCaseName, script, testCasesDB);
//                    int exitStatus = sshUtils.runScriptsLogToFile(script, ip, testCaseName + "_process820", testCasesDB);
//                    BrowserUtils.waitFor(0.5);
//                    MongoDBUtils.setSingletonSettings(false, "notKept", "none", testCasesDB);
//                    BrowserUtils.waitFor(1);
//                    logUtils.assertEquals("process820 run NOT Success!!! \n" + script, 0, exitStatus);
//                }
//                break;
//            } else {
//                System.out.println("----waiting for singleton script run process820----");
//                BrowserUtils.waitFor(3);
//            }
//        }
//    }

    @Given("find check number from 835 file and match with claim")
    public void findCheckNumberFrom835FileAndMatchWithClaim() {

        try (MongoClient mongoClient = MongoDBUtils.getMongoClient()) {
            for (String fileName : fileNameList837) {
                String fileName835 = fileName.replace(".837", ".835");
                System.out.println("fileName835 = " + fileName835);
                String filePath = "/synthetic-chi/era_outbound";

                String fileContent835 = sshUtils.readFile(filePath, fileName835);
                System.out.println("fileContent835 = " + fileContent835);
                int fileContentSize = fileContent835.length();
                if (fileContentSize > 0) {
                    System.out.println("fileContent835 = " + fileContent835);

                    int trn02Index = fileContent835.indexOf("~TRN*1*") + 7;
                    String trn02Str = fileContent835.substring(trn02Index);
//                    System.out.println("trn02Str = " + trn02Str);
                    trn02Str = trn02Str.substring(0, trn02Str.indexOf("~"));
//                    System.out.println("trn02Str 2= " + trn02Str);
                    String checkNumber = trn02Str.substring(0, trn02Str.indexOf("*"));
                    System.out.println("checkNumber = " + checkNumber);

                    String filter = "{fileName837:'" + fileName + "'}";
                    BasicDBObject query = BasicDBObject.parse(filter);
                    System.out.println("query = " + query);
                    MongoCollection<Document> eraTestColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
                    Document document = eraTestColl.find(query).first();

                    if (document != null) {
                        System.out.println(fileName835 + " check Number = " + checkNumber);
//                System.out.println("documenJson() = " + document.toJson());

                        String update = "{$set:{checkNumber:'" + checkNumber + "', file835Content:'" + fileContent835 + "', fileName835:'" + fileName835 + "'}}";
                        MongoDBUtils.executeUpdateQuery(eraTestColl, filter, update);
                    }
                }
                System.out.println("---------------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Given("find check number from 835 file and match with claim from 835 file")
    public void findCheckNumberFrom835FileAndMatchWithClaimFrom835File() {

        String filePath = "/synthetic-chi/era_outbound";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> cycleCountColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, "cycleCount");
        Bson filter2 = eq("countName", "eraCycle");
        Document cycleCountDoc = cycleCountColl.find(filter2).first();
        Date restoreTimeStamp = cycleCountDoc.getDate("updatedAt");
        System.out.println("restoreTimeStamp = " + restoreTimeStamp);
//        List<String> newFileNameList837 = new ArrayList<>();
//        List<String> newFileNameList837 = new ArrayList<>();
        List<String> sftpFileNameList835 = sshUtils.sftpGet835FileNameList(filePath);
        System.out.println("sftpFileNameList835.size() = " + sftpFileNameList835.size());
        for (String s : sftpFileNameList835) {
            System.out.println("fileName = " + s);
        }
//        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        try {
            for (String sftpFileName : sftpFileNameList835) {
                System.out.println("835 file name = " + sftpFileName);
                try {
                    int fileNameLength = sftpFileName.length();
                    if (fileNameLength > 23) {
                        System.out.println("835 file name 1= " + sftpFileName);
                        sftpFileName = sftpFileName.substring(fileNameLength - 23);
                        System.out.println("835 file name 2= " + sftpFileName);
                    }

                    String dateStr = sftpFileName.substring(0, 12);
                    System.out.println("dateStr = " + dateStr);
                    Date createDate835 = DateUtils.getDate(dateStr, "yyyyMMddHHmm");
                    System.out.println("restoreTimeStamp = " + restoreTimeStamp);
                    System.out.println("createDate835    = " + createDate835);
                    boolean inThisCycle = false;
                    if (createDate835.after(restoreTimeStamp)) {
//                    System.out.println("createDate835    = " + createDate835);
                        inThisCycle = true;
                        System.out.println("inThisCycle = " + inThisCycle);
                    }

                    if (inThisCycle) {
                        MongoCollection<Document> eraTestColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
                        String fileContent835 = sshUtils.readFile(filePath, sftpFileName);
                        boolean isNpiMatch = fileContent835.contains(npi);
                        System.out.println("isNpiMatch = " + isNpiMatch);


                        System.out.println("npi = " + npi);
                        System.out.println("fileContent835 = " + fileContent835);
                        int clpIndex = fileContent835.indexOf("~CLP*") + 5;
                        String clpStr = fileContent835.substring(clpIndex);
//                System.out.println("clpStr = " + clpStr);
                        clpStr = clpStr.substring(0, clpStr.indexOf("~"));
//                System.out.println("clpStr 2= " + clpStr);
                        String claimIdentifier835 = clpStr.substring(0, clpStr.indexOf("+"));
                        System.out.println("claimIdentifier835 = " + claimIdentifier835);
                        String filterStr = "{claimIdentifier:{$regex:'" + claimIdentifier835 + "'}}";
                        if (!isNpiMatch) {
                            String npiMissMatchMessage = sftpFileName + " lookslike inThisCycle but npi NOT matched \n****Passed next file****";
                            System.out.println(npiMissMatchMessage);
//                        String update = "{$set:{fileName835:'notFound', fileName835MissMatchMessage: '"+npiMissMatchMessage+"'}}";
//                        MongoDBUtils.executeUpdateQuery(eraTestColl,filterStr,update);
                            System.out.println("---------------------------------------");
                            continue;
                        }
                        int trn02Index = fileContent835.indexOf("~TRN*1*") + 7;
                        String trn02Str = fileContent835.substring(trn02Index);
//                System.out.println("trn02Str = " + trn02Str);
                        trn02Str = trn02Str.substring(0, trn02Str.indexOf("~"));
//                System.out.println("trn02Str 2= " + trn02Str);
                        checkNumber = trn02Str.substring(0, trn02Str.indexOf("*"));
                        System.out.println("checkNumber = " + checkNumber);
                        reference = checkNumber;

//              String filter = "{claimIdentifier:'"+claimIdentifier835+"'}";
                        Document filter = Document.parse(filterStr);
                        BasicDBObject query = BasicDBObject.parse(filterStr);
                        System.out.println("query = " + query);
                        Document document = eraTestColl.find(query).first();

                        if (document != null && isNpiMatch) {
                            System.out.println(sftpFileName + " matched to claim = " + claimIdentifier835);
//                System.out.println("documenJson() = " + document.toJson());
//                    Document update = new Document()
//                            .append("$set", new Document("fileName835", sftpFileName)
//                                    .append("file835Content", fileContent835)
//                                    .append("")
//                            );
                            String update = "{$set:{fileName835:'" + sftpFileName + "', file835Content:'" + fileContent835 + "', checkNumber:'" + checkNumber + "'}}";
                            MongoDBUtils.executeUpdateQuery(eraTestColl, filterStr, update);
//                System.out.println("fileName = " + fileName);
//                System.out.println("fileContent837 = " + fileContent837);
                        }
                    }

                    System.out.println("---------------------------------------");
                } catch (Exception e) {
                    System.out.println("FAILED sftpFileName = " + sftpFileName);
                    e.printStackTrace();
                }

            }
            System.out.println("Total " + sftpFileNameList835.size() + " - 835 file/s exists in sftp era_outbound folder");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }

    }

    @And("reconcile the inspay ledger")
    public void reconcileTheInspayLedger() {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> ledgerColl = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");

        Document filter = new Document("reference", checkNumber);
        BasicDBObject query = BasicDBObject.parse("{reference:'" + reference + "'}");

        Document ledgerDoc = null;

        // Retry logic to wait for the document to be generated
        for (int attempt = 0; attempt < 300; attempt++) {
            ledgerDoc = ledgerColl.find(query).first();
            if (ledgerDoc != null) {
                break;
            } else {
                BrowserUtils.waitFor(1);
            }
        }

        // If no document is found after retries, fail the test
        if (ledgerDoc == null) {
            throw new AssertionError("Document not found in ledger_1 collection after waiting.");
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        // Set hour, minute, second, and millisecond to zero
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Convert the Calendar object to a Date object
        Date specificDate = calendar.getTime();

        System.out.println("specificDate = " + specificDate);

        Document update = new Document()
                .append("$set", new Document("date", specificDate));
        ledgerColl.updateOne(filter, update);

        ObjectId ObjId = ledgerDoc.getObjectId("_id");
        String ledgerObjId = ObjId.toHexString(); // Convert ObjectId to String
        System.out.println("ledgerObjId = " + ledgerObjId);

        Map<String, Object> postBody = new HashMap<>();
        postBody.put("_id", ledgerObjId);
        postBody.put("status", "reconciled");
        postBody.put("reconcileDate", new Date());
        postBody.put("practice_id", practiceId);

        System.out.println("postBody = " + postBody);
        apiUtils.reconcileLedger(partner, postBody);
        eraCycleUtils.updateTestCaseDoc(testId, "testStatus", "ledger reconciled", testCasesDB);
        BrowserUtils.waitFor(10);
    }


    @And("reconcile the inspay ledger for {string}")
    public void reconcileTheInspayLedgerFor(String testCaseName) {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> ledgerColl = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");

        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");

//        checkNumber= "3uTdUXmYq2Cj";  //check Number for Test
        Document filter = new Document("reference", checkNumber);
        String filter2 = "{reference:'" + checkNumber + "'}";
        BasicDBObject query = BasicDBObject.parse(filter2);
//        System.out.println("query = " + query);
        Document document = ledgerColl.find(query).first();
//        System.out.println("document.toJson() = " + document.toJson());

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        // Set hour, minute, second, and millisecond to zero
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Convert the Calendar object to a Date object
        Date specificDate = calendar.getTime();

        System.out.println("specificDate = " + specificDate);

        Document update = new Document()
                .append("$set", new Document("date", specificDate));
        ledgerColl.updateOne(filter, update);

        String ledgerId = document.getString("ledger_id");
        System.out.println("ledgerId = " + ledgerId);

        Map<String, Object> postBody = new HashMap<>();
        postBody.put("ledger_id", ledgerId);
        postBody.put("status", "reconciled");
        postBody.put("reconcileDate", new Date());
        postBody.put("practice_id", practiceId);

        System.out.println("postBody = " + postBody);
        apiUtils.reconcileLedger(partner, postBody);
        BrowserUtils.waitFor(10);
    }

    @And("retrace approve")
    public void retraceApprove() {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");

        Document eraSplitFileAssignedDoc = eraSplitFileAssignedColl.find(query).first();
        Object approveSuccess = null;
        try {
//            eraForSweep = JsonPath.from(eraSplitFileAssignedDoc.toJson());
            eraSplitFileAssignedId = (String) eraSplitFileAssignedDoc.get("era_split_file_assigned_id");
//        checkNumber = eraForSweep.get("check_number");
            eraWritebackRecords = (List<Object>) eraSplitFileAssignedDoc.get("writeback_records");
            dfiAccountId = eraSplitFileAssignedDoc.get("dfi_account_id").toString();
            System.out.println("dfiAccountId = " + dfiAccountId);

//        eraSplitFileAssignedId = "tAZNXdF5HBGaojiP";

            JsonPath sweepEra = apiUtils.retApproveERA(partner, eraSplitFileAssignedId);
//            sweepEra.prettyPrint();
            approveSuccess = sweepEra.get("success");
            System.out.println("approveSuccess = " + approveSuccess);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error : retraceApprove step failed --> " + Arrays.toString(e.getStackTrace()));
            throw e;
        } finally {
            mongoClient.close();
        }
        logUtils.assertNotNull(approveSuccess, "Retrace approve process is not success!!!");
        eraCycleUtils.updateTestCaseDoc(testId, "testStatus", "retraceApproved", testCasesDB);
        BrowserUtils.waitFor(10);
    }

    @And("retrace approve for {string}")
    public void retraceApproveFor(String testCaseName) {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");

        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");

        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");

        Document eraSplitFileAssignedDoc = eraSplitFileAssignedColl.find(query).first();
        Object approveSuccess = null;
        try {
            JsonPath eraForSweep = JsonPath.from(eraSplitFileAssignedDoc.toJson());
            String eraSplitFileAssignedId = eraForSweep.get("era_split_file_assigned_id");
//        checkNumber = eraForSweep.get("check_number");
            List<Object> eraWritebackRecords = eraForSweep.get("writeback_records");
            String dfiAccountId = eraForSweep.get("dfi_account_id");
            System.out.println("dfiAccountId = " + dfiAccountId);

//        eraSplitFileAssignedId = "tAZNXdF5HBGaojiP";

            JsonPath sweepEra = apiUtils.retApproveERA(partner, eraSplitFileAssignedId);
//            sweepEra.prettyPrint();
            approveSuccess = sweepEra.get("success");
            System.out.println("approveSuccess = " + approveSuccess);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error : retraceApprove step failed --> " + Arrays.toString(e.getStackTrace()));
            throw e;
        } finally {
            mongoClient.close();
        }
        logUtils.assertNotNull(approveSuccess, "Retrace approve process is not success!!!");
        BrowserUtils.waitFor(10);
    }

    @And("retrace disapprove")
    public void retraceDisapprove() {

        eraCycleUtils.updateTestCaseDoc(testId, "testStatus", "providerDisapprove inprogress", testCasesDB);
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");

        Document eraSplitFileAssignedDoc = eraSplitFileAssignedColl.find(query).first();
        try {
//            eraForSweep = JsonPath.from(eraSplitFileAssignedDoc.toJson());
            eraSplitFileAssignedId = eraSplitFileAssignedDoc.getString("era_split_file_assigned_id");
//            checkNumber = eraForSweep.get("check_number");
            eraWritebackRecords = (List<Object>) eraSplitFileAssignedDoc.get("writeback_records");
            dfiAccountId = eraSplitFileAssignedDoc.getString("dfi_account_id");

//        eraSplitFileAssignedId = "tAZNXdF5HBGaojiP";

            JsonPath sweepEra = apiUtils.retDisapproveERA(partner, eraSplitFileAssignedId);
//            sweepEra.prettyPrint();
            Object approveSuccess = sweepEra.get("success");
            System.out.println("approveSuccess = " + approveSuccess);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error : retraceDisApprove step failed --> " + Arrays.toString(e.getStackTrace()));
            throw e;
        } finally {
            mongoClient.close();
        }
        eraCycleUtils.updateTestCaseDoc(testId, "testStatus", "providerDisapproved", testCasesDB);
    }

    @Then("retrace disapprove for {string}")
    public void retraceDisapproveFor(String testCaseName) {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");

        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");

        Document eraSplitFileAssignedDoc = eraSplitFileAssignedColl.find(query).first();
        String eraSplitFileAssignedId = "";
        try {
            JsonPath eraForSweep = JsonPath.from(eraSplitFileAssignedDoc.toJson());
            eraSplitFileAssignedId = eraForSweep.get("era_split_file_assigned_id");
//            checkNumber = eraForSweep.get("check_number");
            List<Object> eraWritebackRecords = eraForSweep.get("writeback_records");
            String dfiAccountId = eraForSweep.get("dfi_account_id");

//        eraSplitFileAssignedId = "tAZNXdF5HBGaojiP";

            JsonPath sweepEra = apiUtils.retDisapproveERA(partner, eraSplitFileAssignedId);
//            sweepEra.prettyPrint();
            Object approveSuccess = sweepEra.get("success");
            System.out.println("approveSuccess = " + approveSuccess);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error : retraceDisApprove step failed --> " + Arrays.toString(e.getStackTrace()));
            throw e;
        } finally {
            mongoClient.close();
        }
    }

    @And("retrace sweep")
    public void retraceSweep() {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query2 = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");

        Document eraSplitFileAssignedDoc = eraSplitFileAssignedColl.find(query2).first();
        try {
//            eraForSweep = JsonPath.from(eraSplitFileAssignedDoc.toJson());
            eraSplitFileAssignedId = (String) eraSplitFileAssignedDoc.get("era_split_file_assigned_id");
//        checkNumber = eraForSweep.get("check_number");
            eraWritebackRecords = (List<Object>) eraSplitFileAssignedDoc.get("writeback_records");
            dfiAccountId = (String) eraSplitFileAssignedDoc.get("dfi_account_id");
            System.out.println("dfiAccountId = " + dfiAccountId);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error : " + Arrays.toString(e.getStackTrace()));
            throw e;
        }

        MongoCollection<Document> dfiAccount1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "dfi_account_1");
        BasicDBObject query = BasicDBObject.parse("{dfi_account_id:'" + dfiAccountId + "'}");
        Document dfiAccountDoc = dfiAccount1Coll.find(query).first();
        JsonPath dfiAccountJson = JsonPath.from(dfiAccountDoc.toJson());
        System.out.println("dfiAccountDoc.toJson() = " + dfiAccountDoc.toJson());

        // Check if the fields exist before removing them
        boolean hasSweepByUser = dfiAccountDoc.containsKey("sweep_by_user");
        boolean hasSweepKey = dfiAccountDoc.containsKey("sweep_key");
        Object sweepByUser = null;
        Object sweepKey = null;
//        boolean isSweepByUserNull = true;
//        boolean isSweepKeyNull = true;

        if (hasSweepKey || hasSweepByUser) {
            sweepByUser = dfiAccountDoc.get("sweep_by_user");
            System.out.println("sweepByUser = " + sweepByUser);
            sweepKey = dfiAccountDoc.get("sweep_key");
            System.out.println("sweepKey = " + sweepKey);
        }

//        if (hasSweepByUser || hasSweepKey) {
//            BasicDBObject updateFields = new BasicDBObject();
//            if (hasSweepByUser) {
//                updateFields.append("sweep_by_user", "");
//            }
//            if (hasSweepKey) {
//                updateFields.append("sweep_key", "");
//            }
//
//            BasicDBObject updateQuery = new BasicDBObject();
//            updateQuery.append("$unset", updateFields);
//
//            // Update the document in the collection
//            dfiAccount1Coll.updateOne(query, updateQuery);
//            System.out.println("sweep_by_user and sweep_key are set to null");
//        }

        String sweepResponseBody = null;
        int responseStatusCode = 0;
        try {
            String dfiAccount = dfiAccountDoc.getString("dfi_account_id");
            System.out.println("dfiAccount = " + dfiAccount);
            Response sweepResponse = apiUtils.getSweepResponse(partner, dfiAccount);
            sweepResponseBody = sweepResponse.getBody().toString();
            responseStatusCode = sweepResponse.statusCode();

            int i = 0;
            while (responseStatusCode != 200 ||
                    sweepByUser != null ||
                    sweepKey != null ||
                    sweepResponseBody.contains("If you sweep the new balance will be") ||
                    sweepResponseBody.contains("is currently not available.  Please wait and try again later")) {
                BrowserUtils.waitFor(10);
                i++;
                sweepResponse = apiUtils.getSweepResponse(partner, dfiAccount);
                sweepResponseBody = sweepResponse.getBody().toString();
                responseStatusCode = sweepResponse.statusCode();

                sweepByUser = dfiAccountDoc.get("sweep_by_user");
                System.out.println("sweepByUser = " + sweepByUser);
                sweepKey = dfiAccountDoc.get("sweep_key");
                System.out.println("sweepKey = " + sweepKey);

//                hasSweepByUser = dfiAccountDoc.containsKey("sweep_by_user");
//                hasSweepKey = dfiAccountDoc.containsKey("sweep_key");

                if (i == 30) {
                    System.out.println("sweep has been tried " + i + " times");
                    break;
                }
            }
//            System.out.println("sweepResponseBody = " + sweepResponseBody);
            System.out.println("responseStatusCode = " + responseStatusCode);
        } catch (Exception e) {
            logger.error("error : " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
//            System.out.println("sweepResponseBody = " + sweepResponseBody);
            System.out.println("responseStatusCode = " + responseStatusCode);
            throw e;
        } finally {
            mongoClient.close();
//            System.out.println("sweepResponseBody = " + sweepResponseBody);
            System.out.println("responseStatusCode = " + responseStatusCode);
        }
        logUtils.assertEquals("Sweep is not success!!!", 200, responseStatusCode);

        eraCycleUtils.updateTestCaseDoc(testId, "testStatus", "retraceSweeped", testCasesDB);
        BrowserUtils.waitFor(5);
    }


    @And("retrace sweep for {string}")
    public void retraceSweepFor(String testCaseName) {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");
        BasicDBObject query2 = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");

        Document eraSplitFileAssignedDoc = eraSplitFileAssignedColl.find(query2).first();
        String dfiAccountId = "";
        try {
            JsonPath eraForSweep = JsonPath.from(eraSplitFileAssignedDoc.toJson());
            String eraSplitFileAssignedId = eraForSweep.get("era_split_file_assigned_id");
//        checkNumber = eraForSweep.get("check_number");
            List<Object> eraWritebackRecords = eraForSweep.get("writeback_records");
            dfiAccountId = eraForSweep.get("dfi_account_id");
            System.out.println("dfiAccountId = " + dfiAccountId);
        } catch (Exception e) {
            logger.error("error : " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }

        MongoCollection<Document> dfiAccount1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "dfi_account_1");
        BasicDBObject query = BasicDBObject.parse("{virtual_account_id:'" + dfiAccountId + "'}");
        Document dfiAccountDoc = dfiAccount1Coll.find(query).first();
        JsonPath dfiAccountJson = JsonPath.from(dfiAccountDoc.toJson());
        System.out.println("dfiAccountDoc.toJson() = " + dfiAccountDoc.toJson());

        // Check if the fields exist before removing them
        boolean hasSweepByUser = dfiAccountDoc.containsKey("sweep_by_user");
        boolean hasSweepKey = dfiAccountDoc.containsKey("sweep_key");
        Object sweepByUser = null;
        Object sweepKey = null;
//        boolean isSweepByUserNull = true;
//        boolean isSweepKeyNull = true;

        if (hasSweepKey || hasSweepByUser) {
            sweepByUser = dfiAccountDoc.get("sweep_by_user");
            System.out.println("sweepByUser = " + sweepByUser);
            sweepKey = dfiAccountDoc.get("sweep_key");
            System.out.println("sweepKey = " + sweepKey);
        }

//        if (hasSweepByUser || hasSweepKey) {
//            BasicDBObject updateFields = new BasicDBObject();
//            if (hasSweepByUser) {
//                updateFields.append("sweep_by_user", "");
//            }
//            if (hasSweepKey) {
//                updateFields.append("sweep_key", "");
//            }
//
//            BasicDBObject updateQuery = new BasicDBObject();
//            updateQuery.append("$unset", updateFields);
//
//            // Update the document in the collection
//            dfiAccount1Coll.updateOne(query, updateQuery);
//            System.out.println("sweep_by_user and sweep_key are set to null");
//        }

        String sweepResponseBody = null;
        int responseStatusCode = 0;
        try {
            String dfiAccount = dfiAccountDoc.getString("dfi_account_id");
            System.out.println("dfiAccount = " + dfiAccount);
            Response sweepResponse = apiUtils.getSweepResponse(partner, dfiAccount);
            sweepResponseBody = sweepResponse.getBody().toString();
            responseStatusCode = sweepResponse.statusCode();

            int i = 0;
            while (responseStatusCode != 200 ||
                    sweepByUser != null ||
                    sweepKey != null ||
                    sweepResponseBody.contains("If you sweep the new balance will be") ||
                    sweepResponseBody.contains("is currently not available.  Please wait and try again later")) {
                BrowserUtils.waitFor(20);
                i++;
                sweepResponse = apiUtils.getSweepResponse(partner, dfiAccount);
                sweepResponseBody = sweepResponse.getBody().toString();
                responseStatusCode = sweepResponse.statusCode();

                sweepByUser = dfiAccountDoc.get("sweep_by_user");
                System.out.println("sweepByUser = " + sweepByUser);
                sweepKey = dfiAccountDoc.get("sweep_key");
                System.out.println("sweepKey = " + sweepKey);
                if (i == 100) {
                    System.out.println("sweep has been tried " + i + " times");
                    break;
                }
            }
//            System.out.println("sweepResponseBody = " + sweepResponseBody);
            System.out.println("responseStatusCode = " + responseStatusCode);
        } catch (Exception e) {
            logger.error("error : " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
//            System.out.println("sweepResponseBody = " + sweepResponseBody);
            System.out.println("responseStatusCode = " + responseStatusCode);
        } finally {
            mongoClient.close();
            System.out.println("sweepResponseBody = " + sweepResponseBody);
            System.out.println("responseStatusCode = " + responseStatusCode);
        }
        logUtils.assertEquals("Sweep is not success!!!", 200, responseStatusCode);

        eraCycleUtils.updateTestCaseDoc(testId, "testStatus", "retraceSweeped", testCasesDB);
        BrowserUtils.waitFor(5);
    }

    @And("provider approve")
    public void providerApprove() {
        // check era split file assign -> status wait until ---->auto_finalize then sweep

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");

        Document eraSplitFileAssignedDoc = eraSplitFileAssignedColl.find(query).first();
        try {
            eraForSweep = JsonPath.from(eraSplitFileAssignedDoc.toJson());
            eraSplitFileAssignedId = eraForSweep.get("era_split_file_assigned_id");
//        checkNumber = eraForSweep.get("check_number");
            eraWritebackRecords = eraForSweep.get("writeback_records");
            dfiAccountId = eraForSweep.get("dfi_account_id");
            System.out.println("dfiAccountId = " + dfiAccountId);
        } catch (Exception e) {
            logger.error("error : " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }

        JsonPath finalizeEra = apiUtils.providerApproveERA(partner, eraSplitFileAssignedId);
        boolean finalizeEraSuccess = false;
        if (finalizeEra != null) {
            finalizeEraSuccess = finalizeEra.get("success");
        }
        logUtils.assertTrue(finalizeEraSuccess, "Provider Approve process NOT success!!!");
        eraCycleUtils.updateTestCaseDoc(testId, "testStatus", "providerApproved", testCasesDB);
        BrowserUtils.waitFor(10);
        System.out.println("finalizeEraSuccess = " + finalizeEraSuccess);
    }

    @And("provider approve for {string}")
    public void providerApproveFor(String testCaseName) {
        // check era split file assign -> status wait until ---->auto_finalize then sweep

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");

        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");

        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");

        Document eraSplitFileAssignedDoc = eraSplitFileAssignedColl.find(query).first();

        String eraSplitFileAssignedId = "";
        try {
            JsonPath eraForSweep = JsonPath.from(eraSplitFileAssignedDoc.toJson());
            eraSplitFileAssignedId = eraForSweep.get("era_split_file_assigned_id");
//        checkNumber = eraForSweep.get("check_number");
            List<Object> eraWritebackRecords = eraForSweep.get("writeback_records");
            String dfiAccountId = eraForSweep.get("dfi_account_id");
            System.out.println("dfiAccountId = " + dfiAccountId);
        } catch (Exception e) {
            logger.error("error : " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }

        JsonPath finalizeEra = apiUtils.providerApproveERA(partner, eraSplitFileAssignedId);
        boolean finalizeEraSuccess = false;
        if (finalizeEra != null) {
            finalizeEraSuccess = finalizeEra.get("success");
        }
        logUtils.assertTrue(finalizeEraSuccess, "Provider Approve process NOT success!!!");
        BrowserUtils.waitFor(10);
        System.out.println("finalizeEraSuccess = " + finalizeEraSuccess);
    }

    @And("provider disapprove")
    public void providerDisapprove() {
        // check era split file assign -> status wait until ---->auto_finalize then sweep
        JsonPath finalizeEra = apiUtils.providerDisapproveERA(partner, eraSplitFileAssignedId);
        boolean finalizeEraSuccess = false;
        if (finalizeEra != null) {
            finalizeEraSuccess = finalizeEra.get("success");
        }
        eraCycleUtils.updateTestCaseDoc(testId, "testStatus", "providerDisapproved", testCasesDB);
        System.out.println("finalizeEraSuccess = " + finalizeEraSuccess);
    }


    @And("provider disapprove for {string}")
    public void providerDisapproveFor(String testCaseName) {
        // check era split file assign -> status wait until ---->auto_finalize then sweep

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");

        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");

        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");

        Document eraSplitFileAssignedDoc = eraSplitFileAssignedColl.find(query).first();

        String eraSplitFileAssignedId = "";
        JsonPath eraForSweep = JsonPath.from(eraSplitFileAssignedDoc.toJson());
        eraSplitFileAssignedId = eraForSweep.get("era_split_file_assigned_id");

        JsonPath finalizeEra = apiUtils.providerDisapproveERA(partner, eraSplitFileAssignedId);
        boolean finalizeEraSuccess = false;
        if (finalizeEra != null) {
            finalizeEraSuccess = finalizeEra.get("success");
        }
        System.out.println("finalizeEraSuccess = " + finalizeEraSuccess);
    }

    @Then("verify era_split_file_assigned collection")
    public void verifyEra_split_file_assignedCollection() {
        // autopost qualified -- false
        // rejection reasons -- sts820 not found
        //checkNumber= "jIdsAiBq76CS";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
        try {
            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            boolean auto_post_qualified = eraSplitFileJson.get("auto_post.qualified");
            System.out.println("auto_post_qualified = " + auto_post_qualified);
//            Assert.assertFalse("auto_post.qualified is True",auto_post_qualified);
            List<String> rejectionReason = eraSplitFileJson.get("rejection_reasons");
            System.out.println("rejectionReason = " + rejectionReason);
            Assert.assertEquals("ST820 not found for " + checkNumber, rejectionReason.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }

    }


    @Then("verify ledger_1 collection")
    public void verifyLedger_1Collection() {

        // ledger1 tin mismatche -- false
        // era split file assingned id - equal to ledger1 to era_split file assign collections
        // check ledger id in era_split file assign collection
        // matching_bpr_835_820 ----true
        // autopost qualified --- true from era_split_file_assingned collection

//        checkNumber= "jIdsAiBq76CS";
//        reference= "jIdsAiBq76CS";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject eraQuery = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(eraQuery).first();

        MongoCollection<Document> ledger1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");
        BasicDBObject ledgerQuery = BasicDBObject.parse("{reference:'" + reference + "'}");
        System.out.println("ledgerQuery = " + ledgerQuery);
        Document ledger1Doc = ledger1Coll.find(ledgerQuery).first();
        String era_split_file_assigned_id_ERA = "";
        String ledgerId_ERA = "";
        String era_split_file_assigned_id_LEDGER = "";
        String ledgerId_ledger = "";


        try {
            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            era_split_file_assigned_id_ERA = eraSplitFileJson.get("era_split_file_assigned_id");
            ledgerId_ERA = eraSplitFileJson.get("ledger_id");
            boolean matching_bpr_835_820 = eraSplitFileJson.get("matching_bpr_835_820");
            Assert.assertEquals(true, matching_bpr_835_820);
            boolean autoPostQualifyStatus = eraSplitFileJson.get("auto_post.qualified");
            Assert.assertTrue("auto-post is not qualified", autoPostQualifyStatus);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            JsonPath ledger1Json = JsonPath.from(ledger1Doc.toJson());

            boolean ledger1_tinMismatch = ledger1Json.get("tin_mismatch");
            System.out.println("ledger1_tinMismatch = " + ledger1_tinMismatch);
            Assert.assertFalse(ledger1_tinMismatch);

            era_split_file_assigned_id_LEDGER = ledger1Json.get("era_split_file_assigned_id");
            ledgerId_ledger = ledger1Json.get("ledger_id");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }

        Assert.assertEquals("era_split_file_assigned_id does not match", era_split_file_assigned_id_ERA, era_split_file_assigned_id_LEDGER);
        Assert.assertEquals("ledger_id does not match", ledgerId_ERA, ledgerId_ledger);
    }


    @Then("verify ledger_1 collection for {string}")
    public void verifyLedger_1CollectionFor(String testCaseName) {

        // ledger1 tin mismatche -- false
        // era split file assingned id - equal to ledger1 to era_split file assign collections
        // check ledger id in era_split file assign collection
        // matching_bpr_835_820 ----true
        // autopost qualified --- true from era_split_file_assingned collection

//        checkNumber= "jIdsAiBq76CS";
//        reference= "jIdsAiBq76CS";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");
        BasicDBObject eraQuery = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(eraQuery).first();

        MongoCollection<Document> ledger1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");
        BasicDBObject ledgerQuery = BasicDBObject.parse("{reference:'" + checkNumber + "'}");
        System.out.println("ledgerQuery = " + ledgerQuery);
        Document ledger1Doc = ledger1Coll.find(ledgerQuery).first();
        String era_split_file_assigned_id_ERA = "";
        String ledgerId_ERA = "";
        String era_split_file_assigned_id_LEDGER = "";
        String ledgerId_ledger = "";


        try {
            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            era_split_file_assigned_id_ERA = eraSplitFileJson.get("era_split_file_assigned_id");
            ledgerId_ERA = eraSplitFileJson.get("ledger_id");
            boolean matching_bpr_835_820 = eraSplitFileJson.get("matching_bpr_835_820");
            Assert.assertEquals(true, matching_bpr_835_820);
            boolean autoPostQualifyStatus = eraSplitFileJson.get("auto_post.qualified");
            Assert.assertTrue("auto-post is not qualified", autoPostQualifyStatus);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            JsonPath ledger1Json = JsonPath.from(ledger1Doc.toJson());

            boolean ledger1_tinMismatch = ledger1Json.get("tin_mismatch");
            System.out.println("ledger1_tinMismatch = " + ledger1_tinMismatch);
            Assert.assertFalse(ledger1_tinMismatch);

            era_split_file_assigned_id_LEDGER = ledger1Json.get("era_split_file_assigned_id");
            ledgerId_ledger = ledger1Json.get("ledger_id");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }

        Assert.assertEquals("era_split_file_assigned_id does not match", era_split_file_assigned_id_ERA, era_split_file_assigned_id_LEDGER);
        Assert.assertEquals("ledger_id does not match", ledgerId_ERA, ledgerId_ledger);
    }

    @Then("verify reconcile status")
    public void verifyReconcileStatus() {
        //verify from ledger1 --->status

//        reference= "jIdsAiBq76CS";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> ledger1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");
        BasicDBObject ledgerQuery = BasicDBObject.parse("{reference:'" + reference + "'}");
        Document ledger1Doc = ledger1Coll.find(ledgerQuery).first();

        try {
            JsonPath ledger1Json = JsonPath.from(ledger1Doc.toJson());

            String ledger1_status = ledger1Json.get("status");
            System.out.println("ledger1_status = " + ledger1_status);
            Assert.assertEquals("status is not reconciled", "reconciled", ledger1_status);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }
    }

    @Then("verify reconcile status for {string}")
    public void verifyReconcileStatusFor(String testCaseName) {
        //verify from ledger1 --->status

//        reference= "jIdsAiBq76CS";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> ledger1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");
        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");
        BasicDBObject ledgerQuery = BasicDBObject.parse("{reference:'" + checkNumber + "'}");
        Document ledger1Doc = ledger1Coll.find(ledgerQuery).first();

        try {
            JsonPath ledger1Json = JsonPath.from(ledger1Doc.toJson());

            String ledger1_status = ledger1Json.get("status");
            System.out.println("ledger1_status = " + ledger1_status);
            Assert.assertEquals("status is not reconciled", "reconciled", ledger1_status);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }
    }

    @Then("verify approval status")
    public void verifyApprovalStatus() {
        // check era split file assigned ----> autopost -> approval = approved
//        checkNumber= "jIdsAiBq76CS";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject eraQuery = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(eraQuery).first();

        try {
            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            String autoPost_Approval = eraSplitFileJson.get("auto_post.approval");
            System.out.println("autoPost_Approval = " + autoPost_Approval);
            try {
                Assert.assertEquals("auto_post approval status is not approved", "Approved", autoPost_Approval);
            } catch (AssertionError e) {
                logger.error("auto_post approval status is not approved " + Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }
    }


    @Then("verify approval status for {string}")
    public void verifyApprovalStatusFor(String testCaseName) {
        // check era split file assigned ----> autopost -> approval = approved
//        checkNumber= "jIdsAiBq76CS";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");

        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");

        BasicDBObject eraQuery = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(eraQuery).first();

        try {
            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            String autoPost_Approval = eraSplitFileJson.get("auto_post.approval");
            System.out.println("autoPost_Approval = " + autoPost_Approval);
            try {
                Assert.assertEquals("auto_post approval status is not approved", "Approved", autoPost_Approval);
            } catch (AssertionError e) {
                logger.error("auto_post approval status is not approved " + Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }
    }

    @Then("verify sweep result")
    public void verifySweepResult() {

        // check era split file assign -> status =  not created
        // check disburstment and fees from ledger_1
        // check era status -->auto_finalize from eraspiltfileassigned collection
//        checkNumber= "RgN4Uf9uPjmZ";
//        reference="RgN4Uf9uPjmZ";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        System.out.println("query = " + query);

        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
        System.out.println("eraSplitFileDoc.toJson() = " + eraSplitFileDoc.toJson());

        MongoCollection<Document> ledger_1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");
        BasicDBObject query1 = BasicDBObject.parse("{reference:'" + reference + "',category:'fees'}");
        System.out.println("query1 = " + query1);
        BasicDBObject query2 = BasicDBObject.parse("{reference:'" + reference + "',category:'disbursement'}");
        System.out.println("query2 = " + query2);
        Document ledgerFeeDoc = ledger_1Coll.find(query1).first();
        Document ledgerDisbursementDoc = ledger_1Coll.find(query2).first();


        try {
            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            JsonPath ledgerFeeJson = JsonPath.from(ledgerFeeDoc.toJson());
            JsonPath ledgerDisbursementJson = JsonPath.from(ledgerDisbursementDoc.toJson());
            // eraSplitFileJson.prettyPrint();
            String status = eraSplitFileJson.getString("status");
            System.out.println("status = " + status);
            Assert.assertTrue(!status.equals("created"));


            String categoryFee = ledgerFeeJson.get("category");
            System.out.println("category = " + categoryFee);
            Assert.assertEquals("fees", categoryFee);
            String categoryDisbursement = ledgerDisbursementJson.get("category");
            System.out.println("categoryDisbursement = " + categoryDisbursement);
            Assert.assertEquals("disbursement", categoryDisbursement);


        } catch (Exception e) {
            e.printStackTrace();
        }
        MongoCollection<Document> eraSplitFileColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject eraQuery = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc1 = eraSplitFileColl.find(eraQuery).first();
        System.out.println("eraSplitFileDoc1.toJson() = " + eraSplitFileDoc1.toJson());
        try {
            JsonPath ledger1Json = JsonPath.from(eraSplitFileDoc1.toJson());
            String status = ledger1Json.get("status");
            System.out.println("status = " + status);
            for (int i = 0; i < 20; i++) {
                eraSplitFileDoc1 = eraSplitFileColl.find(eraQuery).first();
                ledger1Json = JsonPath.from(eraSplitFileDoc1.toJson());
                status = ledger1Json.get("status");
                System.out.println("status = " + status);
                if (!status.equals("auto_finalize")) {
                    BrowserUtils.waitFor(10);
                } else {
                    Assert.assertEquals("auto_finalize", status);
                    break;
                }
            }
            Assert.assertEquals("auto_finalize", status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Then("verify provider approval result")
    public void verifyProviderApprovalResult() {
        // check approvalProvider status -- approved from eraspiltfileassigned collection
        //
//        checkNumber= "jIdsAiBq76CS";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");

        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();

        Gson gson = new Gson();
        Map<String, Object> autoPostMap = (Map<String, Object>) gson.fromJson(eraSplitFileDoc.toJson(), Map.class).get("auto_post");
        String providerApproval = autoPostMap.get("approvalProvider") != null ? autoPostMap.get("approvalProvider").toString() : null;

        System.out.println("providerApprove = " + providerApproval);
//            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());

        logUtils.assertEquals("Provider Approval Status not Approved", "Approved", providerApproval);

        mongoClient.close();

    }

    @Then("verify provider approval result for {string}")
    public void verifyProviderApprovalResultFor(String testCaseName) {
        // check approvalProvider status -- approved from eraspiltfileassigned collection
        //
//        checkNumber= "jIdsAiBq76CS";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");

        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");

        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");

        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();

        try {
            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            String providerApproval = eraSplitFileJson.getString("auto_post.approvalProvider");
            System.out.println("providerApprove = " + providerApproval);
            Assert.assertEquals("Approved", providerApproval);

        } catch (Exception e) {
            logger.error("error : " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }
    }

    @And("verify matching for CLP-SVC level")
    public void verifyMatchingForCLPSVCLevel() {
    }

    @Then("apply automation settings for test")
    public void applyAutomationSettingsForTest() {

        Document settings = (Document) testDoc.get("settings");
        System.out.println("settings = " + settings.toJson());

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
//        MongoCollection<Document> practiceColl = MongoDBUtils.connectMongodb(mongoClient,partner,"practice_1");
        MongoCollection<Document> partner1Coll = MongoDBUtils.connectMongodb(mongoClient, "directory", "partner_1");
        try {
//            Document filter = new Document("practice_id", practiceId);
//            Document update = new Document()
//                    .append("$set", new Document("settings.era",settings));
////            System.out.println("filter = " + filter.toJson());
////            System.out.println("update = " + update.toJson());
//
//            MongoDBUtils.executeUpdateQuery(practiceColl,filter,update);
            Document filter2 = new Document("partner_id", partner);
            Document update2 = new Document("$set", new Document("match_inbound_835", true));
            MongoDBUtils.executeUpdateQuery(partner1Coll, filter2, update2);
        } catch (Exception e) {
            logger.error("error " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }
    }

    @Then("apply automation settings for test2")
    public void applyAutomationSettingsForTest2() {

        Document settings = (Document) testDoc.get("settings");
        System.out.println("settings = " + settings.toJson());

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
//        MongoCollection<Document> practiceColl = MongoDBUtils.connectMongodb(mongoClient,partner,"practice_1");
        MongoCollection<Document> partner1Coll = MongoDBUtils.connectMongodb(mongoClient, "directory", "partner_1");

//        for (int i = 0; i < 200; i++) {
//            if (!MongoDBUtils.getSingletonSettings()) {
//                System.out.println("----started for singleton script run checkclearingHouseFtp----");
//                if (!MongoDBUtils.getSingletonSettings()) { // check last time and then start
//                    MongoDBUtils.pushAndKeepSettings(partner, practiceId, logger, testCaseName, "EraCycleNewShortTestRunning", testCasesDB);
//                    MongoDBUtils.deleteSingletonRecord("checkClearinghouseFtp");
//                }
//                BrowserUtils.waitFor(0.5);
////                MongoDBUtils.setSingletonSettings(false, "notKept", "none", testCasesDB);
//                BrowserUtils.waitFor(1);
                eraCycleUtils.updateTestCaseDoc(testId, "testStatus", "apply settings step", testCasesDB);
//                break;
//            } else {
//                System.out.println("----waiting for singleton script run checkclearingHouseFtp----");
//                BrowserUtils.waitFor(3);
//            }
//        }

        try {
//            Document filter = new Document("practice_id", practiceId);
//            Document update = new Document()
//                    .append("$set", new Document("settings.era",settings));
////            System.out.println("filter = " + filter.toJson());
////            System.out.println("update = " + update.toJson());
//
//            MongoDBUtils.executeUpdateQuery(practiceColl,filter,update);
            Document filter2 = new Document("partner_id", partner);
            Document update2 = new Document("$set", new Document("match_inbound_835", true));
            MongoDBUtils.executeUpdateQuery(partner1Coll, filter2, update2);
        } catch (Exception e) {
            logger.error("error " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            throw e;
        } finally {
            mongoClient.close();
        }
    }

    @Then("apply automation settings for test for {string}")
    public void applyAutomationSettingsForTestFor(String testCaseName) {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        Document testDoc = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB);

        Document settings = (Document) testDoc.get("settings");
        System.out.println("settings = " + settings.toJson());

//        MongoCollection<Document> practiceColl = MongoDBUtils.connectMongodb(mongoClient,partner,"practice_1");
        MongoCollection<Document> partner1Coll = MongoDBUtils.connectMongodb(mongoClient, "directory", "partner_1");
        try {
//            Document filter = new Document("practice_id", practiceId);
//            Document update = new Document()
//                    .append("$set", new Document("settings.era",settings));
////            System.out.println("filter = " + filter.toJson());
////            System.out.println("update = " + update.toJson());
//
//            MongoDBUtils.executeUpdateQuery(practiceColl,filter,update);
            Document filter2 = new Document("partner_id", partner);
            Document update2 = new Document("$set", new Document("match_inbound_835", true));
            MongoDBUtils.executeUpdateQuery(partner1Coll, filter2, update2);
        } catch (Exception e) {
            logger.error("error " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }
    }

    @Then("add {int} seconds wait")
    public void addSecondsWait(int sec) {
        BrowserUtils.waitFor(sec);
    }

    @Given("wait for reprocess")
    public void waitForReprocess() {

        BrowserUtils.waitFor(10);
        System.out.println(apiUtils.ANSI_YELLOW_BACKGROUND + apiUtils.ANSI_RED + "------------------Wait for Reprocess Started------------------" + apiUtils.ANSI_RESET);
        try {
            JsonPath reprocessStatusJson = apiUtils.getReprocessStatus(partner, integrationId);
//          reprocessStatusJson.prettyPrint();
            List<String> tablesToProcess = reprocessStatusJson.getList("custom_reprocess.tables_to_process");
            int tableSize = tablesToProcess.size();
            System.out.println("tablesToProcess.size() = " + tableSize);
            for (int i = 0; i < 30; i++) {
                if (tableSize > 0) {
                    System.out.println("Waiting for reprocess");
                    BrowserUtils.waitFor(5);
                    reprocessStatusJson = apiUtils.getReprocessStatus(partner, integrationId);
                    tablesToProcess = reprocessStatusJson.getList("custom_reprocess.tables_to_process");
                    tableSize = tablesToProcess.size();
                } else {
                    System.out.println("all tables are reprocessed");
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Reprocess tables ERROR...");
            e.printStackTrace();
        }

    }

    @Given("delete old sftp records")
    public void deleteOldSftpRecords() {
        sshUtils.sftpDeleteFilesInFolder("/synthetic-chi/inbound");
//        sshUtils.sftpDeleteFilesInFolder("/synthetic-chi/era_outbound");
        sshUtils.sftpDeleteFilesInFolder("/synthetic-chi/outbound");
        sshUtils.sftpDeleteFilesInFolder("/synthetic-chi/processed_inbound");
        sshUtils.sftpDeleteFilesInFolder("/synthetic-chi/status_outbound");

        sshUtils.sftpDelete820FilesInFolder("/hsbc-chi/pymt_outbound", "hsbc-chi");

        String filePath = "/synthetic-chi/era_outbound";
        sshUtils.sftpDeleteXDayOldRecords(filePath,"synthetic-chi",2);




//        MongoClient mongoClient = MongoDBUtils.getMongoClient();
//        MongoCollection<Document> cycleCountColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, "cycleCount");
//        Bson filter2 = eq("countName", "eraCycle");
//        Document cycleCountDoc = cycleCountColl.find(filter2).first();
////        Date restoreTimeStamp2 = cycleCountDoc.getDate("updatedAt");
////        System.out.println("restoreTimeStamp = " + restoreTimeStamp2);
//
//        Date restoreTimeStamp = cycleCountDoc.getDate("updatedAt");
//        LocalDate restoreLocalDate = restoreTimeStamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//        LocalDate threeDaysBeforeLocalDate = restoreLocalDate.minusDays(2);
//        Date threeDaysBefore = Date.from(threeDaysBeforeLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//
////        System.out.println("Date 2 days before restoreTimeStamp = " + threeDaysBefore);
//
//        List<String> sftpFileNameList835 = sshUtils.sftpGet835FileNameList(filePath);
////        for (String s : sftpFileNameList835) {
////            System.out.println("fileName = " + s);
////        }
////        MongoClient mongoClient = MongoDBUtils.getMongoClient();
//
////        Vector<ChannelSftp.LsEntry> fileEntrylist =
//
//        try {
//            for (String sftpFileName : sftpFileNameList835) {
//                System.out.println("835 file name = " + sftpFileName);
//
//                String fileName = sftpFileName;
//
////                if (sftpFileName.startsWith("2024100406")||sftpFileName.startsWith("2024100407")){
////                    continue;
////                }
//                int fileNameLength = sftpFileName.length();
//                if (fileNameLength > 23) {
////                    System.out.println("835 file name 1= " + sftpFileName);
//                    sftpFileName = sftpFileName.substring(fileNameLength - 23);
////                    System.out.println("835 file name 2= " + sftpFileName);
//                }
//
//                String dateStr = sftpFileName.substring(0, 12);
////                System.out.println("dateStr = " + dateStr);
//
//                Date createDate835 = null;
//                try {
//                    createDate835 = DateUtils.getDate(dateStr, "yyyyMMddHHmm");
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
////                System.out.println("threeDaysBefore = " + threeDaysBefore);
////                System.out.println("createDate835    = " + createDate835);
//                if (createDate835.before(threeDaysBefore) || createDate835 == null) {
//                    sshUtils.sftpDeleteFile(filePath, fileName);
//                }
//
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            mongoClient.close();
//        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ERAAutomation_Defs.class);

    //    boolean isAutoPostQualified;
    @Then("verify era_split_file_assigned collection status: {string}, auto_post.qualified {string}")
    public void verifyEra_split_file_assignedCollectionStatusAuto_postQualified(String expectedStatus, String expectedQualifiedStatus) {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        MongoCollection<Document> testColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
        String filter = "{check_number:'" + checkNumber + "'}";
        BasicDBObject query = BasicDBObject.parse(filter);
        System.out.println("query = " + query);
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();

//        System.out.println("eraSplitFileDoc.toJson() = " + eraSplitFileDoc.toJson());
        JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
//        eraSplitFileJson.prettyPrint();
//        String actualAutoPostQualifiedStatus = String.valueOf(eraSplitFileJson.get("auto_post.qualified"));
        Document filterDoc = Document.parse("{checkNumber:'" + checkNumber + "'}");
        Gson gson = new Gson();
        Map<String, Object> eraSplitFileDocMap = (Map<String, Object>) gson.fromJson(eraSplitFileDoc.toJson(), Map.class).get("auto_post");
        System.out.println("eraSplitFileDocMap = " + eraSplitFileDocMap);
        String eraSplitFileId = eraSplitFileDoc.get("era_split_file_assigned_id").toString();
        System.out.println("eraSplitFileId = " + eraSplitFileId);
        boolean isAutoPostQualified = (boolean) eraSplitFileDocMap.get("qualified");
        boolean isPotentialAutoPostQualified = (boolean) eraSplitFileDocMap.get("potential_qualified");

        Document update = new Document("$set", new Document("isAutoPostQualified", isAutoPostQualified)
                .append("eraSplitFileId", eraSplitFileId)
                .append("isPotentialAutoPostQualified", isPotentialAutoPostQualified)
                .append("updatedAt", new Date()));
//            testColl.updateOne(filterDoc,update);
        MongoDBUtils.executeUpdateQuery(testColl, filterDoc, update);
        System.out.println("isAutoPostQualifed = " + isAutoPostQualified);

        // print rejection reasons if there is any
        if (eraSplitFileDoc != null) {
            // Fetch the rejection_reasons array
            List<Object> rejectionReasons = eraSplitFileDoc.getList("rejection_reasons", Object.class);

            if (rejectionReasons != null && !rejectionReasons.isEmpty()) {
                System.out.println("Rejection Reasons:");
                for (Object reason : rejectionReasons) {
                    System.out.println(reason.toString());
                }
            } else {
                System.out.println("The rejection_reasons array is either null or empty.");
            }
        } else {
            System.out.println("No document found matching the query.");
        }

        String actualAutoPostQualifiedStatus = String.valueOf(isAutoPostQualified);
        System.out.println("actualAutoPostQualifiedStatus = " + actualAutoPostQualifiedStatus);
        try {
            boolean settingsMatched = eraCycleUtils.isAutomationSettingsMatching(partner, practiceId, testCasesDB, testCaseName);
            System.out.println("settingsMatched = " + settingsMatched);
//            Assert.assertTrue(settingsMatched);
        } catch (AssertionError e) {
            boolean settingsMatchedCatchBlok = eraCycleUtils.isAutomationSettingsMatching(partner, practiceId, testCasesDB, testCaseName);
            System.out.println("settingsMatchedCatchBlok = " + settingsMatchedCatchBlok);
            logger.error("auto_post.qualified is False which is not expected " + Arrays.toString(e.getStackTrace()), expectedQualifiedStatus, actualAutoPostQualifiedStatus, e);
            throw e;  // Rethrow the exception so the test still fails
        }

        Assert.assertEquals("auto_post.qualified is False which is not expected", expectedQualifiedStatus, actualAutoPostQualifiedStatus);

        String actualStatus = (String) eraSplitFileDoc.get("status");
        System.out.println("actualStatus = " + actualStatus);
        logUtils.assertEquals("status is not created", expectedStatus, actualStatus);

        mongoClient.close();
    }


    @Then("verify era_split_file_assigned collection status: {string}, auto_post.qualified {string} for {string}")
    public void verifyEra_split_file_assignedCollectionStatusAuto_postQualifiedFor(String expectedStatus, String expectedQualifiedStatus, String testCaseName) {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        MongoCollection<Document> testColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
        String filter = "{check_number:'" + checkNumber + "'}";
        BasicDBObject query = BasicDBObject.parse(filter);
        System.out.println("query = " + query);
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();

        JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
//            String actualAutoPostQualifiedStatus = String.valueOf(eraSplitFileJson.get("auto_post.qualified"));
        Document filterDoc = Document.parse("{checkNumber:'" + checkNumber + "'}");
        String eraSplitFileId = eraSplitFileJson.get("era_split_file_assigned_id");
        boolean isAutoPostQualified = eraSplitFileJson.get("auto_post.qualified");
        boolean isPotentialAutoPostQualified = eraSplitFileJson.get("auto_post.potential_qualified");

        Document update = new Document("$set", new Document("isAutoPostQualified", isAutoPostQualified)
                .append("eraSplitFileId", eraSplitFileId)
                .append("isPotentialAutoPostQualified", isPotentialAutoPostQualified)
                .append("updatedAt", new Date()));
//            testColl.updateOne(filterDoc,update);
        MongoDBUtils.executeUpdateQuery(testColl, filterDoc, update);
        System.out.println("isAutoPostQualifed = " + isAutoPostQualified);
        String actualAutoPostQualifiedStatus = String.valueOf(isAutoPostQualified);
//            System.out.println("actualAutoPostQualifiedStatus = " + actualAutoPostQualifiedStatus);
        try {
            Assert.assertEquals(expectedQualifiedStatus, actualAutoPostQualifiedStatus);
        } catch (AssertionError e) {
            logger.error("auto_post.qualified is False which is not expected " + Arrays.toString(e.getStackTrace()), expectedQualifiedStatus, actualAutoPostQualifiedStatus, e);
            throw e;  // Rethrow the exception so the test still fails
        }

//            Assert.assertEquals("auto_post.qualified is False which is not expected",expectedQualifiedStatus,actualAutoPostQualifiedStatus);

        String actualStatus = eraSplitFileJson.get("status");
        System.out.println("actualStatus = " + actualStatus);
        try {
            Assert.assertEquals("status is not created", expectedStatus, actualStatus);
        } catch (AssertionError e) {
            logger.error("status is not created " + Arrays.toString(e.getStackTrace()), expectedQualifiedStatus, actualAutoPostQualifiedStatus, e);
            throw e;  // Rethrow the exception so the test still fails
        }

        mongoClient.close();

    }

    @Then("verify era_split_file_assigned collection status: {string}")
    public void verifyEra_split_file_assignedCollectionStatus(String expectedStatus) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        System.out.println("query = " + query);
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();

        try {
            String actualStatus = (String) eraSplitFileDoc.get("status");
            System.out.println("actualStatus = " + actualStatus);

//             Wait for the expected status for up to a certain time
            for (int i = 0; i < 100; i++) {
                if (!actualStatus.equals(expectedStatus)) {
                    BrowserUtils.waitFor(1);
                    eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
                    actualStatus = (String) eraSplitFileDoc.get("status");
                    System.out.println("actualStatus while waiting = " + actualStatus);
                } else {
                    break;
                }
            }

            // Check if the status is still "hold" after waiting and expectedStatus is "auto_finalize"
            if (expectedStatus.equals("auto_finalize") && actualStatus.equals("hold")) {
                Document filter = new Document("check_number", checkNumber);
                Document update = new Document("$set", new Document("status", "auto_finalize"));
                MongoDBUtils.executeUpdateQuery(eraSplitFileAssignedColl, filter, update);
                System.out.println("Status has been changed to 'auto_finalize' manually");
                actualStatus = "auto_finalize"; // Update the actualStatus variable for assertion
            }

            // Assert that the final status matches the expected status
            try {
                Assert.assertEquals("Status is not as expected", expectedStatus, actualStatus);
            } catch (AssertionError e) {
                logger.error("Status is not as expected: Expected = " + expectedStatus + ", Actual = " + actualStatus, Arrays.toString(e.getStackTrace()));
                throw e;  // Rethrow the exception so the test still fails
            }
        } catch (Exception e) {
            logger.error("Error: " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }
    }


    @Then("verify era_split_file_assigned collection status: {string} for {string}")
    public void verifyEra_split_file_assignedCollectionStatusFor(String expectedStatus, String testCaseName) {
//        verify era_split_file_assigned.status: 'created' and
        //checkNumber= "jIdsAiBq76CS";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");

        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");

        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        System.out.println("query = " + query);
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
        try {
//            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            String actualStatus = (String) eraSplitFileDoc.get("status");
            System.out.println("actualStatus = " + actualStatus);
            for (int i = 0; i < 300; i++) {
                if (!actualStatus.equals(expectedStatus)) {
                    BrowserUtils.waitFor(2);
                    eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
                    actualStatus = (String) eraSplitFileDoc.get("status");
                    System.out.println("actualStatus while waiting = " + actualStatus);
                } else break;
            }
            try {
                Assert.assertEquals("status is not as expected", expectedStatus, actualStatus);
            } catch (AssertionError e) {
                logger.error("status is not as expected " + e, expectedStatus, actualStatus, Arrays.toString(e.getStackTrace()));
                throw e;  // Rethrow the exception so the test still fails
            }
        } catch (Exception e) {
            logger.error("Error : " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }
    }

    @Then("verify era_split_file_assigned collection status: {string} and {string}")
    public void verifyEra_split_file_assignedCollectionStatusAnd(String firstExpectedStatus, String secondExpectedStatus) {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
        try {
//            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            String actualStatus = (String) eraSplitFileDoc.get("status");
            System.out.println("actualStatus at the beginning = " + actualStatus);
            for (int i = 0; i < 300; i++) {
                if (actualStatus.equals(firstExpectedStatus) || actualStatus.equals(secondExpectedStatus)) {
                    break;
                } else {
                    BrowserUtils.waitFor(1);
                    eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
                    actualStatus = (String) eraSplitFileDoc.get("status");
                    System.out.println("actualStatus while waiting = " + actualStatus);
                }
            }
            System.out.println("actualStatus before transferring check= " + actualStatus);

//            Assert.assertTrue(actualStatus+" is actualStatus ---- Status is neither 'transferring' nor 'transferred'",firstExpectedStatus.equals(actualStatus) || secondExpectedStatus.equals(actualStatus));
            logUtils.assertTrue(firstExpectedStatus.equals(actualStatus) || secondExpectedStatus.equals(actualStatus), actualStatus + " is actualStatus ---- Status is neither 'transferring' nor 'transferred'");

            eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
            actualStatus = (String) eraSplitFileDoc.get("status");
            System.out.println("transferred actualStatus = " + actualStatus);
            for (int i = 0; i < 300; i++) {
                if (!actualStatus.equals(secondExpectedStatus)) {
                    BrowserUtils.waitFor(1);
                    eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
                    actualStatus = (String) eraSplitFileDoc.get("status");
                    System.out.println("actualStatus for transferred while waiting= " + actualStatus);
                } else break;
            }

            System.out.println("actualStatus before transferred check= " + actualStatus);

//            Assert.assertEquals("Status is not 'transferred'", secondExpectedStatus, actualStatus);
            logUtils.assertEquals("Status is not 'transferred'", secondExpectedStatus, actualStatus);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error : " + Arrays.toString(e.getStackTrace()));
            throw e;
        } finally {
            mongoClient.close();
        }
    }

    @Then("verify era_split_file_assigned collection status: {string} and {string} for {string}")
    public void verifyEra_split_file_assignedCollectionStatusAndFor(String firstExpectedStatus, String secondExpectedStatus, String testCaseName) {
//        verify era_split_file_assigned.status: 'created' and
        //checkNumber= "jIdsAiBq76CS";
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
        try {
//            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            String actualStatus = (String) eraSplitFileDoc.get("status");
            System.out.println("actualStatus at the beginning = " + actualStatus);
            for (int i = 0; i < 300; i++) {
                if (actualStatus.equals(firstExpectedStatus) || actualStatus.equals(secondExpectedStatus)) {
                    break;
                } else {
                    BrowserUtils.waitFor(1);
                    eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
                    actualStatus = (String) eraSplitFileDoc.get("status");
                    System.out.println("actualStatus while waiting = " + actualStatus);
                }
            }
            System.out.println("actualStatus before transferring check= " + actualStatus);

//            Assert.assertTrue(actualStatus+" is actualStatus ---- Status is neither 'transferring' nor 'transferred'",firstExpectedStatus.equals(actualStatus) || secondExpectedStatus.equals(actualStatus));
            logUtils.assertTrue(firstExpectedStatus.equals(actualStatus) || secondExpectedStatus.equals(actualStatus), actualStatus + " is actualStatus ---- Status is neither 'transferring' nor 'transferred'");

            eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
            actualStatus = (String) eraSplitFileDoc.get("status");
            System.out.println("transferred actualStatus = " + actualStatus);
            for (int i = 0; i < 300; i++) {
                if (!actualStatus.equals(secondExpectedStatus)) {
                    BrowserUtils.waitFor(1);
                    eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
                    actualStatus = (String) eraSplitFileDoc.get("status");
                    System.out.println("actualStatus for transferred while waiting= " + actualStatus);
                } else break;
            }

            System.out.println("actualStatus before transferred check= " + actualStatus);

//            Assert.assertEquals("Status is not 'transferred'", secondExpectedStatus, actualStatus);
            logUtils.assertEquals("Status is not 'transferred'", secondExpectedStatus, actualStatus);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in line 2581 : " + Arrays.toString(e.getStackTrace()));
        } finally {
            mongoClient.close();
        }
    }

    @Then("verify legder_1 collection category {string}")
    public void verifyLegder_1CollectionCategory(String expectedCategory) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> ledger_1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");
        BasicDBObject query = BasicDBObject.parse("{reference:'" + reference + "',category:'" + expectedCategory + "'}");
        System.out.println("query = " + query);

        Document ledgerDoc = null;

        // Retry logic to wait for the document to be generated
        for (int attempt = 0; attempt < 300; attempt++) {
            ledgerDoc = ledger_1Coll.find(query).first();
            if (ledgerDoc != null) {
                break;
            } else {
                BrowserUtils.waitFor(1);
            }
        }

        // If no document is found after retries, fail the test
        if (ledgerDoc == null) {
            throw new AssertionError("Document not found in ledger_1 collection after waiting.");
        }

        try {
            // Process the found document
            String ledgerJsonString = ledgerDoc.toJson();
            System.out.println("ledgerDoc.toJson() = " + ledgerJsonString);

            JsonPath ledgerJson = JsonPath.from(ledgerJsonString);
            String actualCategory = ledgerJson.get("category");
            System.out.println("actualCategory = " + actualCategory);

            for (int i = 0; i < 300; i++) {
                if (actualCategory.equals(expectedCategory)) {
                    break;
                } else {
                    BrowserUtils.waitFor(1);
                    ledgerDoc = ledger_1Coll.find(query).first();

                    // Ensure null safety in the loop
                    if (ledgerDoc == null) {
                        throw new AssertionError("Document disappeared during processing.");
                    }

                    ledgerJsonString = ledgerDoc.toJson();
                    ledgerJson = JsonPath.from(ledgerJsonString);
                    actualCategory = ledgerJson.get("category");
                    System.out.println("actualCategory while waiting = " + actualCategory);
                }
            }

            logUtils.assertEquals("categories are not matched", expectedCategory, actualCategory);

            if (Objects.equals(actualCategory, "fees")) {
                String actualFees = ledgerJson.get("amount.$numberDecimal");
                try {
                    Assert.assertNotEquals("fee amount is 0", "0", actualFees);
                } catch (AssertionError e) {
                    logger.error("Error : fee amount is 0 " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    throw e;
                }
            }
        } catch (Exception e) {
            logger.error("error : " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            throw e;
        }
    }

    @Then("verify legder_1 collection category {string} for {string}")
    public void verifyLegder_1CollectionCategoryFor(String expectedCategory, String testCaseName) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> ledger_1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");

        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");

        BasicDBObject query = BasicDBObject.parse("{reference:'" + checkNumber + "',category:'" + expectedCategory + "'}");
        System.out.println("query = " + query);
        Document ledgerDoc = ledger_1Coll.find(query).first();
        System.out.println("ledgerDoc.toJson() = " + ledgerDoc.toJson());

        try {
            JsonPath ledgerJson = JsonPath.from(ledgerDoc.toJson());
            String actualCategory = ledgerJson.get("category");
            System.out.println("actualCategory = " + actualCategory);

            for (int i = 0; i < 300; i++) {
                if (actualCategory.equals(expectedCategory)) {
                    break;
                } else {
                    BrowserUtils.waitFor(1);
                    ledgerDoc = ledger_1Coll.find(query).first();
                    ledgerJson = JsonPath.from(ledgerDoc.toJson());
                    actualCategory = ledgerJson.get("category");
                    System.out.println("actualCatefory while waiting = " + actualCategory);
                }
            }

//            Assert.assertEquals(expectedCategory, actualCategory);
            logUtils.assertEquals("categories are not matched", expectedCategory, actualCategory);

            if (Objects.equals(actualCategory, "fees")) {
                String actualFees = ledgerJson.get("amount.$numberDecimal");
                try {
                    Assert.assertNotEquals("fee amount is 0", "0", actualFees);
                } catch (AssertionError e) {
                    logger.error("Error : fee amount is 0 " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    throw e;
                }
            }
        } catch (Exception e) {
            logger.error("error : " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
    }


    @Then("verify legder_1 collection status {string}")
    public void verifyLegder_1CollectionStatus(String expectedStatus) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> ledger_1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");
        BasicDBObject query = BasicDBObject.parse("{reference:'" + reference + "'}");
        System.out.println("query = " + query);
        Document ledgerDoc = ledger_1Coll.find(query).first();
        System.out.println("ledgerDoc.toJson() = " + ledgerDoc.toJson());

        JsonPath ledgerJson = JsonPath.from(ledgerDoc.toJson());
        String actualStatus = ledgerJson.get("status");
        System.out.println("actualStatus = " + actualStatus);
        logUtils.assertEquals("expected status " + expectedStatus + " not matched with actual " + expectedStatus + " ", expectedStatus, actualStatus);
    }


    @Then("verify legder_1 collection status {string} for {string}")
    public void verifyLegder_1CollectionStatusFor(String expectedStatus, String testCaseName) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> ledger_1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");
        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");

        BasicDBObject query = BasicDBObject.parse("{reference:'" + checkNumber + "'}");
        System.out.println("query = " + query);
        Document ledgerDoc = ledger_1Coll.find(query).first();
        System.out.println("ledgerDoc.toJson() = " + ledgerDoc.toJson());

        JsonPath ledgerJson = JsonPath.from(ledgerDoc.toJson());
        String actualStatus = ledgerJson.get("status");
        System.out.println("actualStatus = " + actualStatus);
        logUtils.assertEquals("expected status " + expectedStatus + " not matched with actual " + expectedStatus + " ", expectedStatus, actualStatus);
    }

    @Then("verify legder_1 collection does not have category {string}")
    public void verifyLegder_1CollectionDoesNotHaveCategory(String unexpectedCategory) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> ledger_1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");
        BasicDBObject query = BasicDBObject.parse("{reference:'" + reference + "'}");
        System.out.println("query = " + query);
        FindIterable<Document> ledgerDocs = ledger_1Coll.find(query);

        boolean categoryFound = false;
        try {
            for (Document ledgerDoc : ledgerDocs) {
                JsonPath ledgerJson = JsonPath.from(ledgerDoc.toJson());
                String actualCategory = ledgerJson.get("category");
                System.out.println("actualCategory = " + actualCategory);
                if (unexpectedCategory.equals(actualCategory)) {
                    categoryFound = true;
                    break;
                }
            }
//            Assert.assertFalse("There is a record with unexpectedCategory", categoryFound);
            logUtils.assertFalse(categoryFound, "There is a record with unexpectedCategory");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in line 1987 : " + Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }


    @Then("verify legder_1 collection does not have category {string} for {string}")
    public void verifyLegder_1CollectionDoesNotHaveCategoryFor(String unexpectedCategory, String testCaseName) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> ledger_1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");
        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");
        BasicDBObject query = BasicDBObject.parse("{reference:'" + checkNumber + "'}");
        System.out.println("query = " + query);
        FindIterable<Document> ledgerDocs = ledger_1Coll.find(query);

        boolean categoryFound = false;
        try {
            for (Document ledgerDoc : ledgerDocs) {
                JsonPath ledgerJson = JsonPath.from(ledgerDoc.toJson());
                String actualCategory = ledgerJson.get("category");
                System.out.println("actualCategory = " + actualCategory);
                if (unexpectedCategory.equals(actualCategory)) {
                    categoryFound = true;
                    break;
                }
            }
//            Assert.assertFalse("There is a record with unexpectedCategory", categoryFound);
            logUtils.assertFalse(categoryFound, "There is a record with unexpectedCategory");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error : " + Arrays.toString(e.getStackTrace()));
        }
    }

    @Then("verify retrace approval status as {string}")
    public void verifyRetraceApprovalStatusAs(String expectedApprovalStatus) throws Exception {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject eraQuery = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(eraQuery).first();
        Gson gson = new Gson();
        Map<String, Object> eraSplitFileMap = gson.fromJson(eraSplitFileDoc.toJson(), Map.class);
        Map<String, Object> autoPostMap = (Map<String, Object>) eraSplitFileMap.get("auto_post");
        String autoPost_Approval = null;
        try {
//            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            autoPost_Approval = autoPostMap.get("approval").toString();
            System.out.println("autoPost_Approval = " + autoPost_Approval);
//            Assert.assertEquals("auto_post approval status does not match",expectedApprovalStatus,autoPost_Approval );
        } catch (Exception e) {
            e.printStackTrace();
            // Log the exception with method name and line number
            StackTraceElement[] stackTrace = e.getStackTrace();
            logUtils.logError(stackTrace, e);
        } finally {
            mongoClient.close();
        }
        logUtils.assertEquals("auto_post approval status does not match", expectedApprovalStatus, autoPost_Approval);

    }

    @Then("verify retrace approval status as {string} for {string}")
    public void verifyRetraceApprovalStatusAsFor(String expectedApprovalStatus, String testCaseName) throws Exception {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");
        BasicDBObject eraQuery = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(eraQuery).first();

        try {
            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            String autoPost_Approval = eraSplitFileJson.get("auto_post.approval");
            System.out.println("autoPost_Approval = " + autoPost_Approval);
//            Assert.assertEquals("auto_post approval status does not match",expectedApprovalStatus,autoPost_Approval );
            logUtils.assertEquals("auto_post approval status does not match", expectedApprovalStatus, autoPost_Approval);
        } catch (Exception e) {
            e.printStackTrace();
            // Log the exception with method name and line number
            StackTraceElement[] stackTrace = e.getStackTrace();
            logUtils.logError(stackTrace, e);
        } finally {
            mongoClient.close();
        }
    }

    @Then("verify provider approval status as {string}")
    public void verifyProviderApprovalStatusAs(String expectedApprovalStatus) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");

        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();

        try {
            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            String providerApproval = eraSplitFileJson.getString("auto_post.approvalProvider");
            System.out.println("providerApprove = " + providerApproval);
            Assert.assertEquals(expectedApprovalStatus, providerApproval);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }
    }

    @Then("verify provider approval status as {string} for {string}")
    public void verifyProviderApprovalStatusAsFor(String expectedApprovalStatus, String testCaseName) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();

        try {
            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            String providerApproval = eraSplitFileJson.getString("auto_post.approvalProvider");
            System.out.println("providerApprove = " + providerApproval);
            Assert.assertEquals(expectedApprovalStatus, providerApproval);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }
    }

    @Then("verify era_split_file_assigned collection status: {string} , {string} and {string}")
    public void verifyEra_split_file_assignedCollectionStatusAnd(String transferringExpectedStatus, String transferredExpectedStatus, String receivedExpectedStatus) throws Exception {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
        try {
//            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            String actualStatus = (String) eraSplitFileDoc.get("status");
            System.out.println("actualStatus begining= " + actualStatus);
            for (int i = 0; i < 300; i++) {
                if (actualStatus.equals(transferringExpectedStatus) || actualStatus.equals(transferredExpectedStatus)) {
                    break;
                } else {
                    BrowserUtils.waitFor(1);
                    eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
                    actualStatus = (String) eraSplitFileDoc.get("status");
                    System.out.println("actualStatus while waiting= " + actualStatus);
                }
            }
            System.out.println("actualStatus before transferring check= " + actualStatus);
//            Assert.assertTrue("Status is neither 'transferring' nor 'transferred'",transferringExpectedStatus.equals(actualStatus) || transferredExpectedStatus.equals(actualStatus));

            logUtils.assertTrue(transferringExpectedStatus.equals(actualStatus) || transferredExpectedStatus.equals(actualStatus),
                    "Status is neither 'transferring' nor 'transferred'");

            eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
            actualStatus = (String) eraSplitFileDoc.get("status");
            System.out.println("2. actualStatus = " + actualStatus);
            for (int i = 0; i < 300; i++) {
                if (!actualStatus.equals(transferredExpectedStatus)) {
                    BrowserUtils.waitFor(1);
                    eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
                    actualStatus = (String) eraSplitFileDoc.get("status");
                    System.out.println("actualStatus for 2. while waiting= " + actualStatus);
                } else break;
            }
            System.out.println("actualStatus before transferred check = " + actualStatus);
            try {
                Assert.assertEquals("Status is not 'transferred'", transferredExpectedStatus, actualStatus);
            } catch (AssertionError e) {
                logger.error("Status is not 'transferred' : " + Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
                throw e;
            }
            eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
            actualStatus = (String) eraSplitFileDoc.get("status");
            System.out.println("3. actualStatus = " + actualStatus);
            for (int i = 0; i < 300; i++) {
                if (actualStatus.equals(receivedExpectedStatus)) {
                    break;
                } else {
                    BrowserUtils.waitFor(1);
                    eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
                    actualStatus = (String) eraSplitFileDoc.get("status");
                    System.out.println("actualStatus for 3. while waiting= " + actualStatus);
                }
            }

            System.out.println("actualStatus before received check = " + actualStatus);
            try {
                Assert.assertEquals("Status is not 'received'", receivedExpectedStatus, actualStatus);
            } catch (AssertionError e) {
                logger.error("Status is not 'received' : " + Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
                throw e;
            }
        } catch (Exception e) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            logUtils.logError(stackTrace, e);
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }
    }


    @Then("verify era_split_file_assigned collection status: {string} , {string} and {string} for {string}")
    public void verifyEra_split_file_assignedCollectionStatusAndFor(String transferringExpectedStatus, String transferredExpectedStatus, String receivedExpectedStatus, String testCaseName) throws Exception {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
        try {
//            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
            String actualStatus = (String) eraSplitFileDoc.get("status");
            System.out.println("actualStatus begining= " + actualStatus);
            for (int i = 0; i < 300; i++) {
                if (actualStatus.equals(transferringExpectedStatus) || actualStatus.equals(transferredExpectedStatus)) {
                    break;
                } else {
                    BrowserUtils.waitFor(1);
                    eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
                    actualStatus = (String) eraSplitFileDoc.get("status");
                    System.out.println("actualStatus while waiting= " + actualStatus);
                }
            }
            System.out.println("actualStatus before transferring check= " + actualStatus);
//            Assert.assertTrue("Status is neither 'transferring' nor 'transferred'",transferringExpectedStatus.equals(actualStatus) || transferredExpectedStatus.equals(actualStatus));

            logUtils.assertTrue(transferringExpectedStatus.equals(actualStatus) || transferredExpectedStatus.equals(actualStatus),
                    "Status is neither 'transferring' nor 'transferred'");

            eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
            actualStatus = (String) eraSplitFileDoc.get("status");
            System.out.println("2. actualStatus = " + actualStatus);
            for (int i = 0; i < 300; i++) {
                if (!actualStatus.equals(transferredExpectedStatus)) {
                    BrowserUtils.waitFor(1);
                    eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
                    actualStatus = (String) eraSplitFileDoc.get("status");
                    System.out.println("actualStatus for 2. while waiting= " + actualStatus);
                } else break;
            }
            System.out.println("actualStatus before transferred check = " + actualStatus);
            try {
                Assert.assertEquals("Status is not 'transferred'", transferredExpectedStatus, actualStatus);
            } catch (AssertionError e) {
                logger.error("Status is not 'transferred' : " + Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
                throw e;
            }
            eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
            actualStatus = (String) eraSplitFileDoc.get("status");
            System.out.println("3. actualStatus = " + actualStatus);
            for (int i = 0; i < 300; i++) {
                if (actualStatus.equals(receivedExpectedStatus)) {
                    break;
                } else {
                    BrowserUtils.waitFor(1);
                    eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
                    actualStatus = (String) eraSplitFileDoc.get("status");
                    System.out.println("actualStatus for 3. while waiting= " + actualStatus);
                }
            }

            System.out.println("actualStatus before received check = " + actualStatus);
            try {
                Assert.assertEquals("Status is not 'received'", receivedExpectedStatus, actualStatus);
            } catch (AssertionError e) {
                logger.error("Status is not 'received' : " + Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
                throw e;
            }
        } catch (Exception e) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            e.printStackTrace();
            logUtils.logError(stackTrace, e);
        } finally {
            mongoClient.close();
        }
    }


    @Then("verify writeback from MongoDB and MySQL")
    public void verify_writeback_from_mongo_db_and_my_sql() {
//        checkNumber = "J3ruUNXGPu5x";
        SyncUtils.waitForMongoOdWriteback1Sync(partner, practiceId);
        System.out.println(textColorUtils.ANSI_YELLOW_BACKGROUND + textColorUtils.ANSI_RED + "***************  Verify Writeback Step Started  ***************" + textColorUtils.ANSI_RESET);
        BrowserUtils.waitFor(5);

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        sqlUtils.createConnection(mysqlIp, mysqlDbName, "root", "retrace123");
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        MongoCollection<Document> opendentalWritebackArchive1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "opendental_writeback_archive_1");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        System.out.println("query = " + query);
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
        JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
//            eraSplitFileJson.prettyPrint();

        String writeBackMethod = eraSplitFileJson.getString("write_back_method");
        String eraSplitFileAssignedId = eraSplitFileJson.getString("era_split_file_assigned_id");

        System.out.println("writeBackMethod = " + writeBackMethod);
        System.out.println("eraSplitFileAssignedId = " + eraSplitFileAssignedId);

        List<String> retraceIdList = new ArrayList<>();
        List<Map<String, Object>> writebackListEraSplitFileAssigned = eraSplitFileJson.getList("writeback_records");
        if (writeBackMethod.equals("auto_post")) {
            for (Map<String, Object> recordMap : writebackListEraSplitFileAssigned) {
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//                System.out.println("recordMap = " + recordMap);
                String getClass = recordMap.get("retrace_id").getClass().getSimpleName();
//                System.out.println("getClass = " + getClass);
                JsonPath mapWritebackEraSplitFileJson = convertUtils.mapToJsonpath(recordMap);
//                mapWritebackEraSplitFileJson.prettyPrint();
                boolean blocked = mapWritebackEraSplitFileJson.getBoolean("blocked");

                String retraceId = "";
                if (getClass.equals("String")) {
                    retraceId = mapWritebackEraSplitFileJson.getString("retrace_id");
                } else {
                    Map<String, Object> retraceIdMap = mapWritebackEraSplitFileJson.get("retrace_id");
                    retraceId = (String) retraceIdMap.get("$oid");
                }
                retraceIdList.add(retraceId);
                System.out.println("retraceId = " + retraceId);

//                System.out.println("---------------------------------------------------------");

                ////////////////verify from opendental_writeback_archive_1 collection//////////////////
                BasicDBObject queryWriteback = BasicDBObject.parse("{retrace_id:'" + retraceId + "'}");
//                System.out.println("queryWriteback = " + queryWriteback);

                Document writebackRecord = opendentalWritebackArchive1Coll.find(queryWriteback).first();
                if (blocked) {
                    Assert.assertNull(writebackRecord);
                } else {
                    JsonPath mapOdWritebackArchive = JsonPath.from(writebackRecord.toJson());
//                    mapOdWritebackArchive.prettyPrint();

//                    Gson gson = new Gson();
                    String tableName = mapOdWritebackArchive.getString("table_name");
                    String writeBackAction = mapOdWritebackArchive.getString("write_back_action");
                    System.out.println("tableName = " + tableName);
                    Map<String, Object> dataMapMongo = mapOdWritebackArchive.getMap("data");
                    System.out.println("dataMapMongo = " + dataMapMongo);
//                    Map<String, Object> dataMap3 = gson.fromJson(writebackRecord.toJson(), Map.class);
//                    System.out.println("dataMap3 = " + dataMap3);
//                    Map<String, Object> dataMap2 = (Map<String, Object>) writebackRecord.get("data");
//                    System.out.println("dataMap2 = " + dataMap2);

                    if (tableName.equals("clearinghouse")) {
//                            Assert.assertEquals("era_split_file_assigned_id NOT matched", eraSplitFileAssignedId, dataMapMongo.get("era_file_id"));
//                            Assert.assertEquals("Writeback Action Has to be 'write_back_835_archive' but NOT!!","write_back_835_archive",writeBackAction);

                        logUtils.assertEquals("era_split_file_assigned_id NOT matched", eraSplitFileAssignedId, dataMapMongo.get("era_file_id"));
                        logUtils.assertEquals("Writeback Action Has to be 'write_back_835_archive' but NOT!!", "write_back_835_archive", writeBackAction);

                        String accessKey = ConfigurationReader.get("accessKeyStaging");
                        String secretKey = ConfigurationReader.get("secretKeyStaging");
                        String instanceId = "i-05fea3b9a69d91990";

                        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
                        AWSSimpleSystemsManagement ssmClient = AWSSimpleSystemsManagementClientBuilder.standard()
                                .withRegion(Regions.US_EAST_1)
                                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                                .build();

                        // Execute the command on the EC2 instance
                        String filePath835onOD = (String) dataMapMongo.get("file_name");
                        System.out.println("filePath835onOD = " + filePath835onOD);
                        BrowserUtils.waitFor(5);
                        String file835ContentFromOD = readFileContentOnAws(ssmClient, instanceId, filePath835onOD);

//                        System.out.println("file835Content = " + file835ContentFromOD);

                        try {
                            Assert.assertEquals(dataMapMongo.get("contents"), file835ContentFromOD);
                        } catch (AssertionError e) {
                            e.printStackTrace();
                            logger.error("Error :" + Arrays.toString(e.getStackTrace()));
                            throw e;
                        }
                        // verify file content++++++++++++++++++++++

                    } else {
                        String priKeyQuery = "SELECT COLUMN_NAME\n" +
                                "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE\n" +
                                "WHERE TABLE_NAME = '" + tableName + "'\n" +
                                "  AND CONSTRAINT_NAME = 'PRIMARY'\n" +
                                "  AND TABLE_SCHEMA = '" + mysqlDbName + "';";
                        String primaryKey = (String) sqlUtils.getCellValue(priKeyQuery);
                        System.out.println("primaryKey = " + primaryKey);

                        Object primaryValue = dataMapMongo.get(primaryKey);
                        System.out.println("primaryValue = " + primaryValue);

                        String mySqlQuery = "select * from " + tableName + " where " + primaryKey + " = " + primaryValue + ";";
                        System.out.println("mySqlQuery = " + mySqlQuery);
                        Map<String, Object> dataMapMySQL = sqlUtils.getRowMap(mySqlQuery);
                        System.out.println("dataMapMySQL = " + dataMapMySQL);
                        Set<String> keySetList = dataMapMongo.keySet();
                        String getClassMySQL = "";
                        for (String key : keySetList) {
                            System.out.println("key = " + key);
                            String getClassMongo = dataMapMongo.get(key).getClass().getSimpleName();
                            getClassMySQL = dataMapMySQL.get(key).getClass().getSimpleName();
                            Object valueMongo = dataMapMongo.get(key);
                            Object valueMySQL = dataMapMySQL.get(key);
                            System.out.println("\n-----vales------\n" + getClassMongo + "\n" + getClassMySQL + "\n" + valueMongo + "\n" + valueMySQL + "\n================");

//                        if (!getClassMySQL.toLowerCase().contains("date") &&
//                                !getClassMySQL.toLowerCase().contains("time")
//                                && !getClassMySQL.equals("Double")){
//                            Assert.assertEquals("Records for ***"+key+"*** NOT matched", dataMapMongo.get(key).toString(), dataMapMySQL.get(key).toString());
//                        }
                            if (getClassMySQL.equals("LocalDateTime")) {
                                LocalDateTime mysqlDateTime = LocalDateTime.parse(valueMySQL.toString());
                                DateTimeFormatter mongoFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a");
                                LocalDateTime mongoDateTime = LocalDateTime.parse(valueMongo.toString(), mongoFormatter);

                                boolean isEqual = mongoDateTime.equals(mysqlDateTime);

                                System.out.println("Are the dates equal? " + isEqual);
                                try {
                                    Assert.assertTrue("Dates are not matched", isEqual);
                                } catch (AssertionError e) {
                                    e.printStackTrace();
                                    logger.error("Dates are not matched " + Arrays.toString(e.getStackTrace()));
                                    throw e;
                                }
                            } else if (getClassMySQL.equals("Timestamp")) {
                                DateTimeFormatter mongoFormatter;
                                if (valueMongo.toString().contains("/")){
                                    mongoFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a");
                                } else mongoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

//                                DateTimeFormatter mongoFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a");
                                LocalDateTime mongoDateTime = LocalDateTime.parse(valueMongo.toString(), mongoFormatter);
                                DateTimeFormatter mysqlFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
                                LocalDateTime mysqlDateTime = LocalDateTime.parse(valueMySQL.toString(), mysqlFormatter);

                                boolean isEqual = mongoDateTime.equals(mysqlDateTime);

                                System.out.println("Are the dates equal? " + isEqual);
                                try {
                                    Assert.assertTrue("Dates are not matched", isEqual);
                                } catch (AssertionError e) {
                                    e.printStackTrace();
                                    logger.error("Dates are not matched : " + Arrays.toString(e.getStackTrace()));
                                    throw e;
                                }
                            } else if (getClassMySQL.equals("Date")) {

                                DateTimeFormatter mongoFormatter;
                                if (valueMongo.toString().contains("/")){
                                    mongoFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
                                } else mongoFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");

                                LocalDate mongoDate = LocalDate.parse(valueMongo.toString(), mongoFormatter);
//                                System.out.println("mongoDate = " + mongoDate);
                                LocalDate mysqlDate = LocalDate.parse(valueMySQL.toString());
//                                System.out.println("mysqlDate = " + mysqlDate);

                                boolean isEqual = mongoDate.equals(mysqlDate);

                                System.out.println("Are the dates equal? " + isEqual);
                                logUtils.assertTrue(isEqual, "Dates are not matched");
                            } else if (getClassMySQL.equals("Double")) {
                                double valueMongoDouble = Double.parseDouble(valueMongo.toString());
//                            System.out.println("valueMongoDouble = " + valueMongoDouble);
                                logUtils.assertEquals("Records for ***" + key + "*** NOT matched", valueMongoDouble, valueMySQL);
                            } else if (getClassMySQL.equals("String") && key.equals("Remarks") && valueMongo.toString().length()>255) {
                                String valueMongoStringShort = valueMongo.toString().substring(0,255);
//                            System.out.println("valueMongoStringShort = " + valueMongoStringShort);
                                logUtils.assertEquals("Records for ***" + key + "*** NOT matched", valueMongoStringShort, valueMySQL);
                            } else {
                                logUtils.assertEquals("Records for ***" + key + "*** NOT matched", dataMapMongo.get(key).toString(), dataMapMySQL.get(key).toString());
                            }

                        }

                    }

                    String[] keys = {
                            "write_back_action",
                            "practice_id",
                            "table_name"
                    };

                    for (String key : keys) {
//                        System.out.println("key = " + key);
                        logUtils.assertEquals("Records for ***" + key + "*** NOT matched", mapWritebackEraSplitFileJson.getString(key), mapOdWritebackArchive.getString(key));
                    }

                    logUtils.assertEquals("Records for *** Transaction id *** NOT matched", eraSplitFileJson.getString("writeback_transaction_id"), mapOdWritebackArchive.getString("transaction_id"));

                    logUtils.assertEquals("Writeback NOT successful!!!", "success", mapOdWritebackArchive.getString("execution_status"));
                }
            }
        } else if (writeBackMethod.equals("file")) {
            boolean isAutoPostQualifed2 = eraSplitFileJson.get("auto_post.qualified");
            System.out.println("isAutoPostQualifed = " + isAutoPostQualifed2);

//                try {
//                    Assert.assertFalse(isAutoPostQualified);
//                } catch (AssertionError e) {
//                    e.printStackTrace();
//                    logger.error("isAutoPostQualified NOT successful!!! "+ Arrays.toString(e.getStackTrace()));
//                    throw e;
//                }
//                    Assert.assertEquals(isAutoPostQualifed2,isAutoPostQualifed);

            BasicDBObject query2 = BasicDBObject.parse("{'data.era_file_id':'" + eraSplitFileAssignedId + "'}");
            System.out.println("query2 = " + query2);
            Document opendentalWritebackArchive1Doc = opendentalWritebackArchive1Coll.find(query2).first();
            JsonPath opendentalWritebackArchive1Json = JsonPath.from(opendentalWritebackArchive1Doc.toJson());

            String tableName = opendentalWritebackArchive1Json.getString("table_name");
            System.out.println("tableName = " + tableName);
            Map<String, Object> dataMapMongo = opendentalWritebackArchive1Json.getMap("data");
            System.out.println("dataMapMongo = " + dataMapMongo);

            logUtils.assertEquals("era_split_file_assigned_id NOT matched",
                    eraSplitFileJson.getString("era_split_file_assigned_id"),
                    dataMapMongo.get("era_file_id"));

            String accessKey = ConfigurationReader.get("accessKeyStaging");
            String secretKey = ConfigurationReader.get("secretKeyStaging");
            String instanceId = "i-05fea3b9a69d91990";

            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
            AWSSimpleSystemsManagement ssmClient = AWSSimpleSystemsManagementClientBuilder.standard()
                    .withRegion(Regions.US_EAST_1)
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .build();

            // Execute the command on the EC2 instance
            String filePath835onOD = (String) dataMapMongo.get("file_name");
            System.out.println("filePath835onOD = " + filePath835onOD);
            BrowserUtils.waitFor(5);
            String file835ContentFromOD = readFileContentOnAws(ssmClient, instanceId, filePath835onOD);
            String file835ContentFromMongo = dataMapMongo.get("contents").toString();

            System.out.println("file835Content from opendental era file = " + file835ContentFromOD);
            System.out.println("file835Content from mongodb    era file = " + file835ContentFromMongo);

            logUtils.assertEquals("835 file content NOT matched! ", file835ContentFromMongo, file835ContentFromOD);


        } else System.out.println("Writeback Method Not Populated!!!!");

        mongoClient.close();

    }


    @Then("verify writeback from MongoDB and MySQL for {string}")
    public void verifyWritebackFromMongoDBAndMySQLFor(String testCaseName) {
//        checkNumber = "J3ruUNXGPu5x";
        SyncUtils.waitForMongoOdWriteback1Sync(partner, practiceId);
        System.out.println(textColorUtils.ANSI_YELLOW_BACKGROUND + textColorUtils.ANSI_RED + "***************  Verify Writeback Step Started  ***************" + textColorUtils.ANSI_RESET);
        BrowserUtils.waitFor(5);
        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");

        try (MongoClient mongoClient = MongoDBUtils.getMongoClient()) {
            sqlUtils.createConnection(mysqlIp, mysqlDbName, "root", "retrace123");
            MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
            MongoCollection<Document> opendentalWritebackArchive1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "opendental_writeback_archive_1");
            BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
            System.out.println("query = " + query);
            Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
//            eraSplitFileJson.prettyPrint();

            String writeBackMethod = eraSplitFileJson.getString("write_back_method");
            String eraSplitFileAssignedId = eraSplitFileJson.getString("era_split_file_assigned_id");

            System.out.println("writeBackMethod = " + writeBackMethod);
            System.out.println("eraSplitFileAssignedId = " + eraSplitFileAssignedId);

            List<String> retraceIdList = new ArrayList<>();
            List<Map<String, Object>> writebackListEraSplitFileAssigned = eraSplitFileJson.getList("writeback_records");
            if (writeBackMethod.equals("auto_post")) {
                for (Map<String, Object> recordMap : writebackListEraSplitFileAssigned) {
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//                System.out.println("recordMap = " + recordMap);
                    String getClass = recordMap.get("retrace_id").getClass().getSimpleName();
//                System.out.println("getClass = " + getClass);
                    JsonPath mapWritebackEraSplitFileJson = convertUtils.mapToJsonpath(recordMap);
//                mapWritebackEraSplitFileJson.prettyPrint();
                    boolean blocked = mapWritebackEraSplitFileJson.getBoolean("blocked");

                    String retraceId = "";
                    if (getClass.equals("String")) {
                        retraceId = mapWritebackEraSplitFileJson.getString("retrace_id");
                    } else {
                        Map<String, Object> retraceIdMap = mapWritebackEraSplitFileJson.get("retrace_id");
                        retraceId = (String) retraceIdMap.get("$oid");
                    }
                    retraceIdList.add(retraceId);
                    System.out.println("retraceId = " + retraceId);

//                System.out.println("---------------------------------------------------------");

                    ////////////////verify from opendental_writeback_archive_1 collection//////////////////
                    BasicDBObject queryWriteback = BasicDBObject.parse("{retrace_id:'" + retraceId + "'}");
//                System.out.println("queryWriteback = " + queryWriteback);

                    Document writebackRecord = opendentalWritebackArchive1Coll.find(queryWriteback).first();
                    if (blocked) {
                        Assert.assertNull(writebackRecord);
                    } else {
                        JsonPath mapOdWritebackArchive = JsonPath.from(writebackRecord.toJson());
//                    mapOdWritebackArchive.prettyPrint();

//                    Gson gson = new Gson();
                        String tableName = mapOdWritebackArchive.getString("table_name");
                        String writeBackAction = mapOdWritebackArchive.getString("write_back_action");
                        System.out.println("tableName = " + tableName);
                        Map<String, Object> dataMapMongo = mapOdWritebackArchive.getMap("data");
                        System.out.println("dataMapMongo = " + dataMapMongo);
//                    Map<String, Object> dataMap3 = gson.fromJson(writebackRecord.toJson(), Map.class);
//                    System.out.println("dataMap3 = " + dataMap3);
//                    Map<String, Object> dataMap2 = (Map<String, Object>) writebackRecord.get("data");
//                    System.out.println("dataMap2 = " + dataMap2);

                        if (tableName.equals("clearinghouse")) {
//                            Assert.assertEquals("era_split_file_assigned_id NOT matched", eraSplitFileAssignedId, dataMapMongo.get("era_file_id"));
//                            Assert.assertEquals("Writeback Action Has to be 'write_back_835_archive' but NOT!!","write_back_835_archive",writeBackAction);

                            logUtils.assertEquals("era_split_file_assigned_id NOT matched", eraSplitFileAssignedId, dataMapMongo.get("era_file_id"));
                            logUtils.assertEquals("Writeback Action Has to be 'write_back_835_archive' but NOT!!", "write_back_835_archive", writeBackAction);

                            String accessKey = ConfigurationReader.get("accessKeyStaging");
                            String secretKey = ConfigurationReader.get("secretKeyStaging");
                            String instanceId = "i-05fea3b9a69d91990";

                            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
                            AWSSimpleSystemsManagement ssmClient = AWSSimpleSystemsManagementClientBuilder.standard()
                                    .withRegion(Regions.US_EAST_1)
                                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                                    .build();

                            // Execute the command on the EC2 instance
                            String filePath835onOD = (String) dataMapMongo.get("file_name");
                            System.out.println("filePath835onOD = " + filePath835onOD);
                            BrowserUtils.waitFor(5);
                            String file835ContentFromOD = readFileContentOnAws(ssmClient, instanceId, filePath835onOD);

//                        System.out.println("file835Content = " + file835ContentFromOD);

                            try {
                                Assert.assertEquals(dataMapMongo.get("contents"), file835ContentFromOD);
                            } catch (AssertionError e) {
                                e.printStackTrace();
                                logger.error("Error :" + Arrays.toString(e.getStackTrace()));
                                throw e;
                            }
                            // verify file content++++++++++++++++++++++

                        } else {
                            String priKeyQuery = "SELECT COLUMN_NAME\n" +
                                    "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE\n" +
                                    "WHERE TABLE_NAME = '" + tableName + "'\n" +
                                    "  AND CONSTRAINT_NAME = 'PRIMARY'\n" +
                                    "  AND TABLE_SCHEMA = '" + mysqlDbName + "';";
                            String primaryKey = (String) sqlUtils.getCellValue(priKeyQuery);
                            System.out.println("primaryKey = " + primaryKey);

                            Object primaryValue = dataMapMongo.get(primaryKey);
                            System.out.println("primaryValue = " + primaryValue);

                            String mySqlQuery = "select * from " + tableName + " where " + primaryKey + " = " + primaryValue + ";";
                            Map<String, Object> dataMapMySQL = sqlUtils.getRowMap(mySqlQuery);
                            System.out.println("dataMapMySQL = " + dataMapMySQL);
                            Set<String> keySetList = dataMapMongo.keySet();
                            String getClassMySQL = "";
                            for (String key : keySetList) {
                                System.out.println("key = " + key);
                                String getClassMongo = dataMapMongo.get(key).getClass().getSimpleName();
                                getClassMySQL = dataMapMySQL.get(key).getClass().getSimpleName();
                                Object valueMongo = dataMapMongo.get(key);
                                Object valueMySQL = dataMapMySQL.get(key);
                                System.out.println("vales = " + "\n-----------\n" + getClassMongo + "\n" + getClassMySQL + "\n" + valueMongo + "\n" + valueMySQL + "\n-----------");

//                        if (!getClassMySQL.toLowerCase().contains("date") &&
//                                !getClassMySQL.toLowerCase().contains("time")
//                                && !getClassMySQL.equals("Double")){
//                            Assert.assertEquals("Records for ***"+key+"*** NOT matched", dataMapMongo.get(key).toString(), dataMapMySQL.get(key).toString());
//                        }
                                if (getClassMySQL.equals("LocalDateTime")) {
                                    LocalDateTime mysqlDateTime = LocalDateTime.parse(valueMySQL.toString());
                                    DateTimeFormatter mongoFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a");
                                    LocalDateTime mongoDateTime = LocalDateTime.parse(valueMongo.toString(), mongoFormatter);

                                    boolean isEqual = mongoDateTime.equals(mysqlDateTime);

                                    System.out.println("Are the dates equal? " + isEqual);
                                    try {
                                        Assert.assertTrue("Dates are not matched", isEqual);
                                    } catch (AssertionError e) {
                                        e.printStackTrace();
                                        logger.error("Dates are not matched " + Arrays.toString(e.getStackTrace()));
                                        throw e;
                                    }
                                } else if (getClassMySQL.equals("Timestamp")) {
                                    DateTimeFormatter mongoFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a");
                                    LocalDateTime mongoDateTime = LocalDateTime.parse(valueMongo.toString(), mongoFormatter);
                                    DateTimeFormatter mysqlFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
                                    LocalDateTime mysqlDateTime = LocalDateTime.parse(valueMySQL.toString(), mysqlFormatter);

                                    boolean isEqual = mongoDateTime.equals(mysqlDateTime);

                                    System.out.println("Are the dates equal? " + isEqual);
                                    try {
                                        Assert.assertTrue("Dates are not matched", isEqual);
                                    } catch (AssertionError e) {
                                        e.printStackTrace();
                                        logger.error("Dates are not matched " + Arrays.toString(e.getStackTrace()));
                                        throw e;
                                    }
                                } else if (getClassMySQL.equals("Date")) {
                                    DateTimeFormatter mongoFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
                                    LocalDate mongoDate = LocalDate.parse(valueMongo.toString(), mongoFormatter);
                                    LocalDate mysqlDate = LocalDate.parse(valueMySQL.toString());

                                    boolean isEqual = mongoDate.equals(mysqlDate);

                                    System.out.println("Are the dates equal? " + isEqual);
                                    try {
                                        Assert.assertTrue("Dates are not matched", isEqual);
                                    } catch (AssertionError e) {
                                        e.printStackTrace();
                                        logger.error("Dates are not matched " + Arrays.toString(e.getStackTrace()));
                                        throw e;
                                    }
                                } else if (getClassMySQL.equals("Double")) {
                                    double valueMongoDouble = Double.parseDouble(valueMongo.toString());
//                            System.out.println("valueMongoDouble = " + valueMongoDouble);

                                    try {
                                        Assert.assertEquals("Records for ***" + key + "*** NOT matched", valueMongoDouble, valueMySQL);
                                    } catch (AssertionError e) {
                                        e.printStackTrace();
                                        logger.error("Records for ***" + key + "*** NOT matched " + Arrays.toString(e.getStackTrace()));
                                        throw e;
                                    }

                                } else {

                                    try {
                                        Assert.assertEquals("Records for ***" + key + "*** NOT matched", dataMapMongo.get(key).toString(), dataMapMySQL.get(key).toString());
                                    } catch (AssertionError e) {
                                        e.printStackTrace();
                                        logger.error("Records for ***" + key + "*** NOT matched " + Arrays.toString(e.getStackTrace()));
                                        throw e;
                                    }
                                }

                            }

                        }


                        String[] keys = {
                                "write_back_action",
                                "practice_id",
                                "table_name"
                        };

                        for (String key : keys) {
//                        System.out.println("key = " + key);
                            try {
                                Assert.assertEquals("Records for ***" + key + "*** NOT matched", mapWritebackEraSplitFileJson.getString(key), mapOdWritebackArchive.getString(key));
                            } catch (AssertionError e) {
                                e.printStackTrace();
                                logger.error("Records for ***" + key + "*** NOT matched " + Arrays.toString(e.getStackTrace()));
                                throw e;
                            }
                        }
                        try {
                            Assert.assertEquals("Records for *** Transaction id *** NOT matched", eraSplitFileJson.getString("writeback_transaction_id"), mapOdWritebackArchive.getString("transaction_id"));
                        } catch (AssertionError e) {
                            e.printStackTrace();
                            logger.error("Records for *** Transaction id *** NOT matched " + Arrays.toString(e.getStackTrace()));
                            throw e;
                        }

                        try {
                            Assert.assertEquals("Writeback NOT successful!!!", "success", mapOdWritebackArchive.getString("execution_status"));
                        } catch (AssertionError e) {
                            e.printStackTrace();
                            logger.error("Writeback NOT successful!!! " + Arrays.toString(e.getStackTrace()));
                            throw e;
                        }
                    }
                }
            } else if (writeBackMethod.equals("file")) {
                boolean isAutoPostQualifed2 = eraSplitFileJson.get("auto_post.qualified");
                System.out.println("isAutoPostQualifed = " + isAutoPostQualifed2);

//                try {
//                    Assert.assertFalse(isAutoPostQualified);
//                } catch (AssertionError e) {
//                    e.printStackTrace();
//                    logger.error("isAutoPostQualified NOT successful!!! "+Arrays.toString(e.getStackTrace()));
//                    throw e;
//                }
//                    Assert.assertEquals(isAutoPostQualifed2,isAutoPostQualifed);

                BasicDBObject query2 = BasicDBObject.parse("{'data.era_file_id':'" + eraSplitFileAssignedId + "'}");
                System.out.println("query2 = " + query2);
                Document opendentalWritebackArchive1Doc = opendentalWritebackArchive1Coll.find(query2).first();
                JsonPath opendentalWritebackArchive1Json = JsonPath.from(opendentalWritebackArchive1Doc.toJson());

                String tableName = opendentalWritebackArchive1Json.getString("table_name");
                System.out.println("tableName = " + tableName);
                Map<String, Object> dataMapMongo = opendentalWritebackArchive1Json.getMap("data");
                System.out.println("dataMapMongo = " + dataMapMongo);

                try {
                    Assert.assertEquals("era_split_file_assigned_id NOT matched", eraSplitFileJson.getString("era_split_file_assigned_id"), dataMapMongo.get("era_file_id"));
                } catch (AssertionError e) {
                    e.printStackTrace();
                    logger.error("era_split_file_assigned_id NOT matched! " + Arrays.toString(e.getStackTrace()));
                    throw e;
                }

                String accessKey = ConfigurationReader.get("accessKeyStaging");
                String secretKey = ConfigurationReader.get("secretKeyStaging");
                String instanceId = "i-05fea3b9a69d91990";

                BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
                AWSSimpleSystemsManagement ssmClient = AWSSimpleSystemsManagementClientBuilder.standard()
                        .withRegion(Regions.US_EAST_1)
                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                        .build();

                // Execute the command on the EC2 instance
                String filePath835onOD = (String) dataMapMongo.get("file_name");
                System.out.println("filePath835onOD = " + filePath835onOD);
                BrowserUtils.waitFor(5);
                String file835ContentFromOD = readFileContentOnAws(ssmClient, instanceId, filePath835onOD);
                String file835ContentFromMongo = dataMapMongo.get("contents").toString();

                System.out.println("file835Content from opendental era file = " + file835ContentFromOD);
                System.out.println("file835Content from mongodb    era file = " + file835ContentFromMongo);

                try {
                    Assert.assertEquals(file835ContentFromMongo, file835ContentFromOD);
                } catch (AssertionError e) {
                    e.printStackTrace();
                    logger.error("835 file content NOT matched! " + Arrays.toString(e.getStackTrace()));
                    throw e;
                }
                // verify file content++++++++++++++++++++++

            } else System.out.println("Writeback Method Not Populated!!!!");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error :" + Arrays.toString(e.getStackTrace()));
        }
    }


    private static String readFileContentOnAws(AWSSimpleSystemsManagement ssmClient, String instanceId, String filePath) {

//        String filePath2 = "C:\\ClaimConnect\\ReportCLCStaging\\" + filePath;
        String command = "Get-Content -Path \"" + filePath + "\" -Raw";
        String fileContent = "";

        SendCommandRequest sendCommandRequest = new SendCommandRequest()
                .withInstanceIds(instanceId)
                .withDocumentName("AWS-RunPowerShellScript")
                .withParameters(
                        new HashMap<String, List<String>>() {{
                            put("commands", Arrays.asList(command));
                        }}
                );

        SendCommandResult sendCommandResult = ssmClient.sendCommand(sendCommandRequest);
        String commandId = sendCommandResult.getCommand().getCommandId();

        // Monitor the command execution
        GetCommandInvocationRequest getCommandInvocationRequest = new GetCommandInvocationRequest()
                .withCommandId(commandId)
                .withInstanceId(instanceId);

        int maxRetries = 50;  // Set the maximum number of retries
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                GetCommandInvocationResult getCommandInvocationResult = ssmClient.getCommandInvocation(getCommandInvocationRequest);

                if (!getCommandInvocationResult.getStatus().equals("InProgress")) {
                    String output = getCommandInvocationResult.getStandardOutputContent();
                    String error = getCommandInvocationResult.getStandardErrorContent();

                    if (output == null || output.isEmpty()) {
                        System.out.println("File is empty or not found.");
                    } else {
                        fileContent = output.trim();
                        System.out.println("File content: " + fileContent);
                    }

                    // Handle errors
                    if (error != null && !error.isEmpty()) {
                        System.out.println("Command error: " + error);
                    }

                    System.out.println("Command status: " + getCommandInvocationResult.getStatus());
                    break;
                }

                // Sleep for a while before retrying
                Thread.sleep(1000);
                retryCount++;
            } catch (InvocationDoesNotExistException e) {
                System.out.println("Invocation does not exist yet, retrying...");
                try {
                    Thread.sleep(2000); // Wait before retrying
                    retryCount++;
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }

            if (retryCount >= maxRetries) {
                System.out.println("Reached maximum retry limit. Command status could not be confirmed.");
            }
        }
        return fileContent;
    }

    @Then("verify writeback records created")
    public void verify_writeback_records_created(List<String> writebackTablesList) {

        SyncUtils.waitForWritebackSync(partner, practiceId);
        BrowserUtils.waitFor(2);
        SyncUtils.waitForSyncOdToRetrace(mysqlIp, partner, practiceId, mysqlDbName);
        BrowserUtils.waitFor(2);
        int expectedTableCount = writebackTablesList.size();
//            System.out.println("writebackTablesList.size() = " + expectedTableCount);

//        BrowserUtils.waitFor(1);
        try (MongoClient mongoClient = MongoDBUtils.getMongoClient()) {

            MongoCollection<Document> opendentalWritebackArchive1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "opendental_writeback_archive_1");
            MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
            BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
            System.out.println("query = " + query);
            Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
//            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
//                eraSplitFileJson.prettyPrint();

            String writeBackMethod = eraSplitFileDoc.getString("write_back_method");
            String eraSplitFileAssignedId = eraSplitFileDoc.getString("era_split_file_assigned_id");

            System.out.println("writeBackMethod = " + writeBackMethod);
            System.out.println("eraSplitFileAssignedId = " + eraSplitFileAssignedId);

            int tableCount = 0;
            List<Map<String, Object>> writebackListEraSplitFileAssigned = (List<Map<String, Object>>) eraSplitFileDoc.get("writeback_records");
            System.out.println("writebackListEraSplitFileAssigned = " + writebackListEraSplitFileAssigned);
            List<String> tableNamesERAMongo = new ArrayList<>();
            if (writeBackMethod.equals("auto_post")) {
                tableCount = writebackListEraSplitFileAssigned.size();
//                System.out.println("tableCount = " + tableCount);
                for (Map<String, Object> writebackMap : writebackListEraSplitFileAssigned) {
//                    System.out.println("writebackMap = " + writebackMap);
                    String tableNameMap = writebackMap.get("table_name").toString();
//                    System.out.println("tableNameMap = " + tableNameMap);
                    tableNamesERAMongo.add(tableNameMap);
                }
                System.out.println("tableNamesERAMongo = " + tableNamesERAMongo);
            } else if (writeBackMethod.equals("file")) {
                BasicDBObject query2 = BasicDBObject.parse("{'data.era_file_id':'" + eraSplitFileAssignedId + "'}");
                System.out.println("query2 = " + query2);
                Document opendentalWritebackArchive1Doc = opendentalWritebackArchive1Coll.find(query2).first();
//                System.out.println("opendentalWritebackArchive1Doc.toJson() = " + opendentalWritebackArchive1Doc.toJson());
                JsonPath opendentalWritebackArchive1Json = JsonPath.from(opendentalWritebackArchive1Doc.toJson());
//                opendentalWritebackArchive1Json.prettyPrint();
                String tableNameStr = opendentalWritebackArchive1Json.getString("table_name");
                tableNamesERAMongo.add(tableNameStr);
                tableCount = tableNamesERAMongo.size();
//                System.out.println("tableNamesERAMongo = " + tableNamesERAMongo);
            }

//            for (String tableExpected : writebackTablesList) {
//                System.out.println("tableExpected = " + tableExpected);
//            }
//            for (String namesMongo : tableNamesMongo) {
//                System.out.println("namesMongo = " + namesMongo);
//            }
            Set<String> set1 = new HashSet<>(tableNamesERAMongo);
            Set<String> set2 = new HashSet<>(writebackTablesList);
            boolean isEqual = set1.equals(set2);
            Set<String> differences = new HashSet<>();
            if (!isEqual) {
                Set<String> tempSet1 = new HashSet<>(set1);
                Set<String> tempSet2 = new HashSet<>(set2);
                tempSet1.removeAll(set2); // Elements in set1 but not in set2
                tempSet2.removeAll(set1); // Elements in set2 but not in set1

                System.out.println("tableNamesMongo: " + tableNamesERAMongo);
                System.out.println("writebackTablesList = " + writebackTablesList);

                differences.addAll(tempSet1);
                differences.addAll(tempSet2);
                System.out.println("differences = " + differences);
            }
            logUtils.assertTrue(isEqual, differences + " table/s NOT matched ");
            logUtils.assertEquals("Table content NOT matched! ", expectedTableCount, tableCount);

        }
    }


    @Then("verify writeback records created for {string}")
    public void verifyWritebackRecordsCreatedFor(String testCaseName, List<String> writebackTablesList) {

        SyncUtils.waitForWritebackSync(partner, practiceId);
        BrowserUtils.waitFor(2);
        SyncUtils.waitForSyncOdToRetrace(mysqlIp, partner, practiceId, mysqlDbName);
        BrowserUtils.waitFor(2);
        int expectedTableCount = writebackTablesList.size();
//            System.out.println("writebackTablesList.size() = " + expectedTableCount);

//        BrowserUtils.waitFor(1);
        try (MongoClient mongoClient = MongoDBUtils.getMongoClient()) {

            String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");
            MongoCollection<Document> opendentalWritebackArchive1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "opendental_writeback_archive_1");
            MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
            BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
            System.out.println("query = " + query);
            Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
//                eraSplitFileJson.prettyPrint();

            String writeBackMethod = eraSplitFileJson.getString("write_back_method");
            String eraSplitFileAssignedId = eraSplitFileJson.getString("era_split_file_assigned_id");

            System.out.println("writeBackMethod = " + writeBackMethod);
            System.out.println("eraSplitFileAssignedId = " + eraSplitFileAssignedId);

            int tableCount = 0;
            List<Map<String, Object>> writebackListEraSplitFileAssigned = eraSplitFileJson.getList("writeback_records");
            List<String> tableNamesERAMongo = new ArrayList<>();
            if (writeBackMethod.equals("auto_post")) {
                tableCount = writebackListEraSplitFileAssigned.size();
                tableNamesERAMongo = eraSplitFileJson.getList("writeback_records.table_name");
                System.out.println("tableNamesERAMongo = " + tableNamesERAMongo);
            } else if (writeBackMethod.equals("file")) {
                BasicDBObject query2 = BasicDBObject.parse("{'data.era_file_id':'" + eraSplitFileAssignedId + "'}");
                System.out.println("query2 = " + query2);
                Document opendentalWritebackArchive1Doc = opendentalWritebackArchive1Coll.find(query2).first();
//                System.out.println("opendentalWritebackArchive1Doc.toJson() = " + opendentalWritebackArchive1Doc.toJson());
                JsonPath opendentalWritebackArchive1Json = JsonPath.from(opendentalWritebackArchive1Doc.toJson());
//                opendentalWritebackArchive1Json.prettyPrint();
                String tableNameStr = opendentalWritebackArchive1Json.getString("table_name");
                tableNamesERAMongo.add(tableNameStr);
                tableCount = tableNamesERAMongo.size();
//                System.out.println("tableNamesERAMongo = " + tableNamesERAMongo);
            }

//            for (String tableExpected : writebackTablesList) {
//                System.out.println("tableExpected = " + tableExpected);
//            }
//            for (String namesMongo : tableNamesMongo) {
//                System.out.println("namesMongo = " + namesMongo);
//            }
            Set<String> set1 = new HashSet<>(tableNamesERAMongo);
            Set<String> set2 = new HashSet<>(writebackTablesList);
            boolean isEqual = set1.equals(set2);
            Set<String> differences = new HashSet<>();
            if (!isEqual) {
                Set<String> tempSet1 = new HashSet<>(set1);
                Set<String> tempSet2 = new HashSet<>(set2);
                tempSet1.removeAll(set2); // Elements in set1 but not in set2
                tempSet2.removeAll(set1); // Elements in set2 but not in set1

                System.out.println("tableNamesMongo: " + tableNamesERAMongo);
                System.out.println("writebackTablesList = " + writebackTablesList);

                differences.addAll(tempSet1);
                differences.addAll(tempSet2);
                System.out.println("differences = " + differences);
            }
            try {
                Assert.assertTrue(differences + " table/s NOT matched ", isEqual);
            } catch (AssertionError e) {
                e.printStackTrace();
                logger.error(differences + " table/s NOT matched! " + Arrays.toString(e.getStackTrace()));
                throw e;
            }

            try {
                Assert.assertEquals(expectedTableCount, tableCount);
            } catch (AssertionError e) {
                e.printStackTrace();
                logger.error("Table content NOT matched! " + Arrays.toString(e.getStackTrace()));
                throw e;
            }

        }
    }

    @Then("verify etrans835_1.status as {string}")
    public void verifyEtrans835_1StatusAs(String expectedEtrans835Status) {

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        MongoCollection<Document> etrans835Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "etrans835_1");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        System.out.println("query = " + query);
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
        JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
//            eraSplitFileJson.prettyPrint();

        List<Map<String, Object>> writebackListEraSplitFileAssigned = new ArrayList<>();

        writebackListEraSplitFileAssigned = eraSplitFileJson.getList("writeback_records");
        for (Map<String, Object> recordMap : writebackListEraSplitFileAssigned) {
//            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            String getClass = recordMap.get("retrace_id").getClass().getSimpleName();
            JsonPath mapWritebackEraSplitFileJson = convertUtils.mapToJsonpath(recordMap);
            String retraceId = "";
            if (getClass.equals("String")) {
                retraceId = mapWritebackEraSplitFileJson.getString("retrace_id");
            } else {
                Map<String, Object> retraceIdMap = mapWritebackEraSplitFileJson.get("retrace_id");
                retraceId = (String) retraceIdMap.get("$oid");
            }
            String tableName = (String) recordMap.get("table_name");

            if (tableName.equals("etrans835")) {
                System.out.println("tableName = " + tableName);
                BasicDBObject queryetrans835 = BasicDBObject.parse("{_id:ObjectId('" + retraceId + "')}");
                Document etrans835Record = etrans835Coll.find(queryetrans835).first();
                System.out.println("etrans835Record.toJson() = " + etrans835Record.toJson());
                JsonPath jsonEtrans835Record = JsonPath.from(etrans835Record.toJson());
//                jsonEtrans835Record.prettyPrint();

                logUtils.assertEquals("etrans835 status does NOT match", expectedEtrans835Status, jsonEtrans835Record.getString("status"));

            }
        }

    }

    @Then("verify etrans835_1.status as {string} for {string}")
    public void verifyEtrans835_1StatusAs(String expectedEtrans835Status, String testCaseName) {
        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");

        try (MongoClient mongoClient = MongoDBUtils.getMongoClient()) {
            MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
            MongoCollection<Document> etrans835Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "etrans835_1");
            BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
            System.out.println("query = " + query);
            Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
            JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
//            eraSplitFileJson.prettyPrint();

            List<Map<String, Object>> writebackListEraSplitFileAssigned = new ArrayList<>();

            writebackListEraSplitFileAssigned = eraSplitFileJson.getList("writeback_records");
            for (Map<String, Object> recordMap : writebackListEraSplitFileAssigned) {
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                String getClass = recordMap.get("retrace_id").getClass().getSimpleName();
                JsonPath mapWritebackEraSplitFileJson = convertUtils.mapToJsonpath(recordMap);
                String retraceId = "";
                if (getClass.equals("String")) {
                    retraceId = mapWritebackEraSplitFileJson.getString("retrace_id");
                } else {
                    Map<String, Object> retraceIdMap = mapWritebackEraSplitFileJson.get("retrace_id");
                    retraceId = (String) retraceIdMap.get("$oid");
                }
                String tableName = (String) recordMap.get("table_name");
                System.out.println("tableName = " + tableName);

                if (tableName.equals("etrans835")) {
                    BasicDBObject queryetrans835 = BasicDBObject.parse("{_id:'ObjectId('" + retraceId + "')}");
                    Document etrans835Record = etrans835Coll.find(queryetrans835).first();
                    JsonPath jsonEtrans835Record = JsonPath.from(etrans835Record.toJson());

                    try {
                        Assert.assertEquals("etrans835 status does NOT match", expectedEtrans835Status, jsonEtrans835Record.getString("status"));
                    } catch (AssertionError e) {
                        e.printStackTrace();
                        logger.error("etrans835 status does NOT match " + Arrays.toString(e.getStackTrace()));
                        throw e;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error :" + Arrays.toString(e.getStackTrace()));
        }
    }

    @Given("verify Test Case Health")
    public void verifyTestCaseHealth() {
        for (Map<String, Object> testCase : testCaseList) {
            boolean isTestCaseCreated = false;
            boolean isCheckNumberFetched = false;
            boolean isClaimSubmitted = false;
            boolean is820downloaded = false;
            boolean is820uplodedToS3 = false;
            boolean is835Created = false;
//            System.out.println("testCase = " + testCase);
            String testCaseName = (String) testCase.get("testCaseName");
            System.out.println("Test case health verification started for = " + testCaseName);

            try (MongoClient mongoClient = MongoDBUtils.getMongoClient()) {

                List<Document> testCaseHealthErrorList = new ArrayList<>();

                BasicDBObject query = BasicDBObject.parse("{testCaseName:'" + testCaseName + "'}");
                MongoCollection<Document> eraTestColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
                Document testDoc = eraTestColl.find(query).first();
                if (testDoc != null) {
                    isTestCaseCreated = true;
//                    System.out.println("testDoc.toJson() = " + testDoc.toJson());
                    String claimIdentifier = testDoc.getString("claimIdentifier");
                    List<String> fileName837List = (List<String>) testDoc.get("fileName837");
//                    List<String> file837ContentList = (List<String>) testDoc.get("file837Content");
                    String checkNumber = testDoc.getString("checkNumber");
                    String fileName837 = null;
                    if (fileName837List.size() > 0) {
                        fileName837 = fileName837List.get(0);
                    }
                    String fileName835 = testDoc.getString("fileName835");
                    String claimSubmitSuccess = testDoc.getString("claimSubmitSuccess");
                    String download820Success = testDoc.getString("download820Success");
                    String upload820ToS3Success = testDoc.getString("upload820ToS3Success");

                    System.out.println("fileName837 = " + fileName837);
                    System.out.println("fileName835 = " + fileName835);
                    System.out.println("checkNumber = " + checkNumber);
                    System.out.println("claimIdentifier = " + claimIdentifier);
//                    System.out.println("fileName837List = " + fileName837List);
//                    System.out.println("file837Content = " + file837ContentList);
                    System.out.println("claimSubmitSuccess = " + claimSubmitSuccess);
                    System.out.println("download820Success = " + download820Success);
                    System.out.println("upload820ToS3Success = " + upload820ToS3Success);
                    if (checkNumber != null) {
                        isCheckNumberFetched = true;
                    } else {
                        Map<String, Object> errorMap = new HashMap<>();
                        errorMap.put("isCheckNumberFetched", isCheckNumberFetched);
                        testCaseHealthErrorList.add(new Document(errorMap));
                    }

                    if (claimSubmitSuccess.equals("success")) {
                        isClaimSubmitted = true;
                    } else {
                        Map<String, Object> errorMap = new HashMap<>();
                        errorMap.put("isClaimSubmitted", isClaimSubmitted);
                        testCaseHealthErrorList.add(new Document(errorMap));
                    }

                    if (download820Success.contains(".820 downloded succesfully")) {
                        is820downloaded = true;
                    } else {
                        Map<String, Object> errorMap = new HashMap<>();
                        errorMap.put("is820downloaded", is820downloaded);
                        testCaseHealthErrorList.add(new Document(errorMap));
                    }

                    if (upload820ToS3Success.contains(".820 uploaded to server succesfully")) {
                        is820uplodedToS3 = true;
                    } else {
                        Map<String, Object> errorMap = new HashMap<>();
                        errorMap.put("is820uplodedToS3", is820uplodedToS3);
                        testCaseHealthErrorList.add(new Document(errorMap));
                    }

                    if (fileName837 != null && fileName837List.size() > 0) {
                        is835Created = true;
                    } else {
                        Map<String, Object> errorMap = new HashMap<>();
                        errorMap.put("is835Created", is835Created);
                        testCaseHealthErrorList.add(new Document(errorMap));
                    }

                } else {
                    System.out.println("!!!!!!!!Test Case NOT Created!!!!!!!!");
                    break;
                }
                System.out.println("testCaseHealthErrorList = " + testCaseHealthErrorList);
                if (!testCaseHealthErrorList.isEmpty()) {
                    for (Map<String, Object> testCaseMap : testCaseHealthErrorList) {
                        for (String key : testCaseMap.keySet()) {
                            System.out.println(key + " = " + testCaseMap.get(key));
                        }
                    }
                }


                System.out.println(
                        "Test Case Result = " +
                                isTestCaseCreated + " , " +
                                isClaimSubmitted + " , " +
                                isCheckNumberFetched + " , " +
                                is835Created + " , " +
                                is820downloaded + " , " +
                                is820uplodedToS3);

                boolean isTestCaseHealthy = isTestCaseCreated && is835Created && isCheckNumberFetched && isClaimSubmitted && is820downloaded && is820uplodedToS3;
                System.out.println("isTestCaseHealthy = " + isTestCaseHealthy);
                String filter = "{testCaseName:'" + testCaseName + "'}";
                Document filterDoc = Document.parse(filter);
                String update = "";
                if (isTestCaseHealthy) {
                    update = "{$set:{isTestCaseHealthy:true, testStatus: 'waitingForTest'}}";
                } else {
                    update = "{$set:{isTestCaseHealthy:false}}";
                    Document updateDoc = new Document("$set", new Document("testHealthErrorList", testCaseHealthErrorList));

                    System.out.println("updateDoc = " + updateDoc.toJson());
                    MongoDBUtils.executeUpdateQuery(eraTestColl, filterDoc, updateDoc);
                }


                MongoDBUtils.executeUpdateQuery(eraTestColl, filter, update);
                System.out.println("-------------------------------------------------------------------");
            }

        }

    }

    public void exportErrorsToMongo(String errorBody, String testCaseName) {
//        checkNumber = "6lBD8oVBRhlZ";
        System.out.println("checkNumber = " + checkNumber);
        System.out.println("collectionName = " + collectionName);
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> testColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);

        Document filterDoc = Document.parse("{testCaseName:'" + testCaseName + "'}");
        Document update = new Document("$set", new Document("errorLog", errorBody));
//            testColl.updateOne(filterDoc,update);
        MongoDBUtils.executeUpdateQuery(testColl, filterDoc, update);

        mongoClient.close();

    }

    @Then("verify ERA matches Ledger")
    public void verifyERAMatchesLedger() {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        System.out.println("partner = " + partner);
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        MongoCollection<Document> ledgerColl = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");
        MongoCollection<Document> testColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);

        BasicDBObject eraQuery = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        BasicDBObject ledgerQuery = BasicDBObject.parse("{reference:'" + reference + "',category:'inspay'}");
        System.out.println("eraQuery = " + eraQuery);
        System.out.println("ledgerQuery = " + ledgerQuery);

        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(eraQuery).first();
        Document ledgerDoc = ledgerColl.find(ledgerQuery).first();

//        JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
        JsonPath ledgerJson = JsonPath.from(ledgerDoc.toJson());

        //        era_split_file_assigned assertions
        String eraEraSplitFileAssignedId = eraSplitFileDoc.getString("era_split_file_assigned_id");
        System.out.println("eraSplitFileAssignedId = " + eraEraSplitFileAssignedId);
        logUtils.assertNotNull(eraEraSplitFileAssignedId, "era_split_file_assigned_id NOT found in era_split_file_assigned Collection");
//        Assert.assertNotNull(eraEraSplitFileAssignedId);
        ObjectId eraSplitFileAssignedledgerObjId = eraSplitFileDoc.getObjectId("ledger_objid");
        logUtils.assertNotNull(eraSplitFileAssignedledgerObjId, "ledger_objid NOT found in era_split_file_assigned Collection");
//        Assert.assertNotNull(eraSplitFileAssignedLedgerId);
        boolean matching_bpr_835_820 = (boolean) eraSplitFileDoc.get("matching_bpr_835_820");
//        Assert.assertTrue(matching_bpr_835_820);
        logUtils.assertTrue(matching_bpr_835_820, "matching_bpr_835_820 NOT true in era_split_file_assigned Collection");

        //        ledger_1 assertions

        String ledgerEraSplitFileAssignedId = ledgerJson.getString("era_split_file_assigned_id");
        System.out.println("ledgerEraSplitFileAssignedId = " + ledgerEraSplitFileAssignedId);
        logUtils.assertNotNull(ledgerEraSplitFileAssignedId, "era_split_file_assigned_id NOT found in ledger_1 Collection");
        ObjectId ledgerLedgerObjId = ledgerDoc.getObjectId("_id");
        logUtils.assertNotNull(ledgerLedgerObjId, "ledger_id NOT found in ledger_1 Collection");
        boolean tin_mismatch = ledgerJson.get("tin_mismatch");
        logUtils.assertFalse(tin_mismatch, "tin_mismatch NOT false in ledger_1 Collection");


        logUtils.assertEquals("era_split_file_assigned_id does not match", eraEraSplitFileAssignedId, ledgerEraSplitFileAssignedId);
        logUtils.assertEquals("ledger_id does not match", eraSplitFileAssignedledgerObjId, ledgerLedgerObjId);


        String ledgerId = ledgerLedgerObjId.toHexString();;
        Document filterDoc = Document.parse("{checkNumber:'" + checkNumber + "'}");
        Document update = new Document("$set", new Document("ledgerId", ledgerId)
                .append("matchingBpr835820", matching_bpr_835_820)
                .append("tinMismatch", tin_mismatch)
                .append("updatedAt", new Date()));
//            testColl.updateOne(filterDoc,update);
        MongoDBUtils.executeUpdateQuery(testColl, filterDoc, update);
        mongoClient.close();

    }

    @Then("verify ERA matches Ledger for {string}")
    public void verifyERAMatchesLedgerFor(String testCaseName) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        MongoCollection<Document> ledgerColl = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");
        MongoCollection<Document> testColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);

        String checkNumber = eraCycleUtils.getTestCase(testCaseName, collectionName, testCasesDB).getString("checkNumber");
        BasicDBObject eraQuery = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        BasicDBObject ledgerQuery = BasicDBObject.parse("{reference:'" + checkNumber + "',category:'inspay'}");
        System.out.println("eraQuery = " + eraQuery);
        System.out.println("ledgerQuery = " + ledgerQuery);

        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(eraQuery).first();
        Document ledgerDoc = ledgerColl.find(ledgerQuery).first();

        JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
        JsonPath ledgerJson = JsonPath.from(ledgerDoc.toJson());

        //        era_split_file_assigned assertions
        String eraEraSplitFileAssignedId = eraSplitFileJson.getString("era_split_file_assigned_id");
        System.out.println("eraSplitFileAssignedId = " + eraEraSplitFileAssignedId);
        logUtils.assertNotNull(eraEraSplitFileAssignedId, "era_split_file_assigned_id NOT found in era_split_file_assigned Collection");
//        Assert.assertNotNull(eraEraSplitFileAssignedId);
        String eraSplitFileAssignedLedgerId = eraSplitFileJson.getString("ledger_id");
        logUtils.assertNotNull(eraSplitFileAssignedLedgerId, "ledger_id NOT found in era_split_file_assigned Collection");
//        Assert.assertNotNull(eraSplitFileAssignedLedgerId);
        boolean matching_bpr_835_820 = eraSplitFileJson.get("matching_bpr_835_820");
//        Assert.assertTrue(matching_bpr_835_820);
        logUtils.assertTrue(matching_bpr_835_820, "matching_bpr_835_820 NOT true in era_split_file_assigned Collection");

        //        ledger_1 assertions

        String ledgerEraSplitFileAssignedId = ledgerJson.getString("era_split_file_assigned_id");
        System.out.println("ledgerEraSplitFileAssignedId = " + ledgerEraSplitFileAssignedId);
        logUtils.assertNotNull(ledgerEraSplitFileAssignedId, "era_split_file_assigned_id NOT found in ledger_1 Collection");
//        Assert.assertNotNull(ledgerEraSplitFileAssignedId);
        String ledgerLedgerId = ledgerJson.getString("ledger_id");
        logUtils.assertNotNull(ledgerLedgerId, "ledger_id NOT found in ledger_1 Collection");
//        Assert.assertNotNull(ledgerLedgerId);
        boolean tin_mismatch = ledgerJson.get("tin_mismatch");
        logUtils.assertFalse(tin_mismatch, "tin_mismatch NOT false in ledger_1 Collection");
//        Assert.assertFalse(tin_mismatch);


        logUtils.assertEquals("era_split_file_assigned_id does not match", eraEraSplitFileAssignedId, ledgerEraSplitFileAssignedId);
        logUtils.assertEquals("ledger_id does not match", eraSplitFileAssignedLedgerId, ledgerLedgerId);

//        Assert.assertEquals("era_split_file_assigned_id does not match",eraEraSplitFileAssignedId,ledgerEraSplitFileAssignedId );
//        Assert.assertEquals("ledger_id does not match",eraSplitFileAssignedLedgerId,ledgerLedgerId );

        String ledgerId = ledgerLedgerId;
        Document filterDoc = Document.parse("{checkNumber:'" + checkNumber + "'}");
        Document update = new Document("$set", new Document("ledgerId", ledgerId)
                .append("matchingBpr835820", matching_bpr_835_820)
                .append("tinMismatch", tin_mismatch)
                .append("updatedAt", new Date()));
//            testColl.updateOne(filterDoc,update);
        MongoDBUtils.executeUpdateQuery(testColl, filterDoc, update);
        mongoClient.close();

    }

    @Given("delete downloaded old 820 files")
    public void deleteDownloadedOld820Files() {
        fileUtils.deleteFileEndsWith(".820", BrowserUtils.getDownloadPath());
    }

    @Given("delete old 820 files from server")
    public void deleteOld820FilesFromServer() {
        sshUtils.emptyDirectoryOnServer(serverIp, server820UploadPath);
    }

    @Given("archive old test data database {string}")
    public void archiveOldTestDataDatabase(String databaseName) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();

        MongoCollection<Document> cycleCountColl = MongoDBUtils.connectMongodb(mongoClient, databaseName, "cycleCount");
        Document filterDoc = Document.parse("{countName:'eraCycle'}");

        Document cycleCountDoc = cycleCountColl.find(filterDoc).first();
//        System.out.println("cycleCountDoc.toJson() = " + cycleCountDoc.toJson());
        String countNum = cycleCountDoc.get("eraCycleCount").toString();
//        System.out.println("eraCycleCount = " + countNum);

        MongoDatabase database = mongoClient.getDatabase(databaseName);

        String sourceCollectionName = collectionName;
        String targetCollectionName = collectionName + "_archive_" + countNum;

        MongoCollection<Document> sourceCollection = database.getCollection(sourceCollectionName);

        // Drop the target collection if it already exists
        if (database.getCollection(targetCollectionName) != null) {
            database.getCollection(targetCollectionName).drop();
        }

        MongoCollection<Document> targetCollection = database.getCollection(targetCollectionName);
        for (Document doc : sourceCollection.find()) {
            targetCollection.insertOne(doc);
        }

        System.out.println("Collection copied successfully to --> " + targetCollectionName + " collection");


//        cycleCountDoc = cycleCountColl.find(filterDoc).first();
//        System.out.println("cycleCountDoc.toJson() = " + cycleCountDoc.toJson());
        int eraCycleCount = Integer.parseInt(countNum);
        eraCycleCount++;
        eraCycleCountStr = String.valueOf(eraCycleCount);
        System.out.println("eraCycleCount = " + eraCycleCountStr);

        Document update = new Document("$set", new Document("eraCycleCount", eraCycleCountStr)
                .append("updatedAt", new Date()));
        MongoDBUtils.executeUpdateQuery(cycleCountColl, filterDoc, update);

        mongoClient.close();
    }


    @Given("clean old archive collections keep latest {int}")
    public void cleanOldArchiveCollectionsKeepLatest(int keepCount) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase(testCasesDB);

        String archivePrefix = "eraCycleTestCases_archive_";

        // Get the list of all collections in the database
        MongoIterable<String> collections = database.listCollectionNames();
        List<String> archiveCollections = new ArrayList<>();

        // Filter and collect the archived collections
        for (String collectionName : collections) {
            if (collectionName.startsWith(archivePrefix)) {
                archiveCollections.add(collectionName);
            }
        }

        // Sort the collections based on the numeric suffix
        Collections.sort(archiveCollections, (a, b) -> {
            int aNum = Integer.parseInt(a.replace(archivePrefix, ""));
            int bNum = Integer.parseInt(b.replace(archivePrefix, ""));
            return Integer.compare(aNum, bNum);
        });

        // Determine how many collections to delete
        int collectionsToDelete = archiveCollections.size() - keepCount;

        if (collectionsToDelete > 0) {
            for (int i = 0; i < collectionsToDelete; i++) {
                String collectionToDelete = archiveCollections.get(i);
                database.getCollection(collectionToDelete).drop();
                System.out.println("Deleted collection: " + collectionToDelete);
            }
        }

        System.out.println("Cleanup complete. Kept the latest " + keepCount + " ERA-Cycle test results.");
        mongoClient.close();
    }

    @And("generate eraCycle report")
    public void generateEraCycleReport() throws GeneralSecurityException, IOException {

        List<Map<String, Object>> testCaseListWithResults = new ArrayList<>();

        Gson gson = new Gson();
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraTestColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);
        MongoCollection<Document> cycleCountColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, "cycleCount");

        BasicDBObject queryCount = BasicDBObject.parse("{countName:'eraCycle'}");
        Document cycleCountDoc = cycleCountColl.find(queryCount).first();
        String cycleCount = cycleCountDoc.getString("eraCycleCount");

        for (Map<String, Object> testCaseListLine : testCaseList) {

//            System.out.println("testCaseListLine = " + testCaseListLine);
            Map<String, Object> newTestCaseListLine = new HashMap<>();

            String testCaseName = testCaseListLine.get("testCaseName").toString();
            System.out.println("--------------------------------------------------------\ntestCaseName = " + testCaseName);

            BasicDBObject query = BasicDBObject.parse("{testCaseName:'" + testCaseName + "'}");
//        System.out.println("query = " + query);
            Document testDoc = eraTestColl.find(query).first();
            JsonPath testCaseMongoJson = JsonPath.from(testDoc.toJson());
//            testCaseMongoJson.prettyPrint();
            Map<String, Object> testCaseMongoMap = gson.fromJson(testDoc.toJson(), Map.class);


//            if (testCaseMongoJson.get("errorLog.error_logs")==null){
//
//            }

            newTestCaseListLine.put("testId", testCaseListLine.get("testId"));
            newTestCaseListLine.put("testCaseName", testCaseName);
            newTestCaseListLine.put("isTestCaseHealthy", testCaseMongoMap.get("isTestCaseHealthy"));
            newTestCaseListLine.put("claimIdentifier", testCaseListLine.get("claimIdentifier"));
            newTestCaseListLine.put("purpose", testCaseListLine.get("purpose"));
            newTestCaseListLine.put("testStatus", testCaseMongoMap.get("testStatus"));
            newTestCaseListLine.put("errorLogs", testCaseMongoJson.get("errorLog.error_logs"));
//            newTestCaseListLine.put("isTestCaseHealthy",testCaseMongoMap.get("isTestCaseHealthy"));
//            newTestCaseListLine.put("isTestCaseHealthy",testCaseMongoMap.get("isTestCaseHealthy"));
//            newTestCaseListLine.put("isTestCaseHealthy",testCaseMongoMap.get("isTestCaseHealthy"));
//            newTestCaseListLine.put("isTestCaseHealthy",testCaseMongoMap.get("isTestCaseHealthy"));
//            newTestCaseListLine.put("isTestCaseHealthy",testCaseMongoMap.get("isTestCaseHealthy"));


            System.out.println("newTestCaseListLine = " + newTestCaseListLine);


            testCaseListWithResults.add(newTestCaseListLine);

        }


        String spreadsheetId = "1v4b21ceKj5dnxkPo_uDbHx4bf9GSgGLomyG4alsM8Hc";
//        String range = "eraAutomationTestClaimList!A1:AD100";
        String credentialPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" +
                File.separator + "resources" + File.separator + "Downloads" + File.separator + "eraAutomation" +
                File.separator + "cred.json";

//        System.out.println("CREDENTIALS_FILE_PATH = " + CREDENTIALS_FILE_PATH);
        ExcelUtil.exportDataToGoogleSheet(spreadsheetId, "eraCycleTestResult" + cycleCount, testCaseListWithResults, credentialPath);
//        testCaseList.forEach(System.out::println);
    }


    @When("change SVC balance to unbalance ERA")
    public void changeSVCBalanceToUnbalanceERA() {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();

        // get the content hash
        MongoCollection<Document> eraSplitFileAssigned_1_Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + reference + "'}");
        System.out.println("check_number = " + query);
        Document eraSplitFileAssignedDoc = eraSplitFileAssigned_1_Coll.find(query).first();
//        System.out.println("eraSplitFileAssignedDoc.toJson() = " + eraSplitFileAssignedDoc.toJson());
//        JsonPath eraSplitFileAssignedJson = JsonPath.from(eraSplitFileAssignedDoc.toJson());
        String contentHash = (String) eraSplitFileAssignedDoc.get("content_hash");

        // update the SVC amount

        MongoCollection<Document> svc835AssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "svc_835_assigned");
        BasicDBObject querySvc = BasicDBObject.parse("{st_hash:'" + contentHash + "','matched_claim_proc_1.claim_proc_id':'3102'}");
        System.out.println("querySvc = " + querySvc);
        Document svcDoc = svc835AssignedColl.find(querySvc).first();
        System.out.println("svcDoc.toJson() = " + svcDoc.toJson());
        JsonPath svcJson = JsonPath.from(svcDoc.toJson());
        String svcAmount = svcJson.getString("ins_paid");
        System.out.println("svcAmount = " + svcAmount);

        // New value for svcAmount
        String newSvcAmount = "10";

        // Update the ins_paid field
        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("ins_paid", newSvcAmount);

        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);

        svc835AssignedColl.updateOne(querySvc, setQuery);

        System.out.println("Origininal SVC amount: " + svcAmount + "updated to newSvcAmount: " + newSvcAmount);
    }

    @And("run auto_post qualify script")
    public void runAuto_postQualifyScript() {
        // get the era_split_file_assigned_id
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssigned_1_Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + reference + "'}");
        System.out.println("query = " + query);
        Document eraSplitFileAssignedDoc = eraSplitFileAssigned_1_Coll.find(query).first();
//        System.out.println("eraSplitFileAssignedDoc = " + eraSplitFileAssignedDoc.toJson());
        JsonPath eraSplitFileAssignedJson = JsonPath.from(eraSplitFileAssignedDoc.toJson());
        String eraSplitFileAssignedId = eraSplitFileAssignedJson.get("era_split_file_assigned_id");

        String ip = serverIp;
        String script = "node ./background/qualify-era-split-file-assigned.js --partner-id " + partner + " --era-split-file-assigned-id " + eraSplitFileAssignedId;
        System.out.println("script = " + script);
        //node ./background/qualify-era-split-file-assigned.js --partner-id han-ped-dev-test-a --era-split-file-assigned-id 36qHoBsqxVb14z6K

//        for (int i = 0; i < 200; i++) {
//            if (!MongoDBUtils.getSingletonSettings()) {
                System.out.println("----started- script run to auto-post qualify----");
                MongoDBUtils.pushAndKeepSettings(partner, practiceId, logger, testCaseName, script, testCasesDB);
                sshUtils.runScriptsLogToFile(script, ip, testCaseName + "_autoPostQualify", testCasesDB);
                BrowserUtils.waitFor(0.5);
                MongoDBUtils.setSingletonSettings(false, "notKept", "none", testCasesDB);
                BrowserUtils.waitFor(1);
//                break;
//            } else {
//                System.out.println("----waiting for singleton script run auto-post qualify----");
//                BrowserUtils.waitFor(3);
//            }
//        }
    }

    @Then("verify error messages")
    public void verifyErrorMessages() {
        // get the error messages
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssigned_1_Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + reference + "'}");
        System.out.println("query = " + query);
        Document eraSplitFileAssignedDoc = eraSplitFileAssigned_1_Coll.find(query).first();
//        System.out.println("eraSplitFileAssignedDoc = " + eraSplitFileAssignedDoc.toJson());
        JsonPath eraSplitFileAssignedJson = JsonPath.from(eraSplitFileAssignedDoc.toJson());
        List<String> autoPostWarnings = eraSplitFileAssignedJson.getList("auto_post_warning");
        List<String> claimLevelRejectionReasons = eraSplitFileAssignedJson.getList("claim_level_rejection_reasons");
        int numberOfClaimLevelRejectionReasons = claimLevelRejectionReasons.size();

//        Assert.assertEquals("autoPostWarnings message does not match", "CLPs and SVCs do not balance.", autoPostWarnings.get(0));
//        Assert.assertEquals("autoPostWarnings message does not match", "BPR, PLB and SVCs do not balance.", autoPostWarnings.get(1));
        String practiceShort = practiceId.substring(0,11);
        System.out.println("practiceShort = " + practiceShort);
        String expectedClaimLevelRejectionReasonMessage = "CLP+1000/734+"+practiceShort+" CLP03(300.00): CLP - SVC balancing disqualification";
        String expectedClaimLevelRejectionReasonMessageAdjustment = "CLP+1696/733+"+practiceShort+" CLP03(285.00): CLP - disqualification Disqualified due to undercharged negative patient responsibility ERA automations setting";

        String actualClaimLevelRejectionReasonMessage = claimLevelRejectionReasons.get(numberOfClaimLevelRejectionReasons - 2);
        String actualClaimLevelRejectionReasonMessageAdjustment = claimLevelRejectionReasons.get(numberOfClaimLevelRejectionReasons - 1);

        System.out.println("actualClaimLevelRejectionReasonMessage = " + actualClaimLevelRejectionReasonMessage);
        System.out.println("actualClaimLevelRejectionReasonMessageAdjustment = " + actualClaimLevelRejectionReasonMessageAdjustment);

        Assert.assertEquals("claimLevelRejectionReason message does not match", expectedClaimLevelRejectionReasonMessage, actualClaimLevelRejectionReasonMessage);
        Assert.assertEquals("Adjustment claimLevelRejectionReason message does not match", expectedClaimLevelRejectionReasonMessageAdjustment, actualClaimLevelRejectionReasonMessageAdjustment);
    }

    @When("delete SVC record to unbalance ERA")
    public void deleteSVCRecordToUnbalanceERA() {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();

        // get the content hash
        MongoCollection<Document> eraSplitFileAssigned_1_Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + reference + "'}");
        System.out.println("query = " + query);
        Document eraSplitFileAssignedDoc = eraSplitFileAssigned_1_Coll.find(query).first();
//        System.out.println("eraSplitFileAssignedDoc.toJson() = " + eraSplitFileAssignedDoc.toJson());
//        JsonPath eraSplitFileAssignedJson = JsonPath.from(eraSplitFileAssignedDoc.toJson());
        String contentHash = (String) eraSplitFileAssignedDoc.get("content_hash");

        // update the SVC amount

        MongoCollection<Document> svc835AssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "svc_835_assigned");
        BasicDBObject querySvc = BasicDBObject.parse("{st_hash:'" + contentHash + "','matched_claim_proc_1.claim_proc_id':'3108'}");
        System.out.println("querySvc = " + querySvc);
        Document svcDoc = svc835AssignedColl.find(querySvc).first();
        if (svcDoc != null) {
            System.out.println("svcDoc.toJson() = " + svcDoc.toJson());

            // Deleting the document that matches the query
            svc835AssignedColl.deleteOne(querySvc);
            System.out.println("Record deleted successfully.");
        } else {
            System.out.println("No record found matching the query.");
        }
    }

    @Then("verify ERA is embargoed")
    public void verifyERAIsEmbargoed() {
        BrowserUtils.waitFor(300);

        Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned(partner, checkNumber);
        String status = eraSplitFileDoc.getString("status");
        System.out.println("assignedCollectionStatus = " + status);
        BrowserUtils.waitFor(5);
        if (eligibleForEmbargoe) {
            Assert.assertEquals("status is not created", "created", status);
        } else {
            Assert.assertEquals("status is not created", "transferred", status);

        }

    }

    @Then("verify error messages for deleted SVC record")
    public void verifyErrorMessagesForDeletedSVCRecord() {
        // get the error messages
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssigned_1_Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + reference + "'}");
        System.out.println("query = " + query);
        Document eraSplitFileAssignedDoc = eraSplitFileAssigned_1_Coll.find(query).first();
//        System.out.println("eraSplitFileAssignedDoc = " + eraSplitFileAssignedDoc.toJson());
        JsonPath eraSplitFileAssignedJson = JsonPath.from(eraSplitFileAssignedDoc.toJson());
        List<String> autoPostWarnings = eraSplitFileAssignedJson.getList("auto_post_warning");
        List<String> claimLevelRejectionReasons = eraSplitFileAssignedJson.getList("claim_level_rejection_reasons");
        int numberOfClaimLevelRejectionReasons = claimLevelRejectionReasons.size();

//        Assert.assertEquals("autoPostWarnings message does not match", "CLPs and SVCs do not balance.", autoPostWarnings.get(0));
//        Assert.assertEquals("autoPostWarnings message does not match", "BPR, PLB and SVCs do not balance.", autoPostWarnings.get(1));
        String practiceShort = practiceId.substring(0,11);
        String expectedClaimLevelRejectionReasonMessage = "CLP+1677/735+"+practiceShort+" CLP03(280.00): CLP - SVC balancing disqualification";
        String actualClaimLevelRejectionReasonMessage = claimLevelRejectionReasons.get(numberOfClaimLevelRejectionReasons - 1);
        System.out.println("actualClaimLevelRejectionReasonMessage = " + actualClaimLevelRejectionReasonMessage);

        Assert.assertEquals("claimLevelRejectionReason message does not match", expectedClaimLevelRejectionReasonMessage, actualClaimLevelRejectionReasonMessage);
    }

    @When("ERA is transferred manually")
    public void eraIsTransferredManually() {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();

        // get the content hash
        MongoCollection<Document> eraSplitFileAssigned_1_Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + reference + "'}");
        System.out.println("check_number = " + query);
        Document eraSplitFileAssignedDoc = eraSplitFileAssigned_1_Coll.find(query).first();
        System.out.println("eraSplitFileAssignedDoc.toJson() = " + eraSplitFileAssignedDoc.toJson());
        eraSplitFileAssigned_1_Coll.updateOne(eq("check_number", reference), set("status", "received"));

//        JsonPath eraSplitFileAssignedJson = JsonPath.from(eraSplitFileAssignedDoc.toJson());
        String eraStatus = (String) eraSplitFileAssignedDoc.get("status");
        System.out.println("eraStatus = " + eraStatus);
    }

    @And("change practice value for partial claim")
    public void changePracticeValueForPartialClaim() {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();

        // get the content hash
        MongoCollection<Document> eraSplitFileAssigned_1_Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + reference + "'}");
        System.out.println("check_number = " + query);
        Document eraSplitFileAssignedDoc = eraSplitFileAssigned_1_Coll.find(query).first();
//        System.out.println("eraSplitFileAssignedDoc.toJson() = " + eraSplitFileAssignedDoc.toJson());
//        JsonPath eraSplitFileAssignedJson = JsonPath.from(eraSplitFileAssignedDoc.toJson());
        String contentHash = (String) eraSplitFileAssignedDoc.get("content_hash");
//        System.out.println("contentHash = " + contentHash);

        // update the CLP practice info

        MongoCollection<Document> clp835AssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "clp_835_assigned");
        BasicDBObject queryClp = BasicDBObject.parse("{st_hash:'" + contentHash + "',practice_id:null}");
        System.out.println("queryClp = " + queryClp);
        Document clpDoc = clp835AssignedColl.find(queryClp).first();
        System.out.println("clpDoc.toJson() = " + clpDoc.toJson());
        JsonPath svcJson = JsonPath.from(clpDoc.toJson());

        // New value for svcAmount
//        String practiceCode = practiceId;

        // Update the ins_paid field
        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("practice_id", practiceId);

        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);

        clp835AssignedColl.updateOne(queryClp, setQuery);
    }

    @When("inject ready 820 and 835 files")
    public void injectReadyAndFiles() throws IOException {

        System.out.println("testCaseName = " + testCaseName);
        System.out.println("collectionName = " + collectionName);

        String testCaseNameForQuery = testCaseName.replace("_", " ");
        String filterStr = "{testCaseName:'" + testCaseNameForQuery + "'}";

        String fileName820ToDelete = eraCycleUtils.getTestCase(testCaseNameForQuery, collectionName, testCasesDB).getString("fileName820");
        String fileName835ToDelete = eraCycleUtils.getTestCase(testCaseNameForQuery, collectionName, testCasesDB).getString("fileName835");

        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraTestColl = MongoDBUtils.connectMongodb(mongoClient, testCasesDB, collectionName);

        System.out.println("server820UploadPath = " + server820UploadPath);
        System.out.println(fileName820ToDelete + "\n" + fileName835ToDelete);
        sshUtils.deleteFileFromServer(serverIp, fileName820ToDelete, server820UploadPath);
//        sshUtils.deleteFileFromServer("10.200.15.53",fileName835ToDelete,"/synthetic-chi/era_outbound");

        sshUtils.deleteFileFromSftp(fileName835ToDelete, "/synthetic-chi/era_outbound");
//        sshUtils.sftpDownloadFile();

        String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" +
                File.separator + "resources" + File.separator + "testCases";
        File[] files = new File(filePath).listFiles();

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile() && fileName.startsWith(testCaseName + "_")) {
                    System.out.println("file name = " + fileName);
                    if (fileName.endsWith(".820")) {
                        List<String> fileNameList = Arrays.asList(fileName);
                        sshUtils.uploadFileToServerFromProjectPath(serverIp, fileNameList, server820UploadPath, filePath);
                        String fileContent820 = readFile(filePath, fileName);
                        System.out.println("fileContent820 = " + fileContent820);

                        BasicDBObject query = BasicDBObject.parse(filterStr);
                        System.out.println("query = " + query);
                        Document document = eraTestColl.find(query).first();

                        if (document != null) {
                            String update = "{$set:{fileName820:'" + fileName + "'}}";
                            MongoDBUtils.executeUpdateQuery(eraTestColl, filterStr, update);
                        }

                    } else if (fileName.endsWith(".835")) {
                        fileName835 = fileName;
                        sshUtils.uploadFileToSftp(filePath, fileName, "/synthetic-chi/era_outbound");

                        String fileContent835 = readFile(filePath, fileName);
                        System.out.println("fileContent835 = " + fileContent835);
                        int clpIndex = fileContent835.indexOf("~CLP*") + 5;
                        String clpStr = fileContent835.substring(clpIndex);
                        System.out.println("clpStr = " + clpStr);
                        clpStr = clpStr.substring(0, clpStr.indexOf("~"));
                        System.out.println("clpStr 2= " + clpStr);
                        String claimIdentifier835 = clpStr.substring(0, clpStr.indexOf("+"));
                        System.out.println("claimIdentifier835 = " + claimIdentifier835);

                        int trn02Index = fileContent835.indexOf("~TRN*1*") + 7;
                        String trn02Str = fileContent835.substring(trn02Index);
//                System.out.println("trn02Str = " + trn02Str);
                        trn02Str = trn02Str.substring(0, trn02Str.indexOf("~"));
//                        System.out.println("trn02Str 2= " + trn02Str);
                        checkNumber = trn02Str.substring(0, trn02Str.indexOf("*"));
                        System.out.println("checkNumber = " + checkNumber);
                        reference = checkNumber;

//                        String filter = "{claimIdentifier:'"+claimIdentifier835+"'}";
                        Document filter = Document.parse(filterStr);
                        BasicDBObject query = BasicDBObject.parse(filterStr);
                        System.out.println("query = " + query);
                        Document document = eraTestColl.find(query).first();

                        if (document != null) {
                            String update = "{$set:{fileName835:'" + fileName + "', file835Content:'" + fileContent835 + "', checkNumber:'" + checkNumber + "'}}";
                            MongoDBUtils.executeUpdateQuery(eraTestColl, filterStr, update);
                        }
                    }

                    System.out.println("---------------------------------------");

                }
            }
        }
    }

    public static String readFile(String filePath, String fileName) throws IOException {
        Path filePathFull = Paths.get(filePath + File.separator + fileName);
        byte[] fileBytes = Files.readAllBytes(filePathFull);
        String content = new String(fileBytes);
        return content;
    }

    @Then("verify writeback Claim table ClaimStatus is R")
    public void verifyWritebackClaimTableClaimStatusIsR() {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        System.out.println("query = " + query);
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
        JsonPath eraSplitFileJson = JsonPath.from(eraSplitFileDoc.toJson());
//        "writeback_records.data.ClaimNum":"748"
    }


    @When("change claim_proc_id to null to make the SVC unmatched")
    public void changeClaim_proc_idToNullToMakeTheSVCUnmatched() {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();

        // get the content hash
        MongoCollection<Document> eraSplitFileAssigned_1_Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + reference + "'}");
        System.out.println("check_number = " + query);
        Document eraSplitFileAssignedDoc = eraSplitFileAssigned_1_Coll.find(query).first();
        System.out.println("eraSplitFileAssignedDoc.toJson() = " + eraSplitFileAssignedDoc.toJson());
        JsonPath eraSplitFileAssignedJson = JsonPath.from(eraSplitFileAssignedDoc.toJson());
        String contentHash = eraSplitFileAssignedJson.get("content_hash");

        // update the SVC amount

        MongoCollection<Document> svc835AssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "svc_835_assigned");
        BasicDBObject querySvc = BasicDBObject.parse("{st_hash:'" + contentHash + "','matched_claim_proc_1.claim_proc_id':'3125'}");
        System.out.println("querySvc = " + querySvc);
        Document svcDoc = svc835AssignedColl.find(querySvc).first();
        System.out.println("svcDoc.toJson() = " + svcDoc.toJson());
        JsonPath svcJson = JsonPath.from(svcDoc.toJson());
        String claimProcId = svcJson.getString("matched_claim_proc_1.claim_proc_id");
        System.out.println("claimProcId = " + claimProcId);

        // Create an update object to set the fields to null and update svc_matched
        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("matched_claim_proc_1.ref_6r", null);
        updateFields.append("matched_claim_proc_1.claim_proc_id", null);
        updateFields.append("matched_claim_proc_1.matched_criteria_name", null);
        updateFields.append("svc_matched", "unmatched");

        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);

        svc835AssignedColl.updateOne(querySvc, setQuery);

        BasicDBObject querySvc2 = BasicDBObject.parse("{st_hash:'" + contentHash + "','matched_claim_proc_1.claim_proc_id':null}");
        System.out.println("querySvc = " + querySvc);
        Document svc2Doc = svc835AssignedColl.find(querySvc2).first();
        JsonPath svc2Json = JsonPath.from(svc2Doc.toJson());
        String claimProc2Id = svc2Json.getString("matched_claim_proc_1.claim_proc_id");
        System.out.println("claimProcId should be null = " + claimProc2Id);

    }

    @When("change svc835assigned record to make claim_proc {string} unmatched")
    public void changeSvc835assignedRecordToMakeClaim_procUnmatched(String claimProcId) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();

        // get the content hash
        MongoCollection<Document> eraSplitFileAssigned_1_Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + reference + "'}");
        System.out.println("check_number = " + query);
        Document eraSplitFileAssignedDoc = eraSplitFileAssigned_1_Coll.find(query).first();
//        System.out.println("eraSplitFileAssignedDoc.toJson() = " + eraSplitFileAssignedDoc.toJson());
//        JsonPath eraSplitFileAssignedJson = JsonPath.from(eraSplitFileAssignedDoc.toJson());
        String contentHash = (String) eraSplitFileAssignedDoc.get("content_hash");

        // retrieve the svc_835_assigned record

        MongoCollection<Document> svc835AssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "svc_835_assigned");
        BasicDBObject querySvc = BasicDBObject.parse("{st_hash:'" + contentHash + "','matched_claim_proc_1.claim_proc_id':'" + claimProcId + "'}");
        System.out.println("querySvc = " + querySvc);
        Document svcDoc = svc835AssignedColl.find(querySvc).first();
        System.out.println("svcDoc.toJson() = " + svcDoc.toJson());
        JsonPath svcJson = JsonPath.from(svcDoc.toJson());
        System.out.println("claimProcId = " + svcJson.getString("matched_claim_proc_1.claim_proc_id"));

        // Create an update object to set the fields to null and update svc_matched
        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("matched_claim_proc_1.ref_6r", null);
        updateFields.append("matched_claim_proc_1.claim_proc_id", null);
        updateFields.append("matched_claim_proc_1.matched_criteria_name", null);
        updateFields.append("svc_matched", "unmatched");

        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);

        svc835AssignedColl.updateOne(querySvc, setQuery);

        BasicDBObject querySvc2 = BasicDBObject.parse("{st_hash:'" + contentHash + "','matched_claim_proc_1.claim_proc_id':null}");
        System.out.println("querySvc = " + querySvc);
        Document svc2Doc = svc835AssignedColl.find(querySvc2).first();
        JsonPath svc2Json = JsonPath.from(svc2Doc.toJson());
        String claimProc2Id = svc2Json.getString("matched_claim_proc_1.claim_proc_id");
        System.out.println("claimProcId should be null = " + claimProc2Id);
    }

    @Then("verify ERA is not auto-approved by the provider")
    public void verifyERAIsNotAutoApprovedByTheProvider() {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
        Document autoPostDoc = (Document) eraSplitFileDoc.get("auto_post");
        boolean approvalProvider = autoPostDoc.get("approvalProvider") == null;
        Assert.assertTrue("auto_post.approvalProvider field is not null", approvalProvider);
    }

    @Then("verify ERA is not auto-approved by the retrace")
    public void verifyERAIsNotAutoApprovedByTheRetrace() {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
        Document autoPostDoc = (Document) eraSplitFileDoc.get("auto_post");
        boolean approvalProvider = autoPostDoc.get("approval") == null;
        Assert.assertTrue("auto_post.approval field is not null", approvalProvider);
    }

    @Then("verify write-off is zero for claim {string}")
    public void verifyWriteOffIsZeroForClaim(String claimIdentifier) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();

        // get the content hash
        MongoCollection<Document> eraSplitFileAssigned_1_Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + reference + "'}");
        System.out.println("query = " + query);
        Document eraSplitFileAssignedDoc = eraSplitFileAssigned_1_Coll.find(query).first();
//        System.out.println("eraSplitFileAssignedDoc.toJson() = " + eraSplitFileAssignedDoc.toJson());
//        JsonPath eraSplitFileAssignedJson = JsonPath.from(eraSplitFileAssignedDoc.toJson());
        String contentHash = (String) eraSplitFileAssignedDoc.get("content_hash");

        // get the write-off amount

        MongoCollection<Document> svc835AssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "svc_835_assigned");
        BasicDBObject querySvc = BasicDBObject.parse("{st_hash:'" + contentHash + "','matched_claim_1.claim_identifier':'" + claimIdentifier + "'}");
        System.out.println("querySvc = " + querySvc);
        Document svcDoc = svc835AssignedColl.find(querySvc).first();
        Document writeOffComputation = (Document) svcDoc.get("write_off_computation");
        if (writeOffComputation.containsKey("write_off")) {
            // Handle write_off as Decimal128 and convert to BigDecimal
            Decimal128 writeOffDecimal = writeOffComputation.get("write_off", Decimal128.class);
            BigDecimal actualWriteoffValue = writeOffDecimal.bigDecimalValue();
            System.out.println("actualWriteoffValue = " + actualWriteoffValue);

            BigDecimal expectedWriteoffValue = BigDecimal.ZERO;
            System.out.println("expectedWriteoffValue = " + expectedWriteoffValue);

            // Compare the actual write-off with expected value (0)
            Assert.assertEquals("write-off value is not 0", expectedWriteoffValue, actualWriteoffValue);
        }
    }

    @Then("verify writeoff is set according to cas for claim {string}")
    public void verifyWriteoffIsSetAccordingToCasForClaim(String claimIdentifier) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();

        try {
            // Get the content hash
            MongoCollection<Document> eraSplitFileAssigned_1_Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
            BasicDBObject query = BasicDBObject.parse("{check_number:'" + reference + "'}");
            System.out.println("query = " + query);

            Document eraSplitFileAssignedDoc = eraSplitFileAssigned_1_Coll.find(query).first();
            if (eraSplitFileAssignedDoc == null) {
                throw new RuntimeException("No document found in era_split_file_assigned for check_number: " + reference);
            }

//        System.out.println("eraSplitFileAssignedDoc.toJson() = " + eraSplitFileAssignedDoc.toJson());
//        JsonPath eraSplitFileAssignedJson = JsonPath.from(eraSplitFileAssignedDoc.toJson());
            String contentHash = (String) eraSplitFileAssignedDoc.get("content_hash");

            // Get the write-off amount
            MongoCollection<Document> svc835AssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "svc_835_assigned");
            BasicDBObject querySvc = BasicDBObject.parse("{st_hash:'" + contentHash + "','matched_claim_1.claim_identifier':'" + claimIdentifier + "'}");
            System.out.println("querySvc = " + querySvc);

            List<Document> svcDocs = svc835AssignedColl.find(querySvc).into(new ArrayList<>());
            System.out.println("svcDocs.size() = " + svcDocs.size());

            if (svcDocs.isEmpty()) {
                throw new RuntimeException("No matching documents found in svc_835_assigned for claim: " + claimIdentifier);
            }

            for (Document doc : svcDocs) {
                Document writeOffComputation = (Document) doc.get("write_off_computation");
                String casAmount = null;
                String writeoffAmount = null;

                // Extract and format write-off amount
                if (writeOffComputation.containsKey("write_off")) {
                    Decimal128 writeOffDecimal = writeOffComputation.get("write_off", Decimal128.class);
                    BigDecimal actualWriteoffValue = writeOffDecimal.bigDecimalValue();
                    writeoffAmount = formatBigDecimal(actualWriteoffValue);
                    System.out.println("writeoffAmount = " + writeoffAmount);
                }

                // Extract CAS amount
                if (doc.containsKey("cas")) {
                    List<Document> casArray = (List<Document>) doc.get("cas");

                    for (Document casEntry : casArray) {
                        String groupCode = casEntry.getString("claim_adjustment_group_code_1");
                        String reasonCode = casEntry.getString("claim_adjustment_reason_code_1");

                        if ("CO".equals(groupCode) && "45".equals(reasonCode)) {
                            Decimal128 claimAdjustmentAmount1 = casEntry.get("claim_adjustment_amount_1", Decimal128.class);
                            BigDecimal actualClaimAdjustmentAmount1 = claimAdjustmentAmount1.bigDecimalValue();
                            casAmount = formatBigDecimal(actualClaimAdjustmentAmount1);
                            System.out.println("CAS amount is: " + casAmount);
                            break;
                        }
                    }
                } else {
                    System.out.println("No 'cas' array found in the document");
                }

                // Compare the formatted values
                Assert.assertEquals("Write-off value is not equal to write-off CAS amount", writeoffAmount, casAmount);
            }
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Test failed due to an exception: " + e.getMessage());
        } finally {
            mongoClient.close();
        }
    }

    /**
     * Formats a BigDecimal value to a consistent string with two decimal places.
     */
    private static String formatBigDecimal(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    @When("set ERA settings with the following parameters")
    public void setERASettingsWithTheFollowingParameters(DataTable dataTable) {
        Map<String, String> parameters = dataTable.asMap(String.class, String.class);

        // Step 1: Connect to MongoDB client and collection
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> practice1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "practice_1");

        // Step 2: Define query to find the document with the name 'CLS2'
        BasicDBObject query = BasicDBObject.parse("{name:'CLS2'}");

        // Step 3: Find the document and print only the settings.era field before the update
        Document practice1Doc = practice1Coll.find(query).projection(Projections.include("settings.era")).first();
        if (practice1Doc != null) {
            System.out.println("settings.era before update: " + practice1Doc.get("settings", Document.class).get("era", Document.class).toJson());
        } else {
            System.out.println("Document with name 'CLS2' not found.");
            mongoClient.close();
            return; // Exit if the document doesn't exist
        }

        // Step 4: Define the ERA settings to update using values from the parameters map
        BasicDBObject newEraSettings = new BasicDBObject();
        newEraSettings.put("settings.era.partial_payment_era_writeback", Boolean.parseBoolean(parameters.get("partial_payment_era_writeback")));
        newEraSettings.put("settings.era.collect_fees_on_potential_auto_post", Boolean.parseBoolean(parameters.get("collect_fees_on_potential_auto_post")));
        newEraSettings.put("settings.era.enable_auto_post", Boolean.parseBoolean(parameters.get("enable_auto_post")));
        newEraSettings.put("settings.era.auto_post_0_pay_claims", Boolean.parseBoolean(parameters.get("auto_post_0_pay_claims")));
        newEraSettings.put("settings.era.preauth_auto_post", Boolean.parseBoolean(parameters.get("preauth_auto_post")));
        newEraSettings.put("settings.era.auto_post_secondary_claim", Boolean.parseBoolean(parameters.get("auto_post_secondary_claim")));
        newEraSettings.put("settings.era.auto_post_ortho_claims", Boolean.parseBoolean(parameters.get("auto_post_ortho_claims")));
        newEraSettings.put("settings.era.auto_post_plbs", Boolean.parseBoolean(parameters.get("auto_post_plbs")));
        newEraSettings.put("settings.era.primary_with_secondary_claim", parameters.get("primary_with_secondary_claim"));
        newEraSettings.put("settings.era.era_writeback_file_path", Boolean.parseBoolean(parameters.get("era_writeback_file_path")));
        newEraSettings.put("settings.era.multi_practice_split_era_auto_post", Boolean.parseBoolean(parameters.get("multi_practice_split_era_auto_post")));
        newEraSettings.put("settings.era.allow_partial_clps", Boolean.parseBoolean(parameters.get("allow_partial_clps")));
        newEraSettings.put("settings.era.set_claim_to_received_for_partial_clps", Boolean.parseBoolean(parameters.get("set_claim_to_received_for_partial_clps")));
        newEraSettings.put("settings.era.internal_auto_approve", Boolean.parseBoolean(parameters.get("internal_auto_approve")));
        newEraSettings.put("settings.era.approve_auto_post_automatically", Boolean.parseBoolean(parameters.get("approve_auto_post_automatically")));
        newEraSettings.put("settings.era.approve_auto_post_0_pay_claims_automatically", Boolean.parseBoolean(parameters.get("approve_auto_post_0_pay_claims_automatically")));
        newEraSettings.put("settings.era.approve_preauth_auto_post_automatically", Boolean.parseBoolean(parameters.get("approve_preauth_auto_post_automatically")));
        newEraSettings.put("settings.era.approve_partial_payment_era_writeback_automatically", Boolean.parseBoolean(parameters.get("approve_partial_payment_era_writeback_automatically")));
        newEraSettings.put("settings.era.approve_auto_post_secondary_claim_automatically", Boolean.parseBoolean(parameters.get("approve_auto_post_secondary_claim_automatically")));
        newEraSettings.put("settings.era.approve_auto_post_ortho_claims_automatically", Boolean.parseBoolean(parameters.get("approve_auto_post_ortho_claims_automatically")));

        // Step 5: Perform the update
        BasicDBObject update = new BasicDBObject("$set", newEraSettings);
        practice1Coll.updateOne(query, update);

        // Step 6: Retrieve and print only the settings.era field after the update
        Document updatedPractice1Doc = practice1Coll.find(query).projection(Projections.include("settings.era")).first();
        System.out.println("settings.era after update: " + updatedPractice1Doc.get("settings", Document.class).get("era", Document.class).toJson());

        // Step 7: Close MongoDB client connection
        mongoClient.close();
    }

    @When("set virtual account nickname {string}, batch_ach {string}, minimum_waiting_period {int}, maximum_waiting_period {int}, disqualified_era_deposit_waiting_days {int}")
    public void setVirtualAccountNicknameBatch_achMinimum_waiting_periodMaximum_waiting_periodDisqualified_era_deposit_waiting_days(String nickName, String batch_ach, int minWaiting, int maxWaiting, int disEraWaiting) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> dfiAccount1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "dfi_account_1");
        BasicDBObject query = BasicDBObject.parse("{account_name:'virtual', nick_name:'" + nickName + "'}");
        System.out.println("query = " + query);
        Document dfiAccount1Doc = dfiAccount1Coll.find(query).first();
        System.out.println("dfiAccount1Doc.toJson() = " + dfiAccount1Doc.toJson());

        boolean batchAchValue = Boolean.parseBoolean(batch_ach);

        Document update = new Document("$set", new Document("settings.batch_ach", batchAchValue)
                .append("settings.minimum_waiting_period", minWaiting)
                .append("settings.maximum_waiting_period", maxWaiting)
                .append("settings.disqualified_era_deposit_waiting_days", disEraWaiting));

        dfiAccount1Coll.updateMany(query, update);

        JsonPath dfiAccount1Json = JsonPath.from(dfiAccount1Doc.toJson());

        System.out.println("dfiAccount1Json.getString(batch_ach) = " + dfiAccount1Json.getString(batch_ach));
        System.out.println("dfiAccount1Json.getString(\"minimum_waiting_period\") = " + dfiAccount1Json.getString("minimum_waiting_period"));
        System.out.println("dfiAccount1Json.getString(\"maximum_waiting_period\") = " + dfiAccount1Json.getString("maximum_waiting_period"));
        System.out.println("dfiAccount1Json.getString(\"disqualified_era_deposit_waiting_days\") = " + dfiAccount1Json.getString("disqualified_era_deposit_waiting_days"));
    }

    @Then("verify writeback method is null")
    public void verifyWritebackMethodIsNull() {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        System.out.println("query = " + query);
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
        boolean isWritebackMethodNullOrMissing = !eraSplitFileDoc.containsKey("write_back_method") || eraSplitFileDoc.get("write_back_method") == null;
        System.out.println("isWritebackMethodNullOrMissing = " + isWritebackMethodNullOrMissing);
        Assert.assertTrue("The 'write_back_method' field should be null or missing.", isWritebackMethodNullOrMissing);
    }

    @When("delete record from sql db with claim as {string}")
    public void deleteRecordFromSqlDbWithClaimAs(String givenClaimNum) {
        sqlUtils.createConnection(mysqlIp, mysqlDbName);
        List<Map<String, Object>> mapQueryResult = sqlUtils.getQueryResultMap("SELECT ClaimNum, ClaimFee FROM claim WHERE ClaimNum = " + givenClaimNum + ";");
        Object claimNum = mapQueryResult.get(0).get("ClaimNum");
        System.out.println("claimNum before delete = " + claimNum.toString());
        sqlUtils.queryExecuter("DELETE FROM claim WHERE ClaimNum = " + givenClaimNum + ";");
    }

    @Then("verify claim writeback error")
    public void verifyClaimWritebackError() {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();

        // get the writebackTransactionId
        MongoCollection<Document> eraSplitFileAssigned_1_Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + reference + "'}");
        System.out.println("check_number = " + query);
        Document eraSplitFileAssignedDoc = eraSplitFileAssigned_1_Coll.find(query).first();
//        System.out.println("eraSplitFileAssignedDoc.toJson() = " + eraSplitFileAssignedDoc.toJson());
//        JsonPath eraSplitFileAssignedJson = JsonPath.from(eraSplitFileAssignedDoc.toJson());
        String writebackTransactionId = (String) eraSplitFileAssignedDoc.get("writeback_transaction_id");

        // get the error message from opendentalWritebackArchive
        MongoCollection<Document> opendentalWritebackArchive = MongoDBUtils.connectMongodb(mongoClient, partner, "opendental_writeback_archive_1");
        BasicDBObject queryWritebackArchive = BasicDBObject.parse("{transaction_id:'" + writebackTransactionId + "', table_name:'claim'}");
        System.out.println("queryWritebackArchive = " + queryWritebackArchive);
        Document opendentalWritebackArchiveDoc = opendentalWritebackArchive.find(queryWritebackArchive).first();
//        System.out.println("opendentalWritebackArchiveDoc.toJson() = " + opendentalWritebackArchiveDoc.toJson());
        JsonPath opendentalWritebackArchiveJson = JsonPath.from(opendentalWritebackArchiveDoc.toJson());
        String actualClaimTableError = opendentalWritebackArchiveJson.get("error");
        String expectedClaimTableError = "claim could not update. claim does not exist on mysql";

        logUtils.assertEquals("categories are not matched", actualClaimTableError, expectedClaimTableError);
    }

    @Then("verify claim writeback error as {string}")
    public void verifyClaimWritebackErrorAs(String expectedErrorMessage) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();

        // get the writebackTransactionId
        MongoCollection<Document> eraSplitFileAssigned_1_Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + reference + "'}");
        System.out.println("check_number = " + query);
        Document eraSplitFileAssignedDoc = eraSplitFileAssigned_1_Coll.find(query).first();
//        System.out.println("eraSplitFileAssignedDoc.toJson() = " + eraSplitFileAssignedDoc.toJson());
//        JsonPath eraSplitFileAssignedJson = JsonPath.from(eraSplitFileAssignedDoc.toJson());
        String writebackTransactionId = (String) eraSplitFileAssignedDoc.get("writeback_transaction_id");


        // get the error message from opendentalWritebackArchive
        MongoCollection<Document> opendentalWritebackArchive = MongoDBUtils.connectMongodb(mongoClient, partner, "opendental_writeback_archive_1");
        BasicDBObject queryWritebackArchive = BasicDBObject.parse("{transaction_id:'" + writebackTransactionId + "', table_name:'claim'}");
        System.out.println("queryWritebackArchive = " + queryWritebackArchive);
        Document opendentalWritebackArchiveDoc = opendentalWritebackArchive.find(queryWritebackArchive).first();
//        System.out.println("opendentalWritebackArchiveDoc.toJson() = " + opendentalWritebackArchiveDoc.toJson());
        JsonPath opendentalWritebackArchiveJson = JsonPath.from(opendentalWritebackArchiveDoc.toJson());
        String actualClaimTableError = opendentalWritebackArchiveJson.get("error");
        System.out.println("actualClaimTableError = " + actualClaimTableError);
        String actualClaimTableError1 = actualClaimTableError.substring(0, 54);
        System.out.println("actualClaimTableError only message = " + actualClaimTableError1);
        String expectedClaimTableError = "claim could not update. claim does not exist on mysql.";
        boolean expectedResult = actualClaimTableError.contains(expectedClaimTableError);
        Assert.assertTrue("Claim Table error message does not match", expectedResult);
        Assert.assertEquals("Claim Table error message does not match", actualClaimTableError1, expectedErrorMessage);
    }

    @When("delete record from mysql db with claimprocId as {string}")
    public void deleteRecordFromMysqlDbWithClaimprocIdAs(String claimProcNum) {
        sqlUtils.createConnection(mysqlIp, mysqlDbName);
        List<Map<String, Object>> mapQueryResult = sqlUtils.getQueryResultMap("SELECT * FROM claimproc WHERE ClaimProcNum = " + claimProcNum + ";");
        Object claimprocNum = mapQueryResult.get(0).get("ClaimProcNum");
        System.out.println("ClaimProcNum before delete = " + claimprocNum.toString());
        sqlUtils.queryExecuter("DELETE FROM claimproc WHERE ClaimProcNum = " + claimProcNum + ";");
    }

    @Then("verify claimproc writeback error as {string}")
    public void verifyClaimprocWritebackErrorAs(String expectedClaimprocErrorMessage) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();

        // get the writebackTransactionId
        MongoCollection<Document> eraSplitFileAssigned_1_Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + reference + "'}");
        System.out.println("check_number = " + query);
        Document eraSplitFileAssignedDoc = eraSplitFileAssigned_1_Coll.find(query).first();
//        System.out.println("eraSplitFileAssignedDoc.toJson() = " + eraSplitFileAssignedDoc.toJson());
//        JsonPath eraSplitFileAssignedJson = JsonPath.from(eraSplitFileAssignedDoc.toJson());
        String writebackTransactionId = (String) eraSplitFileAssignedDoc.get("writeback_transaction_id");


        // get the error message from opendentalWritebackArchive
        MongoCollection<Document> opendentalWritebackArchive = MongoDBUtils.connectMongodb(mongoClient, partner, "opendental_writeback_archive_1");
        BasicDBObject queryWritebackArchive = BasicDBObject.parse("{transaction_id:'" + writebackTransactionId + "', table_name:'claimproc'}");
        System.out.println("queryWritebackArchive = " + queryWritebackArchive);
        Document opendentalWritebackArchiveDoc = opendentalWritebackArchive.find(queryWritebackArchive).first();
//        System.out.println("opendentalWritebackArchiveDoc.toJson() = " + opendentalWritebackArchiveDoc.toJson());
        JsonPath opendentalWritebackArchiveJson = JsonPath.from(opendentalWritebackArchiveDoc.toJson());
        String actualClaimprocTableError = opendentalWritebackArchiveJson.get("error");
        String actualClaimprocTableError1 = actualClaimprocTableError.substring(0, 62);
        System.out.println("actualClaimprocTableError = " + actualClaimprocTableError);
        System.out.println("actualClaimprocTableError only message = " + actualClaimprocTableError1);
        String expectedClaimprocTableError = "claimproc could not update. claimproc does not exist on mysql.";
        boolean expectedResult = actualClaimprocTableError.contains(expectedClaimprocErrorMessage);
        Assert.assertTrue("Claimproc Table error message does not match", expectedResult);
        Assert.assertEquals("Claimproc Table error message does not match", actualClaimprocTableError1, expectedClaimprocErrorMessage);
    }

    @When("Set Min Waiting Period {int}, Max Waiting Period {int}, Disqualified ERA waiting period {string}")
    public void setMinWaitingPeriodMaxWaitingPeriodDisqualifiedERAWaitingPeriod(int minWaiting, int maxWaiting, String disqEraWaiting) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> dfiAccountColl = MongoDBUtils.connectMongodb(mongoClient, partner, "dfi_account_1");
        Document filter = Document.parse("{dfi_account_id:'"+dfiAccountId+"'}");
        System.out.println("query = " + filter);
        Document dfiAccountDoc = dfiAccountColl.find(filter).first();
        System.out.println("dfiAccountDoc.toJson() = " + dfiAccountDoc.toJson());
        JsonPath dfiAccountJson = JsonPath.from(dfiAccountDoc.toJson());

        // Update dfi_account_1 collection
        Document update = new Document("$set", new Document("settings.minimum_waiting_period", minWaiting)
                .append("settings.maximum_waiting_period", maxWaiting)
                .append("settings.disqualified_era_deposit_waiting_days", disqEraWaiting));
        MongoDBUtils.executeUpdateQuery(dfiAccountColl, filter, update);

        Document dfiAccountDoc2 = dfiAccountColl.find(filter).first();
        System.out.println("Updated dfi_account settings = " + dfiAccountDoc2.toJson());
//        JsonPath dfiAccountJson2 = JsonPath.from(dfiAccountDoc2.toJson());
    }

    @Then("verify ledgers displayed on sweep page")
    public void verifyLedgersDisplayedOnSweepPage() {

        JsonPath responseJson = apiUtils.getListOfSweep("qa-cls5");

// Get the list of JSON elements
        List<Map<String, Object>> items = responseJson.getList("$");

        for (Map<String, Object> item : items) {
            // Check if "nick_name" is "cls2_virt"
            String nickName = (String) item.get("nick_name");
            if ("cls2_virt".equals(nickName)) {
                // If found, get the list of "inspays"
                List<Map<String, Object>> inspaysList = (List<Map<String, Object>>) item.get("inspays");

                if (inspaysList != null && !inspaysList.isEmpty()) {
                    for (Map<String, Object> inspay : inspaysList) {
                        // Check if inspay contains "ledgerType" and retrieve it
                        if (inspay.containsKey("ledgerType")) {
                            String ledgerType = (String) inspay.get("ledgerType");
                            System.out.println("Ledger Type: " + ledgerType);
                        } else {
                            System.out.println("ledgerType not found in inspay item.");
                        }
                    }
                } else {
                    System.out.println("No inspays available for nick_name: " + nickName);
                }
                break; // Exit outer loop after finding the first match for "nick_name"
            }
        }
    }

    @Then("verify {string} pre-ledger is displayed on sweep page")
    public void verifyPreLedgerIsDisplayedOnSweepPage(String expectedPreLedger) {
        JsonPath responseJson = apiUtils.getListOfSweep(partner);

// Get the list of JSON elements
        List<Map<String, Object>> items = responseJson.getList("$");

        for (Map<String, Object> item : items) {
            // Check if "nick_name" is "cls2_virt"
            String dfiAccountIdCollected = (String) item.get("dfi_account_id");
            if (dfiAccountId.equals(dfiAccountIdCollected)) {
                // If found, get the list of "inspays"
                List<Map<String, Object>> inspaysList = (List<Map<String, Object>>) item.get("inspays");
                List<String> ledgerList = new ArrayList<>(); // Initialize ledgerList

                if (inspaysList != null && !inspaysList.isEmpty()) {
                    for (Map<String, Object> inspay : inspaysList) {
                        // Check if inspay contains "reference" and retrieve it
                        String reference = (String) inspay.get("reference");

                        // Compare reference to the desired value
                        if (checkNumber.equals(reference)) {
                            String ledgerType = (String) inspay.get("ledgerType");
                            ledgerList.add(ledgerType); // Add ledgerType to the list
                            System.out.println("Ledger Type: " + ledgerType);
                        } else {
                            System.out.println("ledgerType not found in inspay item.");
                        }
                    }

                    // Check if expectedPreLedger is in ledgerList and assert outside the loop
                    if (ledgerList.contains(expectedPreLedger)) {
                        Assert.assertTrue("Pre-ledger is listed on sweep page", true);
                    } else {
                        Assert.fail("Pre-ledger is not listed on sweep page");
                    }
                } else {
                    System.out.println("Pre-ledger is not listed on sweep page for dfiAccountId: " + dfiAccountIdCollected);
                    Assert.fail("Pre-ledger is not listed on sweep page");
                }
                break; // Exit outer loop after finding the first match for "nick_name"
            }
        }
    }

    @Then("verify {string} pre-ledger is NOT displayed on sweep page")
    public void verifyPreLedgerIsNOTDisplayedOnSweepPage(String notExpectedPreLedger) {
        JsonPath responseJson = apiUtils.getListOfSweep(partner);
//        responseJson.prettyPrint();

// Get the list of JSON elements
        List<Map<String, Object>> items = responseJson.getList("$");

        for (Map<String, Object> item : items) {
            // Check if "nick_name" is "cls2_virt"
            String dfi_account_id = (String) item.get("dfi_account_id");
            if (dfiAccountId.equals(dfi_account_id)) {
                // If found, get the list of "inspays"
                List<Map<String, Object>> inspaysList = (List<Map<String, Object>>) item.get("inspays");
                List<String> ledgerList = new ArrayList<>(); // Initialize ledgerList

                if (inspaysList != null && !inspaysList.isEmpty()) {
                    for (Map<String, Object> inspay : inspaysList) {
                        // Check if inspay contains "reference" and retrieve it
                        String reference = (String) inspay.get("reference");

                        // Compare reference to the desired value
                        if (checkNumber.equals(reference)) {
                            String ledgerType = (String) inspay.get("ledgerType");
                            ledgerList.add(ledgerType); // Add ledgerType to the list
                            System.out.println("Ledger Type: " + ledgerType);
                        } else {
                            System.out.println("ledgerType not found in inspay item.");
                        }
                    }

                    // Check if expectedPreLedger is NOT in ledgerList and assert outside the loop
                    if (!ledgerList.contains(notExpectedPreLedger)) {
                        Assert.assertTrue("Pre-ledger is not listed on sweep page, as expected.", true);
                    } else {
                        Assert.fail("Unexpectedly found Pre-ledger on sweep page");
                    }
                } else {
                    System.out.println("No inspays available for dfi_account_id: " + dfi_account_id);
                }
                break; // Exit outer loop after finding the first match for "nick_name"
            }
        }

    }

    @When("move inspay ledger date {int} days prior according to maximum_waiting_period")
    public void moveInspayLedgerDateDaysPriorAccordingToMaximum_waiting_period(int daysToGoBack) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> ledger_1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");

        BasicDBObject query = BasicDBObject.parse("{reference:'" + reference + "', category:'inspay'}");
        Document ledgerDoc = ledger_1Coll.find(query).first();

        // Get the current date from the document
        Date ledgerDate = ledgerDoc.getDate("date");
        SimpleDateFormat utcFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        utcFormat1.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedLedgerDate = utcFormat1.format(ledgerDate);
        System.out.println("Current ledgerDate = " + formattedLedgerDate);

        // Calculate 15 business days backward
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ledgerDate);

        int businessDaysSubtracted = 0;
        while (businessDaysSubtracted < daysToGoBack) {
            // Move to the previous day
            calendar.add(Calendar.DAY_OF_MONTH, -1);

            // Check if it's a weekday (Monday=1 to Friday=5)
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                businessDaysSubtracted++;
            }
        }

        // Format the new date as a UTC string
        Date newDate = calendar.getTime();
        SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedNewDate = utcFormat.format(newDate);
        System.out.println("New Date after subtracting "+daysToGoBack+ "business days: " + formattedNewDate);

        // Update the collection with the new date
        Document filter = new Document("reference", reference).append("category", "inspay");
        Document update = new Document("$set", new Document("date", newDate));
        MongoDBUtils.executeUpdateQuery(ledger_1Coll, filter, update);
    }

    @When("Set Min Waiting Period {string}, Max Waiting Period {string}, Disqualified ERA waiting period {string}")
    public void setMinWaitingPeriodMaxWaitingPeriodDisqualifiedERAWaitingPeriod(String minWaiting, String maxWaiting, String disqEraWaiting) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> dfiAccountColl = MongoDBUtils.connectMongodb(mongoClient, partner, "dfi_account_1");
        BasicDBObject query = BasicDBObject.parse("{dfi_account_id:'"+dfiAccountId+"'}");
        System.out.println("query = " + query);
        Document dfiAccountDoc = dfiAccountColl.find(query).first();
        System.out.println("dfiAccountDoc.toJson() = " + dfiAccountDoc.toJson());
        JsonPath dfiAccountJson = JsonPath.from(dfiAccountDoc.toJson());

        Integer minWaitingTime = "null".equals(minWaiting) ? null : Integer.parseInt(minWaiting);
        Integer maxWaitingTime = "null".equals(maxWaiting) ? null : Integer.parseInt(maxWaiting);
        Integer disqEraWaitingTime = "null".equals(disqEraWaiting) ? null : Integer.parseInt(disqEraWaiting);

        // Update dfi_account_1 collection
        Document filter = new Document("nick_name", "cls2_virt");
        Document update = new Document("$set", new Document("settings.minimum_waiting_period", minWaitingTime)
                .append("settings.maximum_waiting_period", maxWaitingTime)
                .append("settings.disqualified_era_deposit_waiting_days", disqEraWaitingTime));
        MongoDBUtils.executeUpdateQuery(dfiAccountColl, filter, update);

        Document dfiAccountDoc2 = dfiAccountColl.find(query).first();
        System.out.println("Updated dfi_account settings = " + dfiAccountDoc2.toJson());
        JsonPath dfiAccountJson2 = JsonPath.from(dfiAccountDoc2.toJson());
    }

    @Then("verify auto_post.qualified {string}")
    public void verifyAuto_postQualified(String expectedAutopostStatus) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        String filter = "{check_number:'" + checkNumber + "'}";
        BasicDBObject query = BasicDBObject.parse(filter);
        System.out.println("query = " + query);
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();


        Gson gson = new Gson();
        Map<String, Object> eraSplitFileDocMap = (Map<String, Object>) gson.fromJson(eraSplitFileDoc.toJson(), Map.class).get("auto_post");
        boolean isAutoPostQualified = (boolean) eraSplitFileDocMap.get("qualified");
        String actualAutoPostQualifiedStatus = String.valueOf(isAutoPostQualified);
        System.out.println("actualAutoPostQualifiedStatus = " + actualAutoPostQualifiedStatus);
        Assert.assertEquals("Auto post status dose not match", expectedAutopostStatus, actualAutoPostQualifiedStatus);
    }

    @When("change account number {string} and routing_number {string} for {string}")
    public void changeAccountNumberAndRouting_numberFor(String accountNumber, String routingNumber, String nickName) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> dfiAccountColl = MongoDBUtils.connectMongodb(mongoClient, partner, "dfi_account_1");
        BasicDBObject query = BasicDBObject.parse("{nick_name:'" + nickName + "'}");
        System.out.println("query = " + query);
        Document dfiAccountDoc = dfiAccountColl.find(query).first();
        String dbNickName = dfiAccountDoc.get("nick_name").toString();
        System.out.println("dbNickName = " + dbNickName);
        //        JsonPath dfiAccountJson = JsonPath.from(dfiAccountDoc.toJson());


        // Update dfi_account_1 collection
        Document filter = new Document("nick_name", nickName);
        Document update = new Document("$set", new Document("account_number", accountNumber)
                .append("routing_number", routingNumber));
        MongoDBUtils.executeUpdateQuery(dfiAccountColl, filter, update);

        Document dfiAccountDoc2 = dfiAccountColl.find(query).first();
        String modifiedAccountNumber = dfiAccountDoc2.get("account_number").toString();
        System.out.println("modifiedAccountNumber = " + modifiedAccountNumber);
        String modifiedRoutingNumber = dfiAccountDoc2.get("routing_number").toString();
        System.out.println("modifiedRoutingNumber = " + modifiedRoutingNumber);
    }

    JsonPath listAchOut = null;

    @When("get ACH_Out List for partner {string} and dfi_account_id {string}")
    public void getACH_OutListForPartnerAndDfi_account_id(String partner, String dfiAccountId) {
//        BrowserUtils.waitFor(120);
        // Fetch the JSON response as a JsonPath object
        listAchOut = apiUtils.GetFullListOfAch(partner, dfiAccountId);
    }

    @Then("verify {string} has error")
    public void verifyHasError(String ExpectedAckError) {
        String jsonResponse = listAchOut.prettify();
        // Convert the responseJson to a JSON String to parse as a JSONArray
        JSONArray rootArray = new JSONArray(jsonResponse);  // Assuming getListAchOut provides JSON as String

        // Access the nested array at index 1
        JSONArray dataArray = rootArray.getJSONArray(1);  // Adjust as needed

        JSONObject matchedItem = null;
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject item = dataArray.getJSONObject(i);
            JSONArray endToEndIds = item.optJSONArray("end_to_end_id");

            // Check if end_to_end_id matches the specified value
            if (endToEndIds != null && endToEndIds.length() > 0 && endToEndId.equals(endToEndIds.getString(0))) {
                matchedItem = item;
                break;
            }
        }

        if (matchedItem != null) {
            // Check for errors in ack1 and ack2
            boolean ack1ErrorsExist = matchedItem.has("ack1") &&
                    matchedItem.getJSONObject("ack1").has("ack_errors");
            boolean ack2ErrorsExist = matchedItem.has("ack2") &&
                    matchedItem.getJSONObject("ack2").has("ack_errors");
            boolean ackErrorsExist = matchedItem.has(ExpectedAckError) &&
                    matchedItem.getJSONObject(ExpectedAckError).has("ack_errors");

            // Print the result
            System.out.println("Matching Item found: " + matchedItem);
            System.out.println("ACK1 Errors Exist: " + ack1ErrorsExist);
            System.out.println("ACK2 Errors Exist: " + ack2ErrorsExist);
            Assert.assertTrue("ACK error does not match", ackErrorsExist);
        } else {
            System.out.println("No matching item with the expected end_to_end_id found.");
        }
    }

    String endToEndId = "";

    @When("get end_to_end_id from ledger_1 for {string}")
    public void getEnd_to_end_idFromLedger_1For(String category) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> ledger_1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");
        BasicDBObject query = BasicDBObject.parse("{reference:'" + reference + "', category:'" + category + "'}");
        System.out.println("query = " + query);
        Document ledgerDoc = ledger_1Coll.find(query).first();
        System.out.println("ledgerDoc.toJson() = " + ledgerDoc.toJson());
        JsonPath ledgerJson = JsonPath.from(ledgerDoc.toJson());
        List<String> endToEndIdList = ledgerJson.getList("end_to_end_id");
        endToEndId = (endToEndIdList != null && endToEndIdList.size() > 0) ? endToEndIdList.get(0) : null;
        System.out.println("endToEndId = " + endToEndId);
    }

    @When("change account number {string} and routing_number {string} on partner {string}")
    public void changeAccountNumberAndRouting_numberOnPartner(String accountNumber, String routingNumber, String partnerId) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> partnerColl = MongoDBUtils.connectMongodb(mongoClient, "directory", "partner_1");
        BasicDBObject query = BasicDBObject.parse("{partner_id:'" + partnerId + "'}");
        System.out.println("query = " + query);
        Document partnerDoc = partnerColl.find(query).first();
        String dbPartnerId = partnerDoc.get("partner_id").toString();
        System.out.println("dbPartnerId = " + dbPartnerId);

        // New virtual account data to add
        Document newVirtualAccount = new Document("routing_number", routingNumber)
                .append("account_number", accountNumber);

        // Update the document by pushing the new element into the virtual_account array
        partnerColl.updateOne(
                eq("partner_id", partnerId),
                Updates.push("virtual_account", newVirtualAccount)
        );

        // Verify the update
        Document updatedPartnerDoc = partnerColl.find(query).first();
        if (updatedPartnerDoc != null) {
            System.out.println("Updated virtual_account array: " + updatedPartnerDoc.get("virtual_account"));
        } else {
            System.out.println("Partner document not found for partner_id: " + partnerId);
        }


    }

//    @When("delete last virtual account from partner {string}")
//    public void deleteLastVirtualAccountFromPartner(String partnerId) {
//        MongoClient mongoClient = MongoDBUtils.getMongoClient();
//        MongoCollection<Document> partnerColl = MongoDBUtils.connectMongodb(mongoClient, "directory", "partner_1");
//        BasicDBObject query = BasicDBObject.parse("{partner_id:'" + partnerId + "'}");
//        System.out.println("query = " + query);
//
//        // Fetch the partner document to ensure it exists
//        Document partnerDoc = partnerColl.find(query).first();
//        if (partnerDoc == null) {
//            System.out.println("Partner document not found for partner_id: " + partnerId);
//            return;
//        }
//
//        // Remove the last element from the virtual_account array
//        partnerColl.updateOne(
//                Filters.eq("partner_id", partnerId),
//                Updates.popLast("virtual_account")
//        );
//
//        // Verify the update
//        Document updatedPartnerDoc = partnerColl.find(query).first();
//        if (updatedPartnerDoc != null) {
//            System.out.println("Updated virtual_account array after deletion: " + updatedPartnerDoc.get("virtual_account"));
//        } else {
//            System.out.println("Partner document not found for partner_id: " + partnerId);
//        }
//    }

    @When("delete all virtual accounts except the first from partner {string}")
    public void deleteAllVirtualAccountsExceptTheFirstFromPartner(String partnerId) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> partnerColl = MongoDBUtils.connectMongodb(mongoClient, "directory", "partner_1");
        BasicDBObject query = BasicDBObject.parse("{partner_id:'" + partnerId + "'}");
        System.out.println("query = " + query);

        // Fetch the partner document
        Document partnerDoc = partnerColl.find(query).first();
        if (partnerDoc == null) {
            System.out.println("Partner document not found for partner_id: " + partnerId);
            return;
        }

        // Retrieve the virtual_account array
        List<Object> virtualAccounts = (List<Object>) partnerDoc.get("virtual_account");
        if (virtualAccounts == null || virtualAccounts.isEmpty()) {
            System.out.println("No virtual_account array found or it is empty.");
            return;
        }

        // Keep only the first element
        List<Object> updatedVirtualAccounts = virtualAccounts.subList(0, 1);

        // Update the virtual_account array in MongoDB
        partnerColl.updateOne(
                eq("partner_id", partnerId),
                set("virtual_account", updatedVirtualAccounts)
        );

        // Verify the update
        Document updatedPartnerDoc = partnerColl.find(query).first();
        if (updatedPartnerDoc != null) {
            System.out.println("Updated virtual_account array after deletion: " + updatedPartnerDoc.get("virtual_account"));
        } else {
            System.out.println("Partner document not found for partner_id: " + partnerId);
        }
    }


    @Then("verify writeback category is {string}")
    public void verifyWritebackCategoryIs(String expectedCategory) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        System.out.println("query_era_split_file_assigned = " + query);
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
        String eraSplitFileAssignedId = eraSplitFileDoc.getString("era_split_file_assigned_id");
        String writeBackMethod = eraSplitFileDoc.getString("write_back_method");
        String transactionId = eraSplitFileDoc.getString("writeback_transaction_id");
        System.out.println("writeBackMethod = " + writeBackMethod);
        System.out.println("eraSplitFileAssignedId = " + eraSplitFileAssignedId);

        MongoCollection<Document> opendentalWritebackArchive1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "opendental_writeback_archive_1");

        if (writeBackMethod.equals("auto_post")) {
            BasicDBObject queryAutoPost = BasicDBObject.parse("{'transaction_id':'" + transactionId + "'}");

            List<String> categoryList = new ArrayList<>();
            for (Document doc : opendentalWritebackArchive1Coll.find(queryAutoPost)) {
                // Extract the value of the 'category' field and add it to the list
                String category = doc.getString("category");
                if (category != null) {
                    categoryList.add(category);
                }
            }

            for (String actualArchiveCategory : categoryList) {
                System.out.println("auto_post actualCategoryName = " + actualArchiveCategory);
                Assert.assertEquals("Category name is not 'era_auto_post'", actualArchiveCategory, expectedCategory);
            }
        } else if (writeBackMethod.equals("file")) {
            BasicDBObject query2 = BasicDBObject.parse("{'data.era_file_id':'" + eraSplitFileAssignedId + "'}");
            Document fileWritebackArchive1Doc = opendentalWritebackArchive1Coll.find(query2).first();
            String actualCategoryName = fileWritebackArchive1Doc.get("category").toString();
            System.out.println("file transferred actualCategoryName = " + actualCategoryName);
            Assert.assertEquals("Category name is not 'era_x12_to_folder'", actualCategoryName, expectedCategory);
        }
    }

    @When("transfer ERA manually")
    public void transferERAManually() {
        Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned("qa-cls5", checkNumber);
        String eraSplitFileAssignedId = eraSplitFileDoc.getString("era_split_file_assigned_id");
        JsonPath jsonPath = apiUtils.manuallyTransferEra(eraSplitFileAssignedId, practiceId, practiceName, partner);
        System.out.println("jsonPath = " + jsonPath);
    }

    @When("Insert and enable negative_patient_responsibility for partner {string} and practice {string}")
    public void insertAndEnableNegative_patient_responsibilityForPartnerAndPractice(String partner, String practice) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> practice1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "practice_1");
        BasicDBObject query = BasicDBObject.parse("{name:'" + practice + "'}");
        Document practice1Doc = practice1Coll.find(query).projection(Projections.include("settings.era")).first();

        if (practice1Doc != null) {
            System.out.println("settings.era before update: " + practice1Doc.get("settings", Document.class).get("era", Document.class).toJson());
        } else {
            System.out.println("Document with name 'CLS2' not found.");
            mongoClient.close();
            return;
        }

        // Define the update to add the new field
        BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("settings.era.negative_patient_responsibility", "adjustment"));
        practice1Coll.updateOne(query, update);

        Document updatedPractice1Doc = practice1Coll.find(query).projection(Projections.include("settings.era")).first();
        if (updatedPractice1Doc != null) {
            System.out.println("settings.era after update: " + updatedPractice1Doc.get("settings", Document.class).get("era", Document.class).toJson());
        }
        mongoClient.close();

        JsonPath jsonPath = apiUtils.enableNegativePatientResponsibility(partner, practiceId);
    }

    @Then("verify svc835user_edit_log writeoff values previous writeoff {string} and new writeoff {string}")
    public void verifySvc835User_edit_logWriteoffValuesPreviousWriteoffAndNewWriteoff(String previousWriteoff, String newWriteoff) {
        Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned("qa-cls5", checkNumber);
        String contentHash = eraSplitFileDoc.getString("content_hash");
        Document svc835AssignedDoc = eraCycleUtils.getSvc835Assigned(partner, contentHash, claimIdentifier);
        String svcHash = svc835AssignedDoc.getString("svc_hash");

        Document svc835UserEditLogDoc = eraCycleUtils.svc835UserEditLog(partner, svcHash);

        double previousWriteoffValue = 0; // Default for previous write_off
        double newWriteoffValue = 0;      // Default for new write_off

        // Retrieve previous_attributes write_off
        Document previousAttributes = svc835UserEditLogDoc.get("previous_attributes", Document.class);
        if (previousAttributes != null) {
            Document svc835Assigned = previousAttributes.get("svc_835_assigned", Document.class);
            if (svc835Assigned != null) {
                Document writeOffComputation = svc835Assigned.get("write_off_computation", Document.class);
                if (writeOffComputation != null) {
                    Object writeOffValue = writeOffComputation.get("write_off");
                    if (writeOffValue instanceof Integer) {
                        previousWriteoffValue = ((Integer) writeOffValue).doubleValue();
                    } else if (writeOffValue instanceof Double) {
                        previousWriteoffValue = (Double) writeOffValue;
                    }
                }
            }
        }

        // Retrieve new_attributes write_off
        Document newAttributes = svc835UserEditLogDoc.get("new_attributes", Document.class);
        if (newAttributes != null) {
            Document svc835Assigned = newAttributes.get("svc_835_assigned", Document.class);
            if (svc835Assigned != null) {
                Document writeOffComputation = svc835Assigned.get("write_off_computation", Document.class);
                if (writeOffComputation != null) {
                    Object writeOffValue = writeOffComputation.get("write_off");
                    if (writeOffValue instanceof Integer) {
                        newWriteoffValue = ((Integer) writeOffValue).doubleValue();
                    } else if (writeOffValue instanceof Double) {
                        newWriteoffValue = (Double) writeOffValue;
                    }
                }
            }
        }

        // Parse expected values
        double expectedPreviousWriteoff = Double.parseDouble(previousWriteoff);
        double expectedNewWriteoff = Double.parseDouble(newWriteoff);

        // Assert values
        System.out.println("previousWriteoffValue = " + previousWriteoffValue);
        Assert.assertEquals(
                "svc835user_edit_log previous writeoff value does not match",
                expectedPreviousWriteoff,
                previousWriteoffValue,
                0.0
        );
        System.out.println("newWriteoffValue = " + newWriteoffValue);
        Assert.assertEquals(
                "svc835user_edit_log new writeoff value does not match",
                expectedNewWriteoff,
                newWriteoffValue,
                0.0
        );
    }

    @Then("verify svc835Assigned writeoff value Before_AdjustingWithPr {string} After_AdjustingWithPr {string}")
    public void verifySvc835AssignedWriteoffValueBefore_AdjustingWithPrAfter_AdjustingWithPr(String before, String after) {
        Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned("qa-cls5", checkNumber);
        String eraSplitFileAssignedId = eraSplitFileDoc.getString("era_split_file_assigned_id");
        String contentHash = eraSplitFileDoc.getString("content_hash");
        Document svc835AssignedDoc = eraCycleUtils.getSvc835Assigned(partner, contentHash, claimIdentifier);

        if (svc835AssignedDoc != null) {
            Document svc835Assigned = svc835AssignedDoc.get("write_off_computation", Document.class);
            if (svc835Assigned != null) {
                Decimal128 decimalWriteOffComputation = svc835Assigned.get("write_off", Decimal128.class);
                double writeOffComputation = decimalWriteOffComputation.doubleValue(); // Convert to double
                Decimal128 writeoffDecimal = svc835Assigned.get("writeoff_svc_before_adjusting_with_pr", Decimal128.class);
                double writeoffSvcBeforeAdjustingWithPr = writeoffDecimal.doubleValue(); // Convert to double

                System.out.println("updated writeOffComputation = " + writeOffComputation);
                System.out.println("writeoff Svc Before AdjustingWithPr = " + writeoffSvcBeforeAdjustingWithPr);

                double expectedWriteoffBefore = Double.parseDouble(before);
                double expectedWriteoffAfter = Double.parseDouble(after);

                Assert.assertEquals("svc835Assigned writeOffComputation.writeoff value does not match", expectedWriteoffAfter, writeOffComputation, 0);
                Assert.assertEquals("svc835Assigned writeoffSvcBeforeAdjustingWithPr value does not match", expectedWriteoffBefore, writeoffSvcBeforeAdjustingWithPr, 0);
            }
        }
    }

    @Then("verify era_Split_File_Assigned writeback claimproctable writeoff value {string}")
    public void verifyEra_Split_File_AssignedWritebackClaimproctableWriteoffValue(String expectedWriteoff) {
        Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned("qa-cls5", checkNumber);
        List<Document> writebackRecords = (List<Document>) eraSplitFileDoc.get("writeback_records");

        if (writebackRecords != null) {
            for (Document record : writebackRecords) {
                // Check if the table_name is "claimproc"
                String tableName = record.getString("table_name");
                if ("claimproc".equals(tableName)) {
                    Document data = record.get("data", Document.class);
                    if (data != null) {
                        String writeOff = data.getString("WriteOff"); // Retrieve the WriteOff value
                        System.out.println("WriteOff from claimproc: " + writeOff);
                        Assert.assertEquals("era_Split_File_Assigned writeback claimproctable writeoff value does not match", expectedWriteoff, writeOff);

                    }
                }
            }
        }
    }

    @When("edit claim {string} -- PreWriteOff = {string}, Set Write-off = {string}")
    public void editClaimPreWriteOffSetWriteOff(String claimIdentifier, String preWriteoff, String writeoffValue) {
        Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned("qa-cls5", checkNumber);
        String eraSplitFileAssignedId = eraSplitFileDoc.getString("era_split_file_assigned_id");
        String contentHash = eraSplitFileDoc.getString("content_hash");
        Document svc835AssignedDoc = eraCycleUtils.getSvc835Assigned(partner, contentHash, claimIdentifier);
        String svcHash = svc835AssignedDoc.getString("svc_hash");

        double pre_writeoff = Double.parseDouble(preWriteoff);
        double edit_writeoff = Double.parseDouble(writeoffValue);

        apiUtils.postWriteOffEdit(eraSplitFileAssignedId, partner, svcHash, claimIdentifier, pre_writeoff, edit_writeoff);
    }


    @And("retrace sweep without ERA")
    public void retraceSweepWithoutERA() {
        int responseStatusCode = 0;

        for (int i = 0; i < 5; i++) {
            if (responseStatusCode != 200) {
//                String dfiAccount = "54f0a5e3-754e-4661-8f68-f81d5ff92e99";
                Response sweepResponse = apiUtils.getSweepResponse(partner, dfiAccountId);
                responseStatusCode = sweepResponse.statusCode();
                System.out.println("sweep responseStatusCode.toString() = " + responseStatusCode);
            } else {
                break;
            }
        }

        Assert.assertEquals("sweep is not successful", 200, responseStatusCode);
        BrowserUtils.waitFor(10);
    }

    @When("edit claim {string} -- PreAdjustment = {string},  PostAdjustment = {string}")
    public void editClaimPreAdjustmentPostAdjustment(String claimIdentifier, String preAdj, String postAdj) {
        Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned("qa-cls5", checkNumber);
        String eraSplitFileAssignedId = eraSplitFileDoc.getString("era_split_file_assigned_id");
        String contentHash = eraSplitFileDoc.getString("content_hash");
        Document svc835AssignedDoc = eraCycleUtils.getSvc835Assigned(partner, contentHash, claimIdentifier);
        String svcHash = svc835AssignedDoc.getString("svc_hash");

        double preAdjustment = Double.parseDouble(preAdj);
        double postAdjustment = Double.parseDouble(postAdj);

        apiUtils.matchPayerPRAdjustment(eraSplitFileAssignedId, partner, svcHash, claimIdentifier, preAdjustment, postAdjustment);
    }

    @Then("verify opendental_writeback_archive_1 adjustment table AdjAmt is {string}")
    public void verifyOpendental_writeback_archive_1AdjustmentTableAdjAmtIs(String expectedAdjAmt) {
        String actualAdjAmt = "";

        try {
            boolean recordFound = false;

            for (int i = 0; i < 50; i++) {
                // Fetch the document
                Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned("qa-cls5", checkNumber);
                String transactionId = eraSplitFileDoc.getString("writeback_transaction_id");

                // Fetch the adjustment table document
                Document adjustmentTable = eraCycleUtils.getOpendentalWritebackArchive_1Table("qa-cls5", transactionId, "adjustment");

                if (adjustmentTable != null) {
                    Document data = adjustmentTable.get("data", Document.class);

                    if (data != null) {
                        actualAdjAmt = data.getString("AdjAmt");
                        recordFound = true;
                        break;
                    }
                }

                // Wait before retrying
                BrowserUtils.waitFor(2);
                System.out.println("Adjustment table is null or data is missing, retrying...");
            }

            if (!recordFound) {
                throw new IllegalStateException("Adjustment table data was not found within the allowed time.");
            }

            // Logging the actual AdjAmt value
            System.out.println("opendental_writeback_archive_1 Adj Table actualAdjAmt is " + actualAdjAmt);

            // Assertion
            Assert.assertEquals("AdjAmt does not match in opendental_writeback_archive_1", expectedAdjAmt, actualAdjAmt);
        } catch (Exception e) {
            logger.error("Error: ", e);
            e.printStackTrace();
            throw new RuntimeException("Failed to verify AdjAmt in opendental_writeback_archive_1", e);
        }
    }

    @Then("verify eraSplitFileAssigned writeback records adjustment table is created and AdjAmt is {string}")
    public void verifyEraSplitFileAssignedWritebackRecordsAdjustmentTableIsCreatedAndAdjAmtIs(String expectedAdjAmt) {
        Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned("qa-cls5", checkNumber);
        String adjAmt = eraCycleUtils.getEraSplitFileAssignedAdjustmentTableData(eraSplitFileDoc).getString("AdjAmt");
        Assert.assertNotNull("AdjAmt field is missing in the adjustment record.", adjAmt);
        Assert.assertEquals("AdjAmt value does not match the expected value.", expectedAdjAmt, adjAmt);
        System.out.println("AdjAmt matched successfully: " + adjAmt);
    }

    @Then("verify adjustment_1 has adjustment record with Id {string}, adjAmt {string}, adjType {string}")
    public void verifyAdjustment_1HasAdjustmentRecordWithIdAdjAmtAdjType(String adjId, String adjAmt, String adjType) {
        BrowserUtils.waitFor(5);
        Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned(partner, checkNumber);
        String transactionId = eraSplitFileDoc.getString("writeback_transaction_id");
        System.out.println("transactionId = " + transactionId);
        Document adjustmentTable = eraCycleUtils.getOpendentalWritebackArchive_1Table("qa-cls5", transactionId, "adjustment");
        String recordId = adjustmentTable.get("record_id").toString();
        System.out.println("recordId = " + recordId);
        Document adjustment_1Doc = eraCycleUtils.getAdjustment1("qa-cls5", practiceId, recordId);

        String actualAdjId = adjustment_1Doc.getString("adjustment_id");
        System.out.println("actualAdjId = " + actualAdjId);
        String actualAdjAmt = adjustment_1Doc.get("amount").toString();
        String actualAdjType = adjustment_1Doc.getString("type");

        Assert.assertEquals("Adjustment_1 collection AdjId does not match", adjId, actualAdjId);
        Assert.assertEquals("Adjustment_1 collection adjAmt does not match", adjAmt, actualAdjAmt);
        Assert.assertEquals("Adjustment_1 collection adjType does not match", adjType, actualAdjType);
    }

    @Then("verify claim level rejection reason as {string}")
    public void verifyClaimLevelRejectionReasonAs(String expectedErrorMessage) {
        Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned(partner, checkNumber);
        List<String> rejectionReasons = eraSplitFileDoc.getList("claim_level_rejection_reasons", String.class);
        System.out.println("Claim Level Rejection Reasons: " + rejectionReasons);
        Assert.assertTrue(rejectionReasons.get(0).contains(expectedErrorMessage));
    }

    boolean eligibleForEmbargoe = true;

    @When("check if there is an ERA with in ninety days")
    public void checkIfThereIsAnERAWithInNinetyDays() {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");

        Date ninetyDaysAgo = Date.from(Instant.now().minusSeconds(90L * 24 * 60 * 60));

        String filter = "{"
                + "trans_ref_id: { $ne: 'c5eNhX7CRaW6' }, "
                + "ledger_id: { $exists: true }, "
                + "'payee.tin': '747253938', "
                + "'payer.payer_name': { $regex: 'Delta Dental of Arizona' }, "
                + "createdAt: { $gte: ISODate('" + ninetyDaysAgo.toInstant().toString() + "') }"
                + "}";
        BasicDBObject query = BasicDBObject.parse(filter);

        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();

        if (eraSplitFileDoc != null) {
            // Document exists, so the collection is not empty for the given query
            eligibleForEmbargoe = true;
            System.out.println("ERA status  should be created. embargoe eligibity Document found: " + eraSplitFileDoc.toJson());
        } else {
            // No matching documents, the collection is effectively "empty" for the query
            eligibleForEmbargoe = false;
            System.out.println("embargoe eligibity matching documents NOT found. ERA status should be transferred");
        }

        mongoClient.close();
    }

    @Then("verify ERA is embargoed or not embargoed")
    public void verifyERAIsEmbargoedOrNotEmbargoed() {
        BrowserUtils.waitFor(200);

        Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned(partner, checkNumber);
        String status = eraSplitFileDoc.getString("status");
        System.out.println("assignedCollectionStatus = " + status);
        BrowserUtils.waitFor(5);
        if (eligibleForEmbargoe) {
            Assert.assertEquals("status is not created", "created", status);
        } else {
            Assert.assertEquals("status is not created", "transferred", status);

        }
    }

    @Then("verify adjustment_1 adjAmt {string}, adjType {string}")
    public void verifyAdjustment_1AdjAmtAdjType(String adjAmt, String adjType) {
        String actualAdjId = null;
        String actualAdjAmt = null;
        String actualAdjType = null;

        try {
            boolean recordFound = false;

            for (int i = 0; i < 50; i++) {
                // Fetch the ERA Split File document
                Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned(partner, checkNumber);
                String transactionId = eraSplitFileDoc.getString("writeback_transaction_id");
                System.out.println("transactionId = " + transactionId);

                // Fetch the adjustment table document
                Document adjustmentTable = eraCycleUtils.getOpendentalWritebackArchive_1Table("qa-cls5", transactionId, "adjustment");
                if (adjustmentTable != null) {
                    String recordId = adjustmentTable.get("record_id").toString();
                    System.out.println("recordId = " + recordId);

                    // Fetch the adjustment_1 document
                    Document adjustment_1Doc = eraCycleUtils.getAdjustment1("qa-cls5", practiceId, recordId);

                    if (adjustment_1Doc != null) {
                        actualAdjId = adjustment_1Doc.getString("adjustment_id");
                        System.out.println("actualAdjId = " + actualAdjId);
                        actualAdjAmt = adjustment_1Doc.get("amount").toString();
                        actualAdjType = adjustment_1Doc.getString("type");
                        recordFound = true;
                        break;
                    }
                }

                // Wait before retrying
                BrowserUtils.waitFor(2);
                System.out.println("Adjustment_1 document is null, waiting for the record...");
            }

            if (!recordFound) {
                throw new IllegalStateException("Adjustment_1 record was not found within the allowed time.");
            }

            // Assertions
            Assert.assertEquals("Adjustment_1 collection adjAmt does not match", adjAmt, actualAdjAmt);
            Assert.assertEquals("Adjustment_1 collection adjType does not match", adjType, actualAdjType);
        } catch (Exception e) {
            logger.error("Error: ", e);
            e.printStackTrace();
            throw new RuntimeException("Failed to verify adjustment_1 collection", e);
        }
    }

    @Then("verify eraSplitFileAssigned writeback records adjustment table is created")
    public void verifyEraSplitFileAssignedWritebackRecordsAdjustmentTableIsCreated() {
        try {
            // Fetch the ERA Split File document
            Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned("qa-cls5", checkNumber);

            if (eraSplitFileDoc != null) {
                // Fetch the writeback_records array
                List<Document> writebackRecords = eraSplitFileDoc.getList("writeback_records", Document.class);

                if (writebackRecords != null && !writebackRecords.isEmpty()) {
                    boolean adjustmentTableExists = false;

                    // Iterate through the writeback_records array
                    for (Document record : writebackRecords) {
                        String tableName = record.getString("table_name");
                        if ("adjustment".equalsIgnoreCase(tableName)) {
                            adjustmentTableExists = true;
                            System.out.println("Adjustment table found in writeback_records.");
                            break;
                        }
                    }

                    // Assert that the adjustment table exists
                    Assert.assertTrue("Adjustment table is not created in writeback_records.", adjustmentTableExists);
                } else {
                    System.out.println("writeback_records is null or empty.");
                    Assert.fail("writeback_records array is not present or empty in eraSplitFileAssigned.");
                }
            } else {
                System.out.println("eraSplitFileDoc is null.");
                Assert.fail("eraSplitFileAssigned document not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An exception occurred while verifying writeback records adjustment table: " + e.getMessage());
        }
    }


    @Then("verify opendental_writeback_archive_1 adjustment table is created")
    public void verifyOpendental_writeback_archive_1AdjustmentTableIsCreated() {
        try {
            // Fetch the ERA Split File document
            Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned("qa-cls5", checkNumber);

            if (eraSplitFileDoc != null) {
                // Get the transaction ID from the document
                String transactionId = eraSplitFileDoc.getString("writeback_transaction_id");
                System.out.println("Transaction ID: " + transactionId);

                // Wait loop to give time for the adjustment table to be created
                Document adjustmentTable = null;
                for (int attempt = 0; attempt < 300; attempt++) {
                    adjustmentTable = eraCycleUtils.getOpendentalWritebackArchive_1Table("qa-cls5", transactionId, "adjustment");
                    if (adjustmentTable != null) {
                        break;
                    } else {
                        BrowserUtils.waitFor(1);
                    }
                }

                // Check if the adjustment table is present after waiting
                if (adjustmentTable != null) {
                    System.out.println("Adjustment table found in opendental_writeback_archive_1.");
                } else {
                    System.out.println("Adjustment table not found in opendental_writeback_archive_1.");
                    Assert.fail("Adjustment table is not created in opendental_writeback_archive_1 after waiting.");
                }
            } else {
                System.out.println("ERA Split File document not found.");
                Assert.fail("ERA Split File document is null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An exception occurred while verifying the adjustment table: " + e.getMessage());
        }
    }


    @Then("verify eraSplitFileAssigned writeback records adjustment table is NOT created")
    public void verifyEraSplitFileAssignedWritebackRecordsAdjustmentTableIsNotCreated() {
        try {
            // Fetch the ERA Split File document
            Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned("qa-cls5", checkNumber);

            if (eraSplitFileDoc != null) {
                // Fetch the writeback_records array
                List<Document> writebackRecords = eraSplitFileDoc.getList("writeback_records", Document.class);

                if (writebackRecords != null && !writebackRecords.isEmpty()) {
                    boolean adjustmentTableExists = false;

                    // Iterate through the writeback_records array
                    for (Document record : writebackRecords) {
                        String tableName = record.getString("table_name");
                        if ("adjustment".equalsIgnoreCase(tableName)) {
                            adjustmentTableExists = true;
                            System.out.println("Adjustment table found in writeback_records.");
                            break;
                        }
                    }

                    // Assert that the adjustment table does not exist
                    Assert.assertFalse("Adjustment table is created in writeback_records.", adjustmentTableExists);
                } else {
                    System.out.println("writeback_records is null or empty.");
                    Assert.fail("writeback_records array is not present or empty in eraSplitFileAssigned.");
                }
            } else {
                System.out.println("eraSplitFileDoc is null.");
                Assert.fail("eraSplitFileAssigned document not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An exception occurred while verifying writeback records adjustment table: " + e.getMessage());
        }
    }

    @Then("verify opendental_writeback_archive_1 adjustment table is NOT created")
    public void verifyOpendental_writeback_archive_1AdjustmentTableIsNotCreated() {
        try {
            // Fetch the ERA Split File document
            Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned("qa-cls5", checkNumber);

            if (eraSplitFileDoc != null) {
                // Get the transaction ID from the document
                String transactionId = eraSplitFileDoc.getString("writeback_transaction_id");
                System.out.println("Transaction ID: " + transactionId);

                // Retrieve the adjustment table from the opendental_writeback_archive_1 collection
                Document adjustmentTable = eraCycleUtils.getOpendentalWritebackArchive_1Table("qa-cls5", transactionId, "adjustment");

                // Check if the adjustment table is absent
                if (adjustmentTable == null) {
                    System.out.println("Adjustment table not found in opendental_writeback_archive_1.");
                } else {
                    System.out.println("Adjustment table found in opendental_writeback_archive_1.");
                    Assert.fail("Adjustment table is created in opendental_writeback_archive_1.");
                }
            } else {
                System.out.println("ERA Split File document not found.");
                Assert.fail("ERA Split File document is null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An exception occurred while verifying the adjustment table: " + e.getMessage());
        }
    }

    @Then("verify plb_adjustment_835_assigned record is created")
    public void verifyPlb_adjustment_835_assignedRecordIsCreated() {
        Document eraSplitFileDoc = eraCycleUtils.getEraSplitFileAssigned("qa-cls5", checkNumber);

        if (eraSplitFileDoc != null) {
            // Get the content_hash from the document
            String stHash = eraSplitFileDoc.getString("content_hash");
            System.out.println("stHash: " + stHash);

            // Retrieve the adjustment table from the opendental_writeback_archive_1 collection
            Document getPlbAdjustment835Assigned = eraCycleUtils.getPlbAdjustment835Assigned("qa-cls5", stHash);

            // Check if the adjustment table is absent
            if (getPlbAdjustment835Assigned == null) {
                System.out.println("getPlbAdjustment835Assigned not found");
            } else {
                System.out.println("getPlbAdjustment835Assigned record found ");
                String provNum = getPlbAdjustment835Assigned.getString("ProvNum");
                System.out.println("provNum = " + provNum);
                String patNum = getPlbAdjustment835Assigned.getString("PatNum");
                System.out.println("patNum = " + patNum);
                Decimal128 adjAmt = getPlbAdjustment835Assigned.get("AdjAmt", Decimal128.class);
                BigDecimal adjAmtValue = adjAmt.bigDecimalValue();
                System.out.println("adjAmtValue = " + adjAmtValue.toString());
                BigDecimal expected = new BigDecimal("10.0");

                // Use compareTo for numeric equality
                Assert.assertTrue("BigDecimal values are not equal!", expected.compareTo(adjAmtValue) == 0);
            }
        } else {
            System.out.println("ERA Split File document not found.");
            Assert.fail("ERA Split File document is null.");
        }
    }

    @Then("verify ERA is disqualified due to negative amount")
    public void verifyERAIsDisqualifiedDueToNegativeAmount() {
        Document eraSplitFileAssignedDoc = eraCycleUtils.getEraSplitFileAssigned(partner,checkNumber);
        // Extract the first element of the claim_level_rejection_reasons array
        List<String> rejectionReasons = (List<String>) eraSplitFileAssignedDoc.get("rejection_reasons");
        String firstRejectionReason = "";
        if (rejectionReasons != null && !rejectionReasons.isEmpty()) {
            firstRejectionReason = rejectionReasons.get(0);
            System.out.println("First ERA Rejection Reason: " + firstRejectionReason);
        } else {
            System.out.println("No rejection reasons found.");
        }

        Assert.assertEquals("error message is not expected","BPR02 is negative: -155.52",firstRejectionReason);
    }

    @Then("verify CLP is disqualified due to {string}")
    public void verifyCLPIsDisqualifiedDueTo(String claimLevelRejectionReason) {
        Document eraSplitFileAssignedDoc = eraCycleUtils.getEraSplitFileAssigned(partner,checkNumber);
        // Extract the first element of the claim_level_rejection_reasons array
        List<String> rejectionReasons = (List<String>) eraSplitFileAssignedDoc.get("claim_level_rejection_reasons");
        String firstRejectionReason = "";
        if (rejectionReasons != null && !rejectionReasons.isEmpty()) {
            firstRejectionReason = rejectionReasons.get(0);
            System.out.println("First CLP Rejection Reason: " + firstRejectionReason);
        } else {
            System.out.println("No rejection reasons found.");
        }

        int indexOfRejectionReason = firstRejectionReason.indexOf(":");
        String expectedRejectionReason= firstRejectionReason.substring(indexOfRejectionReason+2);
        System.out.println("expectedRejectionReason = " + expectedRejectionReason);
        Assert.assertEquals("error message is not expected",claimLevelRejectionReason,expectedRejectionReason);
    }

    @Then("verify task is created for {string}")
    public void verifyTaskIsCreatedFor(String claimNum) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> task_1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "task_1");
        BasicDBObject query = BasicDBObject.parse("{'issue.denied_claim.claim_identifier':'"+claimNum+"'}");
        Document task_1Doc = task_1Coll.find(query).first();
        Document issue = (Document) task_1Doc.get("issue");
        String issueType = issue.getString("issue_type");
        System.out.println("task_1 created Task issueType for "+claimNum+" = " + issueType);
        Assert.assertEquals("expected issue type is not -denial-","denial",issueType);
    }

    @When("set workable denial entry for {string} code {string} as {string}")
    public void setWorkableDenialEntryForCodeAs(String practiceId, String codeNumber, String codeStatus) {
        boolean codeBooleanStatus = Boolean.parseBoolean(codeStatus);  // Convert string to boolean
        eraCycleUtils.setWorkableDenialEntry(partner, practiceId, codeNumber, codeBooleanStatus);
    }

    @Then("verify era_split_file_assigned collection status wait three minutes: {string}")
    public void verifyEra_split_file_assignedCollectionStatusWaitThreeMinutes(String expectedStatus) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> eraSplitFileAssignedColl = MongoDBUtils.connectMongodb(mongoClient, partner, "era_split_file_assigned");
        BasicDBObject query = BasicDBObject.parse("{check_number:'" + checkNumber + "'}");
        System.out.println("query = " + query);
        Document eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();

        try {
            String actualStatus = (String) eraSplitFileDoc.get("status");
            System.out.println("actualStatus = " + actualStatus);

//             Wait for the expected status for up to a certain time
            for (int i = 0; i < 180; i++) {
                if (!actualStatus.equals(expectedStatus)) {
                    BrowserUtils.waitFor(1);
                    eraSplitFileDoc = eraSplitFileAssignedColl.find(query).first();
                    actualStatus = (String) eraSplitFileDoc.get("status");
                    System.out.println("actualStatus while waiting = " + actualStatus);
                } else {
                    break;
                }
            }

            // Check if the status is still "hold" after waiting and expectedStatus is "auto_finalize"
//            if (expectedStatus.equals("auto_finalize") && actualStatus.equals("hold")) {
//                Document filter = new Document("check_number", checkNumber);
//                Document update = new Document("$set", new Document("status", "auto_finalize"));
//                MongoDBUtils.executeUpdateQuery(eraSplitFileAssignedColl, filter, update);
//                System.out.println("Status has been changed to 'auto_finalize' manually");
//                actualStatus = "auto_finalize"; // Update the actualStatus variable for assertion
//            }

            // Assert that the final status matches the expected status
            try {
                Assert.assertEquals("Status is not as expected", expectedStatus, actualStatus);
            } catch (AssertionError e) {
                logger.error("Status is not as expected: Expected = " + expectedStatus + ", Actual = " + actualStatus, Arrays.toString(e.getStackTrace()));
                throw e;  // Rethrow the exception so the test still fails
            }
        } catch (Exception e) {
            logger.error("Error: " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        } finally {
            mongoClient.close();
        }
    }

    JsonPath achOutList = null;

    @When("get ACH_Out_Details for partner {string}")
    public void getACH_Out_DetailsForPartner(String partner) {
//        BrowserUtils.waitFor(30);
        // Fetch the JSON response as a JsonPath object
        achOutList = apiUtils.getAchOutList(endToEndId,partner);
    }

    @Then("verify ACH Out Details Category: {string} and Amount: {string}")
    public void verifyACHOutDetailsCategoryAndAmount(String expectedCategory, String expectedAmount) {
        String jsonResponse = achOutList.prettify();
        JSONArray rootArray = new JSONArray(jsonResponse);

        // Loop through the response array
        for (int i = 0; i < rootArray.length(); i++) {
            JSONObject obj = rootArray.getJSONObject(i);

            // Extract required fields
            String reference = obj.getString("reference");
            String category = obj.getString("category");

            // Extract amount (stored as $numberDecimal)
            JSONObject amountObj = obj.getJSONObject("amount");
            String amount = amountObj.getString("$numberDecimal");  // Keep as-is, no formatting

            // Print values for debugging
            System.out.println("Reference: " + reference);
            System.out.println("Category: " + category);
            System.out.println("Amount: " + amount);

            // Assertions
            Assert.assertEquals("Amount does not match!", expectedAmount, amount);
            Assert.assertEquals("Reference does not match!", checkNumber, reference);
            Assert.assertEquals("Category does not match!", expectedCategory, category);
        }
    }

    @When("reconcile {string} ledger")
    public void reconcileLedger(String ledgerCategory) {
        String hsbcPayableSubmissionId = apiUtils.getAchOutField(listAchOut,endToEndId,"hsbc_payables_submission_id");
        System.out.println("hsbcPayableSubmissionId = " + hsbcPayableSubmissionId);
        apiUtils.reconcileLedger("qa-cls5",hsbcPayableSubmissionId);
    }

    @Then("verify legder_1 collection {string} category status {string}")
    public void verifyLegder_1CollectionCategoryStatus(String category, String expectedStatus) {
        MongoClient mongoClient = MongoDBUtils.getMongoClient();
        MongoCollection<Document> ledger_1Coll = MongoDBUtils.connectMongodb(mongoClient, partner, "ledger_1");
        BasicDBObject query = BasicDBObject.parse("{reference:'" + reference + "', category:'"+category+"', account_name:'virtual'}");
        System.out.println("query = " + query);
        Document ledgerDoc = ledger_1Coll.find(query).first();
        System.out.println("ledgerDoc.toJson() = " + ledgerDoc.toJson());

        JsonPath ledgerJson = JsonPath.from(ledgerDoc.toJson());
        String actualStatus = ledgerJson.get("status");
        System.out.println("actualStatus = " + actualStatus);
        logUtils.assertEquals("expected status: " + expectedStatus + " not matched with actual: " + actualStatus + " ", expectedStatus, actualStatus);
    }

    @Then("verify hsbc_payables_submission_1 collection {string}")
    public void verifyHsbc_payables_submission_1Collection(String status) {
        System.out.println("endToEndId = " + endToEndId);

        String actualReconcileStatus = null;

        // Loop with a max retry limit of 100 times
        for (int i = 0; i < 100; i++) {
            Document hsbcPayablesSubmission1 = MongoDBUtils.getMongoDoc(
                    "qa-cls5", "hsbc_payables_submission_1", "{end_to_end_id:'" + endToEndId + "'}"
            );
            actualReconcileStatus = hsbcPayablesSubmission1.getString("status");
            System.out.println("actual reconcile status = " + actualReconcileStatus);

            // Break loop if status is "Reconciled"
            if ("reconciled".equals(actualReconcileStatus)) {
                break;
            }

            BrowserUtils.waitFor(1); // Wait 1 second before retrying
        }

        // Final assertion after the loop
        Assert.assertEquals(
                "hsbc_payables_submission_1 collection reconcile status is NOT reconciled",
                "reconciled",
                actualReconcileStatus
        );
    }

}