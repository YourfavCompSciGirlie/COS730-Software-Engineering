package com.isrs.original;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the evaluation process: receives scores from reviewers,
 * calculates the average, checks consensus, applies rules to determine
 * the outcome, and triggers notifications.
 *
 * Design flaw: Combines scoring, consensus, rule evaluation, and notification
 * dispatch — multiple responsibilities in a single class.
 *
 * Diagram participant: EvaluationManager
 * Diagram interactions:
 *   SubmissionController -> EvaluationManager : startEvaluation()
 *   Reviewer -> EvaluationManager : submitScore(score)
 *   EvaluationManager -> Database : saveScore(score)
 *   EvaluationManager -> EvaluationManager : calculateAverage()
 *   EvaluationManager -> EvaluationManager : checkConsensus()
 *   EvaluationManager -> EvaluationManager : applyRules()
 *   EvaluationManager -> NotificationService : notifyAcceptance/Rejection/Revision()
 */
public class EvaluationManager {

    private Database database;
    private NotificationService notificationService;
    private List<Score> scores = new ArrayList<>();
    private String submissionId;
    private double averageScore;
    private boolean consensusReached;

    public EvaluationManager(Database database, NotificationService notificationService) {
        this.database = database;
        this.notificationService = notificationService;
    }

    /**
     * Initialises the evaluation for a given submission.
     * Diagram: SubmissionController -> EvaluationManager : startEvaluation()
     */
    public void startEvaluation(String submissionId) {
        CallCounter.count("EvaluationManager.startEvaluation");
        this.submissionId = submissionId;
        this.scores.clear();
        this.averageScore = 0.0;
        this.consensusReached = false;
        System.out.println("[EvaluationManager] Evaluation started for submission: " + submissionId);
    }

    /**
     * Receives a score from a reviewer and persists it to the database.
     * Diagram: Reviewer -> EvaluationManager : submitScore(score)
     *          EvaluationManager -> Database : saveScore(score)
     */
    public void submitScore(Score score) {
        CallCounter.count("EvaluationManager.submitScore");
        System.out.println("[EvaluationManager] Received score from reviewer " + score.getReviewerId());
        scores.add(score);
        // Diagram: EvaluationManager -> Database : saveScore(score)
        database.saveScore(score);
    }

    /**
     * Self-call: calculates the average of all submitted scores.
     * Diagram: EvaluationManager -> EvaluationManager : calculateAverage()
     */
    public double calculateAverage() {
        CallCounter.count("EvaluationManager.calculateAverage");
        averageScore = scores.stream()
                .mapToDouble(Score::getScore)
                .average()
                .orElse(0.0);
        System.out.println("[EvaluationManager] Average score: " + String.format("%.2f", averageScore));
        return averageScore;
    }

    /**
     * Self-call: checks whether reviewers reached consensus (scores within 3.0 of each other).
     * Diagram: EvaluationManager -> EvaluationManager : checkConsensus()
     */
    public boolean checkConsensus() {
        CallCounter.count("EvaluationManager.checkConsensus");
        double min = scores.stream().mapToDouble(Score::getScore).min().orElse(0.0);
        double max = scores.stream().mapToDouble(Score::getScore).max().orElse(0.0);
        consensusReached = (max - min) <= 3.0;
        System.out.println("[EvaluationManager] Consensus check: " +
                (consensusReached ? "REACHED" : "NOT REACHED") +
                " (range: " + String.format("%.1f", max - min) + ")");
        return consensusReached;
    }

    /**
     * Self-call: applies evaluation rules to determine the outcome.
     * Uses the previously computed average and consensus values.
     * Diagram: EvaluationManager -> EvaluationManager : applyRules()
     */
    public String applyRules() {
        CallCounter.count("EvaluationManager.applyRules");
        String result;
        if (averageScore >= 7.0 && consensusReached) {
            result = "accepted";
        } else if (averageScore < 5.0) {
            result = "rejected";
        } else {
            result = "revision";
        }
        System.out.println("[EvaluationManager] Rules applied. Outcome: " + result.toUpperCase());

        // Diagram: alt [accepted/rejected/revision]
        // Diagram: EvaluationManager -> NotificationService : notifyX()
        switch (result) {
            case "accepted":
                notificationService.notifyAcceptance();
                break;
            case "rejected":
                notificationService.notifyRejection();
                break;
            default:
                notificationService.notifyRevision();
                break;
        }

        // Diagram: NotificationService --> Researcher : sendNotification()
        notificationService.sendNotification();

        return result;
    }

    public double getAverageScore()      { return averageScore; }
    public boolean isConsensusReached()   { return consensusReached; }
    public List<Score> getScores()        { return scores; }
}
