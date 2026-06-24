package com.isrs.optimised;

import java.util.List;

/**
 * Manages the evaluation workflow: persists scores and delegates
 * outcome determination to the DecisionEngine.
 *
 * Improvements over baseline:
 * - Does not contain decision logic (delegated to DecisionEngine)
 * - Uses ScoreRepository instead of monolithic Database
 * - Batch-saves scores instead of one-at-a-time persistence
 * - Does not trigger notifications (separation of concerns)
 */
public class EvaluationService {

    private ScoreRepository scoreRepository;
    private DecisionEngine decisionEngine;

    public EvaluationService(ScoreRepository scoreRepository, DecisionEngine decisionEngine) {
        this.scoreRepository = scoreRepository;
        this.decisionEngine = decisionEngine;
    }

    /**
     * Evaluates a submission: persists all scores in a batch, then delegates
     * to the DecisionEngine to determine the outcome.
     */
    public EvaluationOutcome evaluate(String submissionId, List<Score> scores) {
        CallCounter.count("EvaluationService.evaluate");
        System.out.println("[EvaluationService] Evaluating submission: " + submissionId);

        // Batch-save all scores at once (replaces per-score save loop)
        scoreRepository.saveAll(scores);

        // Delegate decision to DecisionEngine
        return decisionEngine.evaluate(scores);
    }
}
