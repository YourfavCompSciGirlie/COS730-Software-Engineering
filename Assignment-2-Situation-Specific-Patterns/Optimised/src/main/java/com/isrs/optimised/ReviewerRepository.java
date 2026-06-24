package com.isrs.optimised;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository responsible only for reviewer data access.
 * Replaces the monolithic Database class (single responsibility).
 */
public class ReviewerRepository {

    private List<Reviewer> reviewers = new ArrayList<>();

    public void add(Reviewer reviewer) {
        reviewers.add(reviewer);
    }

    public List<Reviewer> findAll() {
        CallCounter.count("ReviewerRepository.findAll");
        System.out.println("[ReviewerRepository] Fetching reviewers (" + reviewers.size() + " found)");
        return new ArrayList<>(reviewers);
    }
}
