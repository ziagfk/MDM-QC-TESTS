package com.mdm.tests;

import com.azure.storage.blob.BlobClient;
import org.testng.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MdmTestUtilities {

    //Method to extract validation report zip file from the memri validation container
    public static int[] extractZipContents(BlobClient blobClient) {
        int csvCount = 0;
        int cppCount = 0;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            blobClient.download(outputStream);

            try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(outputStream.toByteArray()))) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String fileName = entry.getName();
                    if (fileName.endsWith(".csv")) {
                        csvCount++;
                    } else if (fileName.endsWith(".cc2")) {
                        cppCount++;
                    }
                    zipInputStream.closeEntry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new int[]{csvCount, cppCount};
    }




    public static void assertHeaderEquals(String expected, String actual, String headerName) {
        try {
            System.out.println("Comparing " + headerName + ": Expected - " + expected + ", Actual - " + actual);
            Assert.assertEquals(expected, actual, headerName + " is not as expected.");
            System.out.println(headerName + " is as expected.");
        } catch (AssertionError e) {
            System.out.println(e.getMessage().replaceAll("[\\[\\]]", "")); // Remove brackets
        }
    }


















}
