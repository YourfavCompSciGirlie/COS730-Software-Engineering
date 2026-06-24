package com.isrs.optimised;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles reviewer selection and assignment — extracted from the baseline's
 * SubmissionController (which did reviewer assignment) and ReviewerManager.
 *
 * Improvements over baseline:
 * - Owns the full reviewer assignment workflow (cohesion)
 * - Uses ReviewerRepository instead of monolithic Database (decoupling)
 * - Returns scores directly instead of having Reviewer call EvaluationManager
 */
public class ReviewAssignmentService {

    private ReviewerRepository reviewerRepository;

    public ReviewAssignmentService(ReviewerRepository reviewerRepository) {
        this.reviewerRepository = reviewerRepository;
    }

    /**
     * Finds eligible reviewers, assigns them, and collects their scores.
     * Combines the baseline's getAvailableReviewers(), assignReview loop,
     * and submitScore loop into one cohesive operation.
     */
    public List<Score> assignAndCollectScores(String submissionId) {
        CallCounter.count("ReviewAssignmentService.assignAndCollectScores");
        System.out.println("[ReviewAssignmentService] Assigning reviewers for: " + submissionId);

        // Fetch all reviewers from repository
        List<Reviewer> candidates = reviewerRepository.findAll();

        // Filter: remove conflicts
        List<Reviewer> eligible = candidates.stream()
                .filter(r -> !r.hasConflict())
                .collect(Collectors.toList());
        int conflictRemoved = candidates.size() - eligible.size();
        if (conflictRemoved > 0) {
            System.out.println("[ReviewAssignmentService] Removed " + conflictRemoved + " reviewer(s) with conflicts");
        }

        // Filter: remove overloaded
        List<Reviewer> available = eligible.stream()
                .filter(r -> r.getCurrentWorkload() < 5)
                .collect(Collectors.toList());
        int overloadRemoved = eligible.size() - available.size();
        if (overloadRemoved > 0) {
            System.out.println("[ReviewAssignmentService] Removed " + overloadRemoved + " overloaded reviewer(s)");
        }

        System.out.println("[ReviewAssignmentService] " + available.size() + " reviewers assigned");

        // Assign and collect scores
        List<Score> scores = new ArrayList<>();
        for (Reviewer reviewer : available) {
            reviewer.incrementWorkload();
            double scoreValue = reviewer.generateScore();
            Score score = new Score(reviewer.getReviewerId(), submissionId, scoreValue);
            scores.add(score);
            System.out.println("[ReviewAssignmentService] " + reviewer.getName()
                    + " scored: " + String.format("%.1f", scoreValue));
        }

        return scores;
    }
}
