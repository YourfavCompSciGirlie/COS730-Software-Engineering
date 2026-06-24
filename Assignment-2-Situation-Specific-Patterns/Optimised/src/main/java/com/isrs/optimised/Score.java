package com.isrs.optimised;

/**
 * Domain model representing a score submitted by a reviewer.
 */
public class Score {

    private String reviewerId;
    private String submissionId;
    private double score;

    public Score(String reviewerId, String submissionId, double score) {
        this.reviewerId = reviewerId;
        this.submissionId = submissionId;
        this.score = score;
    }

    public String getReviewerId()   { return reviewerId; }
    public String getSubmissionId() { return submissionId; }
    public double getScore()        { return score; }

    @Override
    public String toString() {
        return "Score{reviewer='" + reviewerId + "', score=" + String.format("%.1f", score) + "}";
    }
}
