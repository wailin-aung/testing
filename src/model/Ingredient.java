package model;

public class Ingredient {
    private int id;
    private String name;
    private double quantity;
    private String unit;
    
    public Ingredient() {}
    
    public Ingredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }
    
    public Ingredient(int id, String name, double quantity, String unit) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    @Override
    public String toString() {
        return name + " (" + quantity + " " + unit + ")";
    }

	public Object getCreatedDate() {
		// TODO Auto-generated method stub
		return null;
	}
}
