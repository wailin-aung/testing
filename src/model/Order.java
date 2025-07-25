package model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Order {
    private final IntegerProperty orderId;
    private final IntegerProperty tableId;
    private final IntegerProperty productId;
    private final IntegerProperty qty;
    private final ObjectProperty<LocalDateTime> dateTime;
    private final StringProperty status;
    private final StringProperty paymentMethod;
    private final DoubleProperty unitPrice;
    private final DoubleProperty totalPrice;
    private final StringProperty productName;
    private final StringProperty tableNumber;

    public Order() {
        this.orderId = new SimpleIntegerProperty();
        this.tableId = new SimpleIntegerProperty();
        this.productId = new SimpleIntegerProperty();
        this.qty = new SimpleIntegerProperty();
        this.dateTime = new SimpleObjectProperty<>();
        this.status = new SimpleStringProperty();
        this.paymentMethod = new SimpleStringProperty();
        this.unitPrice = new SimpleDoubleProperty();
        this.totalPrice = new SimpleDoubleProperty();
        this.productName = new SimpleStringProperty();
        this.tableNumber = new SimpleStringProperty();
    }

    // Property getters
    public IntegerProperty orderIdProperty() { return orderId; }
    public IntegerProperty tableIdProperty() { return tableId; }
    public IntegerProperty productIdProperty() { return productId; }
    public IntegerProperty qtyProperty() { return qty; }
    public ObjectProperty<LocalDateTime> dateTimeProperty() { return dateTime; }
    public StringProperty statusProperty() { return status; }
    public StringProperty paymentMethodProperty() { return paymentMethod; }
    public DoubleProperty unitPriceProperty() { return unitPrice; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }
    public StringProperty productNameProperty() { return productName; }
    public StringProperty tableNumberProperty() { return tableNumber; }

    // Getters
    public int getOrderId() { return orderId.get(); }
    public int getTableId() { return tableId.get(); }
    public int getProductId() { return productId.get(); }
    public int getQty() { return qty.get(); }
    public LocalDateTime getDateTime() { return dateTime.get(); }
    public String getStatus() { return status.get(); }
    public String getPaymentMethod() { return paymentMethod.get(); }
    public double getUnitPrice() { return unitPrice.get(); }
    public double getTotalPrice() { return totalPrice.get(); }
    public String getProductName() { return productName.get(); }
    public String getTableNumber() { return tableNumber.get(); }

    // Setters
    public void setOrderId(int orderId) { this.orderId.set(orderId); }
    public void setTableId(int tableId) { this.tableId.set(tableId); }
    public void setProductId(int productId) { this.productId.set(productId); }
    public void setQty(int qty) {
        this.qty.set(qty);
        updateTotalPrice();
    }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime.set(dateTime); }
    public void setStatus(String status) { this.status.set(status); }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod.set(paymentMethod); }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice.set(unitPrice);
        updateTotalPrice();
    }
    public void setTotalPrice(double totalPrice) { this.totalPrice.set(totalPrice); }
    public void setProductName(String productName) { this.productName.set(productName); }
    public void setTableNumber(String tableNumber) { this.tableNumber.set(tableNumber); }

    private void updateTotalPrice() {
        setTotalPrice(getQty() * getUnitPrice());
    }
}
