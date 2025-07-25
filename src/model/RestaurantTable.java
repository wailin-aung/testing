package model;

import javafx.beans.property.*;

public class RestaurantTable {
    private final IntegerProperty tableId;
    private final StringProperty tableNumber;
    private final IntegerProperty capacity;
    private final StringProperty status;
    private final StringProperty location;
    
    public RestaurantTable() {
        this.tableId = new SimpleIntegerProperty();
        this.tableNumber = new SimpleStringProperty();
        this.capacity = new SimpleIntegerProperty();
        this.status = new SimpleStringProperty();
        this.location = new SimpleStringProperty();
    }
    
    public RestaurantTable(int tableId, String tableNumber, int capacity, String status, String location) {
        this();
        setTableId(tableId);
        setTableNumber(tableNumber);
        setCapacity(capacity);
        setStatus(status);
        setLocation(location);
    }
    
    // Property getters
    public IntegerProperty tableIdProperty() { return tableId; }
    public StringProperty tableNumberProperty() { return tableNumber; }
    public IntegerProperty capacityProperty() { return capacity; }
    public StringProperty statusProperty() { return status; }
    public StringProperty locationProperty() { return location; }
    
    // Getters
    public int getTableId() { return tableId.get(); }
    public String getTableNumber() { return tableNumber.get(); }
    public int getCapacity() { return capacity.get(); }
    public String getStatus() { return status.get(); }
    public String getLocation() { return location.get(); }
    
    // Setters
    public void setTableId(int tableId) { this.tableId.set(tableId); }
    public void setTableNumber(String tableNumber) { this.tableNumber.set(tableNumber); }
    public void setCapacity(int capacity) { this.capacity.set(capacity); }
    public void setStatus(String status) { this.status.set(status); }
    public void setLocation(String location) { this.location.set(location); }
    
    @Override
    public String toString() {
        return getTableNumber() + " (Cap: " + getCapacity() + ")";
    }
}
