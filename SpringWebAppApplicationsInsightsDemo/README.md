
Azure Application Insights Demo using Spring Web App
====================================================
This is example application that demonstrates how to forward application metrics to Azure Application Insights from a Spring application.  This is a working example based on https://azure.microsoft.com/en-us/documentation/articles/app-insights-java-live/

Requirements
------------------
- Maven 3.0 or higher to build 
- Java 7 or higher
- A Java web server (e.g. Tomcat 7.0 or higher)

Useful Links
-----------------
Application Insights - Introduction
https://azure.microsoft.com/en-us/documentation/articles/app-insights-overview/

Instructions for adding Application Insights integration to your Java Web App
https://azure.microsoft.com/en-us/documentation/articles/app-insights-java-live/

Check firewall is not blocking incoming/outcoming Application Insights traffic
https://azure.microsoft.com/en-us/documentation/articles/app-insights-ip-addresses/


Run this application
--------
Step 1:  Edit resources/ApplicationInsights.xml and update this line with your instrumentation key
```
<InstrumentationKey>***YOUR_INSTRUMENTATION_KEY***</InstrumentationKey>
```

Step 2: Run 'mvn install' in this project to build the war file

Step 3: Deploy the war in your web server

Step 4: Use this URL to send REST request to the web application (assuming your web application runs on port 8080)
```
http://localhost:8080/spring-azureai/hello?name=sam
```
This should return a JSON response such as 
```
{"name":"sam","mood":7}
```
'name' is the parameter read from the GET request and mood is a random number from 0 to 10.

This request will trigger a TRACE log statement and a Telemetry Event to be forwarded to Application Insights (AI) immediately.

Step 4: Wait for 5-10 minutes for the data to refresh, then you should be able to view charts like these in the Application Insights charts:
- #### View each of the HTTP requests 
The Metrics Explorer comes with a bar chart showing the request counts during the day
![Azure AI HTTP Request Screenshots](screenshots/azure_demo_http.png?raw=true)

- ####Search for exception or activities in your application log
![Azure AI Log Search Screenshots](screenshots/azure_demo_trace.png?raw=true)

- #### Create charts using metrics from custom events 
![Azure AI Event Telemetry ](screenshots/azure_demo_events.png?raw=true)

For more information on getting the most out of Application Insights, I suggest reading through the [Azure AI Introduction](https://azure.microsoft.com/en-us/documentation/articles/app-insights-overview/) 
