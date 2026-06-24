package com.isrs.optimised;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Repository responsible only for submission persistence.
 * Replaces the monolithic Database class (single responsibility).
 */
public class SubmissionRepository {

    private Map<String, ResearchOutput> submissions = new HashMap<>();

    public String save(ResearchOutput data) {
        CallCounter.count("SubmissionRepository.save");
        String id = "SUB-" + UUID.randomUUID().toString().substring(0, 8);
        submissions.put(id, data);
        System.out.println("[SubmissionRepository] Saved submission: " + id);
        return id;
    }

    public ResearchOutput findById(String id) {
        return submissions.get(id);
    }
}
