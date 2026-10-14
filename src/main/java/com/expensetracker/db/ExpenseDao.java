package com.expensetracker.db;

import com.expensetracker.model.Expense;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class ExpenseDao {

    public void add(Expense e) throws SQLException {
        String sql = "INSERT INTO expenses(date,category,description,amount) VALUES(?,?,?,?)";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getDate().toString());
            ps.setString(2, e.getCategory());
            ps.setString(3, e.getDescription());
            ps.setDouble(4, e.getAmount());
            ps.executeUpdate();
        }
    }

    public void update(Expense e) throws SQLException {
        String sql = "UPDATE expenses SET date=?,category=?,description=?,amount=? WHERE id=?";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getDate().toString());
            ps.setString(2, e.getCategory());
            ps.setString(3, e.getDescription());
            ps.setDouble(4, e.getAmount());
            ps.setInt(5, e.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM expenses WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Expense> findByMonth(YearMonth ym) throws SQLException {
        String sql = "SELECT id,date,category,description,amount FROM expenses "
                + "WHERE date >= ? AND date <= ? ORDER BY date DESC, id DESC";
        List<Expense> list = new ArrayList<>();
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ym.atDay(1).toString());
            ps.setString(2, ym.atEndOfMonth().toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Expense(rs.getInt(1), LocalDate.parse(rs.getString(2)),
                            rs.getString(3), rs.getString(4), rs.getDouble(5)));
                }
            }
        }
        return list;
    }

    /** Total per category for one month (for the pie chart). */
    public Map<String, Double> categoryTotals(YearMonth ym) throws SQLException {
        String sql = "SELECT category, SUM(amount) FROM expenses "
                + "WHERE date >= ? AND date <= ? GROUP BY category ORDER BY 2 DESC";
        Map<String, Double> map = new LinkedHashMap<>();
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ym.atDay(1).toString());
            ps.setString(2, ym.atEndOfMonth().toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString(1), rs.getDouble(2));
            }
        }
        return map;
    }

    /** Total per month for a whole year; index 0 = January (for the bar chart). */
    public double[] monthlyTotals(int year) throws SQLException {
        String sql = "SELECT substr(date,6,2), SUM(amount) FROM expenses "
                + "WHERE substr(date,1,4)=? GROUP BY 1";
        double[] totals = new double[12];
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(year));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) totals[Integer.parseInt(rs.getString(1)) - 1] = rs.getDouble(2);
            }
        }
        return totals;
    }
}
