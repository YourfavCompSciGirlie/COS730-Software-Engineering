package com.isrs.optimised;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * Benchmarking harness for the optimised ISRS implementation.
 * Measures execution time and method call counts.
 */
public class Benchmark {

    private static final int WARMUP_RUNS = 100;
    private static final int BENCHMARK_RUNS = 1000;

    public static void main(String[] args) {
        System.out.println("=== OPTIMISED BENCHMARK ===\n");

        // --- Warm-up phase (JIT compilation) ---
        PrintStream realOut = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));

        for (int i = 0; i < WARMUP_RUNS; i++) {
            runScenario();
        }

        System.setOut(realOut);
        System.out.println("Warm-up: " + WARMUP_RUNS + " runs completed\n");

        // --- Timing benchmark ---
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));

        long[] times = new long[BENCHMARK_RUNS];
        for (int i = 0; i < BENCHMARK_RUNS; i++) {
            long start = System.nanoTime();
            runScenario();
            times[i] = System.nanoTime() - start;
        }

        System.setOut(realOut);

        // Calculate statistics
        long sum = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        for (long t : times) {
            sum += t;
            min = Math.min(min, t);
            max = Math.max(max, t);
        }
        double mean = (double) sum / BENCHMARK_RUNS;

        double varianceSum = 0;
        for (long t : times) {
            varianceSum += Math.pow(t - mean, 2);
        }
        double stddev = Math.sqrt(varianceSum / BENCHMARK_RUNS);

        // Sort for median
        java.util.Arrays.sort(times);
        double median = (times[BENCHMARK_RUNS / 2] + times[(BENCHMARK_RUNS - 1) / 2]) / 2.0;

        System.out.println("Timing (" + BENCHMARK_RUNS + " runs):");
        System.out.printf("  Mean:   %,.0f ns (%.3f ms)%n", mean, mean / 1_000_000);
        System.out.printf("  Median: %,.0f ns (%.3f ms)%n", median, median / 1_000_000);
        System.out.printf("  StdDev: %,.0f ns%n", stddev);
        System.out.printf("  Min:    %,.0f ns%n", (double) min);
        System.out.printf("  Max:    %,.0f ns%n", (double) max);

        // --- Method call count ---
        System.out.println();
        CallCounter.reset();
        CallCounter.enable();

        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        runScenario();
        System.setOut(realOut);

        CallCounter.disable();

        System.out.println("Method Call Counts (single valid submission):");
        Map<String, Integer> counts = CallCounter.getCounts();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            System.out.printf("  %-45s %d%n", entry.getKey(), entry.getValue());
        }
        System.out.println("  ─────────────────────────────────────────────────");
        System.out.printf("  %-45s %d%n", "TOTAL", CallCounter.getTotal());

        System.out.println("\n=== OPTIMISED BENCHMARK COMPLETE ===");
    }

    private static void runScenario() {
        SubmissionRepository submissionRepo = new SubmissionRepository();
        ReviewerRepository reviewerRepo = new ReviewerRepository();
        ScoreRepository scoreRepo = new ScoreRepository();

        reviewerRepo.add(new Reviewer("R001", "Dr. Smith", "AI", 2, false));
        reviewerRepo.add(new Reviewer("R002", "Dr. Johnson", "ML", 1, false));
        reviewerRepo.add(new Reviewer("R003", "Dr. Williams", "DS", 4, false));
        reviewerRepo.add(new Reviewer("R004", "Dr. Brown", "NLP", 0, true));
        reviewerRepo.add(new Reviewer("R005", "Dr. Davis", "CV", 6, false));

        Validator validator = new Validator();
        ReviewAssignmentService reviewAssignment = new ReviewAssignmentService(reviewerRepo);
        DecisionEngine decisionEngine = new DecisionEngine();
        EvaluationService evaluationService = new EvaluationService(scoreRepo, decisionEngine);
        NotificationService notificationService = new NotificationService();

        SubmissionController controller = new SubmissionController(
                validator, submissionRepo, reviewAssignment, evaluationService, notificationService);
        UI ui = new UI(controller);

        ResearchOutput submission = new ResearchOutput(
                "Deep Learning for Climate Prediction",
                "This paper proposes a novel deep learning approach...",
                List.of("A. Researcher", "B. Collaborator"),
                "Artificial Intelligence",
                "researcher@university.ac.za"
        );
        ui.submitResearchOutput(submission);
    }
}
