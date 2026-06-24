package com.isrs.original;

/**
 * Represents an individual reviewer who can be assigned to review submissions
 * and submit evaluation scores.
 *
 * Diagram participant: Reviewer
 * Diagram interactions:
 *   SubmissionController -> Reviewer : assignReview()
 *   Reviewer -> EvaluationManager : submitScore(score)
 */
public class Reviewer {

    private String reviewerId;
    private String name;
    private String expertise;
    private int currentWorkload;
    private boolean hasConflict;
    private String assignedSubmissionId;

    public Reviewer(String reviewerId, String name, String expertise,
                    int currentWorkload, boolean hasConflict) {
        this.reviewerId = reviewerId;
        this.name = name;
        this.expertise = expertise;
        this.currentWorkload = currentWorkload;
        this.hasConflict = hasConflict;
    }

    /**
     * Assigns this reviewer to review a specific submission.
     * Diagram: SubmissionController -> Reviewer : assignReview()
     */
    public void assignReview(String submissionId) {
        CallCounter.count("Reviewer.assignReview");
        this.assignedSubmissionId = submissionId;
        this.currentWorkload++;
        System.out.println("[Reviewer] " + name + " assigned to review submission: " + submissionId);
    }

    /**
     * Generates and submits a score to the EvaluationManager.
     * Diagram: Reviewer -> EvaluationManager : submitScore(score)
     */
    public void submitScore(EvaluationManager evaluationManager) {
        CallCounter.count("Reviewer.submitScore");
        // Generate a deterministic score based on reviewer ID for reproducibility
        double score = 7.0 + (reviewerId.charAt(reviewerId.length() - 1) - '0') * 0.5;
        score = Math.min(score, 10.0);
        Score s = new Score(reviewerId, assignedSubmissionId, score);
        System.out.println("[Reviewer] " + name + " submitting score: " + String.format("%.1f", score));
        evaluationManager.submitScore(s);
    }

    public String getReviewerId()       { return reviewerId; }
    public String getName()             { return name; }
    public String getExpertise()        { return expertise; }
    public int getCurrentWorkload()     { return currentWorkload; }
    public boolean hasConflict()        { return hasConflict; }
    public String getAssignedSubmissionId() { return assignedSubmissionId; }

    @Override
    public String toString() {
        return "Reviewer{id='" + reviewerId + "', name='" + name + "'}";
    }
}
