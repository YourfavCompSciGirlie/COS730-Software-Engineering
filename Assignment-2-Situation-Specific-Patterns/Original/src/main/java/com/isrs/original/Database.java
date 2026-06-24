package com.isrs.original;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Monolithic database class that handles ALL persistence concerns:
 * submissions, reviewers, and scores.
 *
 * Design flaw: Single class with three unrelated responsibilities (God Object).
 *
 * Diagram participant: Database
 * Diagram interactions:
 *   SubmissionController -> Database : saveSubmission(data)
 *   ReviewerManager -> Database : fetchReviewers()
 *   EvaluationManager -> Database : saveScore(score)
 */
public class Database {

    private Map<String, ResearchOutput> submissions = new HashMap<>();
    private List<Reviewer> reviewers = new ArrayList<>();
    private List<Score> scores = new ArrayList<>();

    /**
     * Persists a submission and returns a generated submission ID.
     * Diagram: SubmissionController -> Database : saveSubmission(data)
     */
    public String saveSubmission(ResearchOutput data) {
        CallCounter.count("Database.saveSubmission");
        String submissionId = "SUB-" + UUID.randomUUID().toString().substring(0, 8);
        submissions.put(submissionId, data);
        System.out.println("[Database] Submission saved with ID: " + submissionId);
        return submissionId;
    }

    /**
     * Retrieves all reviewers from the data store.
     * Diagram: ReviewerManager -> Database : fetchReviewers()
     */
    public List<Reviewer> fetchReviewers() {
        CallCounter.count("Database.fetchReviewers");
        System.out.println("[Database] Fetching all reviewers... (" + reviewers.size() + " found)");
        return new ArrayList<>(reviewers);
    }

    /**
     * Persists a single review score.
     * Diagram: EvaluationManager -> Database : saveScore(score)
     */
    public void saveScore(Score score) {
        CallCounter.count("Database.saveScore");
        scores.add(score);
        System.out.println("[Database] Score saved: " + score);
    }

    /** Pre-populates a reviewer into the data store. */
    public void addReviewer(Reviewer reviewer) {
        reviewers.add(reviewer);
    }

    public Map<String, ResearchOutput> getSubmissions() { return submissions; }
    public List<Score> getScores() { return scores; }
}
