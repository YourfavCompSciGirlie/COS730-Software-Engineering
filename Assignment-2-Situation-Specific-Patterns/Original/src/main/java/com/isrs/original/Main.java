package com.isrs.original;

import java.util.Arrays;

/**
 * Entry point for the baseline (original) implementation of the
 * Intelligent Submission and Review System.
 *
 * Runs a complete end-to-end scenario matching every interaction
 * in the provided baseline sequence diagram.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("  Intelligent Submission and Review System — BASELINE");
        System.out.println("================================================================\n");

        // ── Set up the Database with pre-loaded reviewers ───────────────
        Database database = new Database();
        database.addReviewer(new Reviewer("R001", "Dr. Smith",    "Machine Learning", 2, false));
        database.addReviewer(new Reviewer("R002", "Dr. Johnson",  "Data Science",     1, false));
        database.addReviewer(new Reviewer("R003", "Dr. Williams", "Machine Learning", 4, false));
        database.addReviewer(new Reviewer("R004", "Dr. Brown",    "Networks",         3, true));   // conflict
        database.addReviewer(new Reviewer("R005", "Dr. Davis",    "Machine Learning", 6, false));  // overloaded

        // ── Create participant objects ───────────────────────────────────
        Validator validator                 = new Validator();
        ReviewerManager reviewerManager     = new ReviewerManager(database);
        NotificationService notifService    = new NotificationService();
        EvaluationManager evaluationManager = new EvaluationManager(database, notifService);

        SubmissionController controller = new SubmissionController(
                validator, database, reviewerManager, evaluationManager
        );

        UI ui = new UI(controller);

        // ── Create a valid research output ──────────────────────────────
        ResearchOutput output = new ResearchOutput(
                "Deep Learning for Climate Prediction",
                "This paper presents a novel deep-learning approach to long-range "
                        + "climate prediction using transformer architectures.",
                Arrays.asList("Alice Chen", "Bob Kumar"),
                "Machine Learning",
                "alice.chen@university.edu"
        );

        // ── Scenario 1: Valid submission (main success path) ────────────
        System.out.println("--- Scenario 1: Valid submission ---");
        String result = ui.submitResearchOutput(output);
        System.out.println("\n>>> FINAL RESULT: " + result);

        // ── Scenario 2: Invalid submission (alt path) ───────────────────
        System.out.println("\n\n--- Scenario 2: Invalid submission (missing title) ---");
        ResearchOutput invalid = new ResearchOutput(
                "",   // empty title → validation failure
                "Some abstract text",
                Arrays.asList("Charlie Doe"),
                "Biology",
                "charlie@university.edu"
        );

        // Need fresh evaluation manager for second run
        EvaluationManager evalManager2 = new EvaluationManager(database, notifService);
        SubmissionController controller2 = new SubmissionController(
                validator, database, reviewerManager, evalManager2
        );
        UI ui2 = new UI(controller2);

        String result2 = ui2.submitResearchOutput(invalid);
        System.out.println("\n>>> FINAL RESULT: " + result2);

        System.out.println("\n================================================================");
        System.out.println("  Baseline execution complete.");
        System.out.println("================================================================");
    }
}
