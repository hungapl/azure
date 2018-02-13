import java.util.Date;

public class MyApp {

    public static void main(String[] args) throws Exception {
        final String inputFilename = "azureMLInput.csv";
        final String outputFilename = "azureMLOutput.csv";
        final String inputDataset = "MovieId, Rating\n1,10\n,3,6\n4,7\n5,4\n6,6\n7,2\n8,5\n9,10\n10,4";
        AzureMLBESClient client = new AzureMLBESClient("azureML.properties", inputFilename, outputFilename);
        client.setVerbose(true);
        client.uploadInputDataset(inputFilename, inputDataset);
        String jobId = client.submitJob();
        client.startJob(jobId);
        final int ESTIMATED_JOB_DURATION_IN_MILLIS = 1000;
        Thread.sleep(ESTIMATED_JOB_DURATION_IN_MILLIS);
        while (!client.isJobFinished(jobId)) {
            Thread.sleep(1000);
        }
        System.out.println(new Date() + ": Job completed");
        client.downloadDataset(outputFilename, outputFilename);
    }
}
