package model;

import java.time.LocalDate;

public class StationeryRequest extends HelpRequest {
    private final String itemType;

    public StationeryRequest(int id, int requesterId, String description, Priority priority,
                             LocalDate requiredBy, String area, int quantity, String itemType) {
        super(id, requesterId, description, Category.STATIONERY, priority, requiredBy, area, quantity);
        this.itemType = itemType == null ? "General stationery" : itemType.trim();
    }

    @Override public String getSpecificDetails() { return "itemType=" + itemType; }
}
