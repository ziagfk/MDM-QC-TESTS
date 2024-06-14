package com.mdm.tests;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssertionLogger {
    private Map<String, List<AssertionResult>> testResults = new HashMap<>();

    public void addAssertionResult(String testName, String description, boolean passed, String errorMessage) {
        testResults.computeIfAbsent(testName, k -> new ArrayList<>())
                .add(new AssertionResult(description, passed, errorMessage));
    }

    public void writeResultsToFile(String filename) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");

        int testCount = 0;
        for (Map.Entry<String, List<AssertionResult>> entry : testResults.entrySet()) {
            String testName = entry.getKey();
            List<AssertionResult> assertions = entry.getValue();

            jsonBuilder.append("  \"").append(testName).append("\": [\n");

            for (int i = 0; i < assertions.size(); i++) {
                AssertionResult result = assertions.get(i);
                jsonBuilder.append("    {\n");
                jsonBuilder.append("      \"description\": \"").append(result.getDescription()).append("\",\n");
                jsonBuilder.append("      \"passed\": ").append(result.isPassed()).append(",\n");
                jsonBuilder.append("      \"errorMessage\": ").append(result.getErrorMessage() != null ? "\"" + result.getErrorMessage() + "\"" : null).append("\n");
                jsonBuilder.append("    }");
                if (i < assertions.size() - 1) {
                    jsonBuilder.append(",");
                }
                jsonBuilder.append("\n");
            }

            jsonBuilder.append("  ]");
            if (++testCount < testResults.size()) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append("\n");
        }

        jsonBuilder.append("}");

        try (FileWriter fileWriter = new FileWriter(filename)) {
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


