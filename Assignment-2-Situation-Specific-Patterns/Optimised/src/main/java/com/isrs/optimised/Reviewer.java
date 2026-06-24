package com.isrs.optimised;

/**
 * Represents an individual reviewer.
 * Unlike the baseline, reviewers do NOT directly call EvaluationService.
 * Score generation is handled by the ReviewAssignmentService.
 */
public class Reviewer {

    private String reviewerId;
    private String name;
    private String expertise;
    private int currentWorkload;
    private boolean hasConflict;

    public Reviewer(String reviewerId, String name, String expertise,
                    int currentWorkload, boolean hasConflict) {
        this.reviewerId = reviewerId;
        this.name = name;
        this.expertise = expertise;
        this.currentWorkload = currentWorkload;
        this.hasConflict = hasConflict;
    }

    /** Generates a deterministic score (same formula as baseline for equivalence). */
    public double generateScore() {
        CallCounter.count("Reviewer.generateScore");
        double score = 7.0 + (reviewerId.charAt(reviewerId.length() - 1) - '0') * 0.5;
        return Math.min(score, 10.0);
    }

    public void incrementWorkload() { this.currentWorkload++; }

    public String getReviewerId()   { return reviewerId; }
    public String getName()         { return name; }
    public String getExpertise()    { return expertise; }
    public int getCurrentWorkload() { return currentWorkload; }
    public boolean hasConflict()    { return hasConflict; }

    @Override
    public String toString() {
        return "Reviewer{id='" + reviewerId + "', name='" + name + "'}";
    }
}
