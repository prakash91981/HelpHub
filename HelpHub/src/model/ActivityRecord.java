package model;

import java.time.LocalDateTime;

public class ActivityRecord {
    private final int requestId;
    private final String action;
    private final LocalDateTime timestamp;
    private final int volunteerHours;

    public ActivityRecord(int requestId, String action, int volunteerHours) {
        this.requestId = requestId;
        this.action = action;
        this.timestamp = LocalDateTime.now();
        this.volunteerHours = Math.max(0, volunteerHours);
    }

    public int getRequestId() { return requestId; }
    public String getAction() { return action; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getVolunteerHours() { return volunteerHours; }

    public String toCsv() {
        return requestId + "," + action.replace(",", " ") + "," + timestamp + "," + volunteerHours;
    }
}
