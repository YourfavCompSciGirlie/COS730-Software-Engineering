package com.isrs.optimised;

/**
 * Enum representing the possible outcomes of a submission evaluation.
 * Replaces scattered string-based decision logic from the baseline.
 */
public enum EvaluationOutcome {
    ACCEPTED,
    REJECTED,
    REVISION_REQUIRED
}
