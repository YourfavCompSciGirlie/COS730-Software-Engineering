package com.isrs.optimised;

/**
 * Handles sending notifications to the researcher.
 *
 * Improvement over baseline: single parameterised method replaces
 * three separate methods (notifyAcceptance, notifyRejection, notifyRevision).
 */
public class NotificationService {

    /**
     * Sends a notification based on the evaluation outcome.
     * Replaces the baseline's three separate notifyX() methods.
     */
    public void notify(EvaluationOutcome outcome) {
        CallCounter.count("NotificationService.notify");
        System.out.println("[NotificationService] Sending " + outcome + " notification to researcher");
    }
}
