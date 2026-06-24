package com.isrs.optimised;

/**
 * Validates the format of a research output submission.
 * Unchanged from baseline — validation logic is already well-scoped.
 */
public class Validator {

    public boolean validateFormat(ResearchOutput data) {
        CallCounter.count("Validator.validateFormat");
        System.out.println("[Validator] Validating submission format...");

        if (data == null) {
            System.out.println("[Validator] FAILED: data is null");
            return false;
        }
        if (data.getTitle() == null || data.getTitle().isEmpty()) {
            System.out.println("[Validator] FAILED: missing title");
            return false;
        }
        if (data.getAuthors() == null || data.getAuthors().isEmpty()) {
            System.out.println("[Validator] FAILED: missing authors");
            return false;
        }
        if (data.getField() == null || data.getField().isEmpty()) {
            System.out.println("[Validator] FAILED: missing research field");
            return false;
        }
        if (data.getAbstractText() == null || data.getAbstractText().isEmpty()) {
            System.out.println("[Validator] FAILED: missing abstract");
            return false;
        }

        System.out.println("[Validator] Validation PASSED");
        return true;
    }
}
