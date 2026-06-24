package com.isrs.original;

/**
 * Validates the format of a research output submission.
 *
 * Diagram participant: Validator
 * Diagram interaction: SubmissionController -> Validator : validateFormat(data)
 */
public class Validator {

    /**
     * Validates that the research output has all required fields.
     * Returns true if valid, false otherwise.
     */
    public boolean validateFormat(ResearchOutput data) {
        CallCounter.count("Validator.validateFormat");
        System.out.println("[Validator] Validating submission format...");

        if (data == null) {
            System.out.println("[Validator] Validation FAILED: data is null");
            return false;
        }
        if (data.getTitle() == null || data.getTitle().isEmpty()) {
            System.out.println("[Validator] Validation FAILED: missing title");
            return false;
        }
        if (data.getAuthors() == null || data.getAuthors().isEmpty()) {
            System.out.println("[Validator] Validation FAILED: missing authors");
            return false;
        }
        if (data.getField() == null || data.getField().isEmpty()) {
            System.out.println("[Validator] Validation FAILED: missing research field");
            return false;
        }
        if (data.getAbstractText() == null || data.getAbstractText().isEmpty()) {
            System.out.println("[Validator] Validation FAILED: missing abstract");
            return false;
        }

        System.out.println("[Validator] Validation PASSED");
        return true;
    }
}
