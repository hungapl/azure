package com.hungap.azure;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.file.CloudFileClient;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

public class AzureStorageConnection {

    public static CloudFileClient connection(String accountName, String accountKey) throws URISyntaxException, InvalidKeyException {
        return CloudStorageAccount.parse("DefaultEndpointsProtocol=http;AccountName=" +
                accountName + ";AccountKey="+accountKey).createCloudFileClient();
    }

}
