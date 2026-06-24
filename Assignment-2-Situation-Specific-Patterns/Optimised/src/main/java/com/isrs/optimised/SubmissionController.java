package com.isrs.optimised;

import java.util.List;

/**
 * Slim controller that handles only submission validation and persistence,
 * delegating reviewer assignment to ReviewAssignmentService,
 * evaluation to EvaluationService, and notification to NotificationService.
 *
 * Improvements over baseline:
 * - Depends on 5 focused services instead of 4 mixed-responsibility classes
 * - Does not handle reviewer assignment or evaluation logic directly
 * - Each dependency has a clear, single responsibility
 * - No scattered decision logic
 */
public class SubmissionController {

    private Validator validator;
    private SubmissionRepository submissionRepository;
    private ReviewAssignmentService reviewAssignmentService;
    private EvaluationService evaluationService;
    private NotificationService notificationService;

    public SubmissionController(Validator validator,
                                SubmissionRepository submissionRepository,
                                ReviewAssignmentService reviewAssignmentService,
                                EvaluationService evaluationService,
                                NotificationService notificationService) {
        this.validator = validator;
        this.submissionRepository = submissionRepository;
        this.reviewAssignmentService = reviewAssignmentService;
        this.evaluationService = evaluationService;
        this.notificationService = notificationService;
    }

    /**
     * Processes a submission through the optimised pipeline:
     * validate → persist → assign reviewers & collect scores → evaluate → notify.
     */
    public String submit(ResearchOutput data) {
        CallCounter.count("SubmissionController.submit");
        System.out.println("\n[SubmissionController] Processing submission...");

        // Step 1: Validate
        boolean isValid = validator.validateFormat(data);
        if (!isValid) {
            System.out.println("[SubmissionController] Validation failed.");
            return "ERROR: Invalid submission format";
        }

        // Step 2: Persist submission
        String submissionId = submissionRepository.save(data);

        // Step 3: Assign reviewers and collect scores (delegated)
        List<Score> scores = reviewAssignmentService.assignAndCollectScores(submissionId);

        // Step 4: Evaluate (delegated to EvaluationService + DecisionEngine)
        EvaluationOutcome outcome = evaluationService.evaluate(submissionId, scores);

        // Step 5: Notify (single parameterised call)
        notificationService.notify(outcome);

        return "Submission " + submissionId + " processed. Outcome: " + outcome;
    }
}
