package com.isrs.original;

/**
 * Boundary class representing the user interface.
 * Receives the researcher's submission and delegates to the SubmissionController.
 *
 * Diagram participant: UI
 * Diagram interactions:
 *   Researcher -> UI : submitResearchOutput(data)
 *   UI -> SubmissionController : submit(data)
 */
public class UI {

    private SubmissionController controller;

    public UI(SubmissionController controller) {
        this.controller = controller;
    }

    /**
     * Entry point for the researcher submitting a research output.
     * Diagram: Researcher -> UI : submitResearchOutput(data)
     *          UI -> SubmissionController : submit(data)
     */
    public String submitResearchOutput(ResearchOutput data) {
        CallCounter.count("UI.submitResearchOutput");
        System.out.println("[UI] Research output received: " + data.getTitle());
        // Diagram: UI -> SubmissionController : submit(data)
        String result = controller.submit(data);
        System.out.println("[UI] Returning result to researcher: " + result);
        return result;
    }
}
