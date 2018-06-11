package com.hungap.azure;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.*;
import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;

/**
 * For managing files in Azure File storage
 */
public class AzureStorageFileManager {

    private final Logger LOGGER = Logger.getLogger(AzureStorageFileManager.class.getSimpleName());

    /**
     * Find files from Azure file directory that match the given regex, download the files and generated an zip archive
     * This does not support recursive search
     *
     * @param client Azure file client for connecting to storage
     * @param fileShare Name of file share
     * @param dirPath Path to directory or subdirectory in file share.  Directory se
     * @param regex Ther regex to match
     * @param zipFilePath Absolute path of output zip file
     * @return true if successful otherwise false
     */
    public boolean archive(CloudFileClient client, String fileShare, String dirPath, String regex, String zipFilePath) {
        try {
            File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
            temp.delete();
            temp.mkdir();
            String[] path = dirPath.split("/");
            CloudFileDirectory dir = client.getShareReference(fileShare)
                    .getRootDirectoryReference();

            for (String d : path) {
                dir = dir.getDirectoryReference(d);
            }
            List<String> files = matchingFiles(dir, regex);
            LOGGER.info("Found " + files.size() + " matching files.");
            if (files.isEmpty()) {
                LOGGER.info("No matching files.");
                return false;
            }
            int promptEveryN = 20;
            int i = 1;
            LOGGER.info("Downloading files.");
            for (String file : files) {
                dir.getFileReference(file).downloadToFile(temp.getPath() + File.separator + file);
                if (i % promptEveryN == 0) {
                    LOGGER.info("Downloaded " + i + "/" + files.size() + " files.");
                }
                i++;
            }
            LOGGER.info("Downloaded " + files.size() + "/" + files.size() + " files.");
            LOGGER.info("Creating zip " + zipFilePath);
            addFilesToZip(temp, files, new File(zipFilePath));
            return true;
        } catch (Exception e) {
            LOGGER.info("ERROR: Failed to archive files from " + fileShare + ": dirPath=" +dirPath + ", regex: " + regex);
            e.printStackTrace();
            return false;
        }
    }

    private void addFilesToZip(File sourceDir, List<String> files, File destination) throws IOException, ArchiveException {
        OutputStream archiveStream = new java.io.FileOutputStream(destination);
        ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);

        for (int i = 0; i < files.size(); i++) {
            File currentFile = new File(sourceDir.getPath() + File.separator + files.get(i));
            String relativeFilePath = sourceDir.toURI().relativize(
                    new File(currentFile.getAbsolutePath()).toURI()).getPath();

            ZipArchiveEntry entry = new ZipArchiveEntry(currentFile, relativeFilePath);
            entry.setSize(currentFile.length());

            archive.putArchiveEntry(entry);
            archive.write(IOUtils.toByteArray(new java.io.FileInputStream(currentFile)));
            archive.closeArchiveEntry();
        }
        archive.finish();
        archiveStream.close();
    }

    /**
     * Delete files that match the given regex in an Azure file share directory
     *
     * @param client Azure file client for connecting to storage
     * @param fileShare Name of file share
     * @param dirPath Path to directory or subdirectory in file share
     * @param regex The regex to match
     * @param force Do not prompt before deletion if true
     * @throws URISyntaxException
     * @throws StorageException
     */
    public void batchDelete(CloudFileClient client, String fileShare, String dirPath, String regex, boolean force) throws URISyntaxException, StorageException {
        String[] path = dirPath.split("/");
        CloudFileDirectory dir = client.getShareReference(fileShare)
                        .getRootDirectoryReference();

        for(String d: path) {
            dir = dir.getDirectoryReference(d);
        }

        List<String> files = matchingFiles(dir, regex);
        if (files.isEmpty()) {
            LOGGER.info("No matching files");
            return;
        }

        boolean delete = force;
        LOGGER.info("Found the following matching files:");
        LOGGER.info(String.join("\n", files));
        if (!force) {
            LOGGER.info("Delete all " + files.size() + " matching files ? (Y/N)");
            Scanner reader = new Scanner(System.in);
            String answer = reader.next("[Y|y|N|n]");
            delete = answer.toUpperCase().equals("Y");
        }

        int promptEveryN = 100;
        int i = 1;
        if (delete) {
            LOGGER.info("Start deleting " + files.size() + " files...");
            for (String file : files) {
                dir.getFileReference(file).delete();
                if (i % promptEveryN == 0) {
                    LOGGER.info("Deleted " + i + "/" + files.size() + " files.");
                }
                i++;
            }
        }
        LOGGER.info("Deleted " + files.size() + "/" + files.size() + " files.");
    }

    private static List<String> matchingFiles(CloudFileDirectory dir, String regex) {
        List<String> files = new ArrayList<>();
        for ( ListFileItem fileItem : dir.listFilesAndDirectories() ) {
            String filename = fileItem.getUri().toString().replace(dir.getUri().toString(), "").replace("/", "");
            if (filename.matches(regex)) {
                files.add(filename);
            }
        }
        return files;
    }
}
