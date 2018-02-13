
Execute AML Web Service through BES REST API
====================================================

Microsoft offers several machine learning services.  The Azure Machine Learning (AML) Studio was an early offering that supports simple creation of experiments that can be deployed as a RESTful Web Service. An experiment can be a simple data processing pipeline for transforming/cleaning data or for making predictions using a trained model.  Web service deployed through AML provides two types of API to run these experiments.  One is the real-time Request-Response Service (RRS) where the client where the REST call to the web service is synchronous and the second API is for Batch Execution Service (BES) where the client can submit a job then poll for the job status and results.  Clearly, the RRS approach is much simpler as only a single REST call is required to run the job and retrieve results in the same request, however RRS will timeout if the experiment runs longer than 90 minutes.  BES is the alternative if the experiment tends to run over the 90 minutes time constraint.  This code is an example Java client for executing jobs through BES:

Workflow using BES:

- Upload input dataset to cloud storage
- Submit and start the AML job
- Check the AML job status at a regular interval
- Once confirmed the AML job has been completed, download the output data from cloud storage

![AML BES ](assets/AzureMLBES.png?raw=true)

Instead of using the built-in ML modules in AML Studio, my experiment uses a R script to perform very simple recommendation based on the input dataset.  Azure Blob storage has been chosen as the data store for storing input and output data.

Requirements
------------------
- Maven 3.0 or higher to build
- Java 7 or higher
- Azure Subscription


Useful Links
-----------------
If you have never used AML Studio, I suggest you read through this online doc to set up your first workspace:
https://docs.microsoft.com/en-us/azure/machine-learning/studio/what-is-ml-studio

A tutorial on how to create an experiment:
https://docs.microsoft.com/en-gb/azure/machine-learning/studio/create-experiment

Some not-very-well-written doc about the Web Services:
https://docs.microsoft.com/en-us/azure/machine-learning/machine-learning-consume-web-services


Steps
--------
####1. Setup an experiment in Azure ML and deploy it as a Web Service
Login to Azure ML Studio and create the following experiment:
![R Script experiment](assets/Azure_ML_Experiment.png?raw=true)

This example code uses the following R Script experiment to demonstate how a job in AML is executed.
![R Script ](assets/R_Script.png?raw=true)


**What is going on??**
By connecting the sample 'Movie Rating' to the dataset1 port of the R script module indicates the input file follows the same schema as the 'Moving Rating' dataset.  Web Services modules are connected to the input and output port of the R script module such that this experiment can published as a Web Service.  As mentioned before, the input data must be provided as a file from a cloud storage, hence the Web Service input expects the storage connection string.  The R script simply outputs the movie list in descending rating orders and forward it to the Web Service output module.  Once this experiment is setup and ran, you can use the 'Visualise' action (by right clicking the output port of the R script module) to view the results by using 'Movie Rating' as the input dataset.

>> Note: Creating a working experiment in AML Studio is as straight-forward as it seems, so if you already have an AML web service that you want to connect to, then you are not required to setup the above experiment and skip to Step 3.

Finally, click the **Deploy as Web Service** action in the bottom task bar to deploy the web service

####2. Setup Azure cloud storage account
Step 2: Create an Azure Blob Storage account for storing the data files.  Alternatively, create a new container to keep these separate from your other blob data under the same account.

####3. Update resource properties in Java client
Step 3: Once the experiment is published as a web service, update the following properties in *resources/azureML.properties* with your experiment and storage account details:
 - API URL
 - API key
 - Blob Storage Account
 - Blob Storage Account Key
 - Blob Storage Container Name

**Check the REST request JSON string matches the format provided in the properties file**
Go to BES Web Service page for your published experiment and look for **jobs** action section.  Check the expected JSON string matches the format given in *resources/azureML.properties* file (property azure.ml.bes.submitJobRequest.json).

####4. Run
```
Upload input dataset ....
Submit job to Azure ML ....
Send start job request to Azure ML ....
Checking job status....
Mon Mar 06 14:54:58 AEST 2017:GET https://ussouthcentral.services.azureml.net/workspaces/XXXXXXXXX/services/XXXXXXX/jobs/XXXXXXX?api-version=2.0 HTTP/1.1
Job status: NotStarted
Checking job status....
Mon Mar 06 14:54:59 AEST 2017:GET https://ussouthcentral.services.azureml.net/workspaces/XXXXXXXXX/services/XXXXXXX/jobs/XXXXXXX?api-version=2.0 HTTP/1.1
Job status: Running
Checking job status....
Mon Mar 06 14:55:00 AEST 2017:GET https://ussouthcentral.services.azureml.net/workspaces/XXXXXXXXX/services/XXXXXXX/jobs/XXXXXXX?api-version=2.0 HTTP/1.1
Job status: Running
...
Checking job status....
Mon Mar 06 14:55:16 AEST 2017:GET https://ussouthcentral.services.azureml.net/workspaces/XXXXXXXXX/services/XXXXXXX/jobs/XXXXXXX?api-version=2.0 HTTP/1.1
Job status: Finished
Mon Mar 06 14:55:17 AEST 2017: Job completed
Download input dataset....
Output file: /home/username/dev/azure_ml_demo/azureMLOutput.csv
```