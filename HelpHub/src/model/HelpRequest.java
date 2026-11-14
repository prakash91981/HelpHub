package model;

import java.time.LocalDate;

public abstract class HelpRequest {
    private final int id;
    private final int requesterId;
    private final String description;
    private final Category category;
    private final Priority priority;
    private final LocalDate requiredBy;
    private final String area;
    private final int quantity;
    private RequestStatus status;
    private Integer volunteerId;

    protected HelpRequest(int id, int requesterId, String description, Category category,
                          Priority priority, LocalDate requiredBy, String area, int quantity) {
        if (description == null || description.trim().isEmpty())
            throw new IllegalArgumentException("Description cannot be empty.");
        if (requiredBy == null || requiredBy.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Required-by date cannot be in the past.");
        if (area == null || area.trim().isEmpty())
            throw new IllegalArgumentException("Area cannot be empty.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        this.id = id;
        this.requesterId = requesterId;
        this.description = description.trim();
        this.category = category;
        this.priority = priority;
        this.requiredBy = requiredBy;
        this.area = area.trim();
        this.quantity = quantity;
        this.status = RequestStatus.OPEN;
    }

    public int getId() { return id; }
    public int getRequesterId() { return requesterId; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public Priority getPriority() { return priority; }
    public LocalDate getRequiredBy() { return requiredBy; }
    public String getArea() { return area; }
    public int getQuantity() { return quantity; }
    public synchronized RequestStatus getStatus() { return status; }
    public synchronized Integer getVolunteerId() { return volunteerId; }

    // The check and update happen together under the object's monitor.
    public synchronized boolean claim(int volunteerId) {
        if (status != RequestStatus.OPEN) return false;
        this.volunteerId = volunteerId;
        status = RequestStatus.CLAIMED;
        return true;
    }

    public synchronized boolean complete() {
        if (status != RequestStatus.CLAIMED) return false;
        status = RequestStatus.COMPLETED;
        return true;
    }

    public synchronized boolean cancel() {
        if (status == RequestStatus.COMPLETED || status == RequestStatus.CANCELLED) return false;
        status = RequestStatus.CANCELLED;
        return true;
    }

    public abstract String getSpecificDetails();

    public String toCsv() {
        return id + "," + requesterId + "," + category + "," + priority + "," +
                requiredBy + "," + quantity + "," + status + "," +
                (volunteerId == null ? "" : volunteerId) + "," +
                clean(area) + "," + clean(description) + "," + clean(getSpecificDetails());
    }

    private String clean(String value) {
        return value.replace(",", " ").replace("\n", " ").replace("\r", " ");
    }

    @Override
    public String toString() {
        return "Request #" + id + " | " + category + " | " + priority +
                " | status=" + getStatus() + " | required by=" + requiredBy +
                " | area=" + area + " | quantity=" + quantity +
                " | description=" + description +
                (getVolunteerId() == null ? "" : " | volunteerId=" + getVolunteerId()) +
                " | details=" + getSpecificDetails();
    }
}
