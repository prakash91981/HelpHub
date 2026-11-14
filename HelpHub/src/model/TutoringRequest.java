package model;

import java.time.LocalDate;

public class TutoringRequest extends HelpRequest {
    private final String subject;

    public TutoringRequest(int id, int requesterId, String description, Priority priority,
                           LocalDate requiredBy, String area, int quantity, String subject) {
        super(id, requesterId, description, Category.TUTORING, priority, requiredBy, area, quantity);
        this.subject = subject == null ? "General" : subject.trim();
    }

    @Override public String getSpecificDetails() { return "subject=" + subject; }
}
