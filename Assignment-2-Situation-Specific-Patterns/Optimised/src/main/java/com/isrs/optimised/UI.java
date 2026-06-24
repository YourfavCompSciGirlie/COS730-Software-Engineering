package com.isrs.optimised;

/**
 * Boundary class representing the user interface.
 */
public class UI {

    private SubmissionController controller;

    public UI(SubmissionController controller) {
        this.controller = controller;
    }

    public String submitResearchOutput(ResearchOutput data) {
        CallCounter.count("UI.submitResearchOutput");
        System.out.println("[UI] Research output received: " + data.getTitle());
        String result = controller.submit(data);
        System.out.println("[UI] Returning result to researcher: " + result);
        return result;
    }
}
