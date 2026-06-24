package com.isrs.optimised;

import java.util.List;

/**
 * Main entry point for the optimised ISRS implementation.
 * Uses the same test scenarios as the baseline for equivalence verification.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("  Intelligent Submission and Review System — OPTIMISED");
        System.out.println("================================================================");

        // --- Set up repositories ---
        SubmissionRepository submissionRepo = new SubmissionRepository();
        ReviewerRepository reviewerRepo = new ReviewerRepository();
        ScoreRepository scoreRepo = new ScoreRepository();

        // --- Populate reviewers (same data as baseline) ---
        reviewerRepo.add(new Reviewer("R001", "Dr. Smith",    "AI",              2, false));
        reviewerRepo.add(new Reviewer("R002", "Dr. Johnson",  "Machine Learning", 1, false));
        reviewerRepo.add(new Reviewer("R003", "Dr. Williams", "Data Science",    4, false));
        reviewerRepo.add(new Reviewer("R004", "Dr. Brown",    "NLP",             0, true));  // conflict
        reviewerRepo.add(new Reviewer("R005", "Dr. Davis",    "Computer Vision", 6, false)); // overloaded

        // --- Wire up services ---
        Validator validator = new Validator();
        ReviewAssignmentService reviewAssignment = new ReviewAssignmentService(reviewerRepo);
        DecisionEngine decisionEngine = new DecisionEngine();
        EvaluationService evaluationService = new EvaluationService(scoreRepo, decisionEngine);
        NotificationService notificationService = new NotificationService();

        SubmissionController controller = new SubmissionController(
                validator, submissionRepo, reviewAssignment, evaluationService, notificationService
        );
        UI ui = new UI(controller);

        // ===== Scenario 1: Valid submission =====
        System.out.println("\n--- Scenario 1: Valid submission ---");
        ResearchOutput validSubmission = new ResearchOutput(
                "Deep Learning for Climate Prediction",
                "This paper proposes a novel deep learning approach...",
                List.of("A. Researcher", "B. Collaborator"),
                "Artificial Intelligence",
                "researcher@university.ac.za"
        );
        String result1 = ui.submitResearchOutput(validSubmission);
        System.out.println("\n>>> FINAL RESULT: " + result1);

        // ===== Scenario 2: Invalid submission (missing title) =====
        System.out.println("\n\n--- Scenario 2: Invalid submission (missing title) ---");
        ResearchOutput invalidSubmission = new ResearchOutput(
                "",
                "Abstract text present",
                List.of("C. Author"),
                "Biology",
                "invalid@university.ac.za"
        );
        String result2 = ui.submitResearchOutput(invalidSubmission);
        System.out.println("\n>>> FINAL RESULT: " + result2);

        System.out.println("\n================================================================");
        System.out.println("  Optimised execution complete.");
        System.out.println("================================================================");
    }
}
