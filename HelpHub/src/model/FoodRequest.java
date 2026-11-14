package model;

import java.time.LocalDate;

public class FoodRequest extends HelpRequest {
    private final String expiryOrCollectionNote;

    public FoodRequest(int id, int requesterId, String description, Priority priority,
                       LocalDate requiredBy, String area, int quantity, String expiryOrCollectionNote) {
        super(id, requesterId, description, Category.FOOD, priority, requiredBy, area, quantity);
        this.expiryOrCollectionNote = expiryOrCollectionNote == null ? "Confirm safe collection" : expiryOrCollectionNote.trim();
    }

    @Override public String getSpecificDetails() { return "foodSafetyNote=" + expiryOrCollectionNote; }
}
