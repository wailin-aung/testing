package model;

import java.time.LocalDateTime;

public class Report {
    private int id;
    private String itemType; // "INGREDIENT" or "STOCK"
    private int itemId;
    private String itemName;
    private String action; // "ADD", "UPDATE", "DELETE"
    private String oldValue;
    private String newValue;
    private LocalDateTime actionDate;
    private String description;

    public Report() {}

    public Report(String itemType, int itemId, String itemName, String action, 
                  String oldValue, String newValue, String description) {
        this.itemType = itemType;
        this.itemId = itemId;
        this.itemName = itemName;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.description = description;
        this.actionDate = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public LocalDateTime getActionDate() { return actionDate; }
    public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return actionDate.toLocalDate() + " - " + action + " " + itemType + ": " + itemName;
    }
}