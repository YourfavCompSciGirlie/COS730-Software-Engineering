package com.isrs.original;

/**
 * Handles sending notifications to the researcher based on the evaluation outcome.
 *
 * Design flaw: Three separate notification methods (notifyAcceptance, notifyRejection,
 * notifyRevision) instead of a single parameterised method.
 *
 * Diagram participant: NotificationService
 * Diagram interactions:
 *   EvaluationManager -> NotificationService : notifyAcceptance()
 *   EvaluationManager -> NotificationService : notifyRejection()
 *   EvaluationManager -> NotificationService : notifyRevision()
 *   NotificationService --> Researcher : sendNotification()
 */
public class NotificationService {

    /**
     * Prepares an acceptance notification.
     * Diagram: EvaluationManager -> NotificationService : notifyAcceptance()
     */
    public void notifyAcceptance() {
        CallCounter.count("NotificationService.notifyAcceptance");
        System.out.println("[NotificationService] Preparing ACCEPTANCE notification");
    }

    /**
     * Prepares a rejection notification.
     * Diagram: EvaluationManager -> NotificationService : notifyRejection()
     */
    public void notifyRejection() {
        CallCounter.count("NotificationService.notifyRejection");
        System.out.println("[NotificationService] Preparing REJECTION notification");
    }

    /**
     * Prepares a revision-required notification.
     * Diagram: EvaluationManager -> NotificationService : notifyRevision()
     */
    public void notifyRevision() {
        CallCounter.count("NotificationService.notifyRevision");
        System.out.println("[NotificationService] Preparing REVISION REQUIRED notification");
    }

    /**
     * Sends the prepared notification to the researcher.
     * Diagram: NotificationService --> Researcher : sendNotification()
     */
    public void sendNotification() {
        CallCounter.count("NotificationService.sendNotification");
        System.out.println("[NotificationService] Notification sent to researcher");
    }
}
