package dao;

import util.DatabaseConnection;
import model.Report;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {
    
    public void addReport(Report report) throws SQLException {
        String sql = "INSERT INTO reports (item_type, item_id, item_name, action, old_value, new_value, action_date, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, report.getItemType());
            pstmt.setInt(2, report.getItemId());
            pstmt.setString(3, report.getItemName());
            pstmt.setString(4, report.getAction());
            pstmt.setString(5, report.getOldValue());
            pstmt.setString(6, report.getNewValue());
            pstmt.setTimestamp(7, Timestamp.valueOf(report.getActionDate()));
            pstmt.setString(8, report.getDescription());
            pstmt.executeUpdate();
        }
    }
    
    public List<Report> getAllReports() throws SQLException {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports ORDER BY action_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Report report = new Report();
                report.setId(rs.getInt("id"));
                report.setItemType(rs.getString("item_type"));
                report.setItemId(rs.getInt("item_id"));
                report.setItemName(rs.getString("item_name"));
                report.setAction(rs.getString("action"));
                report.setOldValue(rs.getString("old_value"));
                report.setNewValue(rs.getString("new_value"));
                report.setActionDate(rs.getTimestamp("action_date").toLocalDateTime());
                report.setDescription(rs.getString("description"));
                reports.add(report);
            }
        }
        return reports;
    }
    
    public List<Report> getReportsByDate(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE action_date BETWEEN ? AND ? ORDER BY action_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Report report = new Report();
                report.setId(rs.getInt("id"));
                report.setItemType(rs.getString("item_type"));
                report.setItemId(rs.getInt("item_id"));
                report.setItemName(rs.getString("item_name"));
                report.setAction(rs.getString("action"));
                report.setOldValue(rs.getString("old_value"));
                report.setNewValue(rs.getString("new_value"));
                report.setActionDate(rs.getTimestamp("action_date").toLocalDateTime());
                report.setDescription(rs.getString("description"));
                reports.add(report);
            }
        }
        return reports;
    }
    
    public List<Report> getReportsByItemType(String itemType) throws SQLException {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE item_type = ? ORDER BY action_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemType);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Report report = new Report();
                report.setId(rs.getInt("id"));
                report.setItemType(rs.getString("item_type"));
                report.setItemId(rs.getInt("item_id"));
                report.setItemName(rs.getString("item_name"));
                report.setAction(rs.getString("action"));
                report.setOldValue(rs.getString("old_value"));
                report.setNewValue(rs.getString("new_value"));
                report.setActionDate(rs.getTimestamp("action_date").toLocalDateTime());
                report.setDescription(rs.getString("description"));
                reports.add(report);
            }
        }
        return reports;
    }
    
    public void deleteReport(int id) throws SQLException {
        String sql = "DELETE FROM reports WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    public void clearOldReports(int daysToKeep) throws SQLException {
        String sql = "DELETE FROM reports WHERE action_date < DATE_SUB(NOW(), INTERVAL ? DAY)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, daysToKeep);
            pstmt.executeUpdate();
        }
    }
}