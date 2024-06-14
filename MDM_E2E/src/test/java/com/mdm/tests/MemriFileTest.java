package com.mdm.tests;


import org.testng.Assert;
import org.testng.annotations.Test;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class MemriFileTest {

    private static String sessionId = null;
    private static String apiKey = "fce7b4466d9b48c68955658380bc1dfc";
    private static String baseUrl = "https://memri-api-dev.mrisimmons.com/MemriAPI/api/memri/";

    @Test
    public void testCreateMemriFile() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl+"/CreatememriFiles?studyCode=f22mri&environment=4"))
                .header("User-Agent", "insomnia/8.6.1")
                .header("externalAPIKey", apiKey)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());

        String data=response.body();
        data=data.replace("{","").replace("}","");
        Map<String, String> keyValueMap = Arrays.stream(data.split(","))
                .map(kv -> kv.split(":"))
                .filter(kvArray -> kvArray.length == 2)
                .collect(Collectors.toMap(kv -> kv[0].replace("\"",""), kv -> kv[1].replace("\"","")));

        sessionId= keyValueMap.get("sessionID");
        System.out.println(sessionId);

        // Assert that the response body is not empty
        Assert.assertNotNull(response.body(), "Response body should not be null");

        // Assert that the response contains the expected session ID
        Assert.assertTrue(response.body().contains("sessionID"), "Response should contain sessionID");

        // Assert that the session ID is extracted successfully
        Assert.assertNotNull(sessionId, "SessionID should not be null");

    }

    //Check the Job status of the create memri api
    @Test
    public void testStatusCheck() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl+"/JobStatus"))
                .header("Content-Type", "application/json")
                .header("User-Agent", "insomnia/8.6.1")
                .header("externalAPIKey", apiKey)
                .method("POST", HttpRequest.BodyPublishers.ofString("{\n\tsessionID: \""+sessionId+"\"\n}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());

        // Assert that the response body is not empty
        Assert.assertNotNull(response.body(), "Response body should not be null");

    }


    }



//    public void downloadBlob(){
//
//        try {
//            String connectionString = "DefaultEndpointsProtocol=https;AccountName=azeusmdmetlqa;AccountKey=your_account_key";
//            CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
//
//            //Create the service client object for credentialed access to the Blob service.
//            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
//
//            // Retrieve a reference to a container.
//            CloudBlobContainer container = blobClient.getContainerReference("memri-validation");
//
//
//            CloudBlob blob1 =container.getBlockBlobReference("Validation_Report_14_33_30.zip");
//
//            blob1.download(new FileOutputStream("C:\\Users\\georgec\\Documents\\" + blob1.getName()));
//
//
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        } catch (StorageException e) {
//            e.printStackTrace();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//    }




