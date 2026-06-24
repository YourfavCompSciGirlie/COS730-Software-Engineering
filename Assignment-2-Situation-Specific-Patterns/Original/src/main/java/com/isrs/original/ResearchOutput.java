package com.isrs.original;

import java.util.List;

/**
 * Domain model representing a research output submitted by a researcher.
 * Carries all data through the submission pipeline.
 */
public class ResearchOutput {

    private String title;
    private String abstractText;
    private List<String> authors;
    private String field;
    private String researcherEmail;

    public ResearchOutput(String title, String abstractText, List<String> authors,
                          String field, String researcherEmail) {
        this.title = title;
        this.abstractText = abstractText;
        this.authors = authors;
        this.field = field;
        this.researcherEmail = researcherEmail;
    }

    public String getTitle()           { return title; }
    public String getAbstractText()    { return abstractText; }
    public List<String> getAuthors()   { return authors; }
    public String getField()           { return field; }
    public String getResearcherEmail() { return researcherEmail; }

    @Override
    public String toString() {
        return "ResearchOutput{title='" + title + "', field='" + field + "'}";
    }
}
