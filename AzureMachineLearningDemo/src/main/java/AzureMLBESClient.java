import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Date;
import java.util.Properties;

/**
 * A client for executing an Azure ML Job via Batch Execution Service
 *
 * @author bonapetite
 *         Created: 04/03/17
 */
public class AzureMLBESClient {

    private final Gson GSON = new GsonBuilder().create();
    private final HttpClient HTTP_CLIENT = HttpClientBuilder.create().build();

    private String dataStorageConnectionString;
    private String storageAccountName, storageAccountKey, storageContainer;
    private String apiUrl, apiVersion, apiKey, submitJobRequestJson;
    private boolean verbose;

    public AzureMLBESClient(String propertiesFile, String inputFilename, String outputFilename) throws IOException {
        init(propertiesFile, inputFilename, outputFilename);
    }

    private void init(String propertiesFile, String inputFilename, String outputFilename) throws IOException {
        Properties properties = new Properties();
        properties.load(getFile(propertiesFile));
        storageAccountName = properties.get("azure.ml.data.accountName").toString();
        storageAccountKey = properties.get("azure.ml.data.accountKey").toString();
        storageContainer = properties.get("azure.ml.data.container").toString();
        apiUrl = properties.get("azure.ml.depmix.apiURL").toString();
        apiVersion = properties.get("azure.ml.depmix.apiVersion").toString();
        apiKey = properties.get("azure.ml.depmix.apiKey").toString();
        submitJobRequestJson = properties.get("azure.ml.bes.submitJobRequest.json").toString()
                .replace("{ACCOUNT_NAME}", storageAccountName)
                .replace("{ACCOUNT_KEY}", storageAccountKey)
                .replace("{CONTAINER}", storageContainer)
                .replace("{INPUT_FILENAME}", inputFilename)
                .replace("{OUTPUT_FILENAME}", outputFilename);
        dataStorageConnectionString = properties.get("azure.ml.data.connectionString").toString()
                .replace("{ACCOUNT_NAME}", storageAccountName)
                .replace("{ACCOUNT_KEY}", storageAccountKey);
    }

    public String submitJob() throws Exception {
        if (verbose) {
            System.out.println("Submit job to Azure ML ....");
        }
        return httpPost(getUrl("jobs"), submitJobRequestJson).replaceAll("\"", "");
    }

    public void startJob(String jobId) throws Exception {
        if (verbose) {
            System.out.println("Send start job request to Azure ML ....");
        }
        httpPost(getUrl("jobs/" + jobId + "/start"), null);
    }

    public boolean isJobFinished(String jobId) throws Exception {
        if (verbose) {
            System.out.println("Checking job status....");
        }
        JobResultResponse response = httpGet(getUrl("jobs/" + jobId));
        if (verbose) {
            System.out.println("Job status: " + response.StatusCode);
        }
        if (response.StatusCode.equals("Finished") || response.StatusCode.equals("4")) {
            return true;
        }
        return false;
    }

    private String getUrl(String action) {
        return apiUrl + action + "?api-version=" + apiVersion;
    }

    public void uploadInputDataset(String filename, String inputDataset) throws URISyntaxException, InvalidKeyException, StorageException, IOException {
        if (verbose) {
            System.out.println("Upload input dataset ....");
        }
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(dataStorageConnectionString);
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference(storageContainer);
        container.createIfNotExists();

        CloudBlockBlob blob = container.getBlockBlobReference(filename);
        blob.upload(new ByteArrayInputStream(inputDataset.getBytes()), inputDataset.length());
    }

    public void downloadDataset(String filename, String outputFilePath) throws URISyntaxException, InvalidKeyException, StorageException, IOException {
        if (verbose) {
            System.out.println("Download input dataset....");
        }
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(dataStorageConnectionString);
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference(storageContainer);

        CloudBlockBlob blob = container.getBlockBlobReference(filename);
        File file = new File(outputFilePath);
        blob.download(new FileOutputStream(file));
        System.out.println("Output file: " + file.getAbsolutePath());
    }

    private JobResultResponse httpGet(String url) throws IOException, HttpRequestException {
        HttpGet request = new HttpGet(url);
        setHttpHeader(request);

        // Call REST API and retrieve response content
        if (verbose) {
            System.out.println(new Date() + ":" + request);
        }
        HttpResponse authResponse = HTTP_CLIENT.execute(request);

        int statusCode = authResponse.getStatusLine().getStatusCode();
        String responseEntity = EntityUtils.toString(authResponse.getEntity());
        if (!isRequestSuccessful(authResponse)) {
            throw new HttpRequestException(url, statusCode, responseEntity);
        }
        return GSON.fromJson(responseEntity, JobResultResponse.class);
    }

    private String httpPost(String url, String body) throws IOException, HttpRequestException {
        HttpPost post = new HttpPost(url);
        setHttpHeader(post);
        if (body != null) {
            StringEntity entity = new StringEntity(body, "UTF-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("text/json");
            post.setEntity(entity);
        }

        HttpResponse authResponse = HTTP_CLIENT.execute(post);
        HttpEntity entity1 = authResponse.getEntity();
        String entityString = (entity1 != null) ? EntityUtils.toString(entity1) : "";
        if (!isRequestSuccessful(authResponse)) {
            throw new HttpRequestException(url, authResponse.getStatusLine().getStatusCode(), entityString);
        }
        return entityString;
    }

    /**
     * Update http header to include parameters required for Azure ML BES request
     */
    private void setHttpHeader(HttpRequestBase request) {
        request.setHeader("Accept", "text/json");
        request.setHeader("Accept-Charset", "UTF-8");
        request.setHeader("Authorization", ("Bearer " + apiKey));
    }

    private boolean isRequestSuccessful(HttpResponse response) {
        int statusCode = response.getStatusLine().getStatusCode();
        return statusCode == 200 || statusCode == 204;
    }

    private FileInputStream getFile(String filename) throws FileNotFoundException {
        return new FileInputStream(getClass().getClassLoader().getResource(filename).getFile());
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
