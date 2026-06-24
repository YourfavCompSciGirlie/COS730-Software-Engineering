package com.isrs.optimised;

import java.util.List;

/**
 * Centralised decision engine implementing the decision table from Task 3.
 * Replaces the scattered self-calls (calculateAverage, checkConsensus, applyRules)
 * that were embedded in the baseline's EvaluationManager.
 *
 * Decision Table:
 * ┌──────┬───────────┬───────────────────────┬────────────┬──────────────────┬─────────────────────┐
 * │ Rule │ Valid?    │ Avg ≥ Accept Thresh?  │ Consensus? │ Avg < Reject?    │ Outcome             │
 * ├──────┼───────────┼───────────────────────┼────────────┼──────────────────┼─────────────────────┤
 * │ R1   │ N         │ —                     │ —          │ —                │ REJECTED (invalid)  │
 * │ R2   │ Y         │ Y                     │ Y          │ N                │ ACCEPTED            │
 * │ R3   │ Y         │ —                     │ —          │ Y                │ REJECTED            │
 * │ R4   │ Y         │ Y                     │ N          │ N                │ REVISION_REQUIRED   │
 * │ R5   │ Y         │ N                     │ —          │ N                │ REVISION_REQUIRED   │
 * └──────┴───────────┴───────────────────────┴────────────┴──────────────────┴─────────────────────┘
 */
public class DecisionEngine {

    private static final double ACCEPT_THRESHOLD = 7.0;
    private static final double REJECT_THRESHOLD = 5.0;
    private static final double CONSENSUS_RANGE  = 3.0;

    /**
     * Evaluates a list of scores and returns a single EvaluationOutcome.
     * All decision logic is centralised here — no scattered conditionals.
     */
    public EvaluationOutcome evaluate(List<Score> scores) {
        CallCounter.count("DecisionEngine.evaluate");
        System.out.println("[DecisionEngine] Evaluating " + scores.size() + " scores...");

        double average = scores.stream().mapToDouble(Score::getScore).average().orElse(0.0);
        double min = scores.stream().mapToDouble(Score::getScore).min().orElse(0.0);
        double max = scores.stream().mapToDouble(Score::getScore).max().orElse(0.0);
        boolean consensus = (max - min) <= CONSENSUS_RANGE;

        System.out.println("[DecisionEngine] Average: " + String.format("%.2f", average)
                + " | Consensus: " + (consensus ? "YES" : "NO")
                + " (range: " + String.format("%.1f", max - min) + ")");

        // Decision table evaluation
        EvaluationOutcome outcome;
        if (average < REJECT_THRESHOLD) {
            outcome = EvaluationOutcome.REJECTED;           // R3
        } else if (average >= ACCEPT_THRESHOLD && consensus) {
            outcome = EvaluationOutcome.ACCEPTED;           // R2
        } else {
            outcome = EvaluationOutcome.REVISION_REQUIRED;  // R4, R5
        }

        System.out.println("[DecisionEngine] Outcome: " + outcome);
        return outcome;
    }
}
