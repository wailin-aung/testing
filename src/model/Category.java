package model;

import javafx.beans.property.*;

public class Category {
	private final IntegerProperty categoryId;
    private final StringProperty categoryName;
    
//    public Category(int categoryId, String categoryName) {
//		super();
//		this.categoryId = categoryId;
//		this.categoryName = categoryName;
//	}
//
//	public int getCategoryId() {
//		return categoryId;
//	}
//
//	public String getCategoryName() {
//		return categoryName;
//	}
   
    
    
    public Category() {
        this.categoryId = new SimpleIntegerProperty();
        this.categoryName = new SimpleStringProperty();
    }
    
    public Category(int categoryId, String categoryName) {
        this();
        setCategoryId(categoryId);
        setCategoryName(categoryName);
    }
    
    // Property getters
    public IntegerProperty categoryIdProperty() { return categoryId; }
    public StringProperty categoryNameProperty() { return categoryName; }
    
    // Getters
    public int getCategoryId() { return categoryId.get(); }
    public String getCategoryName() { return categoryName.get(); }
    
    // Setters
    public void setCategoryId(int categoryId) { this.categoryId.set(categoryId); }
    public void setCategoryName(String categoryName) { this.categoryName.set(categoryName); }
    
    @Override
    public String toString() {
        return getCategoryName();
    }
}
