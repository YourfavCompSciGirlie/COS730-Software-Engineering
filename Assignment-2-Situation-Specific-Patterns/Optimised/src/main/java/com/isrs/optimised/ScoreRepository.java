package com.isrs.optimised;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository responsible only for score persistence.
 * Replaces the monolithic Database class (single responsibility).
 */
public class ScoreRepository {

    private List<Score> scores = new ArrayList<>();

    public void save(Score score) {
        scores.add(score);
        System.out.println("[ScoreRepository] Saved score: " + score);
    }

    public void saveAll(List<Score> scoresToSave) {
        CallCounter.count("ScoreRepository.saveAll");
        scores.addAll(scoresToSave);
        System.out.println("[ScoreRepository] Batch-saved " + scoresToSave.size() + " scores");
    }

    public List<Score> findBySubmissionId(String submissionId) {
        return scores.stream()
                .filter(s -> s.getSubmissionId().equals(submissionId))
                .toList();
    }
}
