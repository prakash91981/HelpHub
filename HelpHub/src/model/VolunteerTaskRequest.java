package model;

import java.time.LocalDate;

public class VolunteerTaskRequest extends HelpRequest {
    private final String taskType;

    public VolunteerTaskRequest(int id, int requesterId, String description, Priority priority,
                                LocalDate requiredBy, String area, int quantity, String taskType) {
        super(id, requesterId, description, Category.VOLUNTEER, priority, requiredBy, area, quantity);
        this.taskType = taskType == null ? "General task" : taskType.trim();
    }

    @Override public String getSpecificDetails() { return "taskType=" + taskType; }
}
