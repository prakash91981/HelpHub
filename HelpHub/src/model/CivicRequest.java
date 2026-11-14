package model;

import java.time.LocalDate;

public class CivicRequest extends HelpRequest {
    private final String issueType;

    public CivicRequest(int id, int requesterId, String description, Priority priority,
                        LocalDate requiredBy, String area, int quantity, String issueType) {
        super(id, requesterId, description, Category.CIVIC, priority, requiredBy, area, quantity);
        this.issueType = issueType == null ? "General non-emergency issue" : issueType.trim();
    }

    @Override public String getSpecificDetails() { return "issueType=" + issueType; }
}
