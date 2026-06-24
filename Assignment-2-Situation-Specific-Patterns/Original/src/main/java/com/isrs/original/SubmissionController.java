package com.isrs.original;

import java.util.List;

/**
 * Fat controller that orchestrates the ENTIRE submission, review assignment,
 * evaluation, and notification flow.
 *
 * Diagram participant: SubmissionController
 * Diagram interaction: UI -> SubmissionController : submit(data)
 */
public class SubmissionController {

    private Validator validator;
    private Database database;
    private ReviewerManager reviewerManager;
    private EvaluationManager evaluationManager;

    public SubmissionController(Validator validator, Database database,
                                ReviewerManager reviewerManager,
                                EvaluationManager evaluationManager) {
        this.validator = validator;
        this.database = database;
        this.reviewerManager = reviewerManager;
        this.evaluationManager = evaluationManager;
    }

    /**
     * Orchestrates the entire submission and evaluation pipeline.
     * Every method call below is annotated with its corresponding diagram interaction.
     *
     * Diagram: UI -> SubmissionController : submit(data)
     */
    public String submit(ResearchOutput data) {
        CallCounter.count("SubmissionController.submit");
        System.out.println("\n[SubmissionController] Processing submission...");

        // ── Step 1: Validation ──────────────────────────────────────────
        // Diagram: SubmissionController -> Validator : validateFormat(data)
        boolean isValid = validator.validateFormat(data);

        // Diagram: alt [invalid]
        if (!isValid) {
            // Diagram: SubmissionController --> UI : return error
            System.out.println("[SubmissionController] Validation failed. Returning error to UI.");
            return "ERROR: Invalid submission format";
        }

        // Diagram: else [valid]

        // ── Step 2: Save submission ─────────────────────────────────────
        // Diagram: SubmissionController -> Database : saveSubmission(data)
        String submissionId = database.saveSubmission(data);
        System.out.println("[SubmissionController] Submission saved: " + submissionId);

        // ── Step 3: Get available reviewers ──────────────────────────────
        // Diagram: SubmissionController -> ReviewerManager : getAvailableReviewers()
        List<Reviewer> filteredReviewers = reviewerManager.getAvailableReviewers();
        System.out.println("[SubmissionController] " + filteredReviewers.size() + " reviewers available");

        // ── Step 4: Assign reviewers ────────────────────────────────────
        // Diagram: loop [assign reviewers]
        //          SubmissionController -> Reviewer : assignReview()
        for (Reviewer reviewer : filteredReviewers) {
            reviewer.assignReview(submissionId);
        }

        // ── Step 5: Start evaluation ────────────────────────────────────
        // Diagram: SubmissionController -> EvaluationManager : startEvaluation()
        evaluationManager.startEvaluation(submissionId);

        // ── Step 6: Score submission loop ────────────────────────────────
        // Diagram: loop [each reviewer]
        //          Reviewer -> EvaluationManager : submitScore(score)
        //          EvaluationManager -> Database : saveScore(score)
        for (Reviewer reviewer : filteredReviewers) {
            reviewer.submitScore(evaluationManager);
        }

        // ── Step 7: Evaluation self-calls ───────────────────────────────
        // Diagram: EvaluationManager -> EvaluationManager : calculateAverage()
        evaluationManager.calculateAverage();

        // Diagram: EvaluationManager -> EvaluationManager : checkConsensus()
        evaluationManager.checkConsensus();

        // Diagram: EvaluationManager -> EvaluationManager : applyRules()
        // applyRules() also handles notification dispatch:
        //   EvaluationManager -> NotificationService : notifyX()
        //   NotificationService --> Researcher : sendNotification()
        String result = evaluationManager.applyRules();

        return "Submission " + submissionId + " processed. Outcome: " + result.toUpperCase();
    }
}
