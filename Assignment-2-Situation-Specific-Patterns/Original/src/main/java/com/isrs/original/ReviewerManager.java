package com.isrs.original;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the reviewer pool: fetches reviewers from the database,
 * filters out those with conflicts, and removes overloaded reviewers.
 *
 * Diagram participant: ReviewerManager
 * Diagram interactions:
 *   SubmissionController -> ReviewerManager : getAvailableReviewers()
 *   ReviewerManager -> Database : fetchReviewers()
 *   ReviewerManager -> ReviewerManager : filterConflicts(reviewerList)
 *   ReviewerManager -> ReviewerManager : checkWorkload(reviewerList)
 */
public class ReviewerManager {

    private Database database;

    public ReviewerManager(Database database) {
        this.database = database;
    }

    /**
     * Retrieves available reviewers by fetching all reviewers from the database,
     * then applying conflict and workload filters.
     * Diagram: SubmissionController -> ReviewerManager : getAvailableReviewers()
     */
    public List<Reviewer> getAvailableReviewers() {
        CallCounter.count("ReviewerManager.getAvailableReviewers");
        System.out.println("[ReviewerManager] Getting available reviewers...");

        // Diagram: ReviewerManager -> Database : fetchReviewers()
        List<Reviewer> reviewerList = database.fetchReviewers();

        // Diagram: ReviewerManager -> ReviewerManager : filterConflicts(reviewerList)
        reviewerList = filterConflicts(reviewerList);

        // Diagram: ReviewerManager -> ReviewerManager : checkWorkload(reviewerList)
        reviewerList = checkWorkload(reviewerList);

        System.out.println("[ReviewerManager] Returning " + reviewerList.size() + " eligible reviewers");
        return reviewerList;
    }

    /**
     * Self-call: removes reviewers who have a conflict of interest.
     * Diagram: ReviewerManager -> ReviewerManager : filterConflicts(reviewerList)
     */
    public List<Reviewer> filterConflicts(List<Reviewer> reviewerList) {
        CallCounter.count("ReviewerManager.filterConflicts");
        System.out.println("[ReviewerManager] Filtering conflicts...");
        List<Reviewer> filtered = reviewerList.stream()
                .filter(r -> !r.hasConflict())
                .collect(Collectors.toList());
        int removed = reviewerList.size() - filtered.size();
        if (removed > 0) {
            System.out.println("[ReviewerManager] Removed " + removed + " reviewer(s) with conflicts");
        }
        return filtered;
    }

    /**
     * Self-call: removes reviewers whose current workload exceeds the threshold.
     * Diagram: ReviewerManager -> ReviewerManager : checkWorkload(reviewerList)
     */
    public List<Reviewer> checkWorkload(List<Reviewer> reviewerList) {
        CallCounter.count("ReviewerManager.checkWorkload");
        System.out.println("[ReviewerManager] Checking workload (max threshold: 5)...");
        List<Reviewer> filtered = reviewerList.stream()
                .filter(r -> r.getCurrentWorkload() < 5)
                .collect(Collectors.toList());
        int removed = reviewerList.size() - filtered.size();
        if (removed > 0) {
            System.out.println("[ReviewerManager] Removed " + removed + " overloaded reviewer(s)");
        }
        return filtered;
    }
}
