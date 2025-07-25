package service;

import dao.ReportDAO;
import model.Report;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ReportService {
    private final ReportDAO reportDAO;
    
    public ReportService() {
        this.reportDAO = new ReportDAO();
    }
    
    public void logIngredientAdd(int ingredientId, String ingredientName, double quantity, String unit) {
        try {
            String newValue = ingredientName + " - " + quantity + " " + unit;
            Report report = new Report("INGREDIENT", ingredientId, ingredientName, "ADD", 
                                     "", newValue, "New ingredient added to inventory");
            reportDAO.addReport(report);
        } catch (SQLException e) {
            System.err.println("Failed to log ingredient add: " + e.getMessage());
        }
    }
    
    public void logIngredientUpdate(int ingredientId, String ingredientName, 
                                   double oldQuantity, String oldUnit,
                                   double newQuantity, String newUnit) {
        try {
            String oldValue = ingredientName + " - " + oldQuantity + " " + oldUnit;
            String newValue = ingredientName + " - " + newQuantity + " " + newUnit;
            Report report = new Report("INGREDIENT", ingredientId, ingredientName, "UPDATE", 
                                     oldValue, newValue, "Ingredient quantity updated");
            reportDAO.addReport(report);
        } catch (SQLException e) {
            System.err.println("Failed to log ingredient update: " + e.getMessage());
        }
    }
    
    public void logIngredientDelete(int ingredientId, String ingredientName, double quantity, String unit) {
        try {
            String oldValue = ingredientName + " - " + quantity + " " + unit;
            Report report = new Report("INGREDIENT", ingredientId, ingredientName, "DELETE", 
                                     oldValue, "", "Ingredient removed from inventory");
            reportDAO.addReport(report);
        } catch (SQLException e) {
            System.err.println("Failed to log ingredient delete: " + e.getMessage());
        }
    }
    
    public void logStockAdd(int stockId, String stockName, String description) {
        try {
            String newValue = stockName + " - " + description;
            Report report = new Report("STOCK", stockId, stockName, "ADD", 
                                     "", newValue, "New stock item added");
            reportDAO.addReport(report);
        } catch (SQLException e) {
            System.err.println("Failed to log stock add: " + e.getMessage());
        }
    }
    
    public void logStockUpdate(int stockId, String stockName, 
                              String oldDescription, int oldMaxServings,
                              String newDescription, int newMaxServings) {
        try {
            String oldValue = stockName + " - " + oldDescription + " (" + oldMaxServings + " servings)";
            String newValue = stockName + " - " + newDescription + " (" + newMaxServings + " servings)";
            Report report = new Report("STOCK", stockId, stockName, "UPDATE", 
                                     oldValue, newValue, "Stock availability updated");
            reportDAO.addReport(report);
        } catch (SQLException e) {
            System.err.println("Failed to log stock update: " + e.getMessage());
        }
    }
    
    public void logStockDelete(int stockId, String stockName, String description) {
        try {
            String oldValue = stockName + " - " + description;
            Report report = new Report("STOCK", stockId, stockName, "DELETE", 
                                     oldValue, "", "Stock item removed");
            reportDAO.addReport(report);
        } catch (SQLException e) {
            System.err.println("Failed to log stock delete: " + e.getMessage());
        }
    }
    
    public List<Report> getAllReports() throws SQLException {
        return reportDAO.getAllReports();
    }
    
    public List<Report> getReportsByDate(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return reportDAO.getReportsByDate(startDate, endDate);
    }
    
    public List<Report> getIngredientReports() throws SQLException {
        return reportDAO.getReportsByItemType("INGREDIENT");
    }
    
    public List<Report> getStockReports() throws SQLException {
        return reportDAO.getReportsByItemType("STOCK");
    }
    
    public void clearOldReports(int daysToKeep) throws SQLException {
        reportDAO.clearOldReports(daysToKeep);
    }
}