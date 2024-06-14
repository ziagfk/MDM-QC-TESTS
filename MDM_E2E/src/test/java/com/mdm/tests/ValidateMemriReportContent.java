package com.mdm.tests;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static com.mdm.tests.MdmTestUtilities.assertHeaderEquals;
import static com.mdm.tests.MdmTestUtilities.extractZipContents;




import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;



public class ValidateMemriReportContent {
    //Declare the list to store assertion results
    private List<AssertionResult> assertionResults = new ArrayList<>();
//    @Test
//    public void beforeClass() {
//        TestNG testng = new TestNG();
//        testng.addListener(new CustomJSONReporter()); // Register the custom reporter
//        testng.setTestClasses(new Class[] { ValidateMemriReportContent.class }); // Set your test class
//        testng.run();
//    }

    @Test
    public void testFileCount() {
        // Test file counts
        BlobClient blobClient = new BlobClientBuilder()
                .connectionString(MdmConstants.connectionString)
                .containerName(MdmConstants.containerName)
                .blobName(MdmConstants.zipFilePath)
                .buildClient();

        int[] counts = extractZipContents(blobClient);

        Assert.assertEquals(counts[0], 6, "Number of CSV files is not as expected.");
        Assert.assertEquals(counts[1], 1, "Number of CPP files is not as expected.");

        System.out.println("All file counts are as expected.");
    }


    @Test
    public void testFilePathTimestamp() {

        // Extract the timestamp from the file path
        String[] parts = MdmConstants.zipFilePath.split("/");
        String timestamp = parts[0];

        // Convert the timestamp to LocalDate
        LocalDate fileDate = LocalDate.parse(timestamp, DateTimeFormatter.ofPattern("yyyy_MM_dd"));

        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Assert that the timestamp matches the current date
        Assert.assertEquals(currentDate, fileDate, "Timestamp in file path does not match current date.");
    }


    @Test
    public void testCSVHeaderValidationNotFoundInMdm() {
        System.out.println("Starting testCSVHeaderValidationNotFoundInMdm...");

        // Initialize BlobClient
        BlobClient blobClient = new BlobClientBuilder()
                .connectionString(MdmConstants.connectionString)
                .containerName(MdmConstants.containerName)
                .blobName(MdmConstants.zipFilePath)
                .buildClient();

        System.out.println("Blob client initialized: " + blobClient);

        try (ByteArrayInputStream blobInputStream = new ByteArrayInputStream(blobClient.downloadContent().toBytes());
             ZipInputStream zipInputStream = new ZipInputStream(blobInputStream)) {
            ZipEntry entry;
            int entryCount = 0; // To count the number of entries processed
            while ((entry = zipInputStream.getNextEntry()) != null) {
                entryCount++;
                System.out.println("Processing entry: " + entry.getName());

                if (!entry.getName().toLowerCase().contains("not_found_in_mdm") || !entry.getName().toLowerCase().endsWith(".csv")) {
                    continue; // Skip files not matching the criteria
                }

                // Read CSV content
                System.out.println("Processing CSV file: " + entry.getName());
                try (CSVReader reader = new CSVReader(new InputStreamReader(zipInputStream))) {
                    // Read the headers of the CSV file
                    String[] headers = reader.readNext();

                    // Verify the headers
                    Assert.assertNotNull(headers, "Headers are null for file: " + entry.getName());
                    System.out.println("Headers are not null for file: " + entry.getName());

                    // Assert for each header
                    assertHeaderEquals(MdmConstants.Study, headers[0], "First header");
                    assertHeaderEquals(MdmConstants.Code, headers[1], "Second header");
                    assertHeaderEquals(MdmConstants.PersistentID, headers[2], "Third header");
                    assertHeaderEquals(MdmConstants.Text, headers[3], "Fourth header");
                    assertHeaderEquals(MdmConstants.Reason, headers[4], "Fifth header");

                    // Print success message
                    System.out.println("Headers validated successfully for file: " + entry.getName() + "\n");
                }
            }
            System.out.println("Total entries processed: " + entryCount);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Finishing testCSVHeaderValidationNotFoundInMdm...");
    }





    @Test
    public void testCSVHeaderValidationTabulated() {
        // Initialize BlobClient
        BlobClient blobClient = new BlobClientBuilder()
                .connectionString(MdmConstants.connectionString)
                .containerName(MdmConstants.containerName)
                .blobName(MdmConstants.zipFilePath)
                .buildClient();

        System.out.println("Blob client initialized: " + blobClient);
        try (ByteArrayInputStream blobInputStream = new ByteArrayInputStream(blobClient.downloadContent().toBytes());
             ZipInputStream zipInputStream = new ZipInputStream(blobInputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.getName().toLowerCase().contains("_tabulated")&& !entry.getName().toLowerCase().contains("differences_report") || !entry.getName().toLowerCase().endsWith(".csv")) {
                    continue; // Skip files not matching the criteria
                }

                // Read CSV content
                try (CSVReader reader = new CSVReader(new InputStreamReader(zipInputStream))) {
                    // Read the headers of the CSV file
                    String[] headers = reader.readNext();

                    // Verify the headers
                    Assert.assertNotNull(headers, "Headers are null for file: " + entry.getName());
                    System.out.println("Headers are not null for file: " + entry.getName());

                    // Assert for each header
                    assertHeaderEquals(MdmConstants.Study, headers[0], "First header");
                    assertHeaderEquals(MdmConstants.Text, headers[1], "Second header");
                    assertHeaderEquals(MdmConstants.CCP, headers[2], "Third header");
                    assertHeaderEquals(MdmConstants.PersistentID, headers[3], "Fourth header");
                    assertHeaderEquals(MdmConstants.Weighted, headers[4], "Fifth header");
                    assertHeaderEquals(MdmConstants.Unweighted, headers[5], "Sixth header");

                    // Print success message
                    System.out.println("Headers validated successfully for file: " + entry.getName() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testCSVHeaderValidationTabulatedDifferenceReport() {

        BlobClient blobClient = new BlobClientBuilder().connectionString(MdmConstants.connectionString).containerName(MdmConstants.containerName).blobName(MdmConstants.zipFilePath).buildClient();
        try (ByteArrayInputStream blobInputStream = new ByteArrayInputStream(blobClient.downloadContent().toBytes());
             ZipInputStream zipInputStream = new ZipInputStream(blobInputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.getName().toLowerCase().contains("tabulated_differences_report")|| !entry.getName().toLowerCase().endsWith(".csv")) {
                    continue; // Skip files not matching the criteria
                }

                // Read CSV content
                try (CSVReader reader = new CSVReader(new InputStreamReader(zipInputStream))) {
                    // Read the headers of the CSV file
                    String[] headers = reader.readNext();

                    // Verify the headers
                    Assert.assertNotNull(headers, "Headers are null for file: " + entry.getName());
                    System.out.println("Headers are not null for file: " + entry.getName());

                    // Assert for each header
                    assertHeaderEquals(MdmConstants.FileCurrent, headers[0], "First header");
                    assertHeaderEquals(MdmConstants.FilePrior, headers[1], "Second header");
                    assertHeaderEquals(MdmConstants.Label, headers[2], "Third header");
                    assertHeaderEquals(MdmConstants.CCPExpressionCurrent, headers[3], "Fourth header");
                    assertHeaderEquals(MdmConstants.CCPExpressionPrior, headers[4], "Fifth header");
                    assertHeaderEquals(MdmConstants.PersistentIDCurrent, headers[6], "Sixth header");
                    assertHeaderEquals(MdmConstants.PersistentIDPrior, headers[7], "Seventh header");
                    assertHeaderEquals(MdmConstants.WeightedCurrent, headers[8], "Eighth header");
                    assertHeaderEquals(MdmConstants.WeightedPrior, headers[9], "Ninth header");
                    assertHeaderEquals(MdmConstants.UnweightedCurrent, headers[10], "Tenth header");
                    assertHeaderEquals(MdmConstants.UnweightedPrior, headers[11], "Eleventh header");
                    assertHeaderEquals(MdmConstants.Reason, headers[12], "twelfth header");

                    // Print success message
                    System.out.println("Headers validated successfully for file: " + entry.getName() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCSVHeaderValidationCbfDifferencesReport() {

        BlobClient blobClient = new BlobClientBuilder().connectionString(MdmConstants.connectionString).containerName(MdmConstants.containerName).blobName(MdmConstants.zipFilePath).buildClient();
        try (ByteArrayInputStream blobInputStream = new ByteArrayInputStream(blobClient.downloadContent().toBytes());
             ZipInputStream zipInputStream = new ZipInputStream(blobInputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.getName().toLowerCase().contains("cbf_differences_report")|| !entry.getName().toLowerCase().endsWith(".csv")) {
                    continue; // Skip files not matching the criteria
                }

                // Read CSV content
                try (CSVReader reader = new CSVReader(new InputStreamReader(zipInputStream))) {
                    // Read the headers of the CSV file
                    String[] headers = reader.readNext();

                    // Verify the headers
                    Assert.assertNotNull(headers, "Headers are null for file: " + entry.getName());
                    System.out.println("Headers are not null for file: " + entry.getName());

                    // Assert for each header
                    assertHeaderEquals(MdmConstants.Type, headers[0], "First header");
                    assertHeaderEquals(MdmConstants.Catname, headers[1], "Second header");
                    assertHeaderEquals(MdmConstants.Keyname, headers[2], "Third header");
                    assertHeaderEquals(MdmConstants.Newvalue, headers[3], "Fourth header");
                    assertHeaderEquals(MdmConstants.Oldvalue, headers[4], "Fifth header");

                    // Print success message
                    System.out.println("Headers validated successfully for file: " + entry.getName() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testCSVHeaderValidationReportCount() {
        BlobClient blobClient = new BlobClientBuilder()
                .connectionString(MdmConstants.connectionString)
                .containerName(MdmConstants.containerName)
                .blobName(MdmConstants.zipFilePath)
                .buildClient();

        try (ByteArrayInputStream blobInputStream = new ByteArrayInputStream(blobClient.downloadContent().toBytes());
             ZipInputStream zipInputStream = new ZipInputStream(blobInputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.getName().toLowerCase().contains("report_count") || !entry.getName().toLowerCase().endsWith(".csv")) {
                    continue; // Skip files not matching the criteria
                }

                // Read CSV content
                try (CSVReader reader = new CSVReader(new InputStreamReader(zipInputStream))) {
                    // Read the headers of the CSV file
                    String[] headers = reader.readNext();

                    // Verify the headers
                    assertCondition(headers != null, "Headers are null for file: " + entry.getName());

                    // Assert for each header
                    assertHeaderEquals(MdmConstants.CCP, headers[0], "First header");
                    assertHeaderEquals(MdmConstants.PersistentID, headers[1], "Second header");
                    assertHeaderEquals(MdmConstants.Label, headers[2], "Third header");
                    assertHeaderEquals(MdmConstants.ProjectedEngine, headers[3], "Fourth header");
                    assertHeaderEquals(MdmConstants.ProjectedDB, headers[4], "Fifth header");
                    assertHeaderEquals(MdmConstants.CountEngine, headers[5], "Sixth header");
                    assertHeaderEquals(MdmConstants.CountDB, headers[6], "Seventh header");
                    assertHeaderEquals(MdmConstants.Reason, headers[7], "Eighth header");
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public void assertCondition(boolean condition, String message) {
        try {
            Assert.assertTrue(condition, message);
            addAssertionResult(message, true, null);
        } catch (AssertionError e) {
            addAssertionResult(message, false, e.getMessage());
        }
    }

    public void assertHeaderEquals(String expected, String actual, String description) {
        try {
            Assert.assertEquals(actual, expected, description);
            addAssertionResult(description + ": Expected " + expected + ", got " + actual, true, null);
        } catch (AssertionError e) {
            addAssertionResult(description + ": Expected " + expected + ", got " + actual, false, e.getMessage());
        }
    }

    public void addAssertionResult(String description, boolean passed, String errorMessage) {
        assertionResults.add(new AssertionResult(description, passed, errorMessage));
    }

    @AfterClass
    public void writeResultsToFile() {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[\n");

        for (int i = 0; i < assertionResults.size(); i++) {
            AssertionResult result = assertionResults.get(i);
            jsonBuilder.append("  {\n");
            jsonBuilder.append("    \"description\": \"").append(result.getDescription()).append("\",\n");
            jsonBuilder.append("    \"passed\": ").append(result.isPassed()).append(",\n");
            jsonBuilder.append("    \"errorMessage\": ").append(result.getErrorMessage() != null ? "\"" + result.getErrorMessage() + "\"" : null).append("\n");
            jsonBuilder.append("  }");
            if (i < assertionResults.size() - 1) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append("\n");
        }

        jsonBuilder.append("]");

        try (FileWriter fileWriter = new FileWriter("assertionResults.json")) {
            fileWriter.write(jsonBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class AssertionResult {
        private String description;
        private boolean passed;
        private String errorMessage;

        public AssertionResult(String description, boolean passed, String errorMessage) {
            this.description = description;
            this.passed = passed;
            this.errorMessage = errorMessage;
        }

        public String getDescription() {
            return description;
        }

        public boolean isPassed() {
            return passed;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

